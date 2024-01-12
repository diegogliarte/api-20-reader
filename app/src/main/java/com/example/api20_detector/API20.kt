import com.example.api20_detector.Microtube
import com.example.api20_detector.MicrotubeColor

class API20Instance(val name: String, val microtubes: Map<Int, Microtube>) {
    fun getScore(microtubeIndex: Int, detectedColor: MicrotubeColor): Int {
        val microtube = microtubes[microtubeIndex] ?: return 0
        return when {
            detectedColor in microtube.positiveColors -> microtube.index
            detectedColor in microtube.negativeColors -> -1
            else -> 0
        }
    }
}


object API20Factory {
    private val instances = mapOf(
        "API20E" to API20Instance(
            "API20E",
            mapOf(
                1 to Microtube(1, listOf(MicrotubeColor.YELLOW), listOf(MicrotubeColor.WHITE)),
                2 to Microtube(2, listOf(MicrotubeColor.PINK), listOf(MicrotubeColor.YELLOW)),
                3 to Microtube(3, listOf(MicrotubeColor.ORANGE), listOf(MicrotubeColor.YELLOW)),
                4 to Microtube(4, listOf(MicrotubeColor.ORANGE), listOf(MicrotubeColor.YELLOW)),
                5 to Microtube(5, listOf(MicrotubeColor.BLUE), listOf(MicrotubeColor.YELLOW)),
                6 to Microtube(6, listOf(MicrotubeColor.BLACK), listOf(MicrotubeColor.YELLOW)),
                7 to Microtube(7, listOf(MicrotubeColor.PINK), listOf(MicrotubeColor.YELLOW)),
                8 to Microtube(8, listOf(MicrotubeColor.BROWN), listOf(MicrotubeColor.YELLOW)),
                9 to Microtube(9, listOf(MicrotubeColor.ORANGE), listOf(MicrotubeColor.YELLOW)),
                10 to Microtube(10, listOf(MicrotubeColor.BEIGE), listOf(MicrotubeColor.WHITE)),

                11 to Microtube(11, listOf(MicrotubeColor.BLACK), listOf(MicrotubeColor.WHITE)),
                12 to Microtube(12, listOf(MicrotubeColor.YELLOW), listOf(MicrotubeColor.BLUE, MicrotubeColor.GREEN)),
                13 to Microtube(13, listOf(MicrotubeColor.YELLOW), listOf(MicrotubeColor.BLUE, MicrotubeColor.GREEN)),
                14 to Microtube(14, listOf(MicrotubeColor.YELLOW), listOf(MicrotubeColor.BLUE, MicrotubeColor.GREEN)),
                15 to Microtube(15, listOf(MicrotubeColor.YELLOW), listOf(MicrotubeColor.BLUE, MicrotubeColor.GREEN)),
                16 to Microtube(16, listOf(MicrotubeColor.YELLOW), listOf(MicrotubeColor.BLUE, MicrotubeColor.GREEN)),
                17 to Microtube(17, listOf(MicrotubeColor.YELLOW), listOf(MicrotubeColor.BLUE, MicrotubeColor.GREEN)),
                18 to Microtube(18, listOf(MicrotubeColor.YELLOW), listOf(MicrotubeColor.BLUE, MicrotubeColor.GREEN)),
                19 to Microtube(19, listOf(MicrotubeColor.YELLOW), listOf(MicrotubeColor.BLUE, MicrotubeColor.GREEN)),
                20 to Microtube(20, listOf(MicrotubeColor.YELLOW), listOf(MicrotubeColor.BLUE, MicrotubeColor.GREEN)),

                21 to Microtube(21, listOf(MicrotubeColor.GREEN), listOf(MicrotubeColor.RED))
            )
        )
    )

    fun getInstance(name: String): API20Instance? = instances[name]

}