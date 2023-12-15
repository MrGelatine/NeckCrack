package com.google.ar.core.examples.java.neckcrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.google.ar.core.examples.java.neckcrack.databinding.FragmentMenuBinding

class MenuFragment : Fragment() {
    lateinit var navController: NavController
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding:FragmentMenuBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu, container, false)
        binding.ExerciseButton.setOnClickListener { v -> navController.navigate(R.id.action_menuFragment_to_exerciseCameraFragment) }
        binding.SettingButton.setOnClickListener { v -> navController.navigate(R.id.action_menuFragment_to_settingsFragment) }
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController(view)

    }
}