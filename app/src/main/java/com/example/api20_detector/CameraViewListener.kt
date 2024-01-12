package com.example.api20_detector

import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class CameraViewListener(private val drawer: Drawer) : CameraBridgeViewBase.CvCameraViewListener2 {
    private var latestFrame: Mat? = null

    fun getLatestFrame(): Mat? = latestFrame


    override fun onCameraViewStarted(width: Int, height: Int) {
    }

    override fun onCameraViewStopped() {
        latestFrame?.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val frame = inputFrame.rgba()

        latestFrame?.release()
        latestFrame = frame.clone()
        return drawer.draw(frame)
    }

}
