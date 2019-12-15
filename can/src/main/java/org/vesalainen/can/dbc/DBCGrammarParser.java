/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can.dbc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.util.AbstractParser;
import static org.vesalainen.regex.Regex.Option.FIXED_ENDER;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@GenClassname()
@GrammarDef()
public abstract class DBCGrammarParser extends AbstractParser
{
    public static DBCGrammarParser getInstance()
    {
        return (DBCGrammarParser) GenClassFactory.loadGenInstance(DBCGrammarParser.class);
    }
    @Rules({
    @Rule,
    @Rule({"bnf", "rule"})
    })
    protected void bnf()
    {
        
    }
            
    @Rule(left="rule", value={"identifier", "'='", "seqs", "';'"})
    protected void rule1(String symbol, List<String> seq)
    {
        System.err.printf("@Rule(left=\"%s\", value={\"%s\"})\n", symbol, seq.stream().collect(Collectors.joining(" ")));
    }
    
    @Rule(left="rule", value={"identifier", "'='", "choices", "';'"})
    protected void rule2(String symbol, List<String> seq)
    {
        System.err.printf("@Rule(left=\"%s\", value={\"%s\"})\n", symbol, seq.stream().collect(Collectors.joining(" | ", "(", ")")));
    }
    
    @Rule(left="part", value={"'\\['", "seqs", "'\\]'"})
    protected String zeroOrOne(List<String> seqs) throws IOException
    {
        return seqs.stream().collect(Collectors.joining(" ", "(", ")?"));
    }

    @Rule(left="part", value={"'\\{'", "seqs", "'\\}'"})
    protected String repeated(List<String> seqs) throws IOException
    {
        return seqs.stream().collect(Collectors.joining(" ", "(", ")*"));
    }

    @Rule(left="part", value={"'\\['", "choices", "'\\]'"})
    protected String zeroOrOneChoices(List<String> choices) throws IOException
    {
        return choices.stream().collect(Collectors.joining(" | ", "(", ")?"));
    }

    @Rule(left="part", value={"'\\{'", "choices", "'\\}'"})
    protected String repeatedChoices(List<String> choices) throws IOException
    {
        return choices.stream().collect(Collectors.joining(" | ", "(", ")*"));
    }

    @Rule({"(part)+"})
    protected List<String> seqs(List<String> seqs)
    {
        return seqs;
    }

    @Rule(left="part", value={"'\\('", "choices", "'\\)'"})
    protected String choicesPart(List<String> seqs)
    {
        return seqs.stream().collect(Collectors.joining(" | ", "(", ")"));
    }

    @Rule(left="choices", value={"seqs", "'\\|'", "seqs"})
    protected List<String> choices1(List<String> cp1, List<String> cp2)
    {
        List<String> choices = new ArrayList<>();
        choices.add(cp1.stream().collect(Collectors.joining(" ", "(", ")")));
        choices.add(cp2.stream().collect(Collectors.joining(" ", "(", ")")));
        return choices;
    }
    @Rule(left="choices", value={"choices", "'\\|'", "seqs"})
    protected List<String> choices2(List<String> choices, List<String> seqs)
    {
        choices.add(seqs.stream().collect(Collectors.joining(" ", "(", ")")));
        return choices;
    }
    @Rule("identifier")
    @Rule("anonymousTerminal")
    protected String part(String nt) throws IOException
    {
        return nt;
    }

    @Terminal(expression="'[^']+'")
    protected String anonymousTerminal(String name)
    {
        name = name.substring(1, name.length()-1).replace("\"", "\\\"");
        return "'"+name+"'";
    }
    @ParseMethod(start="bnf", whiteSpace={"whiteSpace", "comment"})
    public <T> void parseBnf(
            T text
            )
    {
        throw new UnsupportedOperationException();
    }
    @Terminal(expression = "\\(\\*.*\\*\\)", options = {FIXED_ENDER})
    protected abstract void comment();
}
