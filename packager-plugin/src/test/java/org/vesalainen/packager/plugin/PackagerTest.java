package org.vesalainen.packager.plugin;


import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;

import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;

public class PackagerTest
{
    @Rule
    public MojoRule rule = new MojoRule()
    {
        @Override
        protected void before() throws Throwable 
        {
        }

        @Override
        protected void after()
        {
        }
    };

    /**
     * @throws Exception if any
     */
    @Test
    public void testSomething()
            throws Exception
    {
        File pom = new File( "target/test-classes/project-to-test/" );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        Packager myMojo = ( Packager ) rule.lookupConfiguredMojo( pom, "pack" );
        assertNotNull( myMojo );
        if (false)
        myMojo.execute(
                new File("C:\\\\Users\\\\tkv\\\\share"),
                "deb",
                "org.vesalainen.nmea",
                "nmea-router",
                "1.8.3",
                true,
                "Timo Vesalainen"
        );


    }

    /** Do not need the MojoRule. */
    @WithoutMojo
    @Test
    public void testSomethingWhichDoesNotNeedTheMojoAndProbablyShouldBeExtractedIntoANewClassOfItsOwn()
    {
        assertTrue( true );
    }

}

