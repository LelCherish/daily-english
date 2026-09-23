package com.dailyenglish.checkin

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

const val KEY_DATES = "checkin_dates"
const val KEY_REMIND_ON = "remind_on"
const val KEY_HOUR = "remind_hour"
const val KEY_MINUTE = "remind_minute"

fun prefs(context: Context): SharedPreferences =
    context.applicationContext.getSharedPreferences("daily_english", Context.MODE_PRIVATE)

fun dateKey(cal: Calendar = Calendar.getInstance()): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)

private fun epochDay(key: String): Long {
    val t = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(key)?.time ?: return 0L
    return (t + TimeZone.getDefault().getOffset(t)) / 86_400_000L
}

fun checkedDates(context: Context): MutableSet<String> =
    prefs(context).getStringSet(KEY_DATES, emptySet())!!.toMutableSet()

fun saveDates(context: Context, dates: Set<String>) {
    prefs(context).edit().putStringSet(KEY_DATES, HashSet(dates)).apply()
}

/** 连续天数：今天已打卡则从今天算起，否则从昨天算起 */
fun currentStreak(context: Context): Int {
    val dates = checkedDates(context)
    val cal = Calendar.getInstance()
    if (!dates.contains(dateKey(cal))) cal.add(Calendar.DAY_OF_YEAR, -1)
    var streak = 0
    while (dates.contains(dateKey(cal))) {
        streak++
        cal.add(Calendar.DAY_OF_YEAR, -1)
    }
    return streak
}

fun longestStreak(context: Context): Int {
    var best = 0
    var run = 0
    var prev: Long? = null
    for (d in checkedDates(context).map { epochDay(it) }.sorted()) {
        run = if (prev != null && d == prev + 1) run + 1 else 1
        if (run > best) best = run
        prev = d
    }
    return best
}

fun totalCount(context: Context): Int = checkedDates(context).size

fun monthCount(context: Context): Int {
    val prefix = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
    return checkedDates(context).count { it.startsWith(prefix) }
}
