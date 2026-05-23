package io.github.cming0420.agenticledger

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey
@Serializable data class Tutorial(val type: String) : NavKey
