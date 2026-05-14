package com.darkfactory.plantpotting.kb.model

data class KnowledgeBase(
    val archetypes: Map<String, Archetype>,
    val species: List<Species>,
    val speciesIndex: Map<String, Species>,
) {
    fun findSpecies(idOrAlias: String): Species? = speciesIndex[normalise(idOrAlias)]

    companion object {
        fun normalise(s: String): String = s.trim().lowercase().replace('’', '\'')
    }
}
