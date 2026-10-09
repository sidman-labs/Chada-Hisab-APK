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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.MemberEntity
import com.example.ui.theme.PrimaryGreen

@Composable
fun AddMemberDialog(
    existingMember: MemberEntity? = null,
    nextMemberCode: String = "M-001",
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, code: String, designation: String, fee: Double, feeType: String, address: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(existingMember?.name ?: "") }
    var phone by remember { mutableStateOf(existingMember?.phone ?: "") }
    var memberCode by remember { mutableStateOf(existingMember?.memberCode ?: nextMemberCode) }
    var designation by remember { mutableStateOf(existingMember?.designation ?: "সাধারণ সদস্য") }
    var assignedFeeText by remember {
        mutableStateOf(
            existingMember?.let {
                if (it.assignedFee % 1.0 == 0.0) it.assignedFee.toLong().toString() else it.assignedFee.toString()
            } ?: "500"
        )
    }
    var feeType by remember { mutableStateOf(existingMember?.feeType ?: "মাসিক") }
    var address by remember { mutableStateOf(existingMember?.address ?: "") }
    var notes by remember { mutableStateOf(existingMember?.notes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("add_member_dialog"),
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
                                .background(PrimaryGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (existingMember != null) "সদস্য তথ্য সম্পাদনা" else "নতুন সদস্য যোগ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_name_input"),
                    label = { Text("সদস্যের নাম *") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("member_phone_input"),
                        label = { Text("মোবাইল নম্বর") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = memberCode,
                        onValueChange = { memberCode = it },
                        modifier = Modifier.weight(0.9f),
                        label = { Text("সদস্য ID") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("পদবী (যেমন: সভাপতি / সদস্য / দাতা)") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Fee Type
                Text(
                    text = "চাঁদার ধরন",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("মাসিক", "এককালীন", "বাৎসরিক").forEach { type ->
                        FilterChip(
                            selected = feeType == type,
                            onClick = { feeType = type },
                            label = { Text(type) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = assignedFeeText,
                    onValueChange = { assignedFeeText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_fee_input"),
                    label = { Text("ধার্যকৃত চাঁদা *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    prefix = { Text("৳ ", fontWeight = FontWeight.Bold) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("ঠিকানা / গ্রাম / এলাকা (ঐচ্ছিক)") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("নোট / বিশেষ বিবরণ (ঐচ্ছিক)") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                val parsedFee = assignedFeeText.toDoubleOrNull() ?: 0.0
                val canSave = name.isNotBlank() && parsedFee >= 0

                Button(
                    onClick = {
                        if (canSave) {
                            onSave(name, phone, memberCode, designation, parsedFee, feeType, address, notes)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_member_button"),
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (existingMember != null) "হালনাগাদ করুন" else "সদস্য সংরক্ষণ করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
