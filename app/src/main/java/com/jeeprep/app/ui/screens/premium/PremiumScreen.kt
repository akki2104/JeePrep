package com.jeeprep.app.ui.screens.premium

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jeeprep.app.premium.PremiumManager
import com.jeeprep.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PremiumScreen(
    navController: NavController,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val isPremium by viewModel.isPremium.collectAsState()
    val purchaseState by viewModel.uiState.collectAsState()

    // Confirmation dialog
    if (purchaseState.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissConfirmDialog() },
            icon = { Icon(Icons.Filled.Payment, contentDescription = null, tint = GoldColor, modifier = Modifier.size(32.dp)) },
            title = { Text("Confirm Purchase", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("You are about to purchase JeePrep Pro for ${PremiumManager.PREMIUM_PRICE}.")
                    Spacer(Modifier.height(8.dp))
                    Text("This is a one-time payment. All Pro features will be unlocked permanently on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("JeePrep Pro (Lifetime)", style = MaterialTheme.typography.bodyMedium)
                        Text(PremiumManager.PREMIUM_PRICE, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmPurchase() },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldColor)
                ) {
                    Text("Pay ${PremiumManager.PREMIUM_PRICE}", color = androidx.compose.ui.graphics.Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConfirmDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Processing overlay
    if (purchaseState.isProcessing) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Processing Payment", fontWeight = FontWeight.Bold) },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = GoldColor)
                    Text("Verifying your purchase...")
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("JeePrep Pro") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- DEV TOGGLE (remove before Play Store release) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Developer Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        Text("Toggle premium for testing", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isPremium,
                        onCheckedChange = { checked ->
                            if (checked) viewModel.confirmPurchase()
                            else viewModel.resetPremium()
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = CorrectGreen)
                    )
                }
            }

            if (isPremium) {
                // Already premium
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CorrectGreen.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Verified, contentDescription = null, tint = CorrectGreen, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("You're a Pro!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("All features are unlocked", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                // Premium header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(listOf(GoldColor, GoldDarkColor))
                        )
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("JeePrep Pro", color = androidx.compose.ui.graphics.Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("One-time payment • No subscription", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(PremiumManager.PREMIUM_PRICE, color = androidx.compose.ui.graphics.Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                        Text("Lifetime access", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                }

                // USP Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Why JeePrep?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        UspItem(icon = Icons.Filled.WifiOff, text = "100% Offline — AI runs on YOUR phone")
                        UspItem(icon = Icons.Filled.Payment, text = "One-time ₹50 — No monthly fees ever")
                        UspItem(icon = Icons.Filled.Security, text = "No data leaves your device")
                        UspItem(icon = Icons.Filled.Update, text = "Free updates for life")
                    }
                }

                // Feature list
                Text("Pro Features", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())

                FeatureCard(icon = Icons.Filled.MenuBook, title = "Detailed Explanations", desc = "Step-by-step solution breakdown for every question")
                FeatureCard(icon = Icons.Filled.FlashOn, title = "Tricks & Shortcuts", desc = "Quick solving techniques used by toppers")
                FeatureCard(icon = Icons.Filled.Chat, title = "AI Tutor Chat", desc = "Ask doubts about any question, get instant answers")
                FeatureCard(icon = Icons.Filled.TrendingUp, title = "AI Weakness Analysis", desc = "Personalized study plan based on your mistakes")
                FeatureCard(icon = Icons.Filled.School, title = "PYQ College Info", desc = "Know which IIT/NIT set each question")
                FeatureCard(icon = Icons.Filled.Download, title = "Unlimited Question Downloads", desc = "Free users limited to 10 downloads. Pro gets unlimited extra questions for every topic.")
                FeatureCard(icon = Icons.Filled.Analytics, title = "Advanced Analytics", desc = "Topic-wise trends, time analysis & percentile estimate")

                // CTA
                Button(
                    onClick = { viewModel.showConfirmDialog() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldColor)
                ) {
                    Text("Unlock Pro for ${PremiumManager.PREMIUM_PRICE}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                }

                Text(
                    "Compared to ₹200-500/month for coaching apps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun UspItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun FeatureCard(icon: ImageVector, title: String, desc: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = GoldColor, modifier = Modifier.size(28.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
