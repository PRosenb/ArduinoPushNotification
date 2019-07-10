package ch.pete.arduinopushnotification.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import ch.pete.arduinopushnotification.App
import ch.pete.arduinopushnotification.R
import ch.pete.arduinopushnotification.service.Registration
import ch.pete.arduinopushnotification.service.RegistrationCreate
import ch.pete.arduinopushnotification.service.RegistrationDelete
import ch.pete.arduinopushnotification.view.MainView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import timber.log.Timber

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val installationId: LiveData<String>
        get() = mutableInstallationId
    val serverUrl: LiveData<String>
        get() = mutableServerUrl
    var view: MainView? = null

    private val mutableInstallationId: MutableLiveData<String> = MutableLiveData()
    private val mutableServerUrl: MutableLiveData<String> = MutableLiveData()
    private val prefs = PreferenceManager.getDefaultSharedPreferences(getApplication())
    // is garbage collected when it's a lambda
    @Suppress("ObjectLiteralToLambda")
    private val preferencesChangeListener = object : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            when (key) {
                Registration.PREF_INSTALLATION_ID -> {
                    updateRegistration()
                }
                Registration.PREF_SERVER_URL -> {
                    updateServerUrl()
                }
            }
        }
    }

    fun init() {
        prefs.registerOnSharedPreferenceChangeListener(preferencesChangeListener)
        updateRegistration()
        updateServerUrl()
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

    fun onUrlFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            view?.showUrlEdit()
        } else {
            view?.hideUrlEdit()
        }
    }

    fun onServerUrlSaveClicked(url: String) {
        prefs.edit().putString(Registration.PREF_SERVER_URL, url).apply()
        view?.hideUrlEdit()
    }

    fun onServerUrlCancelClicked() {
        view?.hideUrlEdit()
        updateServerUrl()
    }

    fun onServerUrlResetClicked() {
        prefs.edit().remove(Registration.PREF_SERVER_URL).apply()
    }

    override fun onCleared() {
        prefs.unregisterOnSharedPreferenceChangeListener(preferencesChangeListener)
        super.onCleared()
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
                                } else if (workInfo.state == WorkInfo.State.FAILED) {
                                    showErrorToast(workInfo)
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
                } else if (workInfo.state == WorkInfo.State.FAILED) {
                    showErrorToast(workInfo)
                }
                view?.enableActionButton()
            }
        }
    }

    private fun showErrorToast(workInfo: WorkInfo) {
        val errorMessageResId = workInfo.outputData.getInt(Registration.ERROR_MESSAGE_RES_ID, -1)
        if (errorMessageResId == -1) {
            Toast.makeText(getApplication(), R.string.error_occurred, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(getApplication(), errorMessageResId, Toast.LENGTH_LONG).show()
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

    private fun updateServerUrl() {
        val serverUrlFromPrefs = prefs.getString(Registration.PREF_SERVER_URL, null)
        if (serverUrlFromPrefs != null) {
            view?.updateServerUrl(serverUrlFromPrefs)
            view?.enableUrlReset()
        } else {
            view?.updateServerUrl(getApplication<App>().getString(R.string.default_server_url))
            view?.disableUrlReset()
        }
    }
}
