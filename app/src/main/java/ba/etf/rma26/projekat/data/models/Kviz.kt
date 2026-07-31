package ba.etf.rma26.projekat.data.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

enum class KvizStatus { PLAVA, ZELENA, ZUTA, CRVENA }

data class Kviz(
    @SerializedName("id") val id: Int,
    @SerializedName("naziv") val naziv: String,
    @SerializedName("idGrupe") val idGrupe: Int,
    @SerializedName("datumPocetak") val datumPocetak: String?,
    @SerializedName("datumKraj") val datumKraj: String?,
    @SerializedName("datumRada") val datumRada: String?,
    @SerializedName("trajanje") val trajanje: Int,
    @SerializedName("osvojeniBodovi") val osvojeniBodovi: Int?,

    var nazivPredmeta: String = "",
    var nazivGrupe: String = ""
)

fun odrediStatus(kviz: Kviz, ref: LocalDate): KvizStatus {
    if (kviz.datumPocetak == null || kviz.datumPocetak.length < 10 ||
        kviz.datumKraj == null || kviz.datumKraj.length < 10
    ) {
        return KvizStatus.CRVENA
    }

    val pocetak = LocalDate.parse(kviz.datumPocetak.substring(0, 10))
    val kraj = LocalDate.parse(kviz.datumKraj.substring(0, 10))

    val rad = if (kviz.datumRada != null && kviz.datumRada.length >= 10) {
        LocalDate.parse(kviz.datumRada.substring(0, 10))
    } else {
        null
    }

    return when {
        rad != null -> KvizStatus.PLAVA
        ref.isBefore(pocetak) -> KvizStatus.ZUTA
        ref.isAfter(kraj) -> KvizStatus.CRVENA
        else -> KvizStatus.ZELENA
    }
}