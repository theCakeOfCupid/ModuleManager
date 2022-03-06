package com.james.tasks;

import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.LibraryVariant;

import com.james.exts.ModuleConfig;
import com.james.util.StringUtil;

import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.io.File;

/**
 * @author james
 * @date 2021/7/27
 */
public class AndroidJavaDocsTask extends Javadoc {
    private static final String ANDROID_PLUGIN = "org.jetbrains.kotlin.android";

    public void AndroidJavaDocsTask() {
        Project project = getProject();
        ModuleConfig moduleConfig = project.getExtensions().getByType(ModuleConfig.class);
        LibraryExtension libraryExtension = project.getExtensions().getByType(
                LibraryExtension.class);
        LibraryVariant[] libraryVariants = new LibraryVariant[]{};
        libraryExtension.getLibraryVariants().toArray(libraryVariants);
        int index = libraryVariants.length - 1;
        LibraryVariant libraryVariant = libraryVariants[index];
        TaskProvider<JavaCompile> javaCompileProvider = libraryVariant.getJavaCompileProvider();
        if (!project.getPluginManager().hasPlugin(ANDROID_PLUGIN)) {
            setSource(libraryExtension.getSourceSets().getByName("main").getJava().getSrcDirs());
        }

        setFailOnError(true);
        FileCollection classpath = getClasspath();
        classpath.plus(project.files(
                StringUtil.joinToString(libraryExtension.getBootClasspath(), File.separator)));
        // Safe to call get() here because we'ved marked this as dependent on the TaskProvider
        classpath.plus(javaCompileProvider.get().getClasspath());
        classpath.plus(javaCompileProvider.get().getOutputs().getFiles());

        // We don't need javadoc for internals.
        exclude("**/internal/*");

        // Append Java 8 and Android references

        StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) getOptions();

        if (moduleConfig.needAndroidJavaDocsAddReferencesLinks) {
            options.links("https://developer.android.com/reference");
            options.links("https://docs.oracle.com/javase/8/docs/api/");
        }

        // Workaround for the following error when running on on JDK 9+
        // "The code being documented uses modules but the packages defined in ... are in the
        // unnamed module."
        if (JavaVersion.current().ordinal() >= JavaVersion.VERSION_1_9.ordinal()) {
            options.addStringOption("-release", "8");
        }
    }
}
