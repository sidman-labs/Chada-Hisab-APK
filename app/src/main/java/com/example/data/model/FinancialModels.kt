package com.example.data.model

import com.example.data.local.entities.MemberEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PaymentStatus(val labelBn: String) {
    PAID("পরিশোধিত"),
    PARTIAL("আংশিক বাকি"),
    UNPAID("বকেয়া")
}

data class MemberWithFinancials(
    val member: MemberEntity,
    val totalPaid: Double,
    val dueAmount: Double,
    val status: PaymentStatus,
    val collectionCount: Int,
    val lastPaymentDate: Long?
)

data class FinancialSummary(
    val totalMembers: Int = 0,
    val totalAssignedFee: Double = 0.0,
    val totalCollectedFee: Double = 0.0,
    val totalOtherIncome: Double = 0.0,
    val totalIncome: Double = totalCollectedFee + totalOtherIncome,
    val totalDue: Double = (totalAssignedFee - totalCollectedFee).coerceAtLeast(0.0),
    val totalExpense: Double = 0.0,
    val netBalance: Double = totalIncome - totalExpense,
    val collectionProgressPercent: Float = 0f,
    val paidMembersCount: Int = 0,
    val partialMembersCount: Int = 0,
    val unpaidMembersCount: Int = 0
)

data class MonthlyStat(
    val monthYearKey: String, // e.g. "2026-10"
    val monthNameBn: String, // e.g. "অক্টোবর"
    val collectionAmount: Double,
    val expenseAmount: Double
)

object BengaliFormatter {
    private val digitMap = mapOf(
        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
    )

    fun toBengaliDigits(input: String): String {
        return input.map { digitMap[it] ?: it }.joinToString("")
    }

    fun toBengaliDigits(number: Long): String {
        return toBengaliDigits(number.toString())
    }

    fun toBengaliDigits(number: Int): String {
        return toBengaliDigits(number.toString())
    }

    fun formatCurrency(amount: Double): String {
        val isNegative = amount < 0
        val absVal = kotlin.math.abs(amount)
        val formatted = if (absVal % 1.0 == 0.0) {
            String.format(Locale.US, "%,.0f", absVal)
        } else {
            String.format(Locale.US, "%,.2f", absVal)
        }
        val bnDigits = toBengaliDigits(formatted)
        return if (isNegative) "-৳ $bnDigits" else "৳ $bnDigits"
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "-"
        val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.US)
        val formatted = sdf.format(Date(timestamp))
        // Convert month abbreviations if desired or digits
        return toBengaliDigits(formatted)
            .replace("Jan", "জানু")
            .replace("Feb", "ফেব্রু")
            .replace("Mar", "মার্চ")
            .replace("Apr", "এপ্রিল")
            .replace("May", "মে")
            .replace("Jun", "জুন")
            .replace("Jul", "জুলাই")
            .replace("Aug", "আগস্ট")
            .replace("Sep", "সেপ্টে")
            .replace("Oct", "অক্টো")
            .replace("Nov", "নভে")
            .replace("Dec", "ডিসে")
    }

    fun formatDateTime(timestamp: Long): String {
        if (timestamp <= 0) return "-"
        val sdf = SimpleDateFormat("dd MMM, yyyy - hh:mm a", Locale.US)
        return toBengaliDigits(sdf.format(Date(timestamp)))
    }
}

object OrganizationTypePresets {
    val types = listOf(
        "মসজিদ ফান্ড",
        "পাড়ার ক্লাব",
        "রাজনৈতিক দল",
        "পূজা কমিটি",
        "ওয়াজ মাহফিল",
        "বন্ধুদের সমিতি / পিকনিক",
        "মাসিক সঞ্চয় সমিতি",
        "অন্যান্য সংগঠন"
    )

    fun getDefaultIncomeCategories(type: String): List<String> {
        return when (type) {
            "মসজিদ ফান্ড" -> listOf(
                "মাসিক সদস্য চাঁদা",
                "জুমার কালেকশন",
                "দান বাক্স",
                "বিশেষ অনুদান",
                "রোজার ইফতার ফান্ড",
                "অন্যান্য"
            )
            "রাজনৈতিক দল" -> listOf(
                "সদস্য মাসিক চাঁদা",
                "মাসিক লেভি",
                "দলীয় সম্মেলন অনুদান",
                "শুভাকাঙ্ক্ষী দান",
                "অন্যান্য"
            )
            "পূজা কমিটি" -> listOf(
                "পাড়াভিত্তিক চাঁদা",
                "পরিবারভিত্তিক চাঁদা",
                "প্রণামী ও বিশেষ অনুদান",
                "পূজা দান বাক্স",
                "অন্যান্য"
            )
            "পাড়ার ক্লাব" -> listOf(
                "মাসিক সঞ্চয়",
                "ক্লাব সদস্য ফি",
                "টুর্নামেন্ট এন্ট্রি ফি",
                "উপদেষ্টা অনুদান",
                "অন্যান্য"
            )
            "ওয়াজ মাহফিল" -> listOf(
                "গ্রামভিত্তিক চাঁদা",
                "দোকানদারদের চাঁদা",
                "প্রবাসী ভাইদের দান",
                "মঞ্চ কালেকশন",
                "অন্যান্য"
            )
            "বন্ধুদের সমিতি / পিকনিক" -> listOf(
                "জনপ্রতি চাঁদা",
                "গেস্ট / অতিথি ফি",
                "বিশেষ স্পনসর",
                "অন্যান্য"
            )
            else -> listOf(
                "নিয়মিত চাঁদা",
                "সদস্য ভর্তি ফি",
                "সাধারণ অনুদান",
                "অন্যান্য"
            )
        }
    }

    fun getDefaultExpenseCategories(type: String): List<String> {
        return when (type) {
            "মসজিদ ফান্ড" -> listOf(
                "ইমাম ও মুয়াজ্জিনের সম্মানী",
                "বিদ্যুৎ ও পানি বিল",
                "মসজিদ সংস্কার ও মেরামত",
                "সাউন্ড সিস্টেম ও মাইক",
                "পরিষ্কার-পরিচ্ছন্নতা",
                "অন্যান্য খরচ"
            )
            "রাজনৈতিক দল" -> listOf(
                "সভা ও মিটিং খরচ",
                "পোস্টার, ব্যানার ও লিফলেট",
                "মিছিল ও সমাবেশ",
                "আপ্যায়ন ও চা-নাস্তা",
                "দলীয় কার্যালয় ভাড়া",
                "অন্যান্য খরচ"
            )
            "পূজা কমিটি" -> listOf(
                "মণ্ডপ ও প্যান্ডেল তৈরি",
                "প্রতিমা নির্মাণ ও বায়না",
                "ভোগ ও প্রসাদ বিতরণ",
                "আলোকসজ্জা ও ডেকোরেশন",
                "সাউন্ড ও ঢাকি খরচ",
                "অন্যান্য খরচ"
            )
            "পাড়ার ক্লাব" -> listOf(
                "খেলাধুলার সরঞ্জাম ক্রয়",
                "ক্লাব ঘর রক্ষণাবেক্ষণ",
                "টুর্নামেন্ট আয়োজন ও ট্রফি",
                "বার্ষিক ভোজ ও পিকনিক",
                "আপ্যায়ন",
                "অন্যান্য খরচ"
            )
            "ওয়াজ মাহফিল" -> listOf(
                "প্রধান বক্তা ও ওলামায়ে কেরামের হাদিয়া",
                "প্যান্ডেল ও সামিয়ানা",
                "মাইক ও সাউন্ড সিস্টেম",
                "মেহমানদারি ও খাবার",
                "প্রচার ও পোস্টারিং",
                "অন্যান্য খরচ"
            )
            "বন্ধুদের সমিতি / পিকনিক" -> listOf(
                "বাস / যানবাহন ভাড়া",
                "খাবার ও বাবুর্চি খরচ",
                "স্পট ও পার্ক টিকিট",
                "সাউন্ড ও মিউজিক",
                "টি-শার্ট ও ক্যাপ",
                "অন্যান্য খরচ"
            )
            else -> listOf(
                "আপ্যায়ন",
                "প্রচার ও ব্যানার",
                "রক্ষণাবেক্ষণ",
                "অফিস খরচ",
                "অন্যান্য খরচ"
            )
        }
    }
}
