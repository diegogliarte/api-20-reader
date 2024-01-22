package com.example.api20_detector

import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat

class CameraViewListener(private val drawer: Drawer) : CameraBridgeViewBase.CvCameraViewListener2 {
    @Volatile private var latestFrame: Mat? = null
    @Volatile private var frameForAnalysis: Mat? = null

    fun getLatestFrame(): Mat? = synchronized(this) {
        latestFrame?.clone()
    }

    fun setFrameForAnalysis(frame: Mat?) {
        synchronized(this) {
            frameForAnalysis?.release()
            frameForAnalysis = frame?.clone()
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        // ...
    }

    override fun onCameraViewStopped() {
        synchronized(this) {
            latestFrame?.release()
            frameForAnalysis?.release()
        }
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        synchronized(this) {
            latestFrame?.release()
            latestFrame = inputFrame.rgba()
            return drawer.draw(latestFrame!!)
        }
    }
}
