package org.jenkinsci.plugins.autozoil.util;

import org.jenkinsci.plugins.autozoil.util.Messages;
import hudson.model.HealthReport;
import org.jenkinsci.plugins.autozoil.config.AutozoilConfig;

/**
 * @author Gregory Boissinot
 */
public class AutozoilBuildHealthEvaluator {

    public HealthReport evaluatBuildHealth(AutozoilConfig autozoilConfig, int nbErrorForType) {

        if (autozoilConfig == null) {
            // no thresholds => no report
            return null;
        }

        if (isHealthyReportEnabled(autozoilConfig)) {
            int percentage;

            if (nbErrorForType < AutozoilMetricUtil.convert(autozoilConfig.getConfigTypeEvaluation().getHealthy())) {
                percentage = 100;
            } else if (nbErrorForType > AutozoilMetricUtil.convert(autozoilConfig.getConfigTypeEvaluation().getUnHealthy())) {
                percentage = 0;
            } else {
                percentage = 100 - ((nbErrorForType - AutozoilMetricUtil.convert(autozoilConfig.getConfigTypeEvaluation().getHealthy())) * 100
                        / (AutozoilMetricUtil.convert(autozoilConfig.getConfigTypeEvaluation().getUnHealthy()) - AutozoilMetricUtil.convert(autozoilConfig.getConfigTypeEvaluation().getHealthy())));
            }

            return new HealthReport(percentage, Messages._AutozoilBuildHealthEvaluator_Description(AutozoilMetricUtil.getMessageSelectedSeverties(autozoilConfig)));
        }
        return null;
    }


    private boolean isHealthyReportEnabled(AutozoilConfig autozoilconfig) {
        if (AutozoilMetricUtil.isValid(autozoilconfig.getConfigTypeEvaluation().getHealthy()) && AutozoilMetricUtil.isValid(autozoilconfig.getConfigTypeEvaluation().getUnHealthy())) {
            int healthyNumber = AutozoilMetricUtil.convert(autozoilconfig.getConfigTypeEvaluation().getHealthy());
            int unHealthyNumber = AutozoilMetricUtil.convert(autozoilconfig.getConfigTypeEvaluation().getUnHealthy());
            return unHealthyNumber > healthyNumber;
        }
        return false;
    }
}
