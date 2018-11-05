package pf.miki.matau.helpers

import pf.miki.matau.repository.PAd

/**
 * This interface must be implemented by activities that contain this
 * fragment to allow an interaction in this fragment to be communicated
 * to the activity and potentially other fragments contained in that
 * activity.
 *
 *
 * See the Android Training lesson [Communicating with Other Fragments]
 * (http://developer.android.com/training/basics/fragments/communicating.html)
 * for more information.
 */
interface AdInteractionListener {
    fun adSelected(ad : PAd)
    fun adPinned(ad: PAd, checked: Boolean)
}