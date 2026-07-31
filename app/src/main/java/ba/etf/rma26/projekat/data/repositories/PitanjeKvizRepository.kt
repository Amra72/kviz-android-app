package ba.etf.rma26.projekat.data.repositories


import ba.etf.rma26.projekat.data.models.Pitanje
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PitanjeKvizRepository {
    suspend fun getPitanja(idKviza: Int): List<Pitanje> = withContext(Dispatchers.IO) {
        try {
            val response = ApiConfig.service.getPitanjaZaKviz(idKviza)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}