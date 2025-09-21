package com.autoever.mocar.ui.common.util


fun formatKrwPretty(amount: Long): String {
    val eok = amount / 100_000_000
    val man = (amount % 100_000_000) / 10_000
    return when {
        eok > 0L && man > 0L -> "${eok}억 ${String.format("%,d만원", man)}"
        eok > 0L && man == 0L -> "${eok}억"
        else -> String.format("%,d만원", man)
    }
}