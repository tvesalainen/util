/*
 * Copyright (C) 2017 tkv
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
package org.vesalainen.rpm;

import java.io.IOException;
import java.nio.ByteBuffer;
import static java.nio.ByteOrder.BIG_ENDIAN;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.vesalainen.nio.FilterByteBuffer;
import static org.vesalainen.rpm.HeaderTag.*;
import org.vesalainen.util.HexUtil;

/**
 * RPM is a class for reading and creating of LSB RPM file.
 * @author tkv
 * @see <a href="http://refspecs.linux-foundation.org/LSB_4.0.0/LSB-Core-generic/LSB-Core-generic/book1.html">Linux Standard Base Core Specification 4.0</a>
 */
public class RPM extends RPMBase implements AutoCloseable
{
    private FileChannel fc;
    private ByteBuffer bb;
    private int signatureStart;
    private int headerStart;
    private int payloadStart;

    public RPM()
    {
        
    }
    public void load(Path path) throws IOException, NoSuchAlgorithmException
    {
        fc = FileChannel.open(path, READ);
        bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, Files.size(path));
        bb.order(BIG_ENDIAN);
        // lead
        lead = new Lead(bb);
        
        signatureStart = bb.position();
        
        signature = new HeaderStructure(bb, true);

        align(bb, 8);
        ByteBuffer rest = bb.slice();
        int sigSize = getInt32(RPMSIGTAG_SIZE);
        if (sigSize != rest.limit())
        {
            throw new IllegalArgumentException("sig size don't match");
        }
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(rest);
        byte[] digest = md5.digest();
        byte[] dig = getBin(RPMSIGTAG_MD5);
        
        if (!Arrays.equals(dig, digest))
        {
            throw new IllegalArgumentException("md5 don't match");
        }
        headerStart = bb.position();
        
        header = new HeaderStructure(bb, false);
        
        checkRequiredTags();
        
        payloadStart = bb.position();
        
        FilterByteBuffer fbb = new FilterByteBuffer(bb, GZIPInputStream::new, null);
        int fileCount = getFileCount()+1;
        for (int ii=0;ii<fileCount;ii++)
        {
            fileRecords.add(new FileRecord(fbb));
        }
        checkFileRecords();
    }
    void save(ByteBuffer bb) throws IOException
    {
        this.bb = bb;
        bb.order(BIG_ENDIAN);
        // lead
        lead.save(bb);
        
        signature.save(bb);
        header.save(bb);
    }
    public void save(Path path) throws IOException
    {
        fc = FileChannel.open(path, CREATE, WRITE, TRUNCATE_EXISTING);
        bb = fc.map(FileChannel.MapMode.READ_WRITE, 0, Files.size(path));
        save(bb);
    }
    @Override
    public void close() throws IOException
    {
        if (fc != null)
        {
            fc.close();
        }
    }
    public void append(Appendable out) throws IOException
    {
        out.append(String.format("lead %d signature %d header %d payload\n", signatureStart, headerStart, payloadStart));
        out.append(lead.name).append('\n');
        out.append("Signature\n");
        signature.append(out);
        out.append("Header\n");
        header.append(out);
    }

    private void checkFileRecords() throws NoSuchAlgorithmException
    {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        List<String> names = getFilenames();
        List<Integer> sizes = getInt32Array(RPMTAG_FILESIZES);
        List<String> md5List = getStringArray(RPMTAG_FILEMD5S);
        int len = fileRecords.size()-1;
        for (int ii=0;ii<len;ii++)
        {
            FileRecord fr = fileRecords.get(ii);
            if (!fr.filename.equals(names.get(ii)))
            {
                throw new IllegalArgumentException(fr.filename+" != "+names.get(ii));
            }
            if (fr.content.limit() != sizes.get(ii))
            {
                throw new IllegalArgumentException(fr.content+" size not "+sizes.get(ii));
            }
            ByteBuffer duplicate = fr.content.duplicate();
            duplicate.flip();
            md5.update(duplicate);
            byte[] digest = md5.digest();
            String digStr = HexUtil.toString(digest);
            if (!digStr.equalsIgnoreCase(md5List.get(ii)))
            {
                throw new IllegalArgumentException(fr.filename+" md5 conflict");
            }
            md5.reset();
        }
    }

}
