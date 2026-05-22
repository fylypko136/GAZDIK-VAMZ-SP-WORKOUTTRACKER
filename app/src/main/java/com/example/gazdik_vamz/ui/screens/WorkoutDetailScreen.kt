package com.example.gazdik_vamz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gazdik_vamz.R
import com.example.gazdik_vamz.WorkoutApp
import com.example.gazdik_vamz.data.local.entity.ExerciseSet
import com.example.gazdik_vamz.viewmodel.WorkoutDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

/** Obrazovka detailu dokončeného tréningu. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(navController: NavController, sessionId: Long) {
    val context = LocalContext.current
    val repo = (context.applicationContext as WorkoutApp).repository
    val vm: WorkoutDetailViewModel = viewModel(factory = WorkoutDetailViewModel.Factory(repo, sessionId))

    val session by vm.session.collectAsStateWithLifecycle()
    val sets by vm.sets.collectAsStateWithLifecycle()

    val dateFormat = remember { SimpleDateFormat("d. M. yyyy, HH:mm", Locale.getDefault()) }

    // Zoskupenie sérií podľa názvu cviku
    val grouped = remember(sets) { sets.groupBy { it.exerciseName } }
    // Celkový objem = súčet (opakovania × váha)
    val totalVolume = remember(sets) { sets.sumOf { (it.reps * it.weightKg).toDouble() }.toFloat() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.name ?: stringResource(R.string.detail_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                session?.let { s ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Dátum a čas začiatku
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.CalendarToday, null,
                                    modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(dateFormat.format(Date(s.startTime)),
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                            s.durationSeconds?.let { dur ->
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Timer, null,
                                        modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Text(formatDuration(dur), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            if (totalVolume > 0f) {
                                Spacer(Modifier.height(8.dp))
                                Divider()
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.FitnessCenter, null,
                                        modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Column {
                                        Text(stringResource(R.string.detail_volume_label),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline)
                                        Text(
                                            "%.1f kg".format(totalVolume),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            if (s.notes.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(s.notes, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }

            grouped.forEach { (exerciseName, exerciseSets) ->
                item {
                    ExerciseSetGroup(exerciseName = exerciseName, sets = exerciseSets)
                }
            }

            if (grouped.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.detail_no_sets),
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun ExerciseSetGroup(exerciseName: String, sets: List<ExerciseSet>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(exerciseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            // Hlavička tabuľky
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.set_header), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.reps_header), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.weight_header), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            sets.sortedBy { it.setNumber }.forEach { set ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Text("${set.setNumber}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text("${set.reps}x", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text("${set.weightKg} kg", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
