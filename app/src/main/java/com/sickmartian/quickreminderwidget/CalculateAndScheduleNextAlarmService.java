package com.sickmartian.quickreminderwidget;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import org.joda.time.DateTime;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/13/16.
 */
public class CalculateAndScheduleNextAlarmService extends IntentService {

    private static final int REQUEST_CODE = 9898;

    public CalculateAndScheduleNextAlarmService() {
        super(CalculateAndScheduleNextAlarmService.class.toString());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            Timber.d("CalculateAndScheduleNextAlarmService starting");

            // Calculate next alarm
            Alarm nextAlarm = Alarm.getNextSync(Utils.getNow());

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent notificationIntent = new Intent(this, NotificationReceiver.class);

            if (nextAlarm != null) {
                DateTime alarmTime = nextAlarm.getAlarmTime().toDateTime();
                Timber.i("Next Alarm: " + alarmTime.toString());
                PendingIntent notificationPendingIntent =
                        PendingIntent.getBroadcast(this,
                                CalculateAndScheduleNextAlarmService.REQUEST_CODE,
                                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                            alarmTime.getMillis(),
                            notificationPendingIntent);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                                alarmTime.getMillis(),
                                notificationPendingIntent);
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP,
                                alarmTime.getMillis(),
                                notificationPendingIntent);
                    }
                }
            } else {
                Timber.i("Next Alarm set to none");
                PendingIntent notificationPendingIntent =
                        PendingIntent.getBroadcast(this,
                                CalculateAndScheduleNextAlarmService.REQUEST_CODE,
                                notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                alarmManager.cancel(notificationPendingIntent);
            }
        } finally {
            Timber.d("CalculateAndScheduleNextAlarmService ending");
            if (intent != null) {
                CalculateAndScheduleNextAlarmReceiver.completeWakefulIntent(intent);
            }
        }

    }
}
