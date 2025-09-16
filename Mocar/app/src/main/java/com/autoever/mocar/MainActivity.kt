package com.autoever.mocar

import CarFilter
import SearchResultScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.autoever.mocar.ui.theme.MocarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MocarTheme {
                MaterialTheme {
                    SearchResultScreen(
                        manufacturer = "현대",
                        model = "아반떼",
                        filter = CarFilter(),
                        onCarClick = { car ->
                            // 차량 클릭 시 동작
                            println("Clicked: ${car.trim}")
                        },
                        onFavoriteClick = { car ->
                            // 찜 버튼 클릭 시 동작
                            println("찜 변경: ${car.trim}, 상태=${car.isFavorite}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

}