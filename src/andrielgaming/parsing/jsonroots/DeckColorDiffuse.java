package andrielgaming.parsing.jsonroots;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import andrielgaming.parsing.DeckObjectSerializer;

//@JsonSerialize(using = DeckObjectSerializer.class)
public class DeckColorDiffuse
{
	public float r = 0.713235259f;
	public float g = 0.713235259f;
	public float b = 0.713235259f;
	
	public DeckColorDiffuse(float r, float g, float b)
	{
		super();
		this.r = r;
		this.g = g;
		this.b = b;
	}
}
