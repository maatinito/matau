package pf.miki.matau.fragment

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import pf.miki.matau.repository.AdRepository
import pf.miki.matau.repository.AppDatabase
import pf.miki.matau.repository.PAd
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

open class BaseAdViewModel(app: Application) : AndroidViewModel(app) {
    val repository = AdRepository.getRepository(getApplication())
//    protected val executor: Executor = Executors.newFixedThreadPool(1)
//    protected val dao = AppDatabase.getDatabase(getApplication()).pAdDao()
//    private val db = AppDatabase.getDatabase(getApplication())

    fun pin(pAd: PAd, pinned: Boolean) {
        repository.pin(pAd,pinned)
    }

    fun loadAd(id : String) : LiveData<PAd> {
        return repository.loadAd(id)
    }

}