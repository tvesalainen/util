/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class Nets
{
    /**
     * Returns Path for given url.
     * @param url
     * @return
     * @throws URISyntaxException
     * @throws MalformedURLException 
     */
    public static Path getPath(String url) throws URISyntaxException, MalformedURLException
    {
        return getPath(new URL(url));
    }
    /**
     * Returns true if given url is file: and is writable.
     * @param url
     * @return
     * @throws IOException
     * @throws URISyntaxException 
     */
    public static boolean isWritable(String url) throws IOException, URISyntaxException
    {
        return isWritable(new URL(url));
    }
    /**
     * Returns true if it is possible to read the contents of the url
     * @param url
     * @return
     * @throws MalformedURLException 
     */
    public static boolean exists(String url) throws MalformedURLException
    {
        return exists(new URL(url));
    }
    /**
     * Creates a BufferedReader for url.
     * @param url
     * @return
     * @throws IOException
     * @throws URISyntaxException 
     */
    public static BufferedReader createReader(String url) throws IOException, URISyntaxException
    {
        return createReader(new URL(url));
    }
    /**
     * Returns Path for given url.
     * @param uri
     * @return
     * @throws URISyntaxException
     * @throws MalformedURLException 
     */
    public static Path getPath(URI uri) throws URISyntaxException, MalformedURLException
    {
        return getPath(uri.toURL());
    }
    /**
     * Returns true if given url is file: and is writable.
     * @param url
     * @return
     * @throws IOException
     * @throws URISyntaxException 
     */
    public static boolean isWritable(URI uri) throws IOException, URISyntaxException
    {
        return isWritable(uri.toURL());
    }
    /**
     * Returns true if it is possible to read the contents of the url
     * @param url
     * @return
     * @throws MalformedURLException 
     */
    public static boolean exists(URI uri) throws MalformedURLException
    {
        return exists(uri.toURL());
    }
    /**
     * Creates a BufferedReader for url.
     * @param url
     * @return
     * @throws IOException
     * @throws URISyntaxException 
     */
    public static BufferedReader createReader(URI uri) throws IOException, URISyntaxException
    {
        return createReader(uri.toURL());
    }
    /**
     * Returns Path for given url.
     * @param url
     * @return
     * @throws URISyntaxException 
     */
    public static Path getPath(URL url) throws URISyntaxException
    {
        return new File(url.toURI()).toPath();
    }
    /**
     * Returns true if given url is file: and is writable.
     * @param url
     * @return
     * @throws URISyntaxException 
     */
    public static boolean isWritable(URL url) throws URISyntaxException
    {
        switch (url.getProtocol())
        {
            case "file":
                return Files.isWritable(Paths.get(url.toURI()));
            default:
                return false;
        }
    }
    /**
     * Creates a BufferedReader for url.
     * @param url
     * @return
     * @throws IOException
     * @throws URISyntaxException 
     */
    public static BufferedReader createReader(URL url) throws IOException, URISyntaxException
    {
        switch (url.getProtocol())
        {
            case "file":
                return Files.newBufferedReader(Paths.get(url.toURI()), UTF_8);
            case "http":
            case "https":
                return createReader((HttpURLConnection)url.openConnection());
            default:
                throw new UnsupportedOperationException(url+" not supported");
        }
    }
    private static BufferedReader createReader(HttpURLConnection con) throws IOException
    {
        return new BufferedReader(new InputStreamReader(con.getInputStream(), UTF_8));  // TODO detect charset
    }
    /**
     * Returns true if it is possible to read the contents of the url
     * @param url
     * @return 
     */
    public static boolean exists(URL url)
    {
        try
        {
            switch (url.getProtocol())
            {
                case "file":
                    return Files.exists(Paths.get(url.toURI()));
                case "http":
                case "https":
                    return exists((HttpURLConnection)url.openConnection());
                default:
                    throw new UnsupportedOperationException(url+" not supported");
            }
        }
        catch (URISyntaxException | IOException ex)
        {
            return false;
        }
    }
    private static boolean exists(HttpURLConnection con) throws IOException
    {
        con.setDoInput(true);
        con.setRequestMethod("HEAD");
        con.connect();
        return con.getResponseCode() == HttpURLConnection.HTTP_OK;
    }
}
