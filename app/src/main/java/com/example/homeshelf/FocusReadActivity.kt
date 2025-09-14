package com.example.homeshelf

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.example.homeshelf.ui.theme.HomeShelfTheme

class FocusReadActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // IntentからCOMIC_IDを取得。見つからない場合はデフォルト値（例: "comic1"）を使用
        val comicId = intent.getStringExtra("COMIC_ID") ?: "comic1"

        setContent {
            HomeShelfTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ComicScreen(
                        name = comicId, // 受け取ったcomicIdを渡す
                        modifier = Modifier.padding(innerPadding),
                        isFocusMode = false // 必要に応じてisFocusModeもIntentで渡すように変更可能
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicScreen(name: String, modifier: Modifier = Modifier, isFocusMode: Boolean = false) {
    // 各漫画のページリソースIDのリストを定義
    val comicPagesMap = mapOf(
        "comic1" to listOf(R.drawable.comic_1_1),
        "comic2" to listOf(R.drawable.comic_2_1, R.drawable.comic_2_2, R.drawable.comic_2_3, R.drawable.comic_2_4),
        "comic3" to listOf(R.drawable.comic_3_1),
        "comic4" to listOf(R.drawable.comic_4_1, R.drawable.comic_4_2, R.drawable.comic_4_3)
    )

    // name引数に基づいて表示するページリストを選択
    // マップに存在しない場合は、最初の漫画を表示するか、空のリストを使用
    val pages = comicPagesMap[name] ?: comicPagesMap.values.firstOrNull() ?: emptyList()

    if (pages.isEmpty()) {
        // ページが見つからない場合の処理（例: エラーメッセージ表示）
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("漫画「$name」のページが見つかりません。")
        }
        return
    }

    var isOverlayVisible by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val context = LocalContext.current

    Box(
        modifier = modifier.fillMaxSize(), // modifier パラメータをBoxのトップレベルに適用
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
                        val urlToShare = "https://example.com" // 共有する内容に応じて変更
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
                    IconButton(onClick = { /*TODO: お気に入り機能の実装*/ }) { Icon(imageVector = Icons.Default.Star, contentDescription = "お気に入り") }
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
        // プレビューで表示する漫画のIDを指定
        ComicScreen("comic2")
    }
}
