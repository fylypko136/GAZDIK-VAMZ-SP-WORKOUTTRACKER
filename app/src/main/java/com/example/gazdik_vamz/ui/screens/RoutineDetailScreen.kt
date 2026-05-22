package com.example.gazdik_vamz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.gazdik_vamz.viewmodel.RoutineDetailViewModel

/** Obrazovka vytvorenia alebo úpravy tréningovej rutiny. */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(navController: NavController, routineId: Long) {
    val context = LocalContext.current
    val repo = (context.applicationContext as WorkoutApp).repository
    val vm: RoutineDetailViewModel = viewModel(factory = RoutineDetailViewModel.Factory(repo, routineId))

    val routine by vm.routine.collectAsStateWithLifecycle()
    val exercises by vm.exercises.collectAsStateWithLifecycle()
    val allExercises by vm.allExercises.collectAsStateWithLifecycle()

    var nameText by remember(routine) { mutableStateOf(routine?.name ?: "") }
    var descText by remember(routine) { mutableStateOf(routine?.description ?: "") }
    var savedId by remember { mutableStateOf(if (routineId > 0) routineId else -1L) }

    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showPickExerciseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (routineId > 0) stringResource(R.string.routine_edit_title) else stringResource(R.string.routine_new_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            vm.saveRoutine(nameText, descText) { id ->
                                savedId = id
                            }
                        },
                        enabled = nameText.isNotBlank()
                    ) { Text(stringResource(R.string.save)) }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text(stringResource(R.string.routine_name_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = descText,
                    onValueChange = { descText = it },
                    label = { Text(stringResource(R.string.routine_desc_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }

            if (savedId > 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.routine_exercises_header),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row {
                            TextButton(onClick = { showPickExerciseDialog = true }) {
                                Icon(Icons.Default.List, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.routine_pick_existing))
                            }
                            TextButton(onClick = { showAddExerciseDialog = true }) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.routine_add_new))
                            }
                        }
                    }
                }

                if (exercises.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.routine_no_exercises),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    itemsIndexed(exercises, key = { _, ex -> ex.id }) { idx, exercise ->
                        ExerciseItem(
                            exercise = exercise,
                            isFirst = idx == 0,
                            isLast = idx == exercises.lastIndex,
                            onMoveUp = { vm.moveExerciseUp(exercise) },
                            onMoveDown = { vm.moveExerciseDown(exercise) },
                            onRemove = { vm.removeExercise(exercise) }
                        )
                    }
                }
            } else {
                item {
                    Text(
                        stringResource(R.string.routine_save_first),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
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

    if (showPickExerciseDialog) {
        PickExerciseDialog(
            exercises = allExercises,
            onDismiss = { showPickExerciseDialog = false },
            onPick = { exercise ->
                vm.addExistingExercise(exercise)
                showPickExerciseDialog = false
            }
        )
    }
}

@Composable
private fun ExerciseItem(
    exercise: Exercise,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ReorderButton(onMoveUp, !isFirst, Icons.Default.KeyboardArrowUp,
                    stringResource(R.string.reorder_up_cd), MaterialTheme.colorScheme.primary)
                ReorderButton(onMoveDown, !isLast, Icons.Default.KeyboardArrowDown,
                    stringResource(R.string.reorder_down_cd), MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, style = MaterialTheme.typography.bodyLarge)
                if (exercise.category.isNotBlank()) {
                    Text(exercise.category, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.exercise_remove_cd),
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}


@Composable
fun AddExerciseDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_new_exercise)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.dialog_exercise_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(stringResource(R.string.dialog_exercise_category)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            // zabránenie vytvorenia cviku bez názvu
            TextButton(onClick = { onConfirm(name, category) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}


@Composable
fun PickExerciseDialog(
    exercises: List<Exercise>,
    onDismiss: () -> Unit,
    onPick: (Exercise) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_pick_exercise)) },
        text = {
            if (exercises.isEmpty()) {
                // Prázdna DB cvikov
                Text(stringResource(R.string.dialog_no_exercises))
            } else {
                LazyColumn {
                    items(exercises) { ex ->
                        TextButton(
                            onClick = { onPick(ex) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(ex.name, modifier = Modifier.weight(1f))
                            // Kategória sa zobrazí napravo ak existuje
                            if (ex.category.isNotBlank()) {
                                Text(ex.category, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
