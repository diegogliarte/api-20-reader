package com.example.api20_detector

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Button
import android.widget.TextView
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import java.lang.Double.max
import java.lang.Double.min
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

    private lateinit var gestureDetector: GestureDetector
    private var translateX = 0f
    private var translateY = 0f

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scale = 1.0
    private var initialRotationAngle = 0f

    private var rotationAngle = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        setupCameraView()
        cameraButton.setOnClickListener { cameraButtonListener() }
        gestureDetector = GestureDetector(this, GestureListener())
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
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
                !isAnalysing -> startAnalysis(frame)
                !isFirstSetAnalyzed -> frameForAnalysis?.let { finishFirstAnalysis(it) }
                else -> frameForAnalysis?.let { finishAnalysis(it) }
            }
        }
    }

    private fun startAnalysis(frame: Mat) {
        cameraViewListener.setFrameForAnalysis(frame)
        isAnalysing = true
        textView.text = "Move the image"
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
        val microtubesRegions = drawer.getMicrotubesRegions(frame.width(), frame.height())
        val analyzedColors = colorAnalyzer.analyzeMicrotubes(frame, microtubesRegions)
        allMicrotubesColors.addAll(analyzedColors)
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


    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            initialRotationAngle = 0f
            return super.onDown(e)
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (isAnalysing) {
                translateX -= distanceX
                translateY -= distanceY
            }
            return true
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        val frame = cameraViewListener.getLatestFrame()
        if (event.pointerCount == 2) {
            when (event.actionMasked) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    initialRotationAngle = calculateRotationAngle(event)
                }
                MotionEvent.ACTION_MOVE -> {
                    val currentRotationAngle = calculateRotationAngle(event)
                    val deltaRotationAngle = currentRotationAngle - initialRotationAngle
                    rotationAngle -= deltaRotationAngle
                    initialRotationAngle = currentRotationAngle
                }
            }
        }

        if (isAnalysing && frame != null) {
            applyTransformations(frame)
        }

        return super.onTouchEvent(event)
    }

    private fun calculateRotationAngle(event: MotionEvent): Float {
        val deltaX = (event.getX(1) - event.getX(0)).toDouble()
        val deltaY = (event.getY(1) - event.getY(0)).toDouble()
        return Math.toDegrees(Math.atan2(deltaY, deltaX)).toFloat()
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scale *= detector.scaleFactor
            // Ensure scale is within some reasonable bounds
            scale = max(0.1, min(scale, 5.0))
            return true
        }
    }

    private fun applyTransformations(frame: Mat) {
        // Create a matrix for translation
        val translationMatrix = Mat.zeros(2, 3, CvType.CV_32F)
        translationMatrix.put(0, 0, 1.0, 0.0, translateX.toDouble())
        translationMatrix.put(1, 0, 0.0, 1.0, translateY.toDouble())

        // Translate the frame
        val translatedFrame = Mat()
        Imgproc.warpAffine(
            frame,
            translatedFrame,
            translationMatrix,
            frame.size()
        )

        // Create a matrix for rotation and scaling
        val rotationMatrix = Imgproc.getRotationMatrix2D(
            Point(frame.cols() / 2.0, frame.rows() / 2.0),
            rotationAngle.toDouble(),
            scale
        )

        // Apply rotation and scaling
        val transformedFrame = Mat()
        Imgproc.warpAffine(
            translatedFrame,
            transformedFrame,
            rotationMatrix,
            frame.size()
        )

        cameraViewListener.setFrameForAnalysis(transformedFrame)
    }

}
