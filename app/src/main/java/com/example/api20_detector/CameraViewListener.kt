package com.example.api20_detector

import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat

class CameraViewListener(private val drawer: Drawer) : CameraBridgeViewBase.CvCameraViewListener2 {
    @Volatile private var latestFrame: Mat? = null
    @Volatile private var frameForAnalysis: Mat? = null

    fun getLatestFrame(): Mat? = synchronized(this) {
        return latestFrame
    }

    fun setFrameForAnalysis(frame: Mat?) {
        synchronized(this) {
            frameForAnalysis?.release()
            frameForAnalysis = frame
        }
    }

    fun getFrameForAnalysis(): Mat? = synchronized(this) {
        return frameForAnalysis
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
            if (frameForAnalysis != null) {
                val frame = frameForAnalysis!!
                return drawer.draw(frame)
            }

            val rgbaFrame = inputFrame.rgba()
            latestFrame?.release()
            latestFrame = rgbaFrame
            return drawer.draw(rgbaFrame.clone())
        }
    }
}
