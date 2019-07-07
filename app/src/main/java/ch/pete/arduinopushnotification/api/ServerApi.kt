package ch.pete.arduinopushnotification.api

import ch.pete.arduinopushnotification.api.data.NewRegistrationRequest
import ch.pete.arduinopushnotification.api.data.RegistrationResult
import ch.pete.arduinopushnotification.api.data.UpdateRegistrationRequest
import retrofit2.Call
import retrofit2.http.*

/**
 * Defines the interface between the app and the RESTful server.
 */
interface ServerApi {
    @POST("registration")
    fun createRegistration(@Body newRegistrationRequest: NewRegistrationRequest): Call<RegistrationResult>

    @PUT("registration")
    fun updateRegistration(@Body updateRegistrationRequest: UpdateRegistrationRequest): Call<RegistrationResult>

    @DELETE("registration/{installationId}")
    fun deleteRegistration(@Path("installationId") installationId: String): Call<RegistrationResult>
}
