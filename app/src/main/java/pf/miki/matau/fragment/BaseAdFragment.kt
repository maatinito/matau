package pf.miki.matau.fragment

import android.content.Context
import android.support.v4.app.Fragment
import pf.miki.matau.helpers.AdInteractionListener

open class BaseAdFragment : Fragment() {
    var adapter = StaggeredAdAdapter()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AdInteractionListener)
            adapter.listener = context
        else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        adapter.listener = null
    }
}