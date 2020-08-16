package pf.miki.matau.repository

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.*
import android.arch.persistence.room.migration.Migration
import android.content.Context
import android.os.AsyncTask
import android.support.v7.util.DiffUtil
import java.util.*


// PAd = Persistent ad

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Entity(primaryKeys = ["id"], indices = [Index("fcpPrice"), Index("date"), Index("pinned"), Index("lastViewed")])
data class PAd @Ignore constructor(
        var source: String = "",
        var id: String = "",
        var title: String = "",
        var fcpPrice: Int = 0,
        var vignette: String = "",
        var description: String = "",
        var date: Date = Date(),
        var contact: String = "",
        var location: String = ""//,
//        var locations: String = "",
//        var category: String
) {

    var images: String = ""
    var pinned: Boolean = false
    var lastViewed = Date(0)
    var created = Date()

    var imageList: List<String>
        get() {
            return images.split(" ")
        }
        set(value) {
            images = value.joinToString(" ")
        }


    val euroPrice: Float
        get() = fcpPrice / 119.33174f

    constructor() : this("")

    companion object {
        val EqualCallBack: DiffUtil.ItemCallback<PAd> = object : DiffUtil.ItemCallback<PAd>() {
            override fun areItemsTheSame(oldItem: PAd, newItem: PAd): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: PAd, newItem: PAd): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}

@Dao
interface PAdDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg ad: PAd)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg ad: PAd)

    @Delete
    fun deleteAds(vararg ad: PAd)

    @Query("SELECT * FROM PAd")
    fun loadAds(): LiveData<List<PAd>>

    @Query("SELECT * FROM PAd")
    fun loadAds2(): List<PAd>


    @Query("SELECT * FROM PAd WHERE id = :adId")
    fun load(adId: String): LiveData<PAd>

    @Query("DELETE FROM PAd")
    fun deleteAll()

    @Query("SELECT * FROM PAd WHERE lastViewed > 0 ORDER BY lastViewed DESC")
    fun viewedAds(): DataSource.Factory<Int, PAd>

    @Query("SELECT * FROM PAd WHERE pinned ORDER BY date DESC")
    fun allPinnedAdsByDate(): DataSource.Factory<Int, PAd>

    @Query("SELECT * FROM PAd WHERE id IN (:ads)")
    fun loadAds(ads: List<String>): List<PAd>

    @Query("DELETE FROM PAd WHERE NOT(pinned) AND created < :date")
    fun deleteOldAds(date: Date)

    @Query("SELECT MAX(fcpPrice) as max FROM PAd")
    fun getMaxPrice(): LiveData<Int>
}

@Database(entities = [PAd::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pAdDao(): PAdDAO

    companion object {

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val date = Date().time - (15 * 60 * 1000)
                database.execSQL("ALTER TABLE PAd ADD COLUMN created INTEGER NOT NULL DEFAULT $date")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // compute locations
                // how to compute categories

            }
        }

        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME: String = "Matau.db"

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                AppDatabase::class.java, DB_NAME)
                                .addCallback(object : RoomDatabase.Callback() {
                                    override fun onCreate(db: SupportSQLiteDatabase) {}
                                    override fun onOpen(db: SupportSQLiteDatabase) {
                                        PurgeOldAds(INSTANCE!!).execute()
                                    }
                                })
                                .addMigrations(MIGRATION_1_2)
                                .build()
                    }
                }
            }

            return INSTANCE!!
        }
    }
}

class PurgeOldAds(val db: AppDatabase) : AsyncTask<Unit, Unit, Unit>() {
    override fun doInBackground(vararg params: Unit?) {
        val oldMonths = -2
        // remove ads created a logn time ago to clean the database
        val date = Calendar.getInstance()
        date.roll(Calendar.MONTH, oldMonths)
        db.pAdDao().deleteOldAds(date.time)

    }

}
/*
 class PopulateDbAsync(instance: AppDatabase) : AsyncTask<Void, Void, Void>() {
    private val pAdDao: PAdDAO = instance.pAdDao()

    override fun doInBackground(vararg voids: Void): Void? {
//        pAdDao.deleteAll()
//
        val format = SimpleDateFormat("dd/MM/yy")
        val date1 = format.parse("10/10/18")
        val ad1 = PAd("memory", "http://petites-annonces/tahii=1", "Scooter Tweet 125", 110000, "", "", date1)
        val date2 = format.parse("12/10/18")
        val ad2 = PAd("memory", "http://petites-annonces/tahii=2", "Scooter Tweet 125 II", 120000, "", "", date2)
        val date3 = format.parse("11/10/18")
        val ad3 = PAd("memory", "http://petites-annonces/tahii=3", "Scooter Tweet 125 III", 130000, "", "", date3)

        pAdDao.insert(ad1, ad2, ad3)
        return null
    }
}
*/
