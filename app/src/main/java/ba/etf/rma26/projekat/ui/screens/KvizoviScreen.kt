package ba.etf.rma26.projekat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ba.etf.rma26.projekat.ui.components.KvizCard
import ba.etf.rma26.projekat.viewmodel.KvizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KvizoviScreen(viewModel: KvizViewModel, onKvizSelected: () -> Unit, onBack: () -> Unit) {

    LaunchedEffect(Unit) {
        viewModel.azurirajPrikazKvizovaIBroj()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = viewModel.odabraniFilter) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("listaKvizova"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(viewModel.filtriraniKvizovi) { kviz ->
                KvizCard(
                    kviz = kviz,
                    refDatum = viewModel.referentniDatum,
                    onKvizClick = {
                        viewModel.otvoriIZapocniKviz(kviz) {
                            onKvizSelected()
                        }
                    }
                )
            }
        }
    }
}