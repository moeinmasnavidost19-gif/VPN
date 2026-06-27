package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ConnectionLog
import com.example.data.ConnectionLogRepository
import com.example.data.DefaultServers
import com.example.data.VpnServer
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ConnectionState {
    Disconnected,
    Connecting,
    Connected
}

enum class AppLanguage {
    English,
    Persian
}

class VpnViewModel(private val repository: ConnectionLogRepository) : ViewModel() {

    // Language setting
    private val _language = MutableStateFlow(AppLanguage.Persian) // Default to Persian as requested
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // Active connection state
    private val _connectionState = MutableStateFlow(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // Selected server
    private val _selectedServer = MutableStateFlow(DefaultServers[0]) // Default: Germany
    val selectedServer: StateFlow<VpnServer> = _selectedServer.asStateFlow()

    // Measured connection ping
    private val _currentPing = MutableStateFlow(34)
    val currentPing: StateFlow<Int> = _currentPing.asStateFlow()

    // Running speeds
    private val _downloadSpeed = MutableStateFlow(0.0) // MB/s
    val downloadSpeed: StateFlow<Double> = _downloadSpeed.asStateFlow()

    private val _uploadSpeed = MutableStateFlow(0.0) // MB/s
    val uploadSpeed: StateFlow<Double> = _uploadSpeed.asStateFlow()

    // Bytes transferred during session
    private val _bytesUploaded = MutableStateFlow(0L)
    val bytesUploaded: StateFlow<Long> = _bytesUploaded.asStateFlow()

    private val _bytesDownloaded = MutableStateFlow(0L)
    val bytesDownloaded: StateFlow<Long> = _bytesDownloaded.asStateFlow()

    // Connection timer
    private val _durationSeconds = MutableStateFlow(0L)
    val durationSeconds: StateFlow<Long> = _durationSeconds.asStateFlow()

    // Encryption simulator bytes stream (hex string blocks)
    private val _encryptionStream = MutableStateFlow<List<String>>(emptyList())
    val encryptionStream: StateFlow<List<String>> = _encryptionStream.asStateFlow()

    // Database Logs Flow
    val connectionLogs: StateFlow<List<ConnectionLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var connectionJob: Job? = null
    private var encryptionJob: Job? = null

    init {
        // Start background encryption scanner animation even when disconnected
        // to show beautiful idle tech-stream.
        startEncryptionStreamSim()
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun selectServer(server: VpnServer) {
        if (_connectionState.value == ConnectionState.Disconnected) {
            _selectedServer.value = server
            _currentPing.value = server.baseLatencyMs
        }
    }

    fun toggleConnection() {
        when (_connectionState.value) {
            ConnectionState.Disconnected -> startConnection()
            ConnectionState.Connected -> stopConnection()
            ConnectionState.Connecting -> {
                // Cancel midway
                connectionJob?.cancel()
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }

    private fun startConnection() {
        _connectionState.value = ConnectionState.Connecting
        
        connectionJob = viewModelScope.launch {
            // Authentic handshake latency simulation
            delay(1500)

            // Measure actual latency
            val server = _selectedServer.value
            val actualPing = server.testActualLatency()
            _currentPing.value = actualPing

            _connectionState.value = ConnectionState.Connected
            _durationSeconds.value = 0L
            _bytesDownloaded.value = 0L
            _bytesUploaded.value = 0L

            // Launch active metrics tracking loop
            launchActiveSessionTracker()
        }
    }

    private fun stopConnection() {
        connectionJob?.cancel()
        
        val finalDuration = _durationSeconds.value
        val finalUploaded = _bytesUploaded.value
        val finalDownloaded = _bytesDownloaded.value
        val server = _selectedServer.value

        _connectionState.value = ConnectionState.Disconnected
        _downloadSpeed.value = 0.0
        _uploadSpeed.value = 0.0

        // Save session log into local Room database
        viewModelScope.launch {
            if (finalDuration > 0) {
                repository.insertLog(
                    ConnectionLog(
                        serverName = if (_language.value == AppLanguage.Persian) server.nameFa else server.nameEn,
                        serverCountryCode = server.countryCode,
                        durationSeconds = finalDuration,
                        bytesUploaded = finalUploaded,
                        bytesDownloaded = finalDownloaded
                    )
                )
            }
        }
    }

    private fun launchActiveSessionTracker() {
        connectionJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _durationSeconds.value += 1

                // Simulate high speed in MB/s
                // Generate fast transfer rates to demonstrate 1ms or premium line qualities
                val downSpeed = Random.nextDouble(18.5, 48.2) // Fast download
                val upSpeed = Random.nextDouble(5.1, 14.6)   // Fast upload

                _downloadSpeed.value = downSpeed
                _uploadSpeed.value = upSpeed

                // Accumulate bytes: MB/s * 1,000,000 to convert to bytes per second
                _bytesDownloaded.value += (downSpeed * 1_000_000).toLong()
                _bytesUploaded.value += (upSpeed * 1_000_000).toLong()
            }
        }
    }

    private fun startEncryptionStreamSim() {
        encryptionJob = viewModelScope.launch {
            val charPool = "0123456789ABCDEF"
            while (true) {
                // When connected, encrypt rapid packages. Idle stream when disconnected.
                val interval = if (_connectionState.value == ConnectionState.Connected) 180L else 700L
                delay(interval)

                val newList = mutableListOf<String>()
                val count = if (_connectionState.value == ConnectionState.Connected) 8 else 5
                
                for (i in 0 until count) {
                    val line = StringBuilder()
                    // Create standard secure encrypted cipher blocks: AES-256 style
                    line.append("AES-256 [0x")
                    for (j in 0 until 8) {
                        line.append(charPool[Random.nextInt(charPool.length)])
                    }
                    line.append("] ")
                    if (_connectionState.value == ConnectionState.Connected) {
                        line.append("➔ SECURE_PACKET_CIPHER_OK")
                    } else {
                        line.append("➔ IDLE_STANDBY_SECURE")
                    }
                    newList.add(line.toString())
                }
                _encryptionStream.value = newList
            }
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    override fun onCleared() {
        super.onCleared()
        connectionJob?.cancel()
        encryptionJob?.cancel()
    }
}

class VpnViewModelFactory(private val repository: ConnectionLogRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VpnViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VpnViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
