package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = OrganizationEntity::class,
            parentColumns = ["id"],
            childColumns = ["orgId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("orgId")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orgId: Long,
    val category: String, // মেরামত, আপ্যায়ন, সভা খরচ, ইত্যাদি
    val title: String,
    val amount: Double,
    val paymentMethod: String = "ক্যাশ",
    val spentBy: String = "",
    val voucherNo: String = "",
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)
