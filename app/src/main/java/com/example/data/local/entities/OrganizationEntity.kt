package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "organizations")
data class OrganizationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // রাজনৈতিক দল, পূজা কমিটি, মসজিদ ফান্ড, পাড়ার ক্লাব, ওয়াজ মাহফিল, অন্যান্য
    val adminName: String,
    val adminRole: String, // সভাপতি, সম্পাদক, কোষাধ্যক্ষ, সদস্য
    val phone: String,
    val address: String,
    val currencySymbol: String = "৳",
    val createdAt: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false
)
