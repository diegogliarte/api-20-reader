package com.example.api20_detector

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class ColorAnalyzer {
    data class ColorBounds(val lower: Scalar, val upper: Scalar)

    fun analyzeMicrotubes(
        frame: Mat,
        microtubeRegions: List<Pair<Point, Point>>
    ): List<List<MicrotubeColor>> {
        val results = mutableListOf<List<MicrotubeColor>>()

        for ((topLeft, bottomRight) in microtubeRegions) {
            val roi = frame.submat(
                topLeft.y.toInt(),
                bottomRight.y.toInt(),
                topLeft.x.toInt(),
                bottomRight.x.toInt()
            )
            val roiHsv = Mat()
            Imgproc.cvtColor(roi, roiHsv, Imgproc.COLOR_RGB2HSV)

            val colors = getBestColors(roiHsv)
            results.add(colors)

            roi.release()
            roiHsv.release()
        }

        return results
    }

    private fun getBestColors(microtube: Mat): List<MicrotubeColor> {

        val colorBoundsMap = mapOf(
            MicrotubeColor.RED to ColorBounds(
                Scalar(0.0, 100.0, 100.0),
                Scalar(10.0, 255.0, 255.0)
            ),
            MicrotubeColor.GREEN to ColorBounds(
                Scalar(45.0, 100.0, 100.0),
                Scalar(75.0, 255.0, 255.0)
            ),
            MicrotubeColor.BLUE to ColorBounds(
                Scalar(100.0, 100.0, 100.0),
                Scalar(130.0, 255.0, 255.0)
            ),
            MicrotubeColor.YELLOW to ColorBounds(
                Scalar(15.0, 45.0, 14.0),
                Scalar(27.0, 255.0, 245.0)
            ),
            MicrotubeColor.ORANGE to ColorBounds(
                Scalar(0.0, 111.0, 173.0),
                Scalar(20.0, 240.0, 255.0)
            ),
            MicrotubeColor.BLACK to ColorBounds(Scalar(0.0, 0.0, 0.0), Scalar(255.0, 255.0, 75.0)),
            MicrotubeColor.PINK to ColorBounds(
                Scalar(161.0, 73.0, 131.0),
                Scalar(179.0, 255.0, 255.0)
            ),
            MicrotubeColor.BROWN to ColorBounds(
                Scalar(0.0, 102.0, 17.0),
                Scalar(179.0, 196.0, 73.0)
            ),
            MicrotubeColor.BEIGE to ColorBounds(
                Scalar(0.0, 0.0, 200.0),
                Scalar(180.0, 50.0, 255.0)
            ),
            MicrotubeColor.WHITE to ColorBounds(
                Scalar(0.0, 0.0, 200.0),
                Scalar(180.0, 50.0, 255.0)
            ),
        )

        return colorBoundsMap
            .map { (color, bounds) -> color to countNonZeroInBounds(microtube, bounds) }
            .filter { (_, count) -> count > calculateThreshold(microtube) }
            .sortedByDescending { (_, count) -> count }
            .map { (color, _) -> color }

    }

    private fun countNonZeroInBounds(hsvMat: Mat, bounds: ColorBounds): Int {
        val mask = Mat()
        Core.inRange(hsvMat, bounds.lower, bounds.upper, mask)
        val countNonZero = Core.countNonZero(mask)
        mask.release()
        return countNonZero
    }

    private fun calculateThreshold(hsvMat: Mat): Int {
        val microtubeArea = hsvMat.rows() * hsvMat.cols()
        return (microtubeArea * 0.15).toInt()
    }
}
