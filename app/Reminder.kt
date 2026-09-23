package com.dailyenglish.checkin

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.*

object ReminderScheduler {

    fun sync(context: Context) {
        if (prefs(context).getBoolean(KEY_REMIND_ON, true)) schedule(context) else cancel(context)
    }

    fun timeText(context: Context): String {
        val p = prefs(context)
        return String.format(Locale.CHINA, "%02d:%02d", p.getInt(KEY_HOUR, 21), p.getInt(KEY_MINUTE, 0))
    }

    fun nextTriggerMillis(context: Context): Long {
        val p = prefs(context)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, p.getInt(KEY_HOUR, 21))
            set(Calendar.MINUTE, p.getInt(KEY_MINUTE, 0))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR, 1)
        return cal.timeInMillis
    }

    private fun pendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context, 1001,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun schedule(context: Context) {
        val am = context.getSystemService(AlarmManager::class.java)
        val pi = pendingIntent(context)
        val at = nextTriggerMillis(context)
        try {
            am.setAlarmClock(AlarmManager.AlarmClockInfo(at, pi), pi)
        } catch (e: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi) // 没有精确闹钟权限时降级
        }
    }

    private fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context))
    }
}

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val p = prefs(context)
        if (!p.getBoolean(KEY_REMIND_ON, true)) return

        val manager = context.getSystemService(NotificationManager::class.java)
        val channelId = "daily_remind"
        val channel = NotificationChannel(channelId, "每日提醒", NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel)

        val text = if (checkedDates(context).contains(dateKey()))
            "今天已打卡，学过的内容睡前再看一眼更牢固。"
        else
            "今天还没打卡，学一个单词只要一分钟。"

        val tap = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle("每日学英语")
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setContentIntent(tap)
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)

        // 排好下一天的提醒
        ReminderScheduler.sync(context)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) ReminderScheduler.sync(context)
    }
}
