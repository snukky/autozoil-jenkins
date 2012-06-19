package org.jenkinsci.plugins.autozoil.config;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class AutozoilConfigTypeEvaluation implements Serializable {

    private String threshold;

    private String newThreshold;

    private String failureThreshold;

    private String newFailureThreshold;

    private String healthy;

    private String unHealthy;

    private boolean typeError = true;

    private boolean typeSpell = true;

    private boolean typeGrammar = true;

    private boolean typeTypo = true;

    private boolean typeSuppressor = true;

    public AutozoilConfigTypeEvaluation() {
    }

    public AutozoilConfigTypeEvaluation(String threshold, String newThreshold, String failureThreshold, String newFailureThreshold, String healthy, String unHealthy, boolean typeError, boolean typeSpell, boolean typeGrammar, boolean typeTypo, boolean typeSuppressor) {
        this.threshold = threshold;
        this.newThreshold = newThreshold;
        this.failureThreshold = failureThreshold;
        this.newFailureThreshold = newFailureThreshold;
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.typeError = typeError;
        this.typeSpell = typeSpell;
        this.typeGrammar = typeGrammar;
        this.typeTypo = typeTypo;
        this.typeSuppressor = typeSuppressor;
    }

    public String getThreshold() {
        return threshold;
    }

    public String getNewThreshold() {
        return newThreshold;
    }

    public String getFailureThreshold() {
        return failureThreshold;
    }

    public String getNewFailureThreshold() {
        return newFailureThreshold;
    }

    public String getHealthy() {
        return healthy;
    }

    public String getUnHealthy() {
        return unHealthy;
    }

    public boolean isTypeError() {
        return typeError;
    }

    public boolean isTypeSpell() {
        return typeSpell;
    }

    public boolean isTypeGrammar() {
        return typeGrammar;
    }

    public boolean isTypeTypo() {
        return typeTypo;
    }

    public boolean isTypeSuppressor() {
        return typeSuppressor;
    }
}
