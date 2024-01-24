package com.example.api20_detector

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val cameraModeContainer = findViewById<LinearLayout>(R.id.cameraModeSelector)
        val manualModeContainer = findViewById<LinearLayout>(R.id.manualModeSelector)
        val historyContainer = findViewById<LinearLayout>(R.id.historySelector)


        cameraModeContainer.setOnClickListener {
            val intent = Intent(this, CameraAnalysisActivity::class.java)
            startActivity(intent)
        }

        manualModeContainer.setOnClickListener {
            val intent = Intent(this, ManualAnalysisActivity::class.java)
            startActivity(intent)
        }

        historyContainer.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

    }
}

