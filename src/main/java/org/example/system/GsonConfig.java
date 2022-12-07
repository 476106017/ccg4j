package org.example.system;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Configuration
public class GsonConfig {

    @Bean
    public Gson gson(){
        return new GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .registerTypeAdapter(
            new TypeToken<TreeMap<String, Object>>() {
            }.getType(),
            new JsonDeserializer<TreeMap<String, Object>>() {
                @Override
                public TreeMap<String, Object> deserialize(
                    JsonElement json, Type typeOft,
                    JsonDeserializationContext context) throws JsonParseException {

                    TreeMap<String, Object> treeMap = new TreeMap<>();
                    JsonObject jsonObject = json.getAsJsonObject();
                    Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                    for (Map.Entry<String, JsonElement> entry : entrySet) {
                        treeMap.put(entry.getKey(), entry.getValue());
                    }
                    return treeMap;
                }
            }).create();
    }

}
