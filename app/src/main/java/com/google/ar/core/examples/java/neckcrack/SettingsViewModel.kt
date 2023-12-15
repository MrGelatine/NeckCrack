package com.google.ar.core.examples.java.neckcrack

import android.content.Context
import android.os.Bundle
import android.provider.Settings.System.getString
import android.view.View
import androidx.databinding.ObservableDouble
import androidx.databinding.ObservableField
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager.getDefaultSharedPreferences

class SettingsViewModel : ViewModel() {
    lateinit var navController: NavController
    lateinit var context: FragmentActivity
    var leftAngle:ObservableField<Float> = ObservableField<Float>(0.2f)
    var rightAngle:ObservableField<Float> = ObservableField<Float>(0.2f)
    var forwardAngle:ObservableField<Float> = ObservableField<Float>(0.2f)
    var backAngle:ObservableField<Float> = ObservableField<Float>(0.2f)
    fun loadPreferences() {
        val sharedPref = context?.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        if (sharedPref != null) {
            leftAngle.set(sharedPref.getFloat(context.getString(R.string.left_angle),0.2f))
            rightAngle.set(sharedPref.getFloat(context.getString(R.string.right_angle),0.2f))
            forwardAngle.set(sharedPref.getFloat(context.getString(R.string.forward_angle),0.2f))
            backAngle.set(sharedPref.getFloat(context.getString(R.string.back_angle),0.2f))
        }
    }
    fun changeAngles(angleType: String?, angleValue: Float?){
        if (angleValue != null) {
            val sharedPref = context?.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)!!
            when (angleType) {
                "RIGHT" -> {rightAngle.set(angleValue)
                    with (sharedPref.edit()) {
                        putFloat(context.getString(R.string.right_angle), angleValue)
                        apply()
                    }}
                "LEFT" -> {leftAngle.set(angleValue)
                    with (sharedPref.edit()) {
                        putFloat(context.getString(R.string.left_angle), angleValue)
                        apply()
                    }}
                "FORWARD" -> {forwardAngle.set(angleValue)
                    with (sharedPref.edit()) {
                        putFloat(context.getString(R.string.forward_angle), angleValue)
                        apply()
                    }}
                "BACK" -> {backAngle.set(angleValue)
                    with (sharedPref.edit()) {
                        putFloat(context.getString(R.string.back_angle), angleValue)
                        apply()
                    }}
            }
        }
    }
    fun correctLeftValue(): Boolean{
        val action = SettingsFragmentDirections.actionSettingsFragmentToChooseAngleDialogFragment("LEFT")
        navController.navigate(action)
        return false
    }
    fun correctRightValue(): Boolean{
        val action = SettingsFragmentDirections.actionSettingsFragmentToChooseAngleDialogFragment("RIGHT")
        navController.navigate(action)
        return false
    }
    fun correctForwardValue(): Boolean{
        val action = SettingsFragmentDirections.actionSettingsFragmentToChooseAngleDialogFragment("FORWARD")
        navController.navigate(action)
        return false
    }
    fun correctBackValue(): Boolean{
        val action = SettingsFragmentDirections.actionSettingsFragmentToChooseAngleDialogFragment("BACK")
        navController.navigate(action)
        return false
    }
    fun callCalibrationLeft(): Boolean{
        return false
    }
    fun callCalibrationRight(): Boolean{
        return false
    }
    fun callCalibrationForward(): Boolean{
        return false
    }
    fun callCalibrationBack(): Boolean{
        return false
    }

}