package com.google.ar.core.examples.java.neckcrack

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ArCoreApk.InstallStatus
import com.google.ar.core.AugmentedFace
import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper
import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper
import com.google.ar.core.examples.java.common.helpers.FullScreenHelper
import com.google.ar.core.examples.java.common.helpers.SnackbarHelper
import com.google.ar.core.examples.java.common.helpers.TrackingStateHelper
import com.google.ar.core.examples.java.common.rendering.BackgroundRenderer
import com.google.ar.core.examples.java.neckcrack.databinding.FragmentExerciseCameraBinding
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import java.io.IOException
import java.util.EnumSet
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ExerciseCameraFragment : Fragment(), GLSurfaceView.Renderer,
    IOnFocusListenable {
    private lateinit var surfaceView: GLSurfaceView
    private var installRequested = false
    private var session: Session? = null
    private val messageSnackbarHelper = SnackbarHelper()
    private var displayRotationHelper: DisplayRotationHelper? = null
    private var trackingStateHelper: TrackingStateHelper? = null
    private val backgroundRenderer = BackgroundRenderer()
    private val activityViewModel: AugmentedFacesActivityViewModel by activityViewModels()
    private var fragmentActivity: FragmentActivity? = null
    private var navController: NavController? = null
    private val viewModel: ExerciseCameraFragmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentActivity = activity
        val binding: FragmentExerciseCameraBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_exercise_camera, container, false)
        binding.exerciseCameraFragmentViewModel = viewModel
        surfaceView = binding.exerciseSurfaceView
        displayRotationHelper = DisplayRotationHelper(context)
        surfaceView.preserveEGLContextOnPause = true
        surfaceView.setEGLContextClientVersion(2)
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        surfaceView.setRenderer(this)
        surfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        surfaceView.setWillNotDraw(false)
        trackingStateHelper = activityViewModel!!.trackingStateHelper
        installRequested = false
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.navController = findNavController(view)
        viewModel.context = requireActivity()
        viewModel.loadAngles()
        viewModel.nodChecker = true

        viewModel.setNodVector(ExerciseCameraFragmentViewModel.States.LEFT)
        navController = findNavController(view)
    }

    override fun onDestroy() {
        if (session != null) {
            session!!.close()
            session = null
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (session == null) {
            var exception: Exception? = null
            var message: String? = null
            try {
                when (ArCoreApk.getInstance().requestInstall(fragmentActivity, !installRequested)) {
                    InstallStatus.INSTALL_REQUESTED -> {
                        installRequested = true
                        return
                    }

                    InstallStatus.INSTALLED -> {}
                }
                if (!CameraPermissionHelper.hasCameraPermission(fragmentActivity)) {
                    CameraPermissionHelper.requestCameraPermission(fragmentActivity)
                    return
                }
                session = Session(
                    fragmentActivity, EnumSet.noneOf(
                        Session.Feature::class.java
                    )
                )
                val cameraConfigFilter = CameraConfigFilter(session)
                cameraConfigFilter.facingDirection = CameraConfig.FacingDirection.FRONT
                val cameraConfigs = session!!.getSupportedCameraConfigs(cameraConfigFilter)
                if (!cameraConfigs.isEmpty()) {
                    session!!.cameraConfig = cameraConfigs[0]
                } else {
                    message = "This device does not have a front-facing (selfie) camera"
                    exception = UnavailableDeviceNotCompatibleException(message)
                }
                configureSession()
            } catch (e: UnavailableArcoreNotInstalledException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableUserDeclinedInstallationException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableApkTooOldException) {
                message = "Please update ARCore"
                exception = e
            } catch (e: UnavailableSdkTooOldException) {
                message = "Please update this app"
                exception = e
            } catch (e: UnavailableDeviceNotCompatibleException) {
                message = "This device does not support AR"
                exception = e
            } catch (e: Exception) {
                message = "Failed to create AR session"
                exception = e
            }
            if (message != null) {
                messageSnackbarHelper.showError(fragmentActivity, message)
                Log.e(TAG, "Exception creating session", exception)
                return
            }
        }
        try {
            session!!.resume()
        } catch (e: CameraNotAvailableException) {
            messageSnackbarHelper.showError(
                fragmentActivity,
                "Camera not available. Try restarting the app."
            )
            session = null
            return
        }
        surfaceView!!.onResume()
        displayRotationHelper!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (session != null) {
            displayRotationHelper!!.onPause()
            surfaceView!!.onPause()
            session!!.pause()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (!CameraPermissionHelper.hasCameraPermission(fragmentActivity)) {
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(fragmentActivity)) {
                CameraPermissionHelper.launchPermissionSettings(fragmentActivity)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        FullScreenHelper.setFullScreenOnWindowFocusChanged(fragmentActivity, hasFocus)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        try {
            backgroundRenderer.createOnGlThread(fragmentActivity)
            viewModel!!.setUpRenderers(fragmentActivity!!)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read an asset file", e)
        }
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        displayRotationHelper!!.onSurfaceChanged(width, height)
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        if (session == null) {
            return
        }
        displayRotationHelper!!.updateSessionIfNeeded(session)
        try {
            session!!.setCameraTextureName(backgroundRenderer.textureId)
            val frame: Frame
            frame = session!!.update()
            val camera = frame.camera
            val projectionMatrix = FloatArray(16)
            camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f)
            val viewMatrix = FloatArray(16)
            camera.getViewMatrix(viewMatrix, 0)
            val colorCorrectionRgba = FloatArray(4)
            frame.lightEstimate.getColorCorrection(colorCorrectionRgba, 0)
            backgroundRenderer.draw(frame)
            trackingStateHelper!!.updateKeepScreenOnFlag(camera.trackingState)
            val faces = session!!.getAllTrackables(
                AugmentedFace::class.java
            )
            for (face in faces) {
                if (face.trackingState != TrackingState.TRACKING) {
                    break
                }
                GLES20.glDepthMask(false)
                val modelMatrix = FloatArray(16)
                face.centerPose.toMatrix(modelMatrix, 0)
                val face_rotation = face.centerPose.rotationQuaternion
                // Left: [-0.111158654, -0.006456257, -0.34942585, 0.9303245]
                // Still: [-0.042394277, 0.015830848, -0.008711225, 0.99893755]
                // Right: [-0.0596097, 0.07804108, 0.30957586, 0.94579023]
                //Back: [-0.3244006, -0.085564755, 0.027893635, 0.9416289]
                //Forward: [0.21479343, -0.03203746, 0.00987039, 0.976084]
                val horizontal_face_coeficient = face_rotation[2]
                val vertical_face_cefficient = face_rotation[0]
                viewModel!!.checkState(horizontal_face_coeficient, vertical_face_cefficient)
                viewModel!!.currentFaceRender.draw(
                    projectionMatrix, viewMatrix, modelMatrix, colorCorrectionRgba, face
                )
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Exception on the OpenGL thread", t)
        } finally {
            GLES20.glDepthMask(true)
        }
    }

    private fun configureSession() {
        val config = Config(session)
        config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
        session!!.configure(config)
    }

    companion object {
        private val TAG = AugmentedFacesActivity::class.java.simpleName
    }
}