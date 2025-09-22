package com.autoever.mocar.ui.common.util

fun String.sanitize(): String =
    this.trim().replace(Regex("\\s+"), " ")