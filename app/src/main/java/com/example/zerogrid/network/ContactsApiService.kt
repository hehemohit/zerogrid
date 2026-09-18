package com.example.zerogrid.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

// ── Contact DTOs ───────────────────────────────────────────────────────────

data class ContactUserDto(
    @SerializedName("id")          val id: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("email")       val email: String,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("role")        val role: String? = null,
    @SerializedName("photoUrl")    val photoUrl: String? = null
)

data class ContactDto(
    @SerializedName("id")          val id: String,
    @SerializedName("label")       val label: String = "Emergency Contact",
    @SerializedName("createdAt")   val createdAt: String? = null,
    @SerializedName("contactUser") val contactUser: ContactUserDto
)

data class AddContactRequest(
    @SerializedName("contactEmailOrPhone") val contactEmailOrPhone: String,
    @SerializedName("label")               val label: String = "Emergency Contact"
)

data class ContactsListResponse(
    @SerializedName("contacts") val contacts: List<ContactDto>
)

data class AddContactResponse(
    @SerializedName("message") val message: String,
    @SerializedName("contact") val contact: ContactDto
)

data class DeleteContactResponse(
    @SerializedName("message") val message: String,
    @SerializedName("id")      val id: String
)

// ── Retrofit Service ───────────────────────────────────────────────────────

interface ContactsApiService {

    @GET(ApiConstants.CONTACTS)
    suspend fun getContacts(
        @Header("Authorization") token: String
    ): Response<ContactsListResponse>

    @POST(ApiConstants.CONTACTS)
    suspend fun addContact(
        @Header("Authorization") token: String,
        @Body body: AddContactRequest
    ): Response<AddContactResponse>

    @DELETE("${ApiConstants.CONTACTS}/{id}")
    suspend fun deleteContact(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<DeleteContactResponse>
}
