package pf.miki.matau

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
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
import pf.miki.matau.repository.AdRepository
import pf.miki.matau.repository.PAd
import pf.miki.matau.source.Category
import pf.miki.matau.source.SourceType
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener, AdInteractionListener {

    private lateinit var repository : AdRepository
    private val adapter = PagedAdapter(supportFragmentManager)
    private var binding: ActivityMainBinding? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        /*
         * Step 1: Using DataBinding, we setup the layout for the activity
         *
         * */
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(toolBar)

        requiresInternetAccess()

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
            menuInflater.inflate(R.menu.main, menu)
            val search = menu.findItem(R.id.action_search)
            searchView = search.actionView as SearchView
            searchView?.setOnQueryTextListener(this)
            search.setOnActionExpandListener(this)

        }
        return true
    }

    private val menu2category = hashMapOf(
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
            R.id.alimentaire to Category.alimentaire
    )

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
            R.id.BigCE -> repository.sourceType  = SourceType.BIG_CE
            else -> return super.onOptionsItemSelected(item)
        }
        return true // was PA or BigCE
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

    override fun adPinned(ad: PAd, checked: Boolean) {
        repository.pin(ad,checked)
    }

}

class PagedAdapter(sfm: FragmentManager?) : FragmentStatePagerAdapter(sfm) {

    override fun getItem(index: Int): Fragment {
        return when(index) {
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

/*
class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
    private val adapter: AdPagedAdapter = AdPagedAdapter(this, { adClicked(it) })
    private lateinit var adViewModel: AdViewModel
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        /*
         * Step 1: Using DataBinding, we setup the layout for the activity
         *
         * */
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(toolBar)

        requiresInternetAccess()

        //adding a layoutmanager
        adList.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        adList.adapter = adapter

        /*
         * Step 2: Initialize the ViewModel
         *
         * */
        adViewModel = ViewModelProviders.of(this).get(AdViewModel::class.java)

        // restart with previous category

        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val cint = try {
            settings.getInt("category", -1)
        } catch (e: ClassCastException) {
            -1
        }
        if (cint >= 0)
            settings.edit().remove("category").apply() // old way to store the category
        val c: String = settings.getString("category", null) ?: Category.voiture.toString()
        val category: Category = try {
            Category.valueOf(c)
        } catch (e: IllegalArgumentException) {
            Category.voiture
        }
        val title = settings.getString("title", null) ?: category.toString()
        supportActionBar?.title = title

        adViewModel.category = category

        adViewModel.liveAds.observe(this, object : Observer<PagedList<Ad>> {
            override fun onChanged(t: PagedList<Ad>?) {
                adapter.submitList(t)
            }
        })


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


    private fun adClicked(ad: Ad) {
        val detailIntent = Intent(this@MainActivity, AdDetailActivity::class.java)
        detailIntent.putExtra("ad", ad)
        startActivity(detailIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            menuInflater.inflate(R.menu.main, menu)
            val search = menu.findItem(R.id.action_search)
            val searchView = search.actionView as SearchView
            searchView.setOnQueryTextListener(this)
            search.setOnActionExpandListener(this)

        }
        return true
    }

    val menu2category = hashMapOf<Int, Category>(
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
            R.id.alimentaire to Category.alimentaire
    )

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        val category = menu2category[item.itemId]
        if (category != null) {

            if (category != null && category != adViewModel.category) {
                supportActionBar?.title = item.title
                adViewModel.category = category
                adViewModel.filterOn("")
                val settings = PreferenceManager.getDefaultSharedPreferences(this)
                settings.edit()
                        .putString("category", category.toString())
                        .putString("title", item.title.toString()).apply()
                return true
            }
        }
        when (item.itemId) {
            R.id.PA -> adViewModel.sourceType = SourceType.PETITES_ANNONCES
            R.id.BigCE -> adViewModel.sourceType = SourceType.BIG_CE
            else -> return super.onOptionsItemSelected(item)
        }
        return true // was PA or BigCE
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        adViewModel.filterOn(query ?: "")
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }

    override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
        return true
    }

    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
        adViewModel.filterOn("")
        return true
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putString("category", adViewModel.category.toString())
        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        val c = savedInstanceState.getString("category")
        if (c != null)
            adViewModel.category = Category.valueOf(c)
    }

}
 */
