package org.jenkinsci.plugins.autozoil;


import org.jenkinsci.plugins.autozoil.graph.AutozoilGraph;
import org.jenkinsci.plugins.autozoil.util.AbstractAutozoilBuildAction;
import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import org.jenkinsci.plugins.autozoil.config.AutozoilConfig;
import org.jenkinsci.plugins.autozoil.config.AutozoilConfigGraph;
import org.jenkinsci.plugins.autozoil.util.AutozoilBuildHealthEvaluator;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.Calendar;

/**
 * @author Gregory Boissinot
 */
public class AutozoilBuildAction extends AbstractAutozoilBuildAction {

    public static final String URL_NAME = "autozoilResult";

    private AutozoilResult result;
    private AutozoilConfig autozoilConfig;

    public AutozoilBuildAction(AbstractBuild<?, ?> owner, AutozoilResult result, AutozoilConfig autozoilConfig) {
        super(owner);
        this.result = result;
        this.autozoilConfig = autozoilConfig;
    }

    public String getIconFileName() {
        return "/plugin/autozoil/icons/autozoil-24.png";
    }

    public String getDisplayName() {
        return "Autozoil Result";
    }

    public String getUrlName() {
        return URL_NAME;
    }

    public String getSearchUrl() {
        return getUrlName();
    }

    public AutozoilResult getResult() {
        return this.result;
    }

    AbstractBuild<?, ?> getBuild() {
        return this.owner;
    }

    public Object getTarget() {
        return this.result;
    }

    public HealthReport getBuildHealth() {
        try {
            return new AutozoilBuildHealthEvaluator().evaluatBuildHealth(autozoilConfig, result.getNumberErrorsAccordingConfiguration(autozoilConfig, false));
        } catch (IOException ioe) {
            return new HealthReport();
        }
    }

    private DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> getDataSetBuilder() {
        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

        for (AutozoilBuildAction a = this; a != null; a = a.getPreviousResult()) {
            ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(a.owner);
            AutozoilReport report = a.getResult().getReport();
            AutozoilConfigGraph configGraph = autozoilConfig.getConfigGraph();

            // error
            if (configGraph.isDisplayErrorType())
                dsb.add(report.getErrorTypeList().size(), "Type 'latex'", label);

            //spell
            if (configGraph.isDisplaySpellType())
                dsb.add(report.getSpellTypeList().size(), "Type 'spell'", label);

            //grammar
            if (configGraph.isDisplayGrammarType())
                dsb.add(report.getGrammarTypeList().size(), "Type 'grammar'", label);

            //typo
            if (configGraph.isDisplayTypoType())
                dsb.add(report.getTypoTypeList().size(), "Type 'typo'", label);

            //suppressor
            if (configGraph.isDisplaySuppressorType())
                dsb.add(report.getTypoTypeList().size(), "Type 'suppressor'", label);

            // all errors
            if (configGraph.isDisplayAllErrors())
                dsb.add(report.getAllErrors().size(), "All errors", label);

        }
        return dsb;
    }

    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (ChartUtil.awtProblemCause != null) {
            rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
            return;
        }

        Calendar timestamp = getBuild().getTimestamp();
        if (req.checkIfModified(timestamp, rsp)) {
            return;
        }

        Graph g = new AutozoilGraph(getOwner(), getDataSetBuilder().build(),
                "Number of errors", autozoilConfig.getConfigGraph().getXSize(), autozoilConfig.getConfigGraph().getYSize());
        g.doPng(req, rsp);
    }

    // Backward compatibility
    @Deprecated
    private transient AbstractBuild<?, ?> build;

    /**
     * Initializes members that were not present in previous versions of this plug-in.
     *
     * @return the created object
     */
    @SuppressWarnings({"deprecation", "unused"})
    private Object readResolve() {
        if (build != null) {
            this.owner = build;
        }
        return this;
    }

}
