package andrielgaming.parsing.jsonroots;

import java.util.TreeMap;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.internal.LinkedTreeMap;

import andrielgaming.parsing.TabletopParser;

@JsonIgnoreProperties(value =
{ "deckIDs", "cardset", "customDeck", "nickname", "cardid", "cardId", "cardID", "isChild" })
public class DeckCardContainer
{
	// Essentially contains the same fields as ObjectStates but only holds a single card in its CustomDeck
	// Needs to be a separate container class so I can add logic to prevent the size of CustomDeck from going over 1
	// Crucial difference in fields is DeckIDs being replaced with CardID- an int value correlating to the GUID of the card in the DeckIDs arraylist of ObjectStates
	public String GUID = ("" + UUID.randomUUID()).substring(0, 6);
	public String Name = "Card";
	public DeckTransform Transform;
	public String Nickname;
	public String Description = "";
	public String GMNotes = "";
	public DeckColorDiffuse ColorDiffuse;
	public int LayoutGroupSortIndex = 0;
	public int Value = 0;
	public boolean Locked = false;
	public boolean Grid = true;
	public boolean Snap = true;
	public boolean IgnoreFoW = false;
	public boolean MeasureMovement = false;
	public boolean DragSelectable = true;
	public boolean Autoraise = true;
	public boolean Sticky = true;
	public boolean Tooltip = true;
	public boolean GridProjection = false;
	public boolean HideWhenFaceDown = true;
	public boolean Hands = true;
	public boolean SidewaysCard = false;
	public int CardID;
	@JsonProperty
	public TreeMap<Integer, LinkedTreeMap<String, Object>> CustomDeck = new TreeMap<Integer, LinkedTreeMap<String, Object>>();
	public String LuaScript = "";
	public String LuaScriptState = "";
	public String XmlUI = "";
	public int deckIDs;
	private boolean isChild = false;

	public DeckCardContainer(String Nickname, int CardID, TreeMap<Integer, LinkedTreeMap<String, Object>> CustomDeck)
	{
		super();
		this.GUID = ("" + UUID.randomUUID()).substring(0, 6);
		this.Nickname = Nickname;
		this.CustomDeck = CustomDeck;
		this.deckIDs = CardID;
		this.CardID = CardID;
		this.Transform = new DeckTransform(0, 0, 0, 0, 180, 180, 1.15f, 1.6f, 1.1f);
		this.ColorDiffuse = new DeckColorDiffuse(0.7f, 0.7f, 0.7f);
	}

	public void setChild()
	{
		isChild = true;
	}

	public String getNickname()
	{
		return this.Nickname;
	}

	public int getCardID()
	{
		return this.CardID;
	}

	public void setNickname(String Nickname)
	{
		this.Nickname = Nickname;
	}

	public void setCardID(int deckid)
	{
		this.CardID = TabletopParser.instanceIDs.get(deckid);
	}
}
