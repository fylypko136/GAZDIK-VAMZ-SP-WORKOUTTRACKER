package com.example.gazdik_vamz.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.gazdik_vamz.MainActivity
import com.example.gazdik_vamz.R
import com.example.gazdik_vamz.data.local.AppDatabase
import com.example.gazdik_vamz.data.repository.WorkoutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/** Widget pre počet tréningov v tomto týždni.*/
class WorkoutWidget : AppWidgetProvider() {


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {


        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, WorkoutWidget::class.java)
            )
            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }


        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.workout_widget)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            CoroutineScope(Dispatchers.IO).launch {
                val repository = WorkoutRepository(AppDatabase.getInstance(context))
                val weekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
                val count = repository.getSessionCountSince(weekAgo)

                views.setTextViewText(R.id.widget_count, count.toString())
                views.setTextViewText(R.id.widget_label, context.getString(R.string.widget_label))
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }
}
