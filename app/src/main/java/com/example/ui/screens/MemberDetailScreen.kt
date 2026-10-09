package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CollectionEntity
import com.example.data.local.entities.OrganizationEntity
import com.example.data.model.BengaliFormatter
import com.example.data.model.MemberWithFinancials
import com.example.data.model.PaymentStatus
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.DueOrange
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryGreen

@Composable
fun MemberDetailScreen(
    memberWithFin: MemberWithFinancials,
    collections: List<CollectionEntity>,
    organization: OrganizationEntity?,
    onBack: () -> Unit,
    onCollect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewReceipt: (CollectionEntity) -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val member = memberWithFin.member
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val memberPayments = collections.filter { it.memberId == member.id }

    fun sendWhatsAppReminder() {
        val orgName = organization?.name ?: "আমাদের সংগঠন"
        val collectorName = organization?.adminName ?: "কর্তৃপক্ষ"
        val message = "আসসালামু আলাইকুম ${member.name} ভাই/বোন,\n$orgName-এর আপনার বকেয়া চাঁদার পরিমাণ ${BengaliFormatter.formatCurrency(memberWithFin.dueAmount)}। তহবিলের স্বচ্ছতা বজায় রাখতে দ্রুত চাঁদা পরিশোধের অনুরোধ জানাচ্ছি।\nধন্যবাদান্তে,\n$collectorName"

        val cleanPhone = member.phone.replace("+", "").replace("-", "").replace(" ", "")
        val intlPhone = if (cleanPhone.startsWith("01")) "88$cleanPhone" else cleanPhone

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$intlPhone&text=${Uri.encode(message)}")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp চালু করা যাচ্ছে না", Toast.LENGTH_SHORT).show()
        }
    }

    fun makeCall() {
        if (member.phone.isNotBlank()) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phone}"))
            context.startActivity(intent)
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("সদস্য অপসারণ নিশ্চিতকরণ") },
            text = { Text("আপনি কি নিশ্চিতভাবে '${member.name}'-কে মুছে ফেলতে চান? এতে সদস্যের সকল লেনদেনের তথ্য মুছে যাবে।") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("মুছে ফেলুন", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("member_detail_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "সদস্যের হিসাব বিবরণী",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // Profile Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member.name.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = member.memberCode,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            Text(
                                text = member.designation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Medium
                            )
                            if (member.phone.isNotEmpty()) {
                                Text(
                                    text = BengaliFormatter.toBengaliDigits(member.phone),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (member.address.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ঠিকানা: ${member.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Financial 3-column stats
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ধার্য চাঁদা", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = BengaliFormatter.formatCurrency(member.assignedFee),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(member.feeType, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("মোট পরিশোধ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = BengaliFormatter.formatCurrency(memberWithFin.totalPaid),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                            Text("${BengaliFormatter.toBengaliDigits(memberPayments.size)} বার জমা", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("মোট বকেয়া", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = BengaliFormatter.formatCurrency(memberWithFin.dueAmount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (memberWithFin.dueAmount > 0) MaterialTheme.colorScheme.error else IncomeGreen
                            )
                            val statusBn = when (memberWithFin.status) {
                                PaymentStatus.PAID -> "পরিশোধিত"
                                PaymentStatus.PARTIAL -> "আংশিক বাকি"
                                PaymentStatus.UNPAID -> "বকেয়া"
                            }
                            Text(statusBn, style = MaterialTheme.typography.labelSmall, color = if (memberWithFin.dueAmount > 0) MaterialTheme.colorScheme.error else IncomeGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onCollect,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("collect_from_member_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("চাঁদা গ্রহণ", fontWeight = FontWeight.Bold)
                        }

                        if (member.phone.isNotBlank()) {
                            if (memberWithFin.dueAmount > 0) {
                                OutlinedButton(
                                    onClick = { sendWhatsAppReminder() },
                                    modifier = Modifier.height(46.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("রিমাইন্ডার")
                                }
                            }

                            IconButton(
                                onClick = { makeCall() },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryGreen.copy(alpha = 0.12f))
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = PrimaryGreen)
                            }
                        }
                    }
                }
            }
        }

        // Transaction History Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "চাঁদা জমা প্রদানের ইতিহাস",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "মোট ${BengaliFormatter.toBengaliDigits(memberPayments.size)} টি লেনদেন",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (memberPayments.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "এই সদস্যের কোনো জমা রেকর্ড নেই।\nউপরে 'চাঁদা গ্রহণ' বাটনে ট্যাপ করে চাঁদা যোগ করুন।",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(memberPayments) { collection ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewReceipt(collection) },
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(IncomeGreen.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = collection.receiptNo,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${collection.category} • ${collection.paymentMethod}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = BengaliFormatter.formatDate(collection.date),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = BengaliFormatter.formatCurrency(collection.amount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PrimaryGreen.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "রসিদ দেখুন",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryGreen
                                )
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
