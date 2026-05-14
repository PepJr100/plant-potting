package com.darkfactory.plantpotting.permission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.darkfactory.plantpotting.R

/**
 * Stateless permission screen. Callers wire state + callbacks. Testable in
 * isolation by passing a [PermissionUiState] fixture.
 */
@Composable
fun PermissionScreen(
    state: PermissionUiState,
    onGrantClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var whyExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (state) {
            PermissionUiState.Granted -> {
                Text(
                    text = stringResource(id = R.string.permission_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            PermissionUiState.PermanentlyDenied -> {
                Text(
                    text = stringResource(id = R.string.permission_denied_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = stringResource(id = R.string.permission_denied_body),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onOpenSettingsClick,
                    modifier = Modifier.testTag(PermissionScreenTags.OPEN_SETTINGS_BUTTON),
                ) {
                    Text(stringResource(id = R.string.permission_open_settings))
                }
            }
            else -> {
                Text(
                    text = stringResource(id = R.string.permission_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = stringResource(id = R.string.permission_rationale),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag(PermissionScreenTags.RATIONALE),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onGrantClick,
                    modifier = Modifier.testTag(PermissionScreenTags.GRANT_BUTTON),
                ) {
                    Text(stringResource(id = R.string.permission_grant))
                }
                TextButton(
                    onClick = { whyExpanded = !whyExpanded },
                    modifier = Modifier.testTag(PermissionScreenTags.WHY_TOGGLE),
                ) {
                    Text(stringResource(id = R.string.permission_why_we_ask))
                }
                if (whyExpanded) {
                    Text(
                        text = stringResource(id = R.string.permission_why_we_ask_body),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag(PermissionScreenTags.WHY_BODY),
                    )
                }
            }
        }
    }
}

object PermissionScreenTags {
    const val RATIONALE = "permission.rationale"
    const val GRANT_BUTTON = "permission.grant"
    const val WHY_TOGGLE = "permission.why.toggle"
    const val WHY_BODY = "permission.why.body"
    const val OPEN_SETTINGS_BUTTON = "permission.openSettings"
}

/**
 * Wires the real permission launcher around [PermissionScreen].
 *
 * The platform permission state is *not* trustworthy from inline recomposition
 * alone, because returning from `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`
 * fires `MainActivity.onResume` without otherwise invalidating composition.
 * To recover from a Settings round-trip (Bug 4 / PLANTPOTTING-0002 §1), the
 * host holds the last-seen grant in [mutableStateOf], registers a
 * [LifecycleEventObserver], and re-invokes [CameraPermissionGuard.isGranted]
 * on every `ON_RESUME` — clearing the local `permanentlyDenied` flag when the
 * platform now reports granted.
 *
 * `onGranted` is invoked at most once per Granted transition. The
 * `popUpTo(permission, inclusive = true)` in the caller is the
 * back-stack-level safeguard; the `alreadyNavigated` flag here is the
 * per-composition belt-and-braces guard.
 *
 * @param initialPermanentlyDenied for tests only — production callers leave
 *   this at the default `false`. Tests use `true` to simulate the
 *   "permanently denied" state without driving the system permission dialog.
 */
@Composable
fun PermissionScreenHost(
    guard: CameraPermissionGuard,
    onGranted: () -> Unit,
    initialPermanentlyDenied: Boolean = false,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasRequested by rememberSaveable { mutableStateOf(false) }
    var permanentlyDenied by rememberSaveable { mutableStateOf(initialPermanentlyDenied) }
    var isGrantedNow by remember { mutableStateOf(guard.isGranted()) }
    var alreadyNavigated by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, guard) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    val granted = guard.isGranted()
                    isGrantedNow = granted
                    if (granted) {
                        permanentlyDenied = false
                    }
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasRequested = true
            isGrantedNow = granted
            if (granted) {
                permanentlyDenied = false
            } else {
                // If after a denial the system says no rationale is available, the
                // user has selected "don't ask again".
                val activity = context as? android.app.Activity
                permanentlyDenied = activity != null && !guard.shouldShowRationale(activity)
            }
        }

    val state: PermissionUiState =
        when {
            isGrantedNow -> PermissionUiState.Granted
            permanentlyDenied -> PermissionUiState.PermanentlyDenied
            hasRequested -> PermissionUiState.Denied
            else -> PermissionUiState.NotYetAsked
        }

    LaunchedEffect(state) {
        if (state is PermissionUiState.Granted && !alreadyNavigated) {
            alreadyNavigated = true
            onGranted()
        }
    }

    PermissionScreen(
        state = state,
        onGrantClick = { launcher.launch(Manifest.permission.CAMERA) },
        onOpenSettingsClick = {
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            context.startActivity(intent)
        },
    )
    // Mark the activity-result owner reference to suppress an "unused" warning
    // if the launcher path is reached without LocalActivityResultRegistryOwner.
    @Suppress("UNUSED_VARIABLE")
    val registryOwner = LocalActivityResultRegistryOwner.current
}
