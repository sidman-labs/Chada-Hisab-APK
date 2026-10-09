package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.OrganizationEntity
import com.example.data.model.BengaliFormatter
import com.example.ui.theme.PrimaryGreen
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    currentOrg: OrganizationEntity?,
    allOrgs: List<OrganizationEntity>,
    isPinEnabled: Boolean,
    currentPin: String,
    onSwitchOrg: (Long) -> Unit,
    onAddNewOrg: () -> Unit,
    onUpdatePin: (newPin: String, enabled: Boolean) -> Unit,
    onExportBackup: suspend () -> String,
    onImportBackup: (String, (Boolean) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf(currentPin) }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var restoreStatusMessage by remember { mutableStateOf("") }

    // Backup export action
    fun performBackupExport() {
        scope.launch {
            val json = onExportBackup()
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Chada_Hishab_Backup_${System.currentTimeMillis()}.json")
                putExtra(Intent.EXTRA_TEXT, json)
            }
            try {
                context.startActivity(Intent.createChooser(shareIntent, "ব্যাকআপ ফাইল সংরক্ষণ বা শেয়ার করুন"))
            } catch (e: Exception) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Backup JSON", json)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "ব্যাকআপ টেক্সট কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // PIN Configuration Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("পিন কোড (PIN Lock) সেট করুন") },
            text = {
                Column {
                    Text(
                        text = "অ্যাপ খোলার সময় সুরক্ষার জন্য ৪ সংখ্যার গোপন পিন দিন:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("settings_pin_input"),
                        label = { Text("৪ সংখ্যার পিন") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            onUpdatePin(pinInput, true)
                            showPinDialog = false
                            Toast.makeText(context, "পিন সফলভাবে সক্রিয় হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = pinInput.length == 4,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Restore Backup Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("ব্যাকআপ ফাইল থেকে রিস্টোর") },
            text = {
                Column {
                    Text(
                        text = "আপনার পূর্বে সংরক্ষিত ব্যাকআপ JSON টেক্সটটি নিচে পেস্ট করুন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("restore_json_input"),
                        placeholder = { Text("{\n  \"version\": 1,\n  \"organizations\": ...\n}") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (restoreStatusMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = restoreStatusMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreJsonInput.isNotBlank()) {
                            onImportBackup(restoreJsonInput) { success ->
                                if (success) {
                                    showRestoreDialog = false
                                    Toast.makeText(context, "ডেটা সফলভাবে রিস্টোর হয়েছে!", Toast.LENGTH_LONG).show()
                                } else {
                                    restoreStatusMessage = "ভুল ব্যাকআপ ফরম্যাট! অনুগ্রহ করে সঠিক JSON দিন।"
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("রিস্টোর করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "সেটিংস ও নিরাপত্তা",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Organization Switcher & Management Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = PrimaryGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "সংগঠন ও ইভেন্ট নির্বাচন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = onAddNewOrg, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Add Org", tint = PrimaryGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    allOrgs.forEach { org ->
                        val isSelected = org.id == currentOrg?.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onSwitchOrg(org.id) }
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PrimaryGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
                                        text = org.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${org.type} • ${org.adminName} (${org.adminRole})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryGreen)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Security PIN Lock Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "অ্যাপ লক (PIN কোড)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isPinEnabled) "পিন লক সক্রিয় আছে" else "পিন লক বন্ধ আছে",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isPinEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    showPinDialog = true
                                } else {
                                    onUpdatePin("", false)
                                    Toast.makeText(context, "পিন লক বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("settings_pin_switch")
                        )
                    }

                    if (isPinEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { showPinDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("পিন কোড পরিবর্তন করুন")
                        }
                    }
                }
            }
        }

        // Backup and Restore Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = PrimaryGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ব্যাকআপ ও রিস্টোর (লোকাল ফাইল)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ফোন পরিবর্তন বা ডেটা সুরক্ষার জন্য ব্যাকআপ নিয়ে WhatsApp বা Google Drive-এ রেখে দিন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { performBackupExport() },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("export_backup_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ব্যাকআপ ফাইল", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                restoreStatusMessage = ""
                                showRestoreDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("restore_backup_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ফাইল রিস্টোর", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Offline and Privacy Guarantee Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PrimaryGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = PrimaryGreen.copy(alpha = 0.08f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "আপনার তথ্য, আপনার নিয়ন্ত্রণ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• ১০০% অফলাইনে কাজ করে\n• আপনার ডেটা শুধুমাত্র আপনার ফোনেই (Room SQLite) সংরক্ষিত থাকে\n• কোনো ক্লাউড সার্ভারে কোনো হিসাব পাঠানো হয় না\n• গোপনীয়তা ও পূর্ণ নিরাপত্তা নিশ্চিত",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
