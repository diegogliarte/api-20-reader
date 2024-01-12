package com.example.api20_detector

enum class MicrotubeColor(val displayName: String) {
    RED("Red"),
    GREEN("Green"),
    BLUE("Blue"),
    YELLOW("Yellow"),
    ORANGE("Orange"),
    BROWN("Brown"),
    WHITE("White"),
    PINK("Pink"),
    BLACK("Black"),
    BEIGE("Beige"),
    EMPTY("Empty");
}



class Microtube(
    val index: Int,
    val positiveColors: List<MicrotubeColor>,
    val negativeColors: List<MicrotubeColor>,
    var currentColor: MicrotubeColor = MicrotubeColor.EMPTY
)