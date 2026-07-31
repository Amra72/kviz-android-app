package ba.etf.rma26.projekat.data.models

import com.google.gson.annotations.SerializedName

data class KvizTaken(
    @SerializedName("id") val id: Int,
    @SerializedName("hash") val hash: String,
    @SerializedName("idKviza") val idKviza: Int,
    @SerializedName("datumRada") val datumRada: String?,
    @SerializedName("zavrsen") val zavrsen: Boolean,
    @SerializedName("osvojenBodovi") val osvojenBodovi: Int
)