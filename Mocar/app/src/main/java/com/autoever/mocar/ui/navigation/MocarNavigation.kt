import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.autoever.mocar.ui.detail.CarDetailScreen
//import com.autoever.mocar.ui.home.HomeSampleData
import com.autoever.mocar.ui.auth.LoginPage
import com.autoever.mocar.ui.auth.SignUpPage
//import com.autoever.mocar.ui.home.HomeSampleData.cars
import com.autoever.mocar.ui.home.MainScreen
import com.autoever.mocar.ui.search.ModelSelect
import com.autoever.mocar.ui.search.SearchPage
import com.autoever.mocar.viewmodel.CarDetailRoute

// ----- Routes -----
const val ROUTE_AUTH = "auth"
const val ROUTE_MAIN = "main"
const val ROUTE_CAR_DETAIL = "carDetail"
const val ROUTE_SEARCH = "search"
fun carDetailRoute(carId: String) = "$ROUTE_CAR_DETAIL/$carId"

@Composable
fun MocarNavigation() {
    val navController = rememberNavController()
//    var cars by remember { mutableStateOf(HomeSampleData.cars) }
//
//    val toggleFavorite: (String) -> Unit = { id ->
//        cars = cars.map { c ->
//            if (c.id == id) c.copy(isFavorite = !c.isFavorite) else c
//        }
//    }

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
            SearchPage(navController)
        }
        // 차량 상세
        composable(
            route = "$ROUTE_CAR_DETAIL/{carId}",
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            CarDetailRoute(
                carId = carId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("signup") {
            SignUpPage(navController)
        }
        composable("modelSelect") {
            ModelSelect(
                onBack = { navController.popBackStack() },
                onConfirm = { selectedModels ->
                    println("선택된 모델: $selectedModels")
                    navController.popBackStack()
                }
            )
        }



    }
}
