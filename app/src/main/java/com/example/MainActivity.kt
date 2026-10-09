package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.CollectionEntity
import com.example.data.local.entities.MemberEntity
import com.example.data.model.MemberWithFinancials
import com.example.ui.MainViewModel
import com.example.ui.components.AddCollectionDialog
import com.example.ui.components.AddExpenseDialog
import com.example.ui.components.AddMemberDialog
import com.example.ui.components.AddOrgDialog
import com.example.ui.components.PinLockScreen
import com.example.ui.components.ReceiptDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpenseScreen
import com.example.ui.screens.MemberDetailScreen
import com.example.ui.screens.MemberListScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryGreen

enum class NavScreen(val titleBn: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    HOME("হোম", Icons.Filled.Home, Icons.Outlined.Home),
    MEMBERS("সদস্য", Icons.Filled.People, Icons.Outlined.People),
    EXPENSES("খরচ ও আয়", Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong),
    REPORTS("রিপোর্ট", Icons.Filled.Assessment, Icons.Outlined.Assessment),
    SETTINGS("সেটিংস", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val isSetupCompleted by viewModel.isSetupCompleted.collectAsStateWithLifecycle()
    val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()

    val activeOrg by viewModel.activeOrganization.collectAsStateWithLifecycle()
    val allOrgs by viewModel.allOrganizations.collectAsStateWithLifecycle()
    val financialSummary by viewModel.financialSummary.collectAsStateWithLifecycle()
    val monthlyStats by viewModel.monthlyStats.collectAsStateWithLifecycle()
    val membersWithFin by viewModel.membersWithFinancials.collectAsStateWithLifecycle()
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val otherIncomes by viewModel.otherIncomes.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(NavScreen.HOME) }
    var selectedMemberForDetail by remember { mutableStateOf<MemberWithFinancials?>(null) }

    // Dialog States
    var showAddCollectionDialog by remember { mutableStateOf(false) }
    var collectionInitialMemberId by remember { mutableStateOf<Long?>(null) }
    var collectionInitialTab by remember { mutableIntStateOf(0) }

    var showAddExpenseDialog by remember { mutableStateOf(false) }

    var showAddMemberDialog by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<MemberEntity?>(null) }

    var showAddOrgDialog by remember { mutableStateOf(false) }

    var activeReceiptCollection by remember { mutableStateOf<CollectionEntity?>(null) }

    when {
        !isSetupCompleted -> {
            SetupScreen(
                onCompleteSetup = { name, role, phone, address, orgName, orgType, pin, enablePin ->
                    viewModel.completeProfileSetup(
                        userName = name,
                        userRole = role,
                        phone = phone,
                        address = address,
                        orgName = orgName,
                        orgType = orgType,
                        pin = pin,
                        enablePin = enablePin
                    )
                }
            )
        }

        isLocked -> {
            PinLockScreen(
                onPinEntered = { pin -> viewModel.unlockApp(pin) },
                onSuccess = {}
            )
        }

        selectedMemberForDetail != null -> {
            val currentMemberFin = membersWithFin.find { it.member.id == selectedMemberForDetail?.member?.id }
                ?: selectedMemberForDetail!!

            MemberDetailScreen(
                memberWithFin = currentMemberFin,
                collections = collections,
                organization = activeOrg,
                onBack = { selectedMemberForDetail = null },
                onCollect = {
                    collectionInitialMemberId = currentMemberFin.member.id
                    collectionInitialTab = 0
                    showAddCollectionDialog = true
                },
                onEdit = {
                    memberToEdit = currentMemberFin.member
                    showAddMemberDialog = true
                },
                onDelete = {
                    viewModel.deleteMember(currentMemberFin.member)
                    selectedMemberForDetail = null
                },
                onViewReceipt = { receipt ->
                    activeReceiptCollection = receipt
                }
            )
        }

        else -> {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        NavScreen.entries.forEach { screen ->
                            val isSelected = currentScreen == screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentScreen = screen },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.titleBn,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.titleBn,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryGreen,
                                    selectedTextColor = PrimaryGreen,
                                    indicatorColor = PrimaryGreen.copy(alpha = 0.14f)
                                ),
                                modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                if (currentScreen != NavScreen.HOME) {
                    androidx.activity.compose.BackHandler {
                        currentScreen = NavScreen.HOME
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    when (currentScreen) {
                        NavScreen.HOME -> {
                            DashboardScreen(
                                organization = activeOrg,
                                allOrganizations = allOrgs,
                                financialSummary = financialSummary,
                                monthlyStats = monthlyStats,
                                recentCollections = collections,
                                recentExpenses = expenses,
                                members = membersWithFin,
                                onSwitchOrg = { id -> viewModel.switchOrganization(id) },
                                onAddNewOrg = { showAddOrgDialog = true },
                                onOpenAddCollection = {
                                    collectionInitialMemberId = null
                                    collectionInitialTab = 0
                                    showAddCollectionDialog = true
                                },
                                onOpenAddAnonymousCollection = {
                                    collectionInitialMemberId = null
                                    collectionInitialTab = 1
                                    showAddCollectionDialog = true
                                },
                                onOpenAddExpense = { showAddExpenseDialog = true },
                                onOpenAddMember = {
                                    memberToEdit = null
                                    showAddMemberDialog = true
                                },
                                onOpenReports = { currentScreen = NavScreen.REPORTS },
                                onViewReceipt = { receipt ->
                                    activeReceiptCollection = receipt
                                }
                            )
                        }

                        NavScreen.MEMBERS -> {
                            MemberListScreen(
                                members = membersWithFin,
                                organization = activeOrg,
                                onSelectMember = { memberWithFin ->
                                    selectedMemberForDetail = memberWithFin
                                },
                                onAddMember = {
                                    memberToEdit = null
                                    showAddMemberDialog = true
                                },
                                onCollectFromMember = { memberId ->
                                    collectionInitialMemberId = memberId
                                    collectionInitialTab = 0
                                    showAddCollectionDialog = true
                                }
                            )
                        }

                        NavScreen.EXPENSES -> {
                            ExpenseScreen(
                                expenses = expenses,
                                otherIncomes = otherIncomes,
                                onAddExpenseOrIncome = { showAddExpenseDialog = true },
                                onDeleteExpense = { exp -> viewModel.deleteExpense(exp) },
                                onDeleteOtherIncome = { inc -> viewModel.deleteOtherIncome(inc) }
                            )
                        }

                        NavScreen.REPORTS -> {
                            ReportsScreen(
                                organization = activeOrg,
                                financialSummary = financialSummary,
                                members = membersWithFin,
                                collections = collections,
                                expenses = expenses,
                                otherIncomes = otherIncomes
                            )
                        }

                        NavScreen.SETTINGS -> {
                            SettingsScreen(
                                currentOrg = activeOrg,
                                allOrgs = allOrgs,
                                isPinEnabled = viewModel.preferences.isPinLockEnabled,
                                currentPin = viewModel.preferences.pinCode,
                                onSwitchOrg = { id -> viewModel.switchOrganization(id) },
                                onAddNewOrg = { showAddOrgDialog = true },
                                onUpdatePin = { pin, enabled ->
                                    viewModel.updateSecurityPin(pin, enabled)
                                },
                                onExportBackup = { viewModel.getBackupJson() },
                                onImportBackup = { json, cb -> viewModel.restoreBackup(json, cb) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (showAddCollectionDialog) {
        AddCollectionDialog(
            members = membersWithFin,
            initialMemberId = collectionInitialMemberId,
            initialTab = collectionInitialTab,
            orgType = activeOrg?.type ?: "অন্যান্য",
            onDismiss = { showAddCollectionDialog = false },
            onSave = { memberId, donorName, donorPhone, amount, category, method, trxRef, notes ->
                viewModel.addCollection(
                    memberId = memberId,
                    donorName = donorName,
                    donorPhone = donorPhone,
                    amount = amount,
                    category = category,
                    paymentMethod = method,
                    transactionRef = trxRef,
                    notes = notes,
                    onCreated = { createdReceipt ->
                        activeReceiptCollection = createdReceipt
                    }
                )
                showAddCollectionDialog = false
            }
        )
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            orgType = activeOrg?.type ?: "অন্যান্য",
            adminName = activeOrg?.adminName.orEmpty(),
            onDismiss = { showAddExpenseDialog = false },
            onSaveExpense = { title, amount, category, method, spentBy, voucherNo, notes ->
                viewModel.addExpense(title, amount, category, method, spentBy, voucherNo, notes)
                showAddExpenseDialog = false
            },
            onSaveOtherIncome = { source, amount, method, receivedBy, notes ->
                viewModel.addOtherIncome(source, amount, method, receivedBy, notes)
                showAddExpenseDialog = false
            }
        )
    }

    if (showAddMemberDialog) {
        val nextCode = "M-${String.format("%03d", membersWithFin.size + 1)}"
        AddMemberDialog(
            existingMember = memberToEdit,
            nextMemberCode = nextCode,
            onDismiss = {
                showAddMemberDialog = false
                memberToEdit = null
            },
            onSave = { name, phone, code, designation, fee, feeType, address, notes ->
                if (memberToEdit != null) {
                    viewModel.updateMember(
                        memberToEdit!!.copy(
                            name = name,
                            phone = phone,
                            memberCode = code,
                            designation = designation,
                            assignedFee = fee,
                            feeType = feeType,
                            address = address,
                            notes = notes
                        )
                    )
                } else {
                    viewModel.addMember(name, phone, code, designation, fee, feeType, address, notes)
                }
                showAddMemberDialog = false
                memberToEdit = null
            }
        )
    }

    if (showAddOrgDialog) {
        AddOrgDialog(
            onDismiss = { showAddOrgDialog = false },
            onSave = { name, type, adminName, adminRole, phone, address ->
                viewModel.createNewOrganization(name, type, adminName, adminRole, phone, address)
                showAddOrgDialog = false
            }
        )
    }

    if (activeReceiptCollection != null) {
        val col = activeReceiptCollection!!
        val memberFin = membersWithFin.find { it.member.id == col.memberId }
        ReceiptDialog(
            collection = col,
            member = memberFin?.member,
            organization = activeOrg,
            remainingDue = memberFin?.dueAmount ?: 0.0,
            onDismiss = { activeReceiptCollection = null }
        )
    }
}
