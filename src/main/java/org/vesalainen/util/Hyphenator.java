/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author tkv
 */
public class Hyphenator
{
    public static final char HYPHEN = 173;  // soft hyphen
    public static final String HYPHENSTR = new String(new char[]{HYPHEN});  // soft hyphen
    private enum State {START, CONSONANT, VOCAL, DIPHTHONG};
    private static final char auml = 228;
    private static final char Auml = 196;
    private static final char ouml = 246;
    private static final char Ouml = 214;
    private static final char aring = 229;
    private static final char Aring = 197;
    private static final char scaron = 353;
    private static final char Scaron = 352;
    private static final char zcaron = 382;
    private static final char Zcaron = 381;
    private static final char[] FINNCHARS = new char[] {'a','A','b','B','c','C',
    'd','D','e','E','f','F','g','G','h','H','i','I','j','J','k','K','l','L','m',
    'M','n','N','o','O','p','P','q','Q','r','R','s','S',353,352,'t','T','u','U',
    'v','V','w','W','x','X','y','Y','z','Z',382, 381,229,197,228,196,246,214 };
    private static final String FINNSTRING = new String(FINNCHARS);
    private static final Pattern FINNWORD = Pattern.compile("["+FINNSTRING+"]+");
    private static final Pattern WS = Pattern.compile("\\p{javaWhitespace}+");
    private static final Map<String,String> map;
    static
    {
        map = new HashMap<String,String>();
        map.put("kaivosaukko", "kai"+HYPHEN+"vos"+HYPHEN+"auk"+HYPHEN+"ko");
        map.put("syysolkiperhonen", "syys"+HYPHEN+"ol"+HYPHEN+"ki"+HYPHEN+"per"+HYPHEN+"ho"+HYPHEN+"nen");
    }

    public static final String hyphenate(String text)
    {
        return hyphenate(text, Locale.getDefault());
    }

    public static final String hyphenate(String text, Locale locale)
    {
        text = text.replace(HYPHENSTR, "");
        if (!"fi".equals(locale.getLanguage()))
        {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        StringParser parser = new StringParser(text);
        while (parser.find(FINNWORD))
        {
            sb.append(parser.skipped());
            hyphenateWord(parser.group(), sb);
        }
        sb.append(parser.remaining());
        return sb.toString();
    }
    private static final void hyphenateWord(String word, StringBuilder sb)
    {
        Matcher mm = FINNWORD.matcher(word);
        if (mm.matches())
        {
            if (map.containsKey(word.toLowerCase()))
            {
                String str = map.get(word.toLowerCase());
                int jj=0;
                for (int ii=0;ii<word.length();ii++)
                {
                    if (str.charAt(jj) == HYPHEN)
                    {
                        sb.append(HYPHEN);
                        jj++;
                    }
                    jj++;
                    sb.append(word.charAt(ii));
                }
            }
            else
            {
                State state = State.START;
                for (int ii=0;ii<word.length();ii++)
                {
                    char cc = word.charAt(ii);
                    switch (state)
                    {
                        case VOCAL:
                        {
                            sb.append(HYPHEN);
                            sb.append(cc);
                            String sub = word.substring(ii);
                            state = nextState(cc, sub);
                        }
                            break;
                        case DIPHTHONG:
                        {
                            sb.append(cc);
                            sb.append(HYPHEN);
                            state = State.START;
                        }
                            break;
                        case CONSONANT:
                            if (vocal(cc))
                            {
                                sb.insert(sb.length()-1, HYPHEN);
                                sb.append(cc);
                                String sub = word.substring(ii);
                                state = nextState(cc, sub);
                            }
                            else
                            {
                                sb.append(cc);
                            }
                            break;
                        case START:
                        {
                            String sub = word.substring(ii);
                            state = nextState(cc, sub);
                            sb.append(cc);
                        }
                    }
                }
            }
        }
        else
        {
            sb.append(word);
        }
    }
    private static final State nextState(char cc, String sub)
    {
        if (vocal(cc))
        {
            if (consonantRule(sub))
            {
                return State.CONSONANT;
            }
            else
            {
                if (vocalRule(sub))
                {
                    return State.VOCAL;
                }
                else
                {
                    if (diphthongRule(sub))
                    {
                        return State.DIPHTHONG;
                    }
                }
            }
        }
        return State.START;
    }
    private static final boolean consonantRule(String word)
    {
        if (word.length() > 1)
        {
            char c1 = word.charAt(1);
            if (!vocal(c1))
            {
                for (int ii=2;ii<word.length();ii++)
                {
                    char c2 = word.charAt(ii);
                    if (vocal(c2))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private static final boolean vocalRule(String word)
    {
        if (word.length() > 1)
        {
            if (vocal(word.charAt(1)))
            {
                char c1 = word.charAt(0);
                char c2 = word.charAt(1);
                c1 = Character.toLowerCase(c1);
                c2 = Character.toLowerCase(c2);
                return (c1 != c2 && c2 != 'i' && !diphthong(c1, c2));
            }
        }
        return false;
    }
    private static final boolean diphthongRule(String word)
    {
        if (word.length() > 2)
        {
            char c1 = word.charAt(0);
            char c2 = word.charAt(1);
            char c3 = word.charAt(2);
            c1 = Character.toLowerCase(c1);
            c2 = Character.toLowerCase(c2);
            c3 = Character.toLowerCase(c3);
            if (vocal(c1) && vocal(c2) && vocal(c3))
            {
                return c1 == c2 || diphthong(c1, c2);
            }
        }
        return false;
    }
    private static final boolean diphthong(char c1, char c2)
    {
        c1 = Character.toLowerCase(c1);
        c2 = Character.toLowerCase(c2);
        switch (c1)
        {
            case 'a':
                return c2 == 'u';
            case 'e':
                return c2 == 'u' || c2 == 'y';
            case 'i':
                return c2 == 'e' || c2 == 'u';
            case 'o':
                return c2 == 'u';
            case 'u':
                return c2 == 'o';
            case 'y':
                return c2 == ouml;
            case auml:
                return c2 == 'y';
            case ouml:
                return c2 == 'y';
            default:
                return false;
        }
    }
    private static final boolean vocal(char cc)
    {
        switch (Character.toLowerCase(cc))
        {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
            case 'y':
            case auml:
            case ouml:
            case aring:
                return true;
            default:
                return false;
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            System.err.println(Hyphenator.hyphenate("leffassa kivaa kahdelle"));
            System.err.println(Hyphenator.hyphenate("tragiikkaa sekä horkkatiloja"));
            System.err.println(Hyphenator.hyphenate("luento Aasian kääpiöpuolueista"));
            System.err.println(Hyphenator.hyphenate("raaistunut maailma liuottimet lauantaina tauotta leuan alla"));
            System.err.println(Hyphenator.hyphenate("Kaivosaukko syysolkiperhonen"));
            System.err.println(Hyphenator.hyphenate("venemessuilla kisasuunnitelmia jutun sattuma huomattavasti"));
            System.err.println(Hyphenator.hyphenate("Deepawali, vapaapäivä. ravintolassa: kunniaksi tekoon koristeitaan"));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
