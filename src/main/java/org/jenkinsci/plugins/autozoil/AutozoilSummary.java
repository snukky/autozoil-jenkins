package org.jenkinsci.plugins.autozoil;


import org.jenkinsci.plugins.autozoil.Messages;

/**
 * @author Gregory Boissinot
 */
public class AutozoilSummary {

    private AutozoilSummary() {
    }


    /**
     * Creates an HTML Autozoil summary.
     *
     * @param result the autozoil result object
     * @return the HTML fragment representing the autozoil report summary
     */
    public static String createReportSummary(AutozoilResult result) {

        StringBuilder summary = new StringBuilder();
        int nbErrors = result.getReport().getAllErrors().size();

        summary.append(Messages.autozoil_Errors_ProjectAction_Name());
        summary.append(": ");
        if (nbErrors == 0) {
            summary.append(Messages.autozoil_ResultAction_NoError());
        } else {
            summary.append("<a href=\"" + AutozoilBuildAction.URL_NAME + "\">");

            if (nbErrors == 1) {
                summary.append(Messages.autozoil_ResultAction_OneError());
            } else {
                summary.append(Messages.autozoil_ResultAction_MultipleErrors(nbErrors));
            }
            summary.append("</a>");
        }
        summary.append(".");

        return summary.toString();
    }


    /**
     * Creates an HTML Autozoil detailed summary.
     *
     * @param autozoilResult the autozoil result object
     * @return the HTML fragment representing the autozoil report details summary
     */
    public static String createReportSummaryDetails(AutozoilResult autozoilResult) {

        if (autozoilResult == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        AutozoilResult previousAutozoilResult = autozoilResult.getPreviousResult();

        AutozoilReport autozoilReport = autozoilResult.getReport();
        AutozoilReport previousAutozoilReport = previousAutozoilResult.getReport();

        builder.append("<li>");
        int nbNewErrorType = getNbNew(autozoilReport.getNumberErrorType(), previousAutozoilReport.getNumberErrorType());
        builder.append(String.format("Type Latex (nb/new) : %s/+%s", autozoilReport.getNumberErrorType(), nbNewErrorType));
        builder.append("</li>");

        builder.append("<li>");
        int nbNewSpellType = getNbNew(autozoilReport.getNumberSpellType(), previousAutozoilReport.getNumberSpellType());
        builder.append(String.format("Type Spell (nb/new) : %s/+%s", autozoilReport.getNumberSpellType(), nbNewSpellType));
        builder.append("</li>");

        builder.append("<li>");
        int nbNewGrammarType = getNbNew(autozoilReport.getNumberGrammarType(), previousAutozoilReport.getNumberGrammarType());
        builder.append(String.format("Type Grammar (nb/new) : %s/+%s", autozoilReport.getNumberGrammarType(), nbNewGrammarType));
        builder.append("</li>");

        builder.append("<li>");
        int nbNewTypoType = getNbNew(autozoilReport.getNumberTypoType(), previousAutozoilReport.getNumberTypoType());
        builder.append(String.format("Type Typo (nb/new) : %s/+%s", autozoilReport.getNumberTypoType(), nbNewTypoType));
        builder.append("</li>");

        builder.append("<li>");
        int nbNewSuppressorType = getNbNew(autozoilReport.getNumberSuppressorType(), previousAutozoilReport.getNumberSuppressorType());
        builder.append(String.format("Type Suppressor (nb/new) : %s/+%s", autozoilReport.getNumberSuppressorType(), nbNewSuppressorType));
        builder.append("</li>");

        builder.append("<li>");
        int nbNewNoCategoryType = getNbNew(autozoilReport.getNumberNoCategoryType(), previousAutozoilReport.getNumberNoCategoryType());
        builder.append(String.format("Type Unknown (nb/new) : %s/%s", autozoilReport.getNumberNoCategoryType(), nbNewNoCategoryType));
        builder.append("</li>");

        return builder.toString();
    }

    private static int getNbNew(int numberError, int numberPreviousError) {
        int diff = numberError - numberPreviousError;
        return (diff > 0) ? diff : 0;
    }

}
