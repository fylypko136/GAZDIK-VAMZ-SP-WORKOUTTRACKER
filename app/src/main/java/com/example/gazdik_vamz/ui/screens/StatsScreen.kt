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
import com.example.gazdik_vamz.data.local.entity.Exercise
import com.example.gazdik_vamz.data.local.entity.SessionSetCount
import com.example.gazdik_vamz.data.local.entity.WeightProgressEntry
import com.example.gazdik_vamz.data.local.entity.WorkoutSession
import com.example.gazdik_vamz.viewmodel.ChartType
import com.example.gazdik_vamz.viewmodel.StatsViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.*

/** Obrazovka štatistík */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController) {
    val context = LocalContext.current
    val repo = (context.applicationContext as WorkoutApp).repository
    val vm: StatsViewModel = viewModel(factory = StatsViewModel.Factory(repo))

    val totalSessions by vm.totalSessions.collectAsStateWithLifecycle()
    val sessionsThisWeek by vm.sessionsThisWeek.collectAsStateWithLifecycle()
    val sessionsThisMonth by vm.sessionsThisMonth.collectAsStateWithLifecycle()
    val allSessions by vm.allSessions.collectAsStateWithLifecycle()
    val sessionSetCounts by vm.sessionSetCounts.collectAsStateWithLifecycle()
    val chartType by vm.chartType.collectAsStateWithLifecycle()
    val allExercises by vm.allExercises.collectAsStateWithLifecycle()
    val selectedExercise by vm.selectedExercise.collectAsStateWithLifecycle()
    val weightProgress by vm.weightProgress.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title)) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Tri  karty v jednom riadku (Celkovo / Tento týždeň / Tento mesiac)
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(stringResource(R.string.stats_total), "$totalSessions",
                        stringResource(R.string.stats_sessions_unit), modifier = Modifier.weight(1f))
                    StatCard(stringResource(R.string.stats_this_week), "${sessionsThisWeek.size}",
                        stringResource(R.string.stats_sessions_unit), modifier = Modifier.weight(1f))
                    StatCard(stringResource(R.string.stats_this_month), "${sessionsThisMonth.size}",
                        stringResource(R.string.stats_sessions_unit), modifier = Modifier.weight(1f))
                }
            }

            val avgDuration = allSessions.mapNotNull { it.durationSeconds }.let { durations ->
                if (durations.isNotEmpty()) durations.average().toLong() else null
            }
            val totalDuration = allSessions.mapNotNull { it.durationSeconds }.sum()

            // Karta s časovými štatistikami
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.stats_time_card),
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.stats_total_time),
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text(formatDuration(totalDuration),
                                    style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.stats_avg_time),
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text(
                                    if (avgDuration != null) formatDuration(avgDuration)
                                    else stringResource(R.string.stats_no_data),
                                    style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Graf posledných 7 tréningov
            if (allSessions.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (chartType == ChartType.DURATION)
                                        stringResource(R.string.stats_chart_title_duration)
                                    else
                                        stringResource(R.string.stats_chart_title_sets),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                FilledTonalIconButton(onClick = { vm.toggleChartType() }) {
                                    Icon(
                                        imageVector = if (chartType == ChartType.DURATION)
                                            Icons.Default.FitnessCenter
                                        else
                                            Icons.Default.Timer,
                                        contentDescription = stringResource(R.string.stats_chart_toggle_cd)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            if (chartType == ChartType.DURATION) {
                                WorkoutDurationChart(sessions = allSessions.take(7).reversed())
                            } else {
                                WorkoutSetsChart(sessionSetCounts = sessionSetCounts.take(7).reversed())
                            }
                        }
                    }
                }
            }

            // Sekcia progresu váhy
            item {
                WeightProgressionSection(
                    exercises = allExercises,
                    selectedExercise = selectedExercise,
                    weightProgress = weightProgress,
                    onExerciseSelected = { vm.selectExercise(it) }
                )
            }

            // Zoznam 5 posledných tréningov
            if (allSessions.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.stats_recent),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(allSessions.take(5)) { session ->
                    RecentSessionRow(session)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightProgressionSection(
    exercises: List<Exercise>,
    selectedExercise: Exercise?,
    weightProgress: List<WeightProgressEntry>,
    onExerciseSelected: (Exercise?) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.stats_weight_progress_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedExercise?.name ?: stringResource(R.string.stats_pick_exercise),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    if (exercises.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.stats_no_exercises)) },
                            onClick = { dropdownExpanded = false }
                        )
                    } else {
                        exercises.forEach { exercise ->
                            DropdownMenuItem(
                                text = { Text(exercise.name) },
                                onClick = {
                                    onExerciseSelected(exercise)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Graf sa zobrazí len keď je vybraný cvik
            if (selectedExercise != null) {
                Spacer(Modifier.height(12.dp))
                if (weightProgress.isEmpty()) {
                    // Cvik bol vybraný ale nemá žiadne záznamy
                    Text(
                        stringResource(R.string.stats_no_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    WeightProgressionChart(weightProgress = weightProgress)
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}


@Composable
private fun WorkoutDurationChart(sessions: List<WorkoutSession>) {
    val dateFormat = remember { SimpleDateFormat("d.M", Locale.getDefault()) }
    val entries = remember(sessions) {
        sessions.mapIndexed { i, s ->
            FloatEntry(x = i.toFloat(), y = (s.durationSeconds ?: 0L).toFloat() / 60f)
        }
    }
    if (entries.isEmpty()) return
    val model = remember(entries) { entryModelOf(entries) }

    Chart(
        chart = columnChart(),
        model = model,
        modifier = Modifier.fillMaxWidth().height(160.dp),
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { value, _ ->
                sessions.getOrNull(value.toInt())?.startTime
                    ?.let { dateFormat.format(Date(it)) } ?: ""
            }
        )
    )
}


@Composable
private fun WorkoutSetsChart(sessionSetCounts: List<SessionSetCount>) {
    val dateFormat = remember { SimpleDateFormat("d.M", Locale.getDefault()) }
    val entries = remember(sessionSetCounts) {
        sessionSetCounts.mapIndexed { i, s ->
            FloatEntry(x = i.toFloat(), y = s.setCount.toFloat())
        }
    }
    if (entries.isEmpty()) return
    val model = remember(entries) { entryModelOf(entries) }

    Chart(
        chart = columnChart(),
        model = model,
        modifier = Modifier.fillMaxWidth().height(160.dp),
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { value, _ ->
                sessionSetCounts.getOrNull(value.toInt())?.startTime
                    ?.let { dateFormat.format(Date(it)) } ?: ""
            }
        )
    )
}


@Composable
private fun WeightProgressionChart(weightProgress: List<WeightProgressEntry>) {
    val dateFormat = remember { SimpleDateFormat("d.M", Locale.getDefault()) }
    val entries = remember(weightProgress) {
        weightProgress.mapIndexed { i, entry ->
            FloatEntry(x = i.toFloat(), y = entry.maxWeight)
        }
    }
    if (entries.isEmpty()) return
    val model = remember(entries) { entryModelOf(entries) }

    Chart(
        chart = columnChart(),
        model = model,
        modifier = Modifier.fillMaxWidth().height(160.dp),
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { value, _ ->
                weightProgress.getOrNull(value.toInt())?.startTime
                    ?.let { dateFormat.format(Date(it)) } ?: ""
            }
        )
    )
}

@Composable
private fun RecentSessionRow(session: WorkoutSession) {
    val dateFormat = remember { SimpleDateFormat("d. M. yyyy", Locale.getDefault()) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.FitnessCenter, contentDescription = null,
            modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(session.name, style = MaterialTheme.typography.bodyMedium)
            Text(dateFormat.format(Date(session.startTime)),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
        session.durationSeconds?.let {
            Text(formatDuration(it), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline)
        }
    }
}
