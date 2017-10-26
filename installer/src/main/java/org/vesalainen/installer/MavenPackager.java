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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import static java.nio.charset.StandardCharsets.*;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.vesalainen.bean.ExpressionParser;
import org.vesalainen.graph.Graphs;
import org.vesalainen.test.pom.FileModelResolver;
import org.vesalainen.test.pom.ModelFactory;
import org.vesalainen.test.pom.Version;
import org.vesalainen.util.LoggingCommandLine;
import org.vesalainen.vfs.VirtualFileSystems;
import org.vesalainen.vfs.pm.Dependency;
import org.vesalainen.vfs.pm.FileUse;
import org.vesalainen.vfs.pm.PackageFileAttributes;
import org.vesalainen.vfs.pm.PackageFilenameFactory;
import org.vesalainen.vfs.pm.PackageManagerAttributeView;
import org.vesalainen.vfs.pm.deb.DEBDependency;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MavenPackager extends LoggingCommandLine
{
    private static final String DEFAULT_TEMPLATE = "/etc/default/default.tmpl";
    private static final String INIT_D_TEMPLATE = "/etc/init.d/init.tmpl";
    private static final String LOG_CONFIG_TEMPLATE = "/etc/opt/log-config.tmpl";
    private ModelFactory factory;
    private Model root;
    private Path localRepository;
    private Path packageDirectory;
    private String packageType;
    private boolean shortName;  // use artifactId / groupId.artifactId as package name.
    private String groupId;
    private String artifactId;
    private String version;
    private FileModelResolver fileModelResolver;
    private String classpath;
    private ExpressionParser expressionParser;
    private String maintainer;
    private FileSystem fileSystem;

    public MavenPackager()
    {
        String lr = System.getProperty("localRepository");
        if (lr != null)
        {
            addOption("-lr", "Local Repository", null, Paths.get(lr));
        }
        else
        {
            addOption(File.class, "-lr", "Local Repository", null, true);
        }
        addOption(Path.class, "-pd", "package directory");
        addOption("-pt", "Package type deb/rpm def=deb", null, "deb");
        addOption("-g", "Group Id");
        addOption("-a", "Artifact Id");
        addOption(String.class, "-v", "Version", null, false);
        addOption("-short", "use artifactId as package name", null, false);
        addOption(String.class, "-maintainer", "Maintainer of package", null, false);
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
        packageDirectory = getOption("-pd");
        config("packageDirectory=%s", packageDirectory);
        factory = new ModelFactory(localRepository.toFile(), null);
        fileModelResolver = factory.getFileModelResolver();
        packageType = getOption("-pt");
        config("packageType=%s", packageType);
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
        shortName = getOption("-short");
        maintainer = getOption("-maintainer");
        config("shortName=%s", shortName);
        root = factory.getLocalModel(groupId, artifactId, version);
        expressionParser = new ExpressionParser(root)
                            .addMapper((s)->root.getProperties().getProperty(s))
                            .addMapper(this);
    }
    private void createPackage() throws IOException
    {
        Path path = PackageFilenameFactory.getPath(packageDirectory, packageType, getPackage(), version, null);
        config("package=%s", path);
        Files.createFile(path);
        try (FileSystem pkgFS = VirtualFileSystems.newFileSystem(path, Collections.EMPTY_MAP))
        {
            fileSystem = pkgFS;
            PackageManagerAttributeView view = PackageManagerAttributeView.from(pkgFS);
            view.setSummary(root.getName());
            config("summary=%s", root.getName());
            view.setDescription(root.getDescription());
            config("description=%s", root.getDescription());
            String javaReq = getJavaReq();
            if (javaReq != null)
            {
                view.addRequire(javaReq);
                config("require=%s", javaReq);
            }
            String license = getLicense(root);
            view.setLicense(license);
            config("license=%s", license);
            String developers = getDevelopers(root);
            view.setCopyright(developers);
            config("copyright=%s", developers);
            if (maintainer != null)
            {
                view.setMaintainer(maintainer);
                config("maintainer=%s", maintainer);
            }
            else
            {
                view.setMaintainer(developers);
                warning("maintainer=%s as developers", developers);
            }
            view.setUrl(root.getUrl());
            config("url=%s", root.getUrl());
            copyJarsEtc(appDir(), view);
            Path etcInitDPath = initPath();
            createEtcInit(etcInitDPath);
            view.setPostInstallation("update-rc.d "+getPackage()+" defaults\nservice "+getPackage()+" start\n");
            view.setPreUnInstallation("service "+getPackage()+" stop\nupdate-rc.d "+getPackage()+" remove\n");
            createEtcDefault(etcDefaultPath());
            createLogConfig(getLogConfigPath());
            fileSystem = null;
        }
    }
    private Path appDir()
    {
        return fileSystem.getPath("/opt/"+groupId+"/"+artifactId);
    }
    private Path initPath()
    {
        return fileSystem.getPath("/etc/init.d/"+getPackage());
    }
    public Path etcDefaultPath()
    {
        return fileSystem.getPath("/etc/default/"+getPackage());
    }
    public Path getLogConfigPath()
    {
        return fileSystem.getPath("/etc/opt/"+getPackage()+"/log-config.xml");
    }
    public Path getConfigPath()
    {
        return fileSystem.getPath("/etc/opt/"+getPackage()+"/config.xml");
    }
    public String getLsbDescription()
    {
        return root.getDescription().replace("\n", "\n#   ");
    }
    public String getGeneratedText()
    {
        return "generated by maven packager "+MyVersion.version()+" at "+ZonedDateTime.now();
    }
    static void createPackage(String... args) throws IOException, URISyntaxException, InterruptedException
    {
        MavenPackager packager = new MavenPackager();
        packager.command(args);
        packager.createPackage();
    }
    public String getPackage()
    {
        if (shortName)
        {
            return artifactId;
        }
        else
        {
            return groupId+"."+artifactId;
        }
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

    private void copyJarsEtc(Path optPackage, PackageManagerAttributeView view) throws IOException
    {
        Path jarDir = optPackage.resolve("jar");
        List<Path> jars = new ArrayList<>();
        List<Model> models = getDependencies().collect(Collectors.toList());
        for (Model model : models)
        {
            String grp = model.getGroupId();
            String art = model.getArtifactId();
            String ver = model.getVersion();
            String packaging = model.getPackaging();
            String filename = fileModelResolver.getFilename(grp, art, ver, "jar");
            Path source = localRepository.resolve(filename);
            Path target = jarDir.resolve(localRepository.relativize(source));
            Files.createDirectories(target.getParent());
            Files.copy(source, target);
            jars.add(target);
            Files.setPosixFilePermissions(target, PosixFilePermissions.fromString("rw-r--r--"));
            config("copied %s -> %s", source, target);
            String license = getLicense(model);
            PackageFileAttributes.setLicense(target, license);
            String developers = getDevelopers(model);
            PackageFileAttributes.setCopyright(target, developers);
            String require = model.getProperties().getProperty("org.vesalainen.installer.require");
            if (require != null)
            {
                for (Dependency req : DEBDependency.parse(require))
                {
                    view.addRequire(req.toString());
                }
            }
        }
        classpath = jars.stream().map((p)->p.toString()).collect(Collectors.joining(":"));
    }
    private String getDevelopers(Model model)
    {
        return model.getDevelopers().stream().filter((d)->!d.getName().isEmpty()).map((d)->d.getName()+" <"+d.getEmail()+">").collect(Collectors.joining(", "));
    }
    private String getLicense(Model model)
    {
        return model.getLicenses().stream().map((l)->l.getName()).collect(Collectors.joining(", "));
    }
    private void createEtcInit(Path etcInitD) throws IOException
    {
        createFromResource(etcInitD, INIT_D_TEMPLATE, "rwxr-xr-x");
    }
    private void createEtcDefault(Path etcDefault) throws IOException
    {
        createFromResource(etcDefault, DEFAULT_TEMPLATE, "rw-r--r--");
    }
    private void createLogConfig(Path logConfPath) throws IOException
    {
        createFromResource(logConfPath, LOG_CONFIG_TEMPLATE, "rw-r--r--");
    }

    private void createFromResource(Path path, String resource, String permissions) throws IOException
    {
        Files.createDirectories(path.getParent());
        try (   BufferedReader br = new BufferedReader(new InputStreamReader(MavenPackager.class.getResourceAsStream(resource), UTF_8));
                BufferedWriter bw = Files.newBufferedWriter(path);
                )
        {
            String line = br.readLine();
            while (line != null)
            {
                String replaced = expressionParser.replace(line);
                bw.append(replaced.replace("&{", "${")).append('\n');
                line = br.readLine();
            }
        }
        Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(permissions));
        PackageFileAttributes.setUsage(path, FileUse.CONFIGURATION);
        config("created %s", path);
    }
    private Stream<String> getDependencyNames()
    {
        return getDependencies().map((m)->
        {
            fine("dependency %s", m);
            String grp = m.getGroupId();
            String art = m.getArtifactId();
            String ver = m.getVersion();
            String packaging = m.getPackaging();
            return fileModelResolver.getFilename(grp, art, ver, "jar");
        });
}
    private Stream<Model> getDependencies()
    {
        return Graphs.breadthFirst(root, 
                (y)->y.getDependencies()
                .stream()
                .filter((d)->!"true".equals(d.getOptional()))
                .filter((d)->"compile".equals(d.getScope()) || "runtime".equals(d.getScope()))
                .peek((d)->fine("%s", d))
                .map(factory::getVersionResolver)
                .map((v)->v.resolv())
                .map(factory::getLocalModel)
                );
    }
    public String getMainJar()
    {
        Path appDir = appDir();
        Path jarDir = appDir.resolve("jar");
        String filename = fileModelResolver.getFilename(groupId, artifactId, version, "jar");
        
        Path target = jarDir.resolve(filename);
        return target.toString();
    }
    public String getMainClass()
    {
        Build build = root.getBuild();
        for (Plugin plugin : build.getPlugins())
        {
            Xpp3Dom pluginConfigurations = (Xpp3Dom) plugin.getConfiguration();
            if (pluginConfigurations != null)
            {
                String found = Xpp3DomSearch.find(pluginConfigurations, "mainClass");
                if (found != null)
                {
                    return found;
                }
            }
            for (PluginExecution executions : plugin.getExecutions())
            {
                Xpp3Dom executionConfigurations = (Xpp3Dom) executions.getConfiguration();
                if (executionConfigurations != null)
                {
                    String found = Xpp3DomSearch.find(executionConfigurations, "mainClass");
                    if (found != null)
                    {
                        return found;
                    }
                }
            }
        }
        return null;
    }

    public String getClasspath()
    {
        return classpath;
    }

    private String getJavaReq()
    {
        if ("deb".equals(packageType))
        {
            String target = root.getProperties().getProperty("maven.compiler.target", "1.5");
            switch (target)
            {
                case "1.5":
                    return "java5-runtime";
                case "1.6":
                    return "java6-runtime";
                case "1.7":
                    return "java7-runtime";
                case "1.8":
                    return "java8-runtime";
                case "1.9":
                    return "java9-runtime";
                default:
                    warning("java version %s dependency name unknown");
                    return null;
            }
        }
        return null;
    }

}
