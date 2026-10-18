package com.ottrojja.services.azkar

/** Custom session commands and bundle keys shared by AzkarPlaybackManager and AzkarPlaybackService. */
object AzkarPlaybackProtocol {
    const val SESSION_ID = "azkar"
    const val CMD_PLAY = "com.ottrojja.azkar.PLAY"

    /** Same action string the old services used; stops the player when sent as a plain service intent. */
    const val ACTION_TERMINATE = "TERMINATE"

    const val KEY_ADDRESS = "address"
    const val KEY_TITLE = "title"
}
