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
package org.vesalainen.installer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import org.apache.maven.model.Model;
import org.vesalainen.bean.ExpressionParser;
import org.vesalainen.test.pom.FileModelResolver;
import org.vesalainen.test.pom.ModelFactory;
import org.vesalainen.test.pom.Version;
import org.vesalainen.util.LoggingCommandLine;
import org.vesalainen.vfs.pm.PackageFilenameFactory;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MavenPackager extends LoggingCommandLine
{
    private ModelFactory factory;
    private Model root;
    private File localRepository;
    private String packageType;
    private String groupId;
    private String artifactId;
    private String version;
    private FileModelResolver fileModelResolver;
    private String classpath;
    private ExpressionParser expressionParser;

    public MavenPackager()
    {
        String lr = System.getProperty("localRepository");
        if (lr != null)
        {
            addOption("-lr", "Local Repository", null, new File(lr));
        }
        else
        {
            addOption(File.class, "-lr", "Local Repository", null, true);
        }
        addOption("-pt", "Package type deb/rpm def=deb", null, "deb");
        addOption("-g", "Group Id");
        addOption("-a", "Artifact Id");
        addOption(String.class, "-v", "Version", null, false);
        setLogLevel(Level.CONFIG);
        setPushLevel(Level.CONFIG);
    }
    
    @Override
    public void command(String... args)
    {
        info("Starting %s", MyVersion.version());
        super.command(args);
        localRepository = getOption("-lr");
        config("localRepository=%s", localRepository);
        factory = new ModelFactory(localRepository, null);
        fileModelResolver = factory.getFileModelResolver();
        packageType = getOption("-pt");
        groupId = getOption("-g");
        config("groupId=%s", groupId);
        artifactId = getOption("-a");
        config("artifactId=%s", artifactId);
        version = getOption("-v");
        if (version == null)
        {
            List<Version> versions = fileModelResolver.getVersions(groupId, artifactId);
            if (versions == null || versions.isEmpty())
            {
                throw new IllegalArgumentException("no version for "+groupId+"."+artifactId);
            }
            version = versions.get(versions.size()-1).toString();
        }
        config("version=%s", version);
        root = factory.getLocalModel(groupId, artifactId, version);
        expressionParser = new ExpressionParser(root)
                            .addMapper((s)->root.getProperties().getProperty(s))
                            .addMapper(this);
    }
    private void createPackage()
    {
        
    }
    private Path createPath()
    {
        for (int rr = 1;;rr++)
        {
            Path path = PackageFilenameFactory.getPath(packageType, groupId+"."+artifactId, version, String.valueOf(rr), null);
            if (!Files.exists(path))
            {
                return path;
            }
        }
    }
    static void createPackage(String... args) throws IOException, URISyntaxException, InterruptedException
    {
        MavenPackager packager = new MavenPackager();
        packager.command(args);
        packager.createPackage();
    }
    
    public static void main(String... args)
    {
        try
        {
            createPackage(args);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
