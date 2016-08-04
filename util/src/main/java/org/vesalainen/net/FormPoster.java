/*
 * Copyright (C) 2012 Timo Vesalainen
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

package org.vesalainen.net;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.vesalainen.util.MimeTypes;

/**
 * @author Timo Vesalainen
 */
public class FormPoster
{
    private static final String CRLF = "\r\n";
    private final Map<String,String> formData = new HashMap<>();
    private final List<File> fileList = new ArrayList<>();
    private final URL action;

    public FormPoster(URL action)
    {
        this.action = action;
    }
    
    public void post() throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) action.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        String uid = UUID.randomUUID().toString();
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary="+uid);
        try (PrintStream ps = new PrintStream(connection.getOutputStream(), false, "utf-8"))
        {
            for (String name : formData.keySet())
            {
                String value = formData.get(name);
                ps.append("--"+uid);
                ps.append(CRLF);
                ps.append("Content-Disposition: form-data; name=\""+name+"\"");
                ps.append(CRLF);
                ps.append("Content-Type: text/plain; charset=utf-8");
                ps.append(CRLF);
                ps.append(CRLF);
                ps.append(value);
                ps.append(CRLF);
            }
            byte[] buffer = new byte[8192];
            for (File file : fileList)
            {
                String name = file.getName();
                ps.append("--"+uid);
                ps.append(CRLF);
                ps.append("Content-Disposition: form-data; name=\""+name+"\"; filename=\""+name+"\"");
                ps.append(CRLF);
                ps.append("Content-Type: "+MimeTypes.getMimeType(file));
                ps.append(CRLF);
                ps.append("Content-Transfer-Encoding: binary");
                ps.append(CRLF);
                ps.append(CRLF);
                FileInputStream fis = new FileInputStream(file);
                int rc = fis.read(buffer);
                while (rc != -1)
                {
                    ps.write(buffer, 0, rc);
                    rc = fis.read(buffer);
                }
                fis.close();
                ps.append(CRLF);
            }
            ps.append("--"+uid+"--");
            ps.append(CRLF);
        }
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
        {
            throw new IOException(connection.getResponseCode()+" "+connection.getResponseMessage());
        }
    }
    public void setFormData(String name, String value)
    {
        formData.put(name, value);
    }
    public void addFiles(File... files)
    {
        for (File file : files)
        {
            fileList.add(file);
        }
    }

}
