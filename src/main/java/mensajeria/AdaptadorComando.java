package mensajeria;

import com.google.gson.*;
import java.lang.reflect.Type;

public class AdaptadorComando implements JsonSerializer<Comando>, JsonDeserializer<Comando> {

	@Override
	public JsonElement serialize(Comando src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject result = new JsonObject();
		result.add("type", new JsonPrimitive(src.getClass().getSimpleName()));
		result.add("properties", context.serialize(src, src.getClass()));		
		return result;
	}

	@Override
	public Comando deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject jsonObject = json.getAsJsonObject();	
		String type = jsonObject.get("type").getAsString();
		JsonElement element = jsonObject.get("properties");

		try {
			return context.deserialize(element, Class.forName("mensajeria."+ type));
		} catch (ClassNotFoundException cnfe) {
			throw new JsonParseException("Unknown element type: " + type, cnfe);
		}
	}
}
