# ✈️ PilotLog - Inteligentny Dziennik Lotów

**PilotLog** to aplikacja mobilna na platformę Android, stworzona jako projekt inżynierski. Aplikacja zastępuje tradycyjny papierowy logbook pilota, oferując nowoczesne funkcjonalności takie jak automatyczne śledzenie czasu lotu, integrację z danymi pogodowymi i monitorowanie przeciążeń.

## 📱 Zrzuty Ekranu

> *Zrzuty ekranu aplikacji są dostępne w dokumentacji projektu.*

## 🌟 Główne Funkcjonalności

| Moduł | Opis |
|-------|------|
| **📔 Logbook** | Cyfrowy dziennik lotów z automatycznym sumowaniem czasu i statystykami |
| **✅ Checklisty** | Interaktywne listy kontrolne (Pre-flight, Before Takeoff, Landing) |
| **🧭 Sztuczny Horyzont** | Backup instrument wykorzystujący sensory telefonu |
| **📈 G-Force Monitor** | Pomiar przeciążeń w czasie rzeczywistym |
| **🌤️ Pogoda METAR/TAF** | Aktualne dane meteorologiczne dla lotnisk |
| **📍 Mapa Radarowa** | Wizualizacja ruchu lotniczego w czasie rzeczywistym (OpenSky API) |
| **✈️ Hangar** | Zarządzanie flotą statków powietrznych |

## 🏗️ Architektura

Aplikacja wykorzystuje wzorzec **MVVM (Model-View-ViewModel)** z następującą strukturą:

```
app/src/main/java/com/example/pilotlog/
├── data/                  # Warstwa danych
│   ├── *.kt              # Encje (Room Entity)
│   ├── *Dao.kt           # Data Access Objects
│   └── repository/       # Repozytoria (abstrakcja dostępu do danych)
├── hardware/             # Integracja z sensorami
│   ├── GForceMonitor.kt  # Akcelerometr - pomiar przeciążeń
│   ├── LocationHelper.kt # GPS - lokalizacja
│   └── CameraHelper.kt   # Aparat - zdjęcia lotów
├── network/              # Warstwa sieciowa
│   ├── RetrofitClient.kt # Konfiguracja HTTP
│   ├── WeatherService.kt # API pogodowe (CheckWX)
│   └── OpenSkyService.kt # API ruchu lotniczego
└── ui/                   # Warstwa prezentacji
    ├── *Fragment.kt      # Ekrany aplikacji
    ├── *Adapter.kt       # Adaptery RecyclerView
    ├── *ViewModel.kt     # ViewModele (logika biznesowa)
    └── *View.kt          # Custom Views (Horyzont, SpiderChart)
```

## 🛠️ Technologie

| Kategoria | Technologia |
|-----------|-------------|
| **Język** | Kotlin |
| **UI** | XML Layouts, ViewBinding, Material Design 3 |
| **Baza Danych** | Room (SQLite) z migracjami |
| **Asynchroniczność** | Kotlin Coroutines & Flow |
| **Sieć** | Retrofit 2 + OkHttp 4 |
| **Nawigacja** | Android Navigation Component |
| **Sensory** | SensorManager (Accelerometer, Gyroscope, Magnetic Field) |
| **Lokalizacja** | FusedLocationProviderClient |
| **Mapy** | Leaflet.js (WebView) |

## 🚀 Uruchomienie Projektu

### Wymagania
- Android Studio Hedgehog (2023.1.1) lub nowsze
- JDK 17+
- Android SDK 34+
- Urządzenie z Android 8.0 (API 26) lub nowszym

### Kroki

1. **Sklonuj repozytorium:**
   ```bash
   git clone https://github.com/TWOJ_USERNAME/PilotLog.git
   ```

2. **Otwórz projekt w Android Studio**

3. **Skonfiguruj klucz API (opcjonalnie):**
   
   Aby korzystać z modułu pogodowego, utwórz plik `local.properties`:
   ```properties
   WEATHER_API_KEY=twoj_klucz_checkwx_api
   ```

4. **Zbuduj i uruchom:**
   ```bash
   ./gradlew assembleDebug
   ```

## 📊 Baza Danych

Aplikacja wykorzystuje Room Database z następującymi encjami:

- **Flight** - Loty (data, trasa, czas, zdjęcie)
- **Aircraft** - Statki powietrzne (rejestracja, model, typ)
- **Airport** - Lotniska (kod ICAO, nazwa, współrzędne)

Migracje bazy danych są zdefiniowane w `AppDatabase.kt`.

## 🌐 Lokalizacja

Aplikacja obsługuje dwa języki:
- 🇬🇧 Angielski (domyślny)
- 🇵🇱 Polski

Pliki językowe:
- `res/values/strings.xml`
- `res/values-pl/strings.xml`

## 📄 Licencja

Projekt stworzony w ramach zajęć z **Programowania Aplikacji Mobilnych** na Politechnice.

---

**Autor:** *[Twoje Imię i Nazwisko]*  
**Rok akademicki:** 2024/2025
