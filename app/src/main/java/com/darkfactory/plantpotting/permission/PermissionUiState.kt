package com.darkfactory.plantpotting.permission

sealed interface PermissionUiState {
    data object NotYetAsked : PermissionUiState
    data object Denied : PermissionUiState
    data object PermanentlyDenied : PermissionUiState
    data object Granted : PermissionUiState
}
