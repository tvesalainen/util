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
package org.vesalainen.pm.rpm;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.vesalainen.nio.file.attribute.PosixHelp;
import static org.vesalainen.pm.rpm.HeaderTag.*;
import org.vesalainen.pm.rpm.deb.DEBBuilder;

/**
 *
 * @author tkv
 */
public class RPM2DEB extends DEBBuilder
{

    public RPM2DEB(Path base, RPMBase rpm, String maintainer) throws IOException
    {
        name = rpm.getString(RPMTAG_NAME);
        version = rpm.getString(RPMTAG_VERSION);
        release = rpm.getString(RPMTAG_RELEASE);
        init(base, name, version, release, maintainer);

        control.setPackage(name);
        control.setMaintainer(maintainer);
        
        String arch = rpm.getString(RPMTAG_ARCH);
        control.setArchitecture(arch);
        
        String desc = rpm.getString(RPMTAG_DESCRIPTION);
        control.setDescription(desc);
        
        String license = rpm.getString(RPMTAG_LICENSE);
        copyright().setLicense(license);
        // scripts
        if (rpm.contains(RPMTAG_PREIN))
        {
            String script = rpm.getString(RPMTAG_PREIN);
            String interpreter = rpm.getString(RPMTAG_PREINPROG);
            setPreInst(script, interpreter);
        }
        if (rpm.contains(RPMTAG_POSTIN))
        {
            String script = rpm.getString(RPMTAG_POSTIN);
            String interpreter = rpm.getString(RPMTAG_POSTINPROG);
            setPostInst(script, interpreter);
        }
        if (rpm.contains(RPMTAG_PREUN))
        {
            String script = rpm.getString(RPMTAG_PREUN);
            String interpreter = rpm.getString(RPMTAG_PREUNPROG);
            setPreRm(script, interpreter);
        }
        if (rpm.contains(RPMTAG_POSTUN))
        {
            String script = rpm.getString(RPMTAG_POSTUN);
            String interpreter = rpm.getString(RPMTAG_POSTUNPROG);
            setPostRm(script, interpreter);
        }
        // dependencies
        if (rpm.contains(RPMTAG_REQUIRENAME))
        {
            List<String> requireNames = rpm.getStringArray(RPMTAG_REQUIRENAME);
            List<String> requireVersions = rpm.getStringArray(RPMTAG_REQUIREVERSION);
            List<Integer> requireFlags = rpm.getInt32Array(RPMTAG_REQUIREFLAGS);
            int reqs = requireNames.size();
            for (int ii=0;ii<reqs;ii++)
            {
                control.addDepends(requireNames.get(ii), requireVersions.get(ii), requireFlags.get(ii));
            }
        }
        // conflicts
        if (rpm.contains(RPMTAG_CONFLICTNAME))
        {
            List<String> conflictNames = rpm.getStringArray(RPMTAG_CONFLICTNAME);
            List<String> conflictVersions = rpm.getStringArray(RPMTAG_CONFLICTVERSION);
            List<Integer> conflictFlags = rpm.getInt32Array(RPMTAG_CONFLICTFLAGS);
            int confl = conflictNames.size();
            for (int ii=0;ii<confl;ii++)
            {
                control.addConflict(conflictNames.get(ii), conflictVersions.get(ii), conflictFlags.get(ii));
            }
        }
        // files
        List<FileRecord> files = rpm.fileRecords;
        List<Short> modes = rpm.getInt16Array(RPMTAG_FILEMODES);
        List<Integer> mtimes = rpm.getInt32Array(RPMTAG_FILEMTIMES);
        List<Integer> flags = rpm.getInt32Array(RPMTAG_FILEFLAGS);
        List<String> users = rpm.getStringArray(RPMTAG_FILEUSERNAME);
        List<String> groups = rpm.getStringArray(RPMTAG_FILEGROUPNAME);
        int len = files.size()-1;   // - TRAILER!!!
        for (int ii=0;ii<len;ii++)
        {
            FileRecord fr = files.get(ii);
                addFile(fr.filename, fr.content.duplicate())
                        .setMode(PosixHelp.toString(modes.get(ii)))
                        .setLastModifiedTime(FileTime.from(mtimes.get(ii), TimeUnit.SECONDS ))
                        .setFlags(FileFlag.get(flags.get(ii)))
                        .setUser(users.get(ii))
                        .setGroup(groups.get(ii))
                        ;
                        
        }
    }
}
