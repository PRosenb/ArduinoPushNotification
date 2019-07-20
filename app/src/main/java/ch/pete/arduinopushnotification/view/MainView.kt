package ch.pete.arduinopushnotification.view

interface MainView {
    fun updateActionButton(actionText: String)
    fun disableActionButton()
    fun enableActionButton()
}
