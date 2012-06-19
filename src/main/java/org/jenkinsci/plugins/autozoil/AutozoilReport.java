package org.jenkinsci.plugins.autozoil;

import org.jenkinsci.plugins.autozoil.model.AutozoilFile;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Gregory Boissinot
 */
@ExportedBean
public class AutozoilReport implements Serializable {

    private String version;

    private transient List<AutozoilFile> allErrors = new ArrayList<AutozoilFile>();
    private transient Set<String> versions = new HashSet<String>();

    private List<AutozoilFile> errorTypeList = new ArrayList<AutozoilFile>();
    private List<AutozoilFile> spellTypeList = new ArrayList<AutozoilFile>();
    private List<AutozoilFile> grammarTypeList = new ArrayList<AutozoilFile>();
    private List<AutozoilFile> typoTypeList = new ArrayList<AutozoilFile>();
    private List<AutozoilFile> suppressorTypeList = new ArrayList<AutozoilFile>();
    private List<AutozoilFile> noCategoryTypeList = new ArrayList<AutozoilFile>();

    public String getVersion() {
        return version;
    }

    public Set<String> getVersions() {
        return versions;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<AutozoilFile> getAllErrors() {
        return allErrors;
    }

    public void setAllErrors(List<AutozoilFile> allErrors) {
        this.allErrors = allErrors;
    }

    public List<AutozoilFile> getErrorTypeList() {
        return errorTypeList;
    }

    public void setErrorTypeList(List<AutozoilFile> errorTypeList) {
        this.errorTypeList = errorTypeList;
    }

    public List<AutozoilFile> getSpellTypeList() {
        return spellTypeList;
    }

    public void setSpellTypeList(List<AutozoilFile> spellTypeList) {
        this.spellTypeList = spellTypeList;
    }

    public List<AutozoilFile> getGrammarTypeList() {
        return grammarTypeList;
    }

    public void setGrammarTypeList(List<AutozoilFile> grammarTypeList) {
        this.grammarTypeList = grammarTypeList;
    }

    public List<AutozoilFile> getTypoTypeList() {
        return typoTypeList;
    }

    public void setTypoTypeList(List<AutozoilFile> typoTypeList) {
        this.typoTypeList = typoTypeList;
    }

    public List<AutozoilFile> getSuppressorTypeList() {
        return suppressorTypeList;
    }

    public void setSuppressorTypeList(List<AutozoilFile> suppressorTypeList) {
        this.suppressorTypeList = suppressorTypeList;
    }

    public List<AutozoilFile> getNoCategoryTypeList() {
        return noCategoryTypeList;
    }

    public void setNoCategoryTypeList(List<AutozoilFile> noCategoryTypeList) {
        this.noCategoryTypeList = noCategoryTypeList;
    }

    @Exported
    @SuppressWarnings("unused")
    public int getNumberTotal() {
        return (allErrors == null) ? 0 : allErrors.size();
    }

    @Exported
    @SuppressWarnings("unused")
    public int getNumberErrorType() {
        return (errorTypeList == null) ? 0 : errorTypeList.size();
    }

    @Exported
    @SuppressWarnings("unused")
    public int getNumberSpellType() {
        return (spellTypeList == null) ? 0 : spellTypeList.size();
    }

    @Exported
    @SuppressWarnings("unused")
    public int getNumberGrammarType() {
        return (grammarTypeList == null) ? 0 : grammarTypeList.size();
    }

    @Exported
    @SuppressWarnings("unused")
    public int getNumberTypoType() {
        return (typoTypeList == null) ? 0 : typoTypeList.size();
    }

    @Exported
    @SuppressWarnings("unused")
    public int getNumberSuppressorType() {
        return (suppressorTypeList == null) ? 0 : suppressorTypeList.size();
    }

    @Exported
    @SuppressWarnings("unused")
    public int getNumberNoCategoryType() {
        return (noCategoryTypeList == null) ? 0 : noCategoryTypeList.size();
    }


    @SuppressWarnings("unused")
    private Object readResolve() {
        this.allErrors = new ArrayList<AutozoilFile>();
        this.allErrors.addAll(errorTypeList);
        this.allErrors.addAll(spellTypeList);
        this.allErrors.addAll(grammarTypeList);
        this.allErrors.addAll(typoTypeList);
        this.allErrors.addAll(suppressorTypeList);
        this.allErrors.addAll(noCategoryTypeList);
        return this;
    }
}
