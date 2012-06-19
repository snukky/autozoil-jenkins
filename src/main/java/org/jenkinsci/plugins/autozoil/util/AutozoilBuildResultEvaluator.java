package org.jenkinsci.plugins.autozoil.util;

import org.jenkinsci.plugins.autozoil.util.AutozoilLogger;
import hudson.model.BuildListener;
import hudson.model.Result;
import org.jenkinsci.plugins.autozoil.config.AutozoilConfig;

/**
 * @author Gregory Boissinot
 */
public class AutozoilBuildResultEvaluator {


    public Result evaluateBuildResult(
            final BuildListener listener,
            int errorsCount,
            int newErrorsCount,
            AutozoilConfig autozoilConfig) {

        if (isErrorCountExceeded(errorsCount, autozoilConfig.getConfigTypeEvaluation().getFailureThreshold())) {
            AutozoilLogger.log(listener, "Setting build status to FAILURE since total number of errors ("
                    + AutozoilMetricUtil.getMessageSelectedSeverties(autozoilConfig)
                    + ") exceeds the threshold value '" + autozoilConfig.getConfigTypeEvaluation().getFailureThreshold() + "'.");
            return Result.FAILURE;
        }
        if (isErrorCountExceeded(newErrorsCount, autozoilConfig.getConfigTypeEvaluation().getNewFailureThreshold())) {
            AutozoilLogger.log(listener, "Setting build status to FAILURE since total number of new errors ("
                    + AutozoilMetricUtil.getMessageSelectedSeverties(autozoilConfig)
                    + ") exceeds the threshold value '" + autozoilConfig.getConfigTypeEvaluation().getNewFailureThreshold() + "'.");
            return Result.FAILURE;
        }
        if (isErrorCountExceeded(errorsCount, autozoilConfig.getConfigTypeEvaluation().getThreshold())) {
            AutozoilLogger.log(listener, "Setting build status to UNSTABLE since total number of errors ("
                    + AutozoilMetricUtil.getMessageSelectedSeverties(autozoilConfig)
                    + ") exceeds the threshold value '" + autozoilConfig.getConfigTypeEvaluation().getThreshold() + "'.");
            return Result.UNSTABLE;
        }
        if (isErrorCountExceeded(newErrorsCount, autozoilConfig.getConfigTypeEvaluation().getNewThreshold())) {
            AutozoilLogger.log(listener, "Setting build status to UNSTABLE since total number of new errors ("
                    + AutozoilMetricUtil.getMessageSelectedSeverties(autozoilConfig)
                    + ") exceeds the threshold value '" + autozoilConfig.getConfigTypeEvaluation().getNewThreshold() + "'.");
            return Result.UNSTABLE;
        }

        AutozoilLogger.log(listener, "Not changing build status, since no threshold has been exceeded");
        return Result.SUCCESS;
    }

    private boolean isErrorCountExceeded(final int errorCount, final String errorThreshold) {
        if (errorCount > 0 && AutozoilMetricUtil.isValid(errorThreshold)) {
            return errorCount > AutozoilMetricUtil.convert(errorThreshold);
        }
        return false;
    }
}
