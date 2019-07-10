package ch.pete.arduinopushnotification.api.data

data class RegistrationResult(
        val installationId: String?,
        val error: String? // null unless installationId is null
)
