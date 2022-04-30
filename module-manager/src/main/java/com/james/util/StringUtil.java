package com.james.util;

import java.util.List;

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
        projectName = projectName.replaceAll("\\.", "");
        return projectName;
    }

    public static String joinToString(List<?> list, String joined) {
        StringBuilder stringBuffer = new StringBuilder();
        int count = 0;
        for (Object o : list) {
            if (++count > 1) stringBuffer.append(joined);
            stringBuffer.append(o.toString());
        }
        return stringBuffer.toString();
    }

    public static String getMavenPath(String groupId,String name, String version){
        return groupId+":"+name+":"+version;
    }
}
