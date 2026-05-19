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

```text
Ekran główny aplikacji prezentuje aktualne dane z czujnika IoT w czasie rzeczywistym. Wyświetlana jest bieżąca temperatura, wilgotność oraz lokalizacja urządzenia. Użytkownik widzi również status połączenia z serwerem oraz status samego urządzenia (ONLINE/OFFLINE), który określany jest na podstawie czasu ostatniego pomiaru.
Na ekranie znajdują się wykresy przedstawiające historię zmian temperatury — zarówno z ostatnich kilku minut, jak i z ostatnich 24 godzin (z rozdzielczością godzinową). Dodatkowo prezentowane są aktualne trendy zmian temperatury i wilgotności, które są automatycznie odświeżane co kilka sekund.
```
## Wykorzystane technologie
- Kotlin
- Android SDK
- Retrofit2
- Gson
- MPAndroidChart

## Struktura projektu

```text
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
```


Aplikacja komunikuje się z backendowym REST API.

Dostępne endpointy
```text
| `/api/v1/measurements/last-hour` | Dane z ostatniej godziny |
| `/api/v1/measurements/last-24h` | 24 rekordy z ostatnich 24 godzin (co 1h) |
```
```kotlin
Model danych
data class Measurement (
    val id: Long,
    val ts: String,
    val temperature: Double,
    val humidity: Double,
    val device: String,
    val location: String
)
```

## Instalacja

1. Sklonuj repozytorium
2. Otwórz projekt w Android Studio
3. Skonfiguruj adres backendu edytując plik RetrofitClient.kt:
```text
Zmień:
private const val BASE_URL = "http://10.42.0.1:8080"

Przykład dla sieci lokalnej:
private const val BASE_URL = "http://192.168.1.100:8080"
```
4. Uruchom serwer backend i upewnij się, że REST API działa i jest dostępne z poziomu telefonu lub emulatora Androida.
5. Uruchom aplikację podłączając telefon z Androidem lub emulator Android
6.kliknij Run ▶


## Zależności

w pliku build.gradle dodaj zależności :
```text
implementation 'com.squareup.retrofit2:retrofit:2.11.0'
implementation 'com.squareup.retrofit2:converter-gson:2.11.0' // JSON
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0' // wykresy
```

## Możliwe przyszłe funkcje

Powiadomienia
Tryb jasny/ciemny
Obsługa wielu urządzeń

MIT License

Autor

Rafał Waliczek
