package com.google.ar.core.examples.java.neckcrack

import android.accessibilityservice.AccessibilityGestureEvent
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.databinding.ObservableField
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.properties.Delegates

class ExerciseCameraFragmentViewModel : ViewModel() {
    enum class States {
        LEFT, RIGHT, FORWARD, BACK, DONE
    }
    val nod_counter = ObservableField(0)
    var max_nods = 10
    val augmentedOKFaceRenderer = AugmentedFaceRenderer()
    val augmentedRightFaceRenderer = AugmentedFaceRenderer()
    val augmentedLeftFaceRenderer = AugmentedFaceRenderer()
    val augmentedForwardFaceRenderer = AugmentedFaceRenderer()
    val augmentedBackFaceRenderer = AugmentedFaceRenderer()
    val augmentedFaceGridRenderer = AugmentedFaceRenderer()
    var currentFaceRender = augmentedLeftFaceRenderer
    var nodChecker = false
    var navController: NavController? = null
    var target_head_state = States.LEFT
    lateinit var context: FragmentActivity
    var leftAngle by Delegates.notNull<Float>()
    var rightAngle by Delegates.notNull<Float>()
    var forwardAngle by Delegates.notNull<Float>()
    var backAngle by Delegates.notNull<Float>()

    fun loadAngles(){
        val sharedPref = context?.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        if(sharedPref != null){
            leftAngle = sharedPref.getFloat(context.getString(R.string.left_angle),0.2f)
            rightAngle = sharedPref.getFloat(context.getString(R.string.right_angle),0.2f)
            forwardAngle = sharedPref.getFloat(context.getString(R.string.forward_angle),0.2f)
            backAngle = sharedPref.getFloat(context.getString(R.string.back_angle),0.2f)
        }

    }
    @Throws(IOException::class)
    fun setUpRenderers(activity: FragmentActivity) {
        augmentedLeftFaceRenderer.createOnGlThread(activity, "models/face_left.png")
        augmentedLeftFaceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f)
        augmentedRightFaceRenderer.createOnGlThread(activity, "models/face_right.png")
        augmentedRightFaceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f)
        augmentedBackFaceRenderer.createOnGlThread(activity, "models/face_up.png")
        augmentedBackFaceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f)
        augmentedForwardFaceRenderer.createOnGlThread(activity, "models/face_down.png")
        augmentedForwardFaceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f)
        augmentedOKFaceRenderer.createOnGlThread(activity, "models/face_ok.png")
        augmentedOKFaceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f)
        augmentedFaceGridRenderer.createOnGlThread(activity, "models/face_grid.png")
        augmentedFaceGridRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f)
    }

    @Throws(InterruptedException::class)
    fun checkState(horizontal_face_coeficient: Float, vertical_face_cefficient: Float) {
        if (nodChecker) {
            if (horizontal_face_coeficient > 0.2 && target_head_state == States.RIGHT) {
                nod_counter.set(nod_counter.get()!! + 1)
                changeNodVector(States.LEFT, augmentedLeftFaceRenderer)
            } else if (horizontal_face_coeficient < -0.2 && target_head_state == States.LEFT) {
                nod_counter.set(nod_counter.get()!! + 1)
                changeNodVector(States.BACK, augmentedBackFaceRenderer)
            } else if (vertical_face_cefficient < -0.2 && target_head_state == States.BACK) {
                nod_counter.set(nod_counter.get()!! + 1)
                changeNodVector(States.FORWARD, augmentedForwardFaceRenderer)
            } else if (vertical_face_cefficient > 0.15 && target_head_state == States.FORWARD) {
                nod_counter.set(nod_counter.get()!! + 1)
                changeNodVector(States.RIGHT, augmentedRightFaceRenderer)
            }
        }
    }

    @Throws(InterruptedException::class)
    private fun changeNodVector(state: States, renderer: AugmentedFaceRenderer) {
        if (nod_counter.get()!! < max_nods) {
            target_head_state = state
            currentFaceRender = renderer
        } else {
            nodChecker = false
            target_head_state = States.DONE
            currentFaceRender = augmentedOKFaceRenderer
            viewModelScope.launch(Dispatchers.Main) {
                delay(2000)
                navController!!.navigateUp()
                nod_counter.set(0)
            }
        }
    }

    fun setNodVector(state: States) {
        target_head_state = state
        when (state) {
            States.BACK -> currentFaceRender = augmentedBackFaceRenderer
            States.LEFT -> currentFaceRender = augmentedLeftFaceRenderer
            States.RIGHT -> currentFaceRender = augmentedRightFaceRenderer
            States.FORWARD -> currentFaceRender = augmentedForwardFaceRenderer
            else -> {}
        }
    }
}