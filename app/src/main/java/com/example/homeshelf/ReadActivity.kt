package com.example.homeshelf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.homeshelf.ui.theme.HomeShelfTheme

class ReadActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val comicId =
            intent.getStringExtra("COMIC_ID") ?: "comic1" // Default comicId if not provided

        setContent {
            HomeShelfTheme {
                // The Scaffold in FocusReadActivity provides overall structure and padding for edge-to-edge
                Scaffold(modifier = Modifier.fillMaxSize()) { outerPadding ->
                    ComicScreen(
                        comicId = comicId, // Pass comicId instead of name for clarity
                        modifier = Modifier.padding(outerPadding), // Pass padding to ComicScreen
                        isFocusMode = false
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
    HomeShelfTheme {
        Greeting("Android")
    }
}