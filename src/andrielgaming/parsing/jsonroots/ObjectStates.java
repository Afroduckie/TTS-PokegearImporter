package andrielgaming.parsing.jsonroots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.internal.LinkedTreeMap;

import andrielgaming.parsing.TabletopParser;

// ObjectStates, like most of the container classes, is named that because I'm lazy and that's what the JSON root is named

@JsonIgnoreProperties(value =
{ "deckIDs", "cardset", "customDeck", "nickname", "cardid", "cardId", "cardID" })
public class ObjectStates
{
	// Single-instance variables
	public String GUID = ("" + UUID.randomUUID()).substring(0, 6);	// Literally stole another deck's known-valid GUID in DeckDefaults' init constructor for this class so lets see if that works
	public String Name = "Deck";
	public DeckTransform Transform;
	public String Nickname;
	public String Description;
	public DeckColorDiffuse ColorDiffuse;
	public String GMNotes = "";
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
	public boolean Hands = false;
	public boolean SidewaysCard = false;
	@JsonProperty
	public ArrayList<Integer> DeckIDs = new ArrayList<>();
	@JsonProperty
	public TreeMap<String, LinkedTreeMap<String, Object>> CustomDeck = new TreeMap<String, LinkedTreeMap<String, Object>>();
	public String LuaScript = "";
	public String LuaScriptState = "";
	public String XmlUI = "";
	@JsonProperty
	public ArrayList<DeckCardContainer> ContainedObjects = new ArrayList<>();
	// Arraylist to help ensure a CustomDeck entry isn't entered more than once
	private ArrayList<Integer> verifier = new ArrayList<>();
	private HashMap<Integer, DeckCardContainer> masterlist = new HashMap<>();
	// new DeckCardContainer(GUID, Name, Transform, "", Description, ColorDiffuse, "", 0, 0, false, true, true, true, false, true, true, true, false, false, true, true, false, CustomDeck, "", "", "");

	public ArrayList<Integer> getDeckIDs()
	{
		return DeckIDs;
	}

	public void setDeckIDs(ArrayList<Integer> DeckIDs)
	{
		this.DeckIDs = DeckIDs;
	}

	public void addToCustomDeck(String guid, String furl, String burl, String nick, int cardid, String tooltip)
	{
		LinkedTreeMap<String, Object> temp = new LinkedTreeMap<String, Object>();
		temp.put("FaceURL", furl);
		temp.put("BackURL", burl);
		temp.put("NumWidth", 1);
		temp.put("NumHeight", 1);
		temp.put("BackIsHidden", true);
		temp.put("UniqueBack", false);
		temp.put("Type", 0);

		TreeMap<Integer, LinkedTreeMap<String, Object>> temp1map = new TreeMap<Integer, LinkedTreeMap<String, Object>>();
		temp1map.put(cardid, temp);
		int deckid = TabletopParser.instanceIDs.get(cardid);
		DeckCardContainer temp1 = new DeckCardContainer(nick, deckid, temp1map);
		//System.out.println("Fetched card ID for CustomDeck field " + cardid + " with master ID " + TabletopParser.instanceIDs.get(cardid));

		// Genuinely have no fucking clue why changing this from 'cardid' to 'deckid / 100' made any damn difference as that SHOULD be what cardid fucking is.
		// That said, everything breaks if you change these 2 lines. So don't do that.
		CustomDeck.put("" + deckid / 100, temp);
		verifier.add(deckid / 100);

		ContainedObjects.add(temp1);
		masterlist.put(deckid, temp1);
		this.Description = tooltip;
	}

	public TreeMap<String, LinkedTreeMap<String, Object>> getCustomDeck()
	{
		return CustomDeck;
	}

	public ObjectStates(String GUID, String Name, DeckTransform Transform, String Nickname, String Description, DeckColorDiffuse ColorDiffuse, String GMNotes, int LayoutGroupSortIndex, int Value, boolean Locked, boolean Grid, boolean Snap, boolean IgnoreFoW, boolean MeasureMovement, boolean DragSelectable, boolean Autoraise, boolean Sticky, boolean Tooltip, boolean GridProjection, boolean HideWhenFaceDown, boolean Hands, boolean SidewaysCard, ArrayList<Integer> DeckIDs, TreeMap<String, LinkedTreeMap<String, Object>> CustomDeck, String LuaScript, String LuaScriptState, String XmlUI)
	{
		super();
		this.GUID = "Z4PD05";
		this.Name = "Deck";
		this.Transform = Transform;
		this.Nickname = Nickname;
		this.Description = Description;
		this.ColorDiffuse = ColorDiffuse;
		this.GMNotes = GMNotes;
		this.LayoutGroupSortIndex = LayoutGroupSortIndex;
		this.Value = Value;
		this.Locked = Locked;
		this.Grid = Grid;
		this.Snap = Snap;
		this.IgnoreFoW = IgnoreFoW;
		this.MeasureMovement = MeasureMovement;
		this.DragSelectable = DragSelectable;
		this.Autoraise = Autoraise;
		this.Sticky = Sticky;
		this.Tooltip = Tooltip;
		this.GridProjection = GridProjection;
		this.HideWhenFaceDown = HideWhenFaceDown;
		this.Hands = Hands;
		this.SidewaysCard = SidewaysCard;
		this.DeckIDs = DeckIDs;
		this.CustomDeck = CustomDeck;
		this.LuaScript = LuaScript;
		this.LuaScriptState = LuaScriptState;
		this.XmlUI = XmlUI;
	}

	public void setCustomDeck(TreeMap<String, LinkedTreeMap<String, Object>> CustomDeck)
	{
		this.CustomDeck = CustomDeck;
	}
}
