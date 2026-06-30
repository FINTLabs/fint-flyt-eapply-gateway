package no.novari.flyt.example.gateway.instance.model

data class Filinnhold(
    val filnavn: String,
    val mimeType: String,
    @Suppress("ArrayInDataClass")
    val base64: ByteArray,
)
