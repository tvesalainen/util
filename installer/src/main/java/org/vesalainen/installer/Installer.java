/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.installer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import org.apache.maven.model.Model;
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
    
    private void update()
    {
        File localRepository = getOption("-lr");
        File jarDirectory = getOption("-jd");
        ModelFactory factory = new ModelFactory(localRepository, null);
        FileModelResolver fileModelResolver = factory.getFileModelResolver();
        closure(factory).forEach((m)->
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
    private boolean needsUpdate(File repo, File local)
    {
        return true;
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
