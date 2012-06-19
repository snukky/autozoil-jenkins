package org.jenkinsci.plugins.autozoil.util;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.autozoil.config.AutozoilConfig;

/**
 * @author Gregory Boissinot
 */
public class AutozoilMetricUtil {

    public static int convert(String threshold) {
        if (isValid(threshold)) {
            if (StringUtils.isNotBlank(threshold)) {
                try {
                    return Integer.valueOf(threshold);
                } catch (NumberFormatException exception) {
                    // not valid
                }
            }
        }
        throw new IllegalArgumentException("Not a parsable integer value >= 0: " + threshold);
    }

    public static boolean isValid(final String threshold) {
        if (StringUtils.isNotBlank(threshold)) {
            try {
                return Integer.valueOf(threshold) >= 0;
            } catch (NumberFormatException exception) {
                // not valid
            }
        }
        return false;
    }


    private static boolean isAllTypes(AutozoilConfig autozoilConfig) {
        return autozoilConfig.getConfigTypeEvaluation().isTypeError()
                && autozoilConfig.getConfigTypeEvaluation().isTypeSpell()
                && autozoilConfig.getConfigTypeEvaluation().isTypeGrammar()
                && autozoilConfig.getConfigTypeEvaluation().isTypeTypo()
                && autozoilConfig.getConfigTypeEvaluation().isTypeSuppressor();
    }


    public static String getMessageSelectedSeverties(AutozoilConfig autozoilConfig) {
        StringBuffer sb = new StringBuffer();

        if (isAllTypes(autozoilConfig)) {
            sb.append("With all type values");
            return sb.toString();
        }

        if (autozoilConfig.getConfigTypeEvaluation().isTypeError()) {
            sb.append(" and ");
            sb.append("type 'error'");
        }

        if (autozoilConfig.getConfigTypeEvaluation().isTypeSpell()) {
            sb.append(" and ");
            sb.append("type 'spell'");
        }

        if (autozoilConfig.getConfigTypeEvaluation().isTypeGrammar()) {
            sb.append(" and ");
            sb.append("type 'grammar'");
        }

        if (autozoilConfig.getConfigTypeEvaluation().isTypeTypo()) {
            sb.append(" and ");
            sb.append("type 'typo'");
        }


        if (autozoilConfig.getConfigTypeEvaluation().isTypeSuppressor()) {
            sb.append(" and ");
            sb.append("type 'suppressor'");
        }

        if (sb.length() != 0)
            sb.delete(0, 5);

        return sb.toString();
    }

}
