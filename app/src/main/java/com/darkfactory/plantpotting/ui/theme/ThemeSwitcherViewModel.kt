package com.darkfactory.plantpotting.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkfactory.plantpotting.persistence.PlantLogStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PLANTPOTTING-0010 D5 — debug-only theme switcher state. Reads/writes the persisted candidate via
 * the shared [PlantLogStore]. Production never reaches the switcher (gated by `BuildConfig.DEBUG` at
 * the affordance), but this VM carries no debug guard of its own.
 */
@HiltViewModel
class ThemeSwitcherViewModel
    @Inject
    constructor(
        private val store: PlantLogStore,
    ) : ViewModel() {
        val selected: StateFlow<ThemeCandidate> =
            store.themeCandidate
                .map { ThemeCandidate.fromName(it) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = ThemeCandidate.LEAF,
                )

        fun select(candidate: ThemeCandidate) {
            viewModelScope.launch { store.setThemeCandidate(candidate.name) }
        }
    }
