package com.example.homeshelf

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.ArrayAdapter
import android.widget.RemoteViews
import android.widget.RemoteViewsService


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

/**
 * Implementation of App Widget functionality.
 */
class ComicsWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            val platformVersion = arrayOf<String>(
                "Android 10.0",
                "Android 9",
                "Android 8.1",
                "Android 8.0",
                "Android 7.1.1",
                "Android 7.1",
                "Android 7.0",
                "Android 6.0",
                "Android 5.1",
                "Android 5.0",
                "Android 4.4W",
                "Android 4.4",
                "Android 4.3",
                "Android 4.2、4.2.2",
                "Android 4.1、4.1.1",
                "Android 4.0.3、4.0.4",
                "Android 4.0、4.0.1、4.0.2",
                "Android 3.2",
                "Android 3.1.x",
                "Android 3.0.x",
                "Android 2.3.4",
                "Android 2.3.3",
                "Android 2.3.2",
                "Android 2.3.1",
                "Android 2.3",
                "Android 2.2.x",
                "Android 2.1.x",
                "Android 2.0.1",
                "Android 2.0",
                "Android 1.6",
                "Android 1.5",
                "Android 1.1",
                "Android 1.0",
            )

            val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                context,
                R.layout.widget_comiclist_item,
                platformVersion
            )

            // Set up the intent that starts the StackViewService, which
            // provides the views for this collection.
            val intent = Intent(context, ListWidgetService::class.java).apply {
                // Add the widget ID to the intent extras.
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            // Instantiate the RemoteViews object for the widget layout.
            val views = RemoteViews(context.packageName, R.layout.comics_widget).apply {
                // Set up the RemoteViews object to use a RemoteViews adapter.
                // This adapter connects to a RemoteViewsService through the
                // specified intent.
                // This is how you populate the data.
                setRemoteAdapter(R.id.appwidget_itemlist, intent)

                // The empty view is displayed when the collection has no items.
                // It must be in the same layout used to instantiate the
                // RemoteViews object.
                setEmptyView(R.id.appwidget_itemlist, R.id.appwidget_empty_view)
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

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val widgetText = context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.comics_widget)
//    views.setTextViewText(R.id.appwidget_text, widgetText)

    // 空のPendingIntentを設定してデフォルト動作を上書き
    val emptyIntent = Intent()
    val pendingIntent = PendingIntent.getActivity(
        context, 0, emptyIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(android.R.id.background, pendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}