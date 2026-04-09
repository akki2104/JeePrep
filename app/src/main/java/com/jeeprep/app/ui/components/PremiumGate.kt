package com.jeeprep.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jeeprep.app.premium.PremiumManager
import com.jeeprep.app.ui.theme.GoldColor

/**
 * Shows a premium lock overlay on a button/card when user isn't premium.
 * When clicked, navigates to the premium screen.
 */
@Composable
fun PremiumLockedButton(
    text: String,
    onClick: () -> Unit,
    onPremiumClick: () -> Unit,
    isPremium: Boolean,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    if (isPremium) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
        ) {
            icon?.invoke()
            if (icon != null) Spacer(Modifier.width(8.dp))
            Text(text)
        }
    } else {
        OutlinedButton(
            onClick = onPremiumClick,
            modifier = modifier,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldColor),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = androidx.compose.ui.graphics.SolidColor(GoldColor.copy(alpha = 0.5f))
            )
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = GoldColor)
            Spacer(Modifier.width(6.dp))
            Text(text, color = GoldColor)
            Spacer(Modifier.width(4.dp))
            Badge(containerColor = GoldColor) { Text("PRO", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color.White) }
        }
    }
}

/**
 * Banner shown at the bottom of sections to upsell premium.
 */
@Composable
fun PremiumBanner(
    onPremiumClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPremiumClick),
        colors = CardDefaults.cardColors(containerColor = GoldColor.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = GoldColor, modifier = Modifier.size(28.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Upgrade to Pro", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Unlimited downloads • AI explanations • ${PremiumManager.PREMIUM_PRICE} once",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Filled.Lock, contentDescription = null, tint = GoldColor, modifier = Modifier.size(16.dp))
        }
    }
}
