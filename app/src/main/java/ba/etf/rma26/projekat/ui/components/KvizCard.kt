package ba.etf.rma26.projekat.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ba.etf.rma26.projekat.R
import ba.etf.rma26.projekat.data.models.Kviz
import ba.etf.rma26.projekat.data.models.KvizStatus
import ba.etf.rma26.projekat.data.models.odrediStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KvizCard(kviz: Kviz, refDatum: LocalDate, onKvizClick: () -> Unit) {
    val ulazniFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val izlazniFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    val status = odrediStatus(kviz, refDatum)

    val (slikaId, bojaOpis) = when (status) {
        KvizStatus.PLAVA -> R.drawable.plava to "Plava"
        KvizStatus.ZELENA -> R.drawable.zelena to "Zelena"
        KvizStatus.ZUTA -> R.drawable.zuta to "Žuta"
        KvizStatus.CRVENA -> R.drawable.crvena to "Crvena"
    }

    val datumZaPrikaz = when (status) {
        KvizStatus.PLAVA -> kviz.datumRada?.let { LocalDate.parse(it.substring(0,10), ulazniFormatter).format(izlazniFormatter) } ?: ""
        KvizStatus.ZUTA -> LocalDate.parse(kviz.datumPocetak?.substring(0,10), ulazniFormatter).format(izlazniFormatter)
        else -> LocalDate.parse(kviz.datumKraj?.substring(0,10), ulazniFormatter).format(izlazniFormatter)
    }

    Card(
        onClick = onKvizClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .testTag("kviz_item_${kviz.naziv}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB0BEC5))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = kviz.nazivPredmeta,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )
                Image(
                    painter = painterResource(id = slikaId),
                    contentDescription = bojaOpis,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .testTag("kviz_status_icon")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF8F9B9F)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = kviz.naziv, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text(text = datumZaPrikaz, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "${kviz.trajanje} min", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                        kviz.osvojeniBodovi?.let { bodovi ->
                            Text(text = bodovi.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}