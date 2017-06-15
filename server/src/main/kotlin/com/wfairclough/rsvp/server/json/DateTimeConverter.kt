package com.wfairclough.rsvp.server.json

import com.google.gson.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

import java.lang.reflect.Type

/**
 * GSON serialiser/deserialiser for converting Joda [DateTime] objects.
 */
class DateTimeConverter : JsonSerializer<DateTime>, JsonDeserializer<DateTime> {
    /**
     * Gson invokes this call-back method during serialization when it encounters a field of the
     * specified type.
     *
     *

     * In the implementation of this call-back method, you should consider invoking
     * [JsonSerializationContext.serialize] method to create JsonElements for any
     * non-trivial field of the `src` object. However, you should never invoke it on the
     * `src` object itself since that will cause an infinite loop (Gson will call your
     * call-back method again).
     * @param src the object that needs to be converted to Json.
     * *
     * @param typeOfSrc the actual type (fully genericized version) of the source object.
     * *
     * @return a JsonElement corresponding to the specified object.
     */
    override fun serialize(src: DateTime, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val fmt = ISODateTimeFormat.dateTime()
        return JsonPrimitive(fmt.print(src))
    }

    /**
     * Gson invokes this call-back method during deserialization when it encounters a field of the
     * specified type.
     *
     *

     * In the implementation of this call-back method, you should consider invoking
     * [JsonDeserializationContext.deserialize] method to create objects
     * for any non-trivial field of the returned object. However, you should never invoke it on the
     * the same type passing `json` since that will cause an infinite loop (Gson will call your
     * call-back method again).
     * @param json The Json data being deserialized
     * *
     * @param typeOfT The type of the Object to deserialize to
     * *
     * @return a deserialized object of the specified type typeOfT which is a subclass of `T`
     * *
     * @throws JsonParseException if json is not in the expected format of `typeOfT`
     */
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): DateTime? {
        // Do not try to deserialize null or empty values
        if (json.asString == null || json.asString.isEmpty()) {
            return null
        }

        val fmt = ISODateTimeFormat.dateTimeParser()
        return fmt.parseDateTime(json.asString)
    }
}
