package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.local.entities.OrganizationEntity
import com.example.data.model.BengaliFormatter
import com.example.data.model.MemberWithFinancials
import com.example.data.model.PaymentStatus
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.DueOrange
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryGreen

@Composable
fun MemberListScreen(
    members: List<MemberWithFinancials>,
    organization: OrganizationEntity?,
    onSelectMember: (MemberWithFinancials) -> Unit,
    onAddMember: () -> Unit,
    onCollectFromMember: (Long) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("সবাই") }

    val avatarPalette = listOf(
        Color(0xFF0D5C3A),
        Color(0xFF1E88E5),
        Color(0xFF7B1FA2),
        Color(0xFFE65100),
        Color(0xFF00897B),
        Color(0xFFC2185B)
    )

    val filteredMembers = members.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
                item.member.name.contains(searchQuery, ignoreCase = true) ||
                item.member.phone.contains(searchQuery) ||
                item.member.memberCode.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "বকেয়া" -> item.status == PaymentStatus.UNPAID || item.status == PaymentStatus.PARTIAL
            "পরিশোধিত" -> item.status == PaymentStatus.PAID
            "আংশিক" -> item.status == PaymentStatus.PARTIAL
            else -> true
        }

        matchesSearch && matchesFilter
    }

    fun sendWhatsAppReminder(item: MemberWithFinancials) {
        val orgName = organization?.name ?: "আমাদের সংগঠন"
        val collectorName = organization?.adminName ?: "কর্তৃপক্ষ"
        val collectorRole = organization?.adminRole ?: "দায়িত্বশীল"
        val message = "আসসালামু আলাইকুম ${item.member.name} ভাই/বোন,\n$orgName-এর আপনার বকেয়া চাঁদার পরিমাণ ${BengaliFormatter.formatCurrency(item.dueAmount)}। তহবিলের স্বচ্ছতা ও কার্যক্রম সচল রাখতে দ্রুত পরিশোধের অনুরোধ করা হচ্ছে।\nধন্যবাদান্তে,\n$collectorName ($collectorRole)"

        val cleanPhone = item.member.phone.replace("+", "").replace("-", "").replace(" ", "")
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

    fun makeCall(phone: String) {
        if (phone.isNotBlank()) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            context.startActivity(intent)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("member_search_input"),
                placeholder = { Text("নাম, ফোন বা সদস্য আইডি দিয়ে খুঁজুন...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf(
                    "সবাই (${members.size})",
                    "বকেয়া (${members.count { it.dueAmount > 0 }})",
                    "পরিশোধিত (${members.count { it.status == PaymentStatus.PAID }})",
                    "আংশিক (${members.count { it.status == PaymentStatus.PARTIAL }})"
                )
                items(filters) { filterLabel ->
                    val key = filterLabel.substringBefore(" ")
                    val isSelected = selectedFilter == key
                    Surface(
                        modifier = Modifier.clickable { selectedFilter = key },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = filterLabel,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredMembers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "কোনো সদস্য পাওয়া যায়নি",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("member_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMembers) { item ->
                        val member = item.member
                        val avatarColor = avatarPalette[member.avatarColorIndex % avatarPalette.size]

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectMember(item) }
                                .testTag("member_card_${member.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(avatarColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = member.name.take(1).uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = member.name,
                                                    style = MaterialTheme.typography.titleMedium,
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
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "${member.designation} • ${member.phone.ifEmpty { "ফোন নম্বর নেই" }}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Status Badge
                                    val (badgeBg, badgeColor, badgeText) = when (item.status) {
                                        PaymentStatus.PAID -> Triple(IncomeGreen.copy(alpha = 0.15f), IncomeGreen, "পরিশোধিত")
                                        PaymentStatus.PARTIAL -> Triple(AccentAmber.copy(alpha = 0.15f), AccentAmber, "আংশিক")
                                        PaymentStatus.UNPAID -> Triple(DueOrange.copy(alpha = 0.15f), DueOrange, "বকেয়া")
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = badgeBg
                                    ) {
                                        Text(
                                            text = badgeText,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Financial stats row for this member
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "ধার্য (${member.feeType})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = BengaliFormatter.formatCurrency(member.assignedFee),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "পরিশোধ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = BengaliFormatter.formatCurrency(item.totalPaid),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = IncomeGreen
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "বকেয়া",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = BengaliFormatter.formatCurrency(item.dueAmount),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.dueAmount > 0) MaterialTheme.colorScheme.error else IncomeGreen
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action Buttons: Call, WhatsApp Reminder, Collect
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (member.phone.isNotBlank()) {
                                        IconButton(
                                            onClick = { makeCall(member.phone) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = "Call", tint = PrimaryGreen)
                                        }

                                        if (item.dueAmount > 0) {
                                            IconButton(
                                                onClick = { sendWhatsAppReminder(item) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Default.Send, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Surface(
                                        modifier = Modifier.clickable { onCollectFromMember(member.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = IncomeGreen.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Payments,
                                                contentDescription = null,
                                                tint = IncomeGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "চাঁদা জমা",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = IncomeGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // Floating Action Button to Add Member
        FloatingActionButton(
            onClick = onAddMember,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_member_fab"),
            containerColor = PrimaryGreen,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Member")
        }
    }
}
