/*
 *  Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 *  Programmed by Naohide Sano
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfViewerPreferences;
import com.itextpdf.kernel.pdf.PdfWriter;


/**
 * PdfRtoL.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2018/01/07 umjammer initial version <br>
 */
public class PdfRtoL {

    public static void main(String[] args) throws Exception {
        File inFile = new File(args[0]);
        File outFile = new File(args[1]);

        PdfReader reader = new PdfReader(new FileInputStream(inFile));
        PdfWriter writer = new PdfWriter(new FileOutputStream(outFile));
        PdfDocument pdf = new PdfDocument(reader, writer);
        PdfViewerPreferences prefs = new PdfViewerPreferences();
        prefs.setDirection(com.itextpdf.kernel.pdf.PdfViewerPreferences.PdfViewerPreferencesConstants.RIGHT_TO_LEFT);
        pdf.getCatalog().setViewerPreferences(prefs);
        pdf.close();
        writer.close();
        reader.close();
    }
}
