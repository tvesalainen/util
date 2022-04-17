/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.maven.help;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.DefaultModelProcessor;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.composition.DefaultDependencyManagementImporter;
import org.apache.maven.model.inheritance.DefaultInheritanceAssembler;
import org.apache.maven.model.interpolation.DefaultModelVersionProcessor;
import org.apache.maven.model.interpolation.StringSearchModelInterpolator;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.management.DefaultDependencyManagementInjector;
import org.apache.maven.model.management.DefaultPluginManagementInjector;
import org.apache.maven.model.normalization.DefaultModelNormalizer;
import org.apache.maven.model.path.DefaultModelPathTranslator;
import org.apache.maven.model.path.DefaultModelUrlNormalizer;
import org.apache.maven.model.path.DefaultPathTranslator;
import org.apache.maven.model.path.DefaultUrlNormalizer;
import org.apache.maven.model.plugin.DefaultPluginConfigurationExpander;
import org.apache.maven.model.plugin.DefaultReportConfigurationExpander;
import org.apache.maven.model.plugin.DefaultReportingConverter;
import org.apache.maven.model.profile.DefaultProfileInjector;
import org.apache.maven.model.profile.DefaultProfileSelector;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.model.superpom.DefaultSuperPomProvider;
import org.apache.maven.model.validation.DefaultModelValidator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ModelFactory
{
    private final FileModelResolver fileModelResolver;
    private final UrlModelResolver urlModelResolver;
    private final Map<MavenKey,Model> globalMap = new HashMap<>();
    private final Map<MavenKey,Model> localMap = new HashMap<>();
    private final Map<ArtifactKey,VersionResolver> modelVersionMap = new HashMap<>();

    public ModelFactory()
    {
        this.urlModelResolver = new UrlModelResolver();
        this.fileModelResolver = new FileModelResolver();
    }

    public ModelFactory(File localRepository, String url)
    {
        this.urlModelResolver = new UrlModelResolver(url);
        this.fileModelResolver = new FileModelResolver(localRepository);
    }

    public FileModelResolver getFileModelResolver()
    {
        return fileModelResolver;
    }

    public UrlModelResolver getUrlModelResolver()
    {
        return urlModelResolver;
    }
    
    public VersionResolver getVersionResolver(Dependency dependency)
    {
        VersionRange versionRange = VersionParser.VERSION_PARSER.parseVersionRange(dependency.getVersion());
        ArtifactKey key = new ArtifactKey(dependency);
        VersionResolver versionResolver = modelVersionMap.get(key);
        if (versionResolver == null)
        {
            versionResolver = new VersionResolver(key, versionRange, fileModelResolver.getVersions(dependency.getGroupId(), dependency.getArtifactId()));
            modelVersionMap.put(key, versionResolver);
        }
        versionResolver.addRange(versionRange);
        return versionResolver;
    }
    
    public Model getGlobalModel(Dependency dependency)
    {
        return getGlobalModel(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    }
    public Model getLocalModel(Dependency dependency)
    {
        return getLocalModel(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    }
    public Model getGlobalModel(String groupId, String artifactId, String version)
    {
        return getGlobalModel(new MavenKey(groupId, artifactId, version, "pom"));
    }
    public Model getLocalModel(String groupId, String artifactId, String version)
    {
        return getLocalModel(new MavenKey(groupId, artifactId, version, "pom"));
    }
    public Model getGlobalModel(MavenKey key)
    {
        Model model = globalMap.get(key);
        if (model == null)
        {
            ModelSource modelSource;
            try 
            {
                modelSource = urlModelResolver.resolveModel(key.getGroupId(), key.getArtifactId(), key.getVersion());
            }
            catch (UnresolvableModelException ex) 
            {
                throw new RuntimeException(ex);
            }
            model = getGlobalModel(modelSource);
            globalMap.put(key, model);
        }
        return model;
    }
    public Model getLocalModel(MavenKey key)
    {
        Model model = localMap.get(key);
        if (model == null)
        {
            ModelSource modelSource;
            try 
            {
                modelSource = fileModelResolver.resolveModel(key.getGroupId(), key.getArtifactId(), key.getVersion());
            }
            catch (UnresolvableModelException ex) 
            {
                throw new RuntimeException(ex);
            }
            model = getLocalModel(modelSource);
            localMap.put(key, model);
        }
        return model;
    }
    private Model getGlobalModel(ModelSource modelSource)
    {
        return getModel(modelSource, urlModelResolver);
    }
    private Model getLocalModel(ModelSource modelSource)
    {
        return getModel(modelSource, fileModelResolver);
    }
    private Model getModel(ModelSource modelSource, ModelResolver modelResolver)
    {
        ModelBuildingRequest req = new DefaultModelBuildingRequest();
        req.setProcessPlugins(false);
        req.setModelSource(modelSource);
        req.setModelResolver(modelResolver);
        req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);

        DefaultModelProcessor modelProcessor = new DefaultModelProcessor()
                .setModelReader(new DefaultModelReader());
        DefaultSuperPomProvider superPomProvider = new DefaultSuperPomProvider()
                .setModelProcessor(modelProcessor);
        DefaultPathTranslator pathTranslator = new DefaultPathTranslator();
        DefaultUrlNormalizer urlNormalizer = new DefaultUrlNormalizer();
        StringSearchModelInterpolator stringSearchModelInterpolator = (StringSearchModelInterpolator) new StringSearchModelInterpolator()
                .setPathTranslator(pathTranslator)
                .setUrlNormalizer(urlNormalizer);
        DefaultModelUrlNormalizer modelUrlNormalizer = new DefaultModelUrlNormalizer()
                .setUrlNormalizer(urlNormalizer);
        DefaultModelPathTranslator modelPathTranslator = new DefaultModelPathTranslator()
                .setPathTranslator(pathTranslator);
        ModelBuilder builder = new DefaultModelBuilder()
                .setDependencyManagementImporter(new DefaultDependencyManagementImporter())
                .setDependencyManagementInjector(new DefaultDependencyManagementInjector())
                .setInheritanceAssembler(new DefaultInheritanceAssembler())
                .setModelNormalizer(new DefaultModelNormalizer())
                .setModelPathTranslator(modelPathTranslator)
                .setModelProcessor(modelProcessor)
                .setModelUrlNormalizer(modelUrlNormalizer)
                .setModelValidator(new DefaultModelValidator(new DefaultModelVersionProcessor()))
                .setPluginConfigurationExpander(new DefaultPluginConfigurationExpander())
                .setPluginManagementInjector(new DefaultPluginManagementInjector())
                .setProfileInjector(new DefaultProfileInjector())
                .setProfileSelector(new DefaultProfileSelector())
                .setReportConfigurationExpander(new DefaultReportConfigurationExpander())
                .setReportingConverter(new DefaultReportingConverter())
                .setSuperPomProvider(superPomProvider)
                .setModelInterpolator(stringSearchModelInterpolator)
                ;
        try
        {
            return builder.build(req).getEffectiveModel();
        }
        catch (ModelBuildingException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
