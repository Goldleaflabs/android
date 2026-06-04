package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val farmerId: String,
    val batchId: String? = null,
    val grossAmount: Double = 0.0,
    val totalDeductions: Double = 0.0,
    val netAmount: Double = 0.0,
    val rate: Double = 0.0,
    val mpesaPhone: String? = null,
    val mpesaTransactionId: String? = null,
    val status: String = "PENDING",
    val paidBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "harvest_deliveries")
data class HarvestDeliveryEntity(
    @PrimaryKey val id: String,
    val farmerId: String,
    val batchId: String? = null,
    val declaredKg: Double = 0.0,
    val confirmedKg: Double = 0.0,
    val rejectKg: Double = 0.0,
    val grade: String? = null,
    val receivedDate: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "batch_sales")
data class BatchSalesEntity(
    @PrimaryKey val id: String,
    val batchId: String,
    val buyerName: String,
    val quantitySold: Double = 0.0,
    val unitPrice: Double = 0.0,
    val totalAmount: Double = 0.0,
    val saleDate: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "farmer_deductions")
data class DeductionEntity(
    @PrimaryKey val id: String,
    val farmerId: String,
    val batchId: String? = null,
    val type: String,
    val description: String? = null,
    val amount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "farmer_payout_info")
data class FarmerPayoutInfoEntity(
    @PrimaryKey val farmerId: String,
    val mpesaPhone: String,
    val fullName: String? = null,
    val idNumber: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
