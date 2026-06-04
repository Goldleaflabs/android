package com.goldleaf.core.data.local.dao

import androidx.room.*
import com.goldleaf.core.data.local.*

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE farmerId = :farmerId ORDER BY createdAt DESC")
    suspend fun getPaymentsByFarmer(farmerId: String): List<PaymentEntity>

    @Query("SELECT SUM(netAmount) FROM payments WHERE farmerId = :farmerId AND status = 'SUCCESS'")
    suspend fun getTotalPaid(farmerId: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)
}

@Dao
interface HarvestDeliveryDao {
    @Query("SELECT * FROM harvest_deliveries WHERE farmerId = :farmerId ORDER BY receivedDate DESC")
    suspend fun getByFarmer(farmerId: String): List<HarvestDeliveryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveries(deliveries: List<HarvestDeliveryEntity>)
}

@Dao
interface BatchSalesDao {
    @Query("SELECT * FROM batch_sales WHERE batchId = :batchId ORDER BY saleDate DESC")
    suspend fun getByBatch(batchId: String): List<BatchSalesEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<BatchSalesEntity>)
}

@Dao
interface DeductionDao {
    @Query("SELECT * FROM farmer_deductions WHERE farmerId = :farmerId ORDER BY createdAt DESC")
    suspend fun getByFarmer(farmerId: String): List<DeductionEntity>

    @Query("SELECT SUM(amount) FROM farmer_deductions WHERE farmerId = :farmerId")
    suspend fun getTotalDeductions(farmerId: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeductions(deductions: List<DeductionEntity>)
}

@Dao
interface FarmerPayoutInfoDao {
    @Query("SELECT * FROM farmer_payout_info WHERE farmerId = :farmerId")
    suspend fun getByFarmer(farmerId: String): FarmerPayoutInfoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(info: FarmerPayoutInfoEntity)
}
