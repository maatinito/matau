package pf.miki.matau.helpers

import android.content.Context
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderHeaderAware
import com.opencsv.CSVReaderHeaderAwareBuilder
import pf.miki.matau.R
import java.io.InputStreamReader
import java.io.Reader

class PLocation(val city: String, val superset: List<PLocation>, var population: Int = 0) {

    // returns englobing locations with the input location
    // for example Punauuia in include in Grand_Papette included in Tahiti included in Ils sous le vent
    companion object {
        var locations = mutableMapOf<String, PLocation>()
        val norm = "[- ']|\\([^)]*\\)".toRegex()

        fun init(context: Context) {
            val input = context.resources.openRawResource(R.raw.polynesian_postal_codes)
            val parser = CSVParserBuilder().withSeparator('\t').build()
            val reader2 = CSVReaderHeaderAwareBuilder(InputStreamReader(input, "UTF-8") as Reader).withCSVParser(parser).build() as CSVReaderHeaderAware
            while (true) {
                var fields = reader2.readNext("Commune", "Region", "Ile", "Archipel", "Population")
                if (fields != null) {
                    val population = fields[fields.count() - 1]
                    var superset: List<PLocation> = listOf()
                    for (i in fields.count() - 2 downTo 0) {
                        val it = fields[i]
                        if (it.isNotBlank() && (i != 0 || population.isNotEmpty())) {
                            val entry = norm.replace(it.toLowerCase(), "")
                            var l = locations.get(entry)
                            if (l == null) {
                                l = PLocation(it, superset)
                                locations.put(entry, l)
                            }
                            if (i == 0 && population.isNotEmpty()) {
                                l.population = population.toInt()
//                            if (l.city.contains("Tahiti"))
                                println("Population de ${l.city}=${l.population}")
                            }
                            superset = listOf(l)
                        }
                    }
                } else
                    break
            }
//            for ((k,l) in locations) {
//                print(l.city + '('+k+')')
//                var sl = l
//                while (sl.superset.isNotEmpty()) {
//                    sl = sl.superset.first();
//                    print(" --> " + sl.city)
//                }
//                println()
//            }
            println(getLocations().joinToString())
        }

        fun getLocation(location: String): PLocation? {
            val entry = norm.replace(location.toLowerCase(), "")
            var l = locations.get(entry)
            return l
        }

        fun getLocations(): List<String> {
            return locations.values.sortedWith(compareBy({ it.population < 6000 }, PLocation::city)).map { it.city }
        }
    }
}
