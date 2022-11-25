package com.pdb82.flashlighttiramisu.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pdb82.flashlighttiramisu.databinding.FragmentDeviceNotSupportBinding
import kotlin.system.exitProcess

class DeviceNotSupportFragment : Fragment() {

    private lateinit var binding: FragmentDeviceNotSupportBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDeviceNotSupportBinding.inflate(inflater, container, false)

        binding.button2.setOnClickListener {
            exitProcess(0)
        }

        return binding.root
    }

}