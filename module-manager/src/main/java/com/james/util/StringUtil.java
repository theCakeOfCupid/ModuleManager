package com.james.util;

/**
 * @author james
 * @date 2021/7/12
 */
public class StringUtil {

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String getNormalizeName(String projectName) {
        if (isNullOrEmpty(projectName)) {
            return projectName;
        }
        projectName = projectName.replaceAll("_", "");
        projectName = projectName.replaceAll("-", "");
        return projectName;
    }
}
