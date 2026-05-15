package com.darkfactory.plantpotting.identify

import javax.inject.Qualifier

/**
 * Hilt qualifier for the on-device classifier's label vocabulary (line N = class index N
 * from `labels.csv`). Avoids colliding with any other `List<String>` binding in the graph.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ModelLabelsList
