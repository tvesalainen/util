/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.ham.pdf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RfaxTest
{

    public RfaxTest()
    {
    }

    //@Test dont run again!!!
    public void test() throws IOException
    {
        PDDocument document = PDDocument.load(new File("rfax.pdf"));
        if (!document.isEncrypted())
        {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get("src", "main", "resources", "rfax.txt")))
            {
                bw.write(text);
            }
        }
        document.close();
    }

}
