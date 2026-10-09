package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BengaliFormatter
import com.example.data.model.MemberWithFinancials
import com.example.data.model.OrganizationTypePresets
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryGreen

@Composable
fun AddCollectionDialog(
    members: List<MemberWithFinancials>,
    initialMemberId: Long? = null,
    initialTab: Int = 0,
    orgType: String = "অন্যান্য",
    onDismiss: () -> Unit,
    onSave: (
        memberId: Long?,
        donorName: String,
        donorPhone: String,
        amount: Double,
        category: String,
        method: String,
        trxRef: String,
        notes: String
    ) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(if (initialMemberId != null) 0 else initialTab) }

    // --- Registered Member State ---
    var selectedMemberId by remember {
        mutableStateOf(initialMemberId ?: members.firstOrNull()?.member?.id ?: 0L)
    }
    val selectedMember = members.find { it.member.id == selectedMemberId }

    var memberAmountText by remember {
        mutableStateOf(
            if (selectedMember != null && selectedMember.dueAmount > 0) {
                if (selectedMember.dueAmount % 1.0 == 0.0) selectedMember.dueAmount.toLong().toString()
                else selectedMember.dueAmount.toString()
            } else ""
        )
    }

    val defaultCategories = OrganizationTypePresets.getDefaultIncomeCategories(orgType)
    var selectedMemberCategory by remember { mutableStateOf(defaultCategories.firstOrNull() ?: "মাসিক চাঁদা") }

    // --- Anonymous / Quick Donor State ---
    var donorName by remember { mutableStateOf("বেনামী / শুভাকাঙ্ক্ষী") }
    var donorPhone by remember { mutableStateOf("") }
    var anonymousAmountText by remember { mutableStateOf("500") }

    val anonymousCategories = listOf(
        "মুক্তহস্তে দান",
        "জুমার কালেকশন",
        "দান বাক্স",
        "বিশেষ অনুদান",
        "শুভাকাঙ্ক্ষী চাঁদা",
        "অন্যান্য"
    )
    var selectedAnonymousCategory by remember { mutableStateOf(anonymousCategories.first()) }

    // Shared State
    val paymentMethods = listOf("ক্যাশ", "বিকাশ", "নগদ", "রকেট", "ব্যাংক")
    var selectedMethod by remember { mutableStateOf("ক্যাশ") }
    var trxRef by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var memberDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("add_collection_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (selectedTab == 1) AccentAmber.copy(alpha = 0.15f) else IncomeGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Default.VolunteerActivism else Icons.Default.Payments,
                                contentDescription = null,
                                tint = if (selectedTab == 1) AccentAmber else IncomeGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (selectedTab == 1) "দ্রুত / বেনামী চাঁদা গ্রহণ" else "সদস্যের চাঁদা জমা গ্রহণ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: নিবন্ধিত সদস্য vs দ্রুত/বেনামী চাঁদা
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("নিবন্ধিত সদস্য", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FlashOn, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("দ্রুত / বেনামী", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // --- TAB 0: REGISTERED MEMBER ---
                    Text(
                        text = "সদস্য নির্বাচন করুন *",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .clickable { memberDropdownExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = selectedMember?.member?.name ?: "কোনো সদস্য নেই",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (selectedMember != null) {
                                        Text(
                                            text = "বকেয়া: ${BengaliFormatter.formatCurrency(selectedMember.dueAmount)} | আইডি: ${selectedMember.member.memberCode}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (selectedMember.dueAmount > 0) MaterialTheme.colorScheme.error else IncomeGreen
                                        )
                                    }
                                }
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = memberDropdownExpanded,
                            onDismissRequest = { memberDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            members.forEach { m ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("${m.member.name} (${m.member.memberCode})", fontWeight = FontWeight.Medium)
                                            Text(
                                                "বকেয়া: ${BengaliFormatter.formatCurrency(m.dueAmount)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (m.dueAmount > 0) MaterialTheme.colorScheme.error else IncomeGreen
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedMemberId = m.member.id
                                        if (m.dueAmount > 0) {
                                            memberAmountText = if (m.dueAmount % 1.0 == 0.0) m.dueAmount.toLong().toString()
                                            else m.dueAmount.toString()
                                        }
                                        memberDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "জমার পরিমাণ (টাকা) *",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = memberAmountText,
                        onValueChange = { memberAmountText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("collection_amount_input"),
                        placeholder = { Text("যেমন: ৫০০") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        prefix = { Text("৳ ", fontWeight = FontWeight.Bold) },
                        singleLine = true
                    )

                    // Quick amount chips for member
                    if (selectedMember != null && selectedMember.dueAmount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val due = selectedMember.dueAmount
                            val dueStr = if (due % 1.0 == 0.0) due.toLong().toString() else due.toString()
                            SuggestionChip(
                                label = "সম্পূর্ণ বকেয়া (${BengaliFormatter.formatCurrency(due)})",
                                onClick = { memberAmountText = dueStr }
                            )
                            SuggestionChip(label = "৳ ৫০০", onClick = { memberAmountText = "500" })
                            SuggestionChip(label = "৳ ১০০০", onClick = { memberAmountText = "1000" })
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "চাঁদার খাত",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(defaultCategories) { cat ->
                            FilterChip(
                                selected = selectedMemberCategory == cat,
                                onClick = { selectedMemberCategory = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                } else {
                    // --- TAB 1: ANONYMOUS / QUICK DONOR ---
                    Text(
                        text = "দাতার পরিচয় / নাম",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = donorName,
                        onValueChange = { donorName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("donor_name_input"),
                        placeholder = { Text("যেমন: হাজী রফিকুল ইসলাম (অথবা বেনামী)") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Quick donor name presets
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val namePresets = listOf("বেনামী / অজ্ঞাত", "শুভাকাঙ্ক্ষী", "জুমার মুসল্লি", "সাধারণ দান", "অতিথি / মেহমান")
                        items(namePresets) { preset ->
                            SuggestionChip(label = preset, onClick = { donorName = preset })
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = donorPhone,
                        onValueChange = { donorPhone = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("donor_phone_input"),
                        label = { Text("মোবাইল নম্বর (ঐচ্ছিক - রসিদ পাঠাতে)") },
                        placeholder = { Text("যেমন: 01711223344") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "জমার পরিমাণ (টাকা) *",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = anonymousAmountText,
                        onValueChange = { anonymousAmountText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("anonymous_amount_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        prefix = { Text("৳ ", fontWeight = FontWeight.Bold) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("100", "200", "500", "1000", "2000", "5000")) { amt ->
                            SuggestionChip(
                                label = BengaliFormatter.formatCurrency(amt.toDouble()),
                                onClick = { anonymousAmountText = amt }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "চাঁদা / দানের খাত",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(anonymousCategories) { cat ->
                            FilterChip(
                                selected = selectedAnonymousCategory == cat,
                                onClick = { selectedAnonymousCategory = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentAmber,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Method (Shared for both)
                Text(
                    text = "অর্থ প্রদানের মাধ্যম",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(paymentMethods) { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                if (selectedMethod != "ক্যাশ") {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = trxRef,
                        onValueChange = { trxRef = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("collection_trx_input"),
                        label = { Text("ট্রানজেকশন আইডি / রেফারেন্স (ঐচ্ছিক)") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("মন্তব্য / নোট (ঐচ্ছিক)") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                val parsedMemberAmount = memberAmountText.toDoubleOrNull() ?: 0.0
                val parsedAnonAmount = anonymousAmountText.toDoubleOrNull() ?: 0.0

                val canSave = if (selectedTab == 0) {
                    selectedMemberId > 0 && parsedMemberAmount > 0
                } else {
                    parsedAnonAmount > 0
                }

                Button(
                    onClick = {
                        if (canSave) {
                            if (selectedTab == 0) {
                                onSave(
                                    selectedMemberId,
                                    "",
                                    "",
                                    parsedMemberAmount,
                                    selectedMemberCategory,
                                    selectedMethod,
                                    trxRef,
                                    notes
                                )
                            } else {
                                onSave(
                                    null,
                                    donorName.trim().ifEmpty { "বেনামী / শুভাকাঙ্ক্ষী" },
                                    donorPhone.trim(),
                                    parsedAnonAmount,
                                    selectedAnonymousCategory,
                                    selectedMethod,
                                    trxRef,
                                    notes
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_collection_button"),
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 1) AccentAmber else PrimaryGreen
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (selectedTab == 1) "দ্রুত জমা গ্রহণ ও রসিদ তৈরি করুন ⚡" else "জমা গ্রহণ ও রসিদ তৈরি করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
