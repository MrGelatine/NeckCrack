package com.google.ar.core.examples.java.neckcrack;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedFace;
import com.google.ar.core.Camera;
import com.google.ar.core.CameraConfig;
import com.google.ar.core.CameraConfigFilter;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.examples.java.neckcrack.databinding.FragmentExerciseCameraBinding;
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper;
import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;
import com.google.ar.core.examples.java.common.helpers.FullScreenHelper;
import com.google.ar.core.examples.java.common.helpers.SnackbarHelper;
import com.google.ar.core.examples.java.common.helpers.TrackingStateHelper;
import com.google.ar.core.examples.java.common.rendering.BackgroundRenderer;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ExerciseCameraFragment extends Fragment implements GLSurfaceView.Renderer, IOnFocusListenable {


    private static final String TAG = AugmentedFacesActivity.class.getSimpleName();
    private GLSurfaceView surfaceView;

    private boolean installRequested;

    private Session session;
    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
    private DisplayRotationHelper displayRotationHelper;
    private TrackingStateHelper trackingStateHelper = null;

    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();

    private ExerciseCameraViewModel viewModel;
    private AugmentedFacesViewModel activityViewModel;

    private FragmentExerciseCameraBinding binding;
    private FragmentActivity fragmentActivity;

    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        fragmentActivity = getActivity();
        activityViewModel = new ViewModelProvider(getActivity()).get(AugmentedFacesViewModel.class);
        viewModel = new ViewModelProvider(fragmentActivity).get(ExerciseCameraViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_exercise_camera, container, false);
        binding.setExerciseViewModel(viewModel);
        surfaceView = binding.fakeSurfaceView;
        displayRotationHelper = new DisplayRotationHelper(getContext());

        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        surfaceView.setWillNotDraw(false);
        trackingStateHelper = activityViewModel.trackingStateHelper;

        installRequested = false;
        return binding.getRoot();
    }
    @Override
    public void onViewCreated (View view,  Bundle savedInstanceState){
        viewModel.navController = Navigation.findNavController(view);
        viewModel.nodChecker = true;
        viewModel.setNodVector(ExerciseCameraViewModel.States.LEFT);
        navController = Navigation.findNavController(view);

    }
    @Override
    public void onDestroy() {
        if (session != null) {
            session.close();
            session = null;
        }

        super.onDestroy();
    }
    @Override
    public void onResume() {
        super.onResume();

        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                switch (ArCoreApk.getInstance().requestInstall(fragmentActivity, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }
                if (!CameraPermissionHelper.hasCameraPermission(fragmentActivity)) {
                    CameraPermissionHelper.requestCameraPermission(fragmentActivity);
                    return;
                }
                session = new Session(fragmentActivity, EnumSet.noneOf(Session.Feature.class));
                CameraConfigFilter cameraConfigFilter = new CameraConfigFilter(session);
                cameraConfigFilter.setFacingDirection(CameraConfig.FacingDirection.FRONT);
                List<CameraConfig> cameraConfigs = session.getSupportedCameraConfigs(cameraConfigFilter);
                if (!cameraConfigs.isEmpty()) {
                    session.setCameraConfig(cameraConfigs.get(0));
                } else {
                    message = "This device does not have a front-facing (selfie) camera";
                    exception = new UnavailableDeviceNotCompatibleException(message);
                }
                configureSession();

            } catch (UnavailableArcoreNotInstalledException
                     | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (Exception e) {
                message = "Failed to create AR session";
                exception = e;
            }

            if (message != null) {
                messageSnackbarHelper.showError(fragmentActivity, message);
                Log.e(TAG, "Exception creating session", exception);
                return;
            }
        }
        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            messageSnackbarHelper.showError(fragmentActivity, "Camera not available. Try restarting the app.");
            session = null;
            return;
        }

        surfaceView.onResume();
        displayRotationHelper.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (!CameraPermissionHelper.hasCameraPermission(fragmentActivity)) {
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(fragmentActivity)) {
                CameraPermissionHelper.launchPermissionSettings(fragmentActivity);
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        FullScreenHelper.setFullScreenOnWindowFocusChanged(fragmentActivity, hasFocus);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        try {
            backgroundRenderer.createOnGlThread(fragmentActivity);
            viewModel.setUpRenderers(fragmentActivity);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read an asset file", e);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null) {
            return;
        }
        displayRotationHelper.updateSessionIfNeeded(session);

        try {
            session.setCameraTextureName(backgroundRenderer.getTextureId());

            Frame frame;
            frame = session.update();
            Camera camera = frame.getCamera();

            float[] projectionMatrix = new float[16];
            camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f);

            float[] viewMatrix = new float[16];
            camera.getViewMatrix(viewMatrix, 0);

            final float[] colorCorrectionRgba = new float[4];
            frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

            backgroundRenderer.draw(frame);

            trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

            Collection<AugmentedFace> faces = session.getAllTrackables(AugmentedFace.class);
            for (AugmentedFace face : faces) {
                if (face.getTrackingState() != TrackingState.TRACKING) {
                    break;
                }
                GLES20.glDepthMask(false);

                float[] modelMatrix = new float[16];
                face.getCenterPose().toMatrix(modelMatrix, 0);
                float[] face_rotation = face.getCenterPose().getRotationQuaternion();
                // Left: [-0.111158654, -0.006456257, -0.34942585, 0.9303245]
                // Still: [-0.042394277, 0.015830848, -0.008711225, 0.99893755]
                // Right: [-0.0596097, 0.07804108, 0.30957586, 0.94579023]
                //Back: [-0.3244006, -0.085564755, 0.027893635, 0.9416289]
                //Forward: [0.21479343, -0.03203746, 0.00987039, 0.976084]
                float horizontal_face_coeficient = face_rotation[2];
                float vertical_face_cefficient = face_rotation[0];
                viewModel.checkState(horizontal_face_coeficient, vertical_face_cefficient);

                viewModel.currentFaceRender.draw(
                        projectionMatrix, viewMatrix, modelMatrix, colorCorrectionRgba, face);

            }
        } catch (Throwable t) {
            Log.e(TAG, "Exception on the OpenGL thread", t);
        } finally {
            GLES20.glDepthMask(true);
        }
    }
    private void configureSession() {
        Config config = new Config(session);
        config.setAugmentedFaceMode(Config.AugmentedFaceMode.MESH3D);
        session.configure(config);
    }
}