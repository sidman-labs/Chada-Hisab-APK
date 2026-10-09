package com.example.data.repository

import com.example.data.local.dao.AppDao
import com.example.data.local.entities.CollectionEntity
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.MemberEntity
import com.example.data.local.entities.OrganizationEntity
import com.example.data.local.entities.OtherIncomeEntity
import com.example.data.model.FinancialSummary
import com.example.data.model.MemberWithFinancials
import com.example.data.model.PaymentStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.json.JSONArray
import org.json.JSONObject

class ChadaRepository(private val dao: AppDao) {

    // Organizations
    fun getAllOrganizations(): Flow<List<OrganizationEntity>> = dao.getAllOrganizations()

    fun getOrganizationById(id: Long): Flow<OrganizationEntity?> = dao.getOrganizationById(id)

    suspend fun insertOrganization(org: OrganizationEntity): Long = dao.insertOrganization(org)

    suspend fun updateOrganization(org: OrganizationEntity) = dao.updateOrganization(org)

    suspend fun deleteOrganization(org: OrganizationEntity) = dao.deleteOrganization(org)

    // Members
    fun getMembersByOrg(orgId: Long): Flow<List<MemberEntity>> = dao.getMembersByOrg(orgId)

    fun getMemberById(id: Long): Flow<MemberEntity?> = dao.getMemberById(id)

    suspend fun insertMember(member: MemberEntity): Long = dao.insertMember(member)

    suspend fun updateMember(member: MemberEntity) = dao.updateMember(member)

    suspend fun deleteMember(member: MemberEntity) = dao.deleteMember(member)

    // Collections
    fun getCollectionsByOrg(orgId: Long): Flow<List<CollectionEntity>> = dao.getCollectionsByOrg(orgId)

    fun getCollectionsByMember(memberId: Long): Flow<List<CollectionEntity>> = dao.getCollectionsByMember(memberId)

    suspend fun insertCollection(collection: CollectionEntity): Long = dao.insertCollection(collection)

    suspend fun deleteCollection(collection: CollectionEntity) = dao.deleteCollection(collection)

    suspend fun getNextReceiptNumber(orgId: Long): String {
        val count = dao.getCollectionCount(orgId) + 1
        return "REC-${String.format("%04d", count)}"
    }

    // Expenses
    fun getExpensesByOrg(orgId: Long): Flow<List<ExpenseEntity>> = dao.getExpensesByOrg(orgId)

    suspend fun insertExpense(expense: ExpenseEntity): Long = dao.insertExpense(expense)

    suspend fun deleteExpense(expense: ExpenseEntity) = dao.deleteExpense(expense)

    // Other Incomes
    fun getOtherIncomesByOrg(orgId: Long): Flow<List<OtherIncomeEntity>> = dao.getOtherIncomesByOrg(orgId)

    suspend fun insertOtherIncome(income: OtherIncomeEntity): Long = dao.insertOtherIncome(income)

    suspend fun deleteOtherIncome(income: OtherIncomeEntity) = dao.deleteOtherIncome(income)

    // Calculated Members with Financials
    fun getMembersWithFinancials(orgId: Long): Flow<List<MemberWithFinancials>> {
        return combine(
            dao.getMembersByOrg(orgId),
            dao.getCollectionsByOrg(orgId)
        ) { members, collections ->
            val paymentsByMember = collections.filter { it.memberId != null }.groupBy { it.memberId!! }

            members.map { member ->
                val memberPayments = paymentsByMember[member.id].orEmpty()
                val totalPaid = memberPayments.sumOf { it.amount }
                val due = (member.assignedFee - totalPaid).coerceAtLeast(0.0)
                val status = when {
                    totalPaid >= member.assignedFee && member.assignedFee > 0 -> PaymentStatus.PAID
                    totalPaid > 0 -> PaymentStatus.PARTIAL
                    else -> PaymentStatus.UNPAID
                }
                val lastPayment = memberPayments.maxByOrNull { it.date }?.date

                MemberWithFinancials(
                    member = member,
                    totalPaid = totalPaid,
                    dueAmount = due,
                    status = status,
                    collectionCount = memberPayments.size,
                    lastPaymentDate = lastPayment
                )
            }
        }
    }

    // Financial Summary Flow
    fun getFinancialSummary(orgId: Long): Flow<FinancialSummary> {
        return combine(
            getMembersWithFinancials(orgId),
            dao.getCollectionsByOrg(orgId),
            dao.getExpensesByOrg(orgId),
            dao.getOtherIncomesByOrg(orgId)
        ) { membersWithFin, collections, expenses, otherIncomes ->
            val totalMembers = membersWithFin.size
            val totalAssignedFee = membersWithFin.sumOf { it.member.assignedFee }
            val totalCollectedFee = collections.sumOf { it.amount }
            val totalOtherIncome = otherIncomes.sumOf { it.amount }
            val totalIncome = totalCollectedFee + totalOtherIncome
            val totalDue = membersWithFin.sumOf { it.dueAmount }
            val totalExpense = expenses.sumOf { it.amount }
            val netBalance = totalIncome - totalExpense

            val progressPercent = if (totalAssignedFee > 0) {
                ((totalCollectedFee / totalAssignedFee) * 100).toFloat().coerceIn(0f, 100f)
            } else if (totalCollectedFee > 0) {
                100f
            } else {
                0f
            }

            val paidCount = membersWithFin.count { it.status == PaymentStatus.PAID }
            val partialCount = membersWithFin.count { it.status == PaymentStatus.PARTIAL }
            val unpaidCount = membersWithFin.count { it.status == PaymentStatus.UNPAID }

            FinancialSummary(
                totalMembers = totalMembers,
                totalAssignedFee = totalAssignedFee,
                totalCollectedFee = totalCollectedFee,
                totalOtherIncome = totalOtherIncome,
                totalIncome = totalIncome,
                totalDue = totalDue,
                totalExpense = totalExpense,
                netBalance = netBalance,
                collectionProgressPercent = progressPercent,
                paidMembersCount = paidCount,
                partialMembersCount = partialCount,
                unpaidMembersCount = unpaidCount
            )
        }
    }

    // Backup Export to JSON
    suspend fun exportBackupJson(): String {
        val orgs = dao.getAllOrganizationsSync()
        val members = dao.getAllMembersSync()
        val collections = dao.getAllCollectionsSync()
        val expenses = dao.getAllExpensesSync()
        val otherIncomes = dao.getAllOtherIncomesSync()

        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "Chada Hishab")
        root.put("exportTimestamp", System.currentTimeMillis())

        val orgsArray = JSONArray()
        orgs.forEach {
            val o = JSONObject()
            o.put("id", it.id)
            o.put("name", it.name)
            o.put("type", it.type)
            o.put("adminName", it.adminName)
            o.put("adminRole", it.adminRole)
            o.put("phone", it.phone)
            o.put("address", it.address)
            o.put("currencySymbol", it.currencySymbol)
            o.put("createdAt", it.createdAt)
            o.put("isDefault", it.isDefault)
            orgsArray.put(o)
        }
        root.put("organizations", orgsArray)

        val membersArray = JSONArray()
        members.forEach {
            val m = JSONObject()
            m.put("id", it.id)
            m.put("orgId", it.orgId)
            m.put("memberCode", it.memberCode)
            m.put("name", it.name)
            m.put("phone", it.phone)
            m.put("designation", it.designation)
            m.put("assignedFee", it.assignedFee)
            m.put("feeType", it.feeType)
            m.put("joinedDate", it.joinedDate)
            m.put("address", it.address)
            m.put("notes", it.notes)
            m.put("avatarColorIndex", it.avatarColorIndex)
            m.put("isActive", it.isActive)
            membersArray.put(m)
        }
        root.put("members", membersArray)

        val collectionsArray = JSONArray()
        collections.forEach {
            val c = JSONObject()
            c.put("id", it.id)
            c.put("orgId", it.orgId)
            if (it.memberId != null) c.put("memberId", it.memberId)
            c.put("donorName", it.donorName)
            c.put("donorPhone", it.donorPhone)
            c.put("receiptNo", it.receiptNo)
            c.put("amount", it.amount)
            c.put("category", it.category)
            c.put("paymentMethod", it.paymentMethod)
            c.put("transactionRef", it.transactionRef)
            c.put("collectedBy", it.collectedBy)
            c.put("date", it.date)
            c.put("notes", it.notes)
            collectionsArray.put(c)
        }
        root.put("collections", collectionsArray)

        val expensesArray = JSONArray()
        expenses.forEach {
            val e = JSONObject()
            e.put("id", it.id)
            e.put("orgId", it.orgId)
            e.put("category", it.category)
            e.put("title", it.title)
            e.put("amount", it.amount)
            e.put("paymentMethod", it.paymentMethod)
            e.put("spentBy", it.spentBy)
            e.put("voucherNo", it.voucherNo)
            e.put("date", it.date)
            e.put("notes", it.notes)
            expensesArray.put(e)
        }
        root.put("expenses", expensesArray)

        val otherIncomesArray = JSONArray()
        otherIncomes.forEach {
            val oi = JSONObject()
            oi.put("id", it.id)
            oi.put("orgId", it.orgId)
            oi.put("source", it.source)
            oi.put("amount", it.amount)
            oi.put("paymentMethod", it.paymentMethod)
            oi.put("receivedBy", it.receivedBy)
            oi.put("date", it.date)
            oi.put("notes", it.notes)
            otherIncomesArray.put(oi)
        }
        root.put("otherIncomes", otherIncomesArray)

        return root.toString(2)
    }

    // Restore from JSON string
    suspend fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("organizations")) return false

            dao.deleteAllCollections()
            dao.deleteAllExpenses()
            dao.deleteAllOtherIncomes()
            dao.deleteAllMembers()
            dao.deleteAllOrganizations()

            val orgsArray = root.getJSONArray("organizations")
            for (i in 0 until orgsArray.length()) {
                val o = orgsArray.getJSONObject(i)
                dao.insertOrganization(
                    OrganizationEntity(
                        id = o.optLong("id", 0L),
                        name = o.getString("name"),
                        type = o.optString("type", "অন্যান্য"),
                        adminName = o.optString("adminName", ""),
                        adminRole = o.optString("adminRole", "সভাপতি"),
                        phone = o.optString("phone", ""),
                        address = o.optString("address", ""),
                        currencySymbol = o.optString("currencySymbol", "৳"),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        isDefault = o.optBoolean("isDefault", i == 0)
                    )
                )
            }

            if (root.has("members")) {
                val membersArray = root.getJSONArray("members")
                for (i in 0 until membersArray.length()) {
                    val m = membersArray.getJSONObject(i)
                    dao.insertMember(
                        MemberEntity(
                            id = m.optLong("id", 0L),
                            orgId = m.getLong("orgId"),
                            memberCode = m.optString("memberCode", "M-${i + 1}"),
                            name = m.getString("name"),
                            phone = m.optString("phone", ""),
                            designation = m.optString("designation", "সদস্য"),
                            assignedFee = m.optDouble("assignedFee", 0.0),
                            feeType = m.optString("feeType", "মাসিক"),
                            joinedDate = m.optLong("joinedDate", System.currentTimeMillis()),
                            address = m.optString("address", ""),
                            notes = m.optString("notes", ""),
                            avatarColorIndex = m.optInt("avatarColorIndex", i % 6),
                            isActive = m.optBoolean("isActive", true)
                        )
                    )
                }
            }

            if (root.has("collections")) {
                val collectionsArray = root.getJSONArray("collections")
                for (i in 0 until collectionsArray.length()) {
                    val c = collectionsArray.getJSONObject(i)
                    val memberIdVal = if (c.has("memberId") && !c.isNull("memberId")) c.getLong("memberId") else null
                    dao.insertCollection(
                        CollectionEntity(
                            id = c.optLong("id", 0L),
                            orgId = c.getLong("orgId"),
                            memberId = memberIdVal,
                            donorName = c.optString("donorName", ""),
                            donorPhone = c.optString("donorPhone", ""),
                            receiptNo = c.optString("receiptNo", "REC-$i"),
                            amount = c.getDouble("amount"),
                            category = c.optString("category", "মাসিক চাঁদা"),
                            paymentMethod = c.optString("paymentMethod", "ক্যাশ"),
                            transactionRef = c.optString("transactionRef", ""),
                            collectedBy = c.optString("collectedBy", ""),
                            date = c.optLong("date", System.currentTimeMillis()),
                            notes = c.optString("notes", "")
                        )
                    )
                }
            }

            if (root.has("expenses")) {
                val expensesArray = root.getJSONArray("expenses")
                for (i in 0 until expensesArray.length()) {
                    val e = expensesArray.getJSONObject(i)
                    dao.insertExpense(
                        ExpenseEntity(
                            id = e.optLong("id", 0L),
                            orgId = e.getLong("orgId"),
                            category = e.optString("category", "সাধারণ খরচ"),
                            title = e.getString("title"),
                            amount = e.getDouble("amount"),
                            paymentMethod = e.optString("paymentMethod", "ক্যাশ"),
                            spentBy = e.optString("spentBy", ""),
                            voucherNo = e.optString("voucherNo", ""),
                            date = e.optLong("date", System.currentTimeMillis()),
                            notes = e.optString("notes", "")
                        )
                    )
                }
            }

            if (root.has("otherIncomes")) {
                val oiArray = root.getJSONArray("otherIncomes")
                for (i in 0 until oiArray.length()) {
                    val oi = oiArray.getJSONObject(i)
                    dao.insertOtherIncome(
                        OtherIncomeEntity(
                            id = oi.optLong("id", 0L),
                            orgId = oi.getLong("orgId"),
                            source = oi.getString("source"),
                            amount = oi.getDouble("amount"),
                            paymentMethod = oi.optString("paymentMethod", "ক্যাশ"),
                            receivedBy = oi.optString("receivedBy", ""),
                            date = oi.optLong("date", System.currentTimeMillis()),
                            notes = oi.optString("notes", "")
                        )
                    )
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
