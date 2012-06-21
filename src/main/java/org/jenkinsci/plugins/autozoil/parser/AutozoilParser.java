package org.jenkinsci.plugins.autozoil.parser;

import org.jenkinsci.plugins.autozoil.model.AutozoilFile;
import org.jenkinsci.plugins.autozoil.AutozoilReport;
import org.jenkinsci.plugins.autozoil.model.Errors;
import org.jenkinsci.plugins.autozoil.model.Results;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Gregory Boissinot
 */
public class AutozoilParser implements Serializable {

    private static final long serialVersionUID = 1L;

    public AutozoilReport parse(final File file) throws IOException {

        if (file == null) {
            throw new IllegalArgumentException("File input is mandatory.");
        }

        if (!file.exists()) {
            throw new IllegalArgumentException("File input " + file.getName() + " must exist.");
        }

        AutozoilReport report;
        AtomicReference<JAXBContext> jc = new AtomicReference<JAXBContext>();
        try {
            jc.set(JAXBContext.newInstance(
                    org.jenkinsci.plugins.autozoil.model.Error.class,
                    org.jenkinsci.plugins.autozoil.model.Errors.class,
                    org.jenkinsci.plugins.autozoil.model.Autozoil.class,
                    org.jenkinsci.plugins.autozoil.model.Results.class));
            Unmarshaller unmarshaller = jc.get().createUnmarshaller();
            org.jenkinsci.plugins.autozoil.model.Results results = (org.jenkinsci.plugins.autozoil.model.Results) unmarshaller.unmarshal(file);
            if (results.getAutozoil() == null) {
                throw new JAXBException("Test with versio 1");
            }
            report = getReportVersion2(results);
        } catch (JAXBException jxe) {
            throw new IOException(jxe);
        }
        return report;
    }

    private AutozoilReport getReportVersion2(Results results) {

        AutozoilReport autoZoilReport = new AutozoilReport();
        List<AutozoilFile> allErrors = new ArrayList<AutozoilFile>();
        List<AutozoilFile> errorTypeList = new ArrayList<AutozoilFile>();
        List<AutozoilFile> spellTypeList = new ArrayList<AutozoilFile>();
        List<AutozoilFile> grammarTypeList = new ArrayList<AutozoilFile>();
        List<AutozoilFile> typoTypeList = new ArrayList<AutozoilFile>();
        List<AutozoilFile> suppressorTypeList = new ArrayList<AutozoilFile>();
        List<AutozoilFile> noCategoryTypeList = new ArrayList<AutozoilFile>();

        AutozoilFile autozoilFile;

        Errors errors = results.getErrors();

        if (errors != null) {
            for (int i = 0; i < errors.getError().size(); i++) {
                org.jenkinsci.plugins.autozoil.model.Error error = errors.getError().get(i);
                autozoilFile = new AutozoilFile();

                autozoilFile.setAutoZoilId(error.getId());
                autozoilFile.setType(error.getType());
                autozoilFile.setMessage(error.getMsg());
                autozoilFile.setError(error.getError());
                autozoilFile.setCorrection(error.getCorrection());

                if ("spell".equals(autozoilFile.getType())) {
                    spellTypeList.add(autozoilFile);
                } else if ("grammar".equals(autozoilFile.getType())) {
                    grammarTypeList.add(autozoilFile);
                } else if ("typo".equals(autozoilFile.getType())) {
                    typoTypeList.add(autozoilFile);
                } else if ("latex".equals(autozoilFile.getType())) {
                    errorTypeList.add(autozoilFile);
                } else if ("suppressor".equals(autozoilFile.getType())) {
                    suppressorTypeList.add(autozoilFile);
                } else {
                    noCategoryTypeList.add(autozoilFile);
                }
                allErrors.add(autozoilFile);

                //FileName and Line
                org.jenkinsci.plugins.autozoil.model.Error.Location location = error.getLocation();
                if (location != null) {
                    autozoilFile.setFileName(location.getFile());
                    String lineAtr;
                    if ((lineAtr = location.getLine()) != null) {
                        autozoilFile.setLineNumber(Integer.parseInt(lineAtr));
                    }
                }
            }
        }

        autoZoilReport.setAllErrors(allErrors);
        autoZoilReport.setErrorTypeList(errorTypeList);
        autoZoilReport.setSuppressorTypeList(suppressorTypeList);
        autoZoilReport.setNoCategoryTypeList(noCategoryTypeList);
        autoZoilReport.setTypoTypeList(typoTypeList);
        autoZoilReport.setGrammarTypeList(grammarTypeList);
        autoZoilReport.setSpellTypeList(spellTypeList);

        if (results.getAutozoil() != null) {
            autoZoilReport.setVersion(results.getAutozoil().getVersion());
        }

        return autoZoilReport;
    }


}

