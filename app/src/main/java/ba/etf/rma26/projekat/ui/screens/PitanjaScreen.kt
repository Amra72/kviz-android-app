package ba.etf.rma26.projekat.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ba.etf.rma26.projekat.viewmodel.KvizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PitanjaScreen(viewModel: KvizViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = viewModel.aktivniKviz?.naziv ?: "Pokušaj Kviza") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (viewModel.rezultatPoruka.isNotEmpty()) {
                Surface(
                    color = Color(0xFFE0F7FA),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("rezultat_poruka"),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = viewModel.rezultatPoruka,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
                }
            }

            if (viewModel.aktivnaPitanja.isEmpty()) {
                Text(
                    text = "Učitavanje pitanja ili kviz nema pitanja...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("prazna_pitanja_poruka")
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize().testTag("lista_pitanja")
                ) {
                    items(viewModel.aktivnaPitanja) { pitanje ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pitanje_kartica_${pitanje.id}"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = pitanje.tekstPitanja,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                pitanje.odgovori.forEachIndexed { indeks, odgovor ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                viewModel.odgovoriNaPitanje(pitanje.id, indeks)
                                            }
                                            .testTag("odgovor_${pitanje.id}_$indeks"),
                                        color = Color.White,
                                        shape = MaterialTheme.shapes.small,
                                        border = BorderStroke(1.dp, Color.LightGray)
                                    ) {
                                        Text(
                                            text = "${indeks + 1}. $odgovor",
                                            modifier = Modifier.padding(12.dp),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}