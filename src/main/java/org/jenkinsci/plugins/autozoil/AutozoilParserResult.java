package org.jenkinsci.plugins.autozoil;


import org.jenkinsci.plugins.autozoil.util.AutozoilLogger;
import hudson.FilePath;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.jenkinsci.plugins.autozoil.parser.AutozoilParser;

import java.io.File;
import java.io.IOException;

/**
 * @author Gregory Boissinot
 */
public class AutozoilParserResult implements FilePath.FileCallable<AutozoilReport> {

    private static final long serialVersionUID = 1L;

    private final BuildListener listener;

    private final String autozoilReportPattern;

    private final boolean ignoreBlankFiles;

    public static final String DELAULT_REPORT_MAVEN = "**/autozoil-result.xml";

    public AutozoilParserResult(final BuildListener listener, String autozoilReportPattern, boolean ignoreBlankFiles) {

        if (autozoilReportPattern == null) {
            autozoilReportPattern = DELAULT_REPORT_MAVEN;
        }

        if (autozoilReportPattern.trim().length() == 0) {
            autozoilReportPattern = DELAULT_REPORT_MAVEN;
        }

        this.listener = listener;
        this.autozoilReportPattern = autozoilReportPattern;
        this.ignoreBlankFiles = ignoreBlankFiles;
    }

    public AutozoilReport invoke(java.io.File basedir, VirtualChannel channel) throws IOException {

        AutozoilReport autozoilReportResult = new AutozoilReport();
        try {
            String[] autozoilReportFiles = findAutozoilReports(basedir);
            if (autozoilReportFiles.length == 0) {
                String msg = "No autozoil test report file(s) were found with the pattern '"
                        + autozoilReportPattern + "' relative to '"
                        + basedir + "'."
                        + "  Did you enter a pattern relative to the correct directory?"
                        + "  Did you generate the XML report(s) for Autozoil?";
                AutozoilLogger.log(listener, msg);
                throw new IllegalArgumentException(msg);
            }

            AutozoilLogger.log(listener, "Processing " + autozoilReportFiles.length + " files with the pattern '" + autozoilReportPattern + "'.");

            for (String cppchecReportkFileName : autozoilReportFiles) {
                AutozoilReport autozoilReport = new AutozoilParser().parse(new File(basedir, cppchecReportkFileName));
                mergeReport(autozoilReportResult, autozoilReport);
            }
        } catch (Exception e) {
            AutozoilLogger.log(listener, "Parsing throws exceptions. " + e.getMessage());
            return null;
        }

        return autozoilReportResult;
    }


    private static void mergeReport(AutozoilReport autozoilReportResult, AutozoilReport autozoilReport) {
        autozoilReportResult.getErrorTypeList().addAll(autozoilReport.getErrorTypeList());
        autozoilReportResult.getSpellTypeList().addAll(autozoilReport.getSpellTypeList());
        autozoilReportResult.getGrammarTypeList().addAll(autozoilReport.getGrammarTypeList());
        autozoilReportResult.getTypoTypeList().addAll(autozoilReport.getTypoTypeList());
        autozoilReportResult.getSuppressorTypeList().addAll(autozoilReport.getSuppressorTypeList());
        autozoilReportResult.getNoCategoryTypeList().addAll(autozoilReport.getNoCategoryTypeList());
        autozoilReportResult.getAllErrors().addAll(autozoilReport.getAllErrors());
        autozoilReportResult.getVersions().add(autozoilReport.getVersion());
    }

    /**
     * Return all autozoil report files
     *
     * @param parentPath parent
     * @return an array of strings
     */
    private String[] findAutozoilReports(File parentPath) {
        FileSet fs = Util.createFileSet(parentPath, this.autozoilReportPattern);
        if (this.ignoreBlankFiles) {
            fs.add(new FileSelector() {
                public boolean isSelected(File basedir, String filename, File file) throws BuildException {
                    return file != null && file.length() != 0;
                }
            });
        }
        DirectoryScanner ds = fs.getDirectoryScanner();
        return ds.getIncludedFiles();
    }

    @SuppressWarnings("unused")
    public String getAutozoilReportPattern() {
        return autozoilReportPattern;
    }

}
