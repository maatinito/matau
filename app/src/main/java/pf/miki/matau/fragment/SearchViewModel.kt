package pf.miki.matau.fragment

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import pf.miki.matau.repository.AdRepository

class SearchViewModel(app: Application) : AndroidViewModel(app) {
    val repository = AdRepository.getRepository(getApplication())
    val parameters = repository.liveParameters

    fun computeMaxPrice(): LiveData<Int> = repository.computeMaxPrice()

}
