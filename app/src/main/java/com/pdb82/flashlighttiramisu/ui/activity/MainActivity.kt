package com.pdb82.flashlighttiramisu.ui.activity


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.pdb82.flashlighttiramisu.databinding.ActivityMainBinding
import com.pdb82.flashlighttiramisu.ui.fragments.MainFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MainFragment.firstStart = true

        supportFragmentManager.findFragmentById(binding.fragmentContainerView.id) as NavHostFragment
    }

}