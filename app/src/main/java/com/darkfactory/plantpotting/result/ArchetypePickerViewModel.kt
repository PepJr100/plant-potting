package com.darkfactory.plantpotting.result

import androidx.lifecycle.ViewModel
import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * PLANTPOTTING-0003 §6.6 — surfaces the 8 KB archetypes as a flat list.
 *
 * Tapping an archetype routes to the archetype-recommendation destination, which reuses
 * `RecommendationScreen` with a `RecommendationViewModel` that called `recommendByArchetype`
 * instead of `recommend(speciesId)`.
 */
@HiltViewModel
class ArchetypePickerViewModel
    @Inject
    constructor(
        kb: KnowledgeBase,
    ) : ViewModel() {
        val archetypes: List<Archetype> = kb.archetypes.values.toList()
    }
