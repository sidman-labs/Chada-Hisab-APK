package com.example

import com.example.data.model.BengaliFormatter
import com.example.data.model.FinancialSummary
import com.example.data.model.PaymentStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testBengaliDigitConversion() {
    val bnNum = BengaliFormatter.toBengaliDigits(1234567890L)
    assertEquals("১২৩৪৫৬৭৮৯০", bnNum)
  }

  @Test
  fun testCurrencyFormatting() {
    val formatted = BengaliFormatter.formatCurrency(500.0)
    assertEquals("৳ ৫০০", formatted)
  }

  @Test
  fun testFinancialSummaryCalculations() {
    val summary = FinancialSummary(
      totalMembers = 10,
      totalAssignedFee = 10000.0,
      totalCollectedFee = 7500.0,
      totalExpense = 2000.0,
      totalOtherIncome = 500.0
    )
    assertEquals(8000.0, summary.totalIncome, 0.01)
    assertEquals(6000.0, summary.netBalance, 0.01)
    assertEquals(2500.0, summary.totalDue, 0.01)
  }
}
