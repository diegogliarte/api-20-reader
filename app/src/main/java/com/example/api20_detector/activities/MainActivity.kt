package com.example.api20_detector.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.api20_detector.R

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Setting up click listeners in a more concise way
        findViewById<LinearLayout>(R.id.cameraModeSelector).setOnClickListener(this)
        findViewById<LinearLayout>(R.id.manualModeSelector).setOnClickListener(this)
        findViewById<LinearLayout>(R.id.historySelector).setOnClickListener(this)
    }

    // Centralizing click handling to improve readability and maintainability
    override fun onClick(view: View?) {
        // Determine which container was clicked and navigate accordingly
        val targetClass = when (view?.id) {
            R.id.cameraModeSelector -> CameraAnalysisActivity::class.java
            R.id.manualModeSelector -> ManualAnalysisActivity::class.java
            R.id.historySelector -> HistoryActivity::class.java
            else -> null
        }

        // Start the activity if a match was found
        targetClass?.let {
            val intent = Intent(this, it)
            startActivity(intent)
        }
    }
}


