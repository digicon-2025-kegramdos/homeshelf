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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment // Added for Alignment.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.edit
import com.example.homeshelf.ui.theme.HomeShelfTheme
import org.json.JSONArray

// SharedPreferences key for the ordered list of favorite comics
// (Same as in SettingsActivity.kt - ideally in a shared constants file)
private const val FOCUS_ACTIVITY_PREFS_NAME = "com.example.homeshelf.favorites_prefs"
private const val FOCUS_ACTIVITY_KEY_FAVORITE_COMICS_ORDERED = "favorite_comics_ordered"

// Helper function to load ordered favorites (similar to SettingsActivity)
private fun loadOrderedFavoritesFocus(context: Context): MutableList<String> {
    val sharedPrefs = context.getSharedPreferences(FOCUS_ACTIVITY_PREFS_NAME, Context.MODE_PRIVATE)
    val jsonString = sharedPrefs.getString(FOCUS_ACTIVITY_KEY_FAVORITE_COMICS_ORDERED, null)
    val list = mutableListOf<String>()
    if (jsonString != null) {
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
        } catch (_: Exception) { // Changed e to _
            // Log error or handle
        }
    }
    return list
}

// Helper function to save ordered favorites (similar to SettingsActivity)
private fun saveOrderedFavoritesFocus(context: Context, orderedList: List<String>) {
    val sharedPrefs = context.getSharedPreferences(FOCUS_ACTIVITY_PREFS_NAME, Context.MODE_PRIVATE)
    val jsonArray = JSONArray(orderedList)
    sharedPrefs.edit {
        putString(FOCUS_ACTIVITY_KEY_FAVORITE_COMICS_ORDERED, jsonArray.toString())
    }
}


class FocusReadActivity : ComponentActivity() {
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
                        isFocusMode = true
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicScreen(comicId: String, modifier: Modifier = Modifier, isFocusMode: Boolean = false) {
    val comicPagesMap = mapOf(
        "comic1" to listOf(R.drawable.comic_1_1),
        "comic2" to listOf(
            R.drawable.comic_2_1,
            R.drawable.comic_2_2,
            R.drawable.comic_2_3,
            R.drawable.comic_2_4
        ),
        "comic3" to listOf(R.drawable.comic_3_1),
        "comic4" to listOf(R.drawable.comic_4_1, R.drawable.comic_4_2, R.drawable.comic_4_3)
    )

    val pages = comicPagesMap[comicId] ?: comicPagesMap.values.firstOrNull() ?: emptyList()

    if (pages.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("漫画「$comicId」のページが見つかりません。")
        }
        return
    }

    var isOverlayVisible by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val context = LocalContext.current

    var isFavorite by remember { mutableStateOf(false) }
    // Store the full list to manage additions/removals correctly
    val favoriteList = remember { mutableStateListOf<String>() }

    LaunchedEffect(comicId) {
        val currentFavorites = loadOrderedFavoritesFocus(context)
        favoriteList.clear()
        favoriteList.addAll(currentFavorites)
        isFavorite = favoriteList.contains(comicId)
    }

    val toggleFavorite: () -> Unit = {
        val currentFavorites = loadOrderedFavoritesFocus(context).toMutableList() // Load fresh list
        val comicIsCurrentlyFavorite = currentFavorites.contains(comicId)

        if (comicIsCurrentlyFavorite) {
            currentFavorites.remove(comicId)
        } else {
            currentFavorites.add(comicId) // Add to the end of the list
        }
        saveOrderedFavoritesFocus(context, currentFavorites)
        isFavorite = !comicIsCurrentlyFavorite // Update local UI state
        // Update local list for consistency if needed, though LaunchedEffect will reload
        favoriteList.clear()
        favoriteList.addAll(currentFavorites)
    }

    // ComicScreen's root is now a Box. It applies the modifier from FocusReadActivity (outerPadding)
    Box(modifier = modifier.fillMaxSize()) {
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

        // AnimatedVisibility is aligned to the bottom of the parent Box
        AnimatedVisibility(
            visible = isOverlayVisible,
            modifier = Modifier.align(Alignment.BottomCenter), // Align to the bottom of the Box
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            ) { // This is RowScope
                Text(text = "$comicId (${pagerState.currentPage + 1}/${pages.size})") // Show comicId in title

                Spacer(Modifier.weight(1f)) // Pushes subsequent items to the end

                if (!isFocusMode) {
                    IconButton(onClick = {
                        val urlToShare = "https://example.com/comic/$comicId" // Example share URL
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "漫画「$comicId」を読んでいます！ $urlToShare")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "共有")
                    }
                    IconButton(onClick = toggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = if (isFavorite) "お気に入りから削除" else "お気に入りに追加",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun ComicScreenPreview() { // Renamed from GreetingPreview for clarity
    HomeShelfTheme {
        ComicScreen("comic2", isFocusMode = false)
    }
}
