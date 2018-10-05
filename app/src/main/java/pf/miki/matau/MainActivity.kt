package pf.miki.matau

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import pf.miki.matau.ViewModel.AdPagedAdapter
import pf.miki.matau.ViewModel.AdViewModel
import pf.miki.matau.databinding.ActivityMainBinding
import pub.devrel.easypermissions.EasyPermissions
import android.preference.PreferenceManager
import android.content.SharedPreferences




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

        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val c = settings.getInt("category",9)
        adViewModel.category = c

        adViewModel.liveAds.observe(this, object : Observer<PagedList<Ad>> {
            override fun onChanged(t: PagedList<Ad>?) {
                adapter.submitList(t)
            }
        })


    }


/*
        //adding some dummy data to the list
        adapter.add(Ad("1225099",
                "TOYOTA HILUX 3L DIESEL - 4X4",
                "TOYOTA HILUX 3L DIESEL - 4X4 - BOITE MANUELLE - 172.000P - DÉCEMBRE 2005 - 205.000KM - 1ERE MAIN - RÉVISION FAITE - DISTRIBUTION CHANGÉE - AUCUN TRAVAUX A PRÉVOIR",
                "Tahiti", 16_676.20f, 1_990_000, "87 773 761"))
        adapter.add(Ad("1225116", "18/221A - FAAA APPARTEMENT DUPLEX F5 MEUBLÉ (VTP)",
                "Idéalement situé dans petit ensemble immobilier neuf, à proximité immédiate de tous les commerces, grands axes et écoles, très bel appartement neuf de type F5 de 109 M² à louer.\n" +
                        "\n" +
                        "Meublé et équipé, le logement se compose:\n" +
                        "\n" +
                        "- Au RDC : d'un salon avec cuisine, d'une buanderie et d'un WC séparé, avec une belle terrasse et un jardinet clos, d'une chambre avec salle d'eau attenante,\n" +
                        "\n" +
                        "- A l'étage : de trois chambres (une suite parentale climatisée), et de deux salles de bains.\n" +
                        "\n" +
                        "Disponible début novembre (travaux en cours), la location est soumise à conditions de ressources.\n" +
                        "\n" +
                        "*Visuels non-contractuels.",
                "Tahiti",
                1676f, 200000,
                "THOMAS : 87 27 31 21\n" +
                        "Whatsapp et Viber :+689 87 27 31 21"
        ))
        adapter.add(Ad("1224904",
                "cafetière senséo",
                "vds cafetière senséo tb état",
                "Pirae / Tahiti",
                41.90f, 5000,
                "89201761"))


        //now adding the adapter to recyclerview
        adList.adapter = adapter

        val url = "http://petitesannonces.pf/annonces.php?c=9"


    }
*/

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

    val menu2category = hashMapOf<Int, Int>(R.id.location_appartement to 4,
            R.id.vente_appartement to 1,
            R.id.vente_maison to 2,
            R.id.vente_terrain to 3,
            R.id.location_appartement to 4,
            R.id.location_maison to 5,
            R.id.location_vacances to 6,
            R.id.immobilier_autre to 7,
            R.id.voiture to 9,
            R.id.motos to 10,
            R.id.bateau to 11,
            R.id.pieces to 13,
            R.id.voiture_autre to 14,
            R.id.meubles to 15,
            R.id.bricolage to 16,
            R.id.informatique to 17,
            R.id.jeux to 18,
            R.id.multimedia to 19,
            R.id.telephone to 20,
            R.id.sport to 21,
            R.id.vetements to 23,
            R.id.puericulture to 24,
            R.id.bijoux to 25,
            R.id.collection to 26,
            R.id.alimentaire to 27
    )

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        val category = menu2category[item.itemId]
        if (category != null) {
            adViewModel.category = category
            val settings = PreferenceManager.getDefaultSharedPreferences(this)
            settings.edit().putInt("category",category).apply()
            return true
        }
        return super.onOptionsItemSelected(item)
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
        savedInstanceState.putInt("category", adViewModel.category)
        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        val c = savedInstanceState.getInt("category", -1)
        if (c >= 0)
            adViewModel.category = c
    }

}

