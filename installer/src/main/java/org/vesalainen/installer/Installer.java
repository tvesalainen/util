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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import static java.nio.charset.StandardCharsets.*;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.vesalainen.bean.ExpressionParser;
import org.vesalainen.graph.Graphs;
import org.vesalainen.loader.LibraryLoader;
import org.vesalainen.loader.LibraryLoader.OS;
import static org.vesalainen.loader.LibraryLoader.OS.*;
import org.vesalainen.nio.FileUtil;
import org.vesalainen.test.pom.FileModelResolver;
import org.vesalainen.test.pom.ModelFactory;
import org.vesalainen.test.pom.Version;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.LoggingCommandLine;
import org.vesalainen.util.OSProcess;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Installer extends LoggingCommandLine
{
    private static final String TEMPLATE_PATH = "org/vesalainen/installer/template";
    private static final String DEFAULT_TEMPLATE = "/etc/default/default.tmpl";
    private static final String INIT_D_TEMPLATE = "/etc/init.d/init.tmpl";
    private static final String USR_LOCAL_BIN_TEMPLATE = "/usr/local/bin/exe.tmpl";
    private static final String USR_LOCAL_BIN_SCRIPT = "/usr/local/bin/script.tmpl";
    private static final String BIN_TEMPLATE = "/bin/win.tmpl";
    private ModelFactory factory;
    private Model root;
    private File localRepository;
    private File jarDirectory;
    private String groupId;
    private String artifactId;
    private String version;
    private FileModelResolver fileModelResolver;
    private File defaultDirectory;
    private File initDirectory;
    private String classpath;
    private ExpressionParser expressionParser;
    private File exeDirectory;
    private int jmxPort;

    private enum Action {CLIENT, SERVER, SCRIPT, REMOVE};
    
    public Installer()
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
        addOption("-jd", "Jar Directory", null, new File("/usr/local/lib"));
        addOption("-ed", "Executive Directory", "client", new File("/usr/local/bin"));
        addOption("-dd", "Default Directory", "server", new File("/etc/default"));
        addOption("-id", "Init Directory", "server", new File("/etc/init.d"));
        addOption("-ss", "Self Install Script", "self", new File("maven-installer.sh"));
        addOption("-jp", "JMX Port", "server", -1);
        addOption("-g", "Group Id");
        addOption("-a", "Artifact Id");
        addOption(String.class, "-v", "Version", null, false);
        addArgument(Action.class, "Action");
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
        jarDirectory = getOption("-jd");
        config("jarDirectory=%s", jarDirectory);
        exeDirectory = getOption("-ed");
        config("exeDirectory=%s", exeDirectory);
        defaultDirectory = getOption("-dd");
        config("defaultDirectory=%s", defaultDirectory);
        initDirectory = getOption("-id");
        config("initDirectory=%s", initDirectory);
        factory = new ModelFactory(localRepository, null);
        fileModelResolver = factory.getFileModelResolver();
        jmxPort = getOption("-jp");
        config("jmxPort=%d", jmxPort);
        groupId = getOption("-g");
        config("groupId=%s", groupId);
        artifactId = getOption("-a");
        config("artifactId=%s", artifactId);
        version = getOption("-v");
        config("version=%s", version);
        if (version == null)
        {
            List<Version> versions = fileModelResolver.getVersions(groupId, artifactId);
            if (versions == null || versions.isEmpty())
            {
                throw new IllegalArgumentException("no version for "+groupId+"."+artifactId);
            }
            version = versions.get(versions.size()-1).toString();
        }
        root = factory.getLocalModel(groupId, artifactId, version);
        expressionParser = new ExpressionParser(root)
                            .addMapper((s)->root.getProperties().getProperty(s))
                            .addMapper(this);
    }
    
    private void createInstallScript() throws IOException
    {
        classpath = getDependencyNames().map((s)->"$JAR/"+s).collect(Collectors.joining(":"));
        File installFile = getOption("-ss");
        try (   BufferedWriter bw = Files.newBufferedWriter(installFile.toPath(), US_ASCII);
                InputStream is = templateStream(USR_LOCAL_BIN_SCRIPT))
        {
            bw.append("#! /bin/sh").append('\n');
            
            bw.append("REPO=/mnt/m2/repository\n");
            bw.append("JAR=/usr/local/lib\n");
            bw.append("EXE=/usr/local/bin\n");
            
            bw.append("echo >").append("$EXE/").append(artifactId).append('\n');
            FileUtil.lines(is).forEach((l)->
            {
                try
                {
                    l = expressionParser.replace(l);
                    bw.append("echo \"").append(l).append("\" >>").append("$EXE/").append(artifactId).append('\n');
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            });
            bw.append("chmod 744 ").append("$EXE/").append(artifactId).append('\n');
            bw.append("chown root ").append("$EXE/").append(artifactId).append('\n');
            getDependencyNames().forEach((n)->
            {
                try
                {
                    String d = n.substring(0, n.lastIndexOf('/'));
                    bw.append("mkdir -p $JAR/").append(d).append('\n');
                    bw.append("cp $REPO/").append(n).append(" $JAR/").append(n).append('\n');
                    bw.append("chmod 644").append(" $JAR/").append(n).append('\n');
                    bw.append("chown root").append(" $JAR/").append(n).append('\n');
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            });
        }
        info("created %s", installFile);
    }

    private void installLinuxClient() throws IOException
    {
        classpath = updateJars().map((p)->p.toString()).collect(Collectors.joining(":"));
        File exe = new File(exeDirectory, artifactId);
        mergeTemplate(exe, USR_LOCAL_BIN_TEMPLATE);
        setPosixFilePermissions(exe.toPath(), "rwxr--r--");
    }

    private void installWindowsClient() throws IOException
    {
        classpath = updateJars().map((p)->p.toString()).collect(Collectors.joining(File.pathSeparator));
        mergeTemplate(new File(exeDirectory, artifactId.toUpperCase()+".BAT"), BIN_TEMPLATE);
    }

    private void installLinuxServer() throws IOException, URISyntaxException, InterruptedException
    {
        classpath = updateJars().map((p)->p.toString()).collect(Collectors.joining(File.pathSeparator));
        File init = new File(initDirectory, artifactId);
        etcInitD(init);
        File def = new File(defaultDirectory, artifactId);
        mergeTemplate(def, DEFAULT_TEMPLATE);
        setPosixFilePermissions(init.toPath(), "rwxr-xr-x");
        setPosixFilePermissions(def.toPath(), "rw-r--r--");
        call("maven_installer_start");
    }
    private void removeLinuxServer() throws IOException, InterruptedException
    {
        Path init = new File(initDirectory, artifactId).toPath();
        Files.deleteIfExists(init);
        call("maven_installer_stop");
    }

    private void setPosixFilePermissions(Path path, String posixPermissions) throws IOException
    {
        try
        {
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(posixPermissions));
            finest("posix %s -> %s", posixPermissions, path);
        }
        catch (UnsupportedOperationException ex)
        {
            warning("setPosixFilePermissions not supported");
        }
    }
    private void mergeTemplate(File def, String templatePath) throws IOException
    {
        if (def.exists())
        {
            Map<String,String> map = new HashMap<>();
            try (InputStream is = templateStream(templatePath))
            {
                FileUtil.lines(is, US_ASCII).forEach((l)->
                {
                    String line = expressionParser.replace(l);
                    if (!l.equals(line))
                    {
                        CharSequence key = CharSequences.split(l, '=').findFirst().get();
                        map.put(key.toString(), line);
                    }
                });
            }
            List<String> list = Files.readAllLines(def.toPath(), US_ASCII);
            try (BufferedWriter bw = Files.newBufferedWriter(def.toPath(), US_ASCII))
            {
                for (String line : list)
                {
                    CharSequence key = CharSequences.split(line, '=').findFirst().orElse(null);
                    String l = map.get(key);
                    if (l != null)
                    {
                        bw.append(l);
                    }
                    else
                    {
                        bw.append(line);
                    }
                    bw.newLine();
                }
            }
            info("updated %s", def);
        }
        else
        {
            try (InputStream is = templateStream(templatePath);
                    BufferedWriter bw = Files.newBufferedWriter(def.toPath(), US_ASCII))
            {
                FileUtil.lines(is, US_ASCII).forEach((l)->
                {
                    try
                    {
                        String line = expressionParser.replace(l);
                        bw.append(line);
                        bw.newLine();
                    }
                    catch (IOException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                });
            }
            info("created %s", def);
        }
    }
    private void etcInitD(File init) throws IOException
    {
        byte[] initBuf = null;
        try (InputStream is = templateStream(INIT_D_TEMPLATE))
        {
            initBuf = FileUtil.readAllBytes(is);
        }
        CharSequence initTemplate = CharSequences.getAsciiCharSequence(initBuf);
        String initContent = expressionParser.replace(initTemplate);
        try (BufferedWriter bf = Files.newBufferedWriter(init.toPath(), US_ASCII))
        {
            bf.append(initContent);
        }
        info("created %s", init);
    }
    private void call(String template) throws IOException, InterruptedException
    {
        Path path = installTemplate('/'+template, exeDirectory, "rwxr--r--");
        OSProcess.call(template, artifactId);
    }
    private InputStream templateStream(String template) throws IOException
    {
        Path path = installTemplate(template, new File(jarDirectory, TEMPLATE_PATH), "rw-r--r--");
        InputStream is = Files.newInputStream(path);
        return FileUtil.buffer(is);
    }
    private Path installTemplate(String template, File targetDir, String permissions) throws IOException
    {
        Path target = new File(targetDir, template).toPath();
        if (!Files.exists(target))
        {
            try (InputStream is = Installer.class.getResourceAsStream(template))
            {
                Files.createDirectories(target.getParent());
                Files.copy(is, target);
                info("install %s -> %s", template, target);
                setPosixFilePermissions(target, permissions);
            }
        }
        return target;
    }
    private Stream<Path> updateJars()
    {
        return getDependencyNames().map((filename)->
        {
            File repo = new File(localRepository, filename);
            File local = new File(jarDirectory, filename);
            if (needsUpdate(repo, local))
            {
                try
                {
                    Files.createDirectories(local.toPath().getParent());
                    Files.copy(repo.toPath(), local.toPath(), REPLACE_EXISTING);
                    info("updated %s -> %s", repo, local);
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
            return local.toPath().toAbsolutePath();
            });
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
        List<Dependency> dependencies = root.getDependencies();
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
    public String getMainClass()
    {
        Build build = root.getBuild();
        Map<String, Plugin> pluginsAsMap = build.getPluginsAsMap();
        Plugin assemblyPlugin = pluginsAsMap.get("org.apache.maven.plugins:maven-assembly-plugin");
        Objects.requireNonNull(assemblyPlugin, root+" doesn't have main class in org.apache.maven.plugins:maven-assembly-plugin");
        Map<String, PluginExecution> executionsAsMap = assemblyPlugin.getExecutionsAsMap();
        PluginExecution createExecutableJar = executionsAsMap.get("create-executable-jar");
        Objects.requireNonNull(createExecutableJar, root+" doesn't have create-executable-jar in org.apache.maven.plugins:maven-assembly-plugin");
        Xpp3Dom configuration = (Xpp3Dom) createExecutableJar.getConfiguration();
        Objects.requireNonNull(configuration, root+" doesn't have configuration in org.apache.maven.plugins:maven-assembly-plugin");
        Xpp3Dom archive = configuration.getChild("archive");
        Objects.requireNonNull(archive, root+" doesn't have archive in org.apache.maven.plugins:maven-assembly-plugin");
        Xpp3Dom manifest = archive.getChild("manifest");
        Objects.requireNonNull(manifest, root+" doesn't have manifest in org.apache.maven.plugins:maven-assembly-plugin");
        Xpp3Dom mainClass = manifest.getChild("mainClass");
        Objects.requireNonNull(mainClass, root+" doesn't have mainClass in org.apache.maven.plugins:maven-assembly-plugin");
        return mainClass.getValue();
    }
    private boolean needsUpdate(File repo, File local)
    {
        return !local.exists() || repo.lastModified() > local.lastModified();
    }

    public String getJmxSettings()
    {
        if (jmxPort != -1)
        {
            return String.format("-Dcom.sun.management.jmxremote " +
            "-Dcom.sun.management.jmxremote.port=%d " +
            "-Dcom.sun.management.jmxremote.local.only=false " +
            "-Dcom.sun.management.jmxremote.authenticate=false " +
            "-Dcom.sun.management.jmxremote.ssl=false", jmxPort);
        }
        else
        {
            return "";
        }
    }
    public String getClasspath()
    {
        return classpath;
    }

    static void install(String... args) throws IOException, URISyntaxException, InterruptedException
    {
        Installer installer = new Installer();
        installer.command(args);
        Action action = installer.getArgument("Action");
        OS os = LibraryLoader.getOS();
        switch (action)
        {
            case SERVER:
                switch (os)
                {
                    case Linux:
                        installer.installLinuxServer();
                        break;
                    default:
                        throw new IllegalArgumentException(os+" not supported");
                }
                break;
            case REMOVE:
                switch (os)
                {
                    case Linux:
                        installer.removeLinuxServer();
                        break;
                    default:
                        throw new IllegalArgumentException(os+" not supported");
                }
                break;
            case CLIENT:
                switch (os)
                {
                    case Linux:
                        installer.installLinuxClient();
                        break;
                    case Windows:
                        installer.installWindowsClient();
                        break;
                    default:
                        throw new IllegalArgumentException(os+" not supported");
                }
                break;
            case SCRIPT:
                installer.createInstallScript();
                break;
            default:
                System.err.println(action+" not supported yet.");
                System.exit(-1);
        }
    }
    public static void main(String... args)
    {
        try
        {
            install(args);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
