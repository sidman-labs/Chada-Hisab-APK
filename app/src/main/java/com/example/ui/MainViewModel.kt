package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.local.entities.CollectionEntity
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.MemberEntity
import com.example.data.local.entities.OrganizationEntity
import com.example.data.local.entities.OtherIncomeEntity
import com.example.data.model.FinancialSummary
import com.example.data.model.MemberWithFinancials
import com.example.data.model.MonthlyStat
import com.example.data.repository.ChadaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = ChadaRepository(db.appDao())
    val preferences = AppPreferences(application)

    private val _isSetupCompleted = MutableStateFlow(preferences.isSetupCompleted)
    val isSetupCompleted: StateFlow<Boolean> = _isSetupCompleted.asStateFlow()

    private val _isLocked = MutableStateFlow(preferences.isPinLockEnabled && preferences.pinCode.isNotEmpty())
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _activeOrgId = MutableStateFlow(preferences.activeOrgId)
    val activeOrgId: StateFlow<Long> = _activeOrgId.asStateFlow()

    val allOrganizations: StateFlow<List<OrganizationEntity>> = repository.getAllOrganizations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeOrganization: StateFlow<OrganizationEntity?> = _activeOrgId
        .flatMapLatest { id ->
            if (id > 0) repository.getOrganizationById(id) else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val financialSummary: StateFlow<FinancialSummary> = _activeOrgId
        .flatMapLatest { id ->
            if (id > 0) repository.getFinancialSummary(id) else flowOf(FinancialSummary())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FinancialSummary()
        )

    val membersWithFinancials: StateFlow<List<MemberWithFinancials>> = _activeOrgId
        .flatMapLatest { id ->
            if (id > 0) repository.getMembersWithFinancials(id) else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val collections: StateFlow<List<CollectionEntity>> = _activeOrgId
        .flatMapLatest { id ->
            if (id > 0) repository.getCollectionsByOrg(id) else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val expenses: StateFlow<List<ExpenseEntity>> = _activeOrgId
        .flatMapLatest { id ->
            if (id > 0) repository.getExpensesByOrg(id) else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val otherIncomes: StateFlow<List<OtherIncomeEntity>> = _activeOrgId
        .flatMapLatest { id ->
            if (id > 0) repository.getOtherIncomesByOrg(id) else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Monthly breakdown for charts/overview
    val monthlyStats: StateFlow<List<MonthlyStat>> = combine(collections, expenses) { colls: List<CollectionEntity>, exps: List<ExpenseEntity> ->
        val monthMap = linkedMapOf<String, Pair<Double, Double>>() // key -> Pair(income, expense)
        val sdfKey = SimpleDateFormat("yyyy-MM", Locale.US)
        val sdfName = SimpleDateFormat("MMM", Locale.US)

        // Seed current and prior 3 months so chart always has context
        for (i in 3 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.MONTH, -i)
            val k = sdfKey.format(c.time)
            monthMap[k] = Pair(0.0, 0.0)
        }

        colls.forEach { c ->
            val k = sdfKey.format(Date(c.date))
            val current = monthMap[k] ?: Pair(0.0, 0.0)
            monthMap[k] = Pair(current.first + c.amount, current.second)
        }

        exps.forEach { e ->
            val k = sdfKey.format(Date(e.date))
            val current = monthMap[k] ?: Pair(0.0, 0.0)
            monthMap[k] = Pair(current.first, current.second + e.amount)
        }

        val entriesList = monthMap.entries.toList().takeLast(6)
        entriesList.map { entry ->
            val key = entry.key
            val pair = entry.value
            val parsedDate = try { sdfKey.parse(key) } catch (e: Exception) { null }
            val bnName = when (parsedDate?.let { sdfName.format(it) }) {
                "Jan" -> "জানু"
                "Feb" -> "ফেব্রু"
                "Mar" -> "মার্চ"
                "Apr" -> "এপ্রিল"
                "May" -> "মে"
                "Jun" -> "জুন"
                "Jul" -> "জুলাই"
                "Aug" -> "আগস্ট"
                "Sep" -> "সেপ্টে"
                "Oct" -> "অক্টো"
                "Nov" -> "নভে"
                "Dec" -> "ডিসে"
                else -> key
            }
            MonthlyStat(
                monthYearKey = key,
                monthNameBn = bnName,
                collectionAmount = pair.first,
                expenseAmount = pair.second
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Initialize if organizations already exist in DB
        viewModelScope.launch {
            if (_activeOrgId.value <= 0) {
                val firstOrg = db.appDao().getFirstOrganization()
                if (firstOrg != null) {
                    preferences.activeOrgId = firstOrg.id
                    preferences.isSetupCompleted = true
                    _activeOrgId.value = firstOrg.id
                    _isSetupCompleted.value = true
                }
            }
        }
    }

    fun unlockApp(pin: String): Boolean {
        if (!preferences.isPinLockEnabled || preferences.pinCode.isEmpty()) {
            _isLocked.value = false
            return true
        }
        return if (pin == preferences.pinCode) {
            _isLocked.value = false
            true
        } else {
            false
        }
    }

    fun completeProfileSetup(
        userName: String,
        userRole: String,
        phone: String,
        address: String,
        orgName: String,
        orgType: String,
        pin: String,
        enablePin: Boolean,
        sampleMembers: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val org = OrganizationEntity(
                name = orgName.trim().ifEmpty { "আমাদের সংগঠন" },
                type = orgType,
                adminName = userName.trim(),
                adminRole = userRole.trim(),
                phone = phone.trim(),
                address = address.trim(),
                currencySymbol = "৳",
                isDefault = true
            )
            val newOrgId = repository.insertOrganization(org)

            // Add sample members if provided or default 3 members to kickstart
            val initialMembers = if (sampleMembers.isNotEmpty()) {
                sampleMembers.mapIndexed { idx, name ->
                    MemberEntity(
                        orgId = newOrgId,
                        memberCode = "M-${String.format("%03d", idx + 1)}",
                        name = name.trim(),
                        phone = phone,
                        designation = if (idx == 0) userRole else "সদস্য",
                        assignedFee = 500.0,
                        feeType = "মাসিক",
                        avatarColorIndex = idx % 6
                    )
                }
            } else {
                listOf(
                    MemberEntity(
                        orgId = newOrgId,
                        memberCode = "M-001",
                        name = userName.trim().ifEmpty { "সভাপতি সাহেব" },
                        phone = phone,
                        designation = userRole,
                        assignedFee = 1000.0,
                        feeType = "মাসিক",
                        avatarColorIndex = 0
                    ),
                    MemberEntity(
                        orgId = newOrgId,
                        memberCode = "M-002",
                        name = "মোঃ রফিকুল ইসলাম",
                        phone = "01711223344",
                        designation = "সদস্য",
                        assignedFee = 500.0,
                        feeType = "মাসিক",
                        avatarColorIndex = 1
                    ),
                    MemberEntity(
                        orgId = newOrgId,
                        memberCode = "M-003",
                        name = "আব্দুর রহমান",
                        phone = "01811556677",
                        designation = "সদস্য",
                        assignedFee = 500.0,
                        feeType = "মাসিক",
                        avatarColorIndex = 2
                    )
                )
            }

            initialMembers.forEach {
                val memberId = repository.insertMember(it)
                // Add initial collection for the first member
                if (it.memberCode == "M-001") {
                    repository.insertCollection(
                        CollectionEntity(
                            orgId = newOrgId,
                            memberId = memberId,
                            receiptNo = "REC-0001",
                            amount = 1000.0,
                            category = "মাসিক চাঁদা",
                            paymentMethod = "ক্যাশ",
                            collectedBy = userName.trim().ifEmpty { "কোষাধ্যক্ষ" },
                            notes = "উদ্বোধনী চাঁদা পরিশোধ"
                        )
                    )
                }
            }

            if (enablePin && pin.isNotEmpty()) {
                preferences.isPinLockEnabled = true
                preferences.pinCode = pin
            } else {
                preferences.isPinLockEnabled = false
                preferences.pinCode = ""
            }

            preferences.activeOrgId = newOrgId
            preferences.isSetupCompleted = true
            _activeOrgId.value = newOrgId
            _isSetupCompleted.value = true
            _isLocked.value = false
        }
    }

    fun switchOrganization(orgId: Long) {
        preferences.activeOrgId = orgId
        _activeOrgId.value = orgId
    }

    fun createNewOrganization(
        name: String,
        type: String,
        adminName: String,
        adminRole: String,
        phone: String,
        address: String
    ) {
        viewModelScope.launch {
            val org = OrganizationEntity(
                name = name.trim(),
                type = type,
                adminName = adminName.trim(),
                adminRole = adminRole.trim(),
                phone = phone.trim(),
                address = address.trim(),
                currencySymbol = "৳",
                isDefault = false
            )
            val id = repository.insertOrganization(org)
            switchOrganization(id)
        }
    }

    fun addMember(
        name: String,
        phone: String,
        memberCode: String,
        designation: String,
        assignedFee: Double,
        feeType: String,
        address: String,
        notes: String
    ) {
        val orgId = _activeOrgId.value
        if (orgId <= 0) return
        viewModelScope.launch {
            val code = memberCode.trim().ifEmpty {
                val count = membersWithFinancials.value.size + 1
                "M-${String.format("%03d", count)}"
            }
            val member = MemberEntity(
                orgId = orgId,
                memberCode = code,
                name = name.trim(),
                phone = phone.trim(),
                designation = designation.trim().ifEmpty { "সাধারণ সদস্য" },
                assignedFee = assignedFee,
                feeType = feeType,
                address = address.trim(),
                notes = notes.trim(),
                avatarColorIndex = (membersWithFinancials.value.size) % 6
            )
            repository.insertMember(member)
        }
    }

    fun updateMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.updateMember(member)
        }
    }

    fun deleteMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }

    fun addCollection(
        memberId: Long?,
        donorName: String = "",
        donorPhone: String = "",
        amount: Double,
        category: String,
        paymentMethod: String,
        transactionRef: String,
        notes: String,
        date: Long = System.currentTimeMillis(),
        onCreated: ((CollectionEntity) -> Unit)? = null
    ) {
        val orgId = _activeOrgId.value
        if (orgId <= 0) return
        viewModelScope.launch {
            val receiptNo = repository.getNextReceiptNumber(orgId)
            val collector = activeOrganization.value?.let { "${it.adminName} (${it.adminRole})" } ?: "কোষাধ্যক্ষ"
            val collection = CollectionEntity(
                orgId = orgId,
                memberId = memberId,
                donorName = donorName.trim(),
                donorPhone = donorPhone.trim(),
                receiptNo = receiptNo,
                amount = amount,
                category = category,
                paymentMethod = paymentMethod,
                transactionRef = transactionRef,
                collectedBy = collector,
                date = date,
                notes = notes
            )
            repository.insertCollection(collection)
            onCreated?.invoke(collection)
        }
    }

    fun deleteCollection(collection: CollectionEntity) {
        viewModelScope.launch {
            repository.deleteCollection(collection)
        }
    }

    fun addExpense(
        title: String,
        amount: Double,
        category: String,
        paymentMethod: String,
        spentBy: String,
        voucherNo: String,
        notes: String,
        date: Long = System.currentTimeMillis()
    ) {
        val orgId = _activeOrgId.value
        if (orgId <= 0) return
        viewModelScope.launch {
            val expense = ExpenseEntity(
                orgId = orgId,
                title = title.trim(),
                amount = amount,
                category = category,
                paymentMethod = paymentMethod,
                spentBy = spentBy.trim(),
                voucherNo = voucherNo.trim(),
                date = date,
                notes = notes.trim()
            )
            repository.insertExpense(expense)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun addOtherIncome(
        source: String,
        amount: Double,
        paymentMethod: String,
        receivedBy: String,
        notes: String,
        date: Long = System.currentTimeMillis()
    ) {
        val orgId = _activeOrgId.value
        if (orgId <= 0) return
        viewModelScope.launch {
            val income = OtherIncomeEntity(
                orgId = orgId,
                source = source.trim(),
                amount = amount,
                paymentMethod = paymentMethod,
                receivedBy = receivedBy.trim(),
                date = date,
                notes = notes.trim()
            )
            repository.insertOtherIncome(income)
        }
    }

    fun deleteOtherIncome(income: OtherIncomeEntity) {
        viewModelScope.launch {
            repository.deleteOtherIncome(income)
        }
    }

    fun updateSecurityPin(newPin: String, enabled: Boolean) {
        preferences.isPinLockEnabled = enabled
        preferences.pinCode = if (enabled) newPin else ""
        _isLocked.value = false
    }

    suspend fun getBackupJson(): String {
        return repository.exportBackupJson()
    }

    fun restoreBackup(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importBackupJson(jsonString)
            if (success) {
                val firstOrg = db.appDao().getFirstOrganization()
                if (firstOrg != null) {
                    preferences.activeOrgId = firstOrg.id
                    preferences.isSetupCompleted = true
                    _activeOrgId.value = firstOrg.id
                    _isSetupCompleted.value = true
                }
            }
            onResult(success)
        }
    }
}
