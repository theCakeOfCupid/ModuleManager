package com.james.tasks;

import com.android.build.gradle.LibraryExtension;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

/**
 * refer to {@link 'https://github.com/vanniktech/gradle-maven-publish-plugin'}
 *
 * @author james
 * @date 2021/7/27
 */
public class TaskManager {

    /**
     * 获取AndroidSourcesJarTask
     *
     * @param project project
     * @return AndroidSourcesJar
     */
    public static SourcesJar getAndroidSourcesJar(Project project) {
        return project.getTasks().create("androidSourcesJar",
                SourcesJar.class, sourcesJar -> {
                    LibraryExtension libraryExtension = project.getExtensions().getByType(
                            LibraryExtension.class);
                    sourcesJar.from(libraryExtension.getSourceSets().getByName(
                            "main").getJava().getSrcDirs());
                });
    }

    /**
     * 获取JavaSourcesJarTaskProvider
     *
     * @param project project
     * @return JavaSourcesJarTaskProvider
     */
    public static TaskProvider<?> getJavaSourcesJar(Project project) {
        return project.getTasks().register("javaSourcesJar",
                SourcesJar.class, sourcesJar -> {
                    JavaPluginConvention plugin = project.getConvention().getPlugin(
                            JavaPluginConvention.class);
                    sourcesJar.from(plugin.getSourceSets().getByName("main").getAllSource());
                });
    }

    /**
     * 获取AndroidJavaDocTask
     *
     * @param project project
     * @return AndroidJavaDocTask
     */
    public static JavadocJarTask getAndroidJavadocJar(Project project) {
        TaskContainer tasks = project.getTasks();
        TaskProvider<AndroidJavaDocsTask> androidJavadoc = tasks.register(
                "androidJavadoc", AndroidJavaDocsTask.class);
        JavadocJarTask androidJavadocJar = project.getTasks().create("androidJavadocJar",
                JavadocJarTask.class,
                javadocJarTask -> {
                    javadocJarTask.dependsOn(androidJavadoc);
                    javadocJarTask.from(androidJavadoc);
                });
        return androidJavadocJar;
    }

//    public static TaskProvider<?> getSimple(Project project){
//        project.tasks.register("simpleJavadocJar", JavadocJar) {
//            val task = tasks.named("javadoc")
//            it.dependsOn(task)
//            it.from(task)
//        }
//    }
}
