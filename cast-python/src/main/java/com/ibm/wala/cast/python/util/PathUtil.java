package com.ibm.wala.cast.python.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;


public class PathUtil {
    public static String relPath(String fullPath, String[] rootPath) {
        String[] path = fullPath.split("/");
        String[] relPath = Arrays.copyOfRange(path, rootPath.length, path.length);
        return String.join("/", relPath);
    }

    public static String relPath(String fullPath, String rootPath) {
        return relPath(fullPath, rootPath.split("/"));
    }

    public static String getUriString(Path path) {
        String os = System.getProperty("os.name").toLowerCase();
        String uriString;
        if (os.contains("windows")) {
            uriString = path.toUri().toString().replace("file:///", "file:/");
        } else {
            uriString = path.toString();
        }
        if (!uriString.startsWith("file:")){
            uriString="file:"+uriString;
        }
        return uriString;
    }

    public static String getUriString(String path) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            return path.replace("file:///", "file:/");
        } else {
            return path.toString();
        }
    }


    public static Path getPath(URL url) {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return Paths.get(url.getPath().substring(1));
        } else {
            return Paths.get(url.getPath());
        }
    }

    public static Path getPath(String base, String... more) {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            try {
                return Paths.get(new URL(base).getPath().substring(1), more);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return Paths.get(base, more);
        }
    }
}
