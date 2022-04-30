package com.james;


import static com.james.ModuleManageInternalPlugin.MAVEN_PUBLISH_PLUGIN_NAME;
import static com.james.ModuleManageInternalPlugin.PUBLISH_ALL_PUBLICATIONS;

import com.james.exts.AarSettings;
import com.james.exts.ModuleConfig;
import com.james.exts.ModuleSettings;
import com.james.exts.NormalizeConfig;
import com.james.tasks.OneKeyPublishTask;
import com.james.util.StringUtil;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

/**
 * @author james
 * @date 2021/7/7
 */
public class ModuleManagePlugin implements Plugin<Project> {
    public static final String ONE_KEY_PUBLISH = "oneKeyPublish";
    public static final String ONE_KEY_PUBLISH_LOCAL_AAR = "oneKeyPublishLocalAar";

    @Override
    public void apply(Project project) {
        System.out.println("ModuleManagePlugin executed");
        ModuleSettings moduleSettings = project.getExtensions().create("moduleSettings",
                ModuleSettings.class);
        AarSettings aarSettings = project.getExtensions().create("localAarSettings",
                AarSettings.class);
        initSubProject(project);
        if (!project.getPluginManager().hasPlugin(MAVEN_PUBLISH_PLUGIN_NAME)) {
            project.getPluginManager().apply(MAVEN_PUBLISH_PLUGIN_NAME);
        }
        project.afterEvaluate(p -> {
            configLocalAar(project, aarSettings);
        });
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

    /**
     * 配置本地aar发布脚本
     */
    private void configLocalAar(Project project, AarSettings aarSettings) {
        project.getTasks().whenTaskAdded(task -> {
            if (task.getName().equals(PUBLISH_ALL_PUBLICATIONS)) {
                TaskContainer tasks = project.getTasks();
                TaskProvider<Task> taskPublishAar = tasks.register(ONE_KEY_PUBLISH_LOCAL_AAR,
                        (publish) -> System.out.println("begin publish all local aar"));
                Task t = project.getTasks().getByName(PUBLISH_ALL_PUBLICATIONS);
                taskPublishAar.get().finalizedBy(t);
            }
        });
        configLocalAarInternal(project, aarSettings);
    }

    private void configLocalAarInternal(Project project, AarSettings aarSettings) {
        if (StringUtil.isNullOrEmpty(aarSettings.aarDir)) {
            System.out.println("local aarDir not config");
            return;
        }
        File file = new File(aarSettings.aarDir);
        if (!file.isDirectory()) {
            System.out.println("local aarDir is not a directory");
            return;
        }
        File[] files = file.listFiles();
        if (files == null || files.length <= 0) {
            System.out.println("local aarDir is empty");
            return;
        }
        configLocalAarMavenPublication(project, aarSettings, files);
    }

    private void configLocalAarMavenPublication(Project project,
            AarSettings aarSettings, File[] files) {
        PublishingExtension publishingExt = project.getExtensions().getByType(
                PublishingExtension.class);

        String localAarMavenRepository = aarSettings.mavenUrl.toString();

        boolean hasSpecifyMaven = !StringUtil.isNullOrEmpty(localAarMavenRepository);

        System.out.println("local aar maven path configs:" + localAarMavenRepository);

        if (hasSpecifyMaven) {
            //为每个subject配置maven仓库地址
            for (Project subproject : project.getSubprojects()) {
                subproject.getRepositories().maven(mp -> mp.setUrl(localAarMavenRepository));
            }
        }
        HashMap<String, String> aarMap = new HashMap<>();
        project.getExtensions().add("aars", aarMap);
        publishingExt.getRepositories().maven(mp -> mp.setUrl(localAarMavenRepository));
        PublicationContainer publications = publishingExt.getPublications();
        for (File file : files) {
            if (!file.getName().endsWith(".aar")) {
                continue;
            }
            String publishName = StringUtil.getNormalizeName(file.getName());
            publications.create(publishName, MavenPublication.class, publication -> {
                publication.artifact(file.getAbsolutePath());
                System.out.println(
                        "publish = " + aarSettings.getGroupId() + ":" + publishName
                                + ":" + aarSettings.getVersion());
                publication.setGroupId(aarSettings.getGroupId());
                publication.setArtifactId(publishName);
                publication.setVersion(aarSettings.getVersion());
                aarMap.put(file.getName(),
                        StringUtil.getMavenPath(aarSettings.getGroupId(), publishName,
                                aarSettings.getVersion()));
            });
        }
    }

    private void initSubProject(Project project) {
        Set<Project> subProjects = project.getSubprojects();
        project.getTasks().register(ONE_KEY_PUBLISH, OneKeyPublishTask.class);
        for (Project p : subProjects) {
            System.out.println(p.getName());
            p.getPlugins().apply(ModuleManageInternalPlugin.class);
        }
    }
}
