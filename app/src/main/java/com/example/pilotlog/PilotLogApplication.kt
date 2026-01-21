package com.example.pilotlog

import android.app.Application
import com.example.pilotlog.data.AppDatabase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.pilotlog.data.Aircraft
import com.example.pilotlog.data.Flight
import java.util.Date

class PilotLogApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val flightRepository: com.example.pilotlog.data.repository.FlightRepository by lazy { 
        com.example.pilotlog.data.repository.FlightRepositoryImpl(database.flightDao()) 
    }
    val aircraftRepository: com.example.pilotlog.data.repository.AircraftRepository by lazy { 
        com.example.pilotlog.data.repository.AircraftRepositoryImpl(database.aircraftDao()) 
    }

    override fun onCreate() {
        super.onCreate()
        
        CoroutineScope(Dispatchers.IO).launch {
            val airports = listOf(
                com.example.pilotlog.data.Airport(code = "EPWA", name = "Warsaw Chopin", latitude = 52.1657, longitude = 20.9671),
                com.example.pilotlog.data.Airport(code = "EPKK", name = "Krakow Balice", latitude = 50.0777, longitude = 19.7848),
                com.example.pilotlog.data.Airport(code = "EPGD", name = "Gdansk Lech Walesa", latitude = 54.3776, longitude = 18.4662),
                com.example.pilotlog.data.Airport(code = "EPKT", name = "Katowice Pyrzowice", latitude = 50.4743, longitude = 19.0800),
                com.example.pilotlog.data.Airport(code = "EPWR", name = "Wroclaw Copernicus", latitude = 51.1027, longitude = 16.8858),
                com.example.pilotlog.data.Airport(code = "EPPO", name = "Poznan Lawica", latitude = 52.4210, longitude = 16.8263),
                com.example.pilotlog.data.Airport(code = "EPRZ", name = "Rzeszow Jasionka", latitude = 50.1100, longitude = 22.0190),
                com.example.pilotlog.data.Airport(code = "EPSC", name = "Szczecin Goleniow", latitude = 53.5847, longitude = 14.9022),
                com.example.pilotlog.data.Airport(code = "EPLL", name = "Lodz Wladyslaw Reymont", latitude = 51.7219, longitude = 19.3981),
                com.example.pilotlog.data.Airport(code = "EPMO", name = "Warsaw Modlin", latitude = 52.4511, longitude = 20.6518),
                com.example.pilotlog.data.Airport(code = "EPBK", name = "Białystok Krywlany", latitude = 53.1019, longitude = 23.1706),
                com.example.pilotlog.data.Airport(code = "EPOL", name = "Olsztyn-Mazury", latitude = 53.4818, longitude = 20.9377),
                com.example.pilotlog.data.Airport(code = "EPZG", name = "Zielona Gora Babimost", latitude = 52.1385, longitude = 15.7986),
                com.example.pilotlog.data.Airport(code = "EPBY", name = "Bydgoszcz Szwederowo", latitude = 53.0968, longitude = 17.9777),
                com.example.pilotlog.data.Airport(code = "EPLB", name = "Lublin", latitude = 51.2403, longitude = 22.7136)
            )
            airports.forEach { database.airportDao().insert(it) }

            val flightCount = database.flightDao().getCount()
            if (flightCount < 50) {
                if (flightCount > 0) {
                    database.flightDao().deleteAll()
                }

                val aircraftList = listOf(
                    Aircraft(registration = "SP-ABC", model = "Cessna 172", type = "SEP"),
                    Aircraft(registration = "SP-GLD", model = "SZD-50 Puchacz", type = "GLD"),
                    Aircraft(registration = "SP-JET", model = "F-16", type = "JET")
                )
                val aircraftIds = aircraftList.map { database.aircraftDao().insert(it).toInt() }
                
                val airportCodes = listOf("EPWA", "EPKK", "EPGD", "EPKT", "EPWR", "EPPO", "EPRZ", "EPSC", "EPLL", "EPMO")
                val remarksList = listOf("VFR flight", "Training", "Cross-country", "Touch and go", "Night flight", "Instrument approach")
                
                val flights = mutableListOf<Flight>()
                val random = java.util.Random()
                
                for (i in 0 until 50) {
                    val dep = airportCodes[random.nextInt(airportCodes.size)]
                    var arr = airportCodes[random.nextInt(airportCodes.size)]
                    while (arr == dep) arr = airportCodes[random.nextInt(airportCodes.size)]
                    
                    flights.add(Flight(
                        date = Date(System.currentTimeMillis() - random.nextInt(1000 * 60 * 60 * 24 * 365).toLong()),
                        aircraftId = aircraftIds[random.nextInt(aircraftIds.size)],
                        departureCode = dep,
                        arrivalCode = arr,
                        durationMinutes = 30 + random.nextInt(150),
                        remarks = remarksList[random.nextInt(remarksList.size)]
                    ))
                }
                flights.sortByDescending { it.date }
                flights.forEach { database.flightDao().insert(it) }
            }
        }
    }
}
