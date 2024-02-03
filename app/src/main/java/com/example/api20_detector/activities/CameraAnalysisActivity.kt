package com.example.api20_detector.activities

import GestureHandler
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import com.example.api20_detector.CameraViewListener
import com.example.api20_detector.ColorAnalyzer
import com.example.api20_detector.Drawer
import com.example.api20_detector.MicrotubeColor
import com.example.api20_detector.R
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import java.util.Collections


class CameraAnalysisActivity : CameraActivity() {
    private var drawer = Drawer()
    private var cameraViewListener = CameraViewListener(drawer)

    private var isFirstSetAnalyzed = false
    private var isAnalysing = false
    private val allMicrotubesColors = mutableListOf<List<MicrotubeColor>>()

    private val colorAnalyzer = ColorAnalyzer()

    private val cameraBridgeViewBase by lazy { findViewById<CameraBridgeViewBase>(R.id.cameraView) }
    private val cameraButton by lazy { findViewById<Button>(R.id.cameraButton) }
    private val textView by lazy { findViewById<TextView>(R.id.numberOfMicrotubes) }

    private var gestureHandler: GestureHandler? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        setupCameraView()
        cameraButton.setOnClickListener { cameraButtonListener() }
        gestureHandler = GestureHandler(this, cameraViewListener, isAnalysing)

    }

    private fun setupCameraView() {
        if (OpenCVLoader.initDebug()) {
            cameraBridgeViewBase.apply {
                visibility = CameraBridgeViewBase.VISIBLE
                setCvCameraViewListener(cameraViewListener)
                enableView()
            }
        } else {
            Log.e("CameraActivity", "Unable to load OpenCV")
        }
    }

    private fun cameraButtonListener() {
        cameraViewListener.getLatestFrame()?.let { frame ->
            val frameForAnalysis = cameraViewListener.getFrameForAnalysis()
            when {
                !isAnalysing -> startAnalysis(frame.clone())
                !isFirstSetAnalyzed -> frameForAnalysis?.let { finishFirstAnalysis(it.clone()) }
                else -> frameForAnalysis?.let { finishAnalysis(it.clone()) }
            }
        }

        gestureHandler?.setIsAnalysing(isAnalysing);
    }

    private fun startAnalysis(frame: Mat) {
        cameraViewListener.setFrameForAnalysis(frame)
        isAnalysing = true
        textView.text = "Move the image"
        var x = 5
    }

    private fun finishFirstAnalysis(frame: Mat) {
        val microtubesRegions = drawer.getMicrotubesRegions(frame.width(), frame.height())
        val analyzedColors = colorAnalyzer.analyzeMicrotubes(frame, microtubesRegions)
        allMicrotubesColors.addAll(analyzedColors)

        isFirstSetAnalyzed = true
        isAnalysing = false
        cameraViewListener.setFrameForAnalysis(null)
        textView.text = "Microtubes 11 - 20"
    }

    private fun finishAnalysis(frame: Mat) {
        textView.text = "Analyzing microtubes, wait..."
        val microtubesRegions = drawer.getMicrotubesRegions(frame.width(), frame.height())
        val analyzedColors = colorAnalyzer.analyzeMicrotubes(frame, microtubesRegions)
        allMicrotubesColors.addAll(analyzedColors)
        gestureHandler?.reset()
        showAllMicrotubesColors()
    }


    private fun showAllMicrotubesColors() {
        val intent = Intent(this, ManualAnalysisActivity::class.java)

        allMicrotubesColors.forEachIndexed { index, microtubeColors ->
            val colorStrings = ArrayList(microtubeColors.map { it.toString() })
            intent.putStringArrayListExtra("microtube${index}Colors", colorStrings)
        }

        intent.putExtra("microtubeNumber", allMicrotubesColors.size)

        startActivity(intent)
        finish()
    }

    override fun getCameraViewList(): MutableList<out CameraBridgeViewBase> {
        return Collections.singletonList(cameraBridgeViewBase)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureHandler!!.handleTouchEvent(event!!)
        return super.onTouchEvent(event)
    }


}
