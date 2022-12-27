package com.pdb82.flashlighttiramisu.ui.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pdb82.flashlighttiramisu.R
import com.pdb82.flashlighttiramisu.databinding.FragmentSettingsBinding

const val APP_PREFERENCES = "APP_PREFERENCES"
const val SLIDER_AUTO_FLASH = "SLIDER_AUTO_FLASH"
const val SLIDER_FROM_DEFAULT = "SLIDER_FROM_DEFAULT"
const val OPTIONS = "OPTIONS"

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var preferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

//        MainFragment.torchEnabled = false
        MainFragment.settingsState = true

        val appContext = requireContext().applicationContext
        preferences = appContext.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)

        binding.settingsSwitchAutoFlashlight.isChecked =
            preferences.getBoolean(SLIDER_AUTO_FLASH, true)

        binding.settingsAutoSliderFromDefaultValue.isChecked =
            preferences.getBoolean(SLIDER_FROM_DEFAULT, true)

        binding.settingsSwitchSegmentButtonDescription.text =
            preferences.getString(OPTIONS, resources.getString(R.string.option_1))

        backButton()
        controlTopBar()

        binding.explanationOfSettings.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.explanation_of_setting_items))
                .setMessage(resources.getString(R.string.supporting_text))
                .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->

                }
                .setNegativeButton(resources.getString(R.string.decline)) { _, _ ->
                    val clipboardManager =
                        activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText(
                        "supporting_text",
                        getText(R.string.supporting_text)
                    )
                    clipboardManager.setPrimaryClip(clipData)
                    Toast.makeText(
                        requireContext(),
                        R.string.copied_to_clipboard,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setIcon(R.drawable.info_24)
                .show()
        }

        binding.settingsSwitchAutoFlashlight.setOnCheckedChangeListener { _, isChecked ->
            settingsCheckboxAutoSlider(isChecked)
        }
        binding.settingsAutoSliderFromDefaultValue.setOnCheckedChangeListener { _, isChecked ->
            settingsCheckboxSliderFromDefaultValue(isChecked)
        }

        binding.linearSegment.setOnClickListener { v: View ->
            createPopUp(v)
        }


        return binding.root
    }

    private fun controlTopBar() {
        binding.topbarSettings.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> {
                    val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/polodarb"))
                    startActivity(i)
                    true
                }

                else -> false
            }
        }
    }

    private fun settingsCheckboxAutoSlider(isChecked: Boolean) {
        binding.linearSegment.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_RELEASE)
        if (isChecked) {
            preferences.edit().putBoolean(SLIDER_AUTO_FLASH, true).apply()
        } else {
            preferences.edit().putBoolean(SLIDER_AUTO_FLASH, false).apply()
        }
    }

    private fun settingsCheckboxSliderFromDefaultValue(isChecked: Boolean) {
        binding.linearSegment.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_RELEASE)
        if (isChecked) {
            preferences.edit().putBoolean(SLIDER_FROM_DEFAULT, true).apply()
        } else {
            preferences.edit().putBoolean(SLIDER_FROM_DEFAULT, false).apply()
        }
    }

    private fun createPopUp(v: View) {
        binding.linearSegment.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_RELEASE)
        val popup = PopupMenu(activity, v)
        popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.option_1 -> {
                    binding.settingsSwitchSegmentButtonDescription.text =
                        getString(R.string.option_1)
                    preferences.edit().putString(OPTIONS, resources.getString(R.string.option_1))
                        .apply()

                }

                R.id.option_2 -> {
                    binding.settingsSwitchSegmentButtonDescription.text =
                        getString(R.string.option_2)
                    preferences.edit().putString(OPTIONS, resources.getString(R.string.option_2))
                        .apply()

                }

                else -> Toast.makeText(requireContext(), "Menu selection error", Toast.LENGTH_SHORT)
                    .show()
            }
            true
        }
        popup.show()
    }

    private fun backButton() {
        binding.topbarSettings.setNavigationOnClickListener {
            binding.topbarSettings.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_RELEASE)
            findNavController().popBackStack()
        }
    }
}
