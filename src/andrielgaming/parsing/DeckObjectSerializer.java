package andrielgaming.parsing;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import andrielgaming.parsing.jsonroots.ObjectStates;

public class DeckObjectSerializer extends StdSerializer<ObjectStates>
{
	private static final long serialVersionUID = 7969575872418801707L;

	public DeckObjectSerializer()
	{
		this(null);
	}
	
	public DeckObjectSerializer(Class<ObjectStates> d)
	{
		super(d);
	}

	@Override
	public void serialize(ObjectStates value, JsonGenerator gen, SerializerProvider provider) throws IOException
	{
		// gen.writeStringField("", value.);
		// gen.writeNumberField("", value.);
		gen.writeStartObject();
		gen.writeStringField("GUID", value.GUID);
		gen.writeStringField("Name", value.Name);
		gen.writeEndObject();
	}
	
}
