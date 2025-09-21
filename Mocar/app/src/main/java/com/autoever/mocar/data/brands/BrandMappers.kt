package com.autoever.mocar.data.brands

import com.autoever.mocar.ui.common.component.atoms.BrandUi

fun BrandDto.toUi() = BrandUi(
    id = id,
    name = name,
    logoUrl = logoUrl,
)