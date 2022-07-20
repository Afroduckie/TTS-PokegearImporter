package andrielgaming.parsing.jsonroots;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import andrielgaming.parsing.DeckObjectSerializer;

@JsonIgnoreProperties(value = { "deckIDs", "cardset", "customDeck","nickname","cardid","cardId","cardID" })
public class DeckTransform
{
	public float posX = 39.078434f;
	public float posY = 1.1407362f;
	public float posZ = 0.348727971f;
	public float rotX = 0.0168865f;
	public float rotY = 180;
	public float rotZ = 0;
	public float scaleX = 1.13392854f;
	public float scaleY = 1.0f;
	public float scaleZ = 1.11125f;
	
	public DeckTransform(float posX, float posY, float posZ, float rotX, float rotY, float rotZ, float scaleX, float scaleY, float scaleZ)
	{
		super();
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.scaleZ = scaleZ;
	}
}
