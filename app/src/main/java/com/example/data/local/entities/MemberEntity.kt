package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "members",
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
data class MemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orgId: Long,
    val memberCode: String, // e.g. M-101
    val name: String,
    val phone: String,
    val designation: String = "সাধারণ সদস্য", // পদবী
    val assignedFee: Double, // ধার্যকৃত চাঁদা
    val feeType: String = "মাসিক", // মাসিক / এককালীন
    val joinedDate: Long = System.currentTimeMillis(),
    val address: String = "",
    val notes: String = "",
    val avatarColorIndex: Int = 0,
    val isActive: Boolean = true
)
