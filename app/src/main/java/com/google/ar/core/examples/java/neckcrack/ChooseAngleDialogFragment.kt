package com.google.ar.core.examples.java.neckcrack

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.ar.core.examples.java.neckcrack.databinding.FragmentChooseAngleDialogBinding
import com.google.ar.core.examples.java.neckcrack.databinding.FragmentSettingsBinding

class ChooseAngleDialogFragment : DialogFragment() {

    private val parentViewModel:SettingsViewModel by activityViewModels()
    private lateinit var _binding: FragmentChooseAngleDialogBinding
    private lateinit var angleType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_choose_angle_dialog,container,false)
        val values:Array<String> = arrayOf("0.03","0.06","0.09","0.12","0.15","0.18","0.21","0.24","0.27","0.3")
        _binding.numberPicker.minValue = 0
        _binding.numberPicker.maxValue = values.size-1
        _binding.numberPicker.wrapSelectorWheel = false
        _binding.numberPicker.displayedValues = values
        _binding.numberPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        angleType = requireArguments().getString("AngleType").toString()
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCancel(dialog: DialogInterface) {
        val angleValue:Float = _binding.numberPicker.displayedValues[_binding.numberPicker.value].toFloat()
        parentViewModel.changeAngles(angleType, angleValue)
        findNavController().popBackStack()
        super.onCancel(dialog)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}