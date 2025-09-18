package com.autoever.mocar.ui.common.component.atoms

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BrandUi(val id: String, val name: String, val logoRes: Int)

@Composable
fun BrandChip(
    brand: BrandUi,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) Color(0xFF2A5BFF) else Color(0xFFE5E7EB),
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = brand.logoRes),
                contentDescription = brand.name,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(brand.name, fontSize = 12.sp)
    }
}