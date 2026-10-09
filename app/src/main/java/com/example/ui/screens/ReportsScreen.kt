package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.local.entities.CollectionEntity
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.OrganizationEntity
import com.example.data.local.entities.OtherIncomeEntity
import com.example.data.model.BengaliFormatter
import com.example.data.model.FinancialSummary
import com.example.data.model.MemberWithFinancials
import com.example.data.model.PaymentStatus
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.DueOrange
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryGreen
import com.example.util.PdfReportGenerator

@Composable
fun ReportsScreen(
    organization: OrganizationEntity?,
    financialSummary: FinancialSummary,
    members: List<MemberWithFinancials>,
    collections: List<CollectionEntity>,
    expenses: List<ExpenseEntity>,
    otherIncomes: List<OtherIncomeEntity>
) {
    val context = LocalContext.current
    val dueMembers = members.filter { it.dueAmount > 0 }.sortedByDescending { it.dueAmount }
    val anonymousCollections = collections.filter { it.memberId == null }
    val anonymousTotal = anonymousCollections.sumOf { it.amount }
    val memberCollectionsTotal = collections.filter { it.memberId != null }.sumOf { it.amount }

    val orgName = organization?.name ?: "আমাদের সংগঠন"
    val orgAdmin = organization?.adminName ?: "কর্তৃপক্ষ"
    val orgRole = organization?.adminRole ?: "দায়িত্বশীল"

    fun generateTextAuditReport(): String {
        return buildString {
            appendLine("════════════════════════════════")
            appendLine("  $orgName - আয়-ব্যয় ও বকেয়া হিসাব বিবরণী")
            appendLine("  তারিখ: ${BengaliFormatter.formatDate(System.currentTimeMillis())}")
            appendLine("════════════════════════════════")
            appendLine("📊 সাধারণ সারসংক্ষেপ:")
            appendLine("• মোট সদস্য: ${BengaliFormatter.toBengaliDigits(financialSummary.totalMembers)} জন")
            appendLine("• মোট চাঁদা ধার্য: ${BengaliFormatter.formatCurrency(financialSummary.totalAssignedFee)}")
            appendLine("• মোট আদায়কৃত চাঁদা: ${BengaliFormatter.formatCurrency(financialSummary.totalCollectedFee)}")
            if (anonymousCollections.isNotEmpty()) {
                appendLine("  └ নিবন্ধিত সদস্য চাঁদা: ${BengaliFormatter.formatCurrency(memberCollectionsTotal)}")
                appendLine("  └ দ্রুত/বেনামী মুক্তহস্তে দান: ${BengaliFormatter.formatCurrency(anonymousTotal)} (${BengaliFormatter.toBengaliDigits(anonymousCollections.size)} টি)")
            }
            appendLine("• অন্যান্য আয়/অনুদান: ${BengaliFormatter.formatCurrency(financialSummary.totalOtherIncome)}")
            appendLine("• সর্বমোট আয়: ${BengaliFormatter.formatCurrency(financialSummary.totalIncome)}")
            appendLine("• মোট খরচ: ${BengaliFormatter.formatCurrency(financialSummary.totalExpense)}")
            appendLine("────────────────────────────────")
            appendLine("💰 বর্তমান ব্যালেন্স: ${BengaliFormatter.formatCurrency(financialSummary.netBalance)}")
            appendLine("⚠️ মোট বকেয়া চাঁদা: ${BengaliFormatter.formatCurrency(financialSummary.totalDue)}")
            appendLine("════════════════════════════════")
            appendLine("📌 বকেয়া সদস্যদের তালিকা (${BengaliFormatter.toBengaliDigits(dueMembers.size)} জন):")
            dueMembers.forEachIndexed { index, m ->
                val num = BengaliFormatter.toBengaliDigits(index + 1)
                val phoneStr = if (m.member.phone.isNotEmpty()) " (${m.member.phone})" else ""
                appendLine("$num. ${m.member.name}$phoneStr - বকেয়া: ${BengaliFormatter.formatCurrency(m.dueAmount)}")
            }
            appendLine("════════════════════════════════")
            appendLine("রিপোর্ট প্রস্তুতকারক: $orgAdmin ($orgRole)")
        }
    }

    fun generateCsvReport(): String {
        return buildString {
            appendLine("Member Code,Member Name,Phone,Designation,Assigned Fee,Fee Type,Total Paid,Due Amount,Status")
            members.forEach { m ->
                val statusStr = when (m.status) {
                    PaymentStatus.PAID -> "Paid"
                    PaymentStatus.PARTIAL -> "Partial"
                    PaymentStatus.UNPAID -> "Unpaid"
                }
                appendLine("${m.member.memberCode},\"${m.member.name}\",\"${m.member.phone}\",\"${m.member.designation}\",${m.member.assignedFee},\"${m.member.feeType}\",${m.totalPaid},${m.dueAmount},$statusStr")
            }
        }
    }

    fun shareSummaryOnWhatsApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, generateTextAuditReport())
        }
        try {
            context.startActivity(Intent.createChooser(intent, "WhatsApp-এ হিসাব বিবরণী শেয়ার করুন"))
        } catch (e: Exception) {
            Toast.makeText(context, "শেয়ার করা সম্ভব হয়নি", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportAndSharePdf() {
        try {
            val pdfFile = PdfReportGenerator.generateFinancialReportPdf(
                context = context,
                organization = organization,
                financialSummary = financialSummary,
                members = members,
                collections = collections,
                expenses = expenses,
                otherIncomes = otherIncomes
            )
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "$orgName - অডিট ও চাঁদা রিপোর্ট.pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "PDF রিপোর্ট ওপেন বা শেয়ার করুন"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF তৈরিতে সমস্যা: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportAndShareExcelCsv() {
        try {
            val csvFile = PdfReportGenerator.generateExcelCsv(
                context = context,
                organization = organization,
                financialSummary = financialSummary,
                members = members,
                collections = collections,
                expenses = expenses
            )
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", csvFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/comma-separated-values"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "$orgName - Excel Report.csv")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Excel / CSV রিপোর্ট শেয়ার করুন"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Excel ফাইলে সমস্যা: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyReportToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("হিসাব বিবরণী", generateTextAuditReport())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "হিসাব বিবরণী কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun sendPersonalizedReminder(memberWithFin: MemberWithFinancials) {
        val member = memberWithFin.member
        val message = "আসসালামু আলাইকুম ${member.name} ভাই/বোন,\n$orgName-এর আপনার বকেয়া চাঁদার পরিমাণ ${BengaliFormatter.formatCurrency(memberWithFin.dueAmount)}। তহবিলের স্বচ্ছতা ও কার্যক্রম অব্যাহত রাখতে বকেয়া চাঁদা দ্রুত পরিশোধের জন্য অনুরোধ জানাচ্ছি।\nধন্যবাদান্তে,\n$orgAdmin ($orgRole)"

        val cleanPhone = member.phone.replace("+", "").replace("-", "").replace(" ", "")
        val intlPhone = if (cleanPhone.startsWith("01")) "88$cleanPhone" else cleanPhone

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$intlPhone&text=${Uri.encode(message)}")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp ইনস্টল করা নেই", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Action Share & Export Bar with PDF, Excel and WhatsApp
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "রিপোর্ট এক্সপোর্ট ও শেয়ার",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { exportAndSharePdf() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("export_pdf_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PDF রিপোর্ট", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Button(
                            onClick = { exportAndShareExcelCsv() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("export_excel_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Excel / CSV", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { shareSummaryOnWhatsApp() },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("share_whatsapp_report_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp বার্তা", fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { copyReportToClipboard() },
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("copy_text_report_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("কপি")
                        }
                    }
                }
            }
        }

        // Financial Overview Slip
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "সম্পূর্ণ আর্থিক স্থিতি বিবরণী",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryGreen.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = BengaliFormatter.formatDate(System.currentTimeMillis()),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    ReportStatRow("মোট সদস্য সংখ্যা", "${BengaliFormatter.toBengaliDigits(financialSummary.totalMembers)} জন", MaterialTheme.colorScheme.onSurface)
                    ReportStatRow("মোট চাঁদা ধার্য (লক্ষ্যমাত্রা)", BengaliFormatter.formatCurrency(financialSummary.totalAssignedFee), AccentAmber)
                    ReportStatRow("মোট আদায়কৃত সদস্য চাঁদা", BengaliFormatter.formatCurrency(memberCollectionsTotal), IncomeGreen)
                    if (anonymousCollections.isNotEmpty()) {
                        ReportStatRow("└ দ্রুত ও বেনামী দান", BengaliFormatter.formatCurrency(anonymousTotal), AccentAmber)
                    }
                    ReportStatRow("অন্যান্য অনুদান ও বিশেষ আয়", BengaliFormatter.formatCurrency(financialSummary.totalOtherIncome), IncomeGreen)
                    ReportStatRow("সর্বমোট আয় (আদায় + অনুদান)", BengaliFormatter.formatCurrency(financialSummary.totalIncome), IncomeGreen)
                    ReportStatRow("মোট খরচ ও ব্যয়", BengaliFormatter.formatCurrency(financialSummary.totalExpense), ExpenseRed)

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("বর্তমান ক্যাশ ব্যালেন্স (উদ্বৃত্ত)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = BengaliFormatter.formatCurrency(financialSummary.netBalance),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("মোট অপরিশোধিত বকেয়া চাঁদা", style = MaterialTheme.typography.bodyMedium, color = DueOrange)
                        Text(
                            text = BengaliFormatter.formatCurrency(financialSummary.totalDue),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DueOrange
                        )
                    }
                }
            }
        }

        // Due Members Reminder Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = DueOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "বকেয়া সদস্যদের তালিকা ও রিমাইন্ডার",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = DueOrange.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${BengaliFormatter.toBengaliDigits(dueMembers.size)} জন",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DueOrange
                    )
                }
            }
        }

        if (dueMembers.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = IncomeGreen.copy(alpha = 0.1f)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "🎉 আলহামদুলিল্লাহ! কোনো সদস্যের বকেয়া নেই, সবার চাঁদা পরিশোধিত।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = IncomeGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(dueMembers) { m ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(m.member.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Text(m.member.memberCode, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Text(
                                text = "ধার্য: ${BengaliFormatter.formatCurrency(m.member.assignedFee)} | জমা: ${BengaliFormatter.formatCurrency(m.totalPaid)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "বকেয়া: ${BengaliFormatter.formatCurrency(m.dueAmount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = DueOrange
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (m.member.phone.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${m.member.phone}"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call", tint = PrimaryGreen)
                                }

                                Button(
                                    onClick = { sendPersonalizedReminder(m) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("তাগাদা", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReportStatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
