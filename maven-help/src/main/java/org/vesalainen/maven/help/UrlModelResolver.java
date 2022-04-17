/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.maven.help;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.building.UrlModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class UrlModelResolver extends AbstractModelResolver
{
    private final String base;

    public UrlModelResolver()
    {
        this.base = "https://search.maven.org/remotecontent?filepath=";
    }

    public UrlModelResolver(String base)
    {
        this.base = base;
    }
    
    @Override
    public ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException
    {
        try
        {
            String filename = getFilename(groupId, artifactId, version, "pom");
            URL url = new URL(base+filename);
            return new UrlModelSource(url);
        }
        catch (MalformedURLException ex)
        {
            throw new UnresolvableModelException(ex.getMessage(), groupId, artifactId, version, ex);
        }
    }

    @Override
    public ModelResolver newCopy()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ModelSource resolveModel(Parent parent) throws UnresolvableModelException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addRepository(Repository rpstr, boolean bln) throws InvalidRepositoryException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ModelSource resolveModel(Dependency dependency) throws UnresolvableModelException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
