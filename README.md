README.md
# IoT Monitor App

Aplikacja Android do monitorowania danych z urządzeń IoT w czasie rzeczywistym.  
Aplikacja wyświetla temperaturę oraz wilgotność pobieraną z backendowego API i prezentuje dane na żywo przy użyciu wykresów.

## Funkcje

- Monitorowanie temperatury w czasie rzeczywistym
- Monitorowanie wilgotności w czasie rzeczywistym
- Automatyczne odświeżanie co 10 sekund
- Wykrywanie statusu urządzenia (ONLINE/OFFLINE)
- Wykres historii temperatury
- Wizualizacja temperatury z ostatnich 24 godzin
- Integracja z REST API przy użyciu Retrofit
- Obsługa wykresów MPAndroidChart
- Interfejs w stylu retro terminal

---

# Zrzuty ekranu

<p align="center">
  <img src="screenshot/1.png" width="160">
  <img src="screenshot/2.png" width="160">
  <img src="screenshot/3.png" width="160">
</p>

## Wykorzystane technologie
- Kotlin
- Android SDK
- Retrofit2
- Gson
- MPAndroidChart

## Struktura projektu
app/
├── java/pl/rjwaliczek/iot_app/
│   ├── MainActivity.kt
│   ├── RetrofitClient.kt
│   ├── ApiService.kt
│   ├── Measurement.kt
│
├── res/
│   ├── layout/
│   ├── drawable/
│   ├── values/
Endpointy API

Aplikacja komunikuje się z backendowym REST API.

Dostępne endpointy
Endpoint	Opis
/api/v1/measurements/last-hour	Dane z ostatniej godziny
/api/v1/measurements/last-24h	24 rekordy z ostatnich 24 godzin , odstęp  godzina

Model danych
data class Measurement (
    val id: Long,
    val ts: String,
    val temperature: Double,
    val humidity: Double,
    val device: String,
    val location: String
)

## Instalacja

1. Sklonuj repozytorium

2. Otwórz projekt

Uruchom projekt w:

Android Studio
3. Skonfiguruj adres backendu

Edytuj plik:

RetrofitClient.kt

Zmień:

private const val BASE_URL = "http://10.42.0.1:8080"

Przykład dla sieci lokalnej:

private const val BASE_URL = "http://192.168.1.100:8080"

4. Uruchom serwer backend

Upewnij się, że REST API działa i jest dostępne z poziomu telefonu lub emulatora Androida.

5. Uruchom aplikację

Podłącz:

telefon z Androidem
lub
emulator Android

Następnie kliknij:

Run ▶

w Android Studio.

## Zależności

Dodaj do build.gradle:

implementation 'com.squareup.retrofit2:retrofit:2.11.0' -> Retrofit
implementation 'com.squareup.retrofit2:converter-gson:2.11.0' -> MPAndroidChart

implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0' -> Wykresy


Aplikacja zawiera dwa wykresy:

Live Chart:
Ostatnie pomiary temperatury
Automatyczne odświeżanie
Wykres 24h:
Dane historyczne
Znacznik aktualnej godziny („NOW”)


## Możliwe przyszłe funkcje

Powiadomienia
Tryb jasny/ciemny
Obsługa wielu urządzeń

MIT License

Autor

Rafał Waliczek
