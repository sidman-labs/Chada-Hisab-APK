package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.CollectionEntity
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.MemberEntity
import com.example.data.local.entities.OrganizationEntity
import com.example.data.local.entities.OtherIncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Organizations
    @Query("SELECT * FROM organizations ORDER BY id ASC")
    fun getAllOrganizations(): Flow<List<OrganizationEntity>>

    @Query("SELECT * FROM organizations WHERE id = :id LIMIT 1")
    fun getOrganizationById(id: Long): Flow<OrganizationEntity?>

    @Query("SELECT * FROM organizations WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultOrganization(): OrganizationEntity?

    @Query("SELECT * FROM organizations ORDER BY id ASC LIMIT 1")
    suspend fun getFirstOrganization(): OrganizationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganization(org: OrganizationEntity): Long

    @Update
    suspend fun updateOrganization(org: OrganizationEntity)

    @Delete
    suspend fun deleteOrganization(org: OrganizationEntity)

    // Members
    @Query("SELECT * FROM members WHERE orgId = :orgId ORDER BY id ASC")
    fun getMembersByOrg(orgId: Long): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    fun getMemberById(id: Long): Flow<MemberEntity?>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    suspend fun getMemberByIdSync(id: Long): MemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<MemberEntity>)

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Delete
    suspend fun deleteMember(member: MemberEntity)

    // Collections
    @Query("SELECT * FROM collections WHERE orgId = :orgId ORDER BY date DESC, id DESC")
    fun getCollectionsByOrg(orgId: Long): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections WHERE memberId = :memberId ORDER BY date DESC, id DESC")
    fun getCollectionsByMember(memberId: Long): Flow<List<CollectionEntity>>

    @Query("SELECT COUNT(*) FROM collections WHERE orgId = :orgId")
    suspend fun getCollectionCount(orgId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollections(collections: List<CollectionEntity>)

    @Delete
    suspend fun deleteCollection(collection: CollectionEntity)

    // Expenses
    @Query("SELECT * FROM expenses WHERE orgId = :orgId ORDER BY date DESC, id DESC")
    fun getExpensesByOrg(orgId: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    // Other Incomes
    @Query("SELECT * FROM other_incomes WHERE orgId = :orgId ORDER BY date DESC, id DESC")
    fun getOtherIncomesByOrg(orgId: Long): Flow<List<OtherIncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOtherIncome(income: OtherIncomeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOtherIncomes(incomes: List<OtherIncomeEntity>)

    @Delete
    suspend fun deleteOtherIncome(income: OtherIncomeEntity)

    // Backup & Restore
    @Query("SELECT * FROM organizations")
    suspend fun getAllOrganizationsSync(): List<OrganizationEntity>

    @Query("SELECT * FROM members")
    suspend fun getAllMembersSync(): List<MemberEntity>

    @Query("SELECT * FROM collections")
    suspend fun getAllCollectionsSync(): List<CollectionEntity>

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpensesSync(): List<ExpenseEntity>

    @Query("SELECT * FROM other_incomes")
    suspend fun getAllOtherIncomesSync(): List<OtherIncomeEntity>

    @Query("DELETE FROM collections")
    suspend fun deleteAllCollections()

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Query("DELETE FROM other_incomes")
    suspend fun deleteAllOtherIncomes()

    @Query("DELETE FROM members")
    suspend fun deleteAllMembers()

    @Query("DELETE FROM organizations")
    suspend fun deleteAllOrganizations()
}
