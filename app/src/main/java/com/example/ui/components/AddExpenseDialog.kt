package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.OrganizationTypePresets
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryGreen

@Composable
fun AddExpenseDialog(
    orgType: String = "অন্যান্য",
    adminName: String = "",
    onDismiss: () -> Unit,
    onSaveExpense: (title: String, amount: Double, category: String, method: String, spentBy: String, voucherNo: String, notes: String) -> Unit,
    onSaveOtherIncome: (source: String, amount: Double, method: String, receivedBy: String, notes: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = খরচ, 1 = অন্যান্য আয়

    val defaultExpenseCategories = OrganizationTypePresets.getDefaultExpenseCategories(orgType)
    var selectedExpenseCat by remember { mutableStateOf(defaultExpenseCategories.firstOrNull() ?: "সাধারণ খরচ") }

    var titleOrSource by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var spentBy by remember { mutableStateOf(adminName) }
    var voucherNo by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val paymentMethods = listOf("ক্যাশ", "বিকাশ", "নগদ", "ব্যাংক")
    var selectedMethod by remember { mutableStateOf("ক্যাশ") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("add_expense_dialog"),
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
                                .background(if (selectedTab == 0) ExpenseRed.copy(alpha = 0.15f) else IncomeGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = if (selectedTab == 0) ExpenseRed else IncomeGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (selectedTab == 0) "খরচের হিসাব যোগ" else "অন্যান্য আয় / অনুদান",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("খরচ (Expense)", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("অন্যান্য আয় (Income)", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // Category chips
                    Text(
                        text = "খরচের খাত",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(defaultExpenseCategories) { cat ->
                            FilterChip(
                                selected = selectedExpenseCat == cat,
                                onClick = { selectedExpenseCat = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ExpenseRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Title or Source
                OutlinedTextField(
                    value = titleOrSource,
                    onValueChange = { titleOrSource = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_title_input"),
                    label = { Text(if (selectedTab == 0) "খরচের বিবরণ * (যেমন: মাইক ভাড়া)" else "আয়ের উৎস * (যেমন: দানবাক্স)") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input"),
                    label = { Text("টাকার পরিমাণ *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    prefix = { Text("৳ ", fontWeight = FontWeight.Bold) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method
                Text(
                    text = "মাধ্যম",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
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

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = spentBy,
                    onValueChange = { spentBy = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (selectedTab == 0) "ব্যয়কারী / অনুমোদনকারীর নাম" else "গ্রহণকারীর নাম") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (selectedTab == 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = voucherNo,
                        onValueChange = { voucherNo = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("ভাউচার / মেমো নম্বর (ঐচ্ছিক)") },
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

                val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                val canSave = titleOrSource.isNotBlank() && parsedAmount > 0

                Button(
                    onClick = {
                        if (canSave) {
                            if (selectedTab == 0) {
                                onSaveExpense(titleOrSource, parsedAmount, selectedExpenseCat, selectedMethod, spentBy, voucherNo, notes)
                            } else {
                                onSaveOtherIncome(titleOrSource, parsedAmount, selectedMethod, spentBy, notes)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_expense_button"),
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 0) ExpenseRed else IncomeGreen
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (selectedTab == 0) "খরচ সংরক্ষণ করুন" else "আয় সংরক্ষণ করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
