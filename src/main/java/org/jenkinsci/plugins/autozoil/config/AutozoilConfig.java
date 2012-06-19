package org.jenkinsci.plugins.autozoil.config;


import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class AutozoilConfig implements Serializable {

    private String pattern;
    private boolean ignoreBlankFiles;
    private AutozoilConfigTypeEvaluation configTypeEvaluation = new AutozoilConfigTypeEvaluation();
    private AutozoilConfigGraph configGraph = new AutozoilConfigGraph();

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setIgnoreBlankFiles(boolean ignoreBlankFiles) {
        this.ignoreBlankFiles = ignoreBlankFiles;
    }

    public void setConfigTypeEvaluation(AutozoilConfigTypeEvaluation configTypeEvaluation) {
        this.configTypeEvaluation = configTypeEvaluation;
    }

    public void setConfigGraph(AutozoilConfigGraph configGraph) {
        this.configGraph = configGraph;
    }

    public void setAutozoilReportPattern(String autozoilReportPattern) {
        this.autozoilReportPattern = autozoilReportPattern;
    }

    public void setUseWorkspaceAsRootPath(boolean useWorkspaceAsRootPath) {
        this.useWorkspaceAsRootPath = useWorkspaceAsRootPath;
    }

    public String getPattern() {
        return pattern;
    }

    @Deprecated
    public String getAutozoilReportPattern() {
        return autozoilReportPattern;
    }

    public boolean isUseWorkspaceAsRootPath() {
        return useWorkspaceAsRootPath;
    }

    public boolean isIgnoreBlankFiles() {
        return ignoreBlankFiles;
    }

    public AutozoilConfigTypeEvaluation getConfigTypeEvaluation() {
        return configTypeEvaluation;
    }

    public AutozoilConfigGraph getConfigGraph() {
        return configGraph;
    }

    /*
    Backward compatibility
     */
    private transient String autozoilReportPattern;
    private transient boolean useWorkspaceAsRootPath;

    @SuppressWarnings("unused")
    private Object readResolve() {
        if (this.autozoilReportPattern != null) {
            this.pattern = autozoilReportPattern;
        }
        return this;
    }

}
