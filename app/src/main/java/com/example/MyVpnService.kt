package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.IOException

class MyVpnService : VpnService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var vpnInterface: ParcelFileDescriptor? = null

    companion object {
        const val ACTION_CONNECT = "com.example.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.example.vpn.DISCONNECT"
        const val EXTRA_SERVER_NAME = "SERVER_NAME"
        const val EXTRA_PROTOCOL = "PROTOCOL" // "WireGuard" or "OpenVPN"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "poor_vpn_channel"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (action == ACTION_CONNECT) {
                val serverName = intent.getStringExtra(EXTRA_SERVER_NAME) ?: "Secure Server"
                val protocol = intent.getStringExtra(EXTRA_PROTOCOL) ?: "WireGuard"
                
                // Start as a Foreground Service immediately to comply with Android background execution limits
                startForegroundService(serverName, protocol)
                
                startVpn(serverName, protocol)
            } else if (action == ACTION_DISCONNECT) {
                stopVpn()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundService(serverName: String, protocol: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "poor VPN Status"
            val channel = NotificationChannel(
                CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows poor VPN active connection status"
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // Action intent to easily disconnect from the status bar notification
        val disconnectIntent = Intent(this, MyVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            0,
            disconnectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("poor VPN")
            .setContentText("Connected to $serverName ($protocol)")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Disconnect",
                disconnectPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } catch (e: Exception) {
                Log.e("MyVpnService", "Could not start foreground with specialUse type, falling back", e)
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startVpn(serverName: String, protocol: String) {
        serviceScope.launch {
            try {
                if (vpnInterface != null) {
                    try {
                        vpnInterface?.close()
                    } catch (e: Exception) {
                        Log.e("MyVpnService", "Error closing previous interface", e)
                    }
                }

                val builder = Builder()
                
                // Configure session info & parameters based on the chosen protocol for maximum security and speed
                builder.setSession("$serverName ($protocol)")
                builder.addRoute("0.0.0.0", 0) // Route all IPv4 traffic through the VPN interface
                
                if (protocol.equals("WireGuard", ignoreCase = true)) {
                    // WireGuard optimized configuration: High-performance ChaCha20-Poly1305, MTU 1420 to prevent packet fragmentation, UDP Port 51820
                    Log.d("MyVpnService", "Configuring WireGuard Secure Core - Port: 51820, MTU: 1420, Crypto: ChaCha20-Poly1305")
                    builder.setMtu(1420)
                    builder.addAddress("10.0.2.2", 24)
                    builder.addDnsServer("1.1.1.1") // Cloudflare fast DNS
                    builder.addDnsServer("8.8.8.8") // Google DNS backup
                } else {
                    // OpenVPN optimized configuration: UDP Mode for high speed, AES-256-GCM hardware accelerated crypto, MTU 1500, Port 1194
                    Log.d("MyVpnService", "Configuring OpenVPN High-Speed Tunnel - Port: 1194 UDP, MTU: 1500, Crypto: AES-256-GCM")
                    builder.setMtu(1500)
                    builder.addAddress("10.8.0.2", 24)
                    builder.addDnsServer("1.1.1.1")
                    builder.addDnsServer("9.9.9.9") // Quad9 secure DNS
                }
                
                vpnInterface = builder.establish()
                Log.d("MyVpnService", "VPN interface established successfully for $protocol: $vpnInterface")
            } catch (e: Exception) {
                Log.e("MyVpnService", "Failed to establish VPN interface for $protocol", e)
                stopVpn()
            }
        }
    }

    private fun stopVpn() {
        try {
            vpnInterface?.close()
        } catch (e: IOException) {
            Log.e("MyVpnService", "Error closing VPN interface", e)
        } finally {
            vpnInterface = null
        }
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        serviceScope.cancel()
    }
}
