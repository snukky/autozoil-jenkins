/*******************************************************************************
 * Copyright (c) 2009 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot                                                   *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/

package org.jenkinsci.plugins.autozoil.model;

import org.jenkinsci.plugins.autozoil.util.AutozoilLogger;
import hudson.FilePath;
import hudson.model.BuildListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class AutozoilSourceContainer {


    private Map<Integer, AutozoilWorkspaceFile> internalMap = new HashMap<Integer, AutozoilWorkspaceFile>();

    public AutozoilSourceContainer(BuildListener listener, FilePath basedir, List<AutozoilFile> files) throws IOException, InterruptedException {

        int key = 1;
        for (AutozoilFile autozoilFile : files) {

            AutozoilWorkspaceFile autozoilWorkspaceFile = new AutozoilWorkspaceFile();

            String autozoilFileName = autozoilFile.getFileName();
            if (autozoilFileName == null) {
                autozoilWorkspaceFile.setFileName(null);
                autozoilWorkspaceFile.setSourceIgnored(true);
            } else {
                FilePath sourceFilePath = new FilePath(basedir, autozoilFileName);
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
            }

            //The key must be unique for all the files/errors through the merge
            autozoilFile.setKey(key);
            autozoilWorkspaceFile.setAutozoilFile(autozoilFile);
            internalMap.put(key, autozoilWorkspaceFile);
            key = ++key;
        }
    }


    public Map<Integer, AutozoilWorkspaceFile> getInternalMap() {
        return internalMap;
    }
}
