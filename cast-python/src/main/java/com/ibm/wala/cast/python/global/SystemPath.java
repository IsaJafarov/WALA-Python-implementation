package com.ibm.wala.cast.python.global;

import com.ibm.wala.cast.python.util.PathUtil;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class SystemPath {
    private Set<Path> libPaths;
    private Path appPath;
    private boolean appPathLocked;

    public void unlockAppPath() {
        this.appPathLocked = false;
    }

    public Set<Path> getLibPaths() {
        return libPaths;
    }

    public void addLibPath(Path libPath) {
        this.libPaths.add(libPath);
    }

    public Path getAppPath() {
        return appPath;
    }

    public void setAppPath(Path appPath) {
        if (!appPathLocked) {
            this.appPath = appPath;
            appPathLocked = true;
        } else {
            System.err.println("[WARNING] App path is settled before");
        }
    }

    public Path getImportModule(String scriptName, String module) {
        // if app module
        Path importedPath = null;
        if (scriptName.startsWith(PathUtil.getUriString(appPath))) {
            Path importScript = PathUtil.getPath(scriptName);
            int i = 0;
            while (i < module.length() && module.charAt(i) == '.') {
                importScript = importScript.getParent();
                i++;
            }
            if (i + 1 < module.length()) {
                // 防止 `from . import yyy`
                importScript = importScript.resolve(module.substring(i));
            }
            if (i > 0) {
                // from .xxx import yyy
                importedPath = importScript;
            } else {
                // import xxx
                importedPath = appPath.resolve(module);
            }
            if (importedPath.toFile().isDirectory() || new File(importedPath.toString().replace("file:/", "/")).isDirectory()) {
                // `import lib`
                importedPath = importedPath.resolve("__init__");
            }
        }
        if (importedPath != null && (
                new File(importedPath.toString() + ".py").exists() ||
                        new File(importedPath.toString().replace("file:/","/") + ".py").exists())
        ) {
            return importedPath;
        }
        for (Path libPath : libPaths) {
            // script本身就在lib中
            if (scriptName.startsWith(PathUtil.getUriString(libPath))) {
                Path importScript = PathUtil.getPath(scriptName);
                int i = 0;
                while (i < module.length() && module.charAt(i) == '.') {
                    importScript = importScript.getParent();
                    i++;
                }
                if (i + 1 < module.length()) {
                    // 防止 `from . import yyy`
                    importScript = importScript.resolve(module.substring(i));
                }
                if (i > 0) {
                    // from .xxx import yyy
                    importedPath = importScript;
                } else {
                    // import xxx
                    importedPath = libPath.resolve(module);
                }
                if (importedPath.toFile().isDirectory() || new File(importedPath.toString().replace("file:/", "/")).isDirectory()) {
                    // `import libdir`
                    importedPath = importedPath.resolve("__init__");
                }
            } else if (libPath.resolve(module+".py").toFile().exists()||
                    new File(libPath.resolve(module+".py").toString().replace("file:/","/")).exists()
            ) {
                // `import lib`
                importedPath = libPath.resolve(module);
            } else if( libPath.endsWith(module) && (libPath.resolve("__init__.py").toFile().exists()||
                    new File(libPath.resolve("__init__.py").toString().replace("file:/","/")).exists()
            ) ){
                // `import lib`
                importedPath = libPath.resolve("__init__");
            }
            if (importedPath != null && (new File(importedPath.toString() + ".py").exists()||
                    (new File(importedPath.toString().replace("file:/","/") + ".py")).exists()
            )) {
                return importedPath;
            }
        }
        System.err.println("[WARNING] Can't get module: " + module + " in system path, treat it as black box module");
        return new File("UNKNOWN_"+module).toPath();

//        throw new NotImplementedException();

    }

    private static class SingletonHolder {
        private static SystemPath instance = new SystemPath();
    }

    private SystemPath() {
        libPaths = new HashSet<>();
    }

    public static SystemPath getInstance() {
        return SingletonHolder.instance;
    }

}
