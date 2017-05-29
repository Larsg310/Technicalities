package com.technicalitiesmc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.base.Throwables;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.technicalitiesmc.lib.module.ModuleManager;
import com.technicalitiesmc.lib.util.JSONUtils;

public class TKModuleManager extends ModuleManager<ITKModule> {

    private static final File configFile = new File("./config/" + Technicalities.MODID + "/modules.json");
    private static final Supplier<JsonObject> config = Suppliers.memoize(() -> {
        if (configFile.exists()) {
            try {
                return JSONUtils.read(new FileInputStream(configFile), JsonObject.class);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return new JsonObject();
    });

    TKModuleManager() {
        super(ITKModule.class, TKModule.class, TKModuleManager::test, TKModuleManager::compare, TKModule::value, TKModule::dependencies);
    }

    public void save() {
        try {
            JsonObject cfg = config.get();
            if (configFile.exists()) {
                configFile.delete();
            }
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
            JSONUtils.write(new FileOutputStream(configFile), cfg);
        } catch (Exception ex) {
            throw Throwables.propagate(ex);
        }
    }

    private static boolean test(Class<? extends ITKModule> type, TKModule annotation) {
        JsonObject cfg = config.get();
        if (cfg.has(annotation.value())) {
            return cfg.get(annotation.value()).getAsBoolean();
        }
        boolean enabled = annotation.enabledByDefault();
        cfg.add(annotation.value(), new JsonPrimitive(enabled));
        return enabled;
    }

    private static int compare(Pair<Class<? extends ITKModule>, TKModule> modA, Pair<Class<? extends ITKModule>, TKModule> modB) {
        TKModule a = modA.getValue(), b = modB.getValue();
        if (ArrayUtils.contains(a.dependencies(), a.value()) || ArrayUtils.contains(a.after(), a.value())) {
            throw new IllegalStateException("Module " + a.value() + " cannot depend on itself.");
        }
        if (ArrayUtils.contains(b.dependencies(), b.value()) || ArrayUtils.contains(b.after(), b.value())) {
            throw new IllegalStateException("Module " + b.value() + " cannot depend on itself.");
        }
        boolean aBefore = ArrayUtils.contains(b.dependencies(), a.value()) || ArrayUtils.contains(b.after(), a.value());
        boolean bBefore = ArrayUtils.contains(a.dependencies(), b.value()) || ArrayUtils.contains(a.after(), b.value());
        if (aBefore && bBefore) {
            throw new IllegalStateException("Circular dependency found between modules " + a.value() + " and " + b.value() + ".");
        }
        return aBefore ? -1 : bBefore ? 1 : 0;
    }

    static String getName(ITKModule module) {
        return module.getClass().getAnnotation(TKModule.class).value();
    }

}
