package com.example.api20_detector

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class ColorAnalyzer {
    fun analyzeMicrotubes(frame: Mat, flaskRegions: List<Pair<Point, Point>>): List<MicrotubeColor> {
        val results = mutableListOf<MicrotubeColor>()

        for ((topLeft, bottomRight) in flaskRegions) {
            val roi = frame.submat(topLeft.y.toInt(), bottomRight.y.toInt(), topLeft.x.toInt(), bottomRight.x.toInt())
            val roiHsv = Mat()
            Imgproc.cvtColor(roi, roiHsv, Imgproc.COLOR_RGB2HSV)

            val color = categorizeColor(roiHsv)
            results.add(color)

            roi.release()
            roiHsv.release()
        }

        return results
    }

    private fun categorizeColor(flask: Mat): MicrotubeColor {
        val RED_LOWER = Scalar(0.0, 100.0, 100.0)
        val RED_UPPER = Scalar(10.0, 255.0, 255.0)
        val GREEN_LOWER = Scalar(45.0, 100.0, 100.0)
        val GREEN_UPPER = Scalar(75.0, 255.0, 255.0)
        val BLUE_LOWER = Scalar(100.0, 100.0, 100.0)
        val BLUE_UPPER = Scalar(130.0, 255.0, 255.0)
        val YELLOW_LOWER = Scalar(15.0, 45.0, 14.0)
        val YELLOW_UPPER = Scalar(27.0, 255.0, 245.0)
        val ORANGE_LOWER = Scalar(10.0, 100.0, 100.0)
        val ORANGE_UPPER = Scalar(20.0, 255.0, 255.0)
        val DARK_LOWER = Scalar(0.0, 0.0, 0.0)
        val DARK_UPPER = Scalar(180.0, 255.0, 50.0)
        val WHITE_LOWER = Scalar(0.0, 0.0, 200.0)
        val WHITE_UPPER = Scalar(180.0, 50.0, 255.0)
        val PINK_LOWER = Scalar(140.0, 100.0, 100.0)
        val PINK_UPPER = Scalar(170.0, 255.0, 255.0)
        val BROWN_LOWER = Scalar(0.0, 100.0, 100.0)
        val BROWN_UPPER = Scalar(10.0, 255.0, 255.0)
        val BEIGE_LOWER = Scalar(0.0, 0.0, 200.0)
        val BEIGE_UPPER = Scalar(180.0, 50.0, 255.0)

        return when {
            isColorPresent(flask, RED_LOWER, RED_UPPER) -> MicrotubeColor.RED
            isColorPresent(flask, GREEN_LOWER, GREEN_UPPER) -> MicrotubeColor.GREEN
            isColorPresent(flask, BLUE_LOWER, BLUE_UPPER) -> MicrotubeColor.BLUE
            isColorPresent(flask, YELLOW_LOWER, YELLOW_UPPER) -> MicrotubeColor.YELLOW
            isColorPresent(flask, ORANGE_LOWER, ORANGE_UPPER) -> MicrotubeColor.ORANGE
            isColorPresent(flask, DARK_LOWER, DARK_UPPER) -> MicrotubeColor.BLACK
            isColorPresent(flask, PINK_LOWER, PINK_UPPER) -> MicrotubeColor.PINK
            isColorPresent(flask, BROWN_LOWER, BROWN_UPPER) -> MicrotubeColor.BROWN
            isColorPresent(flask, BEIGE_LOWER, BEIGE_UPPER) -> MicrotubeColor.BEIGE
            isColorPresent(flask, WHITE_LOWER, WHITE_UPPER) -> MicrotubeColor.WHITE
            else -> MicrotubeColor.EMPTY
        }
    }

    private fun isColorPresent(hsvMat: Mat, lowerBound: Scalar, upperBound: Scalar): Boolean {
        val mask = Mat()
        Core.inRange(hsvMat, lowerBound, upperBound, mask)
        val countNonZero = Core.countNonZero(mask)
        val flaskArea = hsvMat.rows() * hsvMat.cols()
        val threshold = flaskArea * 0.15

        mask.release()
        return countNonZero > threshold
    }
}
