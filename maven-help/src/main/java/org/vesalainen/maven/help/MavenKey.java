/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.maven.help;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MavenKey implements Comparable<MavenKey>
{
    private String groupId;
    private String artifactId;
    private String version;
    private String type;
    private List<MavenKey> attachment;

    public MavenKey(Model model)
    {
        this(model.getGroupId(), model.getArtifactId(), model.getVersion(), "pom");
    }

    public MavenKey(Dependency dependency)
    {
        this(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(), dependency.getType());
    }

    public MavenKey(String groupId, String artifactId, String version, String type)
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
    }

    public void attach(Deque<MavenKey> stack)
    {
        attachment = new ArrayList<>();
        attachment.addAll(stack);
        Collections.reverse(attachment);
    }

    public List<MavenKey> getAttachment()
    {
        return attachment;
    }
    
    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public String getType()
    {
        return type;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.groupId);
        hash = 23 * hash + Objects.hashCode(this.artifactId);
        hash = 23 * hash + Objects.hashCode(this.version);
        hash = 23 * hash + Objects.hashCode(this.type);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final MavenKey other = (MavenKey) obj;
        if (!Objects.equals(this.groupId, other.groupId))
        {
            return false;
        }
        if (!Objects.equals(this.artifactId, other.artifactId))
        {
            return false;
        }
        if (!Objects.equals(this.version, other.version))
        {
            return false;
        }
        if (!Objects.equals(this.type, other.type))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return groupId + ":" + artifactId + ":" + version + ":" + type;
    }

    @Override
    public int compareTo(MavenKey o)
    {
        int cmp = groupId.compareTo(o.groupId);
        if (cmp != 0)
        {
            return cmp;
        }
        cmp = artifactId.compareTo(o.artifactId);
        if (cmp != 0)
        {
            return cmp;
        }
        cmp = version.compareTo(o.version);
        if (cmp != 0)
        {
            return cmp;
        }
        cmp = type.compareTo(o.type);
        if (cmp != 0)
        {
            return cmp;
        }
        return 0;
    }
    
}
