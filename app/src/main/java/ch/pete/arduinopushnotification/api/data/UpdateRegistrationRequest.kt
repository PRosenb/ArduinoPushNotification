package ch.pete.arduinopushnotification.api.data

data class UpdateRegistrationRequest(val installationId: String, val deviceToken: String)
