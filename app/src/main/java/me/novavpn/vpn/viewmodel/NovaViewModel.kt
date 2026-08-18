package me.novavpn.vpn.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.novavpn.vpn.data.AuthApi
import me.novavpn.vpn.data.AuthException
import me.novavpn.vpn.data.ServiceCatalog
import me.novavpn.vpn.data.SessionStore
import me.novavpn.vpn.engine.TunnelManager
import me.novavpn.vpn.model.*

enum class SessionStartup { RESTORING, READY }

class NovaVPNViewModel(application: Application) : AndroidViewModel(application) {
    private val api = AuthApi()
    private val tunnels = TunnelManager(application)
    private val sessions = SessionStore(application)
    private val connectionStore = application.getSharedPreferences("nova_connection", 0)
    private val _startup = MutableStateFlow(SessionStartup.RESTORING)
    val startup: StateFlow<SessionStartup> = _startup.asStateFlow()
    private val _user = MutableStateFlow<UserAccount?>(null)
    val user: StateFlow<UserAccount?> = _user.asStateFlow()
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()
    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()
    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _connection = MutableStateFlow(
        ConnectionUiState(
            region = ServiceCatalog.regions.firstOrNull { it.code == connectionStore.getString("region", null) } ?: ServiceCatalog.fastest,
            protocol = runCatching { TunnelProtocol.valueOf(connectionStore.getString("protocol", null).orEmpty()) }.getOrDefault(TunnelProtocol.WIREGUARD),
        )
    )
    val connection: StateFlow<ConnectionUiState> = _connection.asStateFlow()
    val regions = ServiceCatalog.regions
    val adminMetrics = MutableStateFlow(AdminMetrics())
    private var telemetryJob: Job? = null

    init {
        val cachedUser = sessions.cachedUser()
        if (cachedUser != null) _user.value = cachedUser
        viewModelScope.launch {
            val minimumSplash = async { delay(900) }
            if (cachedUser == null && sessions.accessToken() != null) restoreSession()
            minimumSplash.await()
            _startup.value = SessionStartup.READY
            if (cachedUser != null) restoreSession()
        }
    }

    private suspend fun restoreSession() {
        val access = sessions.accessToken()
        val refresh = sessions.refreshToken()
        if (access == null || refresh == null) return
        try {
            val current = api.me(access)
            _user.value = current
            sessions.updateUser(current)
        } catch (error: AuthException) {
            if (error.status !in listOf(401, 403)) return
            try {
                val renewed = api.refresh(refresh)
                sessions.save(renewed.accessToken, renewed.refreshToken, renewed.user)
                _user.value = renewed.user
            } catch (refreshError: AuthException) {
                if (refreshError.status in listOf(400, 401, 403)) {
                    sessions.clear()
                    _user.value = null
                }
            } catch (_: Exception) {
                // Keep the cached account during temporary network failures.
            }
        } catch (_: Exception) {
            // Offline startup keeps the cached account and retries on the next launch.
        }
    }

    fun register(firstName: String, lastName: String, username: String, email: String, password: String) {
        if (firstName.isBlank() || lastName.isBlank() || username.length < 3 || password.length < 10) {
            _authError.value = "Completa tus datos y usa una contraseña de 10 caracteres"
            return
        }
        authCall {
            api.register(firstName, lastName, username, email, password)
            _authMessage.value = "Cuenta creada. Revisa tu correo para verificarla."
        }
    }

    fun login(email: String, password: String) = authCall {
        val result = api.login(email, password)
        sessions.save(result.accessToken, result.refreshToken, result.user)
        _user.value = result.user
        if (result.user.role == AccountRole.ADMIN) {
            runCatching { api.adminUserCount(result.accessToken) }.onSuccess { count ->
                adminMetrics.value = adminMetrics.value.copy(registeredClients = count)
            }
        }
    }

    fun forgotPassword(email: String) = authCall {
        api.forgotPassword(email)
        _authMessage.value = "Si la cuenta existe, recibirás un correo de recuperación."
    }

    fun resetPassword(token: String, password: String, onSuccess: () -> Unit) {
        if (password.length < 10) { _authError.value = "Usa al menos 10 caracteres"; return }
        authCall {
            api.resetPassword(token, password)
            _authMessage.value = "Contraseña actualizada. Ya puedes iniciar sesión."
            onSuccess()
        }
    }

    private fun authCall(block: suspend () -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true; _authError.value = null
            try { block() }
            catch (error: AuthException) {
                _authError.value = when (error.code) {
                    "invalid_credentials" -> "Correo o contraseña incorrectos"
                    "email_not_verified" -> "Verifica tu correo antes de entrar"
                    "account_exists" -> "Ya existe una cuenta con ese correo"
                    "weak_password" -> "La contraseña es demasiado corta"
                    "invalid_token", "expired_token" -> "El enlace ya no es válido"
                    else -> "No se pudo completar la operación (${error.code})"
                }
            } catch (_: Exception) { _authError.value = "No se pudo conectar con NovaVPN" }
            finally { _authLoading.value = false }
        }
    }

    fun clearAuthMessage() { _authMessage.value = null; _authError.value = null }
    fun logout() { disconnect(); sessions.clear(); _user.value = null }
    fun selectRegion(region: VpnRegion) {
        if (_connection.value.phase == ConnectionPhase.DISCONNECTED) {
            _connection.value = _connection.value.copy(region = region)
            connectionStore.edit().putString("region", region.code).apply()
        }
    }
    fun setPreferences(value: ConnectionPreferences) { tunnels.setPreferences(value) }
    fun selectProtocol(protocol: TunnelProtocol) {
        if (_connection.value.phase == ConnectionPhase.DISCONNECTED) {
            _connection.value = _connection.value.copy(protocol = protocol)
            connectionStore.edit().putString("protocol", protocol.name).apply()
        }
    }

    fun connectProxy(region: VpnRegion) {
        if (_connection.value.phase != ConnectionPhase.DISCONNECTED) return
        _connection.value = _connection.value.copy(region = region, protocol = TunnelProtocol.SOCKS5)
        connect()
    }

    fun toggleConnection() { when (_connection.value.phase) { ConnectionPhase.DISCONNECTED -> connect(); ConnectionPhase.CONNECTED -> disconnect(); else -> Unit } }

    private fun connect() {
        val target = _connection.value
        _connection.value = target.copy(phase = ConnectionPhase.CONNECTING)
        viewModelScope.launch {
            tunnels.connect(target.protocol, target.region)
                .onSuccess {
                    _connection.value = target.copy(phase = ConnectionPhase.CONNECTED, connectedSeconds = 0, publicIp = "Protegida")
                    startTelemetry()
                }
                .onFailure { error ->
                    _connection.value = target.copy(phase = ConnectionPhase.DISCONNECTED)
                    _authError.value = error.message ?: "No se pudo iniciar el túnel"
                }
        }
    }
    fun disconnect() {
        if (_connection.value.phase == ConnectionPhase.DISCONNECTED) return
        val current = _connection.value
        _connection.value = current.copy(phase = ConnectionPhase.DISCONNECTING)
        telemetryJob?.cancel()
        viewModelScope.launch {
            tunnels.disconnect()
            _connection.value = current.copy(phase = ConnectionPhase.DISCONNECTED, connectedSeconds = 0, downloadMbps = 0.0, uploadMbps = 0.0, publicIp = "—")
        }
    }
    private fun startTelemetry() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            _connection.value = _connection.value.copy(publicIp = tunnels.publicIp())
            var previous = tunnels.traffic()
            while (true) {
                delay(1_000)
                val current = _connection.value
                val bytes = tunnels.traffic()
                val down = ((bytes.first - previous.first).coerceAtLeast(0L) * 8.0) / 1_000_000.0
                val up = ((bytes.second - previous.second).coerceAtLeast(0L) * 8.0) / 1_000_000.0
                previous = bytes
                _connection.value = current.copy(connectedSeconds = current.connectedSeconds + 1, downloadMbps = down, uploadMbps = up)
            }
        }
    }
    fun toggleRegion(code: String) { adminMetrics.value = adminMetrics.value.copy(onlineNodes = (adminMetrics.value.onlineNodes + if (code.hashCode() % 2 == 0) -1 else 1).coerceAtLeast(1)) }
}
