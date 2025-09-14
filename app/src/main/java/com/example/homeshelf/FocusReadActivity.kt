package com.example.homeshelf

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline // Outlined star icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.edit // Import for SharedPreferences KTX extension
import com.example.homeshelf.ui.theme.HomeShelfTheme

class FocusReadActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val comicId = intent.getStringExtra("COMIC_ID") ?: "comic1"

        setContent {
            HomeShelfTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ComicScreen(
                        name = comicId,
                        modifier = Modifier.padding(innerPadding),
                        isFocusMode = false
                    )
                }
            }
        }
    }
}

// SharedPreferences keys
private const val PREFS_NAME = "com.example.homeshelf.favorites"
private fun getFavoriteKey(comicId: String) = "favorite_$comicId"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicScreen(name: String, modifier: Modifier = Modifier, isFocusMode: Boolean = false) {
    val comicPagesMap = mapOf(
        "comic1" to listOf(R.drawable.comic_1_1),
        "comic2" to listOf(R.drawable.comic_2_1, R.drawable.comic_2_2, R.drawable.comic_2_3, R.drawable.comic_2_4),
        "comic3" to listOf(R.drawable.comic_3_1),
        "comic4" to listOf(R.drawable.comic_4_1, R.drawable.comic_4_2, R.drawable.comic_4_3)
    )

    val pages = comicPagesMap[name] ?: comicPagesMap.values.firstOrNull() ?: emptyList()

    if (pages.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("漫画「$name」のページが見つかりません。")
        }
        return
    }

    var isOverlayVisible by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val context = LocalContext.current

    // Favorite state
    var isFavorite by remember { mutableStateOf(false) }

    // Load initial favorite state
    LaunchedEffect(name) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isFavorite = sharedPrefs.getBoolean(getFavoriteKey(name), false)
    }

    // Function to toggle favorite state
    val toggleFavorite: () -> Unit = {
        val newFavoriteState = !isFavorite
        isFavorite = newFavoriteState
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit {
            putBoolean(getFavoriteKey(name), newFavoriteState)
            // apply() is called automatically by the KTX extension
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isOverlayVisible = !isOverlayVisible
                }
        ) { pageIndex ->
            Image(
                painter = painterResource(id = pages[pageIndex]),
                contentDescription = "漫画ページ ${pageIndex + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
    AnimatedVisibility(
        visible = isOverlayVisible,
        modifier = Modifier,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        TopAppBar(
            title = { Text(text = "ページ ${pagerState.currentPage + 1}/${pages.size}") },
            actions = {
                if (!isFocusMode) {
                    IconButton(onClick = {
                        val urlToShare = "https://example.com"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "漫画「$name」を読んでいます！ $urlToShare")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "共有")
                    }
                    IconButton(onClick = toggleFavorite) { // Use the toggle function
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "お気に入り",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            )
        )
    }
}


@Preview(
    showBackground = true,
    widthDp = 480,
    heightDp = 960
)
@Composable
fun GreetingPreview() {
    HomeShelfTheme {
        ComicScreen("comic2")
    }
}
