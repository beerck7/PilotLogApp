package com.example.pilotlog.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.pilotlog.PilotLogApplication
import com.example.pilotlog.data.Flight
import com.example.pilotlog.data.FlightWithAircraft
import com.example.pilotlog.data.AppDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.asLiveData

class FlightViewModel(application: Application) : AndroidViewModel(application) {
    private val flightRepository = (application as PilotLogApplication).flightRepository
    private val aircraftRepository = (application as PilotLogApplication).aircraftRepository
    
    private val searchQuery = MutableLiveData<String>("")

    val allFlights: LiveData<List<FlightWithAircraft>> = searchQuery.switchMap { query ->
        if (query.isNullOrEmpty()) {
            flightRepository.getAllFlights().asLiveData()
        } else {
            flightRepository.searchFlights(query).asLiveData()
        }
    }

    fun searchFlights(query: String) {
        searchQuery.value = query
    }

    fun getFlightById(id: Int): LiveData<Flight?> {
        val result = MutableLiveData<Flight?>()
        viewModelScope.launch {
            result.postValue(flightRepository.getFlightById(id))
        }
        return result
    }
    
    val allAircrafts: LiveData<List<com.example.pilotlog.data.Aircraft>> = aircraftRepository.getAllAircraft().asLiveData()
    val allAirports: LiveData<List<com.example.pilotlog.data.Airport>> = (application as PilotLogApplication).database.airportDao().getAll()

    val totalFlightTime: LiveData<Int?> = flightRepository.getTotalFlightTime().asLiveData()
    val totalFlights: LiveData<Int> = flightRepository.getFlightCount().asLiveData()
    
    val last30DaysTime: LiveData<Int?> = flightRepository.getFlightTimeSince(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000).asLiveData()

    private val _weatherMetar = androidx.lifecycle.MutableLiveData<String>()
    val weatherMetar: LiveData<String> get() = _weatherMetar

    fun fetchWeather(code: String, apiKey: String) = viewModelScope.launch {
        try {
            val response = com.example.pilotlog.network.RetrofitClient.instance.getMetar(code, apiKey)
            if (response.results > 0 && response.data.isNotEmpty()) {
                _weatherMetar.postValue(response.data[0])
            } else {
                _weatherMetar.postValue("No METAR found for $code")
            }
        } catch (e: Exception) {
            _weatherMetar.postValue("Weather Error: ${e.message}")
        }
    }

    fun insert(flight: Flight) = viewModelScope.launch {
        flightRepository.insert(flight)
    }
    
    fun update(flight: Flight) = viewModelScope.launch {
        flightRepository.update(flight)
    }

    fun insertAircraft(aircraft: com.example.pilotlog.data.Aircraft) = viewModelScope.launch {
        aircraftRepository.insert(aircraft)
    }

    fun clearData() = viewModelScope.launch {
        flightRepository.deleteAll()
        aircraftRepository.deleteAll()
    }

    fun deleteFlight(flight: Flight) = viewModelScope.launch {
        flightRepository.delete(flight)
    }

    fun deleteAircraft(aircraft: com.example.pilotlog.data.Aircraft) = viewModelScope.launch {
        aircraftRepository.delete(aircraft)
    }

    fun seedData() = viewModelScope.launch {
        flightRepository.deleteAll()
        aircraftRepository.deleteAll()

        val puchacz = com.example.pilotlog.data.Aircraft(
            registration = "SP-3788",
            model = "SZD-50-3 Puchacz",
            type = "GLD"
        )
        val junior = com.example.pilotlog.data.Aircraft(
            registration = "SP-3372",
            model = "SZD-51-1 Junior",
            type = "GLD"
        )
        
        val puchaczId = aircraftRepository.insert(puchacz)
        val juniorId = aircraftRepository.insert(junior)
        
        val flights = mutableListOf<com.example.pilotlog.data.Flight>()
        val calendar = java.util.Calendar.getInstance()
        
        val exercises = listOf(
            "Circuit training (Krąg nadlotniskowy)",
            "Stall recovery (Przeciągnięcia)",
            "Spin avoidance (Korkociągi)",
            "Thermal soaring (Termika)",
            "Spot landing (Lądowanie w punkcie)",
            "Emergency landing practice (Lądowanie w terenie)",
            "High bank turns (Zakręty głębokie)",
            "Side-slip (Ześlizg)",
            "Winch launch failure (Awaria wyciągarki)",
            "Check flight (Lot sprawdzający)"
        )

        repeat(55) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            flights.add(com.example.pilotlog.data.Flight(
                date = calendar.time,
                aircraftId = puchaczId.toInt(),
                departureCode = "EPPL",
                arrivalCode = "EPPL",
                durationMinutes = (4..8).random(),
                remarks = exercises.random(),
                launchMethod = "Winch",
                releaseHeight = (300..450).random()
            ))
        }
        
        repeat(11) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -2)
            flights.add(com.example.pilotlog.data.Flight(
                date = calendar.time,
                aircraftId = juniorId.toInt(),
                departureCode = "EPPL",
                arrivalCode = "EPPL",
                durationMinutes = (4..15).random(),
                remarks = "Solo: ${exercises.random()}",
                launchMethod = "Aerotow",
                releaseHeight = (500..800).random()
            ))
        }
        
        flights.forEach { flightRepository.insert(it) }
    }

    fun importFromHtml(html: String, clearBefore: Boolean, onResult: (Boolean, Int, String?) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            if (clearBefore) {
                flightRepository.deleteAll()
                aircraftRepository.deleteAll()
            }

            val doc = org.jsoup.Jsoup.parse(html)
            val table = doc.select("table.table-striped").first()
            
            if (table == null) {
                onResult(false, 0, "Table not found")
                return@launch
            }

            val headerRow = table.select("thead tr th")
            val headers = headerRow.mapIndexed { index, element -> 
                element.text().trim() to index 
            }.toMap()

            fun getIndex(key: String): Int? {
                return headers.entries.find { it.key.contains(key, ignoreCase = true) }?.value
            }

            val dateIdx = getIndex("Data")
            val regIdx = getIndex("SP")
            val remarksIdx = getIndex("Zad./Ćw.") ?: getIndex("Zad")
            val depIdx = getIndex("Lotn. odl.") ?: getIndex("Start")
            val arrIdx = getIndex("Lotn. Przyl.") ?: getIndex("Ląd")
            val durationIdx = getIndex("W pow.") ?: getIndex("Czas")

            if (dateIdx == null || regIdx == null) {
                 onResult(false, 0, "Critical columns (Data, SP) not found")
                 return@launch
            }

            val rows = table.select("tbody tr")
            var count = 0
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

            rows.forEach { row ->
                val cols = row.select("td")
                val maxIdx = listOfNotNull(dateIdx, regIdx, remarksIdx, depIdx, arrIdx, durationIdx).maxOrNull() ?: 0
                
                if (cols.size > maxIdx) {
                    val dateStr = cols[dateIdx].text().trim()
                    val rawReg = cols[regIdx].text().trim()
                    
                    if (dateStr.isNotEmpty() && rawReg.isNotEmpty()) {
                        val date = try { dateFormat.parse(dateStr) } catch (e: Exception) { null }
                        
                        if (date != null) {
                            val remarks = if (remarksIdx != null) cols[remarksIdx].text().trim() else ""
                            val dep = if (depIdx != null) cols[depIdx].text().trim() else ""
                            val arr = if (arrIdx != null) cols[arrIdx].text().trim() else ""
                            val durationStr = if (durationIdx != null) cols[durationIdx].text().trim() else "00:00"
                            
                            val duration = parseDuration(durationStr)
                            
                            val regRegex = Regex("(SP-[A-Z0-9]+)", RegexOption.IGNORE_CASE)
                            val match = regRegex.find(rawReg)
                            
                            val registration = match?.value?.uppercase() ?: "UNKNOWN"
                            val model = if (match != null) {
                                rawReg.substring(0, match.range.first).trim()
                            } else {
                                "Unknown"
                            }
                            
                            val aircraftId = getOrCreateAircraftId(registration, model)
                            
                            val flight = com.example.pilotlog.data.Flight(
                                date = date,
                                aircraftId = aircraftId,
                                departureCode = dep,
                                arrivalCode = arr,
                                durationMinutes = duration,
                                remarks = remarks,
                                launchMethod = "Unknown",
                                releaseHeight = 0
                            )
                            flightRepository.insert(flight)
                            count++
                        }
                    }
                }
            }
            onResult(true, count, null)
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false, 0, e.message)
        }
    }

    private fun parseDuration(timeStr: String): Int {
        val parts = timeStr.split(":")
        if (parts.size == 2) {
            return (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
        }
        return 0
    }

    private suspend fun getOrCreateAircraftId(reg: String, model: String): Int {
        val existing = aircraftRepository.getByRegistration(reg)
        if (existing != null) {
            if (existing.model == "Unknown" && model.isNotEmpty() && model != "Unknown") {
                 val updated = existing.copy(model = model)
                 aircraftRepository.update(updated)
            }
            return existing.id
        }
        
        val newAircraft = com.example.pilotlog.data.Aircraft(
            registration = reg,
            model = if (model.isNotEmpty()) model else "Unknown",
            type = "GLD" 
        )
        val newId = aircraftRepository.insert(newAircraft)
        return newId.toInt()
    }
}
