package com.google.ar.core.examples.java.neckcrack

import androidx.databinding.ObservableField
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

class CalibrationViewModel : ViewModel() {

    val faceRenderer = AugmentedFaceRenderer()
    var nodChecker = false
    var navController: NavController? = null
    var freezed: Boolean = false
    lateinit var target_head_state: String
    lateinit var parentViewModel: SettingsViewModel

    fun setUpRenderer(activity: FragmentActivity) {
        when(target_head_state){
            "BACK" -> {
                faceRenderer.createOnGlThread(activity, "models/face_up.png")
                faceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f)
            }
            "FORWARD" -> {
                faceRenderer.createOnGlThread(activity, "models/face_down.png")
                faceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f)
            }
            "LEFT" -> {
                faceRenderer.createOnGlThread(activity, "models/face_left.png")
                faceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f)
            }
            "RIGHT" -> {
                faceRenderer.createOnGlThread(activity, "models/face_right.png")
                faceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f)
            }
            else -> {}
        }
    }
    fun freezeFaceAngle(){
        viewModelScope.launch(Dispatchers.Main) {
            delay(2000)
            freezed = true
        }
    }
}