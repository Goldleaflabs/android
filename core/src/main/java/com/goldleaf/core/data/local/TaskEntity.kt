package com.goldleaf.core.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = FarmerEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farmId"])]  // ADD THIS LINE
)


data class TaskEntity(
    @PrimaryKey
    val id: String,
    val farmId: String,
    val cropId: String,
    val title: String,
    val taskName: String,
    val taskType: String,
    val status: String, // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    val dueDate: String, // ISO 8601 format: "yyyy-MM-dd"
    val completedAt: Long? = null,
    val description: String?,
    val priority: TaskPriority,
    val isCompleted: Boolean,
    val completedDate: String?,
    val assignedTo: String?,
    val estimatedDuration: Int?, // in minutes
    val actualDuration: Int?,
    val notes: String?,
    val category: TaskCategory,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val farmerId: String? = null

)
