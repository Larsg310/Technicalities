package com.technicalitiesmc.base.manual.api;


import com.technicalitiesmc.base.Technicalities;

/**
 * Glue / actual references for the RTFM API.
 */
public final class API {
    /**
     * The ID of the mod, i.e. the internal string it is identified by.
     */
    public static final String MOD_ID = Technicalities.MODID;

    /**
     * The current version of the mod.
     */
    public static final String MOD_VERSION = Technicalities.VERSION;

    // --------------------------------------------------------------------- //

    // Set in RTFM pre-init, prefer using static entry point classes instead.
    public static com.technicalitiesmc.base.manual.api.detail.ManualAPI manualAPI;

    private API() {
    }
}
