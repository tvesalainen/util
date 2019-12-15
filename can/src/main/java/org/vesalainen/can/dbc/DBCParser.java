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
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.util.AbstractParser;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@GenClassname()
@GrammarDef()
public abstract class DBCParser extends AbstractParser
{
    @Rule(left="DBC_file", value={"version? new_symbols bit_timing nodes value_tables messages message_transmitters environment_variables environment_variables_data signal_types comments attribute_definitions attribute_defaults attribute_values value_descriptions signal_type_refs signal_groups signal_extended_value_type_list"})
    @Rule(left="version", value={"'VERSION' '\"' (char_string)* '\"'"})
    protected void version(String version)
    {
        
    }
    @Rule(left="new_symbols", value={"('_NS' ':' ('CM_')? ('BA_DEF_')? ('BA_')? ('VAL_')? ('CAT_DEF_')? ('CAT_')? ('FILTER')? ('BA_DEF_DEF_')? ('EV_DATA_')? ('ENVVAR_DATA_')? ('SGTYPE_')? ('SGTYPE_VAL_')? ('BA_DEF_SGTYPE_')? ('BA_SGTYPE_')? ('SIG_TYPE_REF_')? ('VAL_TABLE_')? ('SIG_GROUP_')? ('SIG_VALTYPE_')? ('SIGTYPE_VALTYPE_')? ('BO_TX_BU_')? ('BA_DEF_REL_')? ('BA_REL_')? ('BA_DEF_DEF_REL_')? ('BU_SG_REL_')? ('BU_EV_REL_')? ('BU_BO_REL_')?)?"})
    protected void newSymbols()
    {
        
    }
    @Rule(left="bit_timing", value={"'BS_:'"})
    protected void bitTiming()
    {
        
    }
    @Rule(left="bit_timing", value={"'BS_:' baudrate ':' BTR1 '\\,' BTR2"})
    protected void bitTiming(Integer rate, Integer b1, Integer b2)
    {
        
    }
    @Rule(left="baudrate", value={"unsigned_integer"})
    protected Integer baudRate(int rate)
    {
        return rate;
    }
    @Rule(left="BTR1", value={"unsigned_integer"})
    protected Integer btr1(Integer btr1)
    {
        return btr1;
    }
    @Rule(left="BTR2", value={"unsigned_integer"})
    protected Integer btr2(Integer btr2)
    {
        return btr2;
    }
    @Rule(left="nodes", value={"'BU_:' (node_name)*"})
    protected void nodes(List<String> nodes)
    {
        
    }
    @Rule(left="node_name", value={"C_identifier"})
    protected String nodeName(String name)
    {
        return name;
    }
    @Rule(left="value_tables", value={"(value_table)*"})
    protected void valueTables(List<ValueTable> list)
    {
        
    }
    @Rule(left="value_table", value={"'VAL_TABLE_' value_table_name (value_description)* ';'"})
    protected ValueTable valueTable(String name, List<ValueDescription> valueDescriptions)
    {
        return new ValueTable(name, valueDescriptions);
    }
    @Rule(left="value_table_name", value={"C_identifier"})
    protected String valueTableName(String name)
    {
        return name;
    }
    @Rule(left="value_description", value={"double char_string"})
    protected ValueDescription valueDescription(Double v, String d)
    {
        return new ValueDescription(v, d);
    }
    @Rule(left="messages", value={"(message)*"})
    protected List<Message> messages(List<Message> list)
    {
        return list;
    }
    @Rule(left="message", value={"'BO_' message_id message_name ':' message_size transmitter (signal)*"})
    protected Message message(Integer id, String name, Integer size, String transmitter, List<Signal> signals)
    {
        return new Message(id, name, size, transmitter, signals);
    }
    @Rule(left="message_id", value={"unsigned_integer"})
    protected Integer messageId(Integer id)
    {
        return id;
    }
    @Rule(left="message_name", value={"C_identifier"})
    protected String messageName(String name)
    {
        return name;
    }
    @Rule(left="message_size", value={"unsigned_integer"})
    protected Integer messageSize(Integer size)
    {
        return size;
    }
    @Rule(left="transmitter", value={"((node_name) | ('Vector__XXX'))"})
    protected String transmitter(String node)
    {
        return node;
    }
    @Rule(left="signal", value={"'SG_' signal_name multiplexer_indicator? ':' start_bit '\\|' signal_size '@' byte_order value_type '\\(' factor '\\,' offset '\\)' '\\[' minimum '\\|' maximum '\\]' unit stringList"})
    protected Signal signal(String name, MultiplexerIndicator multiplexerIndicator, Integer startBit, Integer size, ByteOrder byteOrder, ValueType valueType, Double Factor, Double offset, Double min, Double max, String unit, List<String> receivers)
    {
        
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
    protected MultiplexerIndicator multiplexerIndicator(Integer value)
    {
        return new MultiplexerIndicator(value);
    }
    @Rule(left="multiplexer_switch_value", value={"unsigned_integer"})
    protected Integer multiplexerSwitchValue(Integer value)
    {
        return value;
    }
    @Rule(left="start_bit", value={"unsigned_integer"})
    protected Integer startBit(Integer bit)
    {
        return bit;
    }
    @Rule(left="signal_size", value={"unsigned_integer"})
    protected Integer signalSize(Integer size)
    {
        return size;
    }
    @Rule(left="byte_order", value={"(('0') | ('1'))"})
    protected ByteOrder byteOrder(char order)
    {
        switch (order)
        {
            case 0:
                return ByteOrder.LITTLE_ENDIAN;
            case 1:
                return ByteOrder.BIG_ENDIAN;
            default:
                throw new UnsupportedOperationException(""+order);
        }
    }
    @Rule(left="value_type", value={"(('\\+') | ('\\-'))"})
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
    protected Double factor(Double v)
    {
        return v;
    }
    @Rule(left="offset", value={"double"})
    protected Double offset(Double v)
    {
        return v;
    }
    @Rule(left="minimum", value={"double"})
    protected Double minimum(Double v)
    {
        return v;
    }
    @Rule(left="maximum", value={"double"})
    protected Double maximum(Double v)
    {
        return v;
    }
    @Rule(left="unit", value={"char_string"})
    protected String unit(String name)
    {
        return name;
    }
    @Rule(left="receiver", value={"((node_name) | ('Vector__XXX'))"})
    protected String receiver(String name)
    {
        return name;
    }
    @Rule(left="signal_extended_value_type_list", value={"'SIG_VALTYPE_' message_id signal_name signal_extended_value_type ';'"})
    protected SignalExtendedValueTypeList signalExtendedValueTypeList(Integer id, String name, SignalExtendedValueType type)
    {
        return new SignalExtendedValueTypeList(id, name, type);
    }
    @Rule(left="signal_extended_value_type", value={"(('0') | ('1') | ('2') | ('3'))"})
    protected SignalExtendedValueType signalExtendedValueType(int type)
    {
        return SignalExtendedValueType.values()[type];
    }
    @Rule(left="message_transmitters", value={"(message_transmitter)*"})
    protected List<MessageTransmitter> messageTransmitters(List<MessageTransmitter> list)
    {
        return list;
    }
    @Rule(left="message_transmitter", value={"'BO_TX_BU_' message_id ':' (transmitter)* ';'"})
    protected MessageTransmitter messageTransmitter(Integer id, List<String> transmitter)
    {
        return new MessageTransmitter(id, transmitter);
    }
    @Rule(left="value_descriptions", value={"((value_descriptions_for_signal) | (value_descriptions_for_env_var))*"})
    protected void valueDescriptions(List<ValueDescription> valDesc)
    {
        
    }
    @Rule(left="value_descriptions_for_signal", value={"'VAL_' message_id signal_name (value_description)* ';'"})
    protected void valueDescriptionsForSignal(String name, List<ValueDescription> valDesc)
    {
        
    }
    @Rule(left="environment_variables", value={"(environment_variable)*"})
    protected List<EnvironmentVariable> environmentVariables(List<EnvironmentVariable> list)
    {
        return list;
    }
    @Rule(left="environment_variable", value={"'EV_' env_var_name ':' env_var_type '\\[' minimum '\\|' maximum '\\]' unit initial_value ev_id access_type stringList ';'"})
    protected EnvironmentVariable environmentVariable(String name, EnvVarType envVarType, Double min, Double max, String unit, Double initial, Integer evId, AccessType accessType, List<String> accessNodes)
    {
        return new EnvironmentVariable(name, envVarType, min, max, unit, initial, evId, accessType, accessNodes);
    }
    @Rule(left="env_var_name", value={"C_identifier"})
    protected String envVarName(String name)
    {
        return name;
    }
    @Rule(left="env_var_type", value={"(('0') | ('1') | ('2'))"})
    protected EnvVarType envVarType(int type)
    {
        return EnvVarType.values()[type];
    }
    @Rule(left="initial_value", value={"double"})
    protected Double initialValue(Double v)
    {
        return v;
    }
    @Rule(left="ev_id", value={"unsigned_integer"})
    protected Integer evId(Integer id)
    {
        return id;
    }
    @Rule(left="access_type", value={"(('DUMMY_NODE_VECTOR0') | ('DUMMY_NODE_VECTOR1') | ('DUMMY_NODE_VECTOR2') | ('DUMMY_NODE_VECTOR3'))"})
    protected AccessType accessType(String type)
    {
        int i = type.charAt(17)-'0';
        return AccessType.values()[i];
    }
    @Rule(left="access_node", value={"((node_name) | ('VECTOR_XXX'))"})
    protected String accessNode(String node)
    {
        return node;
    }
    @Rule(left="environment_variables_data", value={"environment_variable_data"})
    protected void environmentVariablesData(EnvironmentVariableData data)
    {
        
    }
    @Rule(left="environment_variable_data", value={"'ENVVAR_DATA_' env_var_name ':' data_size ';'"})
    protected EnvironmentVariableData environmentVariableData(String name, Integer size)
    {
        return new EnvironmentVariableData(name, size);
    }
    @Rule(left="data_size", value={"unsigned_integer"})
    protected Integer dataSize(Integer size)
    {
        return size;
    }
    @Rule(left="value_descriptions_for_env_var", value={"'VAL_' env_var_name (value_description)* ';'"})
    protected void valueDescriptionsForEnvVar(String name, List<ValueDescription> valDesc)
    {
        
    }
    @Rule(left="signal_types", value={"(signal_type)*"})
    protected List<SignalType> signalTypes(List<SignalType> list)
    {
        return list;
    }
    @Rule(left="signal_type", value={"'SGTYPE_' signal_type_name ':' signal_size '@' byte_order value_type '\\(' factor '\\,' offset '\\)' '\\[' minimum '\\|' maximum '\\]' unit default_value '\\,' value_table ';'"})
    protected SignalType signalType(String name, Integer size, ByteOrder byteOrder, ValueType valueType, Double factor, Double offset, Double minimum, Double maximum, String unit, Double defValue, ValueTable valueTable)
    {
        return new SignalType(name, size, byteOrder, valueType, factor, offset, minimum, maximum, unit, defValue, valueTable);
    }
    @Rule(left="signal_type_name", value={"C_identifier"})
    protected String signalTypeName(String name)
    {
        return name;
    }
    @Rule(left="default_value", value={"double"})
    protected Double defaultValue(Double v)
    {
        return v;
    }
    @Rule(left="value_table", value={"value_table_name"})
    protected String valueTable(String name)
    {
        return name;
    }
    @Rule(left="signal_type_refs", value={"(signal_type_ref)*"})
    protected List<SignalTypeRef> signalTypeRefs(List<SignalTypeRef> list)
    {
        return list;
    }
    @Rule(left="signal_type_ref", value={"'SGTYPE_' message_id signal_name ':' signal_type_name ';'"})
    protected SignalTypeRef signalTypeRef(Integer id, String name, String typeName)
    {
        return new SignalTypeRef(id, name, typeName);
    }
    @Rule(left="signal_groups", value={"'SIG_GROUP_' message_id signal_group_name repetitions ':' (signal_name)* ';'"})
    protected SignalGroup signalGroups(Integer id, String name, Integer repetitions, List<String> names)
    {
        return new SignalGroup(id, name, repetitions, names);
    }
    @Rule(left="signal_group_name", value={"C_identifier"})
    protected String signalGroupName(String name)
    {
        return name;
    }
    @Rule(left="repetitions", value={"unsigned_integer"})
    protected Integer repetitions(Integer repetitions)
    {
        return repetitions;
    }
    @Rule(left="comments", value={"(comment)*"})
    protected List<Comment> comments(List<Comment> list)
    {
        return list;
    }
    @Rule(left="comment", value={"'CM_' char_string ';'"})
    protected Comment comment(String comment)
    {
        return new Comment(comment);
    }
    @Rule(left="comment", value={"'CM_' 'BU_' node_name char_string ';'"})
    protected Comment comment(String name, String comment)
    {
        return new Comment(name, comment);
    }
    @Rule(left="comment", value={"'CM_' 'BO_' message_id char_string ';'"})
    protected Comment comment(int id, String comment)
    {
        return new Comment(id, comment);
    }
    @Rule(left="comment", value={"'CM_' 'SG_' message_id signal_name char_string ';'"})
    protected Comment comment(int id, String signal, String comment)
    {
        return new Comment(id, signal, comment);
    }
    @Rule(left="comment", value={"'CM_' 'EV_' env_var_name char_string ';'"})
    protected Comment comment2(String var, String comment)
    {
        return new Comment(var, comment);
    }
    @Rule(left="attribute_definitions", value={"(attribute_definition)*"})
    protected List<AttributeDefinition> attributeDefinitions(List<AttributeDefinition> list)
    {
        return list;
    }
    @Rule(left="attribute_definition", value={"'BA_DEF_' object_type attribute_name attribute_value_type ';'"})
    protected AttributeDefinition attributeDefinition(String name, ObjectType objectType, AttributeValueType type)
    {
        return new AttributeDefinition(name, objectType, type);
    }
    @Rule(left="attribute_definition", value={"'BA_DEF_' attribute_name attribute_value_type ';'"})
    protected AttributeDefinition attributeDefinition(String name, AttributeValueType type)
    {
        return new AttributeDefinition(name, type);
    }
    @Rule(left="object_type", value={"(('BU_') | ('BO_') | ('SG_') | ('EV_'))"})
    protected ObjectType objectType(String type)
    {
        return ObjectType.valueOf(type.substring(0, 2));
    }
    @Rule(left="attribute_name", value={"'\"' C_identifier '\"'"})
    protected String attributeName(String name)
    {
        return name;
    }
    @Rule(left="attribute_value_type", value={"'INT' signed_integer signed_integer"})
    protected AttributeValueType attributeValueTypeInt(Integer i1, Integer i2)
    {
        return new IntAttributeValueType(i1, i2);
    }
    @Rule(left="attribute_value_type", value={"'HEX' signed_integer signed_integer"})
    protected AttributeValueType attributeValueTypeHex(Integer i1, Integer i2)
    {
        return new HexAttributeValueType(i1, i2);
    }
    @Rule(left="attribute_value_type", value={"'FLOAT' double double"})
    protected AttributeValueType attributeValueType(Double d1, Double d2)
    {
        return new FloatAttributeValueType(d1, d2);
    }
    @Rule(left="attribute_value_type", value={"'STRING'"})
    protected AttributeValueType attributeValueType()
    {
        return new StringAttributeValueType();
    }
    @Rule(left="attribute_value_type", value={"'ENUM' stringList"})
    protected AttributeValueType attributeValueType(List<String> types)
    {
        return new EnumAttributeValueType(types);
    }
    @Rule(left="attribute_defaults", value={"(attribute_default)*"})
    protected List<AttributeValueForObject> attributeDefaults(List<AttributeValueForObject> values)
    {
        return values;
    }
    @Rule(left="attribute_default", value={"'BA_DEF_DEF_' attribute_name attribute_value ';'"})
    protected AttributeValueForObject attributeDefault(String name, Object value)
    {
        return new AttributeValue(name, value);
    }
    @Rule(left="attribute_value", value={"unsigned_integer"})
    protected Integer attributeValueUnsigned(Integer value)
    {
        return value;
    }
    @Rule(left="attribute_value", value={"signed_integer"})
    protected Integer attributeValueSigned(Integer value)
    {
        return value;
    }
    @Rule(left="attribute_value", value={"double"})
    protected Double attributeValue(Double value)
    {
        return value;
    }
    @Rule(left="attribute_value", value={"char_string"})
    protected String attributeValue(String value)
    {
        return value;
    }
    @Rule(left="attribute_values", value={"(attribute_value_for_object)*"})
    protected List<AttributeValueForObject> attributeValues(List<AttributeValueForObject> values)
    {
        return values;
    }
    @Rule(left="attribute_value_for_object", value={"'BA_' attribute_name attribute_value ';'"})
    protected AttributeValueForObject attributeValueForObject(String name, Object value)
    {
        return new AttributeValue(name, value);
    }
    @Rule(left="attribute_value_for_object", value={"'BA_' attribute_name 'BU_' node_name attribute_value ';'"})
    protected AttributeValueForObject attributeValueForNetwork(String name, String node, Object value)
    {
        return new NetworkAttributeValue(name, node, value);
    }
    @Rule(left="attribute_value_for_object", value={"'BA_' attribute_name 'BO_' message_id attribute_value ';'"})
    protected AttributeValueForObject attributeValueForMessage(String name, int id, Object value)
    {
        return new MessageAttributeValue(name, id, value);
    }
    @Rule(left="attribute_value_for_object", value={"'BA_' attribute_name 'SG_' message_id signal_name attribute_value ';'"})
    protected AttributeValueForObject attributeValueForSignal(String name, int id, String signal, Object value)
    {
        return new SignalAttributeValue(name, id, signal, value);
    }
    @Rule(left="attribute_value_for_object", value={"'BA_' attribute_name 'EV_' env_var_name attribute_value ';'"})
    protected AttributeValueForObject attributeValueForEnvironment(String name, String envVar, Object value)
    {
        return new EnvironmentAttributeValue(name, envVar, value);
    }
    public static DBCParser getInstance()
    {
        return (DBCParser) GenClassFactory.loadGenInstance(DBCParser.class);
    }
    @ParseMethod(start="DBC_file", whiteSpace={"whiteSpace"})
    public <T> void parse(T text)
    {
        throw new UnsupportedOperationException();
    }
    /**
     * unsigned_integer: an unsigned integer
     */
    @Terminal(left="unsigned_integer", expression = "[0-9]+")
    protected abstract Integer unsignedInteger(int value);
    /**
     * signed_integer: a signed integer
     */
    @Terminal(left="signed_integer", expression = "[\\+\\-]?[0-9]+")
    protected abstract Integer signedInteger(int value);
    /**
     * double: a double precision float number
     */
    @Terminal(left="double", expression = "[\\+\\-]?[0-9]+\\.[0-9]+")
    protected abstract Double decimal2(double value);
    /**
     * char_string: an arbitrary string consisting of any printable characters except double hyphens ('"').
     */
    @Terminal(expression = "\"[^\"]*\"")
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

    @Rule("char_string")
    protected List<String> stringList(String item)
    {
        List<String> list = new ArrayList<>();
        list.add(item);
        return list;
    }
    @Rule("stringList '\\,' char_string")
    protected List<String> stringList(List<String> list, String item)
    {
        list.add(item);
        return list;
    }
}
