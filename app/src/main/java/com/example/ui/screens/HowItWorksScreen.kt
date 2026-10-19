package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BlockedRose
import com.example.ui.theme.MobileOrange
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.ShieldEmerald
import com.example.ui.theme.WifiBlue

@Composable
fun HowItWorksScreen(
    isBangla: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("how_it_works_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Privacy Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ShieldEmerald.copy(alpha = 0.1f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(ShieldEmerald.copy(alpha = 0.4f))
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(ShieldEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = ShieldEmerald,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isBangla) "১০০% লোকাল ও নিরাপদ" else "100% Local & Privacy First",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ShieldEmerald
                        )
                        Text(
                            text = if (isBangla)
                                "ডিএফ নেট কন্ট্রোল কোনো সার্ভারে ডেটা পাঠায় না। কোনো লগ সংরক্ষণ বা বিক্রি করা হয় না। কোনো রুট (Root) পারমিশন লাগে না।"
                            else
                                "DF Net Control never uploads or sells traffic. Zero external servers, zero telemetry logs. Works completely without root access.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Section 1: How Local Filtering Works
        item {
            ExplorationCard(
                icon = Icons.Default.Router,
                iconColor = PrimaryCyan,
                title = if (isBangla) "১. লোকাল ভিপিএন প্যাকেট ফিল্টারিং" else "1. Local VPN Packet Filtering Architecture",
                body = if (isBangla)
                    "অ্যান্ড্রয়েডের স্ট্যান্ডার্ড VpnService ব্যবহার করে ডিএফ নেট কন্ট্রোল একটি স্থানীয় ভার্চুয়াল নেটওয়ার্ক ইন্টারফেস (TUN) তৈরি করে। আপনি যখন কোনো অ্যাপকে অবরুদ্ধ করেন, অ্যান্ড্রয়েডের লিনাক্স কার্নেল সেই অ্যাপের আইপি প্যাকেটগুলোকে এই লোকাল ইন্টারফেসে পাঠায় যেখানে সেগুলো তাৎক্ষণিকভাবে ড্রপ (Sinkhole) হয়। যে অ্যাপগুলো অনুমোদিত, সেগুলোর ট্রাফিক সরাসরি ওয়াই-ফাই বা মোবাইলের স্বাভাবিক গতিতেই চলে।"
                else
                    "DF Net Control uses Android's native VpnService to create a local on-device virtual TUN interface. When an app is marked blocked, Android's kernel routes only that app's IP packets into the local TUN where they are discarded. Allowed apps bypass the TUN entirely and communicate directly with Wi-Fi or cellular at 100% full hardware speed."
            )
        }

        // Section 2: Wi-Fi vs Mobile Independent Rules
        item {
            ExplorationCard(
                icon = Icons.Default.Security,
                iconColor = WifiBlue,
                title = if (isBangla) "২. ওয়াই-ফাই ও মোবাইল ডেটা পৃথক নিয়ন্ত্রণ" else "2. Independent Wi-Fi & Mobile Controls",
                body = if (isBangla)
                    "প্রতিটি অ্যাপের জন্য রয়েছে দুটি স্বাধীন সুইচ: ওয়াই-ফাই ও মোবাইল ডেটা। একটি ব্যাকগ্রাউন্ড নেটওয়ার্ক কলব্যাক সার্বক্ষণিক সক্রিয় সংযোগ (Wi-Fi বনাম Cellular) পর্যবেক্ষণ করে। আপনি যখন ওয়াই-ফাই থেকে মোবাইল ডেটায় যান বা এর বিপরীত হয়, ফায়ারওয়াল স্বয়ংক্রিয়ভাবে সেই মাধ্যমের জন্য সংরক্ষিত নিয়মাবলী প্রয়োগ করে।"
                else
                    "Each app has two independent toggle switches: Wi-Fi and Mobile Data. A dynamic Android NetworkCallback tracks network transitions. As soon as you switch from Wi-Fi to 4G/5G mobile data, the firewall seamlessly swaps to your mobile data rule profile without interrupting allowed connections."
            )
        }

        // Section 3: Four Test Scenarios Guide (Prompt Requirement)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(PrimaryCyan.copy(alpha = 0.3f))
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = PrimaryCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (isBangla) "রিয়েল ফোনে ৪টি টেস্ট কেস যাচাইকরণ" else "Testing 4 Scenarios on a Real Phone",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isBangla)
                            "একটি অ্যাপ (যেমন Chrome বা YouTube) বেছে নিয়ে ফায়ারওয়াল চালু করে নিচের ৪টি অবস্থা পরীক্ষা করুন:"
                        else
                            "Pick a test app (such as Chrome or YouTube), start the firewall, and verify these 4 test cases:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TestCaseItem(
                        caseNumber = "Case 1",
                        wifiStatus = if (isBangla) "ওয়াই-ফাই: অনুমোদিত" else "Wi-Fi: Allowed",
                        mobileStatus = if (isBangla) "মোবাইল: অনুমোদিত" else "Mobile: Allowed",
                        expected = if (isBangla) "উভয় নেটওয়ার্কে অ্যাপটি স্বাভাবিকভাবে ইন্টারনেট পাবে।" else "App connects freely on both Wi-Fi and Mobile Data.",
                        isAllowed = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TestCaseItem(
                        caseNumber = "Case 2",
                        wifiStatus = if (isBangla) "ওয়াই-ফাই: অবরুদ্ধ" else "Wi-Fi: Blocked",
                        mobileStatus = if (isBangla) "মোবাইল: অনুমোদিত" else "Mobile: Allowed",
                        expected = if (isBangla) "ওয়াই-ফাইতে নেট কানেকশন ফেইল করবে, কিন্তু মোবাইল ডেটা অন করলে কানেক্ট হবে।" else "App cannot load when on Wi-Fi, but works immediately when switched to Mobile Data.",
                        isAllowed = false
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TestCaseItem(
                        caseNumber = "Case 3",
                        wifiStatus = if (isBangla) "ওয়াই-ফাই: অনুমোদিত" else "Wi-Fi: Allowed",
                        mobileStatus = if (isBangla) "মোবাইল: অবরুদ্ধ" else "Mobile: Blocked",
                        expected = if (isBangla) "ওয়াই-ফাইতে কানেক্ট হবে, কিন্তু মোবাইল ডেটা অন থাকলে নেট পাবে না।" else "App works when connected to Wi-Fi, but fails to load on Mobile Data.",
                        isAllowed = false
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TestCaseItem(
                        caseNumber = "Case 4",
                        wifiStatus = if (isBangla) "ওয়াই-ফাই: অবরুদ্ধ" else "Wi-Fi: Blocked",
                        mobileStatus = if (isBangla) "মোবাইল: অবরুদ্ধ" else "Mobile: Blocked",
                        expected = if (isBangla) "অ্যাপটির সব সংযোগ সম্পূর্ণরূপে বিচ্ছিন্ন থাকবে।" else "App has zero internet connectivity regardless of network.",
                        isAllowed = false
                    )
                }
            }
        }

        // Section 4: Android Limitations
        item {
            ExplorationCard(
                icon = Icons.Default.WarningAmber,
                iconColor = MobileOrange,
                title = if (isBangla) "অ্যান্ড্রয়েড সিস্টেম সীমাবদ্ধতাসমূহ" else "Android OS Constraints & Limitations",
                body = if (isBangla)
                    "• একটি মাত্র ভিপিএন: অ্যান্ড্রয়েডে একসাথে কেবল একটি ভিপিএন চালু থাকতে পারে। অন্য কোনো ভিপিএন চালু করলে এটি পজ হবে।\n• কার্নেল টেথারিং: ফোন যখন হটস্পট শেয়ার করে, টেথার্ড ডিভাইসের প্যাকেট সরাসরি কার্নেল স্তর দিয়ে যায়।\n• সিস্টেম অ্যাপস: ডিভাইসের কিছু গোপনীয় কোর সিস্টেম সার্ভিস (যেমন VoLTE কলিং) ভিপিএন বাইপাস করার জন্য প্রস্তুত থাকে।"
                else
                    "• Single Active VPN: Android enforces that only ONE VpnService can be active at a time. Activating another VPN app will terminate DF Net Control.\n• Hotspot / Tethering: Packets forwarded from other tethered devices pass through kernel NAT and may bypass userspace VPN routing.\n• Core Carrier Services: Certain proprietary carrier packages (VoLTE, IMS emergency) are hardcoded in OEM firmware to bypass non-system VPNs."
            )
        }

        // Section 5: Google Play Policy & QUERY_ALL_PACKAGES
        item {
            ExplorationCard(
                icon = Icons.Default.Policy,
                iconColor = ShieldEmerald,
                title = if (isBangla) "গুগল প্লে পলিসি ও অ্যাপ পারমিশন" else "Google Play Policy & QUERY_ALL_PACKAGES",
                body = if (isBangla)
                    "গুগল প্লে স্টোরের পলিসি অনুযায়ী, ফায়ারওয়াল (Firewall) এবং ডিভাইস সিকিউরিটি অ্যাপ্লিকেশনের মূল কাজ সম্পাদনের জন্য QUERY_ALL_PACKAGES ব্যবহারের সুস্পষ্ট অনুমতি রয়েছে। আপনি কোন কোন অ্যাপের ট্রাফিক নিয়ন্ত্রণ করবেন তা জানার জন্যই এই পারমিশনটি অত্যাবশ্যক।"
                else
                    "Under Google Play's Package Visibility policy, Device Management and Firewall applications are explicitly classified as valid exceptions permitted to declare QUERY_ALL_PACKAGES. The permission is strictly used to display installed applications so you can configure their network rules."
            )
        }
    }
}

@Composable
private fun ExplorationCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    body: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TestCaseItem(
    caseNumber: String,
    wifiStatus: String,
    mobileStatus: String,
    expected: String,
    isAllowed: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAllowed) ShieldEmerald.copy(alpha = 0.3f) else BlockedRose.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = caseNumber,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = wifiStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = mobileStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = expected,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
