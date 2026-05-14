package com.darkfactory.plantpotting.camera

import android.content.Context
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.darkfactory.plantpotting.R
import kotlinx.coroutines.flow.collectLatest
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onSpeciesIdentified: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }

    val injectedImageCapture = CameraScreenTestRegistry.testImageCapture
    var imageCapture by remember { mutableStateOf<ImageCapture?>(injectedImageCapture) }

    LaunchedEffect(viewModel) {
        viewModel.navigate.collectLatest { speciesId ->
            onSpeciesIdentified(speciesId)
        }
    }

    DisposableEffect(viewModel) {
        CameraScreenTestRegistry.current = viewModel
        onDispose {
            if (CameraScreenTestRegistry.current === viewModel) {
                CameraScreenTestRegistry.current = null
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize().testTag(CameraScreenTags.PREVIEW),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                // In production `injectedImageCapture` is null, so the real
                // CameraX binding runs. Tests that pre-set
                // `CameraScreenTestRegistry.testImageCapture` skip the
                // binding entirely so the test's value isn't clobbered.
                if (injectedImageCapture == null) {
                    bindCameraUseCases(
                        context = ctx,
                        previewView = previewView,
                        lifecycleOwner = lifecycleOwner,
                        onBound = { imageCapture = it },
                    )
                }
                previewView
            },
        )

        if (state is CameraUiState.Identifying || state is CameraUiState.Capturing) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
                Text(
                    text = stringResource(id = R.string.camera_loading),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 64.dp),
                )
            }
        }

        if (state is CameraUiState.Failure) {
            Text(
                text = (state as CameraUiState.Failure).reason,
                color = Color.White,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .testTag(CameraScreenTags.ERROR),
            )
        }

        val shutterLabel = stringResource(id = R.string.camera_shutter_label)
        val shutterEnabled = state is CameraUiState.Idle || state is CameraUiState.Failure
        FloatingActionButton(
            onClick = {
                if (!shutterEnabled) return@FloatingActionButton
                val capture = imageCapture ?: return@FloatingActionButton
                takeJpegPicture(capture, executor, viewModel)
            },
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .size(72.dp)
                    .testTag(CameraScreenTags.SHUTTER)
                    .alpha(if (shutterEnabled) 1f else 0.5f)
                    .semantics { if (!shutterEnabled) disabled() },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_camera_shutter),
                contentDescription = shutterLabel,
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose { /* CameraX use cases unbind on lifecycle stop. */ }
    }
}

object CameraScreenTags {
    const val PREVIEW = "camera.preview"
    const val SHUTTER = "camera.shutter"
    const val ERROR = "camera.error"
}

/**
 * Test-only handle on the active [CameraViewModel] and an optional injected
 * [ImageCapture]. The view-model field is populated by [CameraScreen] during
 * composition because the view model is created in the Compose Navigation
 * back-stack-entry's `ViewModelStore`, which an instrumentation test cannot
 * reach from `MainActivity` alone.
 *
 * `testImageCapture`, when non-null, replaces the real CameraX-bound
 * [ImageCapture] in [CameraScreen]. Tests use this to assert shutter
 * enablement (and the §4 bind-loading state) without standing up a real
 * camera — the AOSP GMD has no sensor, so the production binding code path
 * silently leaves `imageCapture = null` there.
 */
internal object CameraScreenTestRegistry {
    @Volatile
    var current: CameraViewModel? = null

    @Volatile
    var testImageCapture: ImageCapture? = null
}

private fun bindCameraUseCases(
    context: Context,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onBound: (ImageCapture) -> Unit,
) {
    val providerFuture = ProcessCameraProvider.getInstance(context)
    providerFuture.addListener(
        {
            val provider = providerFuture.get()
            val selector =
                ResolutionSelector
                    .Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                    .build()
            val preview =
                Preview
                    .Builder()
                    .setResolutionSelector(selector)
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val capture =
                ImageCapture
                    .Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setResolutionSelector(selector)
                    .build()
            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    capture,
                )
                onBound(capture)
            } catch (_: Exception) {
                // Camera binding can fail on emulator without a camera; the
                // shutter remains disabled because imageCapture stays null.
            }
        },
        ContextCompat.getMainExecutor(context),
    )
    // Suppress unused-aspect-ratio warning.
    @Suppress("UNUSED_VARIABLE")
    val ratio = AspectRatio.RATIO_4_3
}

private fun takeJpegPicture(
    capture: ImageCapture,
    executor: Executor,
    viewModel: CameraViewModel,
) {
    capture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bytes = imageProxyToJpegBytes(image)
                image.close()
                viewModel.onCaptureReady(bytes)
            }

            override fun onError(exception: ImageCaptureException) {
                viewModel.onCaptureFailed(exception.message ?: "capture failed")
            }
        },
    )
}

private fun imageProxyToJpegBytes(image: ImageProxy): ByteArray {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return if (image.format == android.graphics.ImageFormat.JPEG) {
        bytes
    } else {
        // Fallback: encode YUV to JPEG via Bitmap. Slow but rarely hit because
        // ImageCapture.takePicture produces JPEG by default.
        val out = ByteArrayOutputStream()
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        bitmap?.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
        out.toByteArray()
    }
}
