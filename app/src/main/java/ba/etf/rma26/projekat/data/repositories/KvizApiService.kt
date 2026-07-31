package ba.etf.rma26.projekat.data.repositories

import ba.etf.rma26.projekat.data.models.Grupa
import ba.etf.rma26.projekat.data.models.Kviz
import ba.etf.rma26.projekat.data.models.KvizTaken
import ba.etf.rma26.projekat.data.models.Odgovor
import ba.etf.rma26.projekat.data.models.OdgovorRequest
import ba.etf.rma26.projekat.data.models.Pitanje
import ba.etf.rma26.projekat.data.models.Predmet
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface KvizApiService {
    @GET("predmet")
    suspend fun getPredmeti(): Response<List<Predmet>>

    @GET("grupa")
    suspend fun getGrupe(): Response<List<Grupa>>

    @GET("predmet/{id}/grupa")
    suspend fun getGrupeZaPredmet(@Path("id") idPredmeta: Int): Response<List<Grupa>>

    @POST("student/{hash}/grupa/{id}")
    suspend fun upisiUGrupu(
        @Path("hash") hash: String,
        @Path("id") idGrupa: Int,
        @Header("X-API-Key") apiKey: String?
    ): Response<Boolean>

    @GET("student/{hash}/grupa")
    suspend fun getUpisaneGrupe(@Path("hash") hash: String): Response<List<Grupa>>

    @GET("kviz")
    suspend fun getAllKvizovi(): Response<List<Kviz>>

    @GET("kviz/{id}")
    suspend fun getKvizById(@Path("id") id: Int): Response<Kviz>

    @GET("student/{hash}/kviz")
    suspend fun getKvizoviZaStudenta(@Path("hash") hash: String): Response<List<Kviz>>

    @GET("kviz/{id}/pitanja")
    suspend fun getPitanjaZaKviz(@Path("id") idKviza: Int): Response<List<Pitanje>>

    @POST("student/{hash}/kviz/{id}")
    suspend fun zapocniKviz(
        @Path("hash") hash: String,
        @Path("id") idKviza: Int,
        @Header("X-API-Key") apiKey: String?
    ): Response<KvizTaken>

    @GET("student/{hash}/kviz/{id}/odgovori")
    suspend fun getOdgovoriKviz(
        @Path("hash") hash: String,
        @Path("id") idKviza: Int
    ): Response<List<Odgovor>>

    @GET("student/{hash}/kviztaken")
    suspend fun getPocetiKvizovi(@Path("hash") hash: String): Response<List<KvizTaken>>

    @POST("student/{hash}/kviztaken/{id}/odgovor")
    suspend fun postaviOdgovorKviz(
        @Path("hash") hash: String,
        @Path("id") idKvizTaken: Int,
        @Header("X-API-Key") apiKey: String?,
        @Body body: OdgovorRequest
    ): Response<ResponseBody>
}