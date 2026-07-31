package ba.etf.rma26.projekat.data.models

import com.google.gson.annotations.SerializedName

data class Odgovor(
    @SerializedName("id") val id: Int,
    @SerializedName("idKvizTaken") val idKvizTaken: Int,
    @SerializedName("idPitanje") val idPitanje: Int,
    @SerializedName("odgovor") val odgovor: Int
)
