package com.example.homeshelf

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.homeshelf.ui.theme.HomeShelfTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeShelfTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    MainScreen()
                }
            }
        }
    }
}

data class Thumbnail(
    @DrawableRes val imageRes: Int,
    val title: String,
    val author: String
)

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val thumbnails = listOf(
        Thumbnail(R.drawable.thumbnail1, "きなのはテロの道具じゃない！", "kegra"),
        Thumbnail(R.drawable.thumbnail2, "pika_testの消失", "kegra"),
        Thumbnail(R.drawable.thumbnail3, "I can flyなんですよ", "kegra"),
        Thumbnail(R.drawable.thumbnail4, "traPへようこそ！", "kegra"),
        )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Enable vertical scrolling
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally
    ) {
        Text("HomeShelf", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
        thumbnails.forEach { thumbnail ->
            Column(
                modifier = Modifier.padding(8.dp),
            ) {
                Image(
                    painter = painterResource(id = thumbnail.imageRes),
                    contentDescription = null, // Provide a meaningful description if needed
                    modifier = Modifier
                        .size(width = 400.dp, height = 200.dp) // Adjust size as needed
                        .padding(vertical = 8.dp) // Add some vertical spacing
                        .clickable {
                            val intent = Intent(context, FocusReadActivity::class.java)
                            context.startActivity(intent)
                        }
                )
                Text(text = thumbnail.title, fontSize=20.sp)
                Text(text = thumbnail.author,color = Color.DarkGray)
            }
        }
    }
}


@Preview(
    showBackground = true,
    widthDp = 480,
    heightDp = 960
)
@Composable
fun MainPreview() {
    HomeShelfTheme {
        MainScreen()
    }
}