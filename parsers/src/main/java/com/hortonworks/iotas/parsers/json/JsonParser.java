package com.hortonworks.iotas.parsers.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hortonworks.iotas.common.Schema;
import com.hortonworks.iotas.parser.BaseParser;
import com.hortonworks.iotas.exception.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * A simple json parser that uses {@link ObjectMapper} to parse
 * json to a Map&lt;String, Object&gt;
 */
public class JsonParser extends BaseParser {
    private static final String VERSION = "1.0";
    private static final Logger LOG = LoggerFactory.getLogger(JsonParser.class);
//    private Schema schema = Schema.of(
//            new Schema.Field("device_id", Schema.Type.STRING),
//            new Schema.Field("device_name", Schema.Type.STRING),
//            new Schema.Field("hub_id", Schema.Type.STRING),
//            new Schema.Field("hub_name", Schema.Type.STRING),
//            new Schema.Field("thermostat_mode", Schema.Type.STRING),
//            new Schema.Field("temperature", Schema.Type.INTEGER),
//            new Schema.Field("thermostat_operating_mode", Schema.Type.STRING),
//            new Schema.Field("thermostat_fan_mode", Schema.Type.STRING),
//            new Schema.Field("heating_setpoint", Schema.Type.INTEGER),
//            new Schema.Field("cooling_setpoint", Schema.Type.INTEGER),
//            new Schema.Field("thermostat_setpoint", Schema.Type.INTEGER));
//    private Schema schema = Schema.of(
//        new Schema.Field("device_id", Schema.Type.STRING),
//        new Schema.Field("device_name", Schema.Type.STRING),
//        new Schema.Field("hub_id", Schema.Type.STRING),
//        new Schema.Field("hub_name", Schema.Type.STRING),
//        new Schema.Field("switch", Schema.Type.STRING));

    private Schema schema = null;
    private final ObjectMapper mapper = new ObjectMapper();

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    /**
     * For self describing data formats like Json we can construct the schema from
     * sample data.
     *
     * @param sampleJsonData
     * @throws ParserException
     */
    public void setSchema(String sampleJsonData) throws ParserException {
        this.schema = schemaFromSampleData(sampleJsonData);
    }

    public String version() {
        return VERSION;
    }

    public Schema schema() {
        return schema;
    }

    public Map<String, Object> parse(byte[] data) throws ParserException {
        try {
            return mapper.readValue(data, new TypeReference<Map<String, Object>>(){});
        } catch (IOException e) {
            throw new ParserException("Error trying to parse data.", e);
        }
    }
}