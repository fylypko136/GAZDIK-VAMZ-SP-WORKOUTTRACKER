package com.example.gazdik_vamz.ui.screens

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.example.gazdik_vamz.R
import com.example.gazdik_vamz.WorkoutApp
import com.example.gazdik_vamz.data.local.entity.Exercise
import com.example.gazdik_vamz.data.local.entity.ExerciseSet
import com.example.gazdik_vamz.ui.navigation.Screen
import com.example.gazdik_vamz.viewmodel.ActiveExerciseState
import com.example.gazdik_vamz.viewmodel.ActiveWorkoutViewModel

/** Obrazovka aktívneho tréningu.*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(navController: NavController, routineId: Long, existingSessionId: Long) {
    val context = LocalContext.current
    val app = context.applicationContext as WorkoutApp

    val vm: ActiveWorkoutViewModel = viewModel(
        factory = ActiveWorkoutViewModel.Factory(app.repository, app.activeSessionPrefs, routineId, existingSessionId),
        key = "workout_${routineId}_${existingSessionId}"
    )

    val exercises by vm.exercises.collectAsStateWithLifecycle()
    val workoutName by vm.workoutName.collectAsStateWithLifecycle()
    val restTimerSeconds by vm.restTimerSeconds.collectAsStateWithLifecycle()
    val restTimerRunning by vm.restTimerRunning.collectAsStateWithLifecycle()

    val allExercises by app.repository.getAllExercises()
        .collectAsStateWithLifecycle(initialValue = emptyList<Exercise>())

    var showFinishDialog by remember { mutableStateOf(false) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showPickExerciseFromAll by remember { mutableStateOf(false) }
    var finishVolume by remember { mutableStateOf(0f) }
    var finishSessionId by remember { mutableStateOf(0L) }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        // Požiadame o povolenie notifikácií len ak ho ešte nemáme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        // Callback z ViewModelu — keď časovač dobeží, zobrazíme systémovú notifikáciu
        vm.onRestTimerFinished = { showRestDoneNotification(context) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workoutName) },
                navigationIcon = {
                    // Šípka späť funguje rovnako ako tlačidlo Dokončiť —
                    // uloží tréning a zobrazí výsledkový dialóg
                    IconButton(onClick = {
                        vm.finishWorkout { sid, vol ->
                            finishSessionId = sid
                            finishVolume = vol
                            showFinishDialog = true
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    TextButton(onClick = {
                        vm.finishWorkout { sid, vol ->
                            finishSessionId = sid
                            finishVolume = vol
                            showFinishDialog = true
                        }
                    }) {
                        Text(stringResource(R.string.workout_finish), color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Lišta odpočinkového časovača — animovane sa zobrazí/skryje
            AnimatedVisibility(visible = restTimerRunning || restTimerSeconds > 0) {
                RestTimerBar(
                    seconds = restTimerSeconds,
                    running = restTimerRunning,
                    onStop = { vm.stopRestTimer() }
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                itemsIndexed(exercises) { index, exerciseState ->
                    ActiveExerciseCard(
                        exerciseState = exerciseState,
                        isFirst = index == 0,
                        isLast = index == exercises.lastIndex,
                        onMoveUp = { vm.moveExerciseUp(index) },
                        onMoveDown = { vm.moveExerciseDown(index) },
                        onAddSet = { reps, weight -> vm.addSet(index, reps, weight) },
                        onRemoveSet = { set -> vm.removeSet(index, set) },
                        onDismissProgress = { vm.clearProgressMessage(index) }
                    )
                }

                // Tlačidlá na pridanie cviku — existujúci zo zoznamu alebo nový
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showPickExerciseFromAll = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.List, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.workout_pick_existing))
                        }
                        OutlinedButton(
                            onClick = { showAddExerciseDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.workout_add_new))
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }

            // Panel rýchleho spustenia časovača odpočinku.
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.rest_timer_card_title), style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            60 to stringResource(R.string.rest_1min),
                            90 to stringResource(R.string.rest_1min30),
                            120 to stringResource(R.string.rest_2min),
                            180 to stringResource(R.string.rest_3min)
                        ).forEach { (secs, label) ->
                            OutlinedButton(
                                onClick = { vm.startRestTimer(secs) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) { Text(label, style = MaterialTheme.typography.labelMedium) }
                        }
                    }
                }
            }
        }
    }

    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { /* zámerné — používateľ musí kliknúť na tlačidlo */ },
            title = { Text(stringResource(R.string.workout_done_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Objem sa zobrazí len ak boli zaznamenané nejaké série s váhou
                    if (finishVolume > 0f) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    stringResource(R.string.workout_done_volume_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "%.1f kg".format(finishVolume),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    Text(stringResource(R.string.workout_done_saved))
                }
            },
            confirmButton = {
                Button(onClick = {
                    showFinishDialog = false
                    navController.navigate(
                        Screen.WorkoutDetail.createRoute(finishSessionId),
                        navOptions { popUpTo(Screen.Home.route) }
                    )
                }) { Text(stringResource(R.string.workout_done_view_detail)) }
            },
            dismissButton = {
                // Domov — vráti na HomeScreen a vymaže celý zásobník navigácie
                TextButton(onClick = {
                    showFinishDialog = false
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }) { Text(stringResource(R.string.workout_done_home)) }
            }
        )
    }

    if (showAddExerciseDialog) {
        AddExerciseDialog(
            onDismiss = { showAddExerciseDialog = false },
            onConfirm = { name, category ->
                vm.addNewExercise(name, category)
                showAddExerciseDialog = false
            }
        )
    }

    if (showPickExerciseFromAll) {
        PickExerciseDialog(
            exercises = allExercises,
            onDismiss = { showPickExerciseFromAll = false },
            onPick = { exercise ->
                vm.addExercise(exercise)
                showPickExerciseFromAll = false
            }
        )
    }
}

/** Animovaná lišta zobrazujúca zostatok odpočinkového časovača. */
@Composable
private fun RestTimerBar(seconds: Int, running: Boolean, onStop: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.width(8.dp))
            Text(
                // Ak časovač beží — zobrazí zostatok, inak "Hotovo! ✓"
                text = if (running)
                    stringResource(R.string.rest_timer_running, formatDuration(seconds.toLong()))
                else
                    stringResource(R.string.rest_timer_done),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onStop) {
                Icon(Icons.Default.Close, stringResource(R.string.rest_timer_stop_cd), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

/**
 * Karta jedného cviku počas aktívneho tréningu.
 * Zobrazuje zaznamenané série, hodnoty z predchádzajúcej session-y.
 */
@Composable
private fun ActiveExerciseCard(
    exerciseState: ActiveExerciseState,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onAddSet: (Int, Float) -> Unit,
    onRemoveSet: (ExerciseSet) -> Unit,
    onDismissProgress: () -> Unit
) {
    var repsText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }

    // Motivačná správa sa automaticky skryje po 4 sekundách
    val progressMsg = exerciseState.progressMessage
    LaunchedEffect(progressMsg) {
        if (progressMsg != null) {
            kotlinx.coroutines.delay(4000)
            onDismissProgress()
        }
    }

    // Najlepšie hodnoty z predchádzajúcej session
    val lastBestWeight = exerciseState.lastSessionSets.maxOfOrNull { it.weightKg }
    val lastBestReps = exerciseState.lastSessionSets.maxOfOrNull { it.reps }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(exerciseState.exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (exerciseState.exercise.category.isNotBlank()) {
                        Text(
                            exerciseState.exercise.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                // Tlačidlá presunu sú zakázané na krajných pozíciách
                ReorderButton(onMoveUp, !isFirst, Icons.Default.KeyboardArrowUp,
                    stringResource(R.string.reorder_up_cd), MaterialTheme.colorScheme.onSurfaceVariant)
                ReorderButton(onMoveDown, !isLast, Icons.Default.KeyboardArrowDown,
                    stringResource(R.string.reorder_down_cd), MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Referencia z minulej session
            if (lastBestWeight != null || lastBestReps != null) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.History, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                    Text(
                        buildString {
                            append(stringResource(R.string.last_session_label))
                            if (lastBestWeight != null) append("${lastBestWeight}kg")
                            if (lastBestWeight != null && lastBestReps != null) append(" × ")
                            if (lastBestReps != null) append("${lastBestReps}x")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Motivačná správa pri prekonaní osobného rekordu — animovane sa objaví/skryje
            AnimatedVisibility(visible = progressMsg != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text(
                        progressMsg ?: "",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Tabuľka zaznamenaných sérií — zobrazí sa len ak sú nejaké
            if (exerciseState.sets.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.set_header), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                    Text(stringResource(R.string.reps_header), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                    Text(stringResource(R.string.weight_header), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(40.dp))
                }
                // Riadky sérií — každá séria má tlačidlo na zmazanie
                exerciseState.sets.forEach { set ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${set.setNumber}", modifier = Modifier.weight(1f))
                        Text("${set.reps}x", modifier = Modifier.weight(1f))
                        Text("${set.weightKg}", modifier = Modifier.weight(1f))
                        IconButton(onClick = { onRemoveSet(set) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = repsText,
                    // "Len číslice"
                    onValueChange = { repsText = it.filter { c -> c.isDigit() } },
                    // Minulá hodnota ak existuje
                    label = {
                        Text(
                            if (lastBestReps != null)
                                stringResource(R.string.reps_hint_prev, lastBestReps)
                            else
                                stringResource(R.string.reps_hint)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = {
                        Text(
                            if (lastBestWeight != null)
                                stringResource(R.string.weight_hint_prev, lastBestWeight)
                            else
                                stringResource(R.string.weight_hint)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Button(
                    onClick = {
                        val reps = repsText.toIntOrNull() ?: return@Button
                        val weight = weightText.toFloatOrNull() ?: 0f
                        onAddSet(reps, weight)
                        repsText = ""
                        weightText = ""
                    },
                    enabled = repsText.isNotBlank(),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) { Text(stringResource(R.string.add_set_button)) }
            }
        }
    }
}

/**
 * Odošle systémovú notifikáciu keď odpočinkový časovač dobeží.
 * Notifikácia sa zobrazí len ak má aplikácia potrebné povolenie.
 */
private fun showRestDoneNotification(context: Context) {
    val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    } else true

    if (!hasPermission) return

    // Kanál REST_TIMER_CHANNEL_ID je v WorkoutApp.createNotificationChannels()
    val notification = NotificationCompat.Builder(context, WorkoutApp.REST_TIMER_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(context.getString(R.string.notif_rest_title))
        .setContentText(context.getString(R.string.notif_rest_text))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(1001, notification)
}
