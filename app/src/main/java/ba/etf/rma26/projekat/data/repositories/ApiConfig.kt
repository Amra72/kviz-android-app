package ba.etf.rma26.projekat.data.repositories


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    private var baseUrl: String = "http://10.0.2.2:3000/"
    var apiKey: String? = null
        private set


    private var retrofitInstance: KvizApiService? = null

    fun postaviBaseURL(baseUrl: String) {
        this.baseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        retrofitInstance = null
    }

    fun postaviApiKey(apiKey: String?) {
        this.apiKey = apiKey
    }

    val service: KvizApiService
        get() {
            if (retrofitInstance == null) {
                retrofitInstance = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(KvizApiService::class.java)
            }
            return retrofitInstance!!
        }
}