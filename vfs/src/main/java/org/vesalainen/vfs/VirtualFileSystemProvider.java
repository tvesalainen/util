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
package org.vesalainen.vfs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import static java.nio.file.LinkOption.*;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.NotLinkException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.nio.ByteBuffers;
import org.vesalainen.util.ArrayHelp;
import static org.vesalainen.vfs.VirtualFile.Type.*;
import org.vesalainen.vfs.arch.cpio.CPIOFileSystem;
import org.vesalainen.vfs.arch.tar.TARFileSystem;
import org.vesalainen.vfs.attributes.AclFileAttributeViewImpl;
import org.vesalainen.vfs.attributes.BasicFileAttributeViewImpl;
import org.vesalainen.vfs.attributes.DosFileAttributeViewImpl;
import org.vesalainen.vfs.attributes.FileAttributeAccess;
import org.vesalainen.vfs.attributes.FileAttributeName;
import org.vesalainen.vfs.attributes.PosixFileAttributeViewImpl;
import org.vesalainen.vfs.unix.UnixFileAttributeView;
import org.vesalainen.vfs.unix.UnixFileAttributeViewImpl;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;
import org.vesalainen.vfs.attributes.FileOwnerAttributeViewImpl;
import org.vesalainen.vfs.attributes.UserDefinedFileAttributeViewImpl;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VirtualFileSystemProvider extends FileSystemProvider
{
    public static final String SCHEME = "org.vesalainen.vfs";
    public static URI URI;
    protected Map<String,Class<? extends FileAttributeView>> viewClasses = new HashMap<>();
    protected Map<String,Function<FileAttributeAccess,? extends FileAttributeView>> viewSuppliers = new HashMap<>();

    static
    {
        try
        {
            URI = new URI("org.vesalainen.vfs:///", null, null);
        }
        catch (URISyntaxException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    public VirtualFileSystemProvider()
    {
        addFileAttributeView(BASIC_VIEW, BasicFileAttributeView.class, BasicFileAttributeViewImpl::new);
        addFileAttributeView(ACL_VIEW, AclFileAttributeView.class, AclFileAttributeViewImpl::new);
        addFileAttributeView(DOS_VIEW, DosFileAttributeView.class, DosFileAttributeViewImpl::new);
        addFileAttributeView(OWNER_VIEW, FileOwnerAttributeView.class, FileOwnerAttributeViewImpl::new);
        addFileAttributeView(POSIX_VIEW, PosixFileAttributeView.class, PosixFileAttributeViewImpl::new);
        addFileAttributeView(USER_VIEW, UserDefinedFileAttributeView.class, UserDefinedFileAttributeViewImpl::new);
        addFileAttributeView(UNIX_VIEW, UnixFileAttributeView.class, UnixFileAttributeViewImpl::new);
        
        FileSystemFactory.register(".cpio", CPIOFileSystem.class);
        FileSystemFactory.register(".cpio.gz", CPIOFileSystem.class);
        FileSystemFactory.register(".tar", TARFileSystem.class);
        FileSystemFactory.register(".tar.gz", TARFileSystem.class);
    }

    protected final void addFileAttributeView(String name, Class<? extends FileAttributeView> cls, Function<FileAttributeAccess,? extends FileAttributeView> func)
    {
        viewClasses.put(name, cls);
        viewSuppliers.put(name, func);
    }
    
    private VirtualFile getFile(Path path, CopyOption... options)
    {
        Path p = path.toAbsolutePath();
        VirtualFile file = store(p).get(p);
        if (file != null && !ArrayHelp.contains(options, NOFOLLOW_LINKS) && file.isSymbolicLink())
        {
            Path normalized = path.resolveSibling(file.getSymbolicTarget()).normalize();
            return getFile(normalized, options);
        }
        else
        {
            return file;
        }
    }
    @Override
    public String getScheme()
    {
        return SCHEME;
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileSystem newFileSystem(Path path, Map<String, ?> env) throws IOException
    {
        return FileSystemFactory.getInstance(this, path, env);
    }

    @Override
    public FileSystem getFileSystem(URI uri)
    {
        if (SCHEME.equalsIgnoreCase(uri.getScheme()))
        {
            try 
            {
                VirtualFileSystem vfs = new VirtualFileSystem(this);
                vfs.addFileStore("/", new VirtualFileStore(vfs, BASIC_VIEW, POSIX_VIEW, UNIX_VIEW, USER_VIEW), true);
                return vfs;
            }
            catch (IOException ex) 
            {
                throw new RuntimeException(ex);
            }
        }
        throw new IllegalArgumentException(uri+" not matching");
    }

    @Override
    public Path getPath(URI uri)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
    {
        return newFileChannel(path, options, attrs);
    }
    @Override
    public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
    {
        Path p = path.toAbsolutePath();
        VirtualFile file = getFile(p);
        Set<OpenOption> opts = new HashSet<>(options);
        if (!opts.contains(READ) && !opts.contains(WRITE) && !opts.contains(APPEND))
        {
            opts.add(READ);
        }
        if (opts.contains(APPEND))
        {
            if (opts.contains(READ) || opts.contains(TRUNCATE_EXISTING))
            {
                throw new IllegalArgumentException("READ or TRUNCATE_EXISTING with APPEND");
            }
            opts.add(WRITE);
        }
        if (options.contains(CREATE_NEW))
        {
            if (file != null)
            {
                throw new FileAlreadyExistsException(p.toString());
            }
            file = createFile(p, attrs);
        }
        if (options.contains(CREATE) && file == null)
        {
            file = createFile(p, attrs);
        }
        if (file == null)
        {
            throw new NoSuchFileException(p.toString());
        }
        return new VirtualFileChannel(p, file, opts);
    }

    private VirtualFile createFile(Path path, FileAttribute<?>... attrs) throws IOException
    {
        Path p = path.toAbsolutePath();
        checkNotExists(p);
        checkHasDirectory(p);
        return store(p).create(p, REGULAR, attrs);
    }
    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException
    {
        VirtualFile dirFile = find(dir);
        if (!dirFile.isDirectory())
        {
            throw new NotDirectoryException(dir.toString());
        }
        return store(dir).directoryStream(dir, filter);
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException
    {
        Path d = dir.toAbsolutePath();
        checkNotExists(d);
        checkHasDirectory(d);
        store(d).create(d, DIRECTORY, attrs);
    }

    @Override
    public void delete(Path path) throws IOException
    {
        Path p = path.toAbsolutePath();
        find(p);
        store(p).remove(p);
    }

    @Override
    public void createLink(Path link, Path existing) throws IOException
    {
        Path l = link.toAbsolutePath();
        checkNotExists(l);
        Path e = existing.toAbsolutePath();
        find(e);
        if (l.getRoot().equals(e.getRoot()))
        {
            store(e).link(l, e);
        }
        else
        {
            throw new UnsupportedOperationException("link not supported across file-stores");
        }
    }

    @Override
    public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) throws IOException
    {
        Path l = link.toAbsolutePath();
        checkNotExists(l);
        checkHasDirectory(l);
        byte[] bytes = target.toString().getBytes(US_ASCII);
        VirtualFile created = store(l).create(l, SYMBOLIC_LINK, attrs);
        ByteBuffer writeView = created.writeView(0, bytes.length);
        writeView.put(bytes);
        created.commit(writeView.position());
    }

    @Override
    public Path readSymbolicLink(Path link) throws IOException
    {
        VirtualFile l = find(link, NOFOLLOW_LINKS);
        if (!l.isSymbolicLink())
        {
            throw new NotLinkException(link.toString());
        }
        return l.getSymbolicTarget();
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException
    {
        if (!ArrayHelp.containsOnly(options, REPLACE_EXISTING, COPY_ATTRIBUTES, NOFOLLOW_LINKS))
        {
            throw new UnsupportedOperationException("some of "+Arrays.toString(options)+" not supported");
        }
        Path src = source.toAbsolutePath();
        Path trg = target.toAbsolutePath();
        if (isSameFile(src, trg))
        {
            return;
        }
        VirtualFile srcFile = find(src, options);
        if (ArrayHelp.contains(options, REPLACE_EXISTING));
        {
            deleteIfExists(trg);
        }
        checkNotExists(trg);
        switch (srcFile.getType())
        {
            case REGULAR:
                VirtualFile trgFile = createFile(trg);
                ByteBuffer rv = srcFile.readView(0);
                ByteBuffer wv = trgFile.writeView(0, rv.remaining());
                long len = ByteBuffers.move(rv, wv);
                trgFile.commit((int) len);
                break;
            case DIRECTORY:
                createDirectory(trg);
                break;
            case SYMBOLIC_LINK:
                createSymbolicLink(trg, srcFile.getSymbolicTarget());
                break;
            default:
                throw new UnsupportedOperationException(srcFile.getType()+" not supported");
        }
        if (ArrayHelp.contains(options, COPY_ATTRIBUTES));
        {
            copyAttributes(src, trg);
        }
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException
    {
        if (!ArrayHelp.containsOnly(options, REPLACE_EXISTING, ATOMIC_MOVE))
        {
            throw new UnsupportedOperationException("some of "+Arrays.toString(options)+" not supported");
        }
        if (ArrayHelp.contains(options, ATOMIC_MOVE))
        {
            throw new AtomicMoveNotSupportedException(source.toString(), target.toString(), "not supported");
        }
        Path src = source.toAbsolutePath();
        Path trg = target.toAbsolutePath();
        if (isSameFile(src, trg))
        {
            return;
        }
        if (store(src).isNonEmptyDirectory(src))
        {
            throw new UnsupportedOperationException("moving non empty directory not supported");
        }
        VirtualFile srcFile = find(src, options);
        if (ArrayHelp.contains(options, REPLACE_EXISTING))
        {
            deleteIfExists(trg);
        }
        checkNotExists(trg);
        store(src).add(trg, srcFile);
        delete(src);
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException
    {
        VirtualFile file = getFile(path);
        VirtualFile file2 = getFile(path2);
        return file != null && file2 != null && file.equals(file2);
    }

    @Override
    public boolean isHidden(Path path) throws IOException
    {
        VirtualFile file = find(path);
        DosFileAttributeView dfaw = file.getFileAttributeView(DosFileAttributeView.class);
        if (dfaw != null)
        {
            DosFileAttributes dfa = dfaw.readAttributes();
            return dfa.isHidden();
        }
        return false;
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException
    {
        VirtualFile file = find(path);
        return file.fileStore;
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException
    {
        VirtualFile file = find(path);
        if (!file.checkAccess(modes))
        {
            throw new AccessDeniedException(path.toString());
        }
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options)
    {
        if (!ArrayHelp.containsOnly(options, NOFOLLOW_LINKS))
        {
            throw new UnsupportedOperationException("some of "+Arrays.toString(options)+" not supported");
        }
        VirtualFile file = getFile(path, options);
        if (file != null)
        {
            return file.getFileAttributeView(type);
        }
        return null;
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException
    {
        if (!ArrayHelp.containsOnly(options, NOFOLLOW_LINKS))
        {
            throw new UnsupportedOperationException("some of "+Arrays.toString(options)+" not supported");
        }
        VirtualFile file = find(path, options);
        return file.readAttributes(type);
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException
    {
        if (!ArrayHelp.containsOnly(options, NOFOLLOW_LINKS))
        {
            throw new UnsupportedOperationException("some of "+Arrays.toString(options)+" not supported");
        }
        VirtualFile file = find(path, options);
        return file.readAttributes(attributes);
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException
    {
        if (!ArrayHelp.containsOnly(options, NOFOLLOW_LINKS))
        {
            throw new UnsupportedOperationException("some of "+Arrays.toString(options)+" not supported");
        }
        Name attr = FileAttributeName.getInstance(attribute);
        if ("size".equals(attr.getName()) || DIGEST_VIEW.equals(attr.getView()))
        {
            throw new IllegalArgumentException(attribute+" setting not supported");
        }
        VirtualFile file = find(path, options);
        file.setAttribute(attribute, value);
    }

    <A extends FileAttributeView> Map<String,Class<A>> createViewClassMap(String... views)
    {
        Map<String,Class<A>> res = new HashMap<>();
        for (String view : views)
        {
            Class<? extends FileAttributeView> cls = viewClasses.get(view);
            if (cls == null)
            {
                throw new UnsupportedOperationException(view+" not supported");
            }
            res.put(view, (Class<A>) cls);
        }
        return res;
    }
    <A extends FileAttributeView> Map<String,A> createViewMap(FileAttributeAccess access, Set<String> views)
    {
        Map<String,A> res = new HashMap<>();
        for (String view : views)
        {
            Function<FileAttributeAccess,? extends FileAttributeView> func = viewSuppliers.get(view);
            if (func == null)
            {
                throw new UnsupportedOperationException(view+" not supported");
            }
            res.put(view, (A) func.apply(access));
        }
        return res;
    }
    private VirtualFileStore store(Path path)
    {
        VirtualFileSystem vfs = (VirtualFileSystem) path.getFileSystem();
        return vfs.getFileStore(path);
    }
    private VirtualFile find(Path path,  CopyOption... options) throws NoSuchFileException
    {
        VirtualFile file = getFile(path.toAbsolutePath(), options);
        if (file == null)
        {
            throw new NoSuchFileException(path.toString());
        }
        return file;
    }
    private void checkNotExists(Path path) throws FileAlreadyExistsException
    {
        VirtualFile file = getFile(path);
        if (file != null)
        {
            throw new FileAlreadyExistsException(path.toString());
        }
    }
    private void checkHasDirectory(Path path) throws IOException
    {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null)
        {
            VirtualFile dir = getFile(parent);
            if (dir == null)
            {
                throw new IOException(parent+" doesn't exist");
            }
            if (!dir.isDirectory())
            {
                throw new IOException(dir+" is not directory");
            }
        }
    }

    private void copyAttributes(Path src, Path trg) throws IOException
    {
        VirtualFile srcFile = find(src);
        VirtualFile trgFile = find(trg);
        Set<String> topViews = FileAttributeName.topViews(trg.getFileSystem().supportedFileAttributeViews());
        for (String view : topViews)
        {
            Map<String, Object> attrs = srcFile.readAttributes(view+":*");
            attrs.forEach((n,a)->trgFile.setAttribute(n, a));
        }
    }
}
