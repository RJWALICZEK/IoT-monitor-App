package pl.rjwaliczek.iot_app

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("api/v1/measurements/last-hour")  //get lfrom last hour
    fun getLastHour(): Call<List<Measurement>>

    @GET("api/v1/measurements/current") //get latest one
    fun getCurrent(): Call<List<Measurement>>

    @GET("api/v1/measurements/latest") //last 50 measurements
    fun getLatest(): Call<List<Measurement>>

    @GET("api/v1/measurements/all")
    fun getAll(): Call<List<Measurement>>

    @GET("api/v1/measurements/last-24h")
    fun getLast24h(): Call<List<Measurement>>
}