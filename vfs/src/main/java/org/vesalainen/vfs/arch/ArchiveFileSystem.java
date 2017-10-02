/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.vfs.arch;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.vesalainen.nio.channels.GZIPChannel;
import org.vesalainen.util.logging.AttachedLogger;
import org.vesalainen.vfs.Env;
import org.vesalainen.vfs.Root;
import org.vesalainen.vfs.VirtualFileSystem;
import org.vesalainen.vfs.VirtualFileSystemProvider;
import org.vesalainen.vfs.attributes.FileAttributeName;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;
import org.vesalainen.vfs.unix.Inode;
import org.vesalainen.vfs.unix.UserPrincipalLookupServiceImpl;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class ArchiveFileSystem extends VirtualFileSystem implements AttachedLogger
{


    protected Path path;
    protected Map<String, ?> env;
    protected Supplier<Header> headerSupplier;
    protected Set<? extends OpenOption> openOptions;
    protected FileFormat format;
    protected boolean readOnly;
    protected SeekableByteChannel channel;

    protected ArchiveFileSystem(
            VirtualFileSystemProvider provider, 
            Path path, 
            Map<String, ?> env,
            Supplier<Header> headerSupplier, 
            int bufSize, 
            int maxSkipSize) throws IOException
    {
        this(provider, 
                path, 
                env,
                headerSupplier, 
                openChannel(path, env, bufSize, maxSkipSize));
    }

    protected ArchiveFileSystem(
            VirtualFileSystemProvider provider, 
            Path path, 
            Map<String, ?> env,
            Supplier<Header> headerSupplier, 
            SeekableByteChannel channel) throws IOException
    {
        super(provider, env);
        this.path = path;
        this.env = env;
        this.headerSupplier = headerSupplier;
        this.openOptions = Env.getOpenOptions(path, env);
        this.format = Env.getFileFormat(path, env);
        this.channel = channel;
        readOnly = !openOptions.contains(WRITE);
    }

    private static SeekableByteChannel openChannel(Path path, Map<String, ?> env, int bufSize, int maxSkipSize) throws IOException
    {
        Set<? extends OpenOption> options = Env.getOpenOptions(path, env);
        FileAttribute<?>[] attrs = Env.getFileAttributes(env);
        SeekableByteChannel ch;
        String filename = path.getFileName().toString();
        if (filename.endsWith(".gz"))
        {
            GZIPChannel gzipChannel = new GZIPChannel(path, options, bufSize, maxSkipSize, attrs);
            ch = gzipChannel;
            if (!options.contains(WRITE))
            {
                filename = gzipChannel.getFilename();
                filename = filename == null ? "" : filename;
            }
            else
            {
                gzipChannel.setFilename(filename);
            }
        }
        else
        {
            ch = FileChannel.open(path, options, attrs);
        }
        return ch;
    }

    @Override
    public final boolean isReadOnly()
    {
        return readOnly;
    }

    @Override
    public boolean isOpen()
    {
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException
    {
        if (!readOnly)
        {
            store(channel, getDefaultRoot());
        }
        channel.close();
    }

    protected final void load(SeekableByteChannel ch, Root root) throws IOException
    {
        Header header = headerSupplier.get();
        header.load(ch);
        while (!header.isEof())
        {
            finest("load %s: %s", format, header);
            Path pth = getPath(root, header.getFilename());
            FileAttribute<?>[] fileAttributes = header.fileAttributes();
            Long size = (long) header.get(SIZE);
            if (size == null)
            {
                throw new IllegalArgumentException("size missing in " + pth);
            }
            Path parent = pth.getParent();
            if (parent != null)
            {
                Files.createDirectories(parent);
            }
            String linkname = header.getLinkname();
            Path linkTarget = null;
            if (linkname != null)
            {
                if (linkname.startsWith(getSeparator()))
                {
                    linkTarget = getPath(root, linkname);
                }
                else
                {
                    linkTarget = getPath(linkname);
                }
            }
            switch (header.getType())
            {
                case DIRECTORY:
                    Files.createDirectories(pth, fileAttributes);
                    break;
                case SYMBOLIC:
                    Files.createSymbolicLink(pth, linkTarget, fileAttributes);
                    break;
                case HARD:
                    Files.createLink(pth, linkTarget);
                    break;
                case REGULAR:
                    if (size > 0)
                    {
                        try (FileChannel fileChannel = FileChannel.open(pth, EnumSet.of(WRITE, CREATE), fileAttributes))
                        {
                            long pos = 0;
                            while (size > 0)
                            {
                                long count = fileChannel.transferFrom(ch, pos, size);
                                pos += count;
                                size -= count;
                            }
                        }
                    }
                    else
                    {
                        Files.createFile(pth, fileAttributes);
                    }
                    break;
            }
            if (size > 0)
            {
                switch (header.getType())
                {
                    case HARD:
                    case REGULAR:
                        try (FileChannel fileChannel = FileChannel.open(pth, EnumSet.of(WRITE, CREATE), fileAttributes))
                        {
                            long pos = 0;
                            while (size > 0)
                            {
                                long count = fileChannel.transferFrom(ch, pos, size);
                                pos += count;
                                size -= count;
                            }
                        }
                        break;
                    case SYMBOLIC:
                        break;  // handled
                    default:    // directory might have 4096 bytes of useless bytes
                        ByteBuffer b = ByteBuffer.allocateDirect(size.intValue());
                        ch.read(b);
                        break;
                }
            }
            if (header.supportsDigest())
            {
                // checksum
                String digestAlgorithm = header.digestAlgorithm();
                byte[] digest = (byte[]) Files.getAttribute(pth, digestAlgorithm);
                byte[] digestHdr = header.digest();
                if (!Arrays.equals(digestHdr, digest))
                {
                    throw new IllegalArgumentException(digestAlgorithm + " don't match");
                }
            }
            header.clear();
            header.load(ch);
        }
    }
    protected Path getPath(Root root, String filename)
    {
        if (filename.startsWith(getSeparator()))
        {
            filename = filename.substring(1);
        }
        return root.resolve(getPath(filename).normalize());
    }
    protected Stream<Path> walk(Root root) throws IOException
    {
        return Files.walk(root).filter((p -> p != root));
    }
    protected final void store(SeekableByteChannel ch, Root root) throws IOException
    {
        enumerateInodes();
        Map<Inode, Path> inodes = new HashMap<>();
        Set<String> supportedFileAttributeViews = supportedFileAttributeViews();
        Header header = headerSupplier.get();
        Set<String> topViews = FileAttributeName.topViews(supportedFileAttributeViews);
        Map<String, Object> all = new HashMap<>();
        walk(root).forEach((sourcePath)
            -> 
            {
                try
                {
                    Path targetPath = root.relativize(sourcePath);
                    all.clear();
                    for (String view : topViews)
                    {
                        Map<String, Object> attrs = Files.readAttributes(sourcePath, view + ":*", NOFOLLOW_LINKS);
                        for (Entry<String, Object> entry : attrs.entrySet())
                        {
                            all.put(FileAttributeName.getInstance(entry.getKey()).toString(), entry.getValue());
                        }
                    }
                    Inode inode = new Inode((int) all.get(DEVICE), (int) all.get(INODE));
                    Path link;
                    if (Files.isSymbolicLink(sourcePath))
                    {
                        link = Files.readSymbolicLink(sourcePath);
                    }
                    else
                    {
                        link = inodes.get(inode);
                    }
                    String linkname = link != null ? link.toString() : null;
                    inodes.put(inode, targetPath);
                    header.clear();
                    byte[] digest = null;
                    // checksum
                    String digestAlgorithm = header.digestAlgorithm();
                    if (digestAlgorithm != null)
                    {
                        digest = (byte[]) Files.getAttribute(sourcePath, digestAlgorithm);
                    }
                    header.store(ch, targetPath.toString(), format, linkname, all, digest);
                    finest("store %s: %s", format, header);
                    long size = header.size();
                    if (size > 0)
                    {
                        try (FileChannel fileChannel = FileChannel.open(sourcePath, READ, NOFOLLOW_LINKS))
                        {
                            fileChannel.transferTo(0, size, ch);
                        }
                    }
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
        });
        header.clear();
        header.storeEof(ch, format);
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService()
    {
        return new UserPrincipalLookupServiceImpl();
    }

}
