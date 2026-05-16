package com.goldleaf.certification.utils

// FILE: feature-certification-quality/src/main/java/com/goldleaf/certification/utils/PDFLabelGenerator.kt


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.goldleaf.core.data.local.ProductBatchEntity
import java.io.File
import java.io.FileOutputStream

object PDFLabelGenerator {

    // Standard sticker label size: 4" x 6" (10.16cm x 15.24cm) at 300 DPI
    private const val PAGE_WIDTH = 1200  // 4 inches * 300 DPI
    private const val PAGE_HEIGHT = 1800 // 6 inches * 300 DPI

    fun generatePrintableLabel(
        context: Context,
        batch: ProductBatchEntity,
        qrCode: Bitmap
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Setup paints
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 60f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 40f
            textAlign = Paint.Align.LEFT
        }

        val smallPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }

        var yPos = 100f

        // Header
        canvas.drawText("🌿 GOLD LEAF LABS", PAGE_WIDTH / 2f, yPos, titlePaint)
        yPos += 80

        canvas.drawText("Product Verification", PAGE_WIDTH / 2f, yPos, smallPaint)
        yPos += 100

        // QR Code (centered, large)
        val qrSize = 600
        val qrLeft = (PAGE_WIDTH - qrSize) / 2f
        canvas.drawBitmap(
            Bitmap.createScaledBitmap(qrCode, qrSize, qrSize, false),
            qrLeft,
            yPos,
            null
        )
        yPos += qrSize + 80

        // Scan instruction
        canvas.drawText("📱 SCAN TO VERIFY AUTHENTICITY", PAGE_WIDTH / 2f, yPos, smallPaint)
        yPos += 80

        // Divider line
        canvas.drawLine(100f, yPos, PAGE_WIDTH - 100f, yPos, Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 3f
        })
        yPos += 60

        // Product Information
        val leftMargin = 150f

        canvas.drawText("Batch Number:", leftMargin, yPos, bodyPaint)
        canvas.drawText(batch.batchNumber, leftMargin + 450f, yPos, bodyPaint.apply { isFakeBoldText = true })
        yPos += 60

        canvas.drawText("Product:", leftMargin, yPos, bodyPaint)
        canvas.drawText(batch.productType, leftMargin + 450f, yPos, bodyPaint)
        yPos += 60

        canvas.drawText("Quantity:", leftMargin, yPos, bodyPaint)
        canvas.drawText("${batch.quantity} ${batch.unit}", leftMargin + 450f, yPos, bodyPaint)
        yPos += 60

        canvas.drawText("Harvest:", leftMargin, yPos, bodyPaint)
        canvas.drawText(batch.harvestDate.toString(), leftMargin + 450f, yPos, bodyPaint)
        yPos += 60

        canvas.drawText("Farmer:", leftMargin, yPos, bodyPaint)
        canvas.drawText(batch.farmerName, leftMargin + 450f, yPos, bodyPaint)
        yPos += 80

        // Footer
        canvas.drawText("verify.goldleaflabs.co.ke", PAGE_WIDTH / 2f, yPos, smallPaint)

        pdfDocument.finishPage(page)

        // Save to file
        val fileName = "Label_${batch.batchNumber}_${System.currentTimeMillis()}.pdf"
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            fileName
        )

        FileOutputStream(file).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }

        pdfDocument.close()
        return file
    }
}