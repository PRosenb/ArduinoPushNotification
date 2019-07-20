package ch.pete.arduinopushnotification.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import ch.pete.arduinopushnotification.R
import ch.pete.arduinopushnotification.viewmodel.MainViewModel
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

        action.setOnClickListener {
            viewModel.onActionButtonClicked()
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
}
