package com.iiitlucknow.android.festify.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.iiitlucknow.android.festify.Repositories.repo
import com.iiitlucknow.android.festify.data.database
import com.iiitlucknow.android.festify.data.my_events
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@ViewModelScoped
class HomeViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val myrepo: repo
    val allwords: LiveData<MutableList<my_events>>

    init {
        val dao = database.getDatabase(application).myDao()
        myrepo = repo(dao)
        allwords = myrepo.getwords
    }

    fun addevent(myEvents: my_events) {
        viewModelScope.launch(Dispatchers.IO) {
            myrepo.insertitem(myEvents)
        }
    }

    fun deleteevent(myEvents: my_events) {
        viewModelScope.launch(Dispatchers.IO) {
            myrepo.deleteword(myEvents)
        }
    }
}
