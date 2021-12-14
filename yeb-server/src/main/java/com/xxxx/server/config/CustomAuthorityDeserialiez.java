package com.xxxx.server.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
public class CustomAuthorityDeserialiez extends JsonDeserializer {
    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode jsonNode = mapper.readTree(jsonParser);
        List<GrantedAuthority> grantedAuthorities = new LinkedList<>();
        Iterator<JsonNode> elements = jsonNode.elements();
        while (elements.hasNext()){
            JsonNode node = elements.next();
            JsonNode authority = node.get("authority");
            grantedAuthorities.add(new SimpleGrantedAuthority(authority.asText()));

        }
        return grantedAuthorities;
    }
}
