package pf.miki.matau

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.*
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import pf.miki.matau.databinding.ActivityMainBinding
import pf.miki.matau.fragment.ads2.Ads2Fragment
import pf.miki.matau.fragment.history.HistoryFragment
import pf.miki.matau.fragment.pin.PinnedAdFragment
import pf.miki.matau.helpers.AdInteractionListener
import pf.miki.matau.helpers.PLocation
import pf.miki.matau.repository.AdRepository
import pf.miki.matau.repository.PAd
import pf.miki.matau.source.Category
import pf.miki.matau.source.SourceType
import pub.devrel.easypermissions.EasyPermissions
import java.util.*


class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener, AdInteractionListener {

    private lateinit var repository: AdRepository
    private val adapter = PagedAdapter(supportFragmentManager)
    private var binding: ActivityMainBinding? = null

    var pageHistory = LinkedList<Int>()

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        PLocation.init(this)

        /*
         * Step 1: Using DataBinding, we setup the layout for the activity
         *
         * */
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(toolBar)

        requiresInternetAccess()
        createNotificationChannel()

        repository = AdRepository.getRepository(this)

        // setup pages
        pager.adapter = adapter // set the adapter used to swithc between pages
        tabs.setupWithViewPager(pager)

        // restart with previous category

        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val iCategory = try {
            settings.getInt("category", -1)
        } catch (e: ClassCastException) {
            -1
        }
        if (iCategory >= 0)
            settings.edit().remove("category").apply() // old way to store the category
        val c: String = settings.getString("category", null) ?: Category.voiture.toString()
        val category: Category = try {
            Category.valueOf(c)
        } catch (e: IllegalArgumentException) {
            Category.voiture
        }
        val title = settings.getString("title", null) ?: category.toString()
        supportActionBar?.title = title
        repository.category = category
        repository.maintenance()

        // managing back stack with sub fragement in pager
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(page: Int) {
                if (pageHistory.isEmpty() || pageHistory.peek() != page)
                    pageHistory.push(page)
//                Log.i("Matau.tabs","current page = $page ${pageHistory.joinToString()}")
            }

            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
            override fun onPageScrollStateChanged(arg0: Int) {}
        })
        pageHistory.push(0) // current page
    }


    private fun requiresInternetAccess() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.INTERNET)) {
            return
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.internet_access), 34, Manifest.permission.INTERNET)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private var searchView: SearchView? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            menuInflater.inflate(R.menu.main, it)
            string2menu.putAll(menu2category.entries.associate { e -> it.findItem(e.key).title.toString() to e.key })
            val search = it.findItem(R.id.action_search)
            searchView = search.actionView as SearchView
            searchView?.setOnQueryTextListener(this)
            search.setOnActionExpandListener(this)
        }
        return true
    }

    companion object {

        public val menu2category = hashMapOf(
                R.id.vente_appartement to Category.Vente_appartement,
                R.id.vente_maison to Category.vente_maison,
                R.id.vente_terrain to Category.vente_terrain,
                R.id.location_appartement to Category.location_appartement,
                R.id.location_maison to Category.location_maison,
                R.id.location_vacances to Category.location_vacances,
                R.id.immobilier_autre to Category.immobilier_autre,
                R.id.voiture to Category.voiture,
                R.id.motos to Category.motos,
                R.id.bateau to Category.bateau,
                R.id.pieces to Category.pieces,
                R.id.voiture_autre to Category.voiture_autre,
                R.id.meubles to Category.meubles,
                R.id.bricolage to Category.bricolage,
                R.id.informatique to Category.informatique,
                R.id.jeux to Category.jeux,
                R.id.multimedia to Category.multimedia,
                R.id.telephone to Category.telephone,
                R.id.sport to Category.sport,
                R.id.vetements to Category.vetements,
                R.id.puericulture to Category.puericulture,
                R.id.bijoux to Category.bijoux,
                R.id.collection to Category.collection,
                R.id.alimentaire to Category.alimentaire,
                R.id.bonnes_affaires to Category.bonnes_affaires

        )
        public var string2menu = hashMapOf<String, Int>()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        val category = menu2category[item.itemId]
        if (category != null) {

            if (category != repository.category) {
                supportActionBar?.title = item.title
                repository.category = category
                val settings = PreferenceManager.getDefaultSharedPreferences(this)
                settings.edit()
                        .putString("category", category.toString())
                        .putString("title", item.title.toString()).apply()
                return true
            }
        }
        when (item.itemId) {
            R.id.PA -> repository.sourceType = SourceType.PETITES_ANNONCES
            R.id.BigCE -> repository.sourceType = SourceType.BIG_CE
            R.id.action_complex_search -> launch_complex_search()
            else -> return super.onOptionsItemSelected(item)
        }
        return true // be here means it was PA or BigCE
    }

    private fun launch_complex_search() {
        val detailIntent = Intent(this, SearchParameterActivity::class.java)
        startActivity(detailIntent)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        repository.search = query ?: ""
        searchView?.clearFocus() // remove keyboard
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }

    override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
        return true
    }

    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
        repository.search = ""
        searchView?.isIconified = true
        return true
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putString("category", repository.category.toString())
        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        val c = savedInstanceState.getString("category")
        if (c != null)
            repository.category = Category.valueOf(c)
    }

    // methods called when an ad is clicked or pinned
    override fun adSelected(ad: PAd) {
        // put ad in the history
        repository.adViewed(ad)
        val detailIntent = Intent(this, AdDetailActivity::class.java)
        detailIntent.putExtra("ad", ad.id)
        startActivity(detailIntent)
    }

    private val CHANNEL_ID = "SearchBot"

    override fun adPinned(ad: PAd, checked: Boolean) {
        repository.pin(ad, checked)
        sendNotification(ad)
    }

    private fun sendNotification(ad: PAd) {
        val detailIntent = Intent(this, AdDetailActivity::class.java)
        detailIntent.putExtra("ad", ad.id)
        val pIntent = PendingIntent.getActivity(this, ad.id.hashCode(), detailIntent, 0)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.hibiscus_selected_2)
                .setContentTitle(ad.title)
                .setContentText(ad.description)
                .setStyle(NotificationCompat.BigTextStyle().bigText(ad.description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build()
        NotificationManagerCompat.from(this).notify(ad.id.hashCode(), notification)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = getString(R.string.channel_description)
            // Register the channel with the system
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }


    override fun onBackPressed() {
        if (pageHistory.size < 2)
            super.onBackPressed()
        else {
            pageHistory.pop()
            pager.currentItem = pageHistory.peek()
        }
    }
}

class PagedAdapter(sfm: FragmentManager?) : FragmentStatePagerAdapter(sfm) {

    override fun getItem(index: Int): Fragment {
        return when (index) {
            0 -> Ads2Fragment.newInstance()
            1 -> PinnedAdFragment.newInstance()
            else -> HistoryFragment.newInstance()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Annonces"
            1 -> "Favorites"
            2 -> "Historique"
            else -> "Inconnu"
        }
    }


    override fun getCount(): Int = 3
}


