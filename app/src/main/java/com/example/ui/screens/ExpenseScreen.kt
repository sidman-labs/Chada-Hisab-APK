package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.OtherIncomeEntity
import com.example.data.model.BengaliFormatter
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryGreen

@Composable
fun ExpenseScreen(
    expenses: List<ExpenseEntity>,
    otherIncomes: List<OtherIncomeEntity>,
    onAddExpenseOrIncome: () -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onDeleteOtherIncome: (OtherIncomeEntity) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = খরচ, 1 = অন্যান্য আয়
    var selectedCategoryFilter by remember { mutableStateOf("সব খাত") }

    var itemToDeleteExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var itemToDeleteIncome by remember { mutableStateOf<OtherIncomeEntity?>(null) }

    val totalExpenseAmount = expenses.sumOf { it.amount }
    val totalOtherIncomeAmount = otherIncomes.sumOf { it.amount }

    if (itemToDeleteExpense != null) {
        AlertDialog(
            onDismissRequest = { itemToDeleteExpense = null },
            title = { Text("খরচ মুছে ফেলার নিশ্চিতকরণ") },
            text = { Text("আপনি কি '${itemToDeleteExpense?.title}'-এর খরচ রেকর্ডটি মুছে ফেলতে চান?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDeleteExpense?.let { onDeleteExpense(it) }
                        itemToDeleteExpense = null
                    }
                ) {
                    Text("মুছে ফেলুন", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDeleteExpense = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    if (itemToDeleteIncome != null) {
        AlertDialog(
            onDismissRequest = { itemToDeleteIncome = null },
            title = { Text("আয় মুছে ফেলার নিশ্চিতকরণ") },
            text = { Text("আপনি কি '${itemToDeleteIncome?.source}'-এর আয়ের রেকর্ডটি মুছে ফেলতে চান?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDeleteIncome?.let { onDeleteOtherIncome(it) }
                        itemToDeleteIncome = null
                    }
                ) {
                    Text("মুছে ফেলুন", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDeleteIncome = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("মোট খরচ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = BengaliFormatter.formatCurrency(totalExpenseAmount),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                        Text("${BengaliFormatter.toBengaliDigits(expenses.size)} টি ভাউচার", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(50.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("অন্যান্য অনুদান / আয়", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = BengaliFormatter.formatCurrency(totalOtherIncomeAmount),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                        Text("${BengaliFormatter.toBengaliDigits(otherIncomes.size)} টি প্রাপ্তি", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("খরচের খাতা (${expenses.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("অন্যান্য আয় (${otherIncomes.size})", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedTab == 0) {
                // Category Filter
                val allCategories = listOf("সব খাত") + expenses.map { it.category }.distinct()
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allCategories) { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        Surface(
                            modifier = Modifier.clickable { selectedCategoryFilter = cat },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) ExpenseRed else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = cat,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val filteredExpenses = if (selectedCategoryFilter == "সব খাত") expenses
                else expenses.filter { it.category == selectedCategoryFilter }

                if (filteredExpenses.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("কোনো খরচ নেই। নিচে '+' চাপুন।", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredExpenses) { expense ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(ExpenseRed.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(expense.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "${expense.category} • ${BengaliFormatter.formatDate(expense.date)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (expense.spentBy.isNotEmpty() || expense.voucherNo.isNotEmpty()) {
                                                Text(
                                                    text = "অনুমোদক: ${expense.spentBy} ${if (expense.voucherNo.isNotEmpty()) "• ভাউচার: ${expense.voucherNo}" else ""}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "-${BengaliFormatter.formatCurrency(expense.amount)}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = ExpenseRed
                                            )
                                            Text(expense.paymentMethod, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(onClick = { itemToDeleteExpense = expense }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(72.dp)) }
                    }
                }
            } else {
                // Other Incomes Tab
                if (otherIncomes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("কোনো অতিরিক্ত আয় নেই। নিচে '+' চাপুন।", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(otherIncomes) { income ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(IncomeGreen.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(income.source, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "${income.paymentMethod} • ${BengaliFormatter.formatDate(income.date)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (income.receivedBy.isNotEmpty()) {
                                                Text(
                                                    text = "গ্রহণকারী: ${income.receivedBy}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "+${BengaliFormatter.formatCurrency(income.amount)}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = IncomeGreen
                                            )
                                            Text("জমা", style = MaterialTheme.typography.labelSmall, color = IncomeGreen)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(onClick = { itemToDeleteIncome = income }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(72.dp)) }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddExpenseOrIncome,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_expense_fab"),
            containerColor = if (selectedTab == 0) ExpenseRed else IncomeGreen,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Expense")
        }
    }
}
