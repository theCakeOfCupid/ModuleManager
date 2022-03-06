package com.james;


import static com.james.ModuleManagePlugin.ONE_KEY_PUBLISH;

import com.james.exts.ModuleConfig;
import com.james.exts.ModuleSettings;
import com.james.tasks.TaskManager;
import com.james.util.StringUtil;

import org.codehaus.groovy.runtime.GStringImpl;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.DependencySubstitutions;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.internal.artifacts.repositories.DefaultMavenArtifactRepository;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.impldep.org.eclipse.jgit.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author james
 * @date 2021/7/8
 */
public class ModuleManageInternalPlugin implements Plugin<Project> {
    private static final String PUBLISH_NAME = "module";
    private static final String DEFAULT_COMPONENT = "release";
    private static final String LIBRARY_PLUGIN_NAME = "com.android.library";
    private static final String MAVEN_PUBLISH_PLUGIN_NAME = "maven-publish";
    private static final String DEFAULT_PUBLISH_TASK = "publishToMavenLocal";
    private static final String ASSEMBLE_TASK = "assembleRelease";

    @Override
    public void apply(Project project) {
        System.out.println("ModuleManageInternalPlugin executed");

        ModuleSettings moduleSettings = project.getRootProject().getExtensions().getByType(
                ModuleSettings.class);

        project.beforeEvaluate(p -> {
            configMavenRepositories(p, moduleSettings);
            p.getPluginManager().apply(MAVEN_PUBLISH_PLUGIN_NAME);
        });

        project.afterEvaluate(p -> {
            configAutoPublishToMavenLocal(p, moduleSettings);
            configResolutionStrategy(p, moduleSettings);
        });

        project.getGradle().projectsEvaluated(gradle -> configPublish(project, moduleSettings));
    }

    /**
     * 配置maven仓库
     *
     * @param project        project
     * @param moduleSettings moduleSettings
     */
    private void configMavenRepositories(Project project, ModuleSettings moduleSettings) {
        String maven = getConfiguredMavenRepository(project, moduleSettings);
        if (!StringUtil.isNullOrEmpty(maven)) {
            project.getRepositories().maven(mp -> mp.setUrl(moduleSettings.mavenUrl));
        }
    }

    /**
     * 获取当前project需要配置的maven仓库
     *
     * @param project        project
     * @param moduleSettings moduleSettings
     * @return 仓库地址
     */
    private String getConfiguredMavenRepository(@NonNull Project project,
            @NonNull ModuleSettings moduleSettings) {

        ModuleConfig moduleConfig = moduleSettings.getModuleConfigHashMap().get(
                StringUtil.getNormalizeName(project.getName()));

        if (null != moduleConfig && null != moduleConfig.mavenUrl) {
            return moduleConfig.mavenUrl.toString();
        }

        if (!StringUtil.isNullOrEmpty(moduleSettings.mavenUrl)) {
            return moduleSettings.mavenUrl;
        }

        return null;
    }

    /**
     * 配置publish
     *
     * @param project project
     */
    private void configPublish(Project project, ModuleSettings moduleSettings) {
        if (!project.getPluginManager().hasPlugin(LIBRARY_PLUGIN_NAME)) {
            return;
        }

        ModuleConfig moduleConfig = moduleSettings.getModuleConfigHashMap().get(
                StringUtil.getNormalizeName(project.getName()));
        if (moduleConfig == null) {
            return;
        }

        System.out.println(project.getName() + " begin configPublish");

        moduleConfig.setProject(project);
        PublishingExtension publishingExt = project.getExtensions().getByType(
                PublishingExtension.class);

        String configuredMavenRepository = getConfiguredMavenRepository(project, moduleSettings);

        boolean hasSpecifyMaven = !StringUtil.isNullOrEmpty(configuredMavenRepository);

        System.out.println("hasSpecifyMaven:" + hasSpecifyMaven);

        if (hasSpecifyMaven) {
            publishingExt.getRepositories().maven(mp -> mp.setUrl(configuredMavenRepository));
        }

        PublicationContainer publications = publishingExt.getPublications();
        if (publications.findByName(PUBLISH_NAME) != null) {
            return;
        }
        publications.create(PUBLISH_NAME, MavenPublication.class, publication -> {
            SoftwareComponent release = project.getComponents().findByName(DEFAULT_COMPONENT);
            if (release == null) {
                System.out.println("can't find default component");
                return;
            }

            publication.from(release);
            if (moduleConfig.androidJavaDoc) {
                System.out.println(project.getName() + "---1doc---" + project.getTasks().findByName(
                        "androidJavadocJar"));
                publication.artifact(TaskManager.getAndroidJavadocJar(project));
            }
            if (moduleConfig.androidJavaSource) {
                publication.artifact(TaskManager.getAndroidSourcesJar(project));
            }
            System.out.println(
                    "publish = " + moduleConfig.getGroupId() + ":" + moduleConfig.getArtifactId()
                            + ":" + moduleConfig.getVersion());
            publication.setGroupId(moduleConfig.getGroupId());
            publication.setArtifactId(moduleConfig.getArtifactId());
            publication.setVersion(moduleConfig.getVersion());
        });
    }

    /**
     * 自动发布脚本
     *
     * @param project project
     */
    private void configAutoPublishToMavenLocal(Project project, ModuleSettings moduleSettings) {
        if (!project.getPluginManager().hasPlugin(LIBRARY_PLUGIN_NAME)) {
            return;
        }
        System.out.println(project.getName() + " begin configAutoPublish");
        if (!StringUtil.isNullOrEmpty(moduleSettings.mavenUrl)) {
            publishToMavenRepository(project, moduleSettings);
            return;
        }
        project.getTasks().whenTaskAdded(task -> {
            if (task.getName().equals(ASSEMBLE_TASK)) {
                Task publishTask = project.getTasks().getByName(DEFAULT_PUBLISH_TASK);
                task.finalizedBy(publishTask);
                configOneKeyPublish(project, publishTask);
            }
        });

    }

    /**
     * 配置一键发布
     *
     * @param project project
     */
    private void configOneKeyPublish(Project project, Task publishTask) {
        System.out.println("config " + ONE_KEY_PUBLISH);
        Task oneKeyPublish = project.getRootProject().getTasks().getByName(ONE_KEY_PUBLISH);
        oneKeyPublish.dependsOn(project.getTasks().getByName(ASSEMBLE_TASK));
        oneKeyPublish.mustRunAfter(publishTask);
    }

    /**
     * 发布到指定仓库
     *
     * @param project project
     */
    private void publishToMavenRepository(Project project, ModuleSettings moduleSettings) {
        if (StringUtil.isNullOrEmpty(moduleSettings.mavenUrl)) {
            return;
        }
        System.out.println("config publishToMavenRepository");
        char firstLetter = Character.toUpperCase(PUBLISH_NAME.charAt(0));
        String theRest = PUBLISH_NAME.substring(1);
        String result = firstLetter + theRest;
        String taskName = "publish" + result + "PublicationToMavenRepository";
        project.getTasks().whenTaskAdded(task -> {
            if (task.getName().equals(taskName)) {
                Task assembleTask = project.getTasks().getByName(ASSEMBLE_TASK);
                task.dependsOn(assembleTask);
                assembleTask.finalizedBy(task);
                configOneKeyPublish(project, task);
            }
        });
    }

    /**
     * 配置替换规则
     *
     * @param project        project
     * @param moduleSettings moduleSettings
     */
    private void configResolutionStrategy(Project project, ModuleSettings moduleSettings) {
        System.out.println("configResolutionStrategy");
        Map<String, String> resolutions = getResolutions(project, moduleSettings);
        if (resolutions.isEmpty()) {
            return;
        }
        project.getConfigurations().all(configuration -> {
            Set<Map.Entry<String, String>> entries = resolutions.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                configuration.resolutionStrategy(
                        resolutionStrategy -> resolutionStrategy.dependencySubstitution(
                                dependencySubstitutions -> {
                                    DependencySubstitutions.Substitution substitute =
                                            dependencySubstitutions.substitute(
                                                    dependencySubstitutions.project(
                                                            entry.getKey()));
                                    substitute.with(
                                            dependencySubstitutions.module(entry.getValue()));
                                }));
            }
        });
    }

    /**
     * 获取替换对象集合
     *
     * @param project        project
     * @param moduleSettings moduleSettings
     * @return 替换对象集合
     */
    private Map<String, String> getResolutions(Project project, ModuleSettings moduleSettings) {
        HashMap<String, String> resolutionHashMap = new HashMap<>();
        ConfigurationContainer configurations = project.getConfigurations();
        for (Configuration configuration : configurations) {
            DependencySet allDependencies = configuration.getAllDependencies();
            for (Dependency dependency : allDependencies) {
                if (!(dependency instanceof ProjectDependency)) {
                    continue;
                }
                String normalizeName = StringUtil.getNormalizeName(dependency.getName());
                ModuleConfig moduleConfig = moduleSettings.getModuleConfigHashMap().get(
                        normalizeName);
                if (null == moduleConfig) {
                    continue;
                }

                GStringImpl ml = moduleConfig.mavenUrl;
                String mavenUrl = ml == null ? "" : ml.toString();

                if (!StringUtil.isNullOrEmpty(mavenUrl)) {
                    mavenIfItNotExist(project, mavenUrl);
                }

                if (!moduleConfig.useByAar) {
                    continue;
                }
                resolutionHashMap.put(":" + dependency.getName(), moduleConfig.getPath());
            }
        }
        return resolutionHashMap;
    }

    /**
     * 如果当前project不包含该maven仓库，则添加该maven仓库
     *
     * @param project  project
     * @param mavenUrl mavenUrl
     */
    private void mavenIfItNotExist(@NonNull Project project, @NonNull String mavenUrl) {
        if (StringUtil.isNullOrEmpty(mavenUrl)) {
            return;
        }
        //防止重复添加
        RepositoryHandler repositories = project.getRepositories();
        for (ArtifactRepository artifactRepository : repositories) {
            if (artifactRepository instanceof DefaultMavenArtifactRepository) {

                DefaultMavenArtifactRepository defaultMavenArtifactRepository =
                        (DefaultMavenArtifactRepository) artifactRepository;
                String url = defaultMavenArtifactRepository.getUrl().getRawPath();

                if (getTempFormatRawPath(url).equals(getTempFormatRawPath(mavenUrl))) {
                    return;
                }
            }
        }
        project.getRepositories().maven(mp -> mp.setUrl(mavenUrl));
    }

    private String getTempFormatRawPath(String url) {
        if (StringUtil.isNullOrEmpty(url)) {
            return url;
        }
        String temp = url;
        temp = temp.replaceAll("\\\\", "");
        temp = temp.replaceAll("/", "");
        return temp;
    }

}
