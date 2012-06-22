package org.jenkinsci.plugins.autozoil;

import org.jenkinsci.plugins.autozoil.AutozoilSource;
import org.jenkinsci.plugins.autozoil.model.AutozoilFile;
import org.jenkinsci.plugins.autozoil.model.AutozoilWorkspaceFile;
import hudson.model.AbstractBuild;
import hudson.model.Api;
import hudson.model.Item;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.autozoil.config.AutozoilConfig;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * @author Gregory Boissinot
 */
public class AutozoilResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The Autozoil report
     */
    private AutozoilReport report;

    /**
     * The Autozoil container with all source files
     */
    private AutozoilSourceContainer autozoilSourceContainer;

    /**
     * The build owner
     */
    private AbstractBuild<?, ?> owner;

    public AutozoilResult(AutozoilReport report, AutozoilSourceContainer autozoilSourceContainer, AbstractBuild<?, ?> owner) {
        this.report = report;
        this.autozoilSourceContainer = autozoilSourceContainer;
        this.owner = owner;
    }

    /**
     * Gets the remote API for the build result.
     *
     * @return the remote API
     */
    @SuppressWarnings("unused")
    public Api getApi() {
        return new Api(report);
    }

    @Exported
    public AutozoilReport getReport() {
        return report;
    }

    @SuppressWarnings("unused")
    public AbstractBuild<?, ?> getOwner() {
        return owner;
    }

    @SuppressWarnings("unused")
    public AutozoilSourceContainer getAutozoilSourceContainer() {
        return autozoilSourceContainer;
    }

    /**
     * Gets the dynamic result of the selection element.
     *
     * @param link     the link to identify the sub page to show
     * @param request  Stapler request
     * @param response Stapler response
     * @return the dynamic result of the analysis (detail page).
     * @throws java.io.IOException if an error occurs
     */
    @SuppressWarnings("unused")
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) throws IOException {

        if (link.startsWith("source.")) {

            if (!owner.getProject().getACL().hasPermission(Item.WORKSPACE)) {
                response.sendRedirect2("nosourcepermission");
                return null;
            }

            Map<Integer, AutozoilWorkspaceFile> agregateMap = autozoilSourceContainer.getInternalMap();

            if (agregateMap != null) {
                Integer key = Integer.parseInt(StringUtils.substringAfter(link, "source."));
                AutozoilWorkspaceFile vAutozoilWorkspaceFile = agregateMap.get(key);

                HashMap<Integer, List<AutozoilFile> > lineNumberToAutozoilFilesMap = 
                    autozoilSourceContainer.getLineNumberToAutozoilFilesMapWithoutOneWithKey(key);

                if (vAutozoilWorkspaceFile == null) {
                    throw new IllegalArgumentException("Error for retrieving the source file with link:" + link);
                }

                return new AutozoilSource(owner, vAutozoilWorkspaceFile, lineNumberToAutozoilFilesMap);
            }
        }
        return null;
    }


    /**
     * Renders the summary Autozoil report for the build result.
     *
     * @return the HTML fragment of the summary Autozoil report
     */
    @SuppressWarnings("unused")
    public String getSummary() {
        return AutozoilSummary.createReportSummary(this);
    }

    /**
     * Renders the detailed summary Autozoil report for the build result.
     *
     * @return the HTML fragment of the summary Autozoil report
     */
    @SuppressWarnings("unused")
    public String getDetails() {
        return AutozoilSummary.createReportSummaryDetails(this);
    }

    /**
     * Gets the previous Autozoil report for the build result.
     *
     * @return the previous Autozoil report
     */
    @SuppressWarnings("unused")
    private AutozoilReport getPreviousReport() {
        AutozoilResult previous = this.getPreviousResult();
        if (previous == null) {
            return null;
        } else {
            return previous.getReport();
        }
    }

    /**
     * Gets the previous Autozoil result for the build result.
     *
     * @return the previous Autozoil result
     */
    public AutozoilResult getPreviousResult() {
        AutozoilBuildAction previousAction = getPreviousAction();
        AutozoilResult previousResult = null;
        if (previousAction != null) {
            previousResult = previousAction.getResult();
        }

        return previousResult;
    }

    /**
     * Gets the previous Action for the build result.
     *
     * @return the previous Autozoil Build Action
     */
    private AutozoilBuildAction getPreviousAction() {
        AbstractBuild<?, ?> previousBuild = owner.getPreviousBuild();
        if (previousBuild != null) {
            return previousBuild.getAction(AutozoilBuildAction.class);
        }
        return null;
    }

    /**
     * Returns the number of new errors from the previous build result.
     *
     * @return the number of new errors
     */
    @SuppressWarnings("unused")
    public int getNumberNewErrorsFromPreviousBuild() {
        AutozoilResult previousAutozoilResult = getPreviousResult();
        if (previousAutozoilResult == null) {
            return 0;
        } else {
            int diff = this.report.getAllErrors().size() - previousAutozoilResult.getReport().getAllErrors().size();
            return (diff > 0) ? diff : 0;
        }
    }

    /**
     * Gets the number of errors according the selected types form the configuration user object.
     *
     * @param cppecheckConfig the Autozoil configuration object
     * @param checkNewError   true, if the request is for the number of new errors
     * @return the number of errors or new errors (if checkNewEroor is set to true) for the current configuration object
     * @throws java.io.IOException if an error occurs
     */
    public int getNumberErrorsAccordingConfiguration(AutozoilConfig cppecheckConfig, boolean checkNewError) throws IOException {

        if (cppecheckConfig == null) {
            throw new IOException("[ERROR] - The autozoil configuration file is missing. Could you save again your job configuration.");
        }

        int nbErrors = 0;
        int nbPreviousError = 0;
        AutozoilResult previousResult = this.getPreviousResult();

        //Error
        if (cppecheckConfig.getConfigTypeEvaluation().isTypeError()) {
            nbErrors = nbErrors + this.getReport().getErrorTypeList().size();
            if (previousResult != null) {
                nbPreviousError = nbPreviousError + previousResult.getReport().getErrorTypeList().size();
            }
        }

        //Spells
        if (cppecheckConfig.getConfigTypeEvaluation().isTypeSpell()) {
            nbErrors = nbErrors + this.getReport().getSpellTypeList().size();
            if (previousResult != null) {
                nbPreviousError = nbPreviousError + previousResult.getReport().getSpellTypeList().size();
            }
        }

        //Grammar
        if (cppecheckConfig.getConfigTypeEvaluation().isTypeGrammar()) {
            nbErrors = nbErrors + this.getReport().getGrammarTypeList().size();
            if (previousResult != null) {
                nbPreviousError = nbPreviousError + previousResult.getReport().getGrammarTypeList().size();
            }
        }

        //Typo
        if (cppecheckConfig.getConfigTypeEvaluation().isTypeTypo()) {
            nbErrors = nbErrors + this.getReport().getTypoTypeList().size();
            if (previousResult != null) {
                nbPreviousError = nbPreviousError + previousResult.getReport().getTypoTypeList().size();
            }
        }

        //Suppressor
        if (cppecheckConfig.getConfigTypeEvaluation().isTypeSuppressor()) {
            nbErrors = nbErrors + this.getReport().getTypoTypeList().size();
            if (previousResult != null) {
                nbPreviousError = nbPreviousError + previousResult.getReport().getSuppressorTypeList().size();
            }
        }


        if (checkNewError) {
            if (previousResult != null) {
                return nbErrors - nbPreviousError;
            } else {
                return 0;
            }
        } else {
            return nbErrors;
        }
    }

}
