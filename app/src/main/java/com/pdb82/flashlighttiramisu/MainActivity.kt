package com.pdb82.flashlighttiramisu

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {

    private val cameraManager by lazy { getSystemService(Context.CAMERA_SERVICE) as CameraManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val errorText = findViewById<TextView>(R.id.textView)
        val card = findViewById<CardView>(R.id.card)

        val list = cameraManager.cameraIdList
        val outCameraId = list[0]
        val cameraCharacteristics = cameraManager.getCameraCharacteristics(outCameraId)

        val torchMaxLevel =
            cameraCharacteristics[CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL] ?: -1

        if (torchMaxLevel > 1) {
            seekBar.max = torchMaxLevel
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    if (p1 == 0) {
                        cameraManager.setTorchMode(outCameraId, false)
                    } else {
                        cameraManager.turnOnTorchWithStrengthLevel(outCameraId, p1)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            })
        } else {
            errorText.visibility = View.VISIBLE
            card.visibility = View.VISIBLE
            errorText.text = "Device not supported"
            seekBar.isEnabled = false
        }


        val button_en = findViewById<Button>(R.id.button)
        val button_ru = findViewById<Button>(R.id.button2)

        val url_en = "https://t.me/google_nws"
        val url_ru = "https://t.me/googlenws_ru"

        val link_en = Intent(Intent.ACTION_VIEW, Uri.parse(url_en))
        val link_ru = Intent(Intent.ACTION_VIEW, Uri.parse(url_ru))

        button_en.setOnClickListener {
            startActivity(link_en)
        }

        button_ru.setOnClickListener {
            startActivity(link_ru)
        }
    }
}
