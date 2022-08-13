package andrielgaming.parsing.jsonroots;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.internal.LinkedTreeMap;

import andrielgaming.utils.EmptyObject;

@JsonIgnoreProperties(value =
{ "deckIDs", "cardset", "customDeck", "nickname", "cardid", "cardId", "cardID" })
public class DeckDefaults
{
	public String SaveName = "";
	public String Date = "";
	public String VersionNumber = "";
	public String GameMode = "";
	public String GameType = "";
	public String GameComplexity = "";
	public ArrayList<String> Tags = new ArrayList<>();
	public float Gravity = 0.5f;
	public float PlayArea = 0.5f;
	public String Table = "";
	public String Sky = "";
	public String Note = "";
	public EmptyObject TabStates = new EmptyObject();
	public String LuaScript = "";
	public String LuaScriptState = "";
	public String XmlUI = "";
	public ObjectStates[] ObjectStates =
	{ new ObjectStates(("" + UUID.randomUUID()).substring(0, 6), "", new DeckTransform(0, 0, 0, 0, 180, 180, 1.15f, 1.1f, 1.1f), "", "", new DeckColorDiffuse(0.7f, 0.7f, 0.7f), "", 0, 0, false, true, true, false, false, true, true, true, true, false, true, false, false, new ArrayList<Integer>(), new TreeMap<String, LinkedTreeMap<String, Object>>(), "", "", "") };

	public DeckDefaults()
	{
		super();
		this.SaveName = "";
		this.Date = "";
		this.VersionNumber = "";
		this.GameMode = "";
		this.GameType = "";
		this.GameComplexity = "";
		this.Tags = new ArrayList<>();
		this.Gravity = 0.5f;
		this.PlayArea = 0.5f;
		this.Table = "";
		this.Sky = "";
		this.Note = "";
		this.TabStates = new EmptyObject();
	}

	public DeckDefaults(ArrayList<Integer> DeckIDs, TreeMap<String, LinkedTreeMap<String, Object>> CustomDeck)
	{
		super();
		this.SaveName = "";
		this.Date = "";
		this.VersionNumber = "";
		this.GameMode = "";
		this.GameType = "";
		this.GameComplexity = "";
		this.Tags = new ArrayList<>();
		this.Gravity = 0.5f;
		this.PlayArea = 0.5f;
		this.Table = "";
		this.Sky = "";
		this.Note = "";
		this.TabStates = new EmptyObject();
		ObjectStates[0].setDeckIDs(DeckIDs);
		ObjectStates[0].setCustomDeck(CustomDeck);
	}

	public void insertSerialValues(String guid, String furl, String burl, String nick, int cardid)
	{
		ObjectStates[0].addToCustomDeck(guid, furl, burl, nick, cardid);
	}

	public void setDeckIDs(ArrayList<Integer> DeckIDs)
	{
		ObjectStates[0].setDeckIDs(DeckIDs);
	}
}
