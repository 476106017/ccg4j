package org.example.system;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import org.example.card.Card;
import org.example.system.util.Func;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

@Configuration
public class GsonConfig {

    @Bean
    public Gson gson(){
        return new GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .registerTypeAdapter(
            new TypeToken<TreeMap<String, Object>>() {}.getType(),
            (JsonDeserializer<TreeMap<String, Object>>) (json, typeOft, context) -> {

                TreeMap<String, Object> treeMap = new TreeMap<>();
                JsonObject jsonObject = json.getAsJsonObject();
                Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                for (Map.Entry<String, JsonElement> entry : entrySet) {
                    treeMap.put(entry.getKey(), entry.getValue());
                }
                return treeMap;
            })
        .create();
    }
    public static class MyEncoder implements Encoder.Text<Object> {
        @Override
        public String encode(Object object) {
            try {

                return Func.toJson(object);
            }catch (Exception e){
                e.printStackTrace();
            }
            return "";
        }
    }


}
