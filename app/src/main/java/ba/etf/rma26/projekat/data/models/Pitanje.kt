package ba.etf.rma26.projekat.data.models

import com.google.gson.annotations.SerializedName

data class Pitanje(
    @SerializedName("id") val id: Int,
    @SerializedName("idKviza") val idKviza: Int,
    @SerializedName("naziv") val naziv: String = "",
    @SerializedName("tekstPitanja") val tekstPitanja: String,
    @SerializedName("opcije") val odgovori: List<String>,
    @SerializedName("tacan") val tacan: Int = 0
)