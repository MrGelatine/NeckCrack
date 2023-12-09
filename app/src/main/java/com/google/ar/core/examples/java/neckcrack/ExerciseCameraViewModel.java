package com.google.ar.core.examples.java.neckcrack;

import android.os.Handler;
import android.os.Looper;

import androidx.databinding.ObservableField;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import java.io.IOException;

public class ExerciseCameraViewModel extends ViewModel {
    enum States {
        LEFT,
        RIGHT,
        FORWARD,
        BACK,

        DONE
    }
    public final ObservableField<Integer> nod_counter = new ObservableField<Integer>(0);
    public Integer max_nods = 10;
    public final AugmentedFaceRenderer augmentedOKFaceRenderer = new AugmentedFaceRenderer();
    public final AugmentedFaceRenderer augmentedRightFaceRenderer = new AugmentedFaceRenderer();
    public final AugmentedFaceRenderer augmentedLeftFaceRenderer = new AugmentedFaceRenderer();
    public final AugmentedFaceRenderer augmentedForwardFaceRenderer = new AugmentedFaceRenderer();
    public final AugmentedFaceRenderer augmentedBackFaceRenderer = new AugmentedFaceRenderer();
    public final AugmentedFaceRenderer augmentedFaceGridRenderer = new AugmentedFaceRenderer();
    public AugmentedFaceRenderer currentFaceRender = augmentedLeftFaceRenderer;

    public boolean nodChecker = false;
    public NavController navController;
    public States target_head_state = States.LEFT;

    public void setUpRenderers(FragmentActivity activity) throws IOException {
        augmentedLeftFaceRenderer.createOnGlThread(activity, "models/face_left.png");
        augmentedLeftFaceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f);

        augmentedRightFaceRenderer.createOnGlThread(activity, "models/face_right.png");
        augmentedRightFaceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f);

        augmentedBackFaceRenderer.createOnGlThread(activity, "models/face_up.png");
        augmentedBackFaceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f);

        augmentedForwardFaceRenderer.createOnGlThread(activity, "models/face_down.png");
        augmentedForwardFaceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f);

        augmentedOKFaceRenderer.createOnGlThread(activity, "models/face_ok.png");
        augmentedOKFaceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f);

        augmentedFaceGridRenderer.createOnGlThread(activity, "models/face_grid.png");
        augmentedFaceGridRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f);
    }
    public void checkState(float horizontal_face_coeficient, float vertical_face_cefficient) throws InterruptedException {
        if(nodChecker){
            if (horizontal_face_coeficient > 0.2 && target_head_state == States.RIGHT) {
                nod_counter.set(nod_counter.get() + 1);
                changeNodVector(States.LEFT, augmentedLeftFaceRenderer);
            } else if (horizontal_face_coeficient < -0.2 && target_head_state == States.LEFT) {
                nod_counter.set(nod_counter.get() + 1);
                changeNodVector(States.BACK, augmentedBackFaceRenderer);
            } else if (vertical_face_cefficient < -0.2 && target_head_state == States.BACK) {
                nod_counter.set(nod_counter.get() + 1);
                changeNodVector(States.FORWARD, augmentedForwardFaceRenderer);
            } else if (vertical_face_cefficient > 0.15 && target_head_state == States.FORWARD) {
                nod_counter.set(nod_counter.get() + 1);
                changeNodVector(States.RIGHT, augmentedRightFaceRenderer);
            }
        }
    }
    private void changeNodVector(States state, AugmentedFaceRenderer renderer) throws InterruptedException {
        if(nod_counter.get() < max_nods) {
            target_head_state = state;
            currentFaceRender = renderer;
        }else{
            nodChecker = false;
            target_head_state = States.DONE;
            currentFaceRender = augmentedOKFaceRenderer;
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.postDelayed(() -> {navController.navigateUp();nod_counter.set(0);},2000);

        }
    }
    public void setNodVector(States state){
        target_head_state = state;
        switch (state){
            case BACK:
                currentFaceRender = augmentedBackFaceRenderer;
                break;
            case LEFT:
                currentFaceRender = augmentedLeftFaceRenderer;
                break;
            case RIGHT:
                currentFaceRender = augmentedRightFaceRenderer;
                break;
            case FORWARD:
                currentFaceRender = augmentedForwardFaceRenderer;
                break;
        }
    }
}