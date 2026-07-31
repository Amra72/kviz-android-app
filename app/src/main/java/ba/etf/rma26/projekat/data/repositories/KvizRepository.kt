package ba.etf.rma26.projekat.data.repositories
import ba.etf.rma26.projekat.data.models.Kviz
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object KvizRepository {

    private suspend fun obogatiKvizoveNazivima(kvizovi: List<Kviz>): List<Kviz> {
        val predmeti = PredmetIGrupaRepository.getPredmeti()
        val grupe = PredmetIGrupaRepository.getGrupe()

        kvizovi.forEach { kviz ->
            val grupa = grupe.find { it.id == kviz.idGrupe }
            val predmet = predmeti.find { it.id == grupa?.idPredmeta }
            kviz.nazivGrupe = grupa?.naziv ?: ""
            kviz.nazivPredmeta = predmet?.naziv ?: ""
        }
        return kvizovi
    }

    suspend fun getAll(): List<Kviz> = withContext(Dispatchers.IO) {
        try {
            val response = ApiConfig.service.getAllKvizovi()
            if (response.isSuccessful) {
                obogatiKvizoveNazivima(response.body() ?: emptyList())
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getById(id: Int): Kviz? = withContext(Dispatchers.IO) {
        try {
            val response = ApiConfig.service.getKvizById(id)
            if (response.isSuccessful) {
                response.body()?.let { obogatiKvizoveNazivima(listOf(it)).firstOrNull() }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUpisani(): List<Kviz> = withContext(Dispatchers.IO) {
        try {
            val hash = AccountRepository.getHash()
            val response =
                ApiConfig.service.getKvizoviZaStudenta(hash)

            if (response.isSuccessful) {
                response.body()
                    ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}