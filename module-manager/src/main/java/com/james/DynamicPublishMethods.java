package com.james;


import com.james.exts.ModuleConfig;

import org.gradle.internal.impldep.org.jetbrains.annotations.NotNull;
import org.gradle.internal.metaobject.DynamicInvokeResult;
import org.gradle.internal.metaobject.MethodAccess;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

/**
 * @author james
 * @date 2021/7/15
 */
public class DynamicPublishMethods implements MethodAccess {
    private Map<String, ModuleConfig> moduleConfigHashMap;


    public DynamicPublishMethods(Map<String, ModuleConfig> moduleConfigHashMap) {
        this.moduleConfigHashMap = moduleConfigHashMap;
    }

    @Override
    public boolean hasMethod(String name, Object... arguments) {
        return true;
    }

    @Override
    public DynamicInvokeResult tryInvokeMethod(String name, Object... arguments) {
        for (Object object : arguments) {
            if (object instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) object;
                ModuleConfig moduleConfig = new ModuleConfig();
                attemptPackageModuleConfig(moduleConfig, name, map);
            }
        }
        return DynamicInvokeResult.found(moduleConfigHashMap);
    }

    private void attemptPackageModuleConfig(@NotNull ModuleConfig moduleConfig,
            @NotNull String name, @NotNull Map<String, Object> paramsMap) {
        if (paramsMap.isEmpty()) {
            return;
        }
        try {
            Class<? extends ModuleConfig> moduleConfigClass = moduleConfig.getClass();
            Set<Map.Entry<String, Object>> entries = paramsMap.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                Field field = moduleConfigClass.getField(entry.getKey());
                field.setAccessible(true);
                field.set(moduleConfig, entry.getValue());
                field.setAccessible(false);
            }
            moduleConfigHashMap.put(name, moduleConfig);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
