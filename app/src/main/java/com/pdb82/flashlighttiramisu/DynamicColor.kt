package com.pdb82.flashlighttiramisu

import android.app.Application
import com.google.android.material.color.DynamicColors

class DynamicColor : Application() {
    override fun onCreate() {
        DynamicColors.applyToActivitiesIfAvailable(this)
        super.onCreate()
    }
}