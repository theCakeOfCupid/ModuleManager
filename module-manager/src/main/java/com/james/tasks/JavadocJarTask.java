package com.james.tasks;

import org.gradle.jvm.tasks.Jar;

/**
 * @author james
 * @date 2021/7/27
 */
public class JavadocJarTask extends Jar {

    {
        getArchiveClassifier().set("javadoc");
    }

}
