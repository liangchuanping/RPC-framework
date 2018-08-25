package com.rpc.common.util;

import com.google.common.base.Splitter;

import java.util.List;

/**
 * @Author: Bojun Ji
 * @Description:
 * @Date: 2018/8/26_12:05 AM
 */
public class StringUtil {
    public static String getZkSubPath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        List<String> results = Splitter.on("/").trimResults().omitEmptyStrings().splitToList(path);
        if (results.isEmpty()) {
            return "";
        } else {
            return results.get(1);
        }
    }
}
