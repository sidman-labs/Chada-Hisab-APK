package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrganizationTypePresets
import com.example.ui.theme.PrimaryGreen

@Composable
fun SetupScreen(
    onCompleteSetup: (
        name: String,
        role: String,
        phone: String,
        address: String,
        orgName: String,
        orgType: String,
        pin: String,
        enablePin: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val defaultRoles = listOf("সভাপতি", "সম্পাদক", "কোষাধ্যক্ষ", "সদস্য", "কাস্টম")
    var selectedRole by remember { mutableStateOf("কোষাধ্যক্ষ") }
    var customRole by remember { mutableStateOf("") }

    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var orgName by remember { mutableStateOf("") }
    var selectedOrgType by remember { mutableStateOf("মসজিদ ফান্ড") }

    var enablePin by remember { mutableStateOf(false) }
    var pinCode by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("setup_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Hero Header
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VolunteerActivism,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "চাঁদা হিসাব",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )

            Text(
                text = "স্বাগতম! আপনার সংগঠন ও প্রোফাইল সেটআপ করুন",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Step 1: Organization details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "১. সংগঠন বা ইভেন্টের তথ্য",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = orgName,
                        onValueChange = { orgName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_org_name_input"),
                        label = { Text("সংগঠন / তহবিলের নাম *") },
                        placeholder = { Text("যেমন: বায়তুল আমান জামে মসজিদ ফান্ড") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "সংগঠনের ধরন",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(OrganizationTypePresets.types) { type ->
                            FilterChip(
                                selected = selectedOrgType == type,
                                onClick = { selectedOrgType = type },
                                label = { Text(type) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("ঠিকানা (গ্রাম / ওয়ার্ড / থানা / জেলা)") },
                        placeholder = { Text("যেমন: মিরপুর-১০, ঢাকা") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 2: Personal Profile
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "২. আপনার তথ্য (রসিদে ছাপা হবে)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_user_name_input"),
                        label = { Text("আপনার নাম * (বাধ্যতামূলক)") },
                        placeholder = { Text("যেমন: আব্দুল্লাহ আল মামুন") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "আপনার পদবী",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        defaultRoles.forEach { role ->
                            FilterChip(
                                selected = selectedRole == role,
                                onClick = { selectedRole = role },
                                label = { Text(role) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    if (selectedRole == "কাস্টম") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customRole,
                            onValueChange = { customRole = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("আপনার পদবী লিখুন") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_phone_input"),
                        label = { Text("মোবাইল নম্বর (রসিদে থাকবে)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        placeholder = { Text("যেমন: 01712345678") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 3: Security PIN
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "অ্যাপ লক (PIN কোড)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "অননুমোদিত দেখা রোধ করতে ৪ সংখ্যার পিন",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = enablePin,
                            onCheckedChange = { enablePin = it },
                            modifier = Modifier.testTag("setup_pin_switch")
                        )
                    }

                    if (enablePin) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { if (it.length <= 4) pinCode = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("setup_pin_input"),
                            label = { Text("৪ সংখ্যার পিন কোড") },
                            placeholder = { Text("যেমন: 1234") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Offline Guarantee Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PrimaryGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                color = PrimaryGreen.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "🔒 আপনার ডেটা শুধুমাত্র আপনার ফোনেই সুরক্ষিত থাকবে। ইন্টারনেট বা ক্লাউড কোনো অ্যাকাউন্টের প্রয়োজন নেই।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val effectiveRole = if (selectedRole == "কাস্টম") customRole.ifBlank { "সদস্য" } else selectedRole
            val canSubmit = name.isNotBlank() && orgName.isNotBlank() && (!enablePin || pinCode.length == 4)

            Button(
                onClick = {
                    if (canSubmit) {
                        onCompleteSetup(
                            name,
                            effectiveRole,
                            phone,
                            address,
                            orgName,
                            selectedOrgType,
                            pinCode,
                            enablePin
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("setup_finish_button"),
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("শুরু করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
