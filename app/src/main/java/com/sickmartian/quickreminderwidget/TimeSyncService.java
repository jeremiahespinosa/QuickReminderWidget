package com.sickmartian.quickreminderwidget;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;

import org.joda.time.LocalDateTime;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/11/16.
 */
public class TimeSyncService extends IntentService {
    private static final int REQUEST_CODE = 5656;
    public static final String AND_UPDATE_WIDGETS = "AND_UPDATE_WIDGETS";
    public static final String AND_DISABLE = "AND_DISABLE";

    public TimeSyncService() {
        super(TimeSyncService.class.toString());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Timber.d("TimeSyncService starting");
            LocalDateTime nextTime = QAWApp.getInitialTime(QAWApp.isThereOneEvery30);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent timeSyncIntent = new Intent(this, TimeSyncReceiver.class);
            timeSyncIntent.putExtra(TimeSyncService.AND_UPDATE_WIDGETS, true);
            PendingIntent timeSyncIntentPI = PendingIntent.getBroadcast(this,
                    TimeSyncService.REQUEST_CODE, timeSyncIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            // If we are disabling, disable
            if (intent.getBooleanExtra(AND_DISABLE, false)) {
                Timber.i("Disabling TimeSync");
                alarmManager.cancel(timeSyncIntentPI);
            } else {
                // If not, we schedule the next call to this service
                Timber.i("Placing TimeSync alarm for: " + nextTime.toString());
                alarmManager.set(AlarmManager.RTC, nextTime.toDateTime().getMillis(),
                        timeSyncIntentPI);
            }

            if (intent.getBooleanExtra(AND_UPDATE_WIDGETS, false)) {
                Timber.i("Updating Widgets");
                QAWApp.updateAllWidgets();
            }
        } finally {
            Timber.d("TimeSyncService ending");
            TimeSyncReceiver.completeWakefulIntent(intent);
        }
    }
}