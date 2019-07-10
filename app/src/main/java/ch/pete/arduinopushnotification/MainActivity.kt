package ch.pete.arduinopushnotification

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MainView {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.view = this
        viewModel.init()

        viewModel.installationId.observe(this, Observer { installationId.text = it })
        viewModel.serverUrl.observe(this, Observer { serverUrl.setText(it) })

        action.setOnClickListener {
            viewModel.onActionButtonClicked()
        }

        serverUrl.setOnFocusChangeListener { _: View, hasFocus: Boolean ->
            viewModel.onUrlFocusChanged(hasFocus)
        }
        serverUrlSave.setOnClickListener {
            viewModel.onServerUrlSaveClicked(serverUrl.text.toString())
        }
        serverUrlCancel.setOnClickListener {
            viewModel.onServerUrlCancelClicked()
        }
        serverUrlReset.setOnClickListener {
            viewModel.onServerUrlResetClicked()
        }
    }

    override fun updateActionButton(actionText: String) {
        action.text = actionText
    }

    override fun enableActionButton() {
        action.isEnabled = true
    }

    override fun disableActionButton() {
        action.isEnabled = false
    }

    override fun updateServerUrl(url: String) {
        serverUrl.setText(url)
    }

    override fun showUrlEdit() {
        serverUrlSave.visibility = View.VISIBLE
        serverUrlCancel.visibility = View.VISIBLE
    }

    override fun hideUrlEdit() {
        serverUrlSave.visibility = View.INVISIBLE
        serverUrlCancel.visibility = View.INVISIBLE
        serverUrl.clearFocus()
    }

    override fun enableUrlReset() {
        serverUrlReset.isEnabled = true
    }

    override fun disableUrlReset() {
        serverUrlReset.isEnabled = false
    }
}
