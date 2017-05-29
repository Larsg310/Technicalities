package com.technicalitiesmc;

import com.technicalitiesmc.lib.module.IModule;

/**
 * Base interface for all Technicalities modules. Must be annotated with {@link TKModule} to be detected.
 */
public interface ITKModule extends IModule {

    /**
     * Called on mod pre-initialization.
     */
    public void preInit();

    /**
     * Called on mod initialization.
     */
    public void init();

    /**
     * Called on mod post-initialization.
     */
    public void postInit();

}
