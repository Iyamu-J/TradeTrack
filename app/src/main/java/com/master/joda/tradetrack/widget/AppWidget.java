package com.master.joda.tradetrack.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import com.master.joda.tradetrack.R;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {

    static void updateAllAppWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds,
                                    String date, String profit) {
        for (int appWidgetId: appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, date, profit);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String date, String  profit) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        if (!date.isEmpty() && !profit.isEmpty()) {
            views.setTextViewText(R.id.appwidget_date, date);
            views.setTextViewText(R.id.appwidget_profit, context.getString(R.string.profit_text, profit));
            views.setViewVisibility(R.id.appwidget_empty_view, View.GONE);
        } else {
            views.setViewVisibility(R.id.appwidget_empty_view, View.VISIBLE);
            views.setTextViewText(R.id.appwidget_empty_view, context.getString(R.string.records_error_message));
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        WidgetService.startActionUpdateWidget(context);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

