package com.anix.rx.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String? = null,
    val data: T? = null
)
