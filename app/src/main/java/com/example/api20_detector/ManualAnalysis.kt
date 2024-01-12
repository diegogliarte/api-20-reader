import android.util.Log
import com.example.api20_detector.Microtube
import com.example.api20_detector.MicrotubeColor
import com.example.api20_detector.R

class MicrotubeAnalysis(private val api20Instance: API20Instance) {

    fun toggleMicrotubeColor(microtubeIndex: Int): MicrotubeColor {
        val microtube = api20Instance.microtubes[microtubeIndex] ?: return MicrotubeColor.EMPTY

        val allColors = microtube.positiveColors + microtube.negativeColors
        val currentColor = microtube.currentColor
        val currentIndex = allColors.indexOf(currentColor)

        microtube.currentColor = if (currentIndex == -1 || currentIndex == allColors.size - 1) {
            allColors.first()
        } else {
            allColors[currentIndex + 1]
        }

        return microtube.currentColor
    }

    fun getColorResource(color: MicrotubeColor): Int {
        return when (color) {
            MicrotubeColor.RED -> R.color.red
            MicrotubeColor.BLUE -> R.color.blue
            MicrotubeColor.GREEN -> R.color.green
            MicrotubeColor.YELLOW -> R.color.yellow
            MicrotubeColor.ORANGE -> R.color.orange
            MicrotubeColor.WHITE -> R.color.gray
            MicrotubeColor.BROWN -> R.color.brown
            MicrotubeColor.PINK -> R.color.pink
            MicrotubeColor.BLACK -> R.color.dark
            MicrotubeColor.BEIGE -> R.color.beige

            else -> R.color.primary_2
        }
    }
}