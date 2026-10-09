package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.entities.CollectionEntity
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.OrganizationEntity
import com.example.data.local.entities.OtherIncomeEntity
import com.example.data.model.BengaliFormatter
import com.example.data.model.FinancialSummary
import com.example.data.model.MemberWithFinancials
import com.example.data.model.PaymentStatus
import java.io.File
import java.io.FileOutputStream

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN_X = 36f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_X * 2)

    fun generateFinancialReportPdf(
        context: Context,
        organization: OrganizationEntity?,
        financialSummary: FinancialSummary,
        members: List<MemberWithFinancials>,
        collections: List<CollectionEntity>,
        expenses: List<ExpenseEntity>,
        otherIncomes: List<OtherIncomeEntity>
    ): File {
        val pdfDocument = PdfDocument()

        val titlePaint = Paint().apply {
            color = Color.rgb(13, 92, 58) // Primary Green
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subTitlePaint = Paint().apply {
            color = Color.rgb(80, 80, 80)
            textSize = 10f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(13, 92, 58)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9.5f
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(210, 215, 212)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val bgPaint = Paint().apply {
            color = Color.rgb(244, 248, 246)
            style = Paint.Style.FILL
        }

        val greenBgPaint = Paint().apply {
            color = Color.rgb(230, 244, 237)
            style = Paint.Style.FILL
        }

        val amberTextPaint = Paint().apply {
            color = Color.rgb(180, 83, 9)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val greenTextPaint = Paint().apply {
            color = Color.rgb(15, 157, 88)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val redTextPaint = Paint().apply {
            color = Color.rgb(217, 48, 37)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val orgName = organization?.name ?: "আমাদের সংগঠন"
        val orgType = organization?.type ?: "সংগঠন"
        val orgAddress = organization?.address.orEmpty()
        val orgPhone = organization?.phone.orEmpty()
        val adminName = organization?.adminName ?: "কর্তৃপক্ষ"
        val adminRole = organization?.adminRole ?: "দায়িত্বশীল"

        val anonymousCollections = collections.filter { it.memberId == null }
        val anonymousTotal = anonymousCollections.sumOf { it.amount }
        val memberTotal = collections.filter { it.memberId != null }.sumOf { it.amount }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var y = 45f

        // Top Brand Header Bar
        canvas.drawRect(MARGIN_X, y - 15f, PAGE_WIDTH - MARGIN_X, y - 10f, titlePaint)
        y += 10f

        // Organization Info
        canvas.drawText(orgName, MARGIN_X, y, titlePaint)
        y += 14f

        val subtitle = buildString {
            append(orgType)
            if (orgAddress.isNotEmpty()) append(" • $orgAddress")
            if (orgPhone.isNotEmpty()) append(" • মোবাইল: ${BengaliFormatter.toBengaliDigits(orgPhone)}")
        }
        canvas.drawText(subtitle, MARGIN_X, y, subTitlePaint)
        y += 18f

        // Document Title
        canvas.drawText("আর্থিক বিবরণী ও চাঁদা অডিট রিপোর্ট", MARGIN_X, y, headerPaint)
        val dateStr = "তারিখ: ${BengaliFormatter.formatDate(System.currentTimeMillis())}"
        canvas.drawText(dateStr, PAGE_WIDTH - MARGIN_X - 110f, y, subTitlePaint)
        y += 8f

        canvas.drawLine(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y, linePaint)
        y += 15f

        // --- Summary Card Box ---
        val summaryBoxHeight = 85f
        canvas.drawRoundRect(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + summaryBoxHeight, 8f, 8f, bgPaint)
        canvas.drawRoundRect(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + summaryBoxHeight, 8f, 8f, linePaint)

        val col1X = MARGIN_X + 14f
        val col2X = MARGIN_X + 180f
        val col3X = MARGIN_X + 350f
        var sumY = y + 18f

        canvas.drawText("মোট সদস্য: ${BengaliFormatter.toBengaliDigits(financialSummary.totalMembers)} জন", col1X, sumY, boldTextPaint)
        canvas.drawText("মোট চাঁদা ধার্য: ${BengaliFormatter.formatCurrency(financialSummary.totalAssignedFee)}", col2X, sumY, textPaint)
        canvas.drawText("মোট খরচ: ${BengaliFormatter.formatCurrency(financialSummary.totalExpense)}", col3X, sumY, redTextPaint)
        sumY += 16f

        canvas.drawText("সদস্য আদায়: ${BengaliFormatter.formatCurrency(memberTotal)}", col1X, sumY, textPaint)
        canvas.drawText("দ্রুত/বেনামী দান: ${BengaliFormatter.formatCurrency(anonymousTotal)} (${BengaliFormatter.toBengaliDigits(anonymousCollections.size)}টি)", col2X, sumY, amberTextPaint)
        canvas.drawText("অন্যান্য আয়: ${BengaliFormatter.formatCurrency(financialSummary.totalOtherIncome)}", col3X, sumY, textPaint)
        sumY += 16f

        canvas.drawLine(col1X, sumY - 2f, PAGE_WIDTH - MARGIN_X - 14f, sumY - 2f, linePaint)
        sumY += 14f

        canvas.drawText("সর্বমোট আদায় ও আয়: ${BengaliFormatter.formatCurrency(financialSummary.totalIncome)}", col1X, sumY, greenTextPaint)
        canvas.drawText("মোট বকেয়া: ${BengaliFormatter.formatCurrency(financialSummary.totalDue)}", col2X, sumY, amberTextPaint)
        canvas.drawText("উদ্বৃত্ত ব্যালেন্স: ${BengaliFormatter.formatCurrency(financialSummary.netBalance)}", col3X, sumY, titlePaint.apply { textSize = 11f })

        y += summaryBoxHeight + 20f

        // --- Members Statement Table ---
        canvas.drawText("সদস্যদের চাঁদা ও বকেয়া বিবরণী", MARGIN_X, y, headerPaint)
        y += 10f

        val rowHeight = 18f
        val colNo = MARGIN_X + 6f
        val colName = MARGIN_X + 30f
        val colId = MARGIN_X + 155f
        val colPhone = MARGIN_X + 215f
        val colFee = MARGIN_X + 300f
        val colPaid = MARGIN_X + 370f
        val colDue = MARGIN_X + 435f
        val colStatus = MARGIN_X + 490f

        // Table Header
        canvas.drawRect(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + rowHeight, greenBgPaint)
        canvas.drawLine(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y, linePaint)
        canvas.drawLine(MARGIN_X, y + rowHeight, PAGE_WIDTH - MARGIN_X, y + rowHeight, linePaint)

        val thY = y + 12f
        canvas.drawText("নং", colNo, thY, boldTextPaint)
        canvas.drawText("সদস্যের নাম", colName, thY, boldTextPaint)
        canvas.drawText("আইডি", colId, thY, boldTextPaint)
        canvas.drawText("মোবাইল", colPhone, thY, boldTextPaint)
        canvas.drawText("ধার্য", colFee, thY, boldTextPaint)
        canvas.drawText("জমা", colPaid, thY, boldTextPaint)
        canvas.drawText("বকেয়া", colDue, thY, boldTextPaint)
        canvas.drawText("স্ট্যাটাস", colStatus, thY, boldTextPaint)

        y += rowHeight

        // Render member rows
        members.forEachIndexed { index, m ->
            if (y > PAGE_HEIGHT - 60f) {
                // Page overflow: close current page and create next
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 40f

                // Repeat table header on next page
                canvas.drawRect(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + rowHeight, greenBgPaint)
                canvas.drawLine(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y, linePaint)
                canvas.drawLine(MARGIN_X, y + rowHeight, PAGE_WIDTH - MARGIN_X, y + rowHeight, linePaint)
                val newThY = y + 12f
                canvas.drawText("নং", colNo, newThY, boldTextPaint)
                canvas.drawText("সদস্যের নাম", colName, newThY, boldTextPaint)
                canvas.drawText("আইডি", colId, newThY, boldTextPaint)
                canvas.drawText("মোবাইল", colPhone, newThY, boldTextPaint)
                canvas.drawText("ধার্য", colFee, newThY, boldTextPaint)
                canvas.drawText("জমা", colPaid, newThY, boldTextPaint)
                canvas.drawText("বকেয়া", colDue, newThY, boldTextPaint)
                canvas.drawText("স্ট্যাটাস", colStatus, newThY, boldTextPaint)
                y += rowHeight
            }

            if (index % 2 == 1) {
                canvas.drawRect(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + rowHeight, bgPaint)
            }
            canvas.drawLine(MARGIN_X, y + rowHeight, PAGE_WIDTH - MARGIN_X, y + rowHeight, linePaint)

            val rY = y + 12f
            canvas.drawText(BengaliFormatter.toBengaliDigits(index + 1), colNo, rY, textPaint)
            val nameDisplay = if (m.member.name.length > 20) m.member.name.take(18) + ".." else m.member.name
            canvas.drawText(nameDisplay, colName, rY, boldTextPaint)
            canvas.drawText(m.member.memberCode, colId, rY, textPaint)
            val phoneDisplay = m.member.phone.ifEmpty { "-" }
            canvas.drawText(BengaliFormatter.toBengaliDigits(phoneDisplay), colPhone, rY, textPaint)
            canvas.drawText(BengaliFormatter.formatCurrency(m.member.assignedFee), colFee, rY, textPaint)
            canvas.drawText(BengaliFormatter.formatCurrency(m.totalPaid), colPaid, rY, greenTextPaint)
            canvas.drawText(BengaliFormatter.formatCurrency(m.dueAmount), colDue, rY, if (m.dueAmount > 0) redTextPaint else textPaint)
            val statusLabel = when (m.status) {
                PaymentStatus.PAID -> "পরিশোধ"
                PaymentStatus.PARTIAL -> "আংশিক"
                PaymentStatus.UNPAID -> "বকেয়া"
            }
            val statusPaint = when (m.status) {
                PaymentStatus.PAID -> greenTextPaint
                PaymentStatus.PARTIAL -> amberTextPaint
                PaymentStatus.UNPAID -> redTextPaint
            }
            canvas.drawText(statusLabel, colStatus, rY, statusPaint)

            y += rowHeight
        }

        y += 35f

        // Signature section (if enough space, otherwise ensure room)
        if (y > PAGE_HEIGHT - 65f) {
            pdfDocument.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            y = 60f
        }

        val sigLinePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 0.8f
        }
        val sig1X = MARGIN_X + 20f
        val sig2X = PAGE_WIDTH - MARGIN_X - 160f

        canvas.drawLine(sig1X, y, sig1X + 130f, y, sigLinePaint)
        canvas.drawLine(sig2X, y, sig2X + 130f, y, sigLinePaint)

        y += 12f
        canvas.drawText("প্রস্তুতকারক: $adminName", sig1X, y, subTitlePaint)
        canvas.drawText("অনুমোদনকারী / সভাপতির স্বাক্ষর", sig2X, y, subTitlePaint)

        y += 10f
        canvas.drawText("পদবী: $adminRole", sig1X, y, subTitlePaint)
        canvas.drawText(orgName, sig2X, y, subTitlePaint)

        // Footer note
        canvas.drawText("🛡️ চাঁদা হিসাব মোবাইল অ্যাপ্লিকেশন দ্বারা সম্পূর্ণ অফলাইনে প্রস্তুতকৃত", MARGIN_X, PAGE_HEIGHT - 20f, subTitlePaint)
        canvas.drawText("পৃষ্ঠা $pageNumber", PAGE_WIDTH - MARGIN_X - 40f, PAGE_HEIGHT - 20f, subTitlePaint)

        pdfDocument.finishPage(page)

        // Save to cache dir
        val cleanOrgName = orgName.replace(Regex("[^a-zA-Z0-9\\u0980-\\u09FF]"), "_")
        val file = File(context.cacheDir, "Chada_Report_${cleanOrgName}_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        pdfDocument.close()

        return file
    }

    fun generateExcelCsv(
        context: Context,
        organization: OrganizationEntity?,
        financialSummary: FinancialSummary,
        members: List<MemberWithFinancials>,
        collections: List<CollectionEntity>,
        expenses: List<ExpenseEntity>
    ): File {
        val cleanOrgName = (organization?.name ?: "Chada_Report").replace(Regex("[^a-zA-Z0-9\\u0980-\\u09FF]"), "_")
        val file = File(context.cacheDir, "Chada_Report_${cleanOrgName}_${System.currentTimeMillis()}.csv")

        val sb = StringBuilder()
        // UTF-8 BOM to guarantee Excel displays Bengali characters without mojibake
        sb.append("\uFEFF")

        sb.appendLine("\"সংগঠনের নাম:\",\"${organization?.name ?: "আমাদের সংগঠন"}\"")
        sb.appendLine("\"ধরন:\",\"${organization?.type ?: ""}\"")
        sb.appendLine("\"দায়িত্বশীল:\",\"${organization?.adminName ?: ""} (${organization?.adminRole ?: ""})\"")
        sb.appendLine("\"তারিখ:\",\"${BengaliFormatter.formatDate(System.currentTimeMillis())}\"")
        sb.appendLine()
        sb.appendLine("\"আর্থিক সারসংক্ষেপ:\"")
        sb.appendLine("\"মোট সদস্য\",\"${financialSummary.totalMembers}\"")
        sb.appendLine("\"মোট ধার্য চাঁদা\",\"${financialSummary.totalAssignedFee}\"")
        sb.appendLine("\"মোট আদায়\",\"${financialSummary.totalCollectedFee}\"")
        sb.appendLine("\"অন্যান্য আয়\",\"${financialSummary.totalOtherIncome}\"")
        sb.appendLine("\"সর্বমোট আয়\",\"${financialSummary.totalIncome}\"")
        sb.appendLine("\"মোট খরচ\",\"${financialSummary.totalExpense}\"")
        sb.appendLine("\"বর্তমান ব্যালেন্স\",\"${financialSummary.netBalance}\"")
        sb.appendLine("\"মোট বকেয়া\",\"${financialSummary.totalDue}\"")
        sb.appendLine()
        sb.appendLine("\"সদস্য তালিকা ও চাঁদার হিসাব:\"")
        sb.appendLine("\"ক্রমিং নং\",\"সদস্য আইডি\",\"নাম\",\"পদবী\",\"মোবাইল\",\"ঠিকানা\",\"চাঁদার ধরন\",\"ধার্য চাঁদা\",\"মোট জমা\",\"বকেয়া\",\"স্ট্যাটাস\"")

        members.forEachIndexed { index, m ->
            val statusBn = when (m.status) {
                PaymentStatus.PAID -> "পরিশোধিত"
                PaymentStatus.PARTIAL -> "আংশিক বাকি"
                PaymentStatus.UNPAID -> "বকেয়া"
            }
            sb.appendLine("\"${index + 1}\",\"${m.member.memberCode}\",\"${m.member.name}\",\"${m.member.designation}\",\"${m.member.phone}\",\"${m.member.address}\",\"${m.member.feeType}\",${m.member.assignedFee},${m.totalPaid},${m.dueAmount},\"$statusBn\"")
        }

        val outputStream = FileOutputStream(file)
        outputStream.write(sb.toString().toByteArray(Charsets.UTF_8))
        outputStream.flush()
        outputStream.close()

        return file
    }
}
