package org.jenkinsci.plugins.autozoil;

import org.jenkinsci.plugins.autozoil.model.AutozoilFile;
import org.jenkinsci.plugins.autozoil.model.AutozoilWorkspaceFile;
import org.jenkinsci.plugins.autozoil.util.AutozoilLogger;
import hudson.FilePath;
import hudson.model.BuildListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gregory Boissinot
 */
public class AutozoilSourceContainer {

    private Map<Integer, AutozoilWorkspaceFile> internalMap = new HashMap<Integer, AutozoilWorkspaceFile>();

    public AutozoilSourceContainer(Map<Integer, AutozoilWorkspaceFile> internalMap) {
        this.internalMap = internalMap;
    }

    public AutozoilSourceContainer(BuildListener listener,
                                   FilePath workspace,
                                   FilePath scmRootDir,
                                   List<AutozoilFile> files) throws IOException, InterruptedException {

        int key = 1;
        for (AutozoilFile autozoilFile : files) {
            AutozoilWorkspaceFile autozoilWorkspaceFile = getAutozoilWorkspaceFile(listener, workspace, scmRootDir, autozoilFile);
            //The key must be unique for all the files/errors through the merge
            autozoilFile.setKey(key);
            autozoilWorkspaceFile.setAutozoilFile(autozoilFile);
            internalMap.put(key, autozoilWorkspaceFile);
            key = ++key;
        }
    }

    private AutozoilWorkspaceFile getAutozoilWorkspaceFile(BuildListener listener,
                                                           FilePath workspace,
                                                           FilePath scmRootDir,
                                                           AutozoilFile autozoilFile) throws IOException, InterruptedException {

        String autozoilFileName = autozoilFile.getFileName();

        if (autozoilFileName == null) {
            AutozoilWorkspaceFile autozoilWorkspaceFile = new AutozoilWorkspaceFile();
            autozoilWorkspaceFile.setFileName(null);
            autozoilWorkspaceFile.setSourceIgnored(true);
            return autozoilWorkspaceFile;
        }

        AutozoilWorkspaceFile autozoilWorkspaceFile = new AutozoilWorkspaceFile();
        FilePath sourceFilePath = getSourceFile(workspace, scmRootDir, autozoilFileName);
        if (!sourceFilePath.exists()) {
            AutozoilLogger.log(listener, "[WARNING] - The source file '" + sourceFilePath.toURI() + "' doesn't exist on the slave. The ability to display its source code has been removed.");
            autozoilWorkspaceFile.setFileName(null);
            autozoilWorkspaceFile.setSourceIgnored(true);
        } else if (sourceFilePath.isDirectory()) {
            autozoilWorkspaceFile.setFileName(sourceFilePath.getRemote());
            autozoilWorkspaceFile.setSourceIgnored(true);
        } else {
            autozoilWorkspaceFile.setFileName(sourceFilePath.getRemote());
            autozoilWorkspaceFile.setSourceIgnored(false);
        }

        return autozoilWorkspaceFile;
    }

    private FilePath getSourceFile(FilePath workspace, FilePath scmRootDir, String autozoilFileName) throws IOException, InterruptedException {
        FilePath sourceFilePath = new FilePath(scmRootDir, autozoilFileName);
        if (!sourceFilePath.exists()) {
            //try from workspace
            sourceFilePath = new FilePath(workspace, autozoilFileName);
        }
        return sourceFilePath;
    }


    public Map<Integer, AutozoilWorkspaceFile> getInternalMap() {
        return internalMap;
    }

}
