package ba.etf.rma26.projekat.navigation


import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ba.etf.rma26.projekat.ui.screens.FilterScreen
import ba.etf.rma26.projekat.ui.screens.KvizoviScreen
import ba.etf.rma26.projekat.ui.screens.PitanjaScreen
import ba.etf.rma26.projekat.viewmodel.KvizViewModel

@Composable
fun KvizNavGraph() {
    val navController = rememberNavController()
    val viewModel: KvizViewModel = viewModel()

    NavHost(navController = navController, startDestination = "filter") {
        composable("filter") {
            FilterScreen(
                viewModel = viewModel,
                onPrikaziKvizoveClick = {
                    navController.navigate("kvizovi")
                }
            )
        }
        composable("kvizovi") {
            KvizoviScreen(
                viewModel = viewModel,
                onKvizSelected = {
                    navController.navigate("pitanja")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("pitanja") {
            PitanjaScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}