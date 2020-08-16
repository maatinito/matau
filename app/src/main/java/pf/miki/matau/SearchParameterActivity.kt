package pf.miki.matau

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener
import kotlinx.android.synthetic.main.activity_search_parameter.*
import pf.miki.matau.helpers.PLocation


class SearchParameterActivity : AppCompatActivity(), OnRangeSeekbarChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_parameter)

        priceRangeSeekbarA.setOnRangeSeekbarChangeListener(this)

        // set available categories
        R.menu.main;
        val entries = MainActivity.string2menu.keys.toList()
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, entries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinnerA.adapter = adapter

        val locationAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, PLocation.getLocations())
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        locationSpinnerA.adapter = locationAdapter

        okButtonA.setOnClickListener({ v -> finish() })
    }

    override fun valueChanged(minValue: Number?, maxValue: Number?) {
        lowPriceTextViewA.text = minValue.toString()
        highPriceTextViewA.text = maxValue.toString()
    }
}
