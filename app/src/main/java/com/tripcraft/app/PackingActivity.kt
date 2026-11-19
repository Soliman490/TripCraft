package com.tripcraft.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.tripcraft.app.ui.theme.TripCraftTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.tripcraft.app.data.journal.TripCraftDatabase

private val MistBackground = Color(0xFFF5F7FA)
private val CardSage = Color(0xFFE8F5E9)
private val CardSand = Color(0xFFFFF8E1)

class PackingActivity : ComponentActivity() {
    private var list by mutableStateOf(listOf<PackingItem>())
    private var tripId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        tripId = intent.getIntExtra("ID", 0)
        runDb {}

        setContent {
            TripCraftTheme {
                Scaffold(containerColor = MistBackground) { pad ->
                    var name by rememberSaveable { mutableStateOf("") }
                    var category by rememberSaveable { mutableStateOf("Clothes") }
                    Column(
                        Modifier
                            .padding(pad)
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HeaderBar(
                            title = "Packing (${list.count { it.packed }}/${list.size})",
                            onBack = { finish() }
                        ) {
                            Button(onClick = {
                                val msg = "Still to pack:\n" + list.filter { !it.packed }
                                    .joinToString("\n") { "- [${it.category}] ${it.name}" }
                                startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND)
                                            .setType("text/plain")
                                            .putExtra(Intent.EXTRA_TEXT, msg),
                                        "Share List"
                                    )
                                )
                            }) { Text("Share") }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Item") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Category") },
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = {
                                if (name.isNotBlank()) {
                                    runDb {
                                        it.packingDao().insert(
                                            PackingItem(
                                                tripId = tripId,
                                                name = name.trim(),
                                                category = category.trim()
                                            )
                                        )
                                    }
                                    name = ""
                                }
                            }) { Text("Add") }
                        }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(list, key = { it.id }) { item ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (item.packed) CardSage else CardSand
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            runDb {
                                                it.packingDao().update(item.copy(packed = !item.packed))
                                            }
                                        }
                                ) {
                                    Row(
                                        Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "[${item.category}] ${item.name}",
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = if (item.packed) TextDecoration.LineThrough else null,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = {
                                            runDb { it.packingDao().delete(item) }
                                        }) { Text("Delete", color = Color.Red) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun runDb(block: suspend (TripCraftDatabase) -> Unit) {
        val db = TripCraftDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            block(db)
            val updated = db.packingDao().getForTrip(tripId)
            withContext(Dispatchers.Main) { list = updated }
        }
    }
}

@Composable
private fun HeaderBar(
    title: String,
    onBack: () -> Unit,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(onClick = onBack) { Text("Back") }
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        action()
    }
}