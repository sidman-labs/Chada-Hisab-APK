package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "collections",
    foreignKeys = [
        ForeignKey(
            entity = OrganizationEntity::class,
            parentColumns = ["id"],
            childColumns = ["orgId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("orgId"), Index("memberId")]
)
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orgId: Long,
    val memberId: Long? = null, // null for anonymous / non-member donation
    val donorName: String = "", // e.g. "অজ্ঞাত দাতা" or guest name
    val donorPhone: String = "", // optional phone for receipt
    val receiptNo: String, // e.g. REC-202610-001
    val amount: Double,
    val category: String, // মুক্তহস্তে দান, জুমার কালেকশন, দানবাক্স, ইত্যাদি
    val paymentMethod: String = "ক্যাশ", // ক্যাশ, বিকাশ, নগদ, রকেট, ব্যাংক
    val transactionRef: String = "",
    val collectedBy: String,
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)
