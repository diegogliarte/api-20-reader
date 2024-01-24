package com.example.api20_detector

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class Drawer {

    fun draw(frame: Mat): Mat {
        val frameWidth = frame.width()
        val frameHeight = frame.height()

        val sideMarginWidth = frameWidth * 0.2 // 20% of frame width for each side margin
        val microtubeWidth = frameWidth * 0.05 // 5% of frame width for each microtube
        val microtubeHeight = frameHeight * 0.25 // 25% of frame height for each microtube
        val cornerRadius = 35 // corner radius for the rounded rectangles

        // Calculate total width of all microtubes and spaces to evenly distribute them
        val totalMicrotubesWidth = microtubeWidth * 10
        val spaceAvailable =
            frameWidth - totalMicrotubesWidth - (2 * sideMarginWidth) // subtract margins from available space
        val spaceBetweenMicrotubes = spaceAvailable / 11 // 11 spaces including sides

        // Calculate the starting x position for the first microtube
        var currentX = sideMarginWidth + spaceBetweenMicrotubes

        // Define the y position for the top and bottom of the microtubes (centered vertically)
        val topY = (frameHeight - microtubeHeight) / 2
        val bottomY = topY + microtubeHeight

        // Color and thickness for the rounded rectangles
        val lineColor = Scalar(144.0, 180.0, 147.0)
        val thickness = 3

        // Draw the microtubes
        for (i in 1..10) {
            val topLeft = Point(currentX, topY)
            val bottomRight = Point(currentX + microtubeWidth, bottomY)
            drawRoundedRectangle(frame, topLeft, bottomRight, lineColor, thickness, cornerRadius)

            // Move to the x position for the next microtube
            currentX += microtubeWidth + spaceBetweenMicrotubes
        }

//        val regions = getMicrotubesRegions(frameWidth, frameHeight)
//        for (i in 0..9) {
//            Imgproc.rectangle(frame, regions[i].first, regions[i].second, Scalar(
//                255.0, 255.0, 0.0, 255.0
//            ))
//        }

        return frame
    }

    private fun cutTransparentHoles(mask: Mat, regions: List<Pair<Point, Point>>) {
        val transparent = Scalar(0.0, 0.0, 0.0, 0.0)
1
        for ((topLeft, bottomRight) in regions) {
            Imgproc.rectangle(mask, topLeft, bottomRight, transparent, -1)
        }
    }

    private fun overlayMaskOnFrame(frame: Mat, mask: Mat) {
        Core.addWeighted(frame, 1.0, mask, 0.5, 0.0, frame)
    }

    private fun drawRoundedRectangle(
        src: Mat,
        topLeft: Point,
        bottomRight: Point,
        lineColor: Scalar,
        thickness: Int,
        cornerRadius: Int
    ) {
        // Calculate points for rectangle sides
        val p1 = topLeft
        val p2 = Point(bottomRight.x, topLeft.y)
        val p3 = bottomRight
        val p4 = Point(topLeft.x, bottomRight.y)

        // Draw straight lines
        Imgproc.line(
            src,
            Point(p1.x + cornerRadius, p1.y),
            Point(p2.x - cornerRadius, p2.y),
            lineColor,
            thickness
        )
        Imgproc.line(
            src,
            Point(p2.x, p2.y + cornerRadius),
            Point(p3.x, p3.y - cornerRadius),
            lineColor,
            thickness
        )
        Imgproc.line(
            src,
            Point(p4.x + cornerRadius, p4.y),
            Point(p3.x - cornerRadius, p3.y),
            lineColor,
            thickness
        )
        Imgproc.line(
            src,
            Point(p1.x, p1.y + cornerRadius),
            Point(p4.x, p4.y - cornerRadius),
            lineColor,
            thickness
        )

        val cornerRadiusDouble = cornerRadius.toDouble()
        // Draw arcs for corners
        Imgproc.ellipse(
            src,
            Point(p1.x + cornerRadius, p1.y + cornerRadius),
            Size(cornerRadiusDouble, cornerRadiusDouble),
            180.0,
            0.0,
            90.0,
            lineColor,
            thickness
        )
        Imgproc.ellipse(
            src,
            Point(p2.x - cornerRadius, p2.y + cornerRadius),
            Size(cornerRadiusDouble, cornerRadiusDouble),
            270.0,
            0.0,
            90.0,
            lineColor,
            thickness
        )
        Imgproc.ellipse(
            src,
            Point(p3.x - cornerRadius, p3.y - cornerRadius),
            Size(cornerRadiusDouble, cornerRadiusDouble),
            0.0,
            0.0,
            90.0,
            lineColor,
            thickness
        )
        Imgproc.ellipse(
            src,
            Point(p4.x + cornerRadius, p4.y - cornerRadius),
            Size(cornerRadiusDouble, cornerRadiusDouble),
            90.0,
            0.0,
            90.0,
            lineColor,
            thickness
        )
    }

    fun getMicrotubesRegions(frameWidth: Int, frameHeight: Int): List<Pair<Point, Point>> {
        val regions = mutableListOf<Pair<Point, Point>>()

        val sideMarginWidth = frameWidth * 0.2
        val microtubeWidth = frameWidth * 0.05
        val microtubeHeight = frameHeight * 0.25
        val totalMicrotubesWidth = microtubeWidth * 10
        val spaceAvailable = frameWidth - totalMicrotubesWidth - (2 * sideMarginWidth)
        val spaceBetweenMicrotubes = spaceAvailable / 11
        var currentX = sideMarginWidth + spaceBetweenMicrotubes
        val topY = (frameHeight - microtubeHeight) / 2
        val bottomY = topY + microtubeHeight

        for (i in 1..10) {
            val topLeft = Point(currentX + microtubeWidth / 3, topY + microtubeHeight / 2)
            val bottomRight = Point(currentX + microtubeWidth -  microtubeWidth / 3, bottomY - 20)
            regions.add(Pair(topLeft, bottomRight))
            currentX += microtubeWidth + spaceBetweenMicrotubes
        }

        return regions
    }
}
