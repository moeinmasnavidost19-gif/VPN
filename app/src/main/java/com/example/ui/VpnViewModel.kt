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
import com.example.BuildConfig
import com.example.network.BrevoApiService
import com.example.network.SendSmtpEmail
import com.example.network.SendSmtpEmailSender
import com.example.network.SendSmtpEmailTo
import com.example.network.ResendApiService
import com.example.network.SendResendEmail
import android.util.Log

enum class ConnectionState {
    Disconnected,
    Connecting,
    Connected
}

enum class AppLanguage {
    English,
    Persian
}

enum class VpnProtocol {
    WireGuard,
    OpenVPN
}

class VpnViewModel(private val repository: ConnectionLogRepository) : ViewModel() {

    // Authentication State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _currentUserName = MutableStateFlow<String?>(null)
    val currentUserName: StateFlow<String?> = _currentUserName.asStateFlow()

    private val _otpSent = MutableStateFlow(false)
    val otpSent: StateFlow<Boolean> = _otpSent.asStateFlow()

    private val _generatedOtp = MutableStateFlow<String?>(null)
    val generatedOtp: StateFlow<String?> = _generatedOtp.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _realEmailSent = MutableStateFlow(false)
    val realEmailSent: StateFlow<Boolean> = _realEmailSent.asStateFlow()

    private val _isDemoMode = MutableStateFlow(false)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    fun loginWithGoogle(email: String, name: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            delay(1200) // Beautiful authentication latency simulation
            _currentUserEmail.value = email
            _currentUserName.value = name
            _isLoggedIn.value = true
            _isAuthLoading.value = false
        }
    }

    fun sendOtpToEmail(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authError.value = if (_language.value == AppLanguage.Persian) "لطفاً یک ایمیل معتبر وارد کنید" else "Please enter a valid email address"
            return
        }
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            _realEmailSent.value = false
            _isDemoMode.value = false

            val otp = (100000..999999).random().toString()
            _generatedOtp.value = otp
            _currentUserEmail.value = email

            val resendKey = BuildConfig.RESEND_API_KEY
            val brevoKey = BuildConfig.BREVO_API_KEY

            val isResendConfigured = resendKey.isNotBlank() && resendKey != "YOUR_RESEND_API_KEY"
            val isBrevoConfigured = brevoKey.isNotBlank() && brevoKey != "YOUR_BREVO_API_KEY"

            if (!isResendConfigured && !isBrevoConfigured) {
                // Fall back to simulation mode gracefully
                _isDemoMode.value = true
                _otpSent.value = true
                _isAuthLoading.value = false
                return@launch
            }

            if (isResendConfigured) {
                try {
                    val service = ResendApiService.create()
                    val response = service.sendEmail(
                        authorizationHeader = "Bearer $resendKey",
                        email = SendResendEmail(
                            from = "poor VPN <onboarding@resend.dev>",
                            to = listOf(email),
                            subject = if (_language.value == AppLanguage.Persian) "کد تایید ورود به poor VPN" else "poor VPN Verification Code",
                            html = """
                                <div style="font-family: Arial, sans-serif; direction: ${if (_language.value == AppLanguage.Persian) "rtl" else "ltr"}; text-align: center; padding: 20px; border-radius: 10px; background-color: #0f172a; color: #f1f5f9; border: 1px solid #334155;">
                                    <h2 style="color: #3a86ff;">poor VPN</h2>
                                    <p>${if (_language.value == AppLanguage.Persian) "کد تایید یک‌بار مصرف شما برای ورود به برنامه:" else "Your one-time verification code to log in is:"}</p>
                                    <div style="display: inline-block; padding: 15px 30px; margin: 15px 0; font-size: 28px; font-weight: bold; letter-spacing: 4px; border-radius: 8px; background-color: #1e293b; color: #3a86ff; border: 1px solid #3a86ff;">
                                        $otp
                                    </div>
                                    <p style="font-size: 12px; color: #94a3b8;">${if (_language.value == AppLanguage.Persian) "این کد پس از چند دقیقه منقضی می‌شود." else "This code will expire in a few minutes."}</p>
                                </div>
                            """.trimIndent()
                        )
                    )

                    if (response.isSuccessful) {
                        _realEmailSent.value = true
                        _otpSent.value = true
                    } else {
                        Log.e("VpnViewModel", "Resend sendEmail error: ${response.code()} ${response.message()}")
                        _isDemoMode.value = true
                        _otpSent.value = true
                        _authError.value = if (_language.value == AppLanguage.Persian) 
                            "ارسال ایمیل واقعی با Resend ناموفق بود. از حالت شبیه‌ساز استفاده شد." 
                            else "Real email sending with Resend failed. Falling back to simulation mode."
                    }
                } catch (e: Exception) {
                    Log.e("VpnViewModel", "Resend sendEmail failed", e)
                    _isDemoMode.value = true
                    _otpSent.value = true
                    _authError.value = if (_language.value == AppLanguage.Persian) 
                        "خطا در ارتباط با سرور Resend. از حالت شبیه‌ساز استفاده شد." 
                        else "Connection error with Resend server. Falling back to simulation mode."
                } finally {
                    _isAuthLoading.value = false
                }
            } else {
                try {
                    val service = BrevoApiService.create()
                    val response = service.sendEmail(
                        apiKey = brevoKey,
                        email = SendSmtpEmail(
                            sender = SendSmtpEmailSender(name = "poor VPN", email = "no-reply@poorvpn.com"),
                            to = listOf(SendSmtpEmailTo(email = email)),
                            subject = if (_language.value == AppLanguage.Persian) "کد تایید ورود به poor VPN" else "poor VPN Verification Code",
                            htmlContent = """
                                <div style="font-family: Arial, sans-serif; direction: ${if (_language.value == AppLanguage.Persian) "rtl" else "ltr"}; text-align: center; padding: 20px; border-radius: 10px; background-color: #0f172a; color: #f1f5f9; border: 1px solid #334155;">
                                    <h2 style="color: #3a86ff;">poor VPN</h2>
                                    <p>${if (_language.value == AppLanguage.Persian) "کد تایید یک‌بار مصرف شما برای ورود به برنامه:" else "Your one-time verification code to log in is:"}</p>
                                    <div style="display: inline-block; padding: 15px 30px; margin: 15px 0; font-size: 28px; font-weight: bold; letter-spacing: 4px; border-radius: 8px; background-color: #1e293b; color: #3a86ff; border: 1px solid #3a86ff;">
                                        $otp
                                    </div>
                                    <p style="font-size: 12px; color: #94a3b8;">${if (_language.value == AppLanguage.Persian) "این کد پس از چند دقیقه منقضی می‌شود." else "This code will expire in a few minutes."}</p>
                                </div>
                            """.trimIndent()
                        )
                    )

                    if (response.isSuccessful) {
                        _realEmailSent.value = true
                        _otpSent.value = true
                    } else {
                        Log.e("VpnViewModel", "Brevo sendEmail error: ${response.code()} ${response.message()}")
                        _isDemoMode.value = true
                        _otpSent.value = true
                        _authError.value = if (_language.value == AppLanguage.Persian) 
                            "ارسال ایمیل واقعی با Brevo ناموفق بود. از حالت شبیه‌ساز استفاده شد." 
                            else "Real email sending with Brevo failed. Falling back to simulation mode."
                    }
                } catch (e: Exception) {
                    Log.e("VpnViewModel", "Brevo sendEmail failed", e)
                    _isDemoMode.value = true
                    _otpSent.value = true
                    _authError.value = if (_language.value == AppLanguage.Persian) 
                        "خطا در ارتباط با سرور Brevo. از حالت شبیه‌ساز استفاده شد." 
                        else "Connection error with Brevo server. Falling back to simulation mode."
                } finally {
                    _isAuthLoading.value = false
                }
            }
        }
    }

    fun verifyOtp(enteredCode: String) {
        if (enteredCode != _generatedOtp.value) {
            _authError.value = if (_language.value == AppLanguage.Persian) "کد وارد شده صحیح نیست" else "Invalid verification code"
            return
        }
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            delay(1000)
            _currentUserName.value = _currentUserEmail.value?.substringBefore("@")
            _isLoggedIn.value = true
            _isAuthLoading.value = false
        }
    }

    fun resetAuthError() {
        _authError.value = null
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUserEmail.value = null
        _currentUserName.value = null
        _otpSent.value = false
        _generatedOtp.value = null
        _realEmailSent.value = false
        _isDemoMode.value = false
    }

    // Language setting
    private val _language = MutableStateFlow(AppLanguage.Persian) // Default to Persian as requested
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // Selected Protocol
    private val _protocol = MutableStateFlow(VpnProtocol.WireGuard)
    val protocol: StateFlow<VpnProtocol> = _protocol.asStateFlow()

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

    // Real-time server latencies map initialized with the default values
    private val _serverLatencies = MutableStateFlow<Map<String, Int>>(DefaultServers.associate { it.id to it.baseLatencyMs })
    val serverLatencies: StateFlow<Map<String, Int>> = _serverLatencies.asStateFlow()

    // Real-time server load percentage map (0 - 100%) initialized with realistic values
    private val _serverLoads = MutableStateFlow<Map<String, Int>>(
        DefaultServers.associate { it.id to (12..82).random() }
    )
    val serverLoads: StateFlow<Map<String, Int>> = _serverLoads.asStateFlow()

    // Real-time pinging progress status
    private val _isPingingAll = MutableStateFlow(false)
    val isPingingAll: StateFlow<Boolean> = _isPingingAll.asStateFlow()

    // Real-time ping progress message or count
    private val _pingProgressText = MutableStateFlow("")
    val pingProgressText: StateFlow<String> = _pingProgressText.asStateFlow()

    // Database Logs Flow
    val connectionLogs: StateFlow<List<ConnectionLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var connectionJob: Job? = null
    private var encryptionJob: Job? = null
    private var loadJob: Job? = null

    init {
        // Start background encryption scanner animation even when disconnected
        // to show beautiful idle tech-stream.
        startEncryptionStreamSim()
        // Start real-time server load simulation to fluctuate load speeds dynamically
        startServerLoadSim()
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun setProtocol(proto: VpnProtocol) {
        if (_connectionState.value == ConnectionState.Disconnected) {
            _protocol.value = proto
        }
    }

    fun selectServer(server: VpnServer) {
        if (_connectionState.value == ConnectionState.Disconnected) {
            _selectedServer.value = server
            val measuredPing = _serverLatencies.value[server.id] ?: server.baseLatencyMs
            _currentPing.value = measuredPing
        }
    }

    fun runRealTimePingAll() {
        if (_isPingingAll.value) return
        _isPingingAll.value = true
        _pingProgressText.value = if (_language.value == AppLanguage.Persian) "در حال شروع تست پینگ..." else "Starting ping test..."
        
        viewModelScope.launch {
            val currentMap = _serverLatencies.value.toMutableMap()
            val servers = DefaultServers
            val total = servers.size
            var completedCount = 0

            // We will ping them in batches of 5 in parallel to make it visually attractive and extremely fast!
            servers.chunked(5).forEach { batch ->
                batch.map { server ->
                    launch {
                        val ping = server.testActualLatency()
                        currentMap[server.id] = ping
                        _serverLatencies.value = currentMap.toMap()
                        completedCount++
                        _pingProgressText.value = if (_language.value == AppLanguage.Persian) {
                            "تست پینگ: $completedCount از $total سرور..."
                        } else {
                            "Ping progress: $completedCount of $total..."
                        }
                    }
                }.forEach { it.join() }
                delay(80)
            }
            
            _pingProgressText.value = if (_language.value == AppLanguage.Persian) "تست با موفقیت پایان یافت!" else "Ping test finished!"
            _isPingingAll.value = false
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
            val actualPing = _serverLatencies.value[server.id] ?: server.testActualLatency()
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

    private fun startServerLoadSim() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            while (true) {
                delay(4000)
                val currentMap = _serverLoads.value.toMutableMap()
                DefaultServers.forEach { server ->
                    val currentLoad = currentMap[server.id] ?: (15..85).random()
                    // Fluctuate load by -4% to +4%
                    val delta = (-4..4).random()
                    val newLoad = (currentLoad + delta).coerceIn(8, 98)
                    currentMap[server.id] = newLoad
                }
                _serverLoads.value = currentMap.toMap()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        connectionJob?.cancel()
        encryptionJob?.cancel()
        loadJob?.cancel()
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
