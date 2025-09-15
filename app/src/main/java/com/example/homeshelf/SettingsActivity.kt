package com.example.homeshelf

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle // Import DragHandle
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
import org.json.JSONArray
import java.util.Collections

// SharedPreferences key for the ordered list of favorite comics
private const val PREFS_NAME = "com.example.homeshelf.favorites_prefs"
private const val KEY_FAVORITE_COMICS_ORDERED = "favorite_comics_ordered"

// Helper function to load ordered favorites
private fun loadOrderedFavorites(context: Context): MutableList<String> {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val jsonString = sharedPrefs.getString(KEY_FAVORITE_COMICS_ORDERED, null)
    val list = mutableListOf<String>()
    if (jsonString != null) {
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
        } catch (e: Exception) {
            // Handle error or clear corrupted data
            // For simplicity, we'll return an empty list if parsing fails
        }
    }
    return list
}

// Helper function to save ordered favorites
private fun saveOrderedFavorites(context: Context, orderedList: List<String>) {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val jsonArray = JSONArray(orderedList)
    sharedPrefs.edit {
        putString(KEY_FAVORITE_COMICS_ORDERED, jsonArray.toString())
    }
}


class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeShelfTheme {
                OrderedFavoriteSettingsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderedFavoriteSettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val favoriteComics = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        favoriteComics.addAll(loadOrderedFavorites(context))
    }

    fun updateAndSaveFavorites(newList: List<String>) {
        favoriteComics.clear()
        favoriteComics.addAll(newList)
        saveOrderedFavorites(context, favoriteComics)
        updateComicWidget(context)
    }

    val onMoveUp = { index: Int ->
        if (index > 0) {
            val newList = favoriteComics.toMutableList()
            Collections.swap(newList, index, index - 1)
            updateAndSaveFavorites(newList)
        }
    }

    val onMoveDown = { index: Int ->
        if (index < favoriteComics.size - 1) {
            val newList = favoriteComics.toMutableList()
            Collections.swap(newList, index, index + 1)
            updateAndSaveFavorites(newList)
        }
    }

    val onRemoveFavorite = { comicId: String ->
        val newList = favoriteComics.toMutableList()
        newList.remove(comicId)
        updateAndSaveFavorites(newList)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("お気に入り順序設定") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (favoriteComics.isEmpty()) {
                Text(
                    text = "お気に入りの漫画はありません。",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(favoriteComics, key = { _, comicId -> comicId }) { index, comicId ->
                        EditableFavoriteListItem(
                            comicId = comicId,
                            index = index,
                            totalItems = favoriteComics.size,
                            onMoveUp = { onMoveUp(index) },
                            onMoveDown = { onMoveDown(index) },
                            onRemove = { onRemoveFavorite(comicId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditableFavoriteListItem(
    comicId: String,
    index: Int,
    totalItems: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        // horizontalArrangement = Arrangement.SpaceBetween // Adjusted for drag handle
    ) {
        Icon(
            imageVector = Icons.Filled.DragHandle,
            contentDescription = "ドラッグして並び替え",
            modifier = Modifier.padding(end = 8.dp) // Add some padding
        )
        Text(text = comicId, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Row {
            IconButton(onClick = onMoveUp, enabled = index > 0) {
                Icon(Icons.Filled.ArrowUpward, contentDescription = "上へ移動")
            }
            IconButton(onClick = onMoveDown, enabled = index < totalItems - 1) {
                Icon(Icons.Filled.ArrowDownward, contentDescription = "下へ移動")
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "お気に入りから削除", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun OrderedFavoriteSettingsScreenPreview() {
    HomeShelfTheme {
        val sampleFavorites = remember {
            mutableStateListOf("comic2", "comic1", "comic4")
        }
        Scaffold(
            topBar = { TopAppBar(title = { Text("お気に入り順序設定 (Preview)") }) }
        ) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                if (sampleFavorites.isEmpty()) {
                    Text("お気に入りの漫画はありません。")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(sampleFavorites, key = { _, id -> id}) { index, comicId ->
                            EditableFavoriteListItem(
                                comicId = comicId,
                                index = index,
                                totalItems = sampleFavorites.size,
                                onMoveUp = {
                                    if (index > 0) Collections.swap(sampleFavorites, index, index - 1)
                                },
                                onMoveDown = {
                                     if (index < sampleFavorites.size - 1) Collections.swap(sampleFavorites, index, index + 1)
                                 },
                                onRemove = { sampleFavorites.remove(comicId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditableFavoriteListItemPreview() {
    HomeShelfTheme {
        EditableFavoriteListItem(
            comicId = "comic_preview",
            index = 0,
            totalItems = 3,
            onMoveUp = {},
            onMoveDown = {},
            onRemove = {}
        )
    }
}
