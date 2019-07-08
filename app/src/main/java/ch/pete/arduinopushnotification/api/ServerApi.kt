package ch.pete.arduinopushnotification.api

import ch.pete.arduinopushnotification.api.data.RegistrationRequest
import ch.pete.arduinopushnotification.api.data.RegistrationResult
import retrofit2.Call
import retrofit2.http.*

/**
 * Defines the interface between the app and the RESTful server.
 */
interface ServerApi {
    @POST("registration")
    fun createRegistration(@Body registrationRequest: RegistrationRequest): Call<RegistrationResult>

    @PUT("registration/{installationId}")
    fun updateRegistration(@Path("installationId") installationId: String, @Body registrationRequest: RegistrationRequest): Call<RegistrationResult>

    @DELETE("registration/{installationId}")
    fun deleteRegistration(@Path("installationId") installationId: String): Call<RegistrationResult>
}
