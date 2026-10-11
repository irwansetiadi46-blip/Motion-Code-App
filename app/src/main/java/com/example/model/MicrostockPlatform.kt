package com.example.model

enum class MicrostockPlatform(
    val id: String,
    val displayName: String,
    val columnSummary: String,
    val filePrefix: String
) {
    ADOBE_STOCK(
        id = "adobestock",
        displayName = "Adobe Stock",
        columnSummary = "Filename, Title, Keywords, Category",
        filePrefix = "adobestock"
    ),
    SHUTTERSTOCK(
        id = "shutterstock",
        displayName = "Shutterstock",
        columnSummary = "Filename, Description, Keywords, Categories",
        filePrefix = "shutterstock"
    ),
    VECTEEZY(
        id = "vecteezy",
        displayName = "Vecteezy",
        columnSummary = "Filename, Title, Description, Keywords, License",
        filePrefix = "vecteezy"
    )
}
