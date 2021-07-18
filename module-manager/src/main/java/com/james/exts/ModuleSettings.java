package com.james.exts;

import com.james.DynamicPublishMethods;

import org.gradle.internal.metaobject.MethodAccess;
import org.gradle.internal.metaobject.MethodMixIn;

import java.util.HashMap;
import java.util.Map;

/**
 * @author james
 * @date 2021/7/9
 */
public class ModuleSettings implements MethodMixIn {
    private final Map<String, ModuleConfig> moduleConfigHashMap = new HashMap<>();

    public String mavenUrl;

    @Override
    public MethodAccess getAdditionalMethods() {
        return new DynamicPublishMethods(moduleConfigHashMap);
    }

    public Map<String, ModuleConfig> getModuleConfigHashMap() {
        return moduleConfigHashMap;
    }

    /**
     * 获取module的仓库标识
     *
     * @param key 配置文件中定义的module名
     */
    public String module(String key) throws Exception {
        ModuleConfig moduleConfig = moduleConfigHashMap.get(key);
        if (null == moduleConfig) {
            throw new Exception("can't find module " + key
                    + ",make sure you have publish it to mavenLocal or specify repo");
        }

        return moduleConfig.getPath();
    }
}
