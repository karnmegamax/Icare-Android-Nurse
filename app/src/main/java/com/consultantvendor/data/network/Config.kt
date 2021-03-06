package com.consultantvendor.data.network

object Config {

    var BASE_URL = ""
    var BASE_URL_DEV = "http://icare.megamaxservices.com"
    var BASE_URL_TEST = "http://icare.megamaxservices.com"
    var BASE_URL_LIVE = "http://icare.megamaxservices.com"

    var IMAGE_URL = "https://icare.megamaxservices.com/storage/icare_bucket/"

    private val appMode = AppMode.DEV

    val baseURL: String
        get() {
            init(appMode)
            return BASE_URL
        }


    val imageURL: String
        get() {
            init(appMode)
            return IMAGE_URL
        }

    private fun init(appMode: AppMode) {

        BASE_URL = when (appMode) {
            AppMode.DEV -> {
                BASE_URL_DEV
            }
            AppMode.TEST -> {
                BASE_URL_TEST
            }
            AppMode.LIVE -> {
                BASE_URL_LIVE
            }
        }
    }

    private enum class AppMode {
        DEV, TEST, LIVE
    }
}