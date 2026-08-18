package me.novavpn.vpn.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.novavpn.vpn.model.AccountRole
import me.novavpn.vpn.model.UserAccount
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AuthApi(private val baseUrl: String = "https://auth.echosmart.me/api") {
    data class LoginResult(val accessToken: String, val refreshToken: String, val user: UserAccount)

    suspend fun register(firstName: String, lastName: String, username: String, email: String, password: String): UserAccount {
        val result = request("POST", "/auth/register", JSONObject().apply {
            put("firstName", firstName); put("lastName", lastName); put("username", username)
            put("email", email); put("password", password)
        })
        return parseUser(result.getJSONObject("user"))
    }

    suspend fun login(email: String, password: String): LoginResult {
        val result = request("POST", "/auth/login", JSONObject().put("email", email).put("password", password))
        return parseLogin(result)
    }

    suspend fun refresh(refreshToken: String): LoginResult {
        val result = request("POST", "/auth/refresh", JSONObject().put("refreshToken", refreshToken))
        return parseLogin(result)
    }

    suspend fun forgotPassword(email: String) {
        request("POST", "/auth/forgot-password", JSONObject().put("email", email))
    }

    suspend fun resetPassword(token: String, password: String) {
        request("POST", "/auth/reset-password", JSONObject().put("token", token).put("password", password))
    }

    suspend fun me(accessToken: String): UserAccount = parseUser(request("GET", "/auth/me", token = accessToken).getJSONObject("user"))


    data class WireGuardProfile(val address: String, val dns: String, val endpoint: String, val serverPublicKey: String, val allowedIps: String, val persistentKeepalive: Int)
    data class ProxyCredentials(val username: String, val password: String)

    suspend fun provisionWireGuard(region: String, publicKey: String, accessToken: String): WireGuardProfile {
        val value = request("POST", "/vpn/wireguard/provision", JSONObject().put("region", region).put("publicKey", publicKey), accessToken)
        return WireGuardProfile(value.getString("address"), value.getString("dns"), value.getString("endpoint"), value.getString("serverPublicKey"), value.getString("allowedIps"), value.getInt("persistentKeepalive"))
    }

    suspend fun proxyCredentials(accessToken: String): ProxyCredentials {
        val value = request("GET", "/proxy/credentials", token = accessToken)
        return ProxyCredentials(value.getString("username"), value.getString("password"))
    }

    suspend fun adminUserCount(accessToken: String): Int = request("GET", "/management/users", token = accessToken).getInt("count")

    private suspend fun request(method: String, path: String, payload: JSONObject? = null, token: String? = null): JSONObject = withContext(Dispatchers.IO) {
        val connection = URL(baseUrl + path).openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = 15_000
        connection.readTimeout = 20_000
        connection.setRequestProperty("accept", "application/json")
        connection.setRequestProperty("content-type", "application/json")
        token?.let { connection.setRequestProperty("authorization", "Bearer $it") }
        if (payload != null) {
            connection.doOutput = true
            connection.outputStream.use { it.write(payload.toString().toByteArray()) }
        }
        val status = connection.responseCode
        val text = (if (status in 200..299) connection.inputStream else connection.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
        val body = if (text.isBlank()) JSONObject() else JSONObject(text)
        if (status !in 200..299) throw AuthException(status, body.optString("error", "request_failed"))
        body
    }

    private fun parseLogin(value: JSONObject) = LoginResult(
        accessToken = value.getString("accessToken"),
        refreshToken = value.getString("refreshToken"),
        user = parseUser(value.getJSONObject("user")),
    )

    private fun parseUser(value: JSONObject) = UserAccount(
        name = listOf(value.optString("firstName"), value.optString("lastName")).filter { it.isNotBlank() }.joinToString(" "),
        email = value.getString("email"),
        role = if (value.optString("role") == "ADMIN") AccountRole.ADMIN else AccountRole.CLIENT,
        username = value.optString("username"),
        firstName = value.optString("firstName"),
        lastName = value.optString("lastName"),
    )
}

class AuthException(val status: Int, val code: String) : Exception(code)

class SessionStore(context: Context) {
    private val preferences = context.getSharedPreferences("echosmart_session", Context.MODE_PRIVATE)
    private val cipher = SecureSessionCipher()

    private data class StoredSession(val accessToken: String, val refreshToken: String, val user: UserAccount?)

    fun save(accessToken: String, refreshToken: String, user: UserAccount) {
        val payload = JSONObject().apply {
            put("access", accessToken)
            put("refresh", refreshToken)
            put("user", userJson(user))
        }
        preferences.edit()
            .putString("secure_session", cipher.encrypt(payload.toString()))
            .remove("access").remove("refresh").remove("user")
            .commit()
    }

    fun accessToken(): String? = load()?.accessToken
    fun refreshToken(): String? = load()?.refreshToken
    fun cachedUser(): UserAccount? = load()?.user

    fun updateUser(user: UserAccount) {
        val current = load() ?: return
        save(current.accessToken, current.refreshToken, user)
    }

    fun clear() = preferences.edit().clear().commit()

    private fun load(): StoredSession? {
        preferences.getString("secure_session", null)?.let { encrypted ->
            runCatching { JSONObject(cipher.decrypt(encrypted)) }.getOrNull()?.let { value ->
                return StoredSession(
                    value.getString("access"),
                    value.getString("refresh"),
                    value.optJSONObject("user")?.let(::parseStoredUser),
                )
            }
        }
        val legacyAccess = preferences.getString("access", null) ?: return null
        val legacyRefresh = preferences.getString("refresh", null) ?: return null
        val legacyUser = preferences.getString("user", null)?.let { runCatching { parseStoredUser(JSONObject(it)) }.getOrNull() }
        return StoredSession(legacyAccess, legacyRefresh, legacyUser)
    }

    private fun userJson(user: UserAccount) = JSONObject().apply {
        put("name", user.name); put("email", user.email); put("role", user.role.name)
        put("plan", user.plan); put("username", user.username); put("firstName", user.firstName); put("lastName", user.lastName)
    }

    private fun parseStoredUser(value: JSONObject) = UserAccount(
        name = value.optString("name"),
        email = value.getString("email"),
        role = AccountRole.valueOf(value.optString("role", AccountRole.CLIENT.name)),
        plan = value.optString("plan", "Quantum"),
        username = value.optString("username"),
        firstName = value.optString("firstName"),
        lastName = value.optString("lastName"),
    )
}
