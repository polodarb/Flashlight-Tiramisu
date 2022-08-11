package com.pdb82.flashlighttiramisu

import android.content.Intent
import android.service.quicksettings.TileService

class MyTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        startActivityAndCollapse(launchIntent)
    }
}