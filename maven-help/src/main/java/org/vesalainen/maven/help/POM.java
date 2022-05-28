/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.maven.help;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Developer;
import org.apache.maven.model.License;
import org.vesalainen.bean.ExpressionParser;
import org.vesalainen.regex.SyntaxErrorException;
import org.vesalainen.xml.SimpleXMLParser;
import org.vesalainen.xml.SimpleXMLParser.Element;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class POM
{
    private static Path base;
    private static Map<Path,POM> pomCache = new HashMap<>();
    private static Map<ArtifactKey,POM> artifactCache = new HashMap<>();
    private static Map<ArtifactKey,VersionResolver> modelVersionMap = new HashMap<>();

    private final Element element;
    private final POM parent;
    private final Properties properties;
    private final Map<String,Dependency> dependencyManagement = new HashMap<>();
    private final List<Dependency> dependencies = new ArrayList<>();
    private final ExpressionParser expressionParser;
    private final List<License> licenses = new ArrayList<>();
    private final List<Developer> developers = new ArrayList<>();
 
    static
    {
        String userHome = System.getProperty("user.home");
        base = Paths.get(userHome, ".m2/repository");    // TODO!!!!!!
    }
    
    private POM(Element element)
    {
        this.element = element;
        String parentGroupId = element.getText("parent", "groupId");
        String parentArtifactId = element.getText("parent", "artifactId");
        String parentVersion = element.getText("parent", "version");
        if (parentGroupId != null || parentArtifactId != null ||  parentVersion != null)
        {
            parent = getInstance(parentGroupId, parentArtifactId, parentVersion);
        }
        else
        {
            parent = null;
        }
        properties = new Properties(parent != null ? parent.properties : null);
        expressionParser = new ExpressionParser(this)
                .addMapper((s)->properties.getProperty(s));
        Element props = element.getElement("properties");
        if (props != null)
        {
            props.forEachChild((e)->
            {
                properties.setProperty(e.getTag(), expressionParser.replace(e.getText()));
            });
        }
        
        element.getElements("dependencyManagement", "dependencies", "dependency")
                .forEach((e)->
                {
                    Dependency dependency = createDependency(e);
                    dependencyManagement.put(dependency.getManagementKey(), dependency);
                });
        element.getElements("dependencies", "dependency")
                .forEach((e)->
                {
                    Dependency dependency = createDependency(e);
                    Dependency managementDependency = getManagementDependency(dependency.getManagementKey());
                    if (managementDependency != null && dependency.getVersion() == null)
                    {
                        dependency.setVersion(managementDependency.getVersion());
                    }
                    dependencies.add(dependency);
                });
        element.getElements("licenses", "license")
                .forEach((e)->
                {
                    License license = new License();
                    license.setName(expressionParser.replace(e.getText("name")));
                    license.setComments(expressionParser.replace(e.getText("comment")));
                    license.setDistribution(expressionParser.replace(e.getText("distribution")));
                    license.setUrl(expressionParser.replace(e.getText("url")));
                    licenses.add(license);
                });
        element.getElements("developers", "developer")
                .forEach((e)->
                {
                    Developer developer = new Developer();
                    developer.setName(expressionParser.replace(e.getText("name")));
                    developer.setEmail(expressionParser.replace(e.getText("email")));
                    developer.setOrganization(expressionParser.replace(e.getText("organization")));
                    developer.setUrl(expressionParser.replace(e.getText("url")));
                    developers.add(developer);
                });
    }

    public String getMainClass()
    {
        Collection<Element> elements = element.getElements("build", "plugins", "plugin", "configuration", "archive", "manifest", "mainClass");
        if (elements.size() == 1)
        {
            return elements.iterator().next().getText();
        }
        else
        {
            if (elements.size() == 0)
            {
                throw new IllegalArgumentException("mainClass not defined");
            }
            else
            {
                throw new IllegalArgumentException("more than one mainClass defined");
            }
        }
    }
    public List<Dependency> getDependencies()
    {
        return dependencies;
    }
    private Dependency getManagementDependency(String managementKey)
    {
        Dependency dependency = dependencyManagement.get(managementKey);
        if (dependency != null)
        {
            return dependency;
        }
        else
        {
            if (parent != null)
            {
                return parent.getManagementDependency(managementKey);
            }
            else
            {
                return null;
            }
        }
    }
    private Dependency createDependency(Element element)
    {
        Dependency dependency = new Dependency();
        if (element.getElement("exclusions") != null)
        {
            //throw new UnsupportedOperationException("exclusions not supported");
        }
        dependency.setArtifactId(expressionParser.replace(element.getText("artifactId")));
        dependency.setGroupId(expressionParser.replace(element.getText("groupId")));
        dependency.setClassifier(expressionParser.replace(element.getText("classifier")));
        String optional = element.getText("optional");
        dependency.setOptional(expressionParser.replace(optional!=null?optional:"false"));
        String scope = element.getText("scope");
        dependency.setScope(expressionParser.replace(scope!=null?scope:"compile"));
        dependency.setSystemPath(expressionParser.replace(element.getText("systemPath")));
        String type = element.getText("type");
        dependency.setType(expressionParser.replace(type!=null?type:"jar"));
        dependency.setVersion(expressionParser.replace(element.getText("version")));
        return dependency;
    }
    public Properties getProperties()
    {
        return properties;
    }

    public List<Developer> getDevelopers()
    {
        if (!developers.isEmpty() || parent == null)
        {
            return developers;
        }
        else
        {
            return parent.getDevelopers();
        }
    }
    
    public List<License> getLicenses()
    {
        if (!licenses.isEmpty() || parent == null)
        {
            return licenses;
        }
        else
        {
            return parent.getLicenses();
        }
    }
    
    public String getVersion()
    {
        return getText("version");
    }

    public String getUrl()
    {
        return getText("url");
    }

    public String getPackaging()
    {
        return getText("packaging");
    }

    public String getName()
    {
        return getText("name");
    }

    public String getModelVersion()
    {
        return getText("modelVersion");
    }

    public String getGroupId()
    {
        return getText("groupId");
    }

    public String getDescription()
    {
        return getText("description");
    }

    public String getArtifactId()
    {
        return getText("artifactId");
    }
    
    public String getText(String... tags)
    {
        String text = element.getText(tags);
        if (text == null && parent != null)
        {
            text = parent.getText(tags);
        }
        return expressionParser.replace(text);
    }
    
    public static VersionResolver getVersionResolver(Dependency dependency)
    {
        return getVersionResolver(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(), dependency.getType());
    }
    public static VersionResolver getVersionResolver(String groupId, String artifactId, String version, String type)
    {
        VersionRange versionRange = VersionParser.VERSION_PARSER.parseVersionRange(version);
        ArtifactKey key = new ArtifactKey(groupId, artifactId, type);
        VersionResolver versionResolver = modelVersionMap.get(key);
        if (versionResolver == null)
        {
            versionResolver = new VersionResolver(key, versionRange, POM.getVersions(groupId, artifactId));
            modelVersionMap.put(key, versionResolver);
        }
        versionResolver.addRange(versionRange);
        return versionResolver;
    }
    
    public static POM getInstance(Dependency key)
    {
        return getInstance(key.getGroupId(), key.getArtifactId(), key.getVersion());
    }
    public static POM getInstance(String groupId, String artifactId, String version)
    {
        VersionResolver versionResolver = getVersionResolver(groupId, artifactId, version, "pom");
        MavenKey key = versionResolver.resolv();
        Path path = base.resolve(getFilename(key.getGroupId(), key.getArtifactId(), key.getVersion(), "pom"));
        POM pom = pomCache.get(path);
        if (pom == null)
        {
            try 
            {
                SimpleXMLParser parser = new SimpleXMLParser(path);
                pom = new POM(parser.getRoot());
                pomCache.put(path, pom);
                ArtifactKey artifactKey = new ArtifactKey(groupId, artifactId, "pom");
                POM old = artifactCache.put(artifactKey, pom);
                if (old != null)
                {
                    throw new UnsupportedOperationException("conflict between "+old+" <> "+pom);
                }
            }
            catch (IOException ex) 
            {
                throw new IllegalArgumentException(groupId+"."+artifactId+"."+version);
            }
        }
        return pom;
    }
    
    public static List<Version> getVersions(String groupId, String artifactId)
    {
        String directoryName = getDirectory(groupId, artifactId);
        File dir = new File(base.toFile(), directoryName);
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

    public static String getFilename(String groupId, String artifactId, String version, String pkg)
    {
        return String.format("%s/%s/%s/%s-%s.%s", groupId.replace('.', '/'), artifactId, version, artifactId, version, pkg);
    }
    public static String getDirectory(String groupId, String artifactId)
    {
        return String.format("%s/%s", groupId.replace('.', '/'), artifactId);
    }

    @Override
    public String toString()
    {
        return "POM{" + getGroupId()+'-'+getArtifactId()+"-"+getVersion();
    }
    
}
