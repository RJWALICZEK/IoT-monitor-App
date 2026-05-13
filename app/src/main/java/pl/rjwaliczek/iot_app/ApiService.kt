package pl.rjwaliczek.iot_app

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("api/v1/measurements/last-hour")
    fun getLatest(): Call<List<Measurement>>
}