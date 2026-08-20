package com.ndfilter.ndfilter

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * This is a helper to generate your Play Store Feature Graphic (1024x500).
 * Open the 'Split' or 'Design' view in Android Studio to see it.
 */
@Composable
fun PlayStoreBanner() {
    Box(
        modifier = Modifier
            .width(1024.dp)
            .height(500.dp)
            .background(Color(0xFF121212)), // Dark background matching your icon
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Your App Icon
            Box(modifier = Modifier.size(200.dp)) {
                // Background of icon
                Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                // Foreground of icon
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_2_foreground),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Name
            Text(
                text = "ND FILTER",
                color = Color(0xFF4CAF50), // Your green accent
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 8.sp
            )
            
            Text(
                text = "Simple. Precise. Photography.",
                color = Color.Gray,
                fontSize = 24.sp
            )
        }
    }
}

@Preview(widthDp = 1024, heightDp = 500)
@Composable
fun PreviewPlayStoreBanner() {
    PlayStoreBanner()
}
