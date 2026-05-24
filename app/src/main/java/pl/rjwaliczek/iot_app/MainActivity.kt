package pl.rjwaliczek.iot_app

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private lateinit var tempText: TextView
    private lateinit var humiText: TextView
    private lateinit var locationText: TextView
    private lateinit var statusText: TextView
    private lateinit var lineChart: LineChart
    private lateinit var dayChart: LineChart

    private lateinit var deviceStatusText: TextView
    private lateinit var trendTemp: TextView
    private lateinit var trendHumi: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval: Long = 10000

    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadData()
            loadTrend()
            loadDayChart()          // ← dodane
            handler.postDelayed(this, refreshInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        lineChart = findViewById(R.id.lineChart)
        dayChart = findViewById(R.id.dayChart)

        tempText = findViewById(R.id.tempText)
        humiText = findViewById(R.id.humiText)
        locationText = findViewById(R.id.locationText)
        statusText = findViewById(R.id.serverStatusText)
        trendTemp = findViewById(R.id.trendTemp)
        trendHumi = findViewById(R.id.trendHumi)
        deviceStatusText = findViewById(R.id.deviceStatusText)

        dayChart.isHighlightPerTapEnabled = false
        dayChart.isHighlightPerDragEnabled = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        handler.post(refreshRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(refreshRunnable)
    }

    // ==================== LOAD DAY CHART ====================
    private fun loadDayChart() {
        RetrofitClient.api.getLast24h()
            .enqueue(object : Callback<List<Measurement>> {
                override fun onResponse(
                    call: Call<List<Measurement>>,
                    response: Response<List<Measurement>>
                ) {
                    val data = response.body() ?: emptyList()
                    updateDayChart(data)
                }

                override fun onFailure(call: Call<List<Measurement>>, t: Throwable) {
                    dayChart.clear()
                }
            })
    }

    // ---------------- DAY CHART (24h) ----------------
    private fun updateDayChart(data: List<Measurement>) {
        if (data.isEmpty()) {
            dayChart.clear()
            return
        }

        val entries = data.map { item ->
            val offsetDateTime = java.time.OffsetDateTime.parse(item.ts)
            val hourOfDay = offsetDateTime.hour + (offsetDateTime.minute / 60f)
            Entry(hourOfDay, item.temperature.toFloat())
        }

        val dataSet = LineDataSet(entries, "Temperatura").apply {
            color = Color.parseColor("#07e8e1")
            lineWidth = 2.8f
            setDrawCircles(true)
            circleRadius = 3.8f
            setCircleColor(Color.WHITE)
            setDrawValues(false)
            mode = LineDataSet.Mode.LINEAR
            cubicIntensity = 0.22f
            highLightColor = Color.YELLOW
        }

        dayChart.apply {
            this.data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            isScaleYEnabled = false
            setVisibleXRangeMinimum(3f)      // minimum zoom
            setVisibleXRangeMaximum(12f)     // domyślny zakres
        }

        // ---------------- OŚ X ----------------
        dayChart.xAxis.apply {
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            granularity = 1f                    // etykiety co 1 godzinę
            labelCount = 12                     // maksymalna liczba etykiet
            textColor = Color.WHITE
            textSize = 11.5f
            axisMinimum = 0f
            axisMaximum = 23.9f

            // Dynamiczny ValueFormatter
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val hour = value.toInt()

                    // Pobieramy aktualny widoczny zakres
                    val visibleRange = dayChart.visibleXRange

                    return when {
                        visibleRange > 10f -> {           // mocno oddalony → co 4 godziny
                            if (hour % 4 == 0) "$hour:00" else ""
                        }
                        visibleRange > 6f -> {            // średnio oddalony → co 2 godziny
                            if (hour % 2 == 0) "$hour:00" else ""
                        }
                        else -> {                         // przybliżony → co 1 godzinę
                            "$hour:00"
                        }
                    }
                }
            }

            setDrawGridLines(true)
            gridColor = Color.parseColor("#444444")
            enableGridDashedLine(10f, 8f, 0f)
        }

        // ---------------- OŚ Y ----------------
        dayChart.axisLeft.apply {
            textColor = Color.WHITE
            textSize = 12f
            setDrawGridLines(true)
            gridColor = Color.parseColor("#444444")
        }
        dayChart.axisRight.isEnabled = false

        // ---------------- LINIA "TERAZ" ----------------
        val currentHour = java.time.LocalDateTime.now().hour.toFloat() +
                java.time.LocalDateTime.now().minute / 60f

        val nowLimitLine = LimitLine(currentHour).apply {
            lineColor = Color.parseColor("#07e8e1")
            lineWidth = 2.3f
            textColor = Color.parseColor("#07e8e1")
            textSize = 11f
            label = "NOW"
            labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
        }

        dayChart.xAxis.removeAllLimitLines()
        dayChart.xAxis.addLimitLine(nowLimitLine)

        dayChart.invalidate()
    }

    // ==================== POZOSTAŁE FUNKCJE \ ====================
    private fun updateChart(data: List<Measurement>) {
        val entries = data.take(20).reversed().mapIndexed { index, item ->
            Entry(index.toFloat(), item.temperature.toFloat())
        }

        val dataSet = LineDataSet(entries, "Temperature").apply {
            color = Color.parseColor("#07e8e1")
            lineWidth = 2f
            setDrawCircles(true)
            circleRadius = 3f
            setCircleColor(Color.LTGRAY)
            valueTextColor = Color.WHITE
            setDrawValues(false)
            highLightColor = Color.RED
        }
        lineChart.description.isEnabled = false
        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
    }

    private fun loadData() {
        RetrofitClient.api.getLatest()
            .enqueue(object : Callback<List<Measurement>> {
                override fun onResponse(call: Call<List<Measurement>>, response: Response<List<Measurement>>) {
                    statusText.text = "ONLINE"
                    statusText.setTextColor(Color.GREEN)

                    val data = response.body() ?: emptyList()
                    val latest = data.firstOrNull()

                    if (latest != null) {
                        tempText.text = "${String.format("%.1f", latest.temperature)}°C  ."
                        humiText.text = "${String.format("%.1f", latest.humidity)}%  ."
                        locationText.text = latest.location

                        try {
                            val measurementTime = Instant.parse(latest.ts)
                            val seconds = Duration.between(measurementTime, Instant.now()).seconds

                            if (seconds <= 30) {
                                deviceStatusText.text = "ONLINE"
                                deviceStatusText.setTextColor(Color.GREEN)
                            } else {
                                deviceStatusText.text = "OFFLINE"
                                deviceStatusText.setTextColor(Color.RED)
                            }
                        } catch (e: Exception) {
                            deviceStatusText.text = "NO DATA"
                            deviceStatusText.setTextColor(Color.GRAY)
                        }
                    } else {
                        tempText.text = "-- °C  ."
                        humiText.text = "-- %  ."
                        locationText.text = "-------"
                        statusText.text = "OFFLINE"
                        deviceStatusText.text = "OFFLINE"
                        deviceStatusText.setTextColor(Color.RED)

                        statusText.setTextColor(Color.RED)
                        trendTemp.text = "--°C/h"
                        trendHumi.text = "--%/h"
                    }
                    updateChart(data)
                }

                override fun onFailure(call: Call<List<Measurement>>, t: Throwable) {
                    tempText.text = "-- °C  ."
                    humiText.text = "-- %  ."
                    locationText.text = "-------"
                    statusText.text = "OFFLINE"
                    deviceStatusText.text = "OFFLINE"
                    deviceStatusText.setTextColor(Color.RED)

                    statusText.setTextColor(Color.RED)
                    trendTemp.text = "--°C/h"
                    trendHumi.text = "--%/h"
                }
            })
    }

    private fun loadTrend() {
        RetrofitClient.api.getLastHour()
            .enqueue(object : Callback<List<Measurement>> {
                override fun onResponse(
                    call: Call<List<Measurement>>,
                    response: Response<List<Measurement>>
                ) {

                    val data = response.body() ?: emptyList()

                    if (data.size >= 2) {
                        val newest = data.first()
                        val oldest = data.last()

                        val tempTrend = newest.temperature - oldest.temperature
                        val humiTrend = newest.humidity - oldest.humidity

                        trendTemp.text = "${String.format("%.1f", tempTrend)}°C/h"
                        trendHumi.text = "${String.format("%.1f", humiTrend)}%/h"
                    } else {
                        trendTemp.text = "--°C/h"
                        trendHumi.text = "--%/h"
                    }
                }

                override fun onFailure(call: Call<List<Measurement>>, t: Throwable) {}
            })
    }
}