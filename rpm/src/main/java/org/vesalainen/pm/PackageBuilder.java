/*
 * Copyright (C) 2017 tkv
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
package org.vesalainen.pm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import static org.vesalainen.pm.Condition.NONE;

/**
 *
 * @author tkv
 */
public interface PackageBuilder
{
    /**
     * Returns the name of this package builder. This name is used by 
     * PackageBuilderFactory to identify requested builder.
     * @return 
     */
    String getPackageBuilderName();
    /**
     * Add conflicting package name
     * @param name
     * @return 
     */
    default PackageBuilder addConflict(String name)
    {
        return addConflict(name, "", NONE);
    }
    /**
     * Add conflicting package name with version dependency.
     * @param name
     * @param version
     * @param dependency
     * @return 
     */
    PackageBuilder addConflict(String name, String version, Condition... dependency);
    /**
     * Add file to package. Returned ComponentBuilder can be used to further
     * configurate the file.
     * @param source Source path
     * @param target Target path
     * @return
     * @throws IOException 
     */
    ComponentBuilder addFile(Path source, String target) throws IOException;
    /**
     * Add file to package. Returned ComponentBuilder can be used to further
     * configurate the file.
     * @param content File content
     * @param target Target path
     * @return
     * @throws IOException 
     */
    ComponentBuilder addFile(ByteBuffer content, String target) throws IOException;
    /**
     * Add directory to package. Returned ComponentBuilder can be used to further
     * configurate the directory.
     * @param target
     * @return
     * @throws IOException 
     */
    ComponentBuilder addDirectory(String target) throws IOException;
    /**
     * Add symbolic link to package. Returned ComponentBuilder can be used to further
     * configurate the link.
     * @param target
     * @param linkTarget
     * @return
     * @throws IOException 
     */
    ComponentBuilder addSymbolicLink(String target, String linkTarget) throws IOException;
    /**
     * Add virtual package name that this package provides.
     * @param name
     * @return 
     */
    default PackageBuilder addProvide(String name)
    {
        return addProvide(name, "", NONE);
    }
    /**
     * Add virtual package name that this package provides with version.
     * @param name
     * @param version
     * @param dependency
     * @return 
     */
    PackageBuilder addProvide(String name, String version, Condition... dependency);
    /**
     * Add package name that this package requires.
     * @param name
     * @return 
     */
    default PackageBuilder addRequire(String name)
    {
        return addRequire(name, "", NONE);
    }
    /**
     * Add package name that this package requires with version.
     * @param name
     * @param version
     * @param dependency
     * @return 
     */
    PackageBuilder addRequire(String name, String version, Condition... dependency);

    /**
     * Creates package file in dir. Returns Path of created file.
     * @param dir
     * @return
     * @throws IOException
     */
    Path build(Path dir) throws IOException;
    /**
     * Sets architecture of this package
     * @param architecture
     * @return 
     */
    PackageBuilder setArchitecture(String architecture);
    /**
     * Set description text for package.
     * @param description
     * @return 
     */
    PackageBuilder setDescription(String description);
    /**
     * Set copyright
     * @param copyright
     * @return 
     */
    PackageBuilder setCopyright(String copyright);
    /**
     * Set license.
     * @param license
     * @return 
     */
    PackageBuilder setLicense(String license);
    /**
     * Set package name
     * @param name
     * @return 
     */
    PackageBuilder setPackageName(String name);
    /**
     * Set operating system
     * @param os
     * @return 
     */
    PackageBuilder setOperatingSystem(String os);
    /**
     * Set post installation script with default interpreter.
     * @param script
     * @return 
     * @see getDefaultInterpreter
     */
    default PackageBuilder setPostInstallation(String script)
    {
        return setPostInstallation(script, getDefaultInterpreter());
    }
    /**
     * Set post un-installation script with default interpreter.
     * @param script
     * @return 
     * @see getDefaultInterpreter
     */
    default PackageBuilder setPostUnInstallation(String script)
    {
        return setPostUnInstallation(script, getDefaultInterpreter());
    }
    /**
     * Set pre installation script with default interpreter.
     * @param script
     * @return 
     * @see getDefaultInterpreter
     */
    default PackageBuilder setPreInstallation(String script)
    {
        return setPreInstallation(script, getDefaultInterpreter());
    }
    /**
     * Set pre un-installation script with default interpreter.
     * @param script
     * @return 
     * @see getDefaultInterpreter
     */
    default PackageBuilder setPreUnInstallation(String script)
    {
        return setPreUnInstallation(script, getDefaultInterpreter());
    }
    /**
     * Returns default interpreter for script. E.g /bin/sh
     * @return 
     */
    String getDefaultInterpreter();
    /**
     * Set post installation script and interpreter
     * @param script
     * @param interpreter
     * @return 
     */
    PackageBuilder setPostInstallation(String script, String interpreter);
    /**
     * Set post un-installation script and interpreter
     * @param script
     * @param interpreter
     * @return 
     */
    PackageBuilder setPostUnInstallation(String script, String interpreter);
    /**
     * Set pre installation script and interpreter
     * @param script
     * @param interpreter
     * @return 
     */
    PackageBuilder setPreInstallation(String script, String interpreter);
    /**
     * Set pre un-installation script and interpreter
     * @param script
     * @param interpreter
     * @return 
     */
    PackageBuilder setPreUnInstallation(String script, String interpreter);
    /**
     * Set package release
     * @param release
     * @return 
     */
    PackageBuilder setRelease(String release);
    /**
     * Set package summary
     * @param summary
     * @return 
     */
    PackageBuilder setSummary(String summary);
    /**
     * Set package version
     * @param version
     * @return 
     */
    PackageBuilder setVersion(String version);
    /**
     * Sets application area of package. This maps differently with package 
     * types so 
     * @param area
     * @return 
     */
    PackageBuilder setApplicationArea(String area);
    /**
     * Sets package priority.
     * @param priority
     * @return 
     */
    PackageBuilder setPriority(String priority);
    /**
     * Set maintainer of package
     * @param maintainer
     * @return 
     */
    PackageBuilder setMaintainer(String maintainer);
}
