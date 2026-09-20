package com.example.camera

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.view.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.sin

object VideoEncoderHelper {

    /**
     * Generates a fully compliant, playable H.264/AVC MP4 video file
     * using standard Android MediaCodec and MediaMuxer APIs.
     * Incorporates subtle anti-shake stabilization motion and live timeline graphics.
     */
    suspend fun createMp4Video(
        outputFile: File,
        baseBitmap: Bitmap,
        durationSeconds: Int,
        fps: Int = 30,
        isStabilized: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        var mediaCodec: MediaCodec? = null
        var mediaMuxer: MediaMuxer? = null
        var inputSurface: Surface? = null

        val width = 1280
        val height = 720
        val totalFrames = maxOf(1, durationSeconds) * fps
        val bitRate = 2_500_000 // 2.5 Mbps crisp H.264
        val frameDurationUs = 1_000_000L / fps

        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1) // 1 keyframe per second
            }

            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = mediaCodec.createInputSurface()
            mediaCodec.start()

            mediaMuxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var videoTrackIndex = -1
            var muxerStarted = false

            val bufferInfo = MediaCodec.BufferInfo()
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 28f
                setShadowLayer(4f, 2f, 2f, Color.BLACK)
            }
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xCC000000.toInt()
            }

            for (frameIndex in 0 until totalFrames) {
                val presentationTimeUs = frameIndex * frameDurationUs

                // Render frame onto the codec surface
                val canvas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    inputSurface.lockHardwareCanvas()
                } else {
                    inputSurface.lockCanvas(null)
                }

                if (canvas != null) {
                    try {
                        // Anti-shake stabilization simulation:
                        // If stabilized, motion is gently damped to micro-steady cinematic drift
                        val timeSec = frameIndex.toFloat() / fps
                        val driftX = if (isStabilized) sin(timeSec * 0.8f) * 6f else sin(timeSec * 3.5f) * 28f
                        val driftY = if (isStabilized) sin(timeSec * 0.5f) * 4f else sin(timeSec * 2.8f) * 22f

                        // Draw background scene
                        canvas.drawBitmap(
                            baseBitmap,
                            Rect(0, 0, baseBitmap.width, baseBitmap.height),
                            Rect(driftX.toInt(), driftY.toInt(), width + driftX.toInt(), height + driftY.toInt()),
                            paint
                        )

                        // Draw modern video HUD element
                        val currentSec = frameIndex / fps
                        val minutes = currentSec / 60
                        val seconds = currentSec % 60
                        val timeStr = String.format("%02d:%02d / %02d:%02d", minutes, seconds, durationSeconds / 60, durationSeconds % 60)

                        // Stabilizer & Time badge
                        canvas.drawRoundRect(24f, 24f, 420f, 76f, 16f, 16f, badgePaint)
                        // Red recording dot
                        paint.color = Color.RED
                        canvas.drawCircle(50f, 50f, 8f, paint)
                        canvas.drawText(timeStr, 75f, 58f, textPaint)

                        if (isStabilized) {
                            textPaint.textSize = 20f
                            textPaint.color = 0xFF00E5FF.toInt()
                            canvas.drawText("SUPER STEADY OIS/EIS", 230f, 58f, textPaint)
                            textPaint.textSize = 28f
                            textPaint.color = Color.WHITE
                        }
                    } finally {
                        inputSurface.unlockCanvasAndPost(canvas)
                    }
                }

                // Drain output from encoder to muxer
                while (true) {
                    val outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10_000)
                    if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break
                    } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (muxerStarted) {
                            throw RuntimeException("Format changed after muxer started")
                        }
                        val newFormat = mediaCodec.outputFormat
                        videoTrackIndex = mediaMuxer.addTrack(newFormat)
                        mediaMuxer.start()
                        muxerStarted = true
                    } else if (outputBufferIndex >= 0) {
                        val encodedData = mediaCodec.getOutputBuffer(outputBufferIndex)
                        if (encodedData != null) {
                            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                bufferInfo.size = 0
                            }
                            if (bufferInfo.size != 0 && muxerStarted) {
                                encodedData.position(bufferInfo.offset)
                                encodedData.limit(bufferInfo.offset + bufferInfo.size)
                                bufferInfo.presentationTimeUs = presentationTimeUs
                                mediaMuxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                            }
                        }
                        mediaCodec.releaseOutputBuffer(outputBufferIndex, false)
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            break
                        }
                    }
                }
            }

            // Signal End of Stream
            mediaCodec.signalEndOfInputStream()

            // Drain remaining frames
            var eosReached = false
            var drainAttempts = 0
            while (!eosReached && drainAttempts < 50) {
                drainAttempts++
                val outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 20_000)
                if (outputBufferIndex >= 0) {
                    val encodedData = mediaCodec.getOutputBuffer(outputBufferIndex)
                    if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        mediaMuxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                    }
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        eosReached = true
                    }
                } else if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (eosReached) break
                }
            }

            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            try {
                mediaCodec?.stop()
                mediaCodec?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                mediaMuxer?.stop()
                mediaMuxer?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                inputSurface?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
