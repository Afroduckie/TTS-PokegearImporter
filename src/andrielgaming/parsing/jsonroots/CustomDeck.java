package andrielgaming.parsing.jsonroots;

import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.internal.LinkedTreeMap;

/*	This class holds a large nested map with a CardID (Integer) that keys to a <String, String> map containing
 * 		the data to be serialized into the JSON file. At no point should the map exceed size 60.
 */
@JsonIgnoreProperties(value =
{ "deckIDs", "cardset", "customDeck", "nickname", "cardid", "cardId", "cardID" })
public class CustomDeck
{
	@JsonProperty
	public TreeMap<Integer, LinkedTreeMap<String, Object>> cardset = new TreeMap<Integer, LinkedTreeMap<String, Object>>();
	private int cardid;

	public CustomDeck()
	{
		cardset = new TreeMap<Integer, LinkedTreeMap<String, Object>>();
	}

	public int getCardID()
	{
		return cardid;
	}

	public void setCustomDeck(int cardid, TreeMap<Integer, LinkedTreeMap<String, Object>> CustomDeck)
	{
		this.cardid = cardid;
		this.cardset = CustomDeck;
	}
}
