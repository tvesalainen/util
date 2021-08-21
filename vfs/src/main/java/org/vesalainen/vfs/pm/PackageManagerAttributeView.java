/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.vfs.pm;

import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.attribute.FileStoreAttributeView;
import java.util.Collection;
import java.util.List;
import static org.vesalainen.vfs.pm.Condition.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface PackageManagerAttributeView extends FileStoreAttributeView
{
    /**
     * Returns first PackageManagerAttributeView from file systems file stores.
     * Returns null if not found.
     * @param fileSystem
     * @return 
     */
    public static PackageManagerAttributeView from(FileSystem fileSystem)
    {
        for (FileStore fs : fileSystem.getFileStores())
        {
            PackageManagerAttributeView view = fs.getFileStoreAttributeView(PackageManagerAttributeView.class);
            if (view != null)
            {
                return view;
            }
        }
        return null;
    }
    /**
     * Add conflicting package name
     * @param name
     * @return 
     */
    default PackageManagerAttributeView addConflict(String name)
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
    PackageManagerAttributeView addConflict(String name, String version, Condition... dependency);
    /**
     * Returns all conflict names
     * @return 
     */
    Collection<String> getConflicts();
    /**
     * Returns conflict
     * @param name
     * @return 
     */
    Dependency getConflict(String name);
    /**
     * Add virtual package name that this package provides.
     * @param name
     * @return 
     */
    default PackageManagerAttributeView addProvide(String name)
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
    PackageManagerAttributeView addProvide(String name, String version, Condition... dependency);
    /**
     * Returns all provide names
     * @return 
     */
    Collection<String> getProvides();
    /**
     * Returns provide
     * @param name
     * @return 
     */
    Dependency getProvide(String name);
    /**
     * Add package name that this package requires.
     * @param name
     * @return 
     */
    default PackageManagerAttributeView addRequire(String name)
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
    PackageManagerAttributeView addRequire(String name, String version, Condition... dependency);
    /**
     * Returns all require names
     * @return 
     */
    Collection<String> getRequires();
    /**
     * Returns require
     * @param name
     * @return 
     */
    Dependency getRequire(String name);
    /**
     * Sets architecture of this package. Default is architecture independent.
     * @param architecture
     * @return 
     */
    PackageManagerAttributeView setArchitecture(String architecture);
    /**
     * Returns Architecture
     * @return 
     */
    String getArchitecture();
    /**
     * Set description text for package.
     * @param description
     * @return 
     */
    PackageManagerAttributeView setDescription(String description);
    /**
     * Returns Description
     * @return 
     */
    String getDescription();
    /**
     * Set copyright
     * @param copyright
     * @return 
     */
    PackageManagerAttributeView setCopyright(String copyright);
    /**
     * Returns Copyright
     * @return 
     */
    String getCopyright();
    /**
     * Set license.
     * @param license
     * @return 
     */
    PackageManagerAttributeView setLicense(String license);
    /**
     * Returns License
     * @return 
     */
    String getLicense();
    /**
     * Set package name
     * @param name
     * @return 
     */
    PackageManagerAttributeView setPackageName(String name);
    /**
     * Returns PackageName
     * @return 
     */
    String getPackageName();
    /**
     * Set operating system. Default is operating system independent.
     * @param os
     * @return 
     */
    PackageManagerAttributeView setOperatingSystem(String os);
    /**
     * Returns OperatingSystem
     * @return 
     */
    String getOperatingSystem();
    /**
     * Set post installation script with default interpreter.
     * @param script
     * @return 
     * @see getDefaultInterpreter
     */
    default PackageManagerAttributeView setPostInstallation(String script)
    {
        return setPostInstallation(script, getDefaultInterpreter());
    }
    /**
     * Returns PostInstallation
     * @return 
     */
    String getPostInstallation();
    /**
     * Returns PostInstallation Interpreter
     * @return 
     */
    String getPostInstallationInterpreter();
    /**
     * Set post un-installation script with default interpreter.
     * @param script
     * @return 
     * @see getDefaultInterpreter
     */
    default PackageManagerAttributeView setPostUnInstallation(String script)
    {
        return setPostUnInstallation(script, getDefaultInterpreter());
    }
    /**
     * Returns PostUnInstallation
     * @return 
     */
    String getPostUnInstallation();
    /**
     * Returns PostUnInstallation Interpreter
     * @return 
     */
    String getPostUnInstallationInterpreter();
    /**
     * Set pre installation script with default interpreter.
     * @param script
     * @return 
     * @see getDefaultInterpreter
     */
    default PackageManagerAttributeView setPreInstallation(String script)
    {
        return setPreInstallation(script, getDefaultInterpreter());
    }
    /**
     * Returns PreInstallation
     * @return 
     */
    String getPreInstallation();
    /**
     * Returns PreInstallation Interpreter
     * @return 
     */
    String getPreInstallationInterpreter();
    /**
     * Set pre un-installation script with default interpreter.
     * @param script
     * @return 
     * @see getDefaultInterpreter
     */
    default PackageManagerAttributeView setPreUnInstallation(String script)
    {
        return setPreUnInstallation(script, getDefaultInterpreter());
    }
    /**
     * Returns PreUnInstallation
     * @return 
     */
    String getPreUnInstallation();
    /**
     * Returns PreUnInstallation Interpreter
     * @return 
     */
    String getPreUnInstallationInterpreter();
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
    PackageManagerAttributeView setPostInstallation(String script, String interpreter);
    /**
     * Set post un-installation script and interpreter
     * @param script
     * @param interpreter
     * @return 
     */
    PackageManagerAttributeView setPostUnInstallation(String script, String interpreter);
    /**
     * Set pre installation script and interpreter
     * @param script
     * @param interpreter
     * @return 
     */
    PackageManagerAttributeView setPreInstallation(String script, String interpreter);
    /**
     * Set pre un-installation script and interpreter
     * @param script
     * @param interpreter
     * @return 
     */
    PackageManagerAttributeView setPreUnInstallation(String script, String interpreter);
    /**
     * Set package release
     * @param release
     * @return 
     */
    PackageManagerAttributeView setRelease(String release);
    /**
     * Returns Release
     * @return 
     */
    String getRelease();
    /**
     * Set package summary
     * @param summary
     * @return 
     */
    PackageManagerAttributeView setSummary(String summary);
    /**
     * Returns Summary
     * @return 
     */
    String getSummary();
    /**
     * Set package version
     * @param version
     * @return 
     */
    PackageManagerAttributeView setVersion(String version);
    /**
     * Returns Version
     * @return 
     */
    String getVersion();
    /**
     * Sets application area of package. This maps differently with package 
     * types so 
     * @param area
     * @return 
     */
    PackageManagerAttributeView setApplicationArea(String area);
    /**
     * Returns ApplicationArea
     * @return 
     */
    String getApplicationArea();
    /**
     * Sets package priority.
     * @param priority
     * @return 
     */
    PackageManagerAttributeView setPriority(String priority);
    /**
     * Returns Priority
     * @return 
     */
    String getPriority();
    /**
     * Set maintainer of package
     * @param maintainer
     * @return 
     */
    PackageManagerAttributeView setMaintainer(String maintainer);
    /**
     * Returns Maintainer
     * @return 
     */
    String getMaintainer();
    default PackageManagerAttributeView addChangeLogs(Collection<ChangeLog> changeLogs)
    {
        for (ChangeLog log : changeLogs)
        {
            addChangeLog(log);
        }
        return this;
    }
    /**
     * Adds change log lines
     * @param changeLog
     * @return 
     */
    PackageManagerAttributeView addChangeLog(ChangeLog changeLog);
    /**
     * Gets change log lines
     * @return 
     */
    List<? extends ChangeLog> getChangeLogs();
    /**
     * Returns package url
     * @return 
     */
    String getUrl();
    /**
     * Set package url
     * @param url
     * @return 
     */
    PackageManagerAttributeView setUrl(String url);
}
