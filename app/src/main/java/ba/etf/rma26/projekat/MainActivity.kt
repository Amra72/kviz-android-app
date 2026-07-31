package ba.etf.rma26.projekat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ba.etf.rma26.projekat.data.repositories.AccountRepository
import ba.etf.rma26.projekat.data.repositories.ApiConfig
import ba.etf.rma26.projekat.navigation.KvizNavGraph
import ba.etf.rma26.projekat.ui.theme.RMA26P19721Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AccountRepository.initialize(applicationContext)
        AccountRepository.postaviHashSinhrono("demo")
       ApiConfig.postaviBaseURL("http://10.0.2.2:3000")
        ApiConfig.postaviApiKey(null)
        enableEdgeToEdge()
        setContent {
            RMA26P19721Theme {
                KvizNavGraph()
            }
        }
    }
}