package com.pdb82.flashlighttiramisu.services

import android.content.Context
import android.content.SharedPreferences
import android.hardware.camera2.CameraManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.pdb82.flashlighttiramisu.R
import com.pdb82.flashlighttiramisu.ui.fragments.APP_PREFERENCES
import com.pdb82.flashlighttiramisu.ui.fragments.MainFragment
import com.pdb82.flashlighttiramisu.ui.fragments.SLIDER_SAVED_VALUE
import com.pdb82.flashlighttiramisu.ui.fragments.TORCH_MAX_LEVEL
import kotlin.math.ceil

class MyTileService : TileService() {

    private lateinit var preferences: SharedPreferences
    private val cameraManager by lazy { getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    private lateinit var torchCallback: CameraManager.TorchCallback


    override fun onStartListening() = refreshTile()

    override fun onStopListening() {
        super.onStopListening()
        if (preferences.getFloat(TORCH_MAX_LEVEL, 0F) > 1) {
            cameraManager.unregisterTorchCallback(torchCallback)
        }
    }

    override fun onClick() {
        val value = ceil(
            (preferences.getFloat(TORCH_MAX_LEVEL, 0F) * preferences.getFloat(
                SLIDER_SAVED_VALUE,
                25F
            )) / 100
        )
        val list = cameraManager.cameraIdList
        val outCameraId = list[0]
        if (qsTile.state == Tile.STATE_ACTIVE) {
            updateTile()
            cameraManager.setTorchMode(outCameraId, false)
        } else {
            updateTile(Tile.STATE_ACTIVE)
            if (ceil(value).toInt() == 0) {
                cameraManager.setTorchMode(outCameraId, false)
            } else {
                cameraManager.turnOnTorchWithStrengthLevel(outCameraId, ceil(value).toInt())
            }
        }
    }

    private fun refreshTile() = qsTile?.run {
        preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        if (preferences.getFloat(TORCH_MAX_LEVEL, 0F) > 1) {
            torchCallback = object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                    super.onTorchModeChanged(cameraId, enabled)
                    subtitle = resources.getString(
                        R.string.tile_subtitle_saved_brightness,
                        preferences.getFloat(SLIDER_SAVED_VALUE, 25F).toInt()
                    )
                    if (!MainFragment.active) {
                        if (enabled) qsTile.state =
                            Tile.STATE_ACTIVE else qsTile.state =
                            Tile.STATE_INACTIVE
                    } else {
                        state = Tile.STATE_UNAVAILABLE
                        subtitle = resources.getString(R.string.tile_subtitle_application_launched)
                    }
                }
            }
            cameraManager.registerTorchCallback(torchCallback, null)
        } else {
            state = Tile.STATE_UNAVAILABLE
            subtitle = resources.getString(R.string.device_not_support)
        }
        updateTile()
    } ?: Unit

    private fun updateTile(state: Int = Tile.STATE_INACTIVE) {
        qsTile ?: return
        qsTile.state = state
        qsTile.updateTile()
    }
}
