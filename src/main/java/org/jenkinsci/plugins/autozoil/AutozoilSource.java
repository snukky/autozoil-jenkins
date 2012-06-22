/*******************************************************************************
 * Copyright (c) 2009 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot                                                   *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/

package org.jenkinsci.plugins.autozoil;

import org.jenkinsci.plugins.autozoil.model.AutozoilFile;
import org.jenkinsci.plugins.autozoil.model.AutozoilWorkspaceFile;
import de.java2html.converter.JavaSource2HTMLConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.JavaSourceConversionOptions;
import hudson.model.AbstractBuild;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.List;
import java.util.HashMap;


public class AutozoilSource implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Offset of the source code generator. After this line the actual source file lines start.
     */
    protected static final int SOURCE_GENERATOR_OFFSET = 13;

    /**
     * Color for the first (primary) annotation range.
     */
    private static final String MESSAGE_COLOR = "#FF0000";

    /**
     * Secondary color for the rest of highlighted lines.
     */
    private static final String MESSAGE_SECONDARY_COLOR = "#FCAF3E";

    /**
     * The current build as owner of this object.
     */
    private final AbstractBuild<?, ?> owner;

    /**
     * The autozoil source file in the workspace to be shown.
     */
    private final AutozoilWorkspaceFile autozoilWorkspaceFile;

    private final HashMap<Integer, List<AutozoilFile> > lineNumberToAutozoilFilesMap;

    /**
     * The rendered source file.
     */
    private String sourceCode = StringUtils.EMPTY;

    /**
     * Creates a new instance of this source code object.
     *
     * @param owner                 the current build as owner of this object
     * @param autozoilWorkspaceFile the abstract workspace file
     */
    public AutozoilSource(final AbstractBuild<?, ?> owner, AutozoilWorkspaceFile autozoilWorkspaceFile) {
        this.owner = owner;
        this.autozoilWorkspaceFile = autozoilWorkspaceFile;
        this.lineNumberToAutozoilFilesMap = null;
        buildFileContent();
    }

    public AutozoilSource(final AbstractBuild<?, ?> owner, AutozoilWorkspaceFile autozoilWorkspaceFile, 
        HashMap<Integer, List<AutozoilFile> > lineNumberToAutozoilFilesMap) {

        this.owner = owner;
        this.autozoilWorkspaceFile = autozoilWorkspaceFile;
        this.lineNumberToAutozoilFilesMap = lineNumberToAutozoilFilesMap;

        buildFileContent();
    }

    /**
     * Builds the file content.
     */
    private void buildFileContent() {
        InputStream is = null;
        try {

            File tempFile = new File(autozoilWorkspaceFile.getTempName(owner));
            if (tempFile.exists()) {
                is = new FileInputStream(tempFile);
            } else {

                if (autozoilWorkspaceFile.getFileName() == null) {
                    throw new IOException("The file doesn't exist.");
                }

                File file = new File(autozoilWorkspaceFile.getFileName());
                if (!file.exists()) {
                    throw new IOException("Can't access the file: " + file.toURI());
                }
                is = new FileInputStream(file);
            }

            splitSourceFile(highlightSource(is));
        } catch (IOException exception) {
            sourceCode = "Can't read file: " + exception.getLocalizedMessage();
        } catch (RuntimeException re) {
            sourceCode = "Problem for display the source code content: " + re.getLocalizedMessage();
        } finally {
            IOUtils.closeQuietly(is);
        }
    }


    /**
     * Splits the source code into three blocks: the line to highlight and the
     * source code before and after this line.
     *
     * @param sourceFile the source code of the whole file as rendered HTML string
     */
    private void splitSourceFile(final String sourceFile) {
        StringBuilder output = new StringBuilder(sourceFile.length());

        AutozoilFile baseAutozoilFile = autozoilWorkspaceFile.getAutozoilFile();
        LineIterator lineIterator = IOUtils.lineIterator(new StringReader(sourceFile));
        int lineNumber = 1;

        //---header
        while (lineNumber < SOURCE_GENERATOR_OFFSET) {
            copyLine(output, lineIterator);
            lineNumber++;
        }
        lineNumber = 1;

        //---iterate before the error line
        //while (lineNumber < autozoilFile.getLineNumber()) {
        while (lineIterator.hasNext()) {

            if (lineNumberToAutozoilFilesMap.keySet().contains(lineNumber)) {
                output.append("</code>\n");
                output.append("</td></tr>\n");
                output.append("<tr><td bgcolor=\"");

                if (lineNumber == baseAutozoilFile.getLineNumber()) {
                    appendRangeColor(output);
                }
                else {
                    appendRangeSecondaryColor(output);
                }
                output.append("\">\n");
                
                output.append("<div tooltip=\"");
                outputEscaped(output, conciseHintFromAutozoilFiles(lineNumberToAutozoilFilesMap.get(lineNumber)));
                output.append("\" nodismiss=\"\">\n");
                output.append("<code><b>\n");

                copyLine(output, lineIterator);
                lineNumber++;

                output.append("</b></code>\n");
                output.append("</div>\n");
                output.append("</td></tr>\n");

                output.append("<tr><td>\n");
                output.append("<code>\n");

                continue;
            }

            copyLine(output, lineIterator);
            lineNumber++;
        }
        output.append("</code>\n");
        output.append("</td></tr>\n");

        sourceCode = output.toString();
    }


    private String conciseHintFromAutozoilFiles(List<AutozoilFile> autozoilFiles) {
        String hint = "";

        for (AutozoilFile autozoilFile : autozoilFiles) {
            hint += autozoilFile.getType() + " => " + autozoilFile.getAutoZoilId() + "<br/>";

            if (autozoilFile.getContext() != null) {
                hint += "&nbsp;&nbsp; context : ";
                hint += autozoilFile.getContext();
                hint += "<br/>";
            }
            if (autozoilFile.getCorrection() != null) {
                hint += "&nbsp;&nbsp; correction : ";
                hint += autozoilFile.getCorrection();
                hint += "<br/>";
            }
            if (autozoilFile.getMessage() != null) {
                hint += "&nbsp;&nbsp; message : ";
                hint += autozoilFile.getMessage();
                hint += "<br/>";
            }
        }

        return hint;
    }

    /**
     * Writes the message to the output stream (with escaped HTML).
     *
     * @param output  the output to write to
     * @param message the message to write
     */
    private void outputEscaped(final StringBuilder output, final String message) {
        output.append(StringEscapeUtils.escapeHtml(message));
    }

    /**
     * Appends the message color.
     *
     * @param output the output to append the color
     */
    private void appendRangeColor(final StringBuilder output) {
        output.append(MESSAGE_COLOR);
    }
  
    private void appendRangeSecondaryColor(final StringBuilder output) {
        output.append(MESSAGE_SECONDARY_COLOR);
    }

    /**
     * Copies the next line of the input to the output.
     *
     * @param output       output
     * @param lineIterator input
     */
    private void copyLine(final StringBuilder output, final LineIterator lineIterator) {
        output.append(lineIterator.nextLine());
        output.append("\n");
    }

    /**
     * Highlights the specified source and returns the result as an HTML string.
     *
     * @param file the source file to highlight
     * @return the source as an HTML string
     * @throws IOException
     */
    public final String highlightSource(final InputStream file) throws IOException {

        JavaSource source = new JavaSourceParser().parse(new InputStreamReader(file));
        JavaSource2HTMLConverter converter = new JavaSource2HTMLConverter();
        StringWriter writer = new StringWriter();
        JavaSourceConversionOptions options = JavaSourceConversionOptions.getDefault();
        options.setShowLineNumbers(true);
        options.setAddLineAnchors(true);
        converter.convert(source, options, writer);
        return writer.toString();
    }


    /**
     * Retrieve the source code for the autozoil source file.
     *
     * @return the source code content as a String object
     */
    @SuppressWarnings("unused")
    public String getSourceCode() {
        return sourceCode;
    }

    /**
     * Returns the abstract Autozoil workspace file.
     *
     * @return the workspace file
     */
    @SuppressWarnings("unused")
    public AutozoilWorkspaceFile getAutozoilWorkspaceFile() {
        return autozoilWorkspaceFile;
    }
}

