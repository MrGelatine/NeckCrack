package com.google.ar.core.examples.java.neckcrack

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.ar.core.examples.java.neckcrack.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var _binding: FragmentSettingsBinding
    private val viewmodel :SettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_settings,container,false)
        val angleType: String? = arguments?.getString("AngleType")
        val angleValue: Float? = arguments?.getFloat("AngleValue")
        viewmodel.changeAngles(angleType, angleValue)
        _binding.viewModel = viewmodel
        return _binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewmodel.navController = Navigation.findNavController(view)
        viewmodel.context = requireActivity()
        viewmodel.loadPreferences()
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onPause() {
        super.onPause()

    }

    override fun onDestroy() {
        super.onDestroy()

    }



}