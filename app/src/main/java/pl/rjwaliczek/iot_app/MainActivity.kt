package pl.rjwaliczek.iot_app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import android.graphics.Color
import com.github.mikephil.charting.formatter.ValueFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var tempText: TextView
    private lateinit var humiText: TextView
    private lateinit var locationText: TextView
    private lateinit var statusText: TextView
    private lateinit var lineChart: LineChart

    private lateinit var trendTemp: TextView

    private lateinit var trendHumi: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval: Long = 10000 //10 sek

    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadData()
            loadTrend()
            handler.postDelayed(this, refreshInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        lineChart = findViewById(R.id.lineChart)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tempText = findViewById(R.id.tempText)
        humiText = findViewById(R.id.humiText)
        locationText = findViewById(R.id.locationText)
        statusText = findViewById(R.id.statusText)
        trendTemp = findViewById(R.id.trendTemp)
        trendHumi = findViewById(R.id.trendHumi)


        //start loop
        handler.post(refreshRunnable)

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(refreshRunnable) //stop
    }
    private fun updateChart(data: List<Measurement>) {

        val entries = data.mapIndexed { index, item ->
            Entry(index.toFloat(), item.temperature.toFloat())
        }


        val dataSet = LineDataSet(entries, "Temperature").apply {
            color = Color.CYAN
            valueTextColor = Color.WHITE
            lineWidth = 2f
            setDrawCircles(true)
            circleRadius = 3f

            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }


        val lineData = LineData(dataSet)

        lineChart.data = lineData
        lineChart.invalidate()
    }
    private fun loadData() {
        RetrofitClient.api.getLatest()
            .enqueue(object : Callback<List<Measurement>> {
                override fun onResponse(
                    call: Call<List<Measurement>>,
                    response: Response<List<Measurement>>
                ) {
                    if (response.isSuccessful) {
                        statusText.setTextColor(Color.GREEN)
                        statusText.text= "ONLINE"

                        val data = response.body() ?: emptyList()
                        val latest = data.firstOrNull()


                        if (latest != null) {
                            tempText.text = "${latest.temperature}°C  ."
                            humiText.text = "${latest.humidity}%  ."
                            locationText.text = latest.location
                        } else {
                            tempText.text = "-- °C"
                            humiText.text = "-- %"
                            locationText.text = "---"

                        }
                        updateChart(data.take(20).reversed())
                        lineChart.axisLeft.axisMinimum = -10f
                        lineChart.axisLeft.axisMaximum = 30f
                        lineChart.axisLeft.labelCount = 8

                        lineChart.axisRight.isEnabled = false

                        lineChart.description.isEnabled = false
                        lineChart.animateX(500)


                    }

                }

                override fun onFailure(call: Call<List<Measurement>>, t: Throwable) {
                    tempText.text = "-- °C"
                    humiText.text = "-- %"
                    locationText.text = "---"
                    statusText.setTextColor(Color.RED)
                    statusText.text = "OFFLINE"
                    trendTemp.text = "---°C/h"
                    trendHumi.text = "---%/h"

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
                    if (response.isSuccessful) {
                        val data = response.body() ?: emptyList()

                        if (data.size >= 2) {
                            val newest = data.first()
                            val oldest = data.last()

                            val humiTrend = newest.humidity - oldest.humidity
                            val tempTrend = newest.temperature - oldest.temperature
                            val tempArrow =
                                if (tempTrend > 0) "▲"
                                else if (tempTrend < 0) "▼"
                                else "■"

                            trendTemp.text = "${tempArrow}${String.format("%.0f", tempTrend)}°C/h"

                            val humiPrefix = if(humiTrend > 0) "+" else ""
                            trendHumi.text = "${humiPrefix}${String.format("%.0f", humiTrend)}%/h"

                            trendTemp.setTextColor(
                                if(tempTrend > 0) {
                                    Color.RED
                                }
                                else if (tempTrend < 0) {
                                    Color.BLUE
                                }
                                else {
                                    Color.WHITE
                                }
                            )
                            trendHumi.setTextColor(
                                if(humiTrend > 0) {
                                    Color.RED
                                }
                                else if (humiTrend < 0) {
                                    Color.WHITE
                                }
                                else {
                                    Color.CYAN
                                }
                            )
                        } else {
                            trendTemp.text = "---°C/h"
                            trendHumi.text = "---%/h"
                        }
                    }
                }
                override fun onFailure(call: Call<List<Measurement>>, t: Throwable) {
                    trendTemp.text = "---°C/h"
                    trendHumi.text = "---%/h"
                }
            })

    }

}