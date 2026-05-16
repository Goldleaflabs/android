package com.goldleaf.core.data.mapper


import com.goldleaf.core.data.dto.LabTestDto
import com.goldleaf.core.data.local.LabTest

fun LabTestDto.toDomain(): LabTest {
    return LabTest(
        id = id,
        batchId=batchId,
        testType=testType,
        testDate=testDate,
        labName=labName,
        status=status,
        isPassed=isPassed,
    resultUrl= resultUrl,
    notes = notes

    )
}
