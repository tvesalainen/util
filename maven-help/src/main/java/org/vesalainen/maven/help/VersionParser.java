/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.maven.help;

import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.regex.SyntaxErrorException;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@GenClassname()
@GrammarDef()
public abstract class VersionParser
{
    public static VersionParser VERSION_PARSER = getInstance();
    
    public static VersionParser getInstance()
    {
        return (VersionParser) GenClassFactory.loadGenInstance(VersionParser.class);
    }

    public Version parseVersion(String text)
    {
        try
        {
            return parseSimpleVersion(text);
        }
        catch (SyntaxErrorException ex)
        {
            return new StringVersion(text);
        }
    }
    
    @ParseMethod(start="versionRange")
    public abstract VersionRange parseVersionRange(String text);

    @ParseMethod(start="version")
    protected abstract Version parseSimpleVersion(String text);

    @Rule("rangeII")
    @Rule("rangeIE")
    @Rule("rangeEI")
    @Rule("rangeEE")
    @Rule("rangeIL")
    @Rule("rangeEL")
    @Rule("rangeIU")
    @Rule("rangeEU")
    @Rule("rangeOnly")
    @Rule("rangeAny")
    protected abstract VersionRange versionRange(VersionRange versionRange);
            
    @Rule("'\\[' ver '\\,' ver '\\]'")
    protected VersionRange rangeII(String s1, String s2)
    {
        Version v1 = parseVersion(s1);
        Version v2 = parseVersion(s2);
        return new VersionRange((v)->v.compareTo(v1)>=0 && v.compareTo(v2)<=0, "["+v1+","+v2+"]");
    }
    @Rule("'\\[' ver '\\,' ver '\\)'")
    protected VersionRange rangeIE(String s1, String s2)
    {
        Version v1 = parseVersion(s1);
        Version v2 = parseVersion(s2);
        return new VersionRange((v)->v.compareTo(v1)>=0 && v.compareTo(v2)<0, "["+v1+","+v2+")");
    }
    @Rule("'\\(' ver '\\,' ver '\\]'")
    protected VersionRange rangeEI(String s1, String s2)
    {
        Version v1 = parseVersion(s1);
        Version v2 = parseVersion(s2);
        return new VersionRange((v)->v.compareTo(v1)>0 && v.compareTo(v2)<=0, "("+v1+","+v2+"]");
    }
    @Rule("'\\(' ver '\\,' ver '\\)'")
    protected VersionRange rangeEE(String s1, String s2)
    {
        Version v1 = parseVersion(s1);
        Version v2 = parseVersion(s2);
        return new VersionRange((v)->v.compareTo(v1)>0 && v.compareTo(v2)<0, "("+v1+","+v2+")");
    }
    @Rule("'\\[' ver '\\,[\\)\\]]'")
    protected VersionRange rangeIL(String s1)
    {
        Version v1 = parseVersion(s1);
        return new VersionRange((v)->v.compareTo(v1)>=0, "["+v1+",)");
    }
    @Rule("'\\(' ver '\\,[\\)\\]]'")
    protected VersionRange rangeEL(String s1)
    {
        Version v1 = parseVersion(s1);
        return new VersionRange((v)->v.compareTo(v1)>0, "("+v1+",)");
    }
    @Rule("'[\\[\\(]\\,' ver '\\]'")
    protected VersionRange rangeIU(String s1)
    {
        Version v1 = parseVersion(s1);
        return new VersionRange((v)->v.compareTo(v1)<=0, "(,"+v1+"]");
    }
    @Rule("'[\\[\\(]\\,' ver '\\)'")
    protected VersionRange rangeEU(String s1)
    {
        Version v1 = parseVersion(s1);
        return new VersionRange((v)->v.compareTo(v1)<0, "(,"+v1+")");
    }
    @Rule("'\\[' ver '\\]'")
    protected VersionRange rangeOnly(String s1)
    {
        Version v1 = parseVersion(s1);
        return new VersionRange((v)->v.compareTo(v1)==0, "["+v1+"]");
    }
    @Rule("ver")
    protected VersionRange rangeAny(String s1)
    {
        Version v1 = parseVersion(s1);
        return new VersionRange((v)->true, v1.toString(), v1);
    }
    @Rule("integer '\\.' integer '\\.' integer '\\-' string")
    protected Version version(int major, int minor, int incremental, String qualifier)
    {
        Version version = new SimpleVersion()
                .setMajor(major)
                .setMinor(minor)
                .setIncremental(incremental)
                .setQualifier(qualifier);
        return version;
    }
    
    @Rule("integer '\\.' integer '\\-' string")
    protected Version version(int major, int minor, String qualifier)
    {
        Version version = new SimpleVersion()
                .setMajor(major)
                .setMinor(minor)
                .setQualifier(qualifier);
        return version;
    }
    
    @Rule("integer '\\-' string")
    protected Version version(int major, String qualifier)
    {
        Version version = new SimpleVersion()
                .setMajor(major)
                .setQualifier(qualifier);
        return version;
    }
    
    @Rule("integer '\\.' integer '\\.' integer")
    protected Version version(int major, int minor, int incremental)
    {
        Version version = new SimpleVersion()
                .setMajor(major)
                .setMinor(minor)
                .setIncremental(incremental);
        return version;
    }
    
    @Rule("integer '\\.' integer")
    protected Version version(int major, int minor)
    {
        Version version = new SimpleVersion()
                .setMajor(major)
                .setMinor(minor);
        return version;
    }
    
    @Rule("integer")
    protected Version version(int major)
    {
        Version version = new SimpleVersion()
                .setMajor(major);
        return version;
    }
    
    @Terminal(expression="[0-9]+")
    protected abstract int integer(int x);
    
    @Terminal(expression="[0-9a-zA-z\\-]+")
    protected abstract String string(String s);
    
    @Terminal(expression="[^\\[\\]\\(\\)\\,]+")
    protected abstract String ver(String s);
    
}
