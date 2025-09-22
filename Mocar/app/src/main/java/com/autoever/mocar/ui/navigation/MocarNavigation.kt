import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.autoever.mocar.ui.auth.LoginPage
import com.autoever.mocar.ui.auth.SignUpPage
import com.autoever.mocar.ui.chat.ChatRoomScreen
import com.autoever.mocar.ui.home.MainScreen
import com.autoever.mocar.ui.search.ModelSelect
import com.autoever.mocar.ui.search.SearchHistoryScreen
import com.autoever.mocar.ui.search.SearchPage
import com.autoever.mocar.ui.search.SubModelSelect
import com.autoever.mocar.ui.mypage.LikeListScreen
import com.autoever.mocar.viewmodel.CarDetailRoute
import com.autoever.mocar.viewmodel.ListingViewModel
import com.autoever.mocar.viewmodel.SearchFilterViewModel
import com.autoever.mocar.viewmodel.SearchFilterViewModelFactory
import com.autoever.mocar.viewmodel.SearchManufacturerViewModel

// ----- Routes -----
const val ROUTE_AUTH = "auth"
const val ROUTE_MAIN = "main"
const val ROUTE_CAR_DETAIL = "carDetail"
const val ROUTE_SEARCH = "search"
fun carDetailRoute(carId: String) = "$ROUTE_CAR_DETAIL/$carId"

@Composable
fun MocarNavigation() {
    val navController = rememberNavController()
    val searchManufacturerViewModel: SearchManufacturerViewModel = viewModel()
    val context = LocalContext.current.applicationContext as Application
    val searchFilterViewModel: SearchFilterViewModel = viewModel(
        factory = SearchFilterViewModelFactory(context.applicationContext as Application)
    )
    val manufacturerViewModel: SearchManufacturerViewModel = viewModel()
    val listingViewModel: ListingViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = ROUTE_AUTH
    ) {
        composable(ROUTE_AUTH) {
            LoginPage(navController)
        }
        composable(ROUTE_MAIN) {
            MainScreen(rootNavController = navController)
        }
        composable(ROUTE_SEARCH) {
            SearchPage(
                navController = navController,
                searchManufacturerViewModel = searchManufacturerViewModel,
                searchFilterViewModel = searchFilterViewModel,
                listingViewModel = listingViewModel,
                onBack = {
                    navController.navigate(ROUTE_MAIN)
                }
            )
        }
        // 차량 상세
        composable(
            route = "$ROUTE_CAR_DETAIL/{carId}",
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            CarDetailRoute(
                carId = carId,
                onBack = { navController.popBackStack() },
                navToChat = { chatId ->
                    navController.navigate("chat_room/$chatId")
                }
            )
        }
        composable("signup") {
            SignUpPage(navController)
        }

        composable("login") {
            LoginPage(navController)
        }

        composable(
            "model_select/{brandName}",
            arguments = listOf(navArgument("brandName") { type = NavType.StringType })
        ) { backStackEntry ->
            val brand = backStackEntry.arguments?.getString("brandName") ?: return@composable
            val listings by listingViewModel.listings.collectAsState()

            ModelSelect(
                navController = navController,
                brandName = brand,
                allListings = listings,
                listingViewModel = listingViewModel,
                searchManufacturerViewModel = searchManufacturerViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            "sub_model_select/{brandName}/{modelName}",
            arguments = listOf(
                navArgument("brandName") { type = NavType.StringType },
                navArgument("modelName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val brand = backStackEntry.arguments?.getString("brandName") ?: return@composable
            val model = backStackEntry.arguments?.getString("modelName") ?: return@composable
            val listings by listingViewModel.listings.collectAsState()

            SubModelSelect(
                navController = navController,
                brandName = brand,
                modelName = model,
                allListings = listings,
                searchManufacturerViewModel = searchManufacturerViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("history") {
            SearchHistoryScreen(
                searchFilterViewModel = searchFilterViewModel,
                searchManufacturerViewModel = manufacturerViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("like_list") {
            LikeListScreen(
                navController = navController,
                onCarClick = { carId ->
                    navController.navigate("carDetail/$carId")
                }
            )
        }

        composable("chats") {
            com.autoever.mocar.ui.chat.ChatsScreen(navController = navController)
        }
        composable(
            "chat_room/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
            ChatRoomScreen(chatId = chatId,
                onBack = { navController.popBackStack() } )
        }


//        composable(
//            route = "search_result/{brandName}/{modelName}",
//            arguments = listOf(
//                navArgument("brandName") { type = NavType.StringType },
//                navArgument("modelName") { type = NavType.StringType }
//            )
//        ) { backStackEntry ->
//            val brand = backStackEntry.arguments?.getString("brandName") ?: return@composable
//            val model = backStackEntry.arguments?.getString("modelName") ?: return@composable
//
//            SearchResultScreen(
//                manufacturer = brand,
//                model = model
//            )
//        }
    }
}
