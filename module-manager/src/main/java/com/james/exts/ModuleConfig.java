package com.james.exts;


import com.james.util.StringUtil;

import org.codehaus.groovy.runtime.GStringImpl;
import org.gradle.api.Project;

/**
 * @author james
 * @date 2021/7/12
 */
public class ModuleConfig {
    public boolean enabled;
    public String groupId;
    public String artifactId;
    public String version;
    public String name;
    private String path;
    public boolean useByAar;
    private Project project;
    public GStringImpl mavenUrl;
    public boolean androidJavaDoc;
    public boolean androidJavaSource;
    public boolean needAndroidJavaDocsAddReferencesLinks;

    public void setProject(Project project) {
        this.project = project;
    }

    public String getGroupId() {
        return StringUtil.isNullOrEmpty(groupId) ? "com.default" : groupId;
    }

    public String getArtifactId() {
        return StringUtil.isNullOrEmpty(artifactId) ? project.getName() : artifactId;
    }

    public String getVersion() {
        return StringUtil.isNullOrEmpty(version) ? "1.0" : version;
    }

    public String getName() {
        return StringUtil.isNullOrEmpty(name) ? project.getName() : name;
    }

    public String getPath() {
        return getGroupId() + ":" + getArtifactId() + ":" + getVersion();
    }

}
