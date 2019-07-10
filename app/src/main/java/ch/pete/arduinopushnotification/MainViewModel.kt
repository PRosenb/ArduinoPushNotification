package ch.pete.arduinopushnotification

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import timber.log.Timber

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val installationId: LiveData<String>
        get() = mutableInstallationId
    var view: MainView? = null

    private val mutableInstallationId: MutableLiveData<String> = MutableLiveData()
    private val prefs = PreferenceManager.getDefaultSharedPreferences(getApplication())
    // is garbage collected when it's a lambda
    @Suppress("ObjectLiteralToLambda")
    private val preferencesChangeListener = object : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            when (key) {
                Registration.PREF_INSTALLATION_ID -> {
                    updateRegistration()
                }
            }
        }
    }

    fun init() {
        prefs.registerOnSharedPreferenceChangeListener(preferencesChangeListener)
        updateRegistration()
    }

    fun onActionButtonClicked() {
        val installationId = prefs.getString(Registration.PREF_INSTALLATION_ID, null)
        if (installationId == null) {
            // not registered, register device
            registerDevice()
        } else {
            // registered, so we unregiser the device
            unregisterDevice()
        }
    }

    private fun registerDevice() {
        view?.disableActionButton()
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Timber.w("getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }

                    val token = task.result?.token
                    if (token != null) {
                        val liveDataWorkInfo = RegistrationCreate.enqueue(token, getApplication())
                        liveDataWorkInfo.observeForever { workInfo ->
                            if (workInfo.state.isFinished) {
                                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                                    view?.updateActionButton(getApplication<App>().getString(R.string.unregister))
                                }
                                view?.enableActionButton()
                            }
                        }
                    } else {
                        view?.enableActionButton()
                    }
                })
    }

    private fun unregisterDevice() {
        view?.disableActionButton()
        val liveDataWorkInfo = RegistrationDelete.enqueue(getApplication())
        liveDataWorkInfo.observeForever { workInfo ->
            if (workInfo.state.isFinished) {
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    view?.updateActionButton(getApplication<App>().getString(R.string.register))
                }
                view?.enableActionButton()
            }
        }
    }

    private fun updateRegistration() {
        val installationId = prefs.getString(Registration.PREF_INSTALLATION_ID, null)
        if (installationId == null) {
            mutableInstallationId.value = getApplication<App>().getString(R.string.not_registered)
            view?.updateActionButton(getApplication<App>().getString(R.string.register))
        } else {
            mutableInstallationId.value = installationId
            view?.updateActionButton(getApplication<App>().getString(R.string.unregister))
        }
    }

    override fun onCleared() {
        prefs.unregisterOnSharedPreferenceChangeListener(preferencesChangeListener)
        super.onCleared()
    }
}
