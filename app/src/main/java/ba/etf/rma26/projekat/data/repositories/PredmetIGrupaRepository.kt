package ba.etf.rma26.projekat.data.repositories

import android.util.Log
import ba.etf.rma26.projekat.data.models.Grupa
import ba.etf.rma26.projekat.data.models.Predmet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PredmetIGrupaRepository {

    suspend fun getPredmeti(): List<Predmet> = withContext(Dispatchers.IO) {
        try {
            val response = ApiConfig.service.getPredmeti()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getGrupe(): List<Grupa> = withContext(Dispatchers.IO) {
        try {
            val response = ApiConfig.service.getGrupe()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getGrupeZaPredmet(idPredmeta: Int): List<Grupa> = withContext(Dispatchers.IO) {
        try {
            val response = ApiConfig.service.getGrupeZaPredmet(idPredmeta)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun upisiUGrupu(idGrupa: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val hash = AccountRepository.getHash()
            val response = ApiConfig.service.upisiUGrupu(hash, idGrupa, ApiConfig.apiKey)
            response.isSuccessful && response.body() == true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUpisaneGrupe(): List<Grupa> = withContext(Dispatchers.IO) {
        try {
            val hash = AccountRepository.getHash()
            val response = ApiConfig.service.getUpisaneGrupe(hash)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}