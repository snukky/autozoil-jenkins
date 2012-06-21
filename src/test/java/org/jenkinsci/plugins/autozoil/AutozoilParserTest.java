package org.jenkinsci.plugins.autozoil;

import org.jenkinsci.plugins.autozoil.AutozoilReport;
import org.jenkinsci.plugins.autozoil.model.AutozoilFile;
import org.jenkinsci.plugins.autozoil.parser.AutozoilParser;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * @author Gregory Boissinot
 */
public class AutozoilParserTest {


    AutozoilParser autozoilParser;

    @Before
    public void setUp() throws Exception {
        autozoilParser = new AutozoilParser();
    }

    @Test
    public void nullFile() throws Exception {
        try {
            autozoilParser.parse(null);
            Assert.fail("null parameter is not allowed.");
        } catch (IllegalArgumentException iea) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void nonExistFile() throws Exception {
        try {
            autozoilParser.parse(new File("nonExistFile"));
            Assert.fail("A valid file is mandatory.");
        } catch (IllegalArgumentException iea) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testautozoil1Version2() throws Exception {
        processAutozoil("version2/testAutozoil.xml", 16, 2, 0, 1, 13, 0, 0);
    }

    private void processAutozoil(String filename,
                                 int nbAllErrors,
                                 int nbTypeLatex,
                                 int nbTypeGrammar,
                                 int nbTypeSpell,
                                 int nbTypeTypo,
                                 int nbTypeSuppressor,
                                 int nbTypeNoCategory) throws Exception {

        AutozoilReport autozoilReport = autozoilParser.parse(new File(this.getClass().getResource(filename).toURI()));

        List<AutozoilFile> allErrors = autozoilReport.getAllErrors();
        List<AutozoilFile> latexes = autozoilReport.getErrorTypeList();
        List<AutozoilFile> spells = autozoilReport.getSpellTypeList();
        List<AutozoilFile> grammars = autozoilReport.getGrammarTypeList();
        List<AutozoilFile> typos = autozoilReport.getTypoTypeList();
        List<AutozoilFile> suppressors = autozoilReport.getSuppressorTypeList();
        List<AutozoilFile> noCategories = autozoilReport.getNoCategoryTypeList();

        assert allErrors != null;
        assert latexes != null;
        assert spells != null;
        assert grammars != null;
        assert typos != null;
        assert suppressors != null;
        assert noCategories != null;

        Assert.assertEquals("Wrong computing of list of errors", allErrors.size(),
                noCategories.size() + suppressors.size() + typos.size() + grammars.size() + spells.size() + latexes.size());

        Assert.assertEquals("Wrong total number of errors", nbAllErrors, allErrors.size());
        Assert.assertEquals("Wrong total number of errors for the type 'latex'", nbTypeLatex, latexes.size());
        Assert.assertEquals("Wrong total number of errors for the type 'spell'", nbTypeSpell, spells.size());
        Assert.assertEquals("Wrong total number of errors for the type 'grammar'", nbTypeGrammar, grammars.size());
        Assert.assertEquals("Wrong total number of errors for the type 'typo'", nbTypeTypo, typos.size());
        Assert.assertEquals("Wrong total number of errors for the type 'suppressor'", nbTypeSuppressor, suppressors.size());
        Assert.assertEquals("Wrong total number of errors with no category", nbTypeNoCategory, noCategories.size());
    }

}
