package com.sportlinktv.player

import com.sportlinktv.data.local.entity.Channel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the pending Channel to be played.
 * Used to pass full Channel data (including DRM keys) between
 * HomeScreen and PlayerScreen without URL-encoding every field.
 */
@Singleton
class PendingChannelHolder @Inject constructor() {
    var pendingChannel: Channel? = null
}
