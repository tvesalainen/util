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
package org.vesalainen.rpm;

import java.util.Objects;
import static org.vesalainen.rpm.IndexType.*;
import static org.vesalainen.rpm.TagStatus.*;

/**
 *
 * @author tkv
 */
public enum HeaderTag
{
    /**
     * The signature tag differentiates a signature header from a metadata header, and identifies the original contents of the signature header.
     */
    RPMTAG_HEADERSIGNATURES(62, BIN, null, 16, Optional),
    /**
     * This tag contains an index record which specifies the portion of the Header Record which was used for the calculation of a signature. This data shall be preserved or any header-only signature will be invalidated.
     */
    RPMTAG_HEADERIMMUTABLE(63, BIN, null, 16, Optional),
    /**
     * Contains a list of locales for which strings are provided in other parts of the package.
     */
    RPMTAG_HEADERI18NTABLE(100, STRING_ARRAY, null, 1, Optional),
    /**
     * This tag specifies the combined size of the Header and Payload sections.
     */
    RPMSIGTAG_SIZE(1000, INT32, true, 1, Required),
    /**
     * This tag specifies the uncompressed size of the Payload archive, including the cpio headers.
     */
    RPMSIGTAG_PAYLOADSIZE(1007, INT32, true, 1, Optional),
    /**
     * This index contains the SHA1 checksum of the entire Header Section, including the Header Record, Index Records and Header store.
     */
    RPMSIGTAG_SHA1(269, STRING, true, 1, Optional),
    /**
     * This tag specifies the 128-bit MD5 checksum of the combined Header and Archive sections.
     */
    RPMSIGTAG_MD5(1004, BIN, true, 16, Required),
    /**
     * The tag contains the DSA signature of the Header section. The data is formatted as a Version 3 Signature Packet as specified in RFC 2440: OpenPGP Message Format. If this tag is present, then the SIGTAG_GPG tag shall also be present.
     */
    RPMSIGTAG_DSA(267, BIN, true, 65, Optional),
    /**
     * 	
The tag contains the RSA signature of the Header section.The data is formatted as a Version 3 Signature Packet as specified in RFC 2440: OpenPGP Message Format. If this tag is present, then the SIGTAG_PGP shall also be present.
     */
    RPMSIGTAG_RSA(268, BIN, true, 1, Optional),
    /**
     * 	
This tag specifies the RSA signature of the combined Header and Payload sections. The data is formatted as a Version 3 Signature Packet as specified in RFC 2440: OpenPGP Message Format.
     */
    RPMSIGTAG_PGP(1002, BIN, true, 1, Optional),
    /**
     * The tag contains the DSA signature of the combined Header and Payload sections. The data is formatted as a Version 3 Signature Packet as specified in RFC 2440: OpenPGP Message Format.
     */
    RPMSIGTAG_GPG(1005, BIN, true, 65, Optional),
    /**
     * This tag specifies the name of the package.
     */
    RPMTAG_NAME(1000, STRING, 1, Required),
    /**
     * This tag specifies the version of the package.
     */
    RPMTAG_VERSION(1001, STRING, 1, Required),
    /**
     * This tag specifies the release of the package.
     */
    RPMTAG_RELEASE(1002, STRING, 1, Required),
    /**
     * This tag specifies the summary description of the package. The summary value pointed to by this index record contains a one line description of the package.
     */
    RPMTAG_SUMMARY(1004, I18NSTRING, 1, Required),
    /**
     * This tag specifies the description of the package. The description value pointed to by this index record contains a full desription of the package.
     */
    RPMTAG_DESCRIPTION(1005, I18NSTRING, 1, Required),
    /**
     * This tag specifies the sum of the sizes of the regular files in the archive.
     */
    RPMTAG_SIZE(1009, INT32, 1, Required),
    /**
     * A string containing the name of the distribution on which the package was built.
     */
    RPMTAG_DISTRIBUTION(1010, STRING, 1, Informational),
    /**
     * A string containing the name of the organization that produced the package.
     */
    RPMTAG_VENDOR(1011, STRING, 1, Informational),
    /**
     * This tag specifies the license which applies to this package.
     */
    RPMTAG_LICENSE(1014, STRING, 1, Required),
    /**
     * A string identifying the tool used to build the package.
     */
    RPMTAG_PACKAGER(1015, STRING, 1, Informational),
    /**
     * This tag specifies the administrative group to which this package belongs.
     */
    RPMTAG_GROUP(1016, I18NSTRING, 1, Required),
    /**
     * Generic package information URL.
     */
    RPMTAG_URL(1020, STRING, 1, Informational),
    /**
     * This tag specifies the OS of the package. The OS value pointed to by this index record shall be "linux".
     */
    RPMTAG_OS(1021, STRING, 1, Required),
    /**
     * This tag specifies the architecture of the package. The architecture value pointed to by this index record is defined in architecture specific LSB specification.
     */
    RPMTAG_ARCH(1022, STRING, 1, Required),
    /**
     * This tag specifies the name of the source RPM.
     */
    RPMTAG_SOURCERPM(1044, STRING, 1, Informational),
    /**
     * This tag specifies the uncompressed size of the Payload archive, including the cpio headers.
     */
    RPMTAG_ARCHIVESIZE(1046, INT32, 1, Optional),
    /**
     * This tag indicates the version of RPM tool used to build this package. The value is unused.
     */
    RPMTAG_RPMVERSION(1064, STRING, 1, Informational),
    /**
     * This tag contains an opaque string whose contents are undefined.
     */
    RPMTAG_COOKIE(1094, STRING, 1, Optional),
    /**
     * URL for package.
     */
    RPMTAG_DISTURL(1123, STRING, 1, Informational),
    /**
     * This tag specifies the format of the Archive section. The format value pointed to by this index record shall be 'cpio'.
     */
    RPMTAG_PAYLOADFORMAT(1124, STRING, 1, Required),
    /**
     * This tag specifies the compression used on the Archive section. The compression value pointed to by this index record shall be 'gzip'.
     */
    RPMTAG_PAYLOADCOMPRESSOR(1125, STRING, 1, Required),
    /**
     * This tag indicates the compression level used for the Payload. This value shall always be '9'.
     */
    RPMTAG_PAYLOADFLAGS(1126, STRING, 1, Required),
    /**
     * This tag specifies the preinstall scriptlet. If present, then RPMTAG_PREINPROG shall also be present.
     */
    RPMTAG_PREIN(1023, STRING, 1, Optional),
    /**
     * This tag specifies the postinstall scriptlet. If present, then RPMTAG_POSTINPROG shall also be present.
     */
    RPMTAG_POSTIN(1024, STRING, 1, Optional),
    /**
     * This tag specifies the preuninstall scriptlet. If present, then RPMTAG_PREUNPROG shall also be present.
     */
    RPMTAG_PREUN(1025, STRING, 1, Optional),
    /**
     * This tag specified the postuninstall scriptlet. If present, then RPMTAG_POSTUNPROG shall also be present.
     */
    RPMTAG_POSTUN(1026, STRING, 1, Optional),
    /**
     * This tag specifies the name of the intepreter to which the preinstall scriptlet will be passed. The intepreter pointed to by this index record shall be /bin/sh.
     */
    RPMTAG_PREINPROG(1085, STRING, 1, Optional),
    /**
     * This tag specifies the name of the intepreter to which the postinstall scriptlet will be passed. The intepreter pointed to by this index record shall be /bin/sh.
     */
    RPMTAG_POSTINPROG(1086, STRING, 1, Optional),
    /**
     * This tag specifies the name of the intepreter to which the preuninstall scriptlet will be passed. The intepreter pointed to by this index record shall be /bin/sh.
     */
    RPMTAG_PREUNPROG(1087, STRING, 1, Optional),
    /**
     * This program specifies the name of the intepreter to which the postuninstall scriptlet will be passed. The intepreter pointed to by this index record shall be /bin/sh.
     */
    RPMTAG_POSTUNPROG(1088, STRING, 1, Optional),
    /**
     * This tag specifies the filenames when not in a compressed format as determined by the absence of rpmlib(CompressedFileNames) in the RPMTAG_REQUIRENAME index.
     */
    RPMTAG_OLDFILENAMES(1027, STRING_ARRAY, Optional),
    /**
     * This tag specifies the size of each file in the archive.
     */
    RPMTAG_FILESIZES(1028, INT32, Required),
    /**
     * This tag specifies the mode of each file in the archive.
     */
    RPMTAG_FILEMODES(1030, INT16, Required),
    /**
     * This tag specifies the device number from which the file was copied.
     */
    RPMTAG_FILERDEVS(1033, INT16, Required),
    /**
     * This tag specifies the modification time in seconds since the epoch of each file in the archive.
     */
    RPMTAG_FILEMTIMES(1034, INT32, Required),
    /**
     * This tag specifies the ASCII representation of the MD5 sum of the corresponding file contents. This value is empty if the corresponding archive entry is not a regular file.
     */
    RPMTAG_FILEMD5S(1035, STRING_ARRAY, Required),
    /**
     * The target for a symlink, otherwise NULL.
     */
    RPMTAG_FILELINKTOS(1036, STRING_ARRAY, Required),
    /**
     * This tag specifies the bit(s) to classify and control how files are to be installed. See below.
     */
    RPMTAG_FILEFLAGS(1037, INT32, Required),
    /**
     * This tag specifies the owner of the corresponding file.
     */
    RPMTAG_FILEUSERNAME(1039, STRING_ARRAY, Required),
    /**
     * This tag specifies the group of the corresponding file.
     */
    RPMTAG_FILEGROUPNAME(1040, STRING_ARRAY, Required),
    /**
     * This tag specifies the 16 bit device number from which the file was copied.
     */
    RPMTAG_FILEDEVICES(1095, INT32, Required),
    /**
     * This tag specifies the inode value from the original file system on the the system on which it was built.
     */
    RPMTAG_FILEINODES(1096, INT32, Required),
    /**
     * This tag specifies a per-file locale marker used to install only locale specific subsets of files when the package is installed.
     */
    RPMTAG_FILELANGS(1097, STRING_ARRAY, Required),
    /**
     * This tag specifies the index into the array provided by the RPMTAG_DIRNAMES Index which contains the directory name for the corresponding filename.
     */
    RPMTAG_DIRINDEXES(1116, INT32, Optional),
    /**
     * This tag specifies the base portion of the corresponding filename.
     */
    RPMTAG_BASENAMES(1117, STRING_ARRAY, Optional),
    /**
     * 
     */
    RPMTAG_DIRNAMES(1118, STRING_ARRAY, Optional),
    /**
     * This tag indicates the name of the dependency provided by this package.
     */
    RPMTAG_PROVIDENAME(1047, STRING_ARRAY, 1, Required),
    /**
     * Bits(s) to specify the dependency range and context.
     */
    RPMTAG_REQUIREFLAGS(1048, INT32, Required),
    /**
     * This tag indicates the dependencies for this package.
     */
    RPMTAG_REQUIRENAME(1049, STRING_ARRAY, Required),
    /**
     * This tag indicates the versions associated with the values found in the RPMTAG_REQUIRENAME Index.
     */
    RPMTAG_REQUIREVERSION(1050, STRING_ARRAY, Required),
    /**
     * Bits(s) to specify the conflict range and context.
     */
    RPMTAG_CONFLICTFLAGS(1053, INT32, Optional),
    /**
     * This tag indicates the conflicting dependencies for this package.
     */
    RPMTAG_CONFLICTNAME(1054, STRING_ARRAY, Optional),
    /**
     * This tag indicates the versions associated with the values found in the RPMTAG_CONFLICTNAME Index.
     */
    RPMTAG_CONFLICTVERSION(1055, STRING_ARRAY, Optional),
    /**
     * This tag indicates the obsoleted dependencies for this package.
     */
    RPMTAG_OBSOLETENAME(1090, STRING_ARRAY, Optional),
    /**
     * Bits(s) to specify the conflict range and context.
     */
    RPMTAG_PROVIDEFLAGS(1112, INT32, Required),
    /**
     * This tag indicates the versions associated with the values found in the RPMTAG_PROVIDENAME Index.
     */
    RPMTAG_PROVIDEVERSION(1113, STRING_ARRAY, Required),
    /**
     * Bits(s) to specify the conflict range and context.
     */
    RPMTAG_OBSOLETEFLAGS(1114, INT32, 1, Optional),
    /**
     * This tag indicates the versions associated with the values found in the RPMTAG_OBSOLETENAME Index.
     */
    RPMTAG_OBSOLETEVERSION(1115, STRING_ARRAY, Optional),
    /**
     * This tag specifies the time as seconds since the epoch at which the package was built.
     */
    RPMTAG_BUILDTIME(1006, INT32, 1, Informational),
    /**
     * This tag specifies the hostname of the system on which which the package was built.
     */
    RPMTAG_BUILDHOST(1007, STRING, 1, Informational),
    /**
     * This tag specifies the bit(s) to control how files are to be verified after install, specifying which checks should be performed.
     */
    RPMTAG_FILEVERIFYFLAGS(1045, INT32, Optional),
    /**
     * This tag specifies the Unix time in seconds since the epoch associated with each entry in the Changelog file.
     */
    RPMTAG_CHANGELOGTIME(1080, INT32, Optional),
    /**
     * This tag specifies the Unix time in seconds since the epoch associated with each entry in the Changelog file.
     */
    RPMTAG_CHANGELOGNAME(1081, STRING_ARRAY, Optional),
    /**
     * This tag specifies the changes asssociated with a changelog entry.
     */
    RPMTAG_CHANGELOGTEXT(1082, STRING_ARRAY, Optional),
    /**
     * This tag indicates additional flags which may have been passed to the compiler when building this package.
     */
    RPMTAG_OPTFLAGS(1122, STRING, 1, Informational),
    /**
     * This tag contains an opaque string whose contents are undefined.
     */
    RPMTAG_RHNPLATFORM(1131, STRING, 1, Deprecated),
    /**
     * This tag contains an opaque string whose contents are undefined.
     */
    RPMTAG_PLATFORM(1132, STRING, 1, Informational),
    // Tags outsize LSB
    RPMTAG_FILECLASS(1141, INT32, 1, NotLSB),
    RPMTAG_CLASSDICT(1142, STRING_ARRAY, NotLSB),
    RPMTAG_FILEDEPENDSX(1143, INT32, 1, NotLSB),
    RPMTAG_FILEDEPENDSN(1144, INT32, 1, NotLSB),
    RPMTAG_DEPENDSDICT(1145, INT32, 1, NotLSB),
    RPMTAG_SOURCEPKGID(1146, BIN, 1, NotLSB),
    RPMTAG_SUGGESTNAME(5049, STRING_ARRAY, NotLSB),
    RPMTAG_SUGGESTVERSION(5050, STRING_ARRAY, NotLSB),
    RPMTAG_SUGGESTFLAGS(5051, INT32, 1, NotLSB)
    
;
    private int tagValue;
    private IndexType type;
    private Boolean signature;
    private int count;
    private TagStatus tagStatus;

    private HeaderTag(int tagValue, IndexType type, TagStatus tagStatus)
    {
        this(tagValue, type, 1, tagStatus);
    }

    private HeaderTag(int tagValue, IndexType type, int count, TagStatus tagStatus)
    {
        this(tagValue, type, false, count, tagStatus);
    }
    private HeaderTag(int tagValue, IndexType type, Boolean signature, int count, TagStatus tagStatus)
    {
        this.tagValue = tagValue;
        this.type = type;
        this.signature = signature;
        this.count = count;
        this.tagStatus = tagStatus;
    }

    public static HeaderTag valueOf(int tagValue, Boolean signature)
    {
        for (HeaderTag tag : HeaderTag.values())
        {
            if (tagValue == tag.tagValue && (tag.signature == null || (tag.signature == signature)))
            {
                return tag;
            }
        }
        throw new IllegalArgumentException("tag "+tagValue+" unknown");
    }
    public int getTagValue()
    {
        return tagValue;
    }

    public IndexType getType()
    {
        return type;
    }

    public int getCount()
    {
        return count;
    }

    public TagStatus getTagStatus()
    {
        return tagStatus;
    }

    public Boolean isSignature()
    {
        return signature;
    }
    
}
