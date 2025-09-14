package com.example.homeshelf

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.widget.ViewFlipper
import androidx.core.net.toUri


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

const val ACTION_LEFTBUTTON = "WIDGET_LEFTBUTTON_CLICKED"
const val ACTION_RIGHTBUTTON = "WIDGET_RIGHTBUTTON_CLICKED"

/**
 * Implementation of App Widget functionality.
 */
class ComicsWidget : AppWidgetProvider() {
    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if(context == null)
            return

        if (intent?.action == ACTION_LEFTBUTTON) {
            val views = RemoteViews(context.packageName, R.layout.comics_widget).apply {
                setInt(R.id.appwidget_itemlist_flipper, "setInAnimation", R.anim.in_from_left)
                setInt(R.id.appwidget_itemlist_flipper, "setOutAnimation", R.anim.out_to_right)
                setDisplayedChild(R.id.appwidget_itemlist_flipper, 0)
            }

            updateWidget(context, views)
        } else if (intent?.action == ACTION_RIGHTBUTTON) {
            val views = RemoteViews(context.packageName, R.layout.comics_widget).apply {
//                setInt(R.id.appwidget_itemlist_flipper, "setInAnimation", R.anim.in_from_right)
//                setInt(R.id.appwidget_itemlist_flipper, "setOutAnimation", R.anim.out_to_left)
                setDisplayedChild(R.id.appwidget_itemlist_flipper, 2)
            }

            updateWidget(context, views)
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
            // Set up the intent that starts the StackViewService, which
            // provides the views for this collection.
            val intent = Intent(context, ListWidgetService::class.java).apply {
                // Add the widget ID to the intent extras.
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = toUri(Intent.URI_INTENT_SCHEME).toUri()
            }

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

                setImageViewResource(R.id.widget_button_left, R.drawable.ic_button_left)
                setOnClickPendingIntent(R.id.widget_button_left, leftButtonPendingIntent)

                setImageViewResource(R.id.widget_button_right, R.drawable.ic_button_right)
                setOnClickPendingIntent(R.id.widget_button_right, rightButtonPendingIntent)

                setDisplayedChild(R.id.appwidget_itemlist_flipper, 1)
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
