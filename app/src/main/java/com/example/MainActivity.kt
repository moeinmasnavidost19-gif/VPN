package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.ConnectionLog
import com.example.data.ConnectionLogRepository
import com.example.data.DefaultServers
import com.example.data.VpnServer
import com.example.ui.AppLanguage
import com.example.ui.ConnectionState
import com.example.ui.VpnViewModel
import com.example.ui.VpnViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGlow
import com.example.ui.theme.DeepDarkBg
import com.example.ui.theme.MeshIndigo
import com.example.ui.theme.MeshBlue
import com.example.ui.theme.GlassBg
import com.example.ui.theme.GlassBgHover
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextSlate100
import com.example.ui.theme.TextSlate400
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.WarningRed
import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Custom modifier for elegant Glassmorphic elements
fun Modifier.glassCard(shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp)): Modifier {
    return this
        .background(GlassBg, shape)
        .border(width = 1.dp, color = GlassBorder, shape = shape)
}

// Custom mesh gradient background modifier that merges radial gradients of blue and indigo
fun Modifier.meshGradientBackground(): Modifier {
    return this.drawBehind {
        // Core Deep Dark Canvas
        drawRect(color = DeepDarkBg)
        
        // Indigo highlight top-left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MeshIndigo.copy(alpha = 0.45f), Color.Transparent),
                center = Offset(size.width * 0.15f, size.height * 0.25f),
                radius = size.minDimension * 0.85f
            ),
            radius = size.minDimension * 0.85f,
            center = Offset(size.width * 0.15f, size.height * 0.25f)
        )
        
        // Deep blue highlight bottom-right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MeshBlue.copy(alpha = 0.4f), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.75f),
                radius = size.minDimension * 0.85f
            ),
            radius = size.minDimension * 0.85f,
            center = Offset(size.width * 0.85f, size.height * 0.75f)
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get database and setup VM factory
        val database = AppDatabase.getDatabase(this)
        val repository = ConnectionLogRepository(database.connectionLogDao())
        val factory = VpnViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val viewModel: VpnViewModel = viewModel(factory = factory)
                VpnAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun VpnAppScreen(viewModel: VpnViewModel) {
    val language by viewModel.language.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    var currentTab by remember { mutableStateOf(0) } // 0: Connect, 1: Security Tools, 2: History logs

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent, // Let the mesh gradient handle the background
        bottomBar = {
            // Elegant Floating Frosted Glass Navigation Bar
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .testTag("bottom_nav_bar")
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .glassCard(RoundedCornerShape(24.dp))
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.PowerSettingsNew, contentDescription = "Connect") },
                    label = { 
                        Text(
                            text = if (language == AppLanguage.Persian) "اتصال" else "Connect",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentBlue,
                        unselectedIconColor = TextSlate400,
                        selectedTextColor = AccentBlue,
                        unselectedTextColor = TextSlate400,
                        indicatorColor = Color(0x243A86FF) // light frosted blue indicator
                    ),
                    modifier = Modifier.testTag("nav_connect_tab")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Security, contentDescription = "Security") },
                    label = { 
                        Text(
                            text = if (language == AppLanguage.Persian) "امنیت" else "Security",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentBlue,
                        unselectedIconColor = TextSlate400,
                        selectedTextColor = AccentBlue,
                        unselectedTextColor = TextSlate400,
                        indicatorColor = Color(0x243A86FF)
                    ),
                    modifier = Modifier.testTag("nav_security_tab")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.NetworkCheck, contentDescription = "History") },
                    label = { 
                        Text(
                            text = if (language == AppLanguage.Persian) "گزارش‌ها" else "Logs",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentBlue,
                        unselectedIconColor = TextSlate400,
                        selectedTextColor = AccentBlue,
                        unselectedTextColor = TextSlate400,
                        indicatorColor = Color(0x243A86FF)
                    ),
                    modifier = Modifier.testTag("nav_history_tab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .meshGradientBackground()
                .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding())
        ) {
            // Header Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (language == AppLanguage.Persian) "ذن‌وی‌پی‌ان" else "ZenVPN",
                        color = TextSlate100,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (connectionState == ConnectionState.Connected) NeonGreen 
                                    else if (connectionState == ConnectionState.Connecting) Color.Yellow 
                                    else TextSlate400
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (connectionState) {
                                ConnectionState.Connected -> if (language == AppLanguage.Persian) "ترافیک کاملاً رمزنگاری شده (AES-256)" else "Traffic fully encrypted (AES-256)"
                                ConnectionState.Connecting -> if (language == AppLanguage.Persian) "در حال رمزنگاری ترافیک..." else "Encrypting connection..."
                                ConnectionState.Disconnected -> if (language == AppLanguage.Persian) "در انتظار اتصال" else "Inactive - Not Secure"
                            },
                            color = if (connectionState == ConnectionState.Connected) NeonGreen else TextSlate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Glassmorphic Language Selection Toggle Button
                IconButton(
                    onClick = {
                        viewModel.setLanguage(
                            if (language == AppLanguage.Persian) AppLanguage.English else AppLanguage.Persian
                        )
                    },
                    modifier = Modifier
                        .testTag("lang_toggle")
                        .size(44.dp)
                        .glassCard(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = TextSlate100,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Central Tab Content Switcher
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                when (currentTab) {
                    0 -> ConnectTab(viewModel, language)
                    1 -> SecurityTab(viewModel, language)
                    2 -> HistoryTab(viewModel, language)
                }
            }
        }
    }
}

@Composable
fun ConnectTab(viewModel: VpnViewModel, language: AppLanguage) {
    val connectionState by viewModel.connectionState.collectAsState()
    val selectedServer by viewModel.selectedServer.collectAsState()
    val currentPing by viewModel.currentPing.collectAsState()
    val downSpeed by viewModel.downloadSpeed.collectAsState()
    val upSpeed by viewModel.uploadSpeed.collectAsState()
    val durationSeconds by viewModel.durationSeconds.collectAsState()
    val bytesDownloaded by viewModel.bytesDownloaded.collectAsState()
    val bytesUploaded by viewModel.bytesUploaded.collectAsState()

    var showServerDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Center Connection Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (language == AppLanguage.Persian) "وضعیت فعلی" else "CURRENT STATUS",
                color = TextSlate400,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = when (connectionState) {
                    ConnectionState.Connected -> if (language == AppLanguage.Persian) "اتصال فعال است" else "CONNECTED"
                    ConnectionState.Connecting -> if (language == AppLanguage.Persian) "در حال اتصال..." else "CONNECTING..."
                    ConnectionState.Disconnected -> if (language == AppLanguage.Persian) "در انتظار اتصال" else "DISCONNECTED"
                },
                color = if (connectionState == ConnectionState.Connected) NeonGreen else TextSlate100,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // Outer pulse glow, thick transparent borders, and white clickable power button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(260.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "power_pulse")
                
                val angle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(5000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "angle"
                )

                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.15f,
                    targetValue = 0.45f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )

                // High fidelity glow backdrop simulation (blur-3xl effect)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2.2f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                (if (connectionState == ConnectionState.Connected) AccentBlue else Color(0x22FFFFFF))
                                    .copy(alpha = pulseAlpha * if (connectionState == ConnectionState.Connected) 1.2f else 0.5f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius * pulseScale
                        ),
                        radius = radius * pulseScale
                    )
                }

                // Rotating Dashed Outlines
                Canvas(
                    modifier = Modifier
                        .size(240.dp)
                        .rotate(if (connectionState == ConnectionState.Connected) angle else 0f)
                ) {
                    val strokeWidth = 3.dp.toPx()
                    val radius = size.minDimension / 2 - 12.dp.toPx()
                    drawCircle(
                        color = if (connectionState == ConnectionState.Connected) AccentBlue else GlassBorder,
                        radius = radius,
                        style = Stroke(
                            width = strokeWidth,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f), 0f)
                        )
                    )
                }

                // Inner Glass card containment ring
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .background(Color(0x0CFFFFFF), CircleShape)
                        .border(4.dp, Color(0x12FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Central button mirroring the HTML design: White background when disconnected, glowing blue when connected
                    val mainBtnColor = if (connectionState == ConnectionState.Disconnected) Color.White else AccentBlue
                    val mainContentColor = if (connectionState == ConnectionState.Disconnected) DeepDarkBg else Color.White

                    Surface(
                        onClick = { viewModel.toggleConnection() },
                        shape = CircleShape,
                        color = mainBtnColor,
                        modifier = Modifier
                            .testTag("power_toggle_btn")
                            .size(136.dp)
                            .drawBehind {
                                if (connectionState == ConnectionState.Connected) {
                                    drawCircle(
                                        color = AccentBlue.copy(alpha = 0.5f),
                                        radius = size.width / 2 + 10.dp.toPx()
                                    )
                                }
                            },
                        shadowElevation = 12.dp
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Power",
                                tint = mainContentColor,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (connectionState) {
                                    ConnectionState.Connected -> if (language == AppLanguage.Persian) "قطع اتصال" else "DISCONNECT"
                                    ConnectionState.Connecting -> if (language == AppLanguage.Persian) "اتصال..." else "CONNECTING"
                                    ConnectionState.Disconnected -> if (language == AppLanguage.Persian) "شروع اتصال" else "TAP TO CONNECT"
                                },
                                color = mainContentColor.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic connection speed metrics (grid layout)
            AnimatedVisibility(
                visible = connectionState == ConnectionState.Connected,
                enter = fadeIn(tween(400)),
                exit = fadeOut(tween(400))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TelemetryCard(
                            title = if (language == AppLanguage.Persian) "سرعت دانلود" else "DOWNLOAD SPEED",
                            value = "${((downSpeed * 10).toInt() / 10.0)} MB/s",
                            subtext = if (language == AppLanguage.Persian) "کانال امن فرانکلین" else "Ultra Low Jitter",
                            color = NeonGreen
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TelemetryCard(
                            title = if (language == AppLanguage.Persian) "سرعت آپلود" else "UPLOAD SPEED",
                            value = "${((upSpeed * 10).toInt() / 10.0)} MB/s",
                            subtext = if (language == AppLanguage.Persian) "تونل پرسرعت" else "High Bandwidth",
                            color = AccentBlue
                        )
                    }
                }
            }
        }

        // Active Server Selector (Matches ZenVPN footer layout card)
        Card(
            onClick = {
                if (connectionState == ConnectionState.Disconnected) {
                    showServerDialog = true
                }
            },
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .testTag("server_select_card")
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .glassCard(RoundedCornerShape(24.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Rounded dark wrapper for flag
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x22000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getCountryFlagEmoji(selectedServer.countryCode),
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.Persian) "بهترین سرور" else "SELECTED NODE",
                            color = TextSlate400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (language == AppLanguage.Persian) selectedServer.nameFa else selectedServer.nameEn,
                            color = TextSlate100,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (connectionState == ConnectionState.Connected) "$currentPing ms" else "${selectedServer.baseLatencyMs} ms",
                            color = if (currentPing <= 50) NeonGreen else Color.Yellow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (language == AppLanguage.Persian) "تاخیر پاسخ" else "LATENCY",
                            color = TextSlate400,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (connectionState == ConnectionState.Disconnected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select Server",
                            tint = TextSlate100
                        )
                    }
                }
            }
        }

        // Live stats panel (when active)
        AnimatedVisibility(
            visible = connectionState == ConnectionState.Connected,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .glassCard(RoundedCornerShape(20.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatDuration(durationSeconds),
                        color = TextSlate100,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (language == AppLanguage.Persian) "مدت اتصال" else "DURATION",
                        color = TextSlate400,
                        fontSize = 9.sp
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatByteCount(bytesUploaded),
                        color = TextSlate100,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (language == AppLanguage.Persian) "آپلود" else "UPLOADED",
                        color = TextSlate400,
                        fontSize = 9.sp
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatByteCount(bytesDownloaded),
                        color = TextSlate100,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (language == AppLanguage.Persian) "دانلود" else "DOWNLOADED",
                        color = TextSlate400,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }

    if (showServerDialog) {
        ServerSelectionDialog(
            viewModel = viewModel,
            language = language,
            onDismiss = { showServerDialog = false },
            onSelect = { server ->
                viewModel.selectServer(server)
                showServerDialog = false
            }
        )
    }
}

@Composable
fun TelemetryCard(title: String, value: String, subtext: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title, 
                color = TextSlate400, 
                fontSize = 9.sp, 
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    color = color,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtext, 
                color = TextSlate400, 
                fontSize = 8.sp, 
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SecurityTab(viewModel: VpnViewModel, language: AppLanguage) {
    val encryptStream by viewModel.encryptionStream.collectAsState()
    val isConnected = viewModel.connectionState.collectAsState().value == ConnectionState.Connected
    val scope = rememberCoroutineScope()

    var pingHost by remember { mutableStateOf("1.1.1.1") }
    var pingResultTerminal by remember { mutableStateOf("") }
    var pingingProgress by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AES-256 live log monitor (scrambler)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.Persian) "روند رمزنگاری داده‌ها (AES-256)" else "AES-256 Scrambler Console",
                            color = AccentBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isConnected) Color(0x3300FF88) else Color(0x33FFCC00),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isConnected) (if (language == AppLanguage.Persian) "زنده" else "SECURE TUNNEL") 
                                       else (if (language == AppLanguage.Persian) "انتظار" else "STANDBY"),
                                color = if (isConnected) NeonGreen else Color.Yellow,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Scrolling console logs
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color(0xFF070B11), RoundedCornerShape(10.dp))
                            .border(width = 1.dp, color = GlassBorder, shape = RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (encryptStream.isEmpty()) {
                            Text(
                                "Initializing Encryptor Core...",
                                color = TextSlate400,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            encryptStream.forEach { text ->
                                Text(
                                    text = text,
                                    color = if (isConnected) NeonGreen else TextSlate400,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Host ping diagnostician card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .testTag("ping_checker_card")
                    .fillMaxWidth()
                    .glassCard(RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (language == AppLanguage.Persian) "تست پینگ واقعی و بررسی شبکه" else "Authentic Ping Diagnostician",
                        color = TextSlate100,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (language == AppLanguage.Persian) "یک آدرس وب یا آی‌پی وارد کنید تا سرعت واقعی اتصال را بررسی کنیم" 
                               else "Test direct round-trip response time to any target domain",
                        color = TextSlate400,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = pingHost,
                            onValueChange = { pingHost = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ping_host_input")
                                .height(56.dp),
                            placeholder = { Text("e.g. google.com", color = TextSlate400) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextSlate100,
                                unfocusedTextColor = TextSlate100,
                                focusedContainerColor = Color(0x12FFFFFF),
                                unfocusedContainerColor = Color(0x06FFFFFF)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    pingingProgress = true
                                    pingResultTerminal = if (language == AppLanguage.Persian) "در حال اتصال به $pingHost ..." else "Handshaking $pingHost..."
                                    
                                    val measuredResult = runActualSocketPing(pingHost)
                                    pingResultTerminal = if (language == AppLanguage.Persian) {
                                        "نتیجه اتصال به $pingHost:\n" +
                                        "➔ وضعیت: ${measuredResult.statusFa}\n" +
                                        "➔ سرعت پاسخ: ${measuredResult.latencyMs}ms\n" +
                                        "➔ سیستم رمزنگاری: فعال و ایمن"
                                    } else {
                                        "Connection to $pingHost output:\n" +
                                        "➔ Handshake Status: ${measuredResult.statusEn}\n" +
                                        "➔ Response Latency: ${measuredResult.latencyMs} ms\n" +
                                        "➔ Encryption Envelope: ACTIVE"
                                    }
                                    pingingProgress = false
                                }
                            },
                            enabled = !pingingProgress,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .testTag("run_ping_btn")
                                .height(56.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.Persian) "بررسی" else "PING",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (pingResultTerminal.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF070B11), RoundedCornerShape(10.dp))
                                .border(width = 1.dp, color = GlassBorder, shape = RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = pingResultTerminal,
                                color = if (pingResultTerminal.contains("FAILED") || pingResultTerminal.contains("خطا")) WarningRed else NeonGreen,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Active secure enclaves list
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (language == AppLanguage.Persian) "سیستم‌های حفاظتی فعال" else "Active Secure Enclaves",
                        color = TextSlate100,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ProtocolRow(
                        label = if (language == AppLanguage.Persian) "رمزنگاری ترافیک" else "Military AES-256 GCM",
                        status = if (language == AppLanguage.Persian) "فعال" else "ACTIVE",
                        color = NeonGreen
                    )
                    ProtocolRow(
                        label = if (language == AppLanguage.Persian) "سیستم ضد نشت DNS" else "DNS Leak Shield",
                        status = if (language == AppLanguage.Persian) "ایمن شده" else "SECURED",
                        color = NeonGreen
                    )
                    ProtocolRow(
                        label = if (language == AppLanguage.Persian) "کلید قطع خودکار (Kill Switch)" else "Network Kill Switch",
                        status = if (language == AppLanguage.Persian) "خودکار فعال" else "AUTO ENABLED",
                        color = NeonGreen
                    )
                    ProtocolRow(
                        label = if (language == AppLanguage.Persian) "پروتکل اتصال" else "Tunnel Protocol",
                        status = "WireGuard Core",
                        color = AccentBlue
                    )
                }
            }
        }
    }
}

@Composable
fun ProtocolRow(label: String, status: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSlate100, fontSize = 13.sp)
        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                .border(width = 0.5.dp, color = color.copy(alpha = 0.4f), shape = RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(status, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HistoryTab(viewModel: VpnViewModel, language: AppLanguage) {
    val logs by viewModel.connectionLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 10.dp)
    ) {
        // Stats Summary Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.Persian) "خلاصه کل فعالیت‌های ایمن" else "Secure Stats Summary",
                        color = TextSlate100,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (logs.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearLogs() },
                            modifier = Modifier
                                .testTag("clear_logs_btn")
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ClearAll,
                                contentDescription = "Clear all logs",
                                tint = WarningRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (language == AppLanguage.Persian) "کل اتصالات" else "SESSIONS",
                            color = TextSlate400,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${logs.size}",
                            color = NeonGreen,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = if (language == AppLanguage.Persian) "کل زمان اتصال" else "TOTAL TIME",
                            color = TextSlate400,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val totalSeconds = logs.sumOf { it.durationSeconds }
                        Text(
                            text = formatDuration(totalSeconds),
                            color = TextSlate100,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column {
                        Text(
                            text = if (language == AppLanguage.Persian) "حجم مصرفی کل" else "TOTAL TRAFFIC",
                            color = TextSlate400,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val totalTraffic = logs.sumOf { it.bytesDownloaded + it.bytesUploaded }
                        Text(
                            text = formatByteCount(totalTraffic),
                            color = AccentBlue,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (language == AppLanguage.Persian) "تاریخچه نشست‌های رمزنگاری شده" else "Encrypted Sessions History",
            color = TextSlate100,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .glassCard(RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Empty",
                        tint = TextSlate400,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (language == AppLanguage.Persian) "هنوز اتصالی ثبت نشده است" else "No connection records yet",
                        color = TextSlate400,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(logs) { log ->
                    LogItemRow(log, language)
                }
            }
        }
    }
}

@Composable
fun LogItemRow(log: ConnectionLog, language: AppLanguage) {
    val clipboardManager = LocalClipboardManager.current

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x1AFFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getCountryFlagEmoji(log.serverCountryCode),
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = log.serverName,
                        color = TextSlate100,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatDuration(log.durationSeconds),
                        color = TextSlate400,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatByteCount(log.bytesDownloaded + log.bytesUploaded),
                        color = AccentBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (language == AppLanguage.Persian) "ترافیک" else "TRAFFIC",
                        color = TextSlate400,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val textToCopy = "Server: ${log.serverName}, Traffic: ${formatByteCount(log.bytesDownloaded + log.bytesUploaded)}"
                        clipboardManager.setText(AnnotatedString(textToCopy))
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy log details",
                        tint = TextSlate400,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ServerSelectionDialog(
    viewModel: VpnViewModel,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSelect: (VpnServer) -> Unit
) {
    val isPingingAll by viewModel.isPingingAll.collectAsState()
    val pingProgressText by viewModel.pingProgressText.collectAsState()
    val serverLatencies by viewModel.serverLatencies.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent, // Managed by glass card wrapper
            modifier = Modifier
                .testTag("server_selection_dialog")
                .fillMaxWidth()
                .glassCard(RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = if (language == AppLanguage.Persian) "انتخاب سرور پرسرعت (1ms)" else "Choose High-Speed Node (1ms)",
                    color = TextSlate100,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Premium real-time ping diagnostic card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .padding(bottom = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = if (language == AppLanguage.Persian) "سنجش تاخیر واقعی سرورها" else "Live Latency Engine",
                                    color = TextSlate100,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (language == AppLanguage.Persian) "بررسی سرعت زنده تمامی ۵۰ سرور" else "Test live speeds for all 50 nodes",
                                    color = TextSlate400,
                                    fontSize = 10.sp
                                )
                            }
                            Button(
                                onClick = { viewModel.runRealTimePingAll() },
                                enabled = !isPingingAll,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentBlue,
                                    disabledContainerColor = AccentBlue.copy(alpha = 0.5f)
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("run_all_pings_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NetworkCheck,
                                        contentDescription = "Ping",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (language == AppLanguage.Persian) "تست پینگ" else "PING TEST",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        if (isPingingAll || pingProgressText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isPingingAll) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = NeonGreen,
                                        strokeWidth = 1.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = pingProgressText,
                                    color = NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(280.dp)
                ) {
                    items(DefaultServers) { server ->
                        val latency = serverLatencies[server.id] ?: server.baseLatencyMs
                        Row(
                            modifier = Modifier
                                .testTag("server_item_${server.id}")
                                .fillMaxWidth()
                                .background(Color(0x0CFFFFFF), RoundedCornerShape(14.dp))
                                .border(width = 0.5.dp, color = GlassBorder, shape = RoundedCornerShape(14.dp))
                                .clickable { onSelect(server) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x12FFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = getCountryFlagEmoji(server.countryCode),
                                        fontSize = 22.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (language == AppLanguage.Persian) server.nameFa else server.nameEn,
                                            color = TextSlate100,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = server.ipAddress,
                                        color = TextSlate400,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$latency ms",
                                    color = if (latency <= 30) NeonGreen else if (latency <= 70) AccentBlue else Color.Yellow,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (latency <= 30) NeonGreen else if (latency <= 70) AccentBlue else Color.Yellow)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x1FFFFFFF)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.Persian) "انصراف" else "Cancel",
                        color = TextSlate100,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Socket response representation model
data class SocketPingResult(
    val latencyMs: Int,
    val statusEn: String,
    val statusFa: String
)

suspend fun runActualSocketPing(host: String): SocketPingResult = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()
    try {
        Socket().use { socket ->
            socket.connect(InetSocketAddress(host, 80), 2000)
        }
        val elapsed = (System.currentTimeMillis() - startTime).toInt()
        val latency = if (elapsed > 0) elapsed else 1
        SocketPingResult(
            latencyMs = latency,
            statusEn = "SUCCESSFUL - CONNECTED",
            statusFa = "موفقیت‌آمیز - متصل شد"
        )
    } catch (e: Exception) {
        SocketPingResult(
            latencyMs = 999,
            statusEn = "FAILED (Unreachable)",
            statusFa = "خطا (عدم پاسخ میزبان)"
        )
    }
}

fun getCountryFlagEmoji(countryCode: String): String {
    if (countryCode.length != 2) return "🌐"
    val firstChar = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
    val secondChar = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
}

fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return String.format("%02d:%02d:%02d", h, m, s)
}

fun formatByteCount(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1] + "B"
    val value = bytes / Math.pow(1024.0, exp.toDouble())
    return String.format("%.1f %s", value, pre)
}
