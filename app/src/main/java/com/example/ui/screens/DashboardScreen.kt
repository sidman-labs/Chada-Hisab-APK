package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CollectionEntity
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.OrganizationEntity
import com.example.data.model.BengaliFormatter
import com.example.data.model.FinancialSummary
import com.example.data.model.MemberWithFinancials
import com.example.data.model.MonthlyStat
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberContainer
import com.example.ui.theme.DueOrange
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.OnAccentAmberContainer
import com.example.ui.theme.PrimaryGreen

@Composable
fun DashboardScreen(
    organization: OrganizationEntity?,
    allOrganizations: List<OrganizationEntity>,
    financialSummary: FinancialSummary,
    monthlyStats: List<MonthlyStat>,
    recentCollections: List<CollectionEntity>,
    recentExpenses: List<ExpenseEntity>,
    members: List<MemberWithFinancials>,
    onSwitchOrg: (Long) -> Unit,
    onAddNewOrg: () -> Unit,
    onOpenAddCollection: () -> Unit,
    onOpenAddAnonymousCollection: () -> Unit,
    onOpenAddExpense: () -> Unit,
    onOpenAddMember: () -> Unit,
    onOpenReports: () -> Unit,
    onViewReceipt: (CollectionEntity) -> Unit
) {
    var orgDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Organization Switcher Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { orgDropdownExpanded = true }
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = organization?.name ?: "আমাদের সংগঠন",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = organization?.type ?: "সংগঠন",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = orgDropdownExpanded,
                        onDismissRequest = { orgDropdownExpanded = false }
                    ) {
                        allOrganizations.forEach { org ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(org.name, fontWeight = FontWeight.Bold)
                                        Text(org.type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    onSwitchOrg(org.id)
                                    orgDropdownExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Text("+ নতুন সংগঠন / ইভেন্ট তৈরি", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                            },
                            onClick = {
                                orgDropdownExpanded = false
                                onAddNewOrg()
                            }
                        )
                    }
                }

                // Profile Avatar / Badge
                Surface(
                    shape = CircleShape,
                    color = PrimaryGreen.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = organization?.adminName?.take(1)?.uppercase() ?: "আ",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = organization?.adminRole ?: "অ্যাডমিন",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryGreen
                        )
                    }
                }
            }
        }

        // Hero Main Wallet / Balance Card (Design inspired by reference image)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("net_balance_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryGreen)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "বর্তমান তহবিলের স্থিতি (ক্যাশ ব্যালেন্স)",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        // Collection % Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "আদায় ${BengaliFormatter.toBengaliDigits(financialSummary.collectionProgressPercent.toInt())}%",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = BengaliFormatter.formatCurrency(financialSummary.netBalance),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Income vs Expense sub-pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "মোট আয়",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = BengaliFormatter.formatCurrency(financialSummary.totalIncome),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "মোট খরচ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = BengaliFormatter.formatCurrency(financialSummary.totalExpense),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Anonymous / Guest Donation Shortcut Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenAddAnonymousCollection() }
                    .border(1.dp, AccentAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .testTag("quick_anonymous_collection_banner"),
                shape = RoundedCornerShape(16.dp),
                color = AccentAmberContainer.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AccentAmber),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "দ্রুত / বেনামী চাঁদা আদায়",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OnAccentAmberContainer
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = AccentAmber
                                ) {
                                    Text(
                                        text = "নতুন",
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "সদস্য তালিকা ছাড়াই মুক্তহস্তে দান বা চাঁদা এন্ট্রি ও রসিদ",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnAccentAmberContainer.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AccentAmber
                    ) {
                        Text(
                            text = "জমা নিন",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 4 Key Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "মোট সদস্য সংখ্যা",
                        value = "${BengaliFormatter.toBengaliDigits(financialSummary.totalMembers)} জন",
                        subtitle = "পরিশোধ: ${BengaliFormatter.toBengaliDigits(financialSummary.paidMembersCount)} | বাকি: ${BengaliFormatter.toBengaliDigits(financialSummary.unpaidMembersCount)}",
                        iconColor = PrimaryGreen,
                        icon = Icons.Default.Group
                    )

                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "মোট চাঁদা ধার্য",
                        value = BengaliFormatter.formatCurrency(financialSummary.totalAssignedFee),
                        subtitle = "নির্ধারিত লক্ষ্যমাত্রা",
                        iconColor = AccentAmber,
                        icon = Icons.Default.Assessment
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "মোট আদায় হয়েছে",
                        value = BengaliFormatter.formatCurrency(financialSummary.totalCollectedFee),
                        subtitle = "সদস্য চাঁদা হতে",
                        iconColor = IncomeGreen,
                        icon = Icons.Default.Payments
                    )

                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "কত টাকা বাকি",
                        value = BengaliFormatter.formatCurrency(financialSummary.totalDue),
                        subtitle = "${BengaliFormatter.toBengaliDigits(financialSummary.unpaidMembersCount + financialSummary.partialMembersCount)} জনের বকেয়া",
                        iconColor = DueOrange,
                        icon = Icons.Default.ReceiptLong
                    )
                }
            }
        }

        // Quick Action Buttons (4 Pillars)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    QuickActionButton(
                        title = "চাঁদা গ্রহণ",
                        icon = Icons.Default.Payments,
                        color = IncomeGreen,
                        onClick = onOpenAddCollection
                    )

                    QuickActionButton(
                        title = "খরচ যোগ",
                        icon = Icons.Default.ArrowUpward,
                        color = ExpenseRed,
                        onClick = onOpenAddExpense
                    )

                    QuickActionButton(
                        title = "সদস্য যোগ",
                        icon = Icons.Default.PersonAdd,
                        color = PrimaryGreen,
                        onClick = onOpenAddMember
                    )

                    QuickActionButton(
                        title = "রিপোর্ট ও রসিদ",
                        icon = Icons.Default.Assessment,
                        color = AccentAmber,
                        onClick = onOpenReports
                    )
                }
            }
        }

        // Collection Progress Bar Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "আদায়ের অগ্রগতি",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${BengaliFormatter.toBengaliDigits(financialSummary.collectionProgressPercent.toInt())}% সম্পন্ন",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (financialSummary.collectionProgressPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = PrimaryGreen,
                        trackColor = PrimaryGreen.copy(alpha = 0.15f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatusMiniPill("পরিশোধিত", financialSummary.paidMembersCount, IncomeGreen)
                        StatusMiniPill("আংশিক বাকি", financialSummary.partialMembersCount, AccentAmber)
                        StatusMiniPill("সম্পূর্ণ বকেয়া", financialSummary.unpaidMembersCount, DueOrange)
                    }
                }
            }
        }

        // Monthly Trend Bar Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মাসিক আদায় ও খরচের তুলনা",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PrimaryGreen))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("আদায়", style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseRed))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("খরচ", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Compose Canvas Chart
                    MonthlyBarChart(
                        stats = monthlyStats,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            }
        }

        // Recent Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সাম্প্রতিক লেনদেন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "রসিদ দেখতে ট্যাপ করুন",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Recent collections list
        if (recentCollections.isEmpty() && recentExpenses.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "এখনও কোনো লেনদেন এন্ট্রি করা হয়নি।\nউপরে 'চাঁদা গ্রহণ' বা 'খরচ যোগ' চাপুন।",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(recentCollections.take(5)) { collection ->
                val isAnonymous = collection.memberId == null
                val member = if (!isAnonymous) members.find { it.member.id == collection.memberId }?.member else null
                val displayName = if (isAnonymous) collection.donorName.ifEmpty { "বেনামী / শুভাকাঙ্ক্ষী" } else (member?.name ?: "সদস্য")

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewReceipt(collection) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isAnonymous) AccentAmber.copy(alpha = 0.15f) else IncomeGreen.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAnonymous) Icons.Default.VolunteerActivism else Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = if (isAnonymous) AccentAmber else IncomeGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isAnonymous) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = AccentAmber.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "বেনামী",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AccentAmber,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${collection.category} • ${BengaliFormatter.formatDate(collection.date)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+${BengaliFormatter.formatCurrency(collection.amount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isAnonymous) AccentAmber else IncomeGreen
                            )
                            Text(
                                text = collection.receiptNo,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(recentExpenses.take(3)) { expense ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ExpenseRed.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = expense.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${expense.category} • ${BengaliFormatter.formatDate(expense.date)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "-${BengaliFormatter.formatCurrency(expense.amount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                            Text(
                                text = expense.paymentMethod,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    iconColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatusMiniPill(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label: ${BengaliFormatter.toBengaliDigits(count)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MonthlyBarChart(
    stats: List<MonthlyStat>,
    modifier: Modifier = Modifier
) {
    val maxVal = stats.maxOfOrNull { maxOf(it.collectionAmount, it.expenseAmount) }?.takeIf { it > 0 } ?: 1000.0

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val width = size.width
            val height = size.height
            val barCount = stats.size.coerceAtLeast(1)
            val slotWidth = width / barCount
            val barWidth = (slotWidth * 0.3f).coerceIn(8.dp.toPx(), 24.dp.toPx())

            stats.forEachIndexed { index, stat ->
                val centerX = slotWidth * index + slotWidth / 2f

                // Income Bar
                val incomeHeight = ((stat.collectionAmount / maxVal) * (height - 8f)).toFloat().coerceAtLeast(4f)
                val incomeX = centerX - barWidth - 2f
                val incomeY = height - incomeHeight

                drawRoundRect(
                    color = Color(0xFF0D5C3A),
                    topLeft = Offset(incomeX, incomeY),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                // Expense Bar
                val expenseHeight = ((stat.expenseAmount / maxVal) * (height - 8f)).toFloat().coerceAtLeast(4f)
                val expenseX = centerX + 2f
                val expenseY = height - expenseHeight

                drawRoundRect(
                    color = Color(0xFFD93025),
                    topLeft = Offset(expenseX, expenseY),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Month Names Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            stats.forEach { stat ->
                Text(
                    text = stat.monthNameBn,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
