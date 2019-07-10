package ch.pete.arduinopushnotification

interface MainView {
    fun updateActionButton(actionText: String)
    fun disableActionButton()
    fun enableActionButton()

    fun updateServerUrl(url: String)
    fun showUrlEdit()
    fun hideUrlEdit()
    fun enableUrlReset()
    fun disableUrlReset()
}
