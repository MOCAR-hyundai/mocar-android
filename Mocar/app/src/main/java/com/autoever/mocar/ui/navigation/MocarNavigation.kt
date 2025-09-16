import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.autoever.mocar.ui.detail.CarDetailScreen
import com.autoever.mocar.ui.home.HomeSampleData
import com.autoever.mocar.ui.auth.LoginPage
import com.autoever.mocar.ui.auth.SignUpPage
import com.autoever.mocar.ui.home.MainScreen

// ----- Routes -----
const val ROUTE_AUTH = "auth"
const val ROUTE_MAIN = "main"
const val ROUTE_CAR_DETAIL = "carDetail"
fun carDetailRoute(carId: String) = "$ROUTE_CAR_DETAIL/$carId"

@Composable
fun MocarNavigation() {
    val navController = rememberNavController()

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
        // 차량 상세
        composable(
            route = "$ROUTE_CAR_DETAIL/{carId}",
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId")
            val car = HomeSampleData.cars.firstOrNull { it.id == carId }
            CarDetailScreen(
                car = car,
                onBack = { navController.popBackStack() }
            )
        }
        composable("signup") {
            SignUpPage(navController)
        }
    }
}
