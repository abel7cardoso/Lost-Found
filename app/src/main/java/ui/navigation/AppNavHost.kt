
package com.abel.lostandfound.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.abel.lostandfound.data.FirebaseRepositoryProvider
import com.abel.lostandfound.ui.AuthViewModel
import com.abel.lostandfound.ui.auth.LoginScreen
import com.abel.lostandfound.ui.auth.RegisterScreen
import com.abel.lostandfound.ui.form.ItemFormScreen
import com.abel.lostandfound.ui.form.ItemFormViewModel
import com.abel.lostandfound.ui.home.HomeScreen
import com.abel.lostandfound.ui.home.HomeViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    val authRepository = FirebaseRepositoryProvider.authRepository
    val lostFoundRepository = FirebaseRepositoryProvider.lostFoundRepository

    val authViewModel: AuthViewModel = viewModel(
        factory = simpleFactory { AuthViewModel(authRepository) }
    )

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val startDestination = if (isLoggedIn) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("home") {

            val homeViewModel: HomeViewModel = viewModel(
                factory = simpleFactory {
                    HomeViewModel(lostFoundRepository)
                }
            )

            HomeScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                homeViewModel = homeViewModel,
                onAddItem = {
                    navController.navigate("itemForm")
                },
                onEditItem = { itemId ->
                    navController.navigate("itemForm/$itemId")
                }
            )
        }

        composable("itemForm") {
            val formViewModel: ItemFormViewModel = viewModel(
                factory = simpleFactory {
                    ItemFormViewModel(lostFoundRepository)
                }
            )

            ItemFormScreen(
                viewModel = formViewModel,
                onDone = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "itemForm/{itemId}",
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.StringType
                }
            )
        ) { entry ->
            val itemId = entry.arguments?.getString("itemId")

            val formViewModel: ItemFormViewModel = viewModel(
                factory = simpleFactory {
                    ItemFormViewModel(lostFoundRepository, itemId)
                }
            )

            ItemFormScreen(
                viewModel = formViewModel,
                onDone = {
                    navController.popBackStack()
                }
            )
        }
    }
}

private fun <T : ViewModel> simpleFactory(
    creator: () -> T
): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
            return creator() as VM
        }
    }
