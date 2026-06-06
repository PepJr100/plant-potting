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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.darkfactory.plantpotting.R
import com.darkfactory.plantpotting.ui.HomeButton
import kotlinx.coroutines.flow.collectLatest
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onNavigate: (NavCommand) -> Unit,
    onHome: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }

    val injectedImageCapture by CameraScreenTestRegistry.testImageCaptureState
    val forceSkipBind by CameraScreenTestRegistry.forceSkipBindState
    var boundImageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    // Tests that inject an `ImageCapture` shadow the real-bind path; the
    // skip-bind flag forces the bind-pending state on AVDs whose emulated
    // back camera would otherwise let the bind succeed.
    val imageCapture: ImageCapture? = injectedImageCapture ?: boundImageCapture

    LaunchedEffect(viewModel) {
        viewModel.navigate.collectLatest { command ->
            onNavigate(command)
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

    // PLANTPOTTING-0008 Phase 3 — re-enable the shutter on return-to-camera.
    // A capture advances the view model to CameraUiState.Success, and that view
    // model survives in the back-stack-entry's ViewModelStore. A Success only
    // navigates to the result screen (it doesn't pop the camera entry), so
    // pressing system-back returns here with the state still Success → the
    // shutter would stay disabled. Resetting terminal Success to Idle on
    // ON_RESUME restores the shutter.
    //
    // Rule: reset ONLY terminal Success. We deliberately do NOT reset Failure —
    // the failure banner owns its own retry affordance (CameraUiState.Failure →
    // Retry button), and resetting on resume would silently dismiss it. The
    // in-flight states (Capturing/Identifying) are non-terminal and never match,
    // so an in-flight identify is never interrupted by this observer.
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME &&
                    viewModel.state.value is CameraUiState.Success
                ) {
                    viewModel.reset()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize().testTag(CameraScreenTags.PREVIEW),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                // Skip the real CameraX bind when a test has injected an
                // ImageCapture (`testImageCapture` non-null) or has
                // explicitly forced the bind-pending state via
                // `forceSkipBind`. Production hits the `else` branch.
                if (CameraScreenTestRegistry.testImageCapture == null &&
                    !CameraScreenTestRegistry.forceSkipBind
                ) {
                    bindCameraUseCases(
                        context = ctx,
                        previewView = previewView,
                        lifecycleOwner = lifecycleOwner,
                        onBound = { boundImageCapture = it },
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
        } else if (imageCapture == null && state is CameraUiState.Idle) {
            // CameraX bind-pending window: nothing to capture yet. Inform the
            // user and keep the shutter disabled so taps aren't silently
            // swallowed by `takeJpegPicture`'s early-return (UX 1).
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.testTag(CameraScreenTags.BIND_PROGRESS),
                )
            }
        }

        if (state is CameraUiState.Failure) {
            val reason = (state as CameraUiState.Failure).reason
            val retryLabel = stringResource(id = R.string.camera_failure_retry)
            val iconDescription = stringResource(id = R.string.camera_failure_banner_content_description)
            // Bottom-anchored banner (Material3 has no first-class Banner composable;
            // built as OutlinedCard per §4.1). `bottom = 144.dp` keeps the FAB shutter
            // visible above the banner — verified by §2.5 / manual check in §7.6.
            OutlinedCard(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 144.dp)
                        .testTag(CameraScreenTags.FAILURE_BANNER),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = iconDescription,
                    )
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier =
                            Modifier
                                .weight(1f)
                                .testTag(CameraScreenTags.ERROR),
                    )
                    TextButton(
                        onClick = { viewModel.reset() },
                        modifier = Modifier.testTag(CameraScreenTags.FAILURE_RETRY),
                    ) {
                        Text(text = retryLabel)
                    }
                }
            }
        }

        val shutterLabel = stringResource(id = R.string.camera_shutter_label)
        val shutterEnabled =
            (state is CameraUiState.Idle || state is CameraUiState.Failure) &&
                imageCapture != null
        FloatingActionButton(
            onClick = {
                if (!shutterEnabled) return@FloatingActionButton
                val capture = imageCapture ?: return@FloatingActionButton
                takeJpegPicture(capture, executor, viewModel)
            },
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 104.dp)
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

        // PLANTPOTTING-0010 (review feedback) — full-width Home button at the bottom, like every
        // other screen. Sits below the (raised) shutter.
        HomeButton(
            onHome = onHome,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
        )
    }

    DisposableEffect(Unit) {
        onDispose { /* CameraX use cases unbind on lifecycle stop. */ }
    }
}

object CameraScreenTags {
    const val PREVIEW = "camera.preview"
    const val SHUTTER = "camera.shutter"

    /** Back-compat tag preserved on the banner body text (was top-center Text pre-0005). */
    const val ERROR = "camera.error"
    const val BIND_PROGRESS = "camera.bindProgress"
    const val FAILURE_BANNER = "camera.failureBanner"
    const val FAILURE_RETRY = "camera.failureRetry"
}

/**
 * Test-only handles for [CameraScreen]. The view-model field is populated
 * by [CameraScreen] during composition because the view model is created
 * in the Compose Navigation back-stack-entry's `ViewModelStore`, which an
 * instrumentation test cannot reach from `MainActivity` alone.
 *
 * - [testImageCapture] — when non-null, replaces the real CameraX-bound
 *   [ImageCapture] in [CameraScreen]. Tests use this to assert shutter
 *   enablement without standing up a real camera.
 * - [forceSkipBind] — when true, [CameraScreen] skips the real
 *   `bindCameraUseCases` call inside `AndroidView.factory`. Necessary
 *   for the UX 1 "bind-pending" assertion on AVDs that ship with an
 *   emulated back camera (`bindCameraUseCases` would otherwise succeed
 *   on the GMD and leave `imageCapture` non-null, making the
 *   bind-pending state unobservable).
 *
 * Both fields are held in Compose [androidx.compose.runtime.MutableState]
 * so the screen recomposes when they change. Production never writes to
 * them.
 */
internal object CameraScreenTestRegistry {
    @Volatile
    var current: CameraViewModel? = null

    val testImageCaptureState: androidx.compose.runtime.MutableState<ImageCapture?> =
        androidx.compose.runtime.mutableStateOf(null)

    val forceSkipBindState: androidx.compose.runtime.MutableState<Boolean> =
        androidx.compose.runtime.mutableStateOf(false)

    var testImageCapture: ImageCapture?
        get() = testImageCaptureState.value
        set(value) {
            testImageCaptureState.value = value
        }

    var forceSkipBind: Boolean
        get() = forceSkipBindState.value
        set(value) {
            forceSkipBindState.value = value
        }
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
