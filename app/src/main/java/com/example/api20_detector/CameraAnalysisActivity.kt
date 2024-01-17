package com.example.api20_detector

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import java.util.Collections

class CameraAnalysisActivity : CameraActivity() {
    private lateinit var cameraBridgeViewBase: CameraBridgeViewBase
    private var drawer = Drawer()
    private var cameraViewListener = CameraViewListener(drawer)

    private var isFirstSetAnalyzed = false
    private val allMicrotubesColors = mutableListOf<List<MicrotubeColor>>()

    private val colorAnalyzer = ColorAnalyzer()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        cameraBridgeViewBase = findViewById(R.id.cameraView)
        cameraBridgeViewBase.visibility = CameraBridgeViewBase.VISIBLE
        cameraBridgeViewBase.setCvCameraViewListener(cameraViewListener)

        if (OpenCVLoader.initDebug()) {
            cameraBridgeViewBase.enableView()
        } else {
            Log.e("CameraActivity", "Unable to load OpenCV")
        }

        val textView: TextView = findViewById(R.id.numberOfMicrotubes)
        val cameraButton: Button = findViewById(R.id.cameraButton)
        cameraButton.setOnClickListener {
            cameraViewListener.getLatestFrame()?.let { frame ->
                val microtubesRegions = drawer.getMicrotubesRegions(frame.width(), frame.height())
                val analyzedColors = colorAnalyzer.analyzeMicrotubes(frame, microtubesRegions)
                allMicrotubesColors.addAll(analyzedColors)

                if (isFirstSetAnalyzed) {
                    showAllMicrotubesColors()
                } else {
                    isFirstSetAnalyzed = true
                    textView.text = "Microtubes 11 - 20"
                }
            }
        }

    }

    private fun showAllMicrotubesColors() {
        val intent = Intent(this, ManualAnalysisActivity::class.java)

        allMicrotubesColors.forEachIndexed { index, microtubeColors  ->
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

}
