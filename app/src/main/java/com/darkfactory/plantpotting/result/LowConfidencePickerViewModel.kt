package com.darkfactory.plantpotting.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.Species
import com.darkfactory.plantpotting.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URLDecoder
import javax.inject.Inject

/**
 * PLANTPOTTING-0003 §6.2 — drives the low-confidence picker screen.
 *
 * Reads the up-to-3 mapped candidates from the nav arg (encoded by
 * `Routes.lowConfidencePicker(...)`), then exposes:
 *  - [topCandidates]: parsed candidate list (empty for the "unmapped" path)
 *  - [allSpecies]: the full 16-species KB list, for the manual search fallback
 *  - [query] + [onQueryChange] + [filteredSpecies]: a simple search filter
 *
 * Navigation actions (tap a top chip / select a species / tap "pick by archetype") are
 * surfaced via callbacks on the screen composable — this VM only owns data.
 */
@HiltViewModel
class LowConfidencePickerViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        kb: KnowledgeBase,
    ) : ViewModel() {
        val allSpecies: List<Species> = kb.species

        private val rawCandidates: String =
            savedStateHandle.get<String>(Routes.ARG_CANDIDATES).orEmpty().let { raw ->
                runCatching { URLDecoder.decode(raw, "UTF-8") }.getOrDefault(raw)
            }

        val topCandidates: List<TopCandidate> = parseCandidates(rawCandidates, kb)

        private val _query = MutableStateFlow("")
        val query: StateFlow<String> = _query.asStateFlow()

        private val _filteredSpecies = MutableStateFlow(allSpecies)
        val filteredSpecies: StateFlow<List<Species>> = _filteredSpecies.asStateFlow()

        fun onQueryChange(newQuery: String) {
            _query.value = newQuery
            _filteredSpecies.value = filterImpl(newQuery)
        }

        private fun filterImpl(query: String): List<Species> {
            if (query.isBlank()) return allSpecies
            val needle = query.trim().lowercase()
            return allSpecies.filter { sp ->
                sp.scientificName.lowercase().contains(needle) ||
                    sp.commonNames.any { it.lowercase().contains(needle) }
            }
        }

        data class TopCandidate(
            val speciesId: String,
            val scientificName: String,
            val commonName: String,
            val probabilityPct: Int,
        )

        private fun parseCandidates(
            raw: String,
            kb: KnowledgeBase,
        ): List<TopCandidate> {
            if (raw.isBlank()) return emptyList()
            return raw
                .split(',')
                .mapNotNull { row ->
                    val pieces = row.split('|')
                    if (pieces.size != 2) return@mapNotNull null
                    val speciesId = pieces[0]
                    val pct = pieces[1].toIntOrNull() ?: return@mapNotNull null
                    val species = kb.findSpecies(speciesId) ?: return@mapNotNull null
                    TopCandidate(
                        speciesId = species.id,
                        scientificName = species.scientificName,
                        commonName = species.commonNames.firstOrNull().orEmpty(),
                        probabilityPct = pct.coerceIn(0, 100),
                    )
                }.take(3)
        }
    }
