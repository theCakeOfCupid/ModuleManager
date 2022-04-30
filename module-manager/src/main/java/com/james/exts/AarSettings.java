package com.james.exts;

import com.james.util.StringUtil;

import org.codehaus.groovy.runtime.GStringImpl;

/**
 * @author james
 * @date :2022/4/30 0:32
 */
public class AarSettings {
    private String groupId;
    private String version;
    public String aarDir;
    public GStringImpl mavenUrl;

    public String getGroupId() {
        return StringUtil.isNullOrEmpty(groupId) ? "com.default" : groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getVersion() {
        return StringUtil.isNullOrEmpty(version) ? "1.0" : version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
