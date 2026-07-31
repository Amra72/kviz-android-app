package ba.etf.rma26.projekat.data.repositories

import ba.etf.rma26.projekat.data.models.Odgovor
import ba.etf.rma26.projekat.data.models.OdgovorRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object OdgovorRepository {
    suspend fun getOdgovoriKviz(idKviza: Int): List<Odgovor> = withContext(Dispatchers.IO) {
        try {
            val hash = AccountRepository.getHash()
            val response = ApiConfig.service.getOdgovoriKviz(hash, idKviza)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun postaviOdgovorKviz(idKvizTaken: Int, idPitanje: Int, odgovor: Int): Int = withContext(Dispatchers.IO) {
        try {
            val hash = AccountRepository.getHash()
            val requestBody = OdgovorRequest(idPitanje = idPitanje, odgovor = odgovor)

            val response = ApiConfig.service.postaviOdgovorKviz(hash, idKvizTaken, ApiConfig.apiKey, requestBody)

            if (response.isSuccessful && response.body() != null) {
                val stringOdgovor = response.body()!!.string().trim()
                stringOdgovor.toInt()
            } else {
                -1
            }
        } catch (e: Exception) {
            -1
        }
    }
}