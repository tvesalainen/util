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
/*
 * MimeTypes.java
 *
 * Created on 31. joulukuuta 2004, 13:44
 */

package org.vesalainen.util;

import java.util.HashMap;
import java.io.File;
import java.net.URI;
import java.util.Map;
/**
 * Resolves MIME types by using file extensions
 * @author tkv
 */
public class MimeTypes
{
    /**
     * Default type if none is resolved
     */
    public static final String DEFAULT = "application/octet-stream";
    private static final Map<String,String> mimeTable = new HashMap<String,String>();
    static
    {
        mimeTable.put("ez", "application/andrew-inset");
        mimeTable.put("hqx", "application/mac-binhex40");
        mimeTable.put("cpt", "application/mac-compactpro");
        mimeTable.put("doc", "application/msword");
        mimeTable.put("bin", "application/octet-stream");
        mimeTable.put("dms", "application/octet-stream");
        mimeTable.put("lha", "application/octet-stream");
        mimeTable.put("lzh", "application/octet-stream");
        mimeTable.put("exe", "application/octet-stream");
        mimeTable.put("class", "application/octet-stream");
        mimeTable.put("so", "application/octet-stream");
        mimeTable.put("dll", "application/octet-stream");
        mimeTable.put("oda", "application/oda");
        mimeTable.put("pdf", "application/pdf");
        mimeTable.put("ai", "application/postscript");
        mimeTable.put("eps", "application/postscript");
        mimeTable.put("ps", "application/postscript");
        mimeTable.put("rtf", "application/rtf");
        mimeTable.put("smi", "application/smil");
        mimeTable.put("smil", "application/smil");
        mimeTable.put("mif", "application/vnd.mif");
        mimeTable.put("xls", "application/vnd.ms-excel");
        mimeTable.put("ppt", "application/vnd.ms-powerpoint");
        mimeTable.put("wbxml", "application/vnd.wap.wbxml");
        mimeTable.put("wmlc", "application/vnd.wap.wmlc");
        mimeTable.put("wmlsc", "application/vnd.wap.wmlscriptc");
        mimeTable.put("bcpio", "application/x-bcpio");
        mimeTable.put("bz2", "application/x-bzip2");
        mimeTable.put("vcd", "application/x-cdlink");
        mimeTable.put("pgn", "application/x-chess-pgn");
        mimeTable.put("cpio", "application/x-cpio");
        mimeTable.put("csh", "application/x-csh");
        mimeTable.put("dcr", "application/x-director");
        mimeTable.put("dir", "application/x-director");
        mimeTable.put("dxr", "application/x-director");
        mimeTable.put("dvi", "application/x-dvi");
        mimeTable.put("spl", "application/x-futuresplash");
        mimeTable.put("gtar", "application/x-gtar");
        mimeTable.put("gz", "application/x-gzip");
        mimeTable.put("tgz", "application/x-gzip");
        mimeTable.put("hdf", "application/x-hdf");
        mimeTable.put("js", "application/x-javascript");
        mimeTable.put("kwd", "application/x-kword");
        mimeTable.put("kwt", "application/x-kword");
        mimeTable.put("ksp", "application/x-kspread");
        mimeTable.put("kpr", "application/x-kpresenter");
        mimeTable.put("kpt", "application/x-kpresenter");
        mimeTable.put("chrt", "application/x-kchart");
        mimeTable.put("kil", "application/x-killustrator");
        mimeTable.put("skp", "application/x-koan");
        mimeTable.put("skd", "application/x-koan");
        mimeTable.put("skt", "application/x-koan");
        mimeTable.put("skm", "application/x-koan");
        mimeTable.put("latex", "application/x-latex");
        mimeTable.put("nc", "application/x-netcdf");
        mimeTable.put("cdf", "application/x-netcdf");
        mimeTable.put("ogg", "application/x-ogg");
        mimeTable.put("rpm", "application/x-rpm");
        mimeTable.put("sh", "application/x-sh");
        mimeTable.put("shar", "application/x-shar");
        mimeTable.put("swf", "application/x-shockwave-flash");
        mimeTable.put("sit", "application/x-stuffit");
        mimeTable.put("sv4cpio", "application/x-sv4cpio");
        mimeTable.put("sv4crc", "application/x-sv4crc");
        mimeTable.put("tar", "application/x-tar");
        mimeTable.put("tcl", "application/x-tcl");
        mimeTable.put("tex", "application/x-tex");
        mimeTable.put("texinfo", "application/x-texinfo");
        mimeTable.put("texi", "application/x-texinfo");
        mimeTable.put("t", "application/x-troff");
        mimeTable.put("tr", "application/x-troff");
        mimeTable.put("roff", "application/x-troff");
        mimeTable.put("man", "application/x-troff-man");
        mimeTable.put("me", "application/x-troff-me");
        mimeTable.put("ms", "application/x-troff-ms");
        mimeTable.put("ustar", "application/x-ustar");
        mimeTable.put("src", "application/x-wais-source");
        mimeTable.put("xhtml", "application/xhtml+xml");
        mimeTable.put("xht", "application/xhtml+xml");
        mimeTable.put("zip", "application/zip");
        mimeTable.put("au", "audio/basic");
        mimeTable.put("snd", "audio/basic");
        mimeTable.put("mid", "audio/midi");
        mimeTable.put("midi", "audio/midi");
        mimeTable.put("kar", "audio/midi");
        mimeTable.put("mpga", "audio/mpeg");
        mimeTable.put("mp2", "audio/mpeg");
        mimeTable.put("mp3", "audio/mpeg");
        mimeTable.put("aif", "audio/x-aiff");
        mimeTable.put("aiff", "audio/x-aiff");
        mimeTable.put("aifc", "audio/x-aiff");
        mimeTable.put("m3u", "audio/x-mpegurl");
        mimeTable.put("ram", "audio/x-pn-realaudio");
        mimeTable.put("rm", "audio/x-pn-realaudio");
        mimeTable.put("ra", "audio/x-realaudio");
        mimeTable.put("wav", "audio/x-wav");
        mimeTable.put("pdb", "chemical/x-pdb");
        mimeTable.put("xyz", "chemical/x-xyz");
        mimeTable.put("bmp", "image/bmp");
        mimeTable.put("gif", "image/gif");
        mimeTable.put("ief", "image/ief");
        mimeTable.put("jpeg", "image/jpeg");
        mimeTable.put("jpg", "image/jpeg");
        mimeTable.put("jpe", "image/jpeg");
        mimeTable.put("png", "image/png");
        mimeTable.put("tiff", "image/tiff");
        mimeTable.put("tif", "image/tiff");
        mimeTable.put("djvu", "image/vnd.djvu");
        mimeTable.put("djv", "image/vnd.djvu");
        mimeTable.put("wbmp", "image/vnd.wap.wbmp");
        mimeTable.put("ras", "image/x-cmu-raster");
        mimeTable.put("pnm", "image/x-portable-anymap");
        mimeTable.put("pbm", "image/x-portable-bitmap");
        mimeTable.put("pgm", "image/x-portable-graymap");
        mimeTable.put("ppm", "image/x-portable-pixmap");
        mimeTable.put("rgb", "image/x-rgb");
        mimeTable.put("xbm", "image/x-xbitmap");
        mimeTable.put("xpm", "image/x-xpixmap");
        mimeTable.put("xwd", "image/x-xwindowdump");
        mimeTable.put("igs", "model/iges");
        mimeTable.put("iges", "model/iges");
        mimeTable.put("msh", "model/mesh");
        mimeTable.put("mesh", "model/mesh");
        mimeTable.put("silo", "model/mesh");
        mimeTable.put("wrl", "model/vrml");
        mimeTable.put("vrml", "model/vrml");
        mimeTable.put("css", "text/css");
        mimeTable.put("html", "text/html");
        mimeTable.put("htm", "text/html");
        mimeTable.put("asc", "text/plain");
        mimeTable.put("txt", "text/plain");
        mimeTable.put("rtx", "text/richtext");
        mimeTable.put("rtf", "text/rtf");
        mimeTable.put("sgml", "text/sgml");
        mimeTable.put("sgm", "text/sgml");
        mimeTable.put("tsv", "text/tab-separated-values");
        mimeTable.put("wml", "text/vnd.wap.wml");
        mimeTable.put("wmls", "text/vnd.wap.wmlscript");
        mimeTable.put("etx", "text/x-setext");
        mimeTable.put("xml", "text/xml");
        mimeTable.put("xsl", "text/xml");
        mimeTable.put("xsd", "text/xml");
        mimeTable.put("mpeg", "video/mpeg");
        mimeTable.put("mpg", "video/mpeg");
        mimeTable.put("mpe", "video/mpeg");
        mimeTable.put("qt", "video/quicktime");
        mimeTable.put("mov", "video/quicktime");
        mimeTable.put("mxu", "video/vnd.mpegurl");
        mimeTable.put("avi", "video/x-msvideo");
        mimeTable.put("movie", "video/x-sgi-movie");
        mimeTable.put("ice", "x-conference/x-cooltalk");
        mimeTable.put("ico", "image/ico");
        mimeTable.put("kml", "application/vnd.google-earth.kml+xml");
        mimeTable.put("kmz", "application/vnd.google-earth.kmz");
        mimeTable.put("docm", "application/vnd.ms-word.document.macroEnabled.12");
        mimeTable.put("xlsx", "application/vnd.openxmlformats");
        mimeTable.put("pptx", "application/vnd.openxmlformats");
        mimeTable.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mimeTable.put("dotm", "application/vnd.ms-word.template.macroEnabled.12");
        mimeTable.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
        mimeTable.put("potm", "application/vnd.ms-powerpoint.template.macroEnabled.12");
        mimeTable.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
        mimeTable.put("ppam", "application/vnd.ms-powerpoint.addin.macroEnabled.12");
        mimeTable.put("ppsm", "application/vnd.ms-powerpoint.slideshow.macroEnabled.12");
        mimeTable.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        mimeTable.put("pptm", "application/vnd.ms-powerpoint.presentation.macroEnabled.12");
        mimeTable.put("xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
        mimeTable.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
        mimeTable.put("xlsm", "application/vnd.ms-excel.sheet.macroEnabled.12");
        mimeTable.put("xlt", "application/vnd.ms-excel");
        mimeTable.put("xla", "application/vnd.ms-excel");
        mimeTable.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
        mimeTable.put("xltm", "application/vnd.ms-excel.template.macroEnabled.12");
        mimeTable.put("xps", "application/vnd.ms-xpsdocument");
        mimeTable.put("ppt", "application/application/vnd.ms-powerpoint");
        mimeTable.put("pot", "application/application/vnd.ms-powerpoint");
        mimeTable.put("pps", "application/application/vnd.ms-powerpoint");
        mimeTable.put("ppa", "application/application/vnd.ms-powerpoint");
        mimeTable.put("doc", "application/msword");
        mimeTable.put("dot", "application/msword");
        mimeTable.put("ser", "application/x-java-serialized-object");
        mimeTable.put("json", "application/json");
    }
    
    /**
     * 
     * @return Returns MIME TYPE for filename extension. Returns null if extension is unknown.
     * <p>If fileExtension contains '.' only right  part of string is used.
     * @param fileExtension File extension. Extension for file README.txt is 'txt'
     */
    
    public static String getMimeType(String fileExtension)
    {
        int idx = fileExtension.lastIndexOf('.');
        if (idx != -1)
        {
            fileExtension = fileExtension.substring(idx+1);
        }
        String type = mimeTable.get(fileExtension.toLowerCase());
        if (type != null)
        {
            return type;
        }
        return null;
    }
    
    /**
     *
     * @return Returns MIME TYPE for filename extension. Returns DEFAULT if extension is unknown.
     * @param file File whose file type is resolved
     */
    public static String getMimeType(File file)
    {
        String name = file.getName();
        int idx = name.lastIndexOf('.');
        if (idx != -1)
        {
            String ext = name.substring(idx+1);
            return getMimeType(ext);
        }
        return DEFAULT;
    }

    /**
     *
     * @return Returns MIME TYPE for filename extension. Returns DEFAULT if extension is unknown.
     * @param uri File whose file type is resolved
     */
    public static String getMimeType(URI uri)
    {
        String name = uri.getPath();
        int idx = name.lastIndexOf('.');
        if (idx != -1)
        {
            String ext = name.substring(idx+1);
            return getMimeType(ext);
        }
        return DEFAULT;
    }

}
