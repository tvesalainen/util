/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.model.Build;
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
import org.vesalainen.util.CmdArgs;

/**
 *
 * @author tkv
 */
public class Installer extends CmdArgs
{
    private static final String DEFAULT_TEMPLATE = "/etc/default/template";
    private static final String INIT_D_TEMPLATE = "/etc/init.d/template";
    private static final String USR_LOCAL_BIN_TEMPLATE = "/usr/local/bin/template";
    private static final String BIN_TEMPLATE = "/bin/template";
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

    private enum Action {CLIENT, SERVER};
    
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
        addOption("-jp", "JMX Port", "server", -1);
        addOption("-g", "Group Id");
        addOption("-a", "Artifact Id");
        addOption(String.class, "-v", "Version", null, false);
        addArgument(Action.class, "Action");
    }

    @Override
    public void command(String... args)
    {
        super.command(args);
        localRepository = getOption("-lr");
        jarDirectory = getOption("-jd");
        exeDirectory = getOption("-ed");
        defaultDirectory = getOption("-dd");
        initDirectory = getOption("-id");
        factory = new ModelFactory(localRepository, null);
        fileModelResolver = factory.getFileModelResolver();
        jmxPort = getOption("-jp");
        groupId = getOption("-g");
        artifactId = getOption("-a");
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
        root = factory.getLocalModel(groupId, artifactId, version);
        expressionParser = new ExpressionParser(root)
                            .addMapper((s)->root.getProperties().getProperty(s))
                            .addMapper(this);
    }
    
    private void installLinuxClient() throws IOException
    {
        classpath = updateJars().stream().map((p)->p.toString()).collect(Collectors.joining(":"));
        File exe = new File(exeDirectory, artifactId);
        mergeTemplate(exe, USR_LOCAL_BIN_TEMPLATE);
        setPosixFilePermissions(exe, "rwxr--r--");
    }

    private void installWindowsClient() throws IOException
    {
        classpath = updateJars().stream().map((p)->p.toString()).collect(Collectors.joining(File.pathSeparator));
        mergeTemplate(new File(exeDirectory, artifactId.toUpperCase()+".BAT"), BIN_TEMPLATE);
    }

    private void installLinuxServer() throws IOException, URISyntaxException
    {
        classpath = updateJars().stream().map((p)->p.toString()).collect(Collectors.joining(File.pathSeparator));
        File init = new File(initDirectory, artifactId);
        etcInitD(init);
        File def = new File(defaultDirectory, artifactId);
        mergeTemplate(def, DEFAULT_TEMPLATE);
        setPosixFilePermissions(init, "rw-r--r--");
        setPosixFilePermissions(def, "rw-r--r--");
    }
    private void setPosixFilePermissions(File file, String posixPermissions) throws IOException
    {
        try
        {
            Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString(posixPermissions));
        }
        catch (UnsupportedOperationException ex)
        {
            System.err.println("setPosixFilePermissions not supported");
        }
    }
    private void mergeTemplate(File def, String templatePath) throws IOException
    {
        if (def.exists())
        {
            Map<String,String> map = new HashMap<>();
            try (InputStream is = Installer.class.getResourceAsStream(templatePath))
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
                    CharSequence key = CharSequences.split(line, '=').findFirst().get();
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
        }
        else
        {
            try (InputStream is = Installer.class.getResourceAsStream(templatePath);
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
        }
    }
    private void etcInitD(File init) throws IOException
    {
        byte[] initBuf = null;
        try (InputStream is = Installer.class.getResourceAsStream("/etc/init.d/template"))
        {
            initBuf = FileUtil.readAllBytes(is);
        }
        CharSequence initTemplate = CharSequences.getAsciiCharSequence(initBuf);
        String initContent = expressionParser.replace(initTemplate);
        try (BufferedWriter bf = Files.newBufferedWriter(init.toPath(), US_ASCII))
        {
            bf.append(initContent);
        }
    }
    private List<Path> updateJars()
    {
        return getDependencies().stream().map((m)->
        {
            System.err.println(m);
            String grp = m.getGroupId();
            String art = m.getArtifactId();
            String ver = m.getVersion();
            String packaging = m.getPackaging();
            String filename = fileModelResolver.getFilename(grp, art, ver, "jar");
            File repo = new File(localRepository, filename);
            File local = new File(jarDirectory, filename);
            if (needsUpdate(repo, local))
            {
                try
                {
                    Files.createDirectories(local.toPath().getParent());
                    Files.copy(repo.toPath(), local.toPath(), REPLACE_EXISTING);
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
            return local.toPath().toAbsolutePath();
            })
            .collect(Collectors.toList());
    }
    
    private List<Model> getDependencies()
    {
        return Graphs.breadthFirst(root, 
                (y)->y.getDependencies()
                .stream()
                .filter((d)->!"true".equals(d.getOptional()))
                .peek((d)->System.err.println(d))
                .filter((d)->"compile".equals(d.getScope()) || "runtime".equals(d.getScope()))
                .map(factory::getVersionResolver)
                .map((v)->v.resolv())
                .map(factory::getLocalModel)
                )
                .collect(Collectors.toList());
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

    public static void main(String... args)
    {
        try
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
                        case Windows:   // TODO remove this line
                            installer.installLinuxServer();
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
                            installer.installLinuxClient();
                            break;
                        default:
                            throw new IllegalArgumentException(os+" not supported");
                    }
                    break;
                default:
                    System.err.println(action+" not supported yet.");
                    System.exit(-1);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
