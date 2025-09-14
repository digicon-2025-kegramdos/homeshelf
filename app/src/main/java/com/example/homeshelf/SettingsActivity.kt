package com.example.homeshelf

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.homeshelf.ui.theme.HomeShelfTheme

// SharedPreferences keys (FocusReadActivity.kt と共通)
private const val PREFS_NAME = "com.example.homeshelf.favorites"
private fun getFavoriteKey(comicId: String) = "favorite_$comicId"

// 仮の漫画リスト (FocusReadActivity.kt の comicPagesMap のキーを元に作成)
// 本来は共通のデータソースから取得するのが望ましい
val allComicIds = listOf("comic1", "comic2", "comic3", "comic4")


class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeShelfTheme {
                FavoriteSettingsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteSettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val favoriteComics = remember { mutableStateListOf<String>() }

    // Load favorites from SharedPreferences
    LaunchedEffect(Unit) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentFavorites = allComicIds.filter { comicId ->
            sharedPrefs.getBoolean(getFavoriteKey(comicId), false)
        }
        favoriteComics.clear()
        favoriteComics.addAll(currentFavorites)
    }

    val toggleFavorite = { comicId: String ->
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isCurrentlyFavorite = sharedPrefs.getBoolean(getFavoriteKey(comicId), false)
        sharedPrefs.edit {
            putBoolean(getFavoriteKey(comicId), !isCurrentlyFavorite)
        }
        if (isCurrentlyFavorite) {
            favoriteComics.remove(comicId)
        } else {
            // この画面ではお気に入り追加は主目的ではないが、
            // 万が一状態が不整合になった場合のために追加しておく
            if (!favoriteComics.contains(comicId)) {
                favoriteComics.add(comicId)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("お気に入り設定") })
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp)) {
            if (favoriteComics.isEmpty()) {
                Text(
                    text = "お気に入りの漫画はありません。",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(favoriteComics.toList()) { comicId -> // toList() でイミュータブルなリストに変換
                        FavoriteItem(
                            comicId = comicId,
                            isFavorite = true, // このリストにあるものは常にお気に入り
                            onToggleFavorite = { toggleFavorite(comicId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteItem(
    comicId: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = comicId, style = MaterialTheme.typography.bodyLarge) // ここでは漫画IDを表示
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = if (isFavorite) "お気に入り解除" else "お気に入り登録",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FavoriteSettingsScreenPreview() {
    HomeShelfTheme {
        // Preview 用のデータで FavoriteSettingsScreen を表示
        // 簡単のため、ここでは SharedPreferences を直接モックせず、
        // LaunchedEffect が空のリストを生成するようにします。
        // より正確なプレビューのためには、SharedPreferences のモックが必要です。
        FavoriteSettingsScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun FavoriteItemPreview() {
    HomeShelfTheme {
        FavoriteItem(comicId = "comic_preview", isFavorite = true, onToggleFavorite = {})
    }
}

@Preview(showBackground = true)
@Composable
fun FavoriteItemNotFavoritePreview() {
    HomeShelfTheme {
        FavoriteItem(comicId = "comic_preview_not_fav", isFavorite = false, onToggleFavorite = {})
    }
}
