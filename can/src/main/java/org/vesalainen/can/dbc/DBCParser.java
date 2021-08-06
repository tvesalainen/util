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

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.ParserInfo;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.ReservedWords;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.util.AbstractParser;
import org.vesalainen.util.IntRange;
import org.vesalainen.util.SimpleIntRange;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@GenClassname()
@GrammarDef()
@Rule(left="DBC_file", value={"version? new_symbols bit_timing nodes value_tables messages message_transmitters environment_variables environment_variables_data signal_types comments attribute_definitions attribute_defaults attribute_values value_descriptions signal_type_refs signal_groups extended_multiplexing"})
@ReservedWords({"NS_DESC_", "SG_MUL_VAL_",  "STRING", "INT", "FLOAT", "HEX", "ENUM", "VERSION", "BS_", "BU_", "BO_", "SG_", "EV_", "NS_", "CM_", "BA_DEF_", "BA_", "VAL_", "CAT_DEF_", "CAT_", "FILTER", "BA_DEF_DEF_", "EV_DATA_", "ENVVAR_DATA_", "SGTYPE_", "SGTYPE_VAL_", "BA_DEF_SGTYPE_", "BA_SGTYPE_", "SIG_TYPE_REF_", "VAL_TABLE_", "SIG_GROUP_", "SIG_VALTYPE_", "SIGTYPE_VALTYPE_", "BO_TX_BU_", "BA_DEF_REL_", "BA_REL_", "BA_DEF_DEF_REL_", "BU_SG_REL_", "BU_EV_REL_", "BU_BO_REL_"})
public abstract class DBCParser extends AbstractParser implements ParserInfo
{

    protected void reservedWords()
    {
        
    }    
    @Rule(left="version", value={"VERSION char_string"})
    protected void version(String version, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.setVersion(version);
    }
    @Rule(left="new_symbols", value={"(NS_ ':' new_symbol*)?"})
    protected void newSymbols()
    {
        
    }
    @Rule(left="new_symbol", value={"(NS_DESC_|CM_|BA_DEF_|BA_|VAL_|CAT_DEF_|CAT_|FILTER|BA_DEF_DEF_|EV_DATA_|ENVVAR_DATA_|SGTYPE_|SGTYPE_VAL_|BA_DEF_SGTYPE_|BA_SGTYPE_|SIG_TYPE_REF_|VAL_TABLE_|SIG_GROUP_|SIG_VALTYPE_|SIGTYPE_VALTYPE_|BO_TX_BU_|BA_DEF_REL_|BA_REL_|BA_DEF_DEF_REL_|BU_SG_REL_|BU_EV_REL_|BU_BO_REL_|SG_MUL_VAL_)"})
    protected void newSymbol()
    {
        
    }
    @Rule(left="bit_timing", value={"BS_ ':'"})
    protected void bitTiming()
    {
        
    }
    @Rule(left="bit_timing", value={"BS_ ':' baudrate ':' BTR1 '\\,' BTR2"})
    protected void bitTiming(int rate, int b1, int b2)
    {
        
    }
    @Rule(left="baudrate", value={"unsigned_integer"})
    protected int baudRate(int rate)
    {
        return rate;
    }
    @Rule(left="BTR1", value={"unsigned_integer"})
    protected int btr1(int btr1)
    {
        return btr1;
    }
    @Rule(left="BTR2", value={"unsigned_integer"})
    protected int btr2(int btr2)
    {
        return btr2;
    }
    @Rule(left="nodes", value={"BU_ ':' (node_name)*"})
    protected void nodes()
    {
        
    }
    @Rule(left="node_name", value={"C_identifier"})
    protected void nodeName(String name, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.addNode(name);
    }
    @Rule(left="value_tables", value={"(value_table)*"})
    protected void valueTables()
    {
        
    }
    @Rule(left="value_table", value={"VAL_TABLE_ value_table_name (value_description)* ';'"})
    protected void valueTable(String name, List<ValueDescription> valueDescriptions, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.addValueTable(name, valueDescriptions);
    }
    @Rule(left="value_table_name", value={"C_identifier"})
    protected String valueTableName(String name)
    {
        return name;
    }
    @Rule(left="value_description", value={"signed_integer char_string"})
    protected ValueDescription valueDescription(int value, String description)
    {
        return new ValueDescription(value, description);
    }
    @Rule(left="messages", value={"(message)*"})
    protected void messages()
    {
    }
    @Rule(left="message", value={"BO_ message_id message_name ':' message_size transmitter (signal)*"})
    protected void message(int id, String name, int size, String transmitter, List<SignalClass> signals, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.addMessage(new MessageClass(dbcFile, id, name, size, transmitter, signals));
    }
    @Rule(left="message_id", value={"unsigned_integer"})
    protected int messageId(int id)
    {
        return id;
    }
    @Rule(left="message_name", value={"C_identifier"})
    protected String messageName(String name)
    {
        return name;
    }
    @Rule(left="message_size", value={"unsigned_integer"})
    protected int messageSize(int size)
    {
        return size;
    }
    @Rule(left="transmitter", value={"C_identifier"})
    protected String transmitter1(String node)
    {
        return node;
    }
    @Rule(left="signal", value={"SG_ signal_name multiplexer_indicator? ':' start_bit '\\|' signal_size '@' byte_order value_type '\\(' factor '\\,' offset '\\)' '\\[' minimum '\\|' maximum '\\]' unit identifierList"})
    protected SignalClass signal(String name, MultiplexerIndicator multiplexerIndicator, int startBit, int size, ByteOrder byteOrder, ValueType valueType, double factor, double offset, double min, double max, String unit, List<String> receivers, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        return new SignalClass(dbcFile, name,  multiplexerIndicator, startBit, size, byteOrder, valueType, factor, offset, min, max, unit, receivers);
    }
    @Rule(left="signal_name", value={"C_identifier"})
    protected String signalName(String name)
    {
        return name;
    }
    @Rule(left="multiplexer_indicator", value={"'M'"})
    protected MultiplexerIndicator multiplexerIndicator()
    {
        return new MultiplexerIndicator();
    }
    @Rule(left="multiplexer_indicator", value={"'m' multiplexer_switch_value"})
    protected MultiplexerIndicator multiplexerIndicator(int value)
    {
        return new MultiplexerIndicator(value);
    }
    @Rule(left="multiplexer_indicator", value={"'m' multiplexer_switch_value 'M'"})
    protected MultiplexerIndicator multiplexerIndicator2(int value)
    {
        return new MultiplexerIndicator(value, true);
    }
    @Rule(left="multiplexer_switch_value", value={"unsigned_integer"})
    protected int multiplexerSwitchValue(int value)
    {
        return value;
    }
    @Rule(left="start_bit", value={"unsigned_integer"})
    protected int startBit(int bit)
    {
        return bit;
    }
    @Rule(left="signal_size", value={"unsigned_integer"})
    protected int signalSize(int size)
    {
        return size;
    }
    @Terminal(left="byte_order", expression="[01]")
    protected ByteOrder byteOrder(char order)
    {
        switch (order)
        {
            case '0':
                return ByteOrder.BIG_ENDIAN;
            case '1':
                return ByteOrder.LITTLE_ENDIAN;
            default:
                throw new UnsupportedOperationException(""+order);
        }
    }
    @Terminal(left="value_type", expression="[\\+\\-]")
    protected ValueType valueType(char type)
    {
        switch (type)
        {
            case '+':
                return ValueType.UNSIGNED;
            case '-':
                return ValueType.SIGNED;
            default:
                throw new UnsupportedOperationException(""+type);
        }
    }
    @Rule(left="factor", value={"double"})
    protected double factor(double v)
    {
        return v;
    }
    @Rule(left="offset", value={"double"})
    protected double offset(double v)
    {
        return v;
    }
    @Rule(left="minimum", value={"double"})
    protected double minimum(double v)
    {
        return v;
    }
    @Rule(left="maximum", value={"double"})
    protected double maximum(double v)
    {
        return v;
    }
    @Rule(left="unit", value={"char_string"})
    protected String unit(String name)
    {
        return name;
    }
    @Rule(left="receiver", value={"C_identifier"})
    protected String receiver(String name)
    {
        return name;
    }
    @Terminal(left="signal_extended_value_type", expression="[0-3]")
    protected SignalExtendedValueType signalExtendedValueType(int type)
    {
        return SignalExtendedValueType.values()[type];
    }
    @Rule(left="message_transmitters", value={"(message_transmitter)*"})
    protected void messageTransmitters(List<MessageTransmitter> list, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.setMessageTransmitters(list);
    }
    @Rule(left="message_transmitter", value={"BO_TX_BU_ message_id ':' identifierList ';'"})
    protected MessageTransmitter messageTransmitter(int id, List<String> transmitter)
    {
        return new MessageTransmitter(id, transmitter);
    }
    @Rule(left="value_descriptions", value={"value_descriptions_for*"})
    protected void valueDescriptions(List<ValueDescriptions> valDesc, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.addValueDescriptions(valDesc);
    }
    @Rule(left="value_descriptions_for", value={"value_descriptions_for_signal"})
    protected ValueDescriptions valueDescriptionsFor1(ValueDescriptions valDesc)
    {
        return valDesc;
    }
    @Rule(left="value_descriptions_for", value={"value_descriptions_for_env_var"})
    protected ValueDescriptions valueDescriptionsFor2(ValueDescriptions valDesc)
    {
        return valDesc;
    }
    @Rule(left="value_descriptions_for_signal", value={"VAL_ message_id signal_name (value_description)* ';'"})
    protected ValueDescriptions valueDescriptionsForSignal(int id, String name, List<ValueDescription> valDesc)
    {
        return new ValueDescriptions(id, name, valDesc);
    }
    @Rule(left="environment_variables", value={"(environment_variable)*"})
    protected void environmentVariables(List<EnvironmentVariable> list)
    {
    }
    @Rule(left="environment_variable", value={"EV_ env_var_name ':' env_var_type '\\[' minimum '\\|' maximum '\\]' unit initial_value ev_id access_type identifierList ';'"})
    protected EnvironmentVariable environmentVariable(String name, EnvVarType envVarType, double min, double max, String unit, double initial, int evId, AccessType accessType, List<String> accessNodes)
    {
        return new EnvironmentVariable(name, envVarType, min, max, unit, initial, evId, accessType, accessNodes);
    }
    @Rule(left="env_var_name", value={"C_identifier"})
    protected String envVarName(String name)
    {
        return name;
    }
    @Terminal(left="env_var_type", expression="[0-2]")
    protected EnvVarType envVarType(int type)
    {
        return EnvVarType.values()[type];
    }
    @Rule(left="initial_value", value={"double"})
    protected double initialValue(double v)
    {
        return v;
    }
    @Rule(left="ev_id", value={"unsigned_integer"})
    protected int evId(int id)
    {
        return id;
    }
    @Terminal(left="access_type", expression="DUMMY_NODE_VECTOR[0-4]")
    protected AccessType accessType(String type)
    {
        int i = type.charAt(17)-'0';
        return AccessType.values()[i];
    }
    @Rule(left="access_node", value={"((C_identifier) | (VECTOR_XXX))"})
    protected String accessNode(String node)
    {
        return node;
    }
    @Rule(left="environment_variables_data", value={"environment_variable_data*"})
    protected void environmentVariablesData(List<EnvironmentVariableData> data)
    {
        
    }
    @Rule(left="environment_variable_data", value={"ENVVAR_DATA_ env_var_name ':' data_size ';'"})
    protected EnvironmentVariableData environmentVariableData(String name, int size)
    {
        return new EnvironmentVariableData(name, size);
    }
    @Rule(left="data_size", value={"unsigned_integer"})
    protected int dataSize(int size)
    {
        return size;
    }
    @Rule(left="value_descriptions_for_env_var", value={"VAL_ env_var_name (value_description)* ';'"})
    protected ValueDescriptions valueDescriptionsForEnvVar(String name, List<ValueDescription> valDesc)
    {
        return new ValueDescriptions(name, valDesc);
    }
    @Rule(left="signal_types", value={"(signal_type)*"})
    protected void signalTypes(List<SignalType> list)
    {
    }
    @Rule(left="signal_type", value={"SGTYPE_ signal_type_name ':' signal_size '@' byte_order value_type '\\(' factor '\\,' offset '\\)' '\\[' minimum '\\|' maximum '\\]' unit default_value '\\,' value_table_name ';'"})
    protected SignalType signalType(String name, int size, ByteOrder byteOrder, ValueType valueType, double factor, double offset, double minimum, double maximum, String unit, double defValue, String valueTable)
    {
        return new SignalType(name, size, byteOrder, valueType, factor, offset, minimum, maximum, unit, defValue, valueTable);
    }
    @Rule(left="signal_type_name", value={"C_identifier"})
    protected String signalTypeName(String name)
    {
        return name;
    }
    @Rule(left="default_value", value={"double"})
    protected double defaultValue(double v)
    {
        return v;
    }
    @Rule(left="signal_type_refs", value={"(signal_type_ref)*"})
    protected void signalTypeRefs(List<SignalTypeRef> list)
    {
    }
    @Rule(left="signal_type_ref", value={"SGTYPE_ message_id signal_name ':' signal_type_name ';'"})
    protected SignalTypeRef signalTypeRef(int id, String name, String typeName)
    {
        return new SignalTypeRef(id, name, typeName);
    }
    @Rule(left="signal_groups", value={"signal_group*"})
    protected void signalGroups(List<SignalGroup> sgs)
    {
        
    }
    @Rule(left="signal_group", value={"SIG_GROUP_ message_id signal_group_name repetitions ':' (signal_name)* ';'"})
    protected SignalGroup signalGroup(int id, String name, int repetitions, List<String> names)
    {
        return new SignalGroup(id, name, repetitions, names);
    }
    @Rule(left="signal_group_name", value={"C_identifier"})
    protected String signalGroupName(String name)
    {
        return name;
    }
    @Rule(left="repetitions", value={"unsigned_integer"})
    protected int repetitions(int repetitions)
    {
        return repetitions;
    }
    @Rule(left="comments", value={"(comment)*"})
    protected void comments()
    {
    }
    @Rule(left="comment", value={"CM_ char_string ';'"})
    protected void comment(String comment, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.setComment(comment);
    }
    @Rule(left="comment", value={"CM_ BU_ C_identifier char_string ';'"})
    protected void comment(String name, String comment, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.setNodeComment(name, comment);
    }
    @Rule(left="comment", value={"CM_ BO_ message_id char_string ';'"})
    protected void comment(int id, String comment, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.setMessageComment(id, comment);
    }
    @Rule(left="comment", value={"CM_ SG_ message_id signal_name char_string ';'"})
    protected void comment(int id, String signal, String comment, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.setSignalComment(id, signal, comment);
    }
    @Rule(left="comment", value={"CM_ EV_ env_var_name char_string ';'"})
    protected void comment2(String var, String comment, @ParserContext("DBCFile") DBCFile dbcFile)
    {
    }
    @Rule(left="attribute_definitions", value={"(attribute_definition)*"})
    protected void attributeDefinitions()
    {
    }
    @Rule(left="attribute_definition", value={"BA_DEF_ object_type attribute_name attribute_value_type ';'"})
    protected void attributeDefinition(ObjectType objectType, String name, AttributeValueType type, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.addAttributeDefinition(objectType, name, type);
    }
    @Rule(left="attribute_definition", value={"BA_DEF_ attribute_name attribute_value_type ';'"})
    protected void attributeDefinition(String name, AttributeValueType type, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.addAttributeDefinition(name, type);
    }
    @Terminal(left="object_type", expression="((BU_)|(BO_)|(SG_)|(EV_))")
    protected ObjectType objectType(String type)
    {
        return ObjectType.valueOf(type);
    }
    @Rule(left="attribute_name", value={"'\"' C_identifier '\"'"})
    protected String attributeName(String name)
    {
        return name;
    }
    @Rule(left="attribute_value_type", value={"INT signed_integer signed_integer"})
    protected AttributeValueType attributeValueTypeInt(int i1, int i2)
    {
        return new IntAttributeValueType(i1, i2);
    }
    @Rule(left="attribute_value_type", value={"HEX signed_integer signed_integer"})
    protected AttributeValueType attributeValueTypeHex(int i1, int i2)
    {
        return new HexAttributeValueType(i1, i2);
    }
    @Rule(left="attribute_value_type", value={"FLOAT double double"})
    protected AttributeValueType attributeValueType(double d1, double d2)
    {
        return new FloatAttributeValueType(d1, d2);
    }
    @Rule(left="attribute_value_type", value={"STRING"})
    protected AttributeValueType attributeValueType()
    {
        return StringAttributeValueType.STRING_ATTRIBUTE_VALUE_TYPE;
    }
    @Rule(left="attribute_value_type", value={"ENUM charStringList"})
    protected AttributeValueType attributeValueType(List<String> types)
    {
        return new EnumAttributeValueType(types);
    }
    @Rule(left="attribute_defaults", value={"(attribute_default)*"})
    protected void attributeDefaults()
    {
    }
    @Rule(left="attribute_default", value={"BA_DEF_DEF_ attribute_name attribute_value ';'"})
    protected void attributeDefault(String name, Object value, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.setAttributeDefault(name, value);
    }
    @Rule(left="attribute_value", value={"double"})
    protected Double attributeValue(double value)
    {
        return value;
    }
    @Rule(left="attribute_value", value={"char_string"})
    protected String attributeValue(String value)
    {
        return value;
    }
    @Rule(left="attribute_values", value={"(attribute_value_for_object)*"})
    protected void attributeValues()
    {
    }
    @Rule(left="attribute_value_for_object", value={"BA_ attribute_name attribute_value ';'"})
    protected void attributeValueForObject(String name, Object value, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.setAttributeValue(name, value);
    }
    @Rule(left="attribute_value_for_object", value={"BA_ attribute_name BU_ C_identifier attribute_value ';'"})
    protected void attributeValueForNode(String name, String node, Object value, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.setNodeAttributeValue(name, node, value);
    }
    @Rule(left="attribute_value_for_object", value={"BA_ attribute_name BO_ message_id attribute_value ';'"})
    protected void attributeValueForMessage(String name, int id, Object value, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.setMessageAttributeValue(name, id, value);
    }
    @Rule(left="attribute_value_for_object", value={"BA_ attribute_name SG_ message_id signal_name attribute_value ';'"})
    protected void attributeValueForSignal(String name, int id, String signal, Object value, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.setSignalAttributeValue(name, id, signal, value);
    }
    @Rule(left="attribute_value_for_object", value={"BA_ attribute_name EV_ env_var_name attribute_value ';'"})
    protected AttributeValueForObject attributeValueForEnvironment(String name, String envVar, Object value, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        return new EnvironmentAttributeValue(name, envVar, value);
    }
    @Rule(left="extended_multiplexing", value={"multiplexed_signal*"})
    protected void extendedMultiplexing()
    {
        
    }
    @Rule(left="multiplexed_signal", value={"SG_MUL_VAL_ message_id signal_name signal_name multiplexor_value_ranges ';'"})
    protected void multiplexedSignal(int id, String multiplexedSignalName, String multiplexorSwitchName, List<IntRange> multiplexorValueRanges, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        dbcFile.addMultiplexedSignal(id, multiplexedSignalName, multiplexorSwitchName, multiplexorValueRanges);
    }
    @Rule(left="multiplexor_value_ranges", value={"multiplexor_value_range"})
    protected List<IntRange>  multiplexorValueRanges(List<IntRange> multiplexorValueRanges)
    {
        return multiplexorValueRanges;
    }
    @Rule(left="multiplexor_value_range", value={"unsigned_integer '\\-' unsigned_integer"})
    protected List<IntRange>  multiplexorValueRange(int from, int to)
    {
        List<IntRange> list = new ArrayList<>();
        list.add(new SimpleIntRange(from, to+1));
        return list;
    }
    @Rule(left="multiplexor_value_range", value={"multiplexor_value_range '\\,' unsigned_integer '\\-' unsigned_integer"})
    protected List<IntRange>  multiplexorValueRange(List<IntRange> list, int from, int to)
    {
        list.add(new SimpleIntRange(from, to+1));
        return list;
    }
    public static DBCParser getInstance()
    {
        return (DBCParser) GenClassFactory.loadGenInstance(DBCParser.class);
    }
    @ParseMethod(start="DBC_file", whiteSpace={"whiteSpace"}, charSet="ISO-8859-1")
    public <T> void parse(T text, @ParserContext("DBCFile") DBCFile dbcFile)
    {
        throw new UnsupportedOperationException();
    }
    /**
     * unsigned_integer: an unsigned integer
     */
    @Terminal(left="unsigned_integer", expression = "[0-9]+")
    protected int unsignedint(long value)
    {
        return (int) value;
    }
    /**
     * signed_integer: a signed integer
     */
    @Terminal(left="signed_integer", expression = "[\\+\\-]?[0-9]+")
    protected abstract int signedint(int value);
    /**
     * double: a double precision float number
     */
    @Terminal(left="double", expression = "[\\+\\-0-9\\.eE]+")
    protected abstract double decimal2(double value);
    /**
     * char_string: an arbitrary string consisting of any printable characters except double hyphens ('"').
     */
    @Terminal(expression = "\"([^\"]|\\\\\")*\"")
    protected String char_string(CharSequence seq)
    {
        return seq.subSequence(1, seq.length() - 1).toString();
    }
    /**
     * C_identifier: a valid C_identifier. C_identifiers have to start with
     * am alpha character or an underscore and may further consist of
     * alpha-numeric characters and underscores.
     * C_identifier = (alpha_char | '_') {alpha_num_char | '_'} 
    */
    @Terminal(left = "C_identifier", expression = "[a-zA-z_][a-zA-z0-9_]*")
    protected abstract String cIdentifier(String value);

    @Rule("C_identifier")
    protected List<String> identifierList(String item)
    {
        List<String> list = new ArrayList<>();
        list.add(item);
        return list;
    }
    @Rule("identifierList '\\,' C_identifier")
    protected List<String> identifierList(List<String> list, String item)
    {
        list.add(item);
        return list;
    }
    @Rule("char_string")
    protected List<String> charStringList(String item)
    {
        List<String> list = new ArrayList<>();
        list.add(item);
        return list;
    }
    @Rule("charStringList '\\,' char_string")
    protected List<String> charStringList(List<String> list, String item)
    {
        list.add(item);
        return list;
    }

}
