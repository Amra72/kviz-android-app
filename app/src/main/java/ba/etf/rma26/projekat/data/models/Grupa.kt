package ba.etf.rma26.projekat.data.models

import com.google.gson.annotations.SerializedName

data class Grupa(
    @SerializedName("id") val id: Int,
    @SerializedName("naziv") val naziv: String,
    @SerializedName("idPredmeta") val idPredmeta: Int
)