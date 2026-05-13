package pl.rjwaliczek.iot_app

data class Measurement (
    val id: Long,
    val ts: String,
    val temperature: Double,
    val humidity: Double,
    val device: String,
    val location: String
)