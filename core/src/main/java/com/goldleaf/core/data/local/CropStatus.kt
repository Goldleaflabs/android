package com.goldleaf.core.data.local

enum class CropStatus {
    PLANNED,
    PLANTED,
    GROWING,
    HARVESTED,
    COMPLETED,
    FAILED;

    fun getDisplayName(): String = when (this) {
        PLANNED -> "Planned"
        PLANTED -> "Planted"
        GROWING -> "Growing"
        HARVESTED -> "Ready to Harvest"
        COMPLETED -> "Completed"
        FAILED -> "Failed"
    }

    fun getOrder(): Int = when (this) {
        PLANNED -> 0
        PLANTED -> 1
        GROWING -> 2
        HARVESTED -> 3
        COMPLETED -> 4
        FAILED -> -1  // Exception status
    }

    fun canTransitionTo(nextStatus: CropStatus): Boolean {
        // FAILED can be reached from any status
        if (nextStatus == FAILED) return true
        
        // Cannot transition FROM terminal states
        if (this == COMPLETED || this == FAILED) return false
        
        // Otherwise, only allow forward progression
        return nextStatus.getOrder() > this.getOrder()
    }

    fun getValidNextStatuses(): List<CropStatus> {
        return when (this) {
            PLANNED -> listOf(PLANTED, FAILED)
            PLANTED -> listOf(GROWING, FAILED)
            GROWING -> listOf(HARVESTED, FAILED)
            HARVESTED -> listOf(COMPLETED, FAILED)
            COMPLETED -> emptyList()  // Terminal state
            FAILED -> emptyList()     // Terminal state
        }
    }

    companion object {
        fun fromString(value: String): CropStatus {
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                PLANNED
            }
        }
    }
}


enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

enum class TaskCategory {
    PLANTING,
    IRRIGATION,
    FERTILIZATION,
    PEST_CONTROL,
    DISEASE_MANAGEMENT,
    WEEDING,
    PRUNING,
    HARVESTING,
    MONITORING,
    OTHER
}

enum class HealthStatus {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    CRITICAL
}

enum class GrowthStage {
    SEED_PREPARATION,
    PLANTING,
    GERMINATION,
    VEGETATIVE,
    FLOWERING,
    FRUIT_DEVELOPMENT,
    MATURATION,
    HARVEST
}

enum class EventType {
    PLANTING,
    IRRIGATION,
    FERTILIZATION,
    PEST_TREATMENT,
    HARVEST,
    MONITORING,
    MAINTENANCE
}

enum class CertificationStatus {
    PENDING,
    MET,
    NOT_MET
}