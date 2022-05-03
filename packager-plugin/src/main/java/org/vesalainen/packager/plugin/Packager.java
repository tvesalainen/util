package org.vesalainen.packager.plugin;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.vesalainen.bean.ExpressionParser;
import org.vesalainen.graph.Graphs;
import org.vesalainen.maven.help.POM;
import org.vesalainen.vfs.VirtualFileSystems;
import org.vesalainen.vfs.pm.Dependency;
import org.vesalainen.vfs.pm.FileUse;
import org.vesalainen.vfs.pm.PackageFileAttributes;
import org.vesalainen.vfs.pm.PackageFilenameFactory;
import org.vesalainen.vfs.pm.PackageManagerAttributeView;
import org.vesalainen.vfs.pm.deb.DEBDependency;

/**
 * Goal which touches a timestamp file.
 */
@Mojo( name = "pack"/*, defaultPhase = LifecyclePhase.COMPILE*/ )
public class Packager
    extends AbstractMojo
{
    private static final String SERVICE_TEMPLATE = "/etc/systemd/system/service.tmpl";
    private static final String OVERRIDE_TEMPLATE = "/etc/systemd/system/service.d/override.conf";
    private static final String LOG_CONFIG_TEMPLATE = "/etc/opt/log-config.tmpl";
    private POM root;
    private Path localRepository;
    private Path packageDirectory;
    private String classpath;
    private ExpressionParser expressionParser;
    private FileSystem fileSystem;
    @Parameter( defaultValue = "${project.build.directory}", name = "packageDir", required = true )
    private File packageDir;
    @Parameter( defaultValue = "deb", property = "pack.packageType", required = true )
    private String packageType;
    @Parameter( defaultValue = "${project.groupId}", property = "groupId", required = true )
    private String groupId;
    @Parameter( defaultValue = "${project.artifactId}", property = "artifactId", required = true )
    private String artifactId;
    @Parameter( defaultValue = "${project.version}", property = "version", required = true )
    private String version;
    @Parameter private boolean shortName;
    @Parameter private String maintainer;
    
    protected void execute(
            File packageDir,
            String packageType,
            String groupId,
            String artifactId,
            String version,
            boolean shortName,
            String maintainer
    ) throws MojoExecutionException
    {
        this.packageDir = packageDir;
        this.packageType = packageType;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.shortName = shortName;
        this.maintainer = maintainer;
        execute();
    }
    public void execute()
        throws MojoExecutionException
    {
        getLog().info("packager starting...");
        try
        {
            packageDirectory = packageDir.toPath();
            String userHome = System.getProperty("user.home");
            localRepository = Paths.get(userHome, ".m2/repository");    // TODO!!!!!!
            getLog().info("localRepository="+localRepository);
            getLog().info("packageDirectory="+packageDir);
            getLog().info("packageType="+packageType);
            getLog().info("groupId="+groupId);
            getLog().info("artifactId="+artifactId);
            getLog().info("version="+version);
            getLog().info("shortName="+shortName);
            root = POM.getInstance(groupId, artifactId, version);
            getLog().info("got factory");
            expressionParser = new ExpressionParser(root)
                    .addMapper((s)->root.getProperties().getProperty(s))
                    .addMapper(this);
            getLog().info("got expressionParser");
            createPackage();
        }
        catch (Throwable ex)
        {
            getLog().error(ex);
        }
    }
    private void createPackage() throws IOException
    {
        Path path = PackageFilenameFactory.getPath(packageDirectory, packageType, getPackage(), version, null);
        getLog().info("package="+path);
        Files.createFile(path);
        try (FileSystem pkgFS = VirtualFileSystems.debianFileSystem(path, Collections.EMPTY_MAP))
        {
            fileSystem = pkgFS;
            PackageManagerAttributeView view = PackageManagerAttributeView.from(pkgFS);
            view.setSummary(root.getName());
            getLog().info("summary="+root.getName());
            view.setDescription(root.getDescription());
            getLog().info("description="+root.getDescription());
            String javaReq = getJavaReq();
            if (javaReq != null)
            {
                view.addRequire(javaReq);
                getLog().info("require="+javaReq);
            }
            String license = getLicense(root);
            view.setLicense(license);
            getLog().info("license="+license);
            String developers = getDevelopers(root);
            view.setCopyright(developers);
            getLog().info("copyright="+developers);
            if (maintainer != null)
            {
                view.setMaintainer(maintainer);
                getLog().info("maintainer="+maintainer);
            }
            else
            {
                view.setMaintainer(developers);
            }
            view.setUrl(root.getUrl());
            getLog().info("url="+root.getUrl());
            copyJarsEtc(appDir(), view);
            Path systemdPath = systemdPath();
            Path overridePath = overridePath();
            createSystemdService(systemdPath, overridePath);
            view.setPostInstallation("systemctl enable "+getPackage()+"\nsystemctl start "+getPackage()+"\n");
            view.setPreUnInstallation("systemctl stop "+getPackage()+"\nsystemctl disable "+getPackage()+"\n");
            createLogConfig(getLogConfigPath());
            Files.createDirectories(getVarPath());
            fileSystem = null;
        }
    }
    private Path appDir()
    {
        return fileSystem.getPath("/opt/"+groupId+"/"+artifactId);
    }
    private Path systemdPath()
    {
        return fileSystem.getPath("/etc/systemd/system/"+getPackage()+".service");
    }
    private Path overridePath()
    {
        return fileSystem.getPath("/etc/systemd/system/"+getPackage()+".service.d/override.conf");
    }
    public Path getLogConfigPath()
    {
        return fileSystem.getPath("/etc/opt/"+getPackage()+"/log-config.xml");
    }
    public Path getConfigPath()
    {
        return fileSystem.getPath("/etc/opt/"+getPackage()+"/config.xml");
    }
    public Path getVarPath()
    {
        return fileSystem.getPath("/var/opt/"+getPackage());
    }
    public String getLsbDescription()
    {
        return root.getDescription().replace("\n", "\n#   ");
    }
    public String getGeneratedText()
    {
        return "generated by maven packager "+MyVersion.version()+" at "+ZonedDateTime.now();
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
    private void copyJarsEtc(Path optPackage, PackageManagerAttributeView view) throws IOException
    {
        Path jarDir = optPackage.resolve("jar");
        List<Path> jars = new ArrayList<>();
        List<POM> models = getDependencies().collect(Collectors.toList());
        for (POM model : models)
        {
            String grp = model.getGroupId();
            String art = model.getArtifactId();
            String ver = model.getVersion();
            String packaging = model.getPackaging();
            String filename = POM.getFilename(grp, art, ver, "jar");
            Path source = localRepository.resolve(filename);
            Path target = jarDir.resolve(localRepository.relativize(source));
            Files.createDirectories(target.getParent());
            Files.copy(source, target);
            getLog().info(target.toString());
            jars.add(target);
            Files.setPosixFilePermissions(target, PosixFilePermissions.fromString("rw-r--r--"));
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
    private String getDevelopers(POM model)
    {
        return model.getDevelopers().stream().filter((d)->!d.getName().isEmpty()).map((d)->d.getName()+" <"+d.getEmail()+">").collect(Collectors.joining(", "));
    }
    private String getLicense(POM model)
    {
        return model.getLicenses().stream().map((l)->l.getName()).collect(Collectors.joining(", "));
    }
    private void createSystemdService(Path systemdPath, Path overridePath) throws IOException
    {
        createFromResource(systemdPath, SERVICE_TEMPLATE, "rw-r--r--");
        createFromResource(overridePath, OVERRIDE_TEMPLATE, "rw-r--r--");
    }
    private void createLogConfig(Path logConfPath) throws IOException
    {
        createFromResource(logConfPath, LOG_CONFIG_TEMPLATE, "rw-r--r--");
    }

    private void createFromResource(Path path, String resource, String permissions) throws IOException
    {
        Files.createDirectories(path.getParent());
        try (   BufferedReader br = new BufferedReader(new InputStreamReader(Packager.class.getResourceAsStream(resource), UTF_8));
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
    }
    private Stream<String> getDependencyNames()
    {
        return getDependencies().map((m)->
        {
            String grp = m.getGroupId();
            String art = m.getArtifactId();
            String ver = m.getVersion();
            String packaging = m.getPackaging();
            return POM.getFilename(grp, art, ver, "jar");
        });
}
    private Stream<POM> getDependencies()
    {
        return Graphs.breadthFirst(root, 
                (y)->y.getDependencies()
                .stream()
                .filter((d)->!"true".equals(d.getOptional()))
                .filter((d)->"compile".equals(d.getScope()) || "runtime".equals(d.getScope()))
                .map(POM::getInstance)
                );
    }
    public String getMainJar()
    {
        Path appDir = appDir();
        Path jarDir = appDir.resolve("jar");
        String filename = POM.getFilename(groupId, artifactId, version, "jar");
        
        Path target = jarDir.resolve(filename);
        return target.toString();
    }
    public String getMainClass()
    {
        return root.getMainClass();
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
                    //warning("java version %s dependency name unknown");
                    return null;
            }
        }
        return null;
    }

}
