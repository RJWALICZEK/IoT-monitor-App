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

class MainActivity : AppCompatActivity() {

    private lateinit var textData: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval: Long = 10000 //10 sek

    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadData()
            handler.postDelayed(this, refreshInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textData = findViewById(R.id.textData)

        //start loop
        handler.post(refreshRunnable)

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(refreshRunnable) //stop
    }

    private fun loadData() {
        RetrofitClient.api.getLatest()
            .enqueue(object : Callback<List<Measurement>> {
                override fun onResponse(
                    call: Call<List<Measurement>>,
                    response: Response<List<Measurement>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: emptyList()

                        val text = data
                            .take(5)
                            .joinToString ( "\n\n" ) {
                                "Temp: ${it.temperature}°C\n" +
                                "Humi: ${it.humidity}%\n" +
                                "Location: ${it.location}\n" +
                                "Time: ${it.ts}"
                            }

                        textData.text = text ?: "No data"
                    }

                }
                override fun onFailure(call: Call<List<Measurement>>, t: Throwable) {
                    textData.text = "Error: ${t.message}"
                }
            })
    }
}