package org.utl.proyectox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.utl.proyectox.model.Residuo
import org.utl.proyectox.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResiduoScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResiduoScreen() {

    var residuos by remember { mutableStateOf(listOf<Residuo>()) }

    LaunchedEffect(Unit) {
        RetrofitClient.instance.getResiduos()
            .enqueue(object : Callback<List<Residuo>> {

                override fun onResponse(
                    call: Call<List<Residuo>>,
                    response: Response<List<Residuo>>
                ) {
                    if (response.isSuccessful) {
                        residuos = response.body() ?: emptyList()
                    }
                }

                override fun onFailure(call: Call<List<Residuo>>, t: Throwable) {
                    println("Error: ${t.message}")
                }
            })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Lista de Residuos") }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(residuos) { residuo ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Tipo: ${residuo.tipo}")
                        Text(text = "Descripción: ${residuo.descripcion}")
                        Text(text = "Estado: ${residuo.estado}")
                    }
                }
            }
        }
    }
}
