package ba.etf.rma26.projekat.data.models

import com.google.gson.annotations.SerializedName

data class OdgovorRequest(
    @SerializedName("idPitanje") val idPitanje: Int,
    @SerializedName("odgovor") val odgovor: Int
)

data class BodoviResponse(
    val ukupnoBodova: Int
)