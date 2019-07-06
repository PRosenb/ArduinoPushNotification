package ch.pete.arduinopushnotification

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableInstallationId: MutableLiveData<String> = MutableLiveData()
    val installationId: LiveData<String> = mutableInstallationId

    private val prefs = PreferenceManager.getDefaultSharedPreferences(getApplication())
    // is garbage collected when it's a lambda
    @Suppress("ObjectLiteralToLambda")
    private val preferencesChangeListener = object : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            when (key) {
                MessagingService.PREF_INSTALLATION_ID -> {
                    mutableInstallationId.value =
                        prefs.getString(MessagingService.PREF_INSTALLATION_ID, null)
                }
            }
        }
    }

    fun init() {
        prefs.registerOnSharedPreferenceChangeListener(preferencesChangeListener)
        mutableInstallationId.value = prefs.getString(MessagingService.PREF_INSTALLATION_ID, null)
    }

    override fun onCleared() {
        prefs.unregisterOnSharedPreferenceChangeListener(preferencesChangeListener)
        super.onCleared()
    }
}
