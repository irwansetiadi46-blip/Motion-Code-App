package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PresetRepository
import com.example.model.RenderConfig
import com.example.model.VideoResolution
import com.example.ui.components.JsSyntaxHighlighter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CodeMotion", appName)
  }

  @Test
  fun `preset repository has rich templates`() {
    assertTrue(PresetRepository.PRESETS.isNotEmpty())
    val particles = PresetRepository.getById("particles")
    assertNotNull(particles)
    assertTrue(particles!!.code.contains("getContext('2d')"))
  }

  @Test
  fun `render config calculates total frames correctly`() {
    val config = RenderConfig(
      resolution = VideoResolution.ALL.first { it.isDefault },
      durationSeconds = 6,
      fps = 60
    )
    assertEquals(360, config.totalFrames)
  }

  @Test
  fun `js syntax highlighter applies styles to code`() {
    val sampleCode = "const x = 42; // comment\nctx.fillRect(0, 0, W, H);"
    val highlighted = JsSyntaxHighlighter.highlight(sampleCode)
    assertTrue(highlighted.spanStyles.isNotEmpty())
  }
}
