package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "other_incomes",
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
data class OtherIncomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orgId: Long,
    val source: String, // দানবাক্স, ব্যাংকের মুনাফা, স্পনসর, অজ্ঞাত অনুদান
    val amount: Double,
    val paymentMethod: String = "ক্যাশ",
    val receivedBy: String = "",
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)
