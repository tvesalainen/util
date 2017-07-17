/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.installer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
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
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.CmdArgs;

/**
 *
 * @author tkv
 */
public class Installer extends CmdArgs
{

    private ModelFactory factory;
    private Model root;
    private File localRepository;
    private File jarDirectory;
    private String groupId;
    private String artifactId;
    private String version;
    private FileModelResolver fileModelResolver;
    private Object defaultDirectory;
    private Object initDirectory;
    private String classpath;

    private enum Action {INSTALL, UPDATE, RUN};
    
    public Installer()
    {
        String localRepository = System.getProperty("localRepository");
        if (localRepository != null)
        {
            addOption("-lr", "Local Repository", null, new File(localRepository));
        }
        else
        {
            addOption(File.class, "-lr", "Local Repository");
        }
        addOption(File.class, "-jd", "Jar Directory", null, false);
        addOption("-ed", "Default Directory", null, new File("/etc/default"));
        addOption("-ei", "Init Directory", null, new File("/etc/init.d"));
        addOption("-g", "Group Id");
        addOption("-a", "Artifact Id");
        addOption("-v", "Version");
        addArgument(Action.class, "Action");
    }

    @Override
    public void command(String... args)
    {
        super.command(args);
        localRepository = getOption("-lr");
        jarDirectory = getOption("-jd");
        defaultDirectory = getOption("-ed");
        initDirectory = getOption("-ei");
        factory = new ModelFactory(localRepository, null);
        groupId = getOption("-g");
        artifactId = getOption("-a");
        version = getOption("-v");
        fileModelResolver = factory.getFileModelResolver();
        root = factory.getLocalModel(groupId, artifactId, version);
    }
    
    private void update() throws IOException, URISyntaxException
    {
        List<Path> jars = updateJars();
        classpath = jars.stream().map((p)->p.toString()).collect(Collectors.joining(";"));
        URL url = Installer.class.getResource("/etc/init.d/template");
        CharSequence initTemplate = CharSequences.getAsciiCharSequence(new File(url.toURI()));
        ExpressionParser parser = new ExpressionParser(this);
        String initContent = parser.replace(initTemplate);
        Properties prop = new Properties();
        try (InputStream is = Installer.class.getResourceAsStream("/etc/default/template"))
        {
            prop.load(is);
        }
        for (String key : prop.stringPropertyNames())
        {
            String property = prop.getProperty(key, "");
            property = parser.replace(property);
            prop.setProperty(key, property);
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
                    Files.copy(repo.toPath(), local.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
            return local.toPath();
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

    public String getClasspath()
    {
        return classpath;
    }

    public String getArtifactId()
    {
        return root.getArtifactId();
    }

    public String getDescription()
    {
        return root.getDescription();
    }

    public String getGroupId()
    {
        return root.getGroupId();
    }

    public String getInceptionYear()
    {
        return root.getInceptionYear();
    }

    public String getModelEncoding()
    {
        return root.getModelEncoding();
    }

    public String getModelVersion()
    {
        return root.getModelVersion();
    }

    public String getName()
    {
        return root.getName();
    }

    public String getPackaging()
    {
        return root.getPackaging();
    }

    public String getUrl()
    {
        return root.getUrl();
    }

    public String getVersion()
    {
        return root.getVersion();
    }

    public String getId()
    {
        return root.getId();
    }

    public static void main(String... args)
    {
        try
        {
            Installer installer = new Installer();
            installer.command(args);
            Action action = installer.getArgument("Action");
            switch (action)
            {
                case UPDATE:
                    installer.update();
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
