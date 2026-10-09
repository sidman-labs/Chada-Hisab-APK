package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.CollectionEntity
import com.example.data.local.entities.MemberEntity
import com.example.data.local.entities.OrganizationEntity
import com.example.data.model.BengaliFormatter
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryGreen

@Composable
fun ReceiptDialog(
    collection: CollectionEntity,
    member: MemberEntity?,
    organization: OrganizationEntity?,
    remainingDue: Double = 0.0,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val orgName = organization?.name ?: "আমাদের সংগঠন"
    val orgAddress = organization?.address.orEmpty()
    val orgPhone = organization?.phone.orEmpty()

    val isAnonymous = collection.memberId == null || member == null
    val displayName = if (isAnonymous) {
        collection.donorName.ifEmpty { "বেনামী / শুভাকাঙ্ক্ষী" }
    } else {
        member?.name ?: "সম্মানিত সদস্য"
    }
    val displayPhone = if (isAnonymous) collection.donorPhone else member?.phone.orEmpty()
    val displayCode = if (isAnonymous) "বেনামী / সাধারণ দান" else (member?.memberCode ?: "M-001")

    val formattedAmount = BengaliFormatter.formatCurrency(collection.amount)
    val formattedDate = BengaliFormatter.formatDate(collection.date)
    val formattedDue = BengaliFormatter.formatCurrency(remainingDue)

    fun generateReceiptText(): String {
        return buildString {
            appendLine("═══════════════════════════")
            appendLine("     $orgName")
            if (orgAddress.isNotEmpty()) appendLine("     ঠিকানা: $orgAddress")
            if (orgPhone.isNotEmpty()) appendLine("     যোগাযোগ: $orgPhone")
            appendLine("═══════════════════════════")
            appendLine("           মানি রসিদ (MONEY RECEIPT)")
            appendLine("রসিদ নং: ${collection.receiptNo}")
            appendLine("তারিখ: $formattedDate")
            appendLine("───────────────────────────")
            if (isAnonymous) {
                appendLine("দাতার নাম: $displayName")
                appendLine("ধরন: দ্রুত / বেনামী চাঁদা")
            } else {
                appendLine("সদস্যের নাম: $displayName")
                appendLine("সদস্য আইডি: $displayCode")
            }
            if (displayPhone.isNotEmpty()) appendLine("মোবাইল: $displayPhone")
            appendLine("───────────────────────────")
            appendLine("খাত: ${collection.category}")
            appendLine("মাধ্যম: ${collection.paymentMethod}")
            if (collection.transactionRef.isNotEmpty()) {
                appendLine("ট্রানজেকশন আইডি: ${collection.transactionRef}")
            }
            appendLine("───────────────────────────")
            appendLine("আদায়ের পরিমাণ: $formattedAmount (পরিশোধিত)")
            if (!isAnonymous && remainingDue > 0) {
                appendLine("অবশিষ্ট বকেয়া: $formattedDue")
            }
            appendLine("───────────────────────────")
            appendLine("আদায়কারী: ${collection.collectedBy}")
            if (collection.notes.isNotEmpty()) appendLine("নোট: ${collection.notes}")
            appendLine("═══════════════════════════")
            appendLine("ধন্যবাদ! আপনার অনুদান ও চাঁদা আমাদের অনুপ্রেরণা।")
        }
    }

    fun shareViaWhatsApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, generateReceiptText())
            if (displayPhone.isNotEmpty()) {
                val cleanPhone = displayPhone.replace("+", "").replace("-", "").replace(" ", "")
                val intlPhone = if (cleanPhone.startsWith("01")) "88$cleanPhone" else cleanPhone
                putExtra("jid", "$intlPhone@s.whatsapp.net")
            }
        }
        try {
            context.startActivity(Intent.createChooser(shareIntent, "রসিদ শেয়ার করুন"))
        } catch (e: Exception) {
            Toast.makeText(context, "শেয়ার করা সম্ভব হয়নি", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("মানি রসিদ", generateReceiptText())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "রসিদ কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("receipt_dialog_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ডিজিটাল রসিদ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Receipt Slip
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = orgName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (orgAddress.isNotEmpty()) {
                            Text(
                                text = orgAddress,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                        if (orgPhone.isNotEmpty()) {
                            Text(
                                text = "মোবাইল: ${BengaliFormatter.toBengaliDigits(orgPhone)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PrimaryGreen)
                                .padding(horizontal = 12.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "মানি রসিদ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "রসিদ নং: ${BengaliFormatter.toBengaliDigits(collection.receiptNo)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "তারিখ: $formattedDate",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Member or Donor details block
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isAnonymous) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = PrimaryGreen.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "বেনামী/মুক্তহস্তে চাঁদা",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PrimaryGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isAnonymous) "সাধারণ অনুদান" else "আইডি: ${BengaliFormatter.toBengaliDigits(displayCode)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (displayPhone.isNotEmpty()) {
                                        Text(
                                            text = BengaliFormatter.toBengaliDigits(displayPhone),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Transaction details table
                        ReceiptRow(label = "খাত", value = collection.category)
                        ReceiptRow(label = "পেমেন্ট মাধ্যম", value = collection.paymentMethod)
                        if (collection.transactionRef.isNotEmpty()) {
                            ReceiptRow(label = "রেফারেন্স ID", value = collection.transactionRef)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Highlighted Amount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "জমা গ্রহণ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formattedAmount,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                        }

                        if (!isAnonymous && remainingDue > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "অবশিষ্ট বকেয়া:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = formattedDue,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "আদায়কারী: ${collection.collectedBy}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (collection.notes.isNotEmpty()) {
                            Text(
                                text = "মন্তব্য: ${collection.notes}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Button(
                    onClick = { shareViaWhatsApp() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("receipt_share_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WhatsApp-এ রসিদ পাঠান", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { copyToClipboard() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("receipt_copy_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("রসিদ কপি করুন")
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
