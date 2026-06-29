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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.VpnService
import android.content.Context
import android.content.Intent
import android.app.Activity
import com.example.data.AppDatabase
import com.example.data.ConnectionLog
import com.example.data.ConnectionLogRepository
import com.example.data.DefaultServers
import com.example.data.VpnServer
import com.example.ui.AppLanguage
import com.example.ui.ConnectionState
import com.example.ui.VpnProtocol
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val language by viewModel.language.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val selectedServer by viewModel.selectedServer.collectAsState()
    val selectedProtocol by viewModel.protocol.collectAsState()
    var currentTab by remember { mutableStateOf(0) } // 0: Connect, 1: Security Tools, 2: History logs

    val vpnPrepareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Permission granted, start VPN service and toggle VM connection
            val intent = Intent(context, MyVpnService::class.java).apply {
                action = MyVpnService.ACTION_CONNECT
                putExtra(MyVpnService.EXTRA_SERVER_NAME, selectedServer.nameEn)
                putExtra(MyVpnService.EXTRA_PROTOCOL, selectedProtocol.name)
            }
            context.startService(intent)
            viewModel.toggleConnection()
        }
    }

    val handleVpnToggle = {
        if (connectionState == ConnectionState.Disconnected) {
            val myUid = android.os.Process.myUid()
            val realPackageName = context.packageManager.getPackagesForUid(myUid)?.firstOrNull() ?: context.packageName
            val wrappedContext = object : android.content.ContextWrapper(context) {
                override fun getPackageName(): String {
                    return realPackageName
                }
            }
            val vpnIntent = VpnService.prepare(wrappedContext)
            if (vpnIntent != null) {
                vpnPrepareLauncher.launch(vpnIntent)
            } else {
                // Already have permission, start VPN service and toggle VM connection
                val intent = Intent(context, MyVpnService::class.java).apply {
                    action = MyVpnService.ACTION_CONNECT
                    putExtra(MyVpnService.EXTRA_SERVER_NAME, selectedServer.nameEn)
                    putExtra(MyVpnService.EXTRA_PROTOCOL, selectedProtocol.name)
                }
                context.startService(intent)
                viewModel.toggleConnection()
            }
        } else {
            // Stop VPN service and toggle VM connection
            val intent = Intent(context, MyVpnService::class.java).apply {
                action = MyVpnService.ACTION_DISCONNECT
            }
            context.startService(intent)
            viewModel.toggleConnection()
        }
    }

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    if (!isLoggedIn) {
        AuthScreen(viewModel = viewModel)
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent, // Let the mesh gradient handle the background
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Elegant Floating Frosted Glass Navigation Bar
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .testTag("bottom_nav_bar")
                        .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 8.dp)
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

                Text(
                    text = "Made by Moein • ©2026 All Rights Reserved",
                    color = TextSlate400.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .testTag("moein_copyright_footer")
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
                        text = "poor",
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

                // Glassmorphic Language Selection and Profile Section
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    val userName by viewModel.currentUserName.collectAsState()
                    val userEmail by viewModel.currentUserEmail.collectAsState()
                    var showProfileDialog by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x14FFFFFF))
                            .border(1.dp, Color(0x33FFFFFF), CircleShape)
                            .clickable { showProfileDialog = true }
                            .testTag("user_avatar_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (userName ?: "M").take(1).uppercase(),
                            color = AccentBlue,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (showProfileDialog) {
                        AlertDialog(
                            onDismissRequest = { showProfileDialog = false },
                            title = {
                                Text(
                                    text = if (language == AppLanguage.Persian) "حساب کاربری" else "User Account",
                                    color = TextSlate100,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            text = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x243A86FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (userName ?: "M").take(1).uppercase(),
                                            color = AccentBlue,
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = userName ?: "User",
                                        color = TextSlate100,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = userEmail ?: "",
                                        color = TextSlate400,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showProfileDialog = false
                                        viewModel.logout()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WarningRed),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "Logout",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (language == AppLanguage.Persian) "خروج از حساب" else "Log Out",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            containerColor = DeepDarkBg,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                                .padding(4.dp)
                        )
                    }
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
                    0 -> ConnectTab(viewModel, language, handleVpnToggle)
                    1 -> SecurityTab(viewModel, language)
                    2 -> HistoryTab(viewModel, language)
                }
            }
        }
    }
}
}

@Composable
fun ConnectTab(
    viewModel: VpnViewModel,
    language: AppLanguage,
    onToggleVpn: () -> Unit
) {
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
                        onClick = { onToggleVpn() },
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

            // Display server load quick panel when disconnected
            AnimatedVisibility(
                visible = connectionState == ConnectionState.Disconnected,
                enter = fadeIn(tween(400)),
                exit = fadeOut(tween(400))
            ) {
                ServerLoadQuickPanel(
                    viewModel = viewModel,
                    language = language,
                    onSelectServer = { server ->
                        viewModel.selectServer(server)
                    }
                )
            }

            VpnProtocolSelector(
                viewModel = viewModel,
                language = language,
                connectionState = connectionState
            )
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
fun VpnProtocolSelector(
    viewModel: VpnViewModel,
    language: AppLanguage,
    connectionState: ConnectionState
) {
    val selectedProtocol by viewModel.protocol.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp)
    ) {
        Text(
            text = if (language == AppLanguage.Persian) "پروتکل اتصال (حداکثر سرعت و امنیت)" else "CONNECTION PROTOCOL (MAX SPEED & SECURITY)",
            color = TextSlate400,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val protocols = listOf(VpnProtocol.WireGuard, VpnProtocol.OpenVPN)
            protocols.forEach { proto ->
                val isSelected = selectedProtocol == proto
                val isEnabled = connectionState == ConnectionState.Disconnected
                
                val title = when (proto) {
                    VpnProtocol.WireGuard -> if (language == AppLanguage.Persian) "وایرگارد (پیشنهادی)" else "WireGuard (Recommended)"
                    VpnProtocol.OpenVPN -> if (language == AppLanguage.Persian) "اوپن‌وی‌پی‌ان" else "OpenVPN"
                }

                val subText = when (proto) {
                    VpnProtocol.WireGuard -> if (language == AppLanguage.Persian) "فوق‌سریع • پورت ۵۱۸۲۰" else "Hyper-speed • Port 51820"
                    VpnProtocol.OpenVPN -> if (language == AppLanguage.Persian) "امنیت پیشرفته • پورت ۱۱۹۴" else "Hardened Security • Port 1194"
                }

                Card(
                    onClick = {
                        if (isEnabled) {
                            viewModel.setProtocol(proto)
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) AccentBlue.copy(alpha = 0.2f) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) AccentBlue else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) AccentBlue else (if (isEnabled) TextSlate100 else TextSlate400),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subText,
                            color = if (isSelected) AccentBlue.copy(alpha = 0.8f) else TextSlate400,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServerLoadQuickPanel(
    viewModel: VpnViewModel,
    language: AppLanguage,
    onSelectServer: (VpnServer) -> Unit
) {
    val serverLoads by viewModel.serverLoads.collectAsState()
    val serverLatencies by viewModel.serverLatencies.collectAsState()
    val selectedServer by viewModel.selectedServer.collectAsState()
    
    val popularServers = DefaultServers.take(12) // Show top 12 popular servers for quick load checking

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language == AppLanguage.Persian) "بار زنده سرورها (کلیک برای انتخاب)" else "LIVE SERVER LOADS (TAP TO SELECT)",
                color = TextSlate400,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(NeonGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.Persian) "بروزرسانی زنده" else "LIVE UPDATE",
                        color = NeonGreen,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(popularServers) { server ->
                val isSelected = selectedServer.id == server.id
                val load = serverLoads[server.id] ?: 35
                val latency = serverLatencies[server.id] ?: server.baseLatencyMs
                
                val loadColor = if (load <= 50) NeonGreen else if (load <= 80) Color.Yellow else Color(0xFFFF5252)
                
                Card(
                    onClick = { onSelectServer(server) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) AccentBlue.copy(alpha = 0.15f) else Color(0x0CFFFFFF)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .width(135.dp)
                        .border(
                            width = 1.2.dp,
                            color = if (isSelected) AccentBlue else GlassBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x22000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getCountryFlagEmoji(server.countryCode),
                                    fontSize = 18.sp
                                )
                            }
                            
                            // Latency indicator
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x1AFFFFFF))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$latency ms",
                                    color = if (latency <= 50) NeonGreen else if (latency <= 120) AccentBlue else Color.Yellow,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = if (language == AppLanguage.Persian) server.nameFa.substringBefore(" ") else server.nameEn.substringBefore(" "),
                            color = TextSlate100,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Load progress bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.Persian) "بار: $load%" else "Load: $load%",
                                color = loadColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { load / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = loadColor,
                            trackColor = Color(0x1AFFFFFF)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SecurityTab(viewModel: VpnViewModel, language: AppLanguage) {
    val encryptStream by viewModel.encryptionStream.collectAsState()
    val isConnected = viewModel.connectionState.collectAsState().value == ConnectionState.Connected
    val selectedProtocol by viewModel.protocol.collectAsState()
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
                        status = if (selectedProtocol == VpnProtocol.WireGuard) "WireGuard Core" else "OpenVPN Secure",
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
    val serverLoads by viewModel.serverLoads.collectAsState()

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
                                    text = if (language == AppLanguage.Persian) "بررسی سرعت زنده تمامی ۱۰۰ سرور" else "Test live speeds for all 100 nodes",
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
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = server.ipAddress,
                                            color = TextSlate400,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        // Load status indicator
                                        val load = serverLoads[server.id] ?: 30
                                        val loadColor = if (load <= 50) NeonGreen else if (load <= 80) Color.Yellow else Color(0xFFFF5252)
                                        val loadText = if (language == AppLanguage.Persian) "بار سرور: $load%" else "Load: $load%"
                                        
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(loadColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = loadText,
                                                color = loadColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: VpnViewModel) {
    val language by viewModel.language.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val otpSent by viewModel.otpSent.collectAsState()
    val generatedOtp by viewModel.generatedOtp.collectAsState()
    val currentUserEmail by viewModel.currentUserEmail.collectAsState()
    val realEmailSent by viewModel.realEmailSent.collectAsState()
    val isDemoMode by viewModel.isDemoMode.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Google, 1: Email OTP
    var emailInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var showGoogleAccountsDialog by remember { mutableStateOf(false) }

    // Clean up error on tab swap
    LaunchedEffect(activeTab) {
        viewModel.resetAuthError()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .meshGradientBackground()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .glassCard(RoundedCornerShape(28.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Logo / Emblem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0x14FFFFFF))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "poor VPN Secure Core",
                    tint = AccentBlue,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "poor",
                color = TextSlate100,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Text(
                text = if (language == AppLanguage.Persian) "سامانه ورود و احراز هویت ایمن" else "Secure Authentication System",
                color = TextSlate400,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Dynamic Option Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x0CFFFFFF))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val googleTabTitle = if (language == AppLanguage.Persian) "حساب گوگل" else "Google Account"
                val emailTabTitle = if (language == AppLanguage.Persian) "کد تایید ایمیلی" else "Email OTP"

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeTab == 0) Color(0x1A3A86FF) else Color.Transparent)
                        .border(1.dp, if (activeTab == 0) AccentBlue.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { if (!isAuthLoading) activeTab = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = googleTabTitle,
                        color = if (activeTab == 0) AccentBlue else TextSlate400,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeTab == 1) Color(0x1A3A86FF) else Color.Transparent)
                        .border(1.dp, if (activeTab == 1) AccentBlue.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { if (!isAuthLoading) activeTab = 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emailTabTitle,
                        color = if (activeTab == 1) AccentBlue else TextSlate400,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (activeTab == 0) {
                // --- GOOGLE SIGN-IN TAB ---
                Text(
                    text = if (language == AppLanguage.Persian) 
                        "برای استفاده سریع و بدون نیاز به تایید ایمیل، با حساب گوگل خود وارد شوید." 
                        else "For frictionless access without extra verification steps, sign in using Google.",
                    color = TextSlate400,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    onClick = {
                        if (!isAuthLoading) {
                            showGoogleAccountsDialog = true
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("google_login_btn"),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(16.dp)) {
                                val w = size.width
                                val h = size.height
                                drawCircle(color = Color(0xFFEA4335), radius = w * 0.5f)
                                drawCircle(color = Color.White, radius = w * 0.35f)
                                drawRect(color = Color.White, size = androidx.compose.ui.geometry.Size(w * 0.5f, h * 0.3f), topLeft = Offset(w * 0.5f, h * 0.35f))
                                drawCircle(color = Color(0xFF4285F4), radius = w * 0.25f, center = Offset(w * 0.55f, h * 0.5f))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = if (language == AppLanguage.Persian) "ورود با حساب گوگل" else "Sign in with Google",
                            color = Color(0xFF1F1F1F),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // --- EMAIL OTP TAB ---
                if (!otpSent) {
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { 
                            emailInput = it
                            viewModel.resetAuthError()
                        },
                        label = { Text(if (language == AppLanguage.Persian) "آدرس ایمیل" else "Email Address") },
                        placeholder = { Text("example@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "EmailIcon", tint = AccentBlue) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = GlassBorder,
                            focusedLabelColor = AccentBlue,
                            unfocusedLabelColor = TextSlate400,
                            focusedTextColor = TextSlate100,
                            unfocusedTextColor = TextSlate100,
                            focusedContainerColor = Color(0x0CFFFFFF),
                            unfocusedContainerColor = Color(0x0CFFFFFF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_field"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.sendOtpToEmail(emailInput) },
                        enabled = !isAuthLoading && emailInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue,
                            disabledContainerColor = AccentBlue.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_send_otp_btn")
                    ) {
                        if (isAuthLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (language == AppLanguage.Persian) "ارسال کد تایید" else "Send Verification Code",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Text(
                        text = if (language == AppLanguage.Persian) 
                            "کد تایید ۶ رقمی به آدرس $currentUserEmail ارسال شد." 
                            else "A 6-digit verification code was sent to $currentUserEmail.",
                        color = TextSlate400,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (realEmailSent) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonGreen.copy(alpha = 0.15f))
                                .border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (language == AppLanguage.Persian)
                                    "✅ کد تایید با موفقیت به ایمیل شما ارسال شد! لطفاً صندوق ورودی خود را بررسی کنید."
                                    else "✅ Verification code sent successfully! Please check your inbox.",
                                color = NeonGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (isDemoMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x24FFAA00))
                                .border(1.dp, Color(0x66FFAA00), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (language == AppLanguage.Persian) "⚠️ حالت شبیه‌سازی (کلید Brevo یا Resend تنظیم نشده است)" else "⚠️ Simulation Mode (Brevo or Resend API Key not configured)",
                                    color = Color(0xFFFFAA00),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = generatedOtp ?: "123456",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 4.sp
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { 
                            if (it.length <= 6) {
                                otpInput = it
                                viewModel.resetAuthError()
                            }
                        },
                        label = { Text(if (language == AppLanguage.Persian) "کد تایید ۶ رقمی" else "6-Digit Code") },
                        placeholder = { Text("000000") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "LockIcon", tint = AccentBlue) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = GlassBorder,
                            focusedLabelColor = AccentBlue,
                            unfocusedLabelColor = TextSlate400,
                            focusedTextColor = TextSlate100,
                            unfocusedTextColor = TextSlate100,
                            focusedContainerColor = Color(0x0CFFFFFF),
                            unfocusedContainerColor = Color(0x0CFFFFFF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_otp_field"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier
                                .size(50.dp)
                                .glassCard(RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = TextSlate100
                            )
                        }

                        Button(
                            onClick = { viewModel.verifyOtp(otpInput) },
                            enabled = !isAuthLoading && otpInput.length == 6,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentBlue,
                                disabledContainerColor = AccentBlue.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("auth_verify_otp_btn")
                        ) {
                            if (isAuthLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = if (language == AppLanguage.Persian) "بررسی کد و ورود" else "Verify & Enter",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (!authError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(WarningRed.copy(alpha = 0.15f))
                        .border(1.dp, WarningRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = authError ?: "",
                        color = WarningRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (showGoogleAccountsDialog) {
            AlertDialog(
                onDismissRequest = { showGoogleAccountsDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.Persian) "انتخاب حساب کاربری گوگل" else "Choose a Google Account",
                            color = TextSlate100,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showGoogleAccountsDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSlate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GoogleAccountRow(
                            name = "Moein Masnavidost",
                            email = "moeinmasnavidost19@gmail.com",
                            onClick = {
                                showGoogleAccountsDialog = false
                                viewModel.loginWithGoogle("moeinmasnavidost19@gmail.com", "Moein")
                            }
                        )

                        GoogleAccountRow(
                            name = "Guest User",
                            email = "guest.poorvpn@gmail.com",
                            onClick = {
                                showGoogleAccountsDialog = false
                                viewModel.loginWithGoogle("guest.poorvpn@gmail.com", "Guest")
                            }
                        )
                    }
                },
                confirmButton = {},
                containerColor = DeepDarkBg,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun GoogleAccountRow(
    name: String,
    email: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x243A86FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    color = AccentBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = name,
                    color = TextSlate100,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email,
                    color = TextSlate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

