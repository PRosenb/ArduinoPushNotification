package ch.pete.arduinopushnotification

interface MainView {
    fun updateActionButton(actionText: String)
    fun disableActionButton()
    fun enableActionButton()
}