package org.jenkinsci.plugins.autozoil.config;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class AutozoilConfigGraph implements Serializable {

    public static final int DEFAULT_CHART_WIDTH = 500;
    public static final int DEFAULT_CHART_HEIGHT = 200;

    private int xSize = DEFAULT_CHART_WIDTH;
    private int ySize = DEFAULT_CHART_HEIGHT;
    private boolean displayAllErrors;
    private boolean displayErrorType;
    private boolean displaySpellType;
    private boolean displayGrammarType;
    private boolean displayTypoType;
    private boolean displaySuppressorType;

    public AutozoilConfigGraph() {
    }

    public AutozoilConfigGraph(int xSize, int ySize, boolean displayAllErrors, boolean displayErrorType, boolean displaySpellType, boolean displayGrammarType, boolean displayTypoType, boolean displaySuppressorType) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.displayAllErrors = displayAllErrors;
        this.displayErrorType = displayErrorType;
        this.displaySpellType = displaySpellType;
        this.displayGrammarType = displayGrammarType;
        this.displayTypoType = displayTypoType;
        this.displaySuppressorType = displaySuppressorType;
    }

    public int getXSize() {
        return xSize;
    }

    public int getYSize() {
        return ySize;
    }

    public boolean isDisplayAllErrors() {
        return displayAllErrors;
    }

    public boolean isDisplayErrorType() {
        return displayErrorType;
    }

    public boolean isDisplaySpellType() {
        return displaySpellType;
    }

    public boolean isDisplayGrammarType() {
        return displayGrammarType;
    }

    public boolean isDisplayTypoType() {
        return displayTypoType;
    }

    public boolean isDisplaySuppressorType() {
        return displaySuppressorType;
    }
}
