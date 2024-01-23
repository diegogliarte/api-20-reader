import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import com.example.api20_detector.CameraViewListener
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc


class GestureHandler(
    context: Context?,
    private val cameraViewListener: CameraViewListener,
    private var isAnalysing: Boolean
) {
    private val gestureDetector: GestureDetector
    private val scaleGestureDetector: ScaleGestureDetector
    private var translateX = 0f
    private var translateY = 0f
    private var initialRotationAngle = 0f
    private var rotationAngle = 0f
    private var scale = 1.0

    init {
        gestureDetector = GestureDetector(context, GestureListener())
        scaleGestureDetector = ScaleGestureDetector(context!!, ScaleListener())
    }

    fun handleTouchEvent(event: MotionEvent) {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        if (event.pointerCount === 2) {
            when (event.actionMasked) {
                MotionEvent.ACTION_POINTER_DOWN -> initialRotationAngle =
                    calculateRotationAngle(event)

                MotionEvent.ACTION_MOVE -> {
                    val currentRotationAngle = calculateRotationAngle(event)
                    val deltaRotationAngle = currentRotationAngle - initialRotationAngle
                    rotationAngle -= deltaRotationAngle
                    initialRotationAngle = currentRotationAngle
                }
            }
        }

        Log.d("TEST", "11111111111111")
        val frame = cameraViewListener.getLatestFrame()
        Log.d("TEST", "222222222222222")
        if (isAnalysing && frame != null) {
            Log.d("TEST", "33333333333333")
            Log.d("TEST", frame.width().toString())
            Log.d("TEST", frame.height().toString())
            applyTransformations(frame)


        }
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            initialRotationAngle = 0f
            return true
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

    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scale *= detector.scaleFactor.toDouble()
            // Ensure scale is within some reasonable bounds
            scale = Math.max(0.1, Math.min(scale, 5.0))
            return true
        }
    }

    private fun calculateRotationAngle(event: MotionEvent): Float {
        val deltaX = event.getX(1) - event.getX(0)
        val deltaY = event.getY(1) - event.getY(0)
        return Math.toDegrees(Math.atan2(deltaY.toDouble(), deltaX.toDouble())).toFloat()
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

    fun setIsAnalysing(isAnalysing: Boolean) {
        this.isAnalysing = isAnalysing
    }

    fun reset() {
        translateX = 0f
        translateY = 0f
        initialRotationAngle = 0f
        rotationAngle = 0f
        scale = 1.0
    }
}