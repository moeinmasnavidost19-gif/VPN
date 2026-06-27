package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connection_logs")
data class ConnectionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serverName: String,
    val serverCountryCode: String, // e.g., "DE", "FI", "US"
    val durationSeconds: Long,
    val bytesUploaded: Long,
    val bytesDownloaded: Long,
    val timestamp: Long = System.currentTimeMillis()
)
