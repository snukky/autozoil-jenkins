package org.jenkinsci.plugins.autozoil;

import org.jenkinsci.plugins.autozoil.model.AutozoilWorkspaceFile;
import org.jenkinsci.plugins.autozoil.util.AutozoilLogger;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixProject;
import hudson.model.*;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import org.jenkinsci.plugins.autozoil.config.AutozoilConfig;
import org.jenkinsci.plugins.autozoil.config.AutozoilConfigGraph;
import org.jenkinsci.plugins.autozoil.config.AutozoilConfigTypeEvaluation;
import org.jenkinsci.plugins.autozoil.util.AutozoilBuildResultEvaluator;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Gregory Boissinot
 */
public class AutozoilPublisher extends Publisher {

    private AutozoilConfig autozoilConfig;

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public AutozoilPublisher(String pattern,
                             boolean ignoreBlankFiles, String threshold,
                             String newThreshold, String failureThreshold,
                             String newFailureThreshold, String healthy, String unHealthy,
                             boolean typeError,
                             boolean typeSpell,
                             boolean typeGrammar,
                             boolean typeTypo,
                             boolean typeSuppressor,
                             int xSize, int ySize,
                             boolean displayAllErrors,
                             boolean displayErrorType,
                             boolean displaySpellType,
                             boolean displayGrammarType,
                             boolean displayTypoType,
                             boolean displaySuppressorType) {

        autozoilConfig = new AutozoilConfig();

        autozoilConfig.setPattern(pattern);
        autozoilConfig.setIgnoreBlankFiles(ignoreBlankFiles);
        AutozoilConfigTypeEvaluation configTypeEvaluation = new AutozoilConfigTypeEvaluation(
                threshold, newThreshold, failureThreshold, newFailureThreshold, healthy, unHealthy,
                typeError,
                typeSpell,
                typeGrammar,
                typeTypo,
                typeSuppressor);
        autozoilConfig.setConfigTypeEvaluation(configTypeEvaluation);
        AutozoilConfigGraph configGraph = new AutozoilConfigGraph(
                xSize, ySize,
                displayAllErrors,
                displayErrorType,
                displaySpellType,
                displayGrammarType,
                displayTypoType,
                displaySuppressorType);
        autozoilConfig.setConfigGraph(configGraph);
    }


    @SuppressWarnings("unused")
    public AutozoilConfig getAutozoilConfig() {
        return autozoilConfig;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new AutozoilProjectAction(project);
    }

    protected boolean canContinue(final Result result) {
        return result != Result.ABORTED && result != Result.FAILURE;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        if (this.canContinue(build.getResult())) {
            AutozoilLogger.log(listener, "Starting the autozoil analysis.");

            AutozoilParserResult parser = new AutozoilParserResult(listener, autozoilConfig.getPattern(), autozoilConfig.isIgnoreBlankFiles());
            AutozoilReport autozoilReport;
            try {
                autozoilReport = build.getWorkspace().act(parser);
            } catch (Exception e) {
                AutozoilLogger.log(listener, "Error on autozoil analysis: " + e);
                build.setResult(Result.FAILURE);
                return false;
            }

            if (autozoilReport == null) {
                build.setResult(Result.FAILURE);
                return false;
            }

            AutozoilSourceContainer autozoilSourceContainer = new AutozoilSourceContainer(listener, build.getWorkspace(), build.getModuleRoot(), autozoilReport.getAllErrors());

            AutozoilResult result = new AutozoilResult(autozoilReport, autozoilSourceContainer, build);

            Result buildResult = new AutozoilBuildResultEvaluator().evaluateBuildResult(
                    listener, result.getNumberErrorsAccordingConfiguration(autozoilConfig, false),
                    result.getNumberErrorsAccordingConfiguration(autozoilConfig, true),
                    autozoilConfig);

            if (buildResult != Result.SUCCESS) {
                build.setResult(buildResult);
            }

            AutozoilBuildAction buildAction = new AutozoilBuildAction(build, result, autozoilConfig);
            build.addAction(buildAction);


            if (build.getWorkspace().isRemote()) {
                copyFilesFromSlaveToMaster(build.getRootDir(), launcher.getChannel(), autozoilSourceContainer.getInternalMap().values());
            }

            AutozoilLogger.log(listener, "Ending the autozoil analysis.");
        }
        return true;
    }


    /**
     * Copies all the source files from stave to master for a remote build.
     *
     * @param rootDir      directory to store the copied files in
     * @param channel      channel to get the files from
     * @param sourcesFiles the sources files to be copied
     * @throws IOException                   if the files could not be written
     * @throws java.io.FileNotFoundException if the files could not be written
     * @throws InterruptedException          if the user cancels the processing
     */
    private void copyFilesFromSlaveToMaster(final File rootDir,
                                            final VirtualChannel channel, final Collection<AutozoilWorkspaceFile> sourcesFiles)
            throws IOException, InterruptedException {

        File directory = new File(rootDir, AutozoilWorkspaceFile.WORKSPACE_FILES);
        if (!directory.exists()) {

            if (!directory.delete()) {
                //do nothing
            }

            if (!directory.mkdir()) {
                throw new IOException("Can't create directory for remote source files: " + directory.getAbsolutePath());
            }
        }

        for (AutozoilWorkspaceFile file : sourcesFiles) {
            if (!file.isSourceIgnored()) {
                File masterFile = new File(directory, file.getTempName());
                if (!masterFile.exists()) {
                    FileOutputStream outputStream = new FileOutputStream(masterFile);
                    new FilePath(channel, file.getFileName()).copyTo(outputStream);
                }
            }
        }
    }

    @Extension
    public static final class AutozoilDescriptor extends BuildStepDescriptor<Publisher> {

        public AutozoilDescriptor() {
            super(AutozoilPublisher.class);
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            boolean isIvyProject = false;
            if (Hudson.getInstance().getPlugin("ivy") != null) {
                isIvyProject = hudson.ivy.AbstractIvyProject.class.isAssignableFrom(jobType);
            }

            return FreeStyleProject.class.isAssignableFrom(jobType)
                    || MatrixProject.class.isAssignableFrom(jobType)
                    || isIvyProject;
        }

        @Override
        public String getDisplayName() {
            return "Publish Autozoil results";
        }

        @Override
        public final String getHelpFile() {
            return getPluginRoot() + "help.html";
        }

        public String getPluginRoot() {
            return "/plugin/autozoil/";
        }

        @SuppressWarnings("unused")
        public AutozoilConfig getConfig() {
            return new AutozoilConfig();
        }

//        @Override
//        public Publisher newInstance(StaplerRequest req, JSONObject formData)
//                throws hudson.model.Descriptor.FormException {
//
//            AutozoilPublisher pub = new AutozoilPublisher();
//            AutozoilConfig autozoilConfig = req.bindJSON(AutozoilConfig.class, formData);
//            pub.setAutozoilConfig(autozoilConfig);
//
//            return pub;
//        }
    }

//
//    public void setAutozoilConfig(AutozoilConfig autozoilConfig) {
//        this.autozoilConfig = autozoilConfig;
//    }


}
