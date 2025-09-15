package com.example.homeshelf

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.net.toUri

const val EXTRA_ITEM = "com.example.homeshelf.comics_widget.EXTRA_ITEM"

const val ACTION_LEFTBUTTON = "WIDGET_LEFTBUTTON_CLICKED"
const val ACTION_RIGHTBUTTON = "WIDGET_RIGHTBUTTON_CLICKED"
const val ACTION_LISTITEM_TAPPED = "WIDGET_LISTITEM_TAPPED"

fun updateComicWidget(context: Context) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val componentName = ComponentName(context, ComicsWidget::class.java)
    val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

    val intent = Intent(context, ComicsWidget::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
    }
    Log.i("MyApp","updateComicWidget()")
    context.sendBroadcast(intent)
}

class ListWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ListRemoteViewsFactory(this.applicationContext)
    }
}

class ListRemoteViewsFactory(
    private val context: Context,
) : RemoteViewsService.RemoteViewsFactory {
    private val mCount: Int = 10
    private val mWidgetItems: MutableList<String> = ArrayList<String>()

    override fun onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        for (i in 0..<mCount) {
            mWidgetItems.add(i.toString())
        }
    }

    override fun onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        mWidgetItems.clear()
    }

    override fun getCount(): Int {
        return mCount
    }

    override fun getViewAt(position: Int): RemoteViews {
        // Construct a remote views item based on the widget item XML file
        // and set the text based on the position.
        return RemoteViews(context.packageName, R.layout.widget_comiclist_item).apply {
            setTextViewText(R.id.widget_comiclist_item_text, mWidgetItems[position])

            // Set a fill-intent to fill in the pending intent template.
            // that is set on the collection view in StackWidgetProvider.
            val fillInIntent = Intent().apply {
                Bundle().also { extras ->
                    extras.putInt(EXTRA_ITEM, position)
                    putExtras(extras)
                }
            }
            // Make it possible to distinguish the individual on-click
            // action of a given item.
            setOnClickFillInIntent(R.id.widget_comiclist_item_root, fillInIntent)
        }
    }

    override fun getLoadingView(): RemoteViews? {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun onDataSetChanged() {
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.
    }
}

private fun setListViewContent(context: Context, views: RemoteViews, appWidgetId: Int, viewIdItemList: Int, viewIdEmpty: Int) {
    // Set up the intent that starts the StackViewService, which
    // provides the views for this collection.
    val intent = Intent(context, ListWidgetService::class.java).apply {
        // Add the widget ID to the intent extras.
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        data = toUri(Intent.URI_INTENT_SCHEME).toUri()
    }

    views.setRemoteAdapter(viewIdItemList, intent)
    views.setEmptyView(viewIdItemList, viewIdEmpty)
}

/**
 * Implementation of App Widget functionality.
 */
class ComicsWidget : AppWidgetProvider() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(context == null)
            return

        if (intent?.action == ACTION_LEFTBUTTON) {
            val views = RemoteViews(context.packageName, R.layout.comics_widget).apply {
                setDisplayedChild(R.id.appwidget_itemlist_flipper, 0)
            }

            updateWidget(context, views)
        } else if (intent?.action == ACTION_RIGHTBUTTON) {
            val views = RemoteViews(context.packageName, R.layout.comics_widget).apply {
                setDisplayedChild(R.id.appwidget_itemlist_flipper, 2)
            }

            updateWidget(context, views)
        } else if (intent?.action == ACTION_LISTITEM_TAPPED) {
            val appWidgetId: Int = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
            val viewIndex: Int = intent.getIntExtra(EXTRA_ITEM, -1)
            Log.i("MyApp", "tap: ${viewIndex}")

            val intent = Intent(context, FocusReadActivity::class.java).apply {
                putExtra("COMIC_ID", "comic1") // comicIdをIntentに追加
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else {
            super.onReceive(context, intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {

            val leftButtonIntent = Intent(context, ComicsWidget::class.java).apply {
                action = ACTION_LEFTBUTTON
            }
            val leftButtonPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                leftButtonIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            val rightButtonIntent = Intent(context, ComicsWidget::class.java).apply {
                action = ACTION_RIGHTBUTTON
            }
            val rightButtonPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                rightButtonIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            // This section makes it possible for items to have individualized
            // behavior. It does this by setting up a pending intent template.
            // Individuals items of a collection can't set up their own pending
            // intents. Instead, the collection as a whole sets up a pending
            // intent template, and the individual items set a fillInIntent
            // to create unique behavior on an item-by-item basis.
            val listSelectPendingIntent: PendingIntent = Intent(
                context,
                ComicsWidget::class.java
            ).run {
                // Set the action for the intent.
                // When the user touches a particular view, it has the effect of
                // broadcasting TOAST_ACTION.
                action = ACTION_LISTITEM_TAPPED
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = toUri(Intent.URI_INTENT_SCHEME).toUri()

                PendingIntent.getBroadcast(context, 0, this, PendingIntent.FLAG_IMMUTABLE)
            }
            val views = RemoteViews(context.packageName, R.layout.comics_widget).apply {
                setListViewContent(context, this, appWidgetId, R.id.appwidget_itemlist_prev, R.id.appwidget_empty_view_prev)
                setListViewContent(context, this, appWidgetId, R.id.appwidget_itemlist, R.id.appwidget_empty_view)
                setListViewContent(context, this, appWidgetId, R.id.appwidget_itemlist_next, R.id.appwidget_empty_view_next)

                setImageViewResource(R.id.widget_button_left, R.drawable.ic_button_left)
                setOnClickPendingIntent(R.id.widget_button_left, leftButtonPendingIntent)

                setImageViewResource(R.id.widget_button_right, R.drawable.ic_button_right)
                setOnClickPendingIntent(R.id.widget_button_right, rightButtonPendingIntent)

                setDisplayedChild(R.id.appwidget_itemlist_flipper, 1)
                setPendingIntentTemplate(R.id.appwidget_itemlist_prev, listSelectPendingIntent)
                setPendingIntentTemplate(R.id.appwidget_itemlist, listSelectPendingIntent)
                setPendingIntentTemplate(R.id.appwidget_itemlist_next, listSelectPendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

private fun updateWidget(context: Context, views: RemoteViews) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetIds = appWidgetManager.getAppWidgetIds(
        ComponentName(
            context,
            ComicsWidget::class.java
        )
    )
    for (appWidgetId in appWidgetIds) {
        Log.i("MyApp", appWidgetId.toString())
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
