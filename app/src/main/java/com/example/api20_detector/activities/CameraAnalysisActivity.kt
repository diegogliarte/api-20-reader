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
    private val drawer = Drawer()
    private val cameraViewListener = CameraViewListener(drawer)
    private var isAnalysing = false
    private val allMicrotubesColors = mutableListOf<List<MicrotubeColor>>()
    private val colorAnalyzer = ColorAnalyzer()

    // Using lazy initialization to ensure views are only loaded once needed
    private val cameraBridgeViewBase by lazy { findViewById<CameraBridgeViewBase>(R.id.cameraView) }
    private val cameraButton by lazy { findViewById<Button>(R.id.cameraButton) }
    private val textView by lazy { findViewById<TextView>(R.id.numberOfMicrotubes) }

    // Nullable type with late initialization
    private var gestureHandler: GestureHandler? = null

    private var isFirstSetAnalyzed = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        if (OpenCVLoader.initDebug()) {
            setupCameraView()
        } else {
            Log.e("CameraActivity", "Unable to load OpenCV")
        }

        setupCameraView()
        cameraButton.setOnClickListener { cameraButtonListener() }
        gestureHandler = GestureHandler(this, cameraViewListener, isAnalysing)

    }

    private fun setupCameraView() {
        cameraBridgeViewBase.apply {
            visibility = CameraBridgeViewBase.VISIBLE
            setCvCameraViewListener(cameraViewListener)
            enableView()
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

        gestureHandler?.setIsAnalysing(isAnalysing)
    }

    private fun startAnalysis(frame: Mat) {
        isAnalysing = true
        cameraViewListener.setFrameForAnalysis(frame)
        textView.setText(R.string.move_image)
    }

    private fun finishFirstAnalysis(frame: Mat) {
        val microtubesRegions = drawer.getMicrotubesRegions(frame.width(), frame.height())
        val analyzedColors = colorAnalyzer.analyzeMicrotubes(frame, microtubesRegions)
        allMicrotubesColors.addAll(analyzedColors)

        isFirstSetAnalyzed = true
        isAnalysing = false
        cameraViewListener.setFrameForAnalysis(null)
        textView.setText(R.string.strips_11_20)
    }

    private fun finishAnalysis(frame: Mat) {
        textView.setText(R.string.analyzing_strips)
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
