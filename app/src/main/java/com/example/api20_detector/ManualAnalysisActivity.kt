package com.example.api20_detector

import API20Instance
import MicrotubeAnalysis
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.log

class ManualAnalysisActivity : AppCompatActivity() {
    private lateinit var analysis: MicrotubeAnalysis
    private lateinit var currentInstance: API20Instance
    private val microtubeIndexMap = mutableMapOf<Int, Int>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual)
        initializeMicrotubeIndexMap()
        setupAnalysisInstance()
        handleReceivedColors()
        updateCodeTextView()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
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
        val colorNames = intent.getStringArrayListExtra("microtubesColors") ?: listOf()
        val colors = convertStringsToMicrotubeColors(colorNames)
        if (colors.isNotEmpty()) {
            fillMicrotubesWithReceivedColors(colors)
        }
    }

    private fun fillMicrotubesWithReceivedColors(colors: List<MicrotubeColor>) {
        for (i in colors.indices) {
            updateMicrotubeView(i + 1, colors[i])
        }
    }

    private fun updateMicrotubeView(microtubeIndex: Int, color: MicrotubeColor) {
        handleMicrotubeUpdate(microtubeIndex, color)
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


    private fun convertStringsToMicrotubeColors(colorNames: List<String>): List<MicrotubeColor> {
        return colorNames.mapNotNull { colorName ->
            try {
                MicrotubeColor.valueOf(colorName)
            } catch (e: IllegalArgumentException) {
                null
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
}