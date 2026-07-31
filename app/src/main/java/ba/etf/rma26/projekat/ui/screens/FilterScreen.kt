package ba.etf.rma26.projekat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ba.etf.rma26.projekat.viewmodel.KvizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(viewModel: KvizViewModel, onPrikaziKvizoveClick: () -> Unit) {

    var expGodina by remember { mutableStateOf(false) }
    var expPredmet by remember { mutableStateOf(false) }
    var expGrupa by remember { mutableStateOf(false) }
    var expFilter by remember { mutableStateOf(false) }

    val opcijeFiltera = listOf("Svi moji kvizovi", "Svi kvizovi", "Urađeni kvizovi", "Budući kvizovi", "Prošli kvizovi")

    LaunchedEffect(Unit) {
        viewModel.osvjeziInicijalnePodatke()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Upis na predmet", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expGodina,
            onExpandedChange = { expGodina = !expGodina },
            modifier = Modifier.fillMaxWidth().testTag("odabirGodina")
        ) {
            TextField(
                value = viewModel.odabranaGodina,
                onValueChange = {},
                readOnly = true,
                label = { Text("Godina") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expGodina) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expGodina, onDismissRequest = { expGodina = false }) {
                (1..5).forEach { godina ->
                    DropdownMenuItem(
                        text = { Text(godina.toString()) },
                        onClick = {
                            viewModel.odabranaGodina = godina.toString()
                            viewModel.odabraniPredmet = null
                            viewModel.odabranaGrupa = null
                            viewModel.azurirajPrikazKvizovaIBroj()
                            expGodina = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val filtriraniPredmeti = viewModel.sviPredmeti.filter { it.godina.toString() == viewModel.odabranaGodina }

        ExposedDropdownMenuBox(
            expanded = expPredmet,
            onExpandedChange = { if (viewModel.odabranaGodina.isNotEmpty()) expPredmet = !expPredmet },
            modifier = Modifier.fillMaxWidth().testTag("odabirPredmet")
        ) {
            TextField(
                value = viewModel.odabraniPredmet?.naziv ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Predmet") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expPredmet) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expPredmet, onDismissRequest = { expPredmet = false }) {
                filtriraniPredmeti.forEach { p ->
                    DropdownMenuItem(
                        text = { Text(p.naziv) },
                        onClick = {
                            viewModel.odabraniPredmet = p
                            viewModel.odabranaGrupa = null
                            viewModel.ucitajGrupeZaPredmet(p.id)
                            viewModel.azurirajPrikazKvizovaIBroj()
                            expPredmet = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expGrupa,
            onExpandedChange = { if (viewModel.odabraniPredmet != null) expGrupa = !expGrupa },
            modifier = Modifier.fillMaxWidth().testTag("odabirGrupa")
        ) {
            TextField(
                value = viewModel.odabranaGrupa?.naziv ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Grupa") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expGrupa) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expGrupa, onDismissRequest = { expGrupa = false }) {
                viewModel.sveGrupeZaOdabraniPredmet.forEach { g ->
                    DropdownMenuItem(
                        text = { Text(g.naziv) },
                        onClick = {
                            viewModel.odabranaGrupa = g
                            viewModel.azurirajPrikazKvizovaIBroj()
                            expGrupa = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.upisiKorisnika() },
            enabled = viewModel.odabranaGodina.isNotEmpty() && viewModel.odabraniPredmet != null && viewModel.odabranaGrupa != null,
            modifier = Modifier.fillMaxWidth().testTag("dodajPredmetDugme")
        ) {
            Text("Upiši me")
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Filteri", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expFilter,
            onExpandedChange = { expFilter = !expFilter },
            modifier = Modifier.fillMaxWidth().testTag("filterKvizova")
        ) {
            TextField(
                value = viewModel.odabraniFilter,
                onValueChange = {},
                readOnly = true,
                label = { Text("Odaberi filter") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expFilter) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expFilter, onDismissRequest = { expFilter = false }) {
                opcijeFiltera.forEach { opcija ->
                    DropdownMenuItem(
                        text = { Text(opcija) },
                        onClick = {
                            viewModel.odabraniFilter = opcija
                            viewModel.azurirajPrikazKvizovaIBroj()
                            expFilter = false
                        }
                    )
                }
            }
        }

        Row(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(text = "Pronađeno je ", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${viewModel.brojKvizovaZaPrikaz}",
                modifier = Modifier.testTag("brojKvizova"),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(text = " kvizova", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onPrikaziKvizoveClick,
            modifier = Modifier.fillMaxWidth().testTag("prikaziKvizoveDugme")
        ) {
            Text("Prikaži kvizove")
        }
    }
}