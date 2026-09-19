package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.camera.CameraFilter
import com.example.camera.ExportProfile
import com.example.camera.ImageProcessor
import com.example.camera.PhotoEditAdjustments
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kamera", appName)
  }

  @Test
  fun `test image processor sample generation and adjustments`() = kotlinx.coroutines.runBlocking {
    val sample = ImageProcessor.createSampleSceneBitmap(200, 200)
    assertNotNull(sample)
    assertEquals(200, sample.width)
    assertEquals(200, sample.height)

    val adjusted = ImageProcessor.applyAdjustments(
      sample,
      PhotoEditAdjustments(
        brightness = 15f,
        contrast = 20f,
        selectedFilter = CameraFilter.CINEMATIC
      )
    )
    assertNotNull(adjusted)
    assertEquals(200, adjusted.width)
  }

  @Test
  fun `test night mode processor`() = kotlinx.coroutines.runBlocking {
    val sample = ImageProcessor.createSampleSceneBitmap(200, 200)
    val nightProcessed = ImageProcessor.processNightMode(sample)
    assertNotNull(nightProcessed)
  }

  @Test
  fun `test export profile presets`() {
    assertEquals(4, ExportProfile.entries.size)
    assertTrue(ExportProfile.PRINT_ULTRA_HD.targetWidth >= 3840)
  }
}

