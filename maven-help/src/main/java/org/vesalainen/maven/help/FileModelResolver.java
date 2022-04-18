/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.maven.help;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.vesalainen.regex.SyntaxErrorException;

/**
 * @deprecated Use POM
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FileModelResolver extends AbstractModelResolver implements ModelResolver
{
    private final File base;

    public FileModelResolver()
    {
        String repository = System.getProperty("localRepository");
        base = new File(repository);
    }

    public FileModelResolver(File repository)
    {
        base = repository;
    }
    
    @Override
    public ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException
    {/*
        if (VersionRange.isVersionRange(version))
        {
            VersionRange range = new VersionRange(version);
            String directoryName = getDirectory(groupId, artifactId);
            File dir = new File(base, directoryName);
            FileFilter filter = new FileFilter() 
            {

                @Override
                public boolean accept(File pathname)
                {
                    if (pathname.isDirectory())
                    {
                        switch (pathname.getName())
                        {
                            case ".":
                            case "..":
                                return false;
                            default:
                                return true;
                        }
                    }
                    return false;
                }
            };
            List<String> versions = new ArrayList<>();
            for (File file : dir.listFiles(filter))
            {
                versions.add(file.getName());
            }
            Collections.sort(versions);
            // find oldest version that satisfies
            for (String v : versions)
            {
                if (range.in(v))
                {
                    return resolveModel(groupId, artifactId, v);
                }
            }
            throw new IllegalArgumentException("no suitable version for "+version);
        }
        else*/
        {
            String filename = getFilename(groupId, artifactId, version, "pom");
            File file = new File(base, filename);
            if (!file.exists())
            {
                throw new UnresolvableModelException("file "+file+" doesn't exist", groupId, artifactId, version);
            }
            if (!file.canRead())
            {
                throw new UnresolvableModelException("can't read file "+file, groupId, artifactId, version);
            }
            return new FileModelSource(file);
        }
    }


    @Override
    public ModelResolver newCopy()
    {
        return new FileModelResolver(base);
    }
    
    public List<Version> getVersions(String groupId, String artifactId)
    {
        String directoryName = getDirectory(groupId, artifactId);
        File dir = new File(base, directoryName);
        FileFilter filter = new FileFilter() 
        {

            @Override
            public boolean accept(File pathname)
            {
                if (pathname.isDirectory())
                {
                    switch (pathname.getName())
                    {
                        case ".":
                        case "..":
                            return false;
                        default:
                            return true;
                    }
                }
                return false;
            }
        };
        List<Version> versions = new ArrayList<>();
        if (dir.isDirectory())
        {
            for (File file : dir.listFiles(filter))
            {
                try
                {
                    versions.add(VersionParser.VERSION_PARSER.parseVersion(file.getName()));
                }
                catch (SyntaxErrorException ex)
                {
                    
                }
            }
        }
        Collections.sort(versions);
        return versions;
    }

    @Override
    public ModelSource resolveModel(Parent parent) throws UnresolvableModelException
    {
        return resolveModel(parent.getGroupId(), parent.getArtifactId(), parent.getVersion());
    }

    @Override
    public void addRepository(Repository rpstr, boolean bln) throws InvalidRepositoryException
    {
    }

    @Override
    public ModelSource resolveModel(Dependency dependency) throws UnresolvableModelException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
