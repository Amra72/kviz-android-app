package ba.etf.rma26.projekat.data.repositories

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

object AccountRepository {
    private const val PREFS_NAME = "RMA26_PREFS"
    private const val HASH_KEY = "student_hash"
    private var sharedPreferences: SharedPreferences? = null

    private val _hash = MutableStateFlow("demo")
    val hash: StateFlow<String> = _hash.asStateFlow()

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val trenutniHash = sharedPreferences?.getString(HASH_KEY, "demo") ?: "demo"
        _hash.value = trenutniHash
    }

    suspend fun getHash(): String = withContext(Dispatchers.IO) {
        _hash.value
    }

    suspend fun postaviHash(acHash: String): Boolean = withContext(Dispatchers.IO) {
        val prefs = sharedPreferences ?: return@withContext false
        if (acHash.isBlank()) return@withContext false

        val uspjeh = prefs.edit().putString(HASH_KEY, acHash).commit()
        if (uspjeh) {
            _hash.value = acHash
        }
        return@withContext uspjeh
    }

    fun postaviHashSinhrono(acHash: String): Boolean {
        val prefs = sharedPreferences ?: return false
        if (acHash.isBlank()) return false
        val uspjeh = prefs.edit().putString(HASH_KEY, acHash).commit()
        if (uspjeh) {
            _hash.value = acHash
        }
        return uspjeh
    }
}