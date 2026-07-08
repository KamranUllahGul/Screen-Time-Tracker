package com.kamran.screentimetracker.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination : NavKey {
    @Serializable
    data object Dashboard : Destination
    @Serializable
    data object Stats : Destination
    @Serializable
    data object Settings : Destination
}
