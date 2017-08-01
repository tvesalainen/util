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
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import static org.vesalainen.rpm.HeaderTag.*;

/**
 *
 * @author tkv
 */
public class RPM implements AutoCloseable
{
    static final byte[] LEAD_MAGIC = new byte[]{(byte)0xed, (byte)0xab, (byte)0xee, (byte)0xdb};
    static final byte[] HEADER_MAGIC = new byte[]{(byte)0x8e, (byte)0xad, (byte)0xe8, (byte)0x01};
    private FileChannel fc;
    private ByteBuffer bb;
    Lead lead;
    HeaderStructure signature;
    HeaderStructure header;
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
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(rest);
        byte[] digest = md5.digest();
        HeaderStructure.IndexRecord md5Record = signature.getIndexRecord(RPMSIGTAG_MD5);
        byte[] dig = (byte[]) md5Record.data;
        
        if (!Arrays.equals(dig, digest))
        {
            throw new IllegalArgumentException("md5 don't match");
        }
        headerStart = bb.position();
        
        header = new HeaderStructure(bb, false);
        
        payloadStart = bb.position();
        
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
    public void append(Appendable out) throws IOException
    {
        out.append(String.format("lead %d signature %d header %d payload\n", signatureStart, headerStart, payloadStart));
        out.append("Signature\n");
        signature.append(out);
        out.append("Header\n");
        header.append(out);
    }
    static void align(ByteBuffer bb, int align)
    {
        bb.position(alignedPosition(bb, align));
    }
    static int alignedPosition(ByteBuffer bb, int align)
    {
        int position = bb.position();
        int mod =  position % align;
        if (mod > 0)
        {
            return position + align-mod;
        }
        else
        {
            return position;
        }
    }
    static void skip(ByteBuffer bb, int skip)
    {
        bb.position(bb.position()+skip);
    }
    @Override
    public void close() throws IOException
    {
        if (fc != null)
        {
            fc.close();
        }
    }
}
