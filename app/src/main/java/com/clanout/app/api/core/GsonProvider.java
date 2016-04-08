package com.clanout.app.api.core;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.lang.reflect.Type;

public class GsonProvider
{
    private static Gson gson;

    static
    {
        gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(DateTime.class, new DateTimeDeserializer())
                .registerTypeAdapter(DateTime.class, new DateTimeSerializer())
                .create();
    }

    public static Gson getGson()
    {
        return gson;
    }

    private static class DateTimeDeserializer implements JsonDeserializer<DateTime>
    {
        @Override
        public DateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext
                jsonDeserializationContext) throws JsonParseException
        {
            return DateTime.parse(jsonElement.getAsString()).toDateTime(DateTimeZone.getDefault());
        }
    }

    public static class DateTimeSerializer implements JsonSerializer<DateTime>
    {
        @Override
        public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context)
        {
            return new JsonPrimitive(src.toString());
        }
    }
}
