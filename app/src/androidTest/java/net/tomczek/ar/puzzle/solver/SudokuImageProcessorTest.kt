package net.tomczek.ar.puzzle.solver

import android.content.Context
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream


@RunWith(AndroidJUnit4::class)
class TextRecognitionTest {

    @Test
    fun textRecognitionTest() {

        val testData = listOf(
            arrayOf("SudokuCell_1.png", "1"),
            arrayOf("SudokuCell_2.png", "2"),
            arrayOf("SudokuCell_3.png", "3"),
            arrayOf("SudokuCell_4.png", "4"),
            arrayOf("SudokuCell_5.png", "5"),
            arrayOf("SudokuCell_6.png", "6"),
            arrayOf("SudokuCell_7.png", "7"),
            arrayOf("SudokuCell_8.png", "8"),
            arrayOf("SudokuCell_9.png", "9"),
            arrayOf("SudokuCell_empty.png", "")
        )

        testData.forEach { testDataItem ->
            val imageFileName = testDataItem[0]
            val expectedText = testDataItem[1][0]

            val ctx: Context = getInstrumentation().context
            val imageInputStream: InputStream = ctx.resources.assets.open(imageFileName)
            val bitmap = BitmapFactory.decodeStream(imageInputStream);
            val image = InputImage.fromBitmap(bitmap, 0)

            val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            val result = Tasks.await(textRecognizer.process(image))
            val trimmedResultText = result.text.trim()
            val firstCharOfRecognizedText = trimmedResultText.firstOrNull()?.toString() ?: ""
            assertEquals("$imageFileName expected '$expectedText' but found '$trimmedResultText'!", expectedText.toString(), firstCharOfRecognizedText.toString())
        }

    }

}