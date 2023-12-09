package com.google.ar.core.examples.java.neckcrack;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.ar.core.examples.java.neckcrack.databinding.FragmentMenuBinding;

public class MenuFragment extends Fragment {


    private FragmentMenuBinding binding;

    public NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu,container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onViewCreated (View view,  Bundle savedInstanceState){
        navController = Navigation.findNavController(view);
        binding.ExerciseButton.setOnClickListener(v ->{
                navController.navigate(R.id.action_menuFragment_to_exerciseCameraFragment);
        });
        binding.SettingButton.setOnClickListener(v ->{
            navController.navigate(R.id.action_menuFragment_to_dummyFragment);
        });

    }

}