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
    private val hourHandler = Handler(Looper.getMainLooper())
    private val refreshInterval: Long = 10000   //co 10 sek
    private val hourRefreshInterval: Long = 3600000  //co godzine

    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadData()
            loadTrend()
            handler.postDelayed(this, refreshInterval)
        }
    }
    private val hourRunable = object : Runnable {
        override fun run() {
            loadDayChartData()
            hourHandler.postDelayed(this, hourRefreshInterval)
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

        dayChart.setBackgroundColor(Color.TRANSPARENT)
        dayChart.isHighlightPerTapEnabled = false
        dayChart.isHighlightPerDragEnabled = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        handler.post(refreshRunnable)
        handler.post(hourRunable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(refreshRunnable)
        handler.removeCallbacks(hourRunable)
    }

    // ---------------- LINE CHART ----------------



    // ---------------- LOAD DATA ----------------

    private fun loadData() {

        RetrofitClient.api.getLatest()
            .enqueue(object : Callback<List<Measurement>> {

                override fun onResponse(
                    call: Call<List<Measurement>>,
                    response: Response<List<Measurement>>
                ) {

                    if (response.isSuccessful) {
                        if (statusText.text != "ONLINE") {
                            loadDayChartData()
                        }
                        statusText.text = "ONLINE"
                        statusText.setTextColor(Color.GREEN)

                        val data = response.body() ?: emptyList()
                        val latest = data.firstOrNull()

                        if (latest != null) {

                            tempText.text = "${String.format("%.1f", latest.temperature)}°C ."
                            humiText.text = "${String.format("%.1f", latest.humidity)}% ."
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
                                deviceStatusText.text = "OFFLINE"
                                deviceStatusText.setTextColor(Color.RED)
                                statusText.text = "OFFLINE"
                                statusText.setTextColor(Color.RED)
                            }

                        } else {
                            deviceStatusText.text = "OFFLINE"
                            deviceStatusText.setTextColor(Color.RED)
                            statusText.text = "OFFLINE"
                            statusText.setTextColor(Color.RED)
                            tempText.text = "-- °C"
                            humiText.text = "-- %"
                            trendTemp.text = "--°C/h"
                            trendHumi.text = "--%/h"
                            locationText.text = "-------"
                        }

                        updateChart(data)

                    } else {
                        statusText.text = "OFFLINE"
                        statusText.setTextColor(Color.RED)
                    }
                }

                override fun onFailure(call: Call<List<Measurement>>, t: Throwable) {
                    statusText.text = "OFFLINE"
                    statusText.setTextColor(Color.RED)
                    deviceStatusText.text = "OFFLINE"
                    deviceStatusText.setTextColor(Color.RED)
                    tempText.text = "-- °C"
                    humiText.text = "-- %"
                    locationText.text = "-------"
                    trendTemp.text = "--°C/h"
                    trendHumi.text = "--%/h"
                }
            })
    }

    private fun loadDayChartData() {

        RetrofitClient.api.getLast24h().enqueue(object: Callback<List<Measurement>> {
            override fun onResponse( call: Call<List<Measurement>>, response: Response<List<Measurement>>) {
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    updateDayChart(data)
                }
            }

            override fun onFailure(p0: Call<List<Measurement>>, t: Throwable) {
                t.printStackTrace()
            }
                })
            }
    // ---------------- TREND ----------------

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




                        trendTemp.text = "${if (tempTrend > 0) "+" else ""}${"%.1f".format(tempTrend)}°C/h"
                        trendHumi.text = "${if (humiTrend > 0) "+" else ""}${"%.1f".format(humiTrend)}%/h"

                    } else {
                        trendTemp.text = "--°C/h"
                        trendHumi.text = "--%/h"
                    }
                }

                override fun onFailure(call: Call<List<Measurement>>, t: Throwable) {}
            })
    }

    // ---------------- CHART 24h ----------------

    private fun updateDayChart(data: List<Measurement>) {
        val hourMap = mutableMapOf<Int, Float?>()
        for (hour in 0..23) {
            hourMap[hour] = null
        }
        data.forEach { item ->

            try {
                val instant = Instant.parse(item.ts)
                val hour = instant.atZone(java.time.ZoneId.systemDefault()).hour

                hourMap[hour] = item.temperature.toFloat()

            } catch (_: Exception) {
            }
        }
        val currentHour = java.time.LocalDateTime.now().hour



        val validEntries = mutableListOf<Entry>()
        val missingEntries = mutableListOf<Entry>()
        val minTemp = data.minOfOrNull { it.temperature }?.toFloat() ?: 0f
        hourMap.forEach { (hour, temp) ->

            if (temp != null) {
                validEntries.add(
                    Entry(hour.toFloat(), temp)
                )
            } else {
                missingEntries.add(
                    Entry(hour.toFloat(), minTemp - 2f)                )
            }
        }
        val missingDataSet = LineDataSet(missingEntries, "No Data").apply {

            color = Color.RED
            lineWidth = 0f

            setDrawCircles(true)
            circleRadius = 6f
            setCircleColor(Color.RED)

            setDrawValues(false)

            setDrawHighlightIndicators(false)
        }
        val validDataSet = LineDataSet(validEntries, "Temperature").apply {

            color = Color.parseColor("#07e8e1")
            lineWidth = 2f

            setDrawCircles(true)
            circleRadius = 4f
            setCircleColor(Color.WHITE)

            setDrawValues(true)
            valueTextColor = Color.WHITE
            setValueFormatter(object : ValueFormatter() {
                override fun getPointLabel(entry: Entry?): String {
                    return "${entry?.y?.toInt()}°C"
                }
            })

            highLightColor = Color.parseColor("#07e8e1")

        }

        dayChart.data = LineData(validDataSet, missingDataSet)

        dayChart.xAxis.apply {
            granularity = 1f
            labelCount = 6
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            axisMinimum = 0f
            axisMaximum = 23f

            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()}:00"
                }
            }

            removeAllLimitLines()
        }
        dayChart.axisLeft.apply {
            granularity = 1f

            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        dayChart.axisRight.isEnabled = false
        dayChart.description.isEnabled = false
        dayChart.legend.isEnabled = true

        val nowLine = LimitLine(currentHour.toFloat()).apply {
            lineColor = Color.parseColor("#07e8e1")
            lineWidth = 2f
            label = "NOW"
            labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
            textColor = Color.parseColor("#07e8e1")
        }

        dayChart.xAxis.addLimitLine(nowLine)
        dayChart.highlightValue(currentHour.toFloat(), 0)
        dayChart.setVisibleXRangeMaximum(8f)
        dayChart.isDragEnabled = true
        dayChart.setScaleEnabled(false)
        dayChart.invalidate()
    }
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
        lineChart.xAxis.isEnabled = false
        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
    }
}