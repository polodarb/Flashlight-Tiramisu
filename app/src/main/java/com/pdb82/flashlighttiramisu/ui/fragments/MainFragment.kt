package com.pdb82.flashlighttiramisu.ui.fragments

import android.annotation.SuppressLint
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.slider.Slider
import com.pdb82.flashlighttiramisu.R
import com.pdb82.flashlighttiramisu.databinding.FragmentMainBinding
import com.pdb82.flashlighttiramisu.services.MyTileService
import java.text.NumberFormat
import kotlin.math.ceil
import kotlin.math.roundToInt


const val SLIDER_SAVED_VALUE = "SLIDER_SAVED_VALUE"
const val TORCH_MAX_LEVEL = "TORCH_MAX_VALUE"

class MainFragment : Fragment() {

    companion object {
        var active: Boolean = false
        var settingsState = false
        var savedInstance = false
    }

    private var changeFromSegment: Boolean = false

    private lateinit var binding: FragmentMainBinding
    private lateinit var preferences: SharedPreferences
    private val preferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            binding.textView.text =
                resources.getString(
                    (R.string.default_value_text),
                    (preferences.getFloat(SLIDER_SAVED_VALUE, 25F).toInt())
                )
        }


    private val cameraManager by lazy { activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager }

    private lateinit var torchCallback: CameraManager.TorchCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)

        val appContext = requireContext().applicationContext
        preferences = appContext.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)

        preferences.registerOnSharedPreferenceChangeListener(preferencesListener)

        binding.textView.text =
            resources.getString(
                (R.string.default_value_text),
                (preferences.getFloat(SLIDER_SAVED_VALUE, 25F).toInt())
            )

        val list = cameraManager.cameraIdList
        val outCameraId = list[0]
        val cameraCharacteristics = cameraManager.getCameraCharacteristics(outCameraId)

        val torchMaxLevel =
            cameraCharacteristics[CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL] ?: -1
        preferences.edit().putFloat(TORCH_MAX_LEVEL, torchMaxLevel.toFloat()).apply()

        if (torchMaxLevel > 1) {
            binding.slider.valueTo = torchMaxLevel.toFloat()

            val savedFromPercentToFloat =
                ceil((torchMaxLevel * preferences.getFloat(SLIDER_SAVED_VALUE, 25F)) / 100)
            binding.slider.value = savedFromPercentToFloat

            val savedValueFromPrefs = preferences.getFloat(SLIDER_SAVED_VALUE, 25F)
            if (savedValueFromPrefs == savedValueFromPrefs) binding.saveValue.isEnabled = false

            segmentPrefs()
            controlTopBar()
            formatSliderValue(torchMaxLevel.toFloat())


            binding.slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    if (preferences.getBoolean(SLIDER_AUTO_FLASH, true)) {
                        changeFromSegment = true
                        binding.button.isChecked = true
                        binding.sb1.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }

                    binding.saveValue.isEnabled = true
                    binding.segmentButton.clearChecked()
                }

                override fun onStopTrackingTouch(slider: Slider) {

                }
            })

            if (preferences.getString(
                    OPTIONS,
                    resources.getString(R.string.option_1)
                ) == resources.getString(R.string.option_1)
            ) {
                segmentButtonControlOption1(torchMaxLevel.toFloat(), outCameraId)
            } else {
                segmentButtonControlOption2(torchMaxLevel.toFloat(), outCameraId)
            }

            if (savedInstanceState != null) {
                savedInstance = true
            }

            binding.slider.addOnChangeListener { _, value, _ ->

                val valuePercent = ((value * 100) / torchMaxLevel).roundToInt().toString()

                binding.lightInfo.text = value.toString()


                if (!savedInstance) {
                    if (preferences.getBoolean(SLIDER_AUTO_FLASH, true)) {
                        if (settingsState) settingsState = false else flashlightControlFromSlider(
                            value,
                            outCameraId
                        )
                    }
                }
                savedInstance = false

                if (binding.button.isChecked) flashlightControlFromSlider(value, outCameraId)

                binding.saveValue.setOnClickListener {
                    preferences.edit().putFloat(SLIDER_SAVED_VALUE, valuePercent.toFloat()).apply()
                    preferences =
                        appContext.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
                    animateScaleTextView()
                }
            }

            binding.button.setOnCheckedChangeListener { _, isChecked ->

                animateScaleButton(isChecked)

                if (isChecked) {

                    if (changeFromSegment) {
                        changeFromSegment = false
                        return@setOnCheckedChangeListener
                    }

                    binding.sb1.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

                    binding.segmentButton.clearChecked()

                    if (preferences.getBoolean(SLIDER_FROM_DEFAULT, true)) {
                        binding.slider.value =
                            (torchMaxLevel * preferences.getFloat(SLIDER_SAVED_VALUE, 25F)) / 100
                        flashlightControlFromSlider(binding.slider.value, outCameraId)
                    } else {
                        if (binding.slider.value.roundToInt() < 1) {
                            cameraManager.setTorchMode(outCameraId, false)
                        } else {
                            cameraManager.turnOnTorchWithStrengthLevel(
                                outCameraId,
                                binding.slider.value.roundToInt()
                            )
                        }
                    }

                    if (preferences.getBoolean(SLIDER_AUTO_FLASH, true)) {
                        if (binding.slider.value.roundToInt() < 1) {
                            cameraManager.setTorchMode(outCameraId, false)
                        } else {
                            cameraManager.turnOnTorchWithStrengthLevel(
                                outCameraId,
                                binding.slider.value.roundToInt()
                            )
                        }
                    }

                    binding.saveValue.isEnabled = true
                } else {
                    binding.sb1.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_RELEASE)
                    binding.segmentButton.clearChecked()
                    changeFromSegment = false
                    cameraManager.setTorchMode(outCameraId, false)
                }
            }
        } else {
            findNavController().navigate(R.id.action_mainFragment_to_deviceNotSupportFragment)
        }

        return binding.root
    }

    private fun flashlightControlFromSlider(value: Float, outCameraId: String) {
        if (ceil(value).toInt() == 0) {
            cameraManager.setTorchMode(outCameraId, false)
        } else {
            cameraManager.turnOnTorchWithStrengthLevel(outCameraId, ceil(value).toInt())
        }
    }

    private fun segmentButtonControlOption1(torchMaxValue: Float, outCameraId: String) {
        binding.segmentButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            binding.segmentButton.clearChecked()
            if (isChecked) {
                when (checkedId) {
                    R.id.sb_1 -> {
                        segmentButtonControl(ceil(torchMaxValue / 100), outCameraId)
                    }

                    R.id.sb_2 -> {
                        segmentButtonControl(torchMaxValue / 4, outCameraId)
                    }

                    R.id.sb_3 -> {
                        segmentButtonControl(torchMaxValue / 2, outCameraId)
                    }

                    R.id.sb_4 -> {
                        segmentButtonControl(torchMaxValue, outCameraId)
                    }
                }
            }
        }
    }

    private fun segmentButtonControlOption2(torchMaxValue: Float, outCameraId: String) {
        binding.segmentButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            binding.segmentButton.clearChecked()
            if (isChecked) {
                when (checkedId) {
                    R.id.sb_1 -> {
                        segmentButtonControl(torchMaxValue / 4, outCameraId)
                    }

                    R.id.sb_2 -> {
                        segmentButtonControl(torchMaxValue / 2, outCameraId)
                    }

                    R.id.sb_3 -> {
                        segmentButtonControl((torchMaxValue * 75) / 100, outCameraId)
                    }

                    R.id.sb_4 -> {
                        segmentButtonControl(torchMaxValue, outCameraId)
                    }
                }
            }
        }
    }

    private fun segmentButtonControl(value: Float, outCameraId: String) {
        changeFromSegment = true
        binding.button.isChecked = true
        flashlightControlFromSlider(value, outCameraId)
        binding.slider.value = value
        binding.saveValue.isEnabled = true
        binding.sb1.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_RELEASE)
    }

    @SuppressLint("SetTextI18n")
    private fun segmentPrefs() {
        when (preferences.getString(OPTIONS, resources.getString(R.string.option_1))) {
            resources.getString(R.string.option_1) -> {
                binding.sb1.text = "1%"
                binding.sb2.text = "25%"
                binding.sb3.text = "50%"
                binding.sb4.text = "100%"
            }

            resources.getString(R.string.option_2) -> {
                binding.sb1.text = "25%"
                binding.sb2.text = "50%"
                binding.sb3.text = "75%"
                binding.sb4.text = "100%"
            }
        }
    }

    private fun animateScaleTextView() {
        binding.textView.animate().apply {
            duration = 150
            scaleX(1.05F)
            scaleY(1.05F)
        }.withEndAction {
            binding.textView.animate().apply {
                duration = 150
                scaleX(1F)
                scaleY(1F)
            }
        }.start()
    }

    private fun animateScaleButton(isChecked: Boolean) {

        if (isChecked) {
            binding.button.animate().apply {
                duration = 150
                scaleX(1.1F)
                scaleY(1.1F)
            }.withEndAction {
                binding.button.animate().apply {
                    duration = 150
                    scaleX(1F)
                    scaleY(1F)
                }
            }.start()
        } else {
            binding.button.animate().apply {
                duration = 150
                scaleX(0.9F)
                scaleY(0.9F)
            }.withEndAction {
                binding.button.animate().apply {
                    duration = 150
                    scaleX(1F)
                    scaleY(1F)
                }
            }.start()
        }
    }

    private fun formatSliderValue(torchMaxLevel: Float) {
        binding.slider.setLabelFormatter {
            val format = NumberFormat.getPercentInstance()
            format.format(it / preferences.getFloat(TORCH_MAX_LEVEL, torchMaxLevel))
        }
    }

    private fun controlTopBar() {
        binding.topbarMain.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.add_title -> {
                    getSystemService(
                        requireContext(),
                        StatusBarManager::class.java
                    )?.requestAddTileService(
                        ComponentName(
                            requireContext(),
                            MyTileService::class.java,
                        ),
                        "Flashlight",
                        Icon.createWithResource(context, R.drawable.baseline_highlight_24),
                        {},
                        {
                            Toast.makeText(
                                context,
                                "ERROR ADD",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                    true
                }

                R.id.settings -> {
                    findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
                    binding.button.isChecked = false
                    binding.segmentButton.clearChecked()
                    true
                }
                else -> false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        active = true
    }

    override fun onResume() {
        super.onResume()
        active = true
        val list = cameraManager.cameraIdList
        val outCameraId = list[0]
        if ((preferences.getFloat(TORCH_MAX_LEVEL, 0F) > 1)) {
            torchCallback = object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                    super.onTorchModeChanged(cameraId, enabled)
                    if (enabled) {
                        if (cameraManager.getTorchStrengthLevel(outCameraId) == (preferences.getFloat(
                                TORCH_MAX_LEVEL,
                                0F
                            )).toInt()
                        ) {
                            changeFromSegment = true
                            binding.button.isChecked = true
                            binding.slider.value =
                                cameraManager.getTorchStrengthLevel(outCameraId).toFloat()
                        } else {
                            changeFromSegment = true
                            binding.button.isChecked =
                                cameraManager.getTorchStrengthLevel(outCameraId).toFloat() > 0
                            binding.slider.value =
                                cameraManager.getTorchStrengthLevel(outCameraId).toFloat()
                        }
                    } else {
                        binding.button.isChecked = false
                    }
                }
            }
            cameraManager.registerTorchCallback(torchCallback, null)
        }
    }

    override fun onStop() {
        super.onStop()
        active = false
        cameraManager.unregisterTorchCallback(torchCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        preferences.unregisterOnSharedPreferenceChangeListener(preferencesListener)
    }
}