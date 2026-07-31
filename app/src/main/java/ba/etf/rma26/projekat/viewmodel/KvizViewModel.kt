package ba.etf.rma26.projekat.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.etf.rma26.projekat.data.models.Grupa
import ba.etf.rma26.projekat.data.models.Kviz
import ba.etf.rma26.projekat.data.models.KvizTaken
import ba.etf.rma26.projekat.data.models.Pitanje
import ba.etf.rma26.projekat.data.models.Predmet
import ba.etf.rma26.projekat.data.repositories.AccountRepository
import ba.etf.rma26.projekat.data.repositories.KvizRepository
import ba.etf.rma26.projekat.data.repositories.OdgovorRepository
import ba.etf.rma26.projekat.data.repositories.PitanjeKvizRepository
import ba.etf.rma26.projekat.data.repositories.PredmetIGrupaRepository
import ba.etf.rma26.projekat.data.repositories.TakeKvizRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class KvizViewModel : ViewModel() {

    val referentniDatum: LocalDate = LocalDate.of(2021, 4, 6)

    var odabranaGodina by mutableStateOf("")
    var odabraniPredmet by mutableStateOf<Predmet?>(null)
    var odabranaGrupa by mutableStateOf<Grupa?>(null)
    var odabraniFilter by mutableStateOf("Svi moji kvizovi")

    var sviPredmeti by mutableStateOf<List<Predmet>>(emptyList())
    var sveGrupeZaOdabraniPredmet by mutableStateOf<List<Grupa>>(emptyList())
    var filtriraniKvizovi by mutableStateOf<List<Kviz>>(emptyList())
    var brojKvizovaZaPrikaz by mutableStateOf(0)

    var aktivniKviz by mutableStateOf<Kviz?>(null)
    var aktivnaPitanja by mutableStateOf<List<Pitanje>>(emptyList())
    var trenutniPokusaj by mutableStateOf<KvizTaken?>(null)

    var rezultatPoruka by mutableStateOf("")

    init {
        viewModelScope.launch {
            AccountRepository.hash.collectLatest { noviHash ->
                osvjeziInicijalnePodatke()
            }
        }
    }

    fun osvjeziInicijalnePodatke() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val predmetiSaServera = PredmetIGrupaRepository.getPredmeti()
                withContext(Dispatchers.Main) {
                    sviPredmeti = predmetiSaServera
                }
                azurirajPrikazKvizovaIBroj()
            } catch (e: Exception) {
                Log.e("RMA_GREŠKA", "Greška pri inicijalizaciji podataka: ${e.message}")
            }
        }
    }

    fun ucitajGrupeZaPredmet(predmetId: Int) {
        viewModelScope.launch {
            sveGrupeZaOdabraniPredmet = PredmetIGrupaRepository.getGrupeZaPredmet(predmetId)
        }
    }

    fun azurirajPrikazKvizovaIBroj() {
        viewModelScope.launch {
            val sviKvizovi = KvizRepository.getAll()
            val upisaniKvizovi = KvizRepository.getUpisani()

            val rez = when (odabraniFilter) {
                "Svi moji kvizovi" -> upisaniKvizovi
                "Svi kvizovi" -> sviKvizovi
                "Urađeni kvizovi" -> upisaniKvizovi.filter { it.datumRada != null }
                "Budući kvizovi" -> upisaniKvizovi.filter {
                    val pDatum = pokusajParsiratiDatum(it.datumPocetak)
                    it.datumRada == null && pDatum != null && pDatum.isAfter(referentniDatum)
                }
                "Prošli kvizovi" -> upisaniKvizovi.filter {
                    val kDatum = pokusajParsiratiDatum(it.datumKraj)
                    it.datumRada == null && kDatum != null && kDatum.isBefore(referentniDatum)
                }
                else -> upisaniKvizovi
            }

            filtriraniKvizovi = rez.sortedBy { pokusajParsiratiDatum(it.datumPocetak) ?: LocalDate.MIN }
            brojKvizovaZaPrikaz = filtriraniKvizovi.size
        }
    }

    fun upisiKorisnika() {
        odabranaGrupa?.let { grupa ->
            viewModelScope.launch {
                val uspjeh = PredmetIGrupaRepository.upisiUGrupu(grupa.id)
                if (uspjeh) {
                    azurirajPrikazKvizovaIBroj()
                }
            }
        }
    }

    fun otvoriIZapocniKviz(kviz: Kviz, onSuccess: () -> Unit) {
        viewModelScope.launch {
            aktivniKviz = kviz
            rezultatPoruka = ""
            aktivnaPitanja = PitanjeKvizRepository.getPitanja(kviz.id)
            val pokusaj = TakeKvizRepository.zapocniKviz(kviz.id)

            if (pokusaj != null) {
                trenutniPokusaj = pokusaj
                onSuccess()
            } else {
                trenutniPokusaj = null
                rezultatPoruka = "Greška: Ne možete započeti ovaj kviz."
            }
        }
    }

    fun odgovoriNaPitanje(pitanjeId: Int, indeksOdgovora: Int) {
        trenutniPokusaj?.let { pokusaj ->
            viewModelScope.launch {
                val ukupnoBodova = OdgovorRepository.postaviOdgovorKviz(
                    idKvizTaken = pokusaj.id,
                    idPitanje = pitanjeId,
                    odgovor = indeksOdgovora
                )
                rezultatPoruka = if (ukupnoBodova != -1) {
                    "Odgovor zabilježen! Ukupno bodova: $ukupnoBodova"
                } else {
                    "Greška pri slanju odgovora."
                }
                azurirajPrikazKvizovaIBroj()
            }
        }
    }

    private fun pokusajParsiratiDatum(datumStr: String?): LocalDate? {
        if (datumStr == null) return null
        return try {
            if (datumStr.length >= 10) {
                LocalDate.parse(datumStr.substring(0, 10))
            } else {
                LocalDate.parse(datumStr)
            }
        } catch (e: Exception) {
            null
        }
    }
}