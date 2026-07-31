package ba.etf.rma26.projekat.data.repositories

import ba.etf.rma26.projekat.data.models.KvizTaken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TakeKvizRepository {
    suspend fun zapocniKviz(idKviza: Int): KvizTaken? = withContext(Dispatchers.IO) {
        try {
            val hash = AccountRepository.getHash()
            val response = ApiConfig.service.zapocniKviz(hash, idKviza, ApiConfig.apiKey)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPocetiKvizovi(): List<KvizTaken>? = withContext(Dispatchers.IO) {
        try {
            val hash = AccountRepository.getHash()
            val response = ApiConfig.service.getPocetiKvizovi(hash)
            if (response.isSuccessful) response.body() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}