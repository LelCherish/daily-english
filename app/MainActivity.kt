package com.dailyenglish.checkin

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class MainActivity : Activity() {

    private lateinit var tvDate: TextView
    private lateinit var tvStreak: TextView
    private lateinit var tvSummary: TextView
    private lateinit var tvWord: TextView
    private lateinit var tvPhonetic: TextView
    private lateinit var tvMeaning: TextView
    private lateinit var tvExample: TextView
    private lateinit var btnStamp: TextView
    private lateinit var tvHint: TextView
    private lateinit var swRemind: Switch
    private lateinit var tvRemindTime: TextView
    private lateinit var tvExactHint: TextView

    private val weekDots = mutableListOf<View>()
    private val weekDays = mutableListOf<TextView>()
    private val weekKeys = mutableListOf<String>()
    private var wordIndex = Words.indexForToday()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDate = findViewById(R.id.tvDate)
        tvStreak = findViewById(R.id.tvStreak)
        tvSummary = findViewById(R.id.tvSummary)
        tvWord = findViewById(R.id.tvWord)
        tvPhonetic = findViewById(R.id.tvPhonetic)
        tvMeaning = findViewById(R.id.tvMeaning)
        tvExample = findViewById(R.id.tvExample)
        btnStamp = findViewById(R.id.btnStamp)
        tvHint = findViewById(R.id.tvHint)
        swRemind = findViewById(R.id.swRemind)
        tvRemindTime = findViewById(R.id.tvRemindTime)
        tvExactHint = findViewById(R.id.tvExactHint)

        buildWeekStrip()
        showWord()

        btnStamp.setOnClickListener { toggleCheckIn() }
        findViewById<TextView>(R.id.btnNewWord).setOnClickListener { randomWord() }
        findViewById<TextView>(R.id.btnHistory).setOnClickListener { showHistory() }
        findViewById<View>(R.id.rowRemindTime).setOnClickListener { pickTime() }
        findViewById<View>(R.id.rowRemind).setOnClickListener { swRemind.performClick() }

        swRemind.setOnCheckedChangeListener { _, on ->
            prefs(this).edit().putBoolean(KEY_REMIND_ON, on).apply()
            ReminderScheduler.sync(this)
            if (on) askNotificationPermission()
            updateRemindUI()
        }

        tvExactHint.setOnClickListener { openExactAlarmSettings() }

        ReminderScheduler.sync(this)
        askNotificationPermission()
        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    // ---------- 界面刷新 ----------

    private fun refresh() {
        val today = Calendar.getInstance()
        tvDate.text = SimpleDateFormat("yyyy年M月d日 · EEEE", Locale.CHINA).format(today.time)

        val done = checkedDates(this).contains(dateKey())
        tvStreak.text = currentStreak(this).toString()
        tvSummary.text = "本月 ${monthCount(this)} 天 · 累计 ${totalCount(this)} 天 · 最长 ${longestStreak(this)} 天"

        if (done) {
            btnStamp.text = "已 打 卡"
            btnStamp.setBackgroundResource(R.drawable.bg_seal_done)
            btnStamp.setTextColor(resources.getColor(R.color.paper, theme))
            btnStamp.rotation = -5f
            tvHint.text = "今天已完成，点击印章可撤销"
        } else {
            btnStamp.text = "打 卡"
            btnStamp.setBackgroundResource(R.drawable.bg_seal)
            btnStamp.setTextColor(resources.getColor(R.color.accent, theme))
            btnStamp.rotation = 0f
            tvHint.text = "点击印章，记录今天的努力"
        }

        refreshWeek()
        updateRemindUI()
    }

    private fun buildWeekStrip() {
        val strip = findViewById<LinearLayout>(R.id.weekStrip)
        val density = resources.displayMetrics.density
        val letters = arrayOf("一", "二", "三", "四", "五", "六", "日")

        for (i in 0..6) {
            val col = LinearLayout(this)
            col.orientation = LinearLayout.VERTICAL
            col.gravity = Gravity.CENTER_HORIZONTAL
            col.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val letter = TextView(this)
            letter.text = letters[i]
            letter.textSize = 10f
            letter.gravity = Gravity.CENTER
            letter.setTextColor(resources.getColor(R.color.faint, theme))

            val day = TextView(this)
            day.textSize = 14f
            day.gravity = Gravity.CENTER
            day.setPadding(0, (3 * density).toInt(), 0, 0)

            val dot = View(this)
            val size = (9 * density).toInt()
            val dotLp = LinearLayout.LayoutParams(size, size)
            dotLp.topMargin = (7 * density).toInt()
            dot.layoutParams = dotLp

            col.addView(letter)
            col.addView(day)
            col.addView(dot)
            strip.addView(col)

            weekDays.add(day)
            weekDots.add(dot)
        }
    }

    private fun refreshWeek() {
        val cal = Calendar.getInstance()
        val diff = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7   // 周一为一周开始
        cal.add(Calendar.DAY_OF_YEAR, -diff)

        val todayKey = dateKey()
        val future = resources.getColor(R.color.hairline, theme)
        val ink = resources.getColor(R.color.ink, theme)
        val accent = resources.getColor(R.color.accent, theme)
        val faint = resources.getColor(R.color.faint, theme)

        for (i in 0..6) {
            val key = dateKey(cal)
            weekKeys.add(key)   // 仅用于调试，重绘时覆盖
            val isFuture = cal.timeInMillis > System.currentTimeMillis() && key != todayKey
            val isToday = key == todayKey

            val dayView = weekDays[i]
            dayView.text = cal.get(Calendar.DAY_OF_MONTH).toString()
            dayView.setTextColor(if (isToday) accent else if (isFuture) future else ink)
            if (isToday) dayView.typeface = android.graphics.Typeface.DEFAULT_BOLD

            val dot = weekDots[i]
            if (checkedDates(this).contains(key)) {
                dot.setBackgroundResource(R.drawable.bg_dot_done)
            } else {
                dot.setBackgroundResource(R.drawable.bg_dot_empty)
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    private fun updateRemindUI() {
        val on = prefs(this).getBoolean(KEY_REMIND_ON, true)
        swRemind.isChecked = on
        tvRemindTime.text = ReminderScheduler.timeText(this)

        val needExact = Build.VERSION.SDK_INT >= 31 &&
                !getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
        tvExactHint.visibility = if (needExact && on) View.VISIBLE else View.GONE
    }

    // ---------- 交互 ----------

    private fun toggleCheckIn() {
        val key = dateKey()
        val dates = checkedDates(this)
        btnStamp.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)

        if (dates.contains(key)) {
            dates.remove(key)
            saveDates(this, dates)
            btnStamp.animate()
                .scaleX(0.92f).scaleY(0.92f).rotation(0f)
                .setDuration(120)
                .withEndAction {
                    refresh()
                    btnStamp.animate().scaleX(1f).scaleY(1f)
                        .setDuration(220).setInterpolator(OvershootInterpolator(1.4f)).start()
                }.start()
        } else {
            dates.add(key)
            saveDates(this, dates)
            btnStamp.animate()
                .scaleX(0.86f).scaleY(0.86f).rotation(-5f)
                .setDuration(90)
                .withEndAction {
                    refresh()
                    btnStamp.animate().scaleX(1f).scaleY(1f)
                        .setDuration(300).setInterpolator(OvershootInterpolator(2f)).start()
                }.start()
        }
    }

    private fun showWord() {
        val w = Words.all[wordIndex]
        tvWord.text = w.word
        tvPhonetic.text = w.phonetic
        tvMeaning.text = w.meaning
        tvExample.text = w.example + "\n" + w.exampleCn
    }

    private fun randomWord() {
        var i = wordIndex
        while (i == wordIndex) i = Random.nextInt(Words.all.size)
        wordIndex = i
        showWord()
    }

    private fun pickTime() {
        val p = prefs(this)
        TimePickerDialog(
            this,
            { _, hour, minute ->
                p.edit().putInt(KEY_HOUR, hour).putInt(KEY_MINUTE, minute).apply()
                ReminderScheduler.sync(this)
                updateRemindUI()
                Toast.makeText(this, "提醒时间已设为 ${ReminderScheduler.timeText(this)}", Toast.LENGTH_SHORT).show()
            },
            p.getInt(KEY_HOUR, 21), p.getInt(KEY_MINUTE, 0), true
        ).show()
    }

    private fun showHistory() {
        val fmt = SimpleDateFormat("M月d日", Locale.CHINA)
        val parse = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val recent = checkedDates(this).sortedDescending().take(7)
            .mapNotNull { parse.parse(it) }
            .joinToString("\n") { "· ${fmt.format(it)}" }

        val msg = buildString {
            append("本月打卡：${monthCount(this@MainActivity)} 天\n")
            append("累计打卡：${totalCount(this@MainActivity)} 天\n")
            append("当前连续：${currentStreak(this@MainActivity)} 天\n")
            append("最长连续：${longestStreak(this@MainActivity)} 天\n\n")
            append("最近记录：\n")
            append(if (recent.isEmpty()) "还没有记录，今天开始吧。" else recent)
        }
        AlertDialog.Builder(this)
            .setTitle("学习记录")
            .setMessage(msg)
            .setPositiveButton("好的", null)
            .show()
    }

    // ---------- 权限 ----------

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            } catch (e: Exception) {
                Toast.makeText(this, "请在系统设置中开启「精确闹钟」权限", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
