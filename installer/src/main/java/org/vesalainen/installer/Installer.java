/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.installer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.vesalainen.graph.Graphs;
import org.vesalainen.test.pom.FileModelResolver;
import org.vesalainen.test.pom.MavenKey;
import org.vesalainen.test.pom.ModelFactory;
import org.vesalainen.test.pom.VersionResolver;
import org.vesalainen.util.CmdArgs;

/**
 *
 * @author tkv
 */
public class Installer extends CmdArgs
{

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
        addOption("-g", "Group Id");
        addOption("-a", "Artifact Id");
        addOption("-v", "Version");
        addArgument(Action.class, "Action");
    }
    
    private Stream<Path> update()
    {
        File localRepository = getOption("-lr");
        File jarDirectory = getOption("-jd");
        ModelFactory factory = new ModelFactory(localRepository, null);
        String mainClass = mainClass(factory);
        FileModelResolver fileModelResolver = factory.getFileModelResolver();
        return closure(factory).map((m)->
        {
            System.err.println(m);
            String groupId = m.getGroupId();
            String artifactId = m.getArtifactId();
            String version = m.getVersion();
            String packaging = m.getPackaging();
            String filename = fileModelResolver.getFilename(groupId, artifactId, version, "jar");
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
        });
    }
    
    private Stream<Model> closure(ModelFactory factory)
    {
        String groupId = getOption("-g");
        String artifactId = getOption("-a");
        String version = getOption("-v");
        Model root = factory.getLocalModel(groupId, artifactId, version);
        return Graphs.breadthFirst(root, 
                (y)->y.getDependencies()
                .stream()
                .filter((d)->!"true".equals(d.getOptional()))
                .peek((d)->System.err.println(d))
                .filter((d)->"compile".equals(d.getScope()) || "runtime".equals(d.getScope()))
                .map(factory::getVersionResolver)
                .map((v)->v.resolv())
                .map(factory::getLocalModel)
        );
    }
    private String mainClass(ModelFactory factory)
    {
        String groupId = getOption("-g");
        String artifactId = getOption("-a");
        String version = getOption("-v");
        Model root = factory.getLocalModel(groupId, artifactId, version);
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

    public static void main(String... args)
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
}
