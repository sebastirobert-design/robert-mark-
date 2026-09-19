package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CceGradeEvaluator
import org.junit.Assert.assertEquals
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
    assertEquals("மதிப்பெண் பதிவேடு", appName)
  }

  @Test
  fun `test cce grading logic for class 1 to 3`() {
    assertEquals("A", CceGradeEvaluator.getGradeForClass1To3(85))
    assertEquals("B", CceGradeEvaluator.getGradeForClass1To3(65))
    assertEquals("C", CceGradeEvaluator.getGradeForClass1To3(45))
    assertEquals("D", CceGradeEvaluator.getGradeForClass1To3(35))
    assertEquals("E", CceGradeEvaluator.getGradeForClass1To3(15))
  }

  @Test
  fun `test cce grading logic for class 4 to 8`() {
    assertEquals("A", CceGradeEvaluator.getGradeForClass4To8(95))
    assertEquals("B", CceGradeEvaluator.getGradeForClass4To8(75))
    assertEquals("C", CceGradeEvaluator.getGradeForClass4To8(55))
    assertEquals("D", CceGradeEvaluator.getGradeForClass4To8(38))
    assertEquals("E", CceGradeEvaluator.getGradeForClass4To8(18))
  }
}
