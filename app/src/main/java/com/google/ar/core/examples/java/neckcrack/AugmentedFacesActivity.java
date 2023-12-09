/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.core.examples.java.neckcrack;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.ar.core.examples.java.neckcrack.databinding.ActivityMainBinding;
import com.google.ar.core.examples.java.common.helpers.TrackingStateHelper;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore API. The application will display any detected planes and will allow the user to tap on a
 * plane to place a 3d model of the Android robot.
 */
public class AugmentedFacesActivity extends AppCompatActivity{
  private static final String TAG = AugmentedFacesActivity.class.getSimpleName();

  // Rendering. The Renderers are created here, and initialized when the GL surface is created.

  private AugmentedFacesViewModel viewModel;




  @SuppressLint("CommitTransaction")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewModel = new ViewModelProvider(this).get(AugmentedFacesViewModel.class);
    viewModel.trackingStateHelper = new TrackingStateHelper(this);
    ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
  }


}
