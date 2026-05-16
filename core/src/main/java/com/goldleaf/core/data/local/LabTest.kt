package com.goldleaf.core.data.local


data class LabTest(
    val id: String,
    val batchId: String,
    val testType: String,
    val testDate: String,
    val labName: String,
    val status: String,
    val isPassed: Boolean? = null,
    val resultUrl: String? = null,
    val notes: String? = null
)