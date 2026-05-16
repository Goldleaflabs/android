package com.goldleaf.core.data.mapper

import com.goldleaf.core.data.dto.BlockchainRecordDto
import com.goldleaf.core.data.local.BlockchainRecord
import com.goldleaf.core.data.local.BlockchainStatus

fun BlockchainRecordDto.toDomain(): BlockchainRecord {
   return BlockchainRecord(
        id=id,
        batchId=batchId,
        transactionHash=transactionHash,
        blockNumber=blockNumber,
        network=network,
        timestamp=timestamp,
        status= BlockchainStatus.valueOf(status)
    )
}