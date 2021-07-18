package com.james;


import com.james.exts.ModuleConfig;
import com.james.exts.ModuleSettings;
import com.james.exts.NormalizeConfig;
import com.james.tasks.OneKeyPublishTask;
import com.james.util.StringUtil;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Set;

/**
 * @author james
 * @date 2021/7/7
 */
public class ModuleManagePlugin implements Plugin<Project> {
    public static final String ONE_KEY_PUBLISH = "oneKeyPublish";

    @Override
    public void apply(Project project) {
        System.out.println("ModuleManagePlugin executed");
        ModuleSettings moduleSettings = project.getExtensions().create("moduleSettings",
                ModuleSettings.class);
        initSubProject(project, moduleSettings);
    }

    private void initNormalize(Project project, NormalizeConfig normalizeConfig) {
        if (!normalizeConfig.moduleNormalizes.isEmpty()) {
            System.out.println("normalize is not empty");
            Set<Project> subProjects = project.getSubprojects();
            for (Project subProject : subProjects) {
                String value = normalizeConfig.moduleNormalizes.get(subProject.getName());
                if (value != null) {
                    System.out.println("value:" + value);
                    project.getRootProject().getExtensions().create(value, ModuleConfig.class);
                }
            }
        } else {
            System.out.println("normalize is empty");
        }
    }

    private void initSubProject(Project project, ModuleSettings moduleSettings) {
        Set<Project> subProjects = project.getSubprojects();
        project.getTasks().register(ONE_KEY_PUBLISH, OneKeyPublishTask.class);
        for (Project subProject : subProjects) {
            subProject.beforeEvaluate(pt -> {
                if (!StringUtil.isNullOrEmpty(moduleSettings.mavenUrl)) {
                    pt.getRepositories().maven(mp -> mp.setUrl(moduleSettings.mavenUrl));
                }
            });

            String name = StringUtil.getNormalizeName(subProject.getName());
            System.out.println("names:" + name);
            project.getRootProject().getExtensions().create(name, ModuleConfig.class);
        }
        for (Project p : subProjects) {
            p.getPlugins().apply(ModuleManageInternalPlugin.class);
        }
    }
}
