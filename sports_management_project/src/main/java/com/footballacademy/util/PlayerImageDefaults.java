package com.footballacademy.util;

public final
class PlayerImageDefaults {
    private PlayerImageDefaults() {
    }
    /** Relative path served under /uploads/** */
    public static final String DEFAULT_PLAYER_IMAGE = "/uploads/defaults/player.jpg";
    public static String resolveRelative(String playerImageUrl) {
        return(playerImageUrl == null || playerImageUrl.isBlank()) ? DEFAULT_PLAYER_IMAGE : playerImageUrl;
    }
}
