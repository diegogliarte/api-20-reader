package com.example.api20_detector.activities

import API20Instance
import MicrotubeAnalysis
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.api20_detector.AnalysisHistoryManager
import com.example.api20_detector.MicrotubeColor
import com.example.api20_detector.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.properties.Delegates

class ManualAnalysisActivity : AppCompatActivity() {
    private lateinit var analysis: MicrotubeAnalysis
    private lateinit var currentInstance: API20Instance
    private val microtubeIndexMap = mutableMapOf<Int, Int>()
    private lateinit var analysisHistoryManager: AnalysisHistoryManager
    private lateinit var historyUUID: String

    private lateinit var titleInput: EditText
    private lateinit var notesInput: EditText
    private lateinit var codeTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual)

        toolbarSetup()
        initializeDependencies()
        setupUIInteractions()
    }

    private fun toolbarSetup() {
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initializeDependencies() {
        analysisHistoryManager = AnalysisHistoryManager.getInstance(this)
        titleInput = findViewById(R.id.titleInput)
        notesInput = findViewById(R.id.notesInput)
        codeTextView = findViewById(R.id.code)

        initializeMicrotubeIndexMap()
        setupAnalysisInstance()
        handleReceivedColors()
        historyUUID = intent.getStringExtra("historyUUID") ?: UUID.randomUUID().toString()
    }

    private fun setupUIInteractions() {
        findViewById<View>(R.id.mainLayout).setOnTouchListener { _, _ ->
            hideSoftKeyboard()
            false
        }
        updateCodeTextView()
    }


    private fun initializeMicrotubeIndexMap() {
        for (i in 1..21) {
            val idName = "microtube$i"
            val resId = resources.getIdentifier(idName, "id", packageName)
            if (resId != 0) {
                microtubeIndexMap[resId] = i
            }
        }
    }

    private fun setupAnalysisInstance() {
        val instanceName = intent.getStringExtra("api20name") ?: "API20E"
        currentInstance = API20Factory.getInstance(instanceName)
            ?: throw IllegalStateException("API20 instance not found")
        analysis = MicrotubeAnalysis(currentInstance)
    }

    private fun handleReceivedColors() {
        val microtubeNumber = intent.getIntExtra("microtubeNumber", 0)
        for (i in 0..microtubeNumber) {
            val colorNames = intent.getStringArrayListExtra("microtube${i}Colors") ?: continue

            val microtube = currentInstance.microtubes[i + 1]
            val allowedColors = microtube?.positiveColors.orEmpty() + microtube?.negativeColors.orEmpty()
            val allowedColorNames = allowedColors.map { it.name }

            val bestColorName = colorNames
                .filter { it in allowedColorNames }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }?.key

            val bestColor = bestColorName?.let { enumValueOf<MicrotubeColor>(it) }

            if (bestColor != null) {
                handleMicrotubeUpdate(i + 1, bestColor)
            }
        }
    }

    private fun handleMicrotubeUpdate(microtubeIndex: Int, color: MicrotubeColor? = null) {
        val actualColor = color ?: analysis.toggleMicrotubeColor(microtubeIndex)
        val microtubeId = resources.getIdentifier("microtube$microtubeIndex", "id", packageName)
        val microtubeView = findViewById<View>(microtubeId)

        val frameLayout = microtubeView.parent as? FrameLayout
        val signTextViewId = resources.getIdentifier("signTextView$microtubeIndex", "id", packageName)
        val signTextView = frameLayout?.findViewById<TextView>(signTextViewId)

        if (actualColor == MicrotubeColor.WHITE) {
            microtubeView.backgroundTintList = null
            microtubeView.setBackgroundResource(R.drawable.microtube_white)
        } else {
            val newColor = analysis.getColorResource(actualColor)
            microtubeView.backgroundTintList = ContextCompat.getColorStateList(this, newColor)
        }

        signTextView?.text = if (actualColor in (currentInstance.microtubes[microtubeIndex]?.positiveColors ?: emptySet())) "+" else "−"
        currentInstance.microtubes[microtubeIndex]?.currentColor = actualColor

        updateCodeTextView()
    }

    fun saveHistory(view: View) {
        val title = titleInput.text.toString()
        val notes = notesInput.text.toString()
        val code = codeTextView.text.toString()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                analysisHistoryManager.saveAnalysisHistory(historyUUID, title, notes, code)
                launch(Dispatchers.Main) {
                    Toast.makeText(this@ManualAnalysisActivity, "History saved successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@ManualAnalysisActivity, "Failed to save history: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun updateCodeTextView() {
        val codeTextView = findViewById<TextView>(R.id.code)
        val code = StringBuilder()

        for (i in 1..21 step 3) {
            var sum = 0
            for (j in 0..2) {
                val microtubeIndex = i + j
                val microtube = currentInstance.microtubes[microtubeIndex]
                if (microtube?.positiveColors?.contains(microtube.currentColor) == true) {
                    sum += getNumberForMicrotube(microtubeIndex)
                }
            }
            code.append(sum)
        }

        codeTextView.text = code.toString()
    }

    private fun getNumberForMicrotube(index: Int): Int {
        return when (index % 3) {
            1 -> 1
            2 -> 2
            0 -> 4
            else -> 0
        }
    }

    fun onMicrotubeClick(view: View) {
        val microtubeIndex = microtubeIndexMap[view.id] ?: return
        handleMicrotubeUpdate(microtubeIndex)
    }

    private fun hideSoftKeyboard() {
        (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)?.let {
            currentFocus?.let { view ->
                it.hideSoftInputFromWindow(view.windowToken, 0)
                view.clearFocus()
            }
        }
    }
}