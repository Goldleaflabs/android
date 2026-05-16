package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "officers",
    indices = [Index(value = ["phone"], unique = true)]
)
data class Officer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val phone: String,
    val county: String,
    val region: String? = null,
    val status: String = "active",
    val farmerId: String? = null,
    val farmId: String? = null
)
