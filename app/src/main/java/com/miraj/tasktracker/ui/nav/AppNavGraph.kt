package com.miraj.tasktracker.ui.nav

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.miraj.tasktracker.TaskTrackerApp
import com.miraj.tasktracker.ui.detail.TaskDetailScreen
import com.miraj.tasktracker.ui.detail.TaskDetailViewModel
import com.miraj.tasktracker.ui.edit.EditTaskScreen
import com.miraj.tasktracker.ui.edit.EditTaskViewModel
import com.miraj.tasktracker.ui.home.HomeScreen
import com.miraj.tasktracker.ui.home.HomeViewModel
import com.miraj.tasktracker.ui.settings.SettingsScreen
import com.miraj.tasktracker.ui.settings.SettingsViewModel
import androidx.compose.ui.platform.LocalContext

object Routes {
    const val HOME = "home"
    const val EDIT = "edit"
    const val EDIT_WITH_ID = "edit/{id}"
    const val DETAIL = "detail/{id}"
    const val SETTINGS = "settings"

    fun edit(id: Long) = "edit/$id"
    fun detail(id: Long) = "detail/$id"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    initialTaskId: Long? = null
) {
    val app = LocalContext.current.applicationContext as TaskTrackerApp
    val repo = app.repository

    androidx.compose.runtime.LaunchedEffect(initialTaskId) {
        if (initialTaskId != null && initialTaskId > 0) {
            navController.navigate(Routes.detail(initialTaskId))
        }
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val vm = viewModel<HomeViewModel>(factory = simpleFactory { HomeViewModel(repo) })
            HomeScreen(
                viewModel = vm,
                onAddTask = { navController.navigate(Routes.EDIT) },
                onOpenTask = { id -> navController.navigate(Routes.detail(id)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.EDIT) {
            val vm = viewModel<EditTaskViewModel>(factory = simpleFactory { EditTaskViewModel(repo) })
            EditTaskScreen(
                viewModel = vm,
                taskId = null,
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            Routes.EDIT_WITH_ID,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            val vm = viewModel<EditTaskViewModel>(factory = simpleFactory { EditTaskViewModel(repo) })
            EditTaskScreen(
                viewModel = vm,
                taskId = id,
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            Routes.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            val vm = viewModel<TaskDetailViewModel>(factory = simpleFactory { TaskDetailViewModel(repo) })
            TaskDetailScreen(
                viewModel = vm,
                taskId = id,
                onBack = { navController.popBackStack() },
                onEdit = { taskId -> navController.navigate(Routes.edit(taskId)) },
                onDeleted = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            val vm = viewModel<SettingsViewModel>(factory = simpleFactory { SettingsViewModel(repo) })
            SettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
}

private inline fun <reified T : androidx.lifecycle.ViewModel> simpleFactory(
    crossinline creator: () -> T
): androidx.lifecycle.ViewModelProvider.Factory {
    return object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : androidx.lifecycle.ViewModel> create(modelClass: Class<VM>): VM {
            return creator() as VM
        }
    }
}
