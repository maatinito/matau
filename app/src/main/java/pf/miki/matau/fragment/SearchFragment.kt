package pf.miki.matau.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener
import kotlinx.android.synthetic.main.activity_search_parameter.*
import kotlinx.android.synthetic.main.search_fragment.*
import pf.miki.matau.MainActivity

import pf.miki.matau.R
import pf.miki.matau.helpers.PLocation
import java.lang.Math.ceil
import kotlin.math.ceil

class SearchFragment : Fragment(), OnRangeSeekbarChangeListener {

    companion object {
        fun newInstance() = SearchFragment()
    }

    private lateinit var viewModel: SearchViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.search_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        // Set list of categories
        val entries = MainActivity.string2menu.keys.toList()
        val adapter = ArrayAdapter<String>(this.categorySpinner.context, android.R.layout.simple_spinner_dropdown_item, entries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // set list of locations
        val locationAdapter = ArrayAdapter<String>(this.categorySpinner.context, android.R.layout.simple_spinner_dropdown_item, PLocation.getLocations())
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        locationSpinner.adapter = locationAdapter

        // initialize range seek bar
        priceRangeSeekbar.setOnRangeSeekbarChangeListener(this)
        viewModel.computeMaxPrice().observe(this, Observer<Int> { v ->
            v?.let {
                priceRangeSeekbar.setMinValue(0F).setMaxValue(it.toFloat()).setSteps(ceil(it.toFloat() / 200F));
                Log.i("SearchFragment", "max=$it")
                valueChanged(0, it)
            }
        })
    }

    override fun valueChanged(minValue: Number?, maxValue: Number?) {
        lowPriceTextView.text = minValue.toString()
        highPriceTextView2.text = maxValue.toString()
    }
}
