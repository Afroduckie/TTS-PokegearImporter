package andrielgaming.parsing;

import static java.lang.System.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.internal.LinkedTreeMap;

import andrielgaming.parsing.jsonroots.DeckDefaults;

// Somewhat unnecessary but sanitycheck-level annotation in MOST classes to prevent invalid root fields
@JsonIgnoreProperties(value =
{ "deckIDs", "deckids", "cardset", "customDeck", "nickname", "cardid", "cardId", "cardID" })
public class TabletopParser
{
	// Card faces for the backs and basic energies since basic energies often fail automatic parsing
	public static final String DEFAULTCARDBACK = "http://cloud-3.steamusercontent.com/ugc/998016607072061655/9BE66430CD3C340060773E321DDD5FD86C1F2703/";
	public static final String ENERGYFIRE = "https://i0.wp.com/pkmncards.com/wp-content/uploads/en_US-SWSH8-284-fire_energy.jpg?fit=734%2C1024&ssl=1";
	public static final String ENERGYGRASS = "https://i0.wp.com/pkmncards.com/wp-content/uploads/en_US-SWSH8-283-grass_energy.jpg?fit=734%2C1024&ssl=1";
	public static final String ENERGYWATER = "https://i0.wp.com/pkmncards.com/wp-content/uploads/en_US-SWSH6-231-water_energy.jpg?fit=734%2C1024&ssl=1";
	public static final String ENERGYPSYCHIC = "https://i0.wp.com/pkmncards.com/wp-content/uploads/en_US-SWSH6-232-psychic_energy.jpg?fit=734%2C1024&ssl=1";
	public static final String ENERGYDARK = "https://i0.wp.com/pkmncards.com/wp-content/uploads/en_US-SWSH7-236-darkness_energy.jpg?fit=734%2C1024&ssl=1";
	public static final String ENERGYFIGHTING = "https://i0.wp.com/pkmncards.com/wp-content/uploads/en_US-SWSH6-233-fighting_energy.jpg?fit=734%2C1024&ssl=1";
	public static final String ENERGYFAIRY = "https://i0.wp.com/pkmncards.com/wp-content/uploads/en_US-SWSH_Energy-009-fairy_energy.jpg?fit=734%2C1024&ssl=1";
	public static final String ENERGYELECTRIC = "https://i0.wp.com/pkmncards.com/wp-content/uploads/en_US-SWSH7-235-lightning_energy.jpg?fit=734%2C1024&ssl=1";
	public static final String ENERGYSTEEL = "https://i0.wp.com/pkmncards.com/wp-content/uploads/en_US-SWSH7-237-metal_energy.jpg?fit=734%2C1024&ssl=1";

	// Generalized URL for jSoup to fill in search terms with
	private static final String cardDB = "https://pkmncards.com/?s=";

	// Jackson JSON Parser Setup
	private static TreeMap<String, LinkedTreeMap<String, Object>> cardset = new TreeMap<>();
	private static ObjectMapper mapper;
	private static ObjectWriter writ;
	private static ArrayList<String> errorcards;

	// Collections needed for building the deck
	private static ArrayList<String[]> decklist = new ArrayList<String[]>();														// ArrayList containing string arrays with each line of input from decklist
	public static LinkedTreeMap<Integer, Integer> instanceIDs = new LinkedTreeMap<Integer, Integer>();								// Key is the UNIQUE ID, value is the CARD ID
	public static LinkedTreeMap<Integer, ArrayList<String>> instanceURLs = new LinkedTreeMap<Integer, ArrayList<String>>();

	// Map for reverse lookup of IDs by nickname
	private static TreeMap<String, String> pokedex = new TreeMap<String, String>();
	// String for regex processing illegal characters
	private static String[] illegalRegex =
	{ "�", "{.}", "é", "^", "[�{*}é]" };
	private static String[] replacements =
	{ "e", "", "e", "" };

	@SuppressWarnings("rawtypes")
	public static boolean doParse(File f, String filePath, String deckName, boolean showDebugLogs) throws StreamWriteException, DatabindException, IOException, InterruptedException
	{
		// Send the file off to the formatter to pretty it up

		// NOTE:: Changed from 'File f' to 'String f' for decoupling, issues may crop up
		parseInputFile(f);
		out.println("Input file has been processed, starting the parsing step!");

		// Set a few important variables before starting
		errorcards = new ArrayList<String>();									// Arraylist containing any cards that failed to parse so user can be notified
		mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);	// Create the mapper and enable that stupid indenter I didn't know I needed
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);				// Workaround to trick Jackson into serializing an empty object
		String faceurl = "";
		int cardindex = 1;
		boolean thumb = false;
		
		for (String[] query : decklist)
		{
			faceurl = "";
			String flag = query[0];
			String count = query[1];
			String name = query[2];
			String set = query[3];
			String num = query[4];

			// Check for energy card
			if (flag.equals("E"))
			{
				if (name.contains("Fire")) faceurl = ENERGYFIRE;
				if (name.contains("Grass")) faceurl = ENERGYWATER;
				if (name.contains("Water")) faceurl = ENERGYFIRE;
				if (name.contains("Darkness")) faceurl = ENERGYDARK;
				if (name.contains("Fighting")) faceurl = ENERGYFIGHTING;
				if (name.contains("Fairy")) faceurl = ENERGYFAIRY;
				if (name.contains("Lightning")) faceurl = ENERGYELECTRIC;
				if (name.contains("Metal")) faceurl = ENERGYSTEEL;
				if (name.contains("Psychic"))
					faceurl = ENERGYPSYCHIC;
				else
					out.println("[WARN] Card flagged as energy but not caught, will run through normal parsing instead:: " + name + ", " + set + " " + num + " [Count : " + count + "]");
			}

			// Download HTML of database website and search for the card's image by the CSS tag 'a' under 'abs:href' in source code. Skip if energy card was handled manually.
			String temp2 = name;
			try
			{
				if (faceurl.length() == 0)
				{
					name = temp2; // .replaceAll(" ","+");
					String reg = "++";
					String tempname = temp2.replaceAll(" ", "+").replaceAll(Pattern.quote(reg), "+");

					// TODO:: This check is for set promos, so I need to figure that shit out. This is just a wild-ass shot in the dark for now.
					boolean promo = false;
					if (name.contains("PR-") || tempname.contains("PR-"))
					{
						tempname = tempname.replaceAll("PR-", "+PR");
						promo = true;
					}

					Document pkmncards = null;
					try
					{
						pkmncards = Jsoup.connect(cardDB + tempname + "+" + set + "+" + num).get();
					} 
					catch (Exception e)
					{
						// Almost certainly an 'agnostic promo' if it falls through here, so try fetch again and replace vars if it succeeds
						if (promo)
						{
							if ((pkmncards = Jsoup.connect(cardDB + tempname.replaceAll("+PR", "") + "+" + "PROMO" + "+" + num).get()) != null)
							{
								tempname = tempname.replaceAll("+PR", "");
								set = "PROMO";
							}
						}
					}

					// TODO -- Replace with a less jank fucking rate throttle because you want to believe you're better than this
					Thread.sleep(200);
					out.println("Polling " + cardDB + tempname + "+" + set + "+" + num);

					// Yeet the card URL from the CSS field header 'a' by fieldname 'href' to get the card's specific source image link
					faceurl = pkmncards.select("a.card-image-link").first().attr("abs:href");
								
					// Loop to search for lowest level subpage for any card with multiple results. Shouldn't ever loop more than once but serves as a good sanitycheck this way
					if (faceurl.contains("https://pkmncards.com/card/")) out.println("Multiple versions of this card found, please wait!");
					while (faceurl.contains("https://pkmncards.com/card/"))
					{
						pkmncards = Jsoup.connect(faceurl).get();
						faceurl = pkmncards.select("a.card-image-link").first().attr("abs:href");
					}
					
					// Set thumbnail if not set already
					if (!thumb)
					{
						// In a nutshell- rip the image, decode it, re-encode it, save to disk
						try
						{
							URL urlImage = new URL(faceurl);
							InputStream in = urlImage.openStream();
							byte[] buffer = new byte[4096];
							int n = -1;
							OutputStream os = new FileOutputStream(filePath + "\\" + deckName + ".png");
							while ((n = in.read(buffer)) != -1)
							{
								os.write(buffer, 0, n);
							}
							os.close();
							out.println("Image saved to disk as thumbnail.");
							thumb = true;
						} 
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}

				// NOTE:: Do NOT ever change this line! EVERYTHING WILL BREAK AND I DON'T KNOW WHY
				int id = cardindex * 100;
				for (int i = 1; i <= Integer.parseInt(count); i++)
				{
					// Loop through deck to assign deck & card IDs for all parsed cards
					int instID = id + (i * 10);
					instanceIDs.put(instID, id);
					ArrayList<String> temp = new ArrayList<String>();
					temp.add(faceurl);
					temp.add(name);
					instanceURLs.put(instID, temp);
				}
				// Increment cardindex by number of this card present in deck
				cardindex += Integer.parseInt(count);
			} catch (NullPointerException n)
			{
				out.print("Whoops, I pressed the 'war crime' button, and the U.N. will soon convene to strongly condemn the following affront to humanity:: ");
				n.printStackTrace();
				out.println();
			}
		}

		// Once basic data parsed, get
		Object json = null;																					// Blank Object instance needed to trick Jackson into behaving itself (and/or to make up for my own ignorance)
		ArrayList<Integer> deckids = new ArrayList<Integer>();												// Arraylist of "deck IDs" for serialization to the final JSON file
		DeckDefaults defs = new DeckDefaults(deckids, cardset);												// Container class that serves as a 'template' for the JSON format TTS uses for Custom Deck objects

		// Pass all of our generated IDs to the container classes
		for (Entry e : instanceIDs.entrySet())
		{
			deckids.add((int) e.getValue());
		}
		defs.setDeckIDs(deckids);

		// Write the info for each distinct card, including a random GUID that for some reason still works in TTS
		for (Entry e : instanceURLs.entrySet())
		{
			@SuppressWarnings("unchecked")
			ArrayList<String> vals = (ArrayList<String>) e.getValue();		// Uncheck deez nuts I wrote the damn program. More to the point, its impossible this list will be an incompatible type.
			defs.insertSerialValues(("" + UUID.randomUUID()).substring(0, 6), vals.get(0), DEFAULTCARDBACK, vals.get(1), (int) e.getKey());
		}

		// Set up the PrettyPrinter using an overridden DefaultPrettyPrinter that will auto-ignore line breaks and use normal breaks instead
		DefaultPrettyPrinter p = new DefaultPrettyPrinter().withoutSpacesInObjectEntries();
		DefaultPrettyPrinter.Indenter indent = new DefaultIndenter(" ", "\n");
		p.indentArraysWith(indent);
		p.indentObjectsWith(indent);
		mapper.setDefaultPrettyPrinter(p);
		writ = mapper.writer(p).withRootValueSeparator("");

		// Serialize DeckDefaults and write it to our JSON file
		String jsondefaults = "";
		jsondefaults = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(defs);
		json = mapper.readValue(jsondefaults, Object.class);
		writ.writeValue(Paths.get(filePath + "\\" + deckName + ".json").toFile(), json);
		return true;
	}

	public static void addCard(int uid, String FaceURL, String BackURL)
	{
		LinkedTreeMap<String, Object> temp = new LinkedTreeMap<String, Object>();
		temp.put("FaceURL", FaceURL);
		temp.put("BackURL", BackURL);
		temp.put("NumWidth", 10);
		temp.put("NumHeight", 7);
		temp.put("BackIsHidden", true);
		temp.put("UniqueBack", false);
		temp.put("Type", 0);
		cardset.put("" + uid, temp);
	}

	public static void parseInputFile(File f) throws FileNotFoundException
	{
		Scanner file = new Scanner(f);
		while (file.hasNext())
		{
			String line = file.nextLine().trim();

			// Check for lines to skip
			if (line.length() <= 3) continue;
			if (!(line.charAt(0) + "").matches("[0-9]") && !(line.substring(0, 2).equals("* "))) continue;

			// Replace ampersand in Tag Team cards with just 'and'
			line = line.replaceAll("&", "%26");
			// FIXME:: Potentially terrible workaround for agnostic promos
			line = line.replaceAll("PR-BLW", "PR+BWP");
			line = line.replaceAll("PR-", " PR+");// NOTE:: Added promo check back to webcrawler
			// Hopefully un-fucks the SWSH elemental special energies since some genius at The Pokemon Company wanted to put fire emojis in names
			line = line.replaceAll("\\{.\\}", "");
			line = line.replaceAll("\\* ", "");

			// Report to console
			out.println("Input string processed:: " + line);

			// Use 2 string arrays and a split to compress the name into 1 index. Array secondpass has 5 indices to hold an energy flag, count, card name, set ID, and set number
			String[] firstpass = line.split(" ");
			String[] secondpass = new String[5];

			// Skip line if firstpass splits to 3 or less, indicating a required field is missing
			if (firstpass.length <= 3) continue;

			for (int i = 0; i < firstpass.length; i++)
			{
				firstpass[i] = firstpass[i].trim();
			}
			for (int i = 0; i < 5; i++)
			{
				secondpass[i] = "";
			}

			// Check for the word 'energy' and set the energy flags of matches to 'E' for special checks later on
			if (line.toUpperCase().contains("ENERGY"))
				secondpass[0] = "E";
			else if (!line.toUpperCase().contains("ENERGY")) secondpass[0] = "N";

			// Format the string array if needed
			if (firstpass.length > 4)
			{
				// Luckily the set number and name are never more than 1 word, so we can just grab the last 2 elements
				secondpass[4] = firstpass[firstpass.length - 1];
				secondpass[3] = firstpass[firstpass.length - 2];
				secondpass[1] = firstpass[0];
				String reformat = "";

				// Collapse the extra indices in firstpass into one string
				for (int i = 1; i < firstpass.length - 2; i++)
					reformat += firstpass[i] + " ";

				// Trim trimmity trim trim tri-meee
				// Because I am paranoia
				secondpass[2] = reformat.trim();
			}
			else
			{
				for (int i = 0; i < firstpass.length; i++)
					secondpass[i + 1] = firstpass[i];
			}
			out.println("[[DEBUG]] Contents of SECONDPASS array:: ");
			for (String s : secondpass)
				out.println(s);
			out.println("------");
			decklist.add(secondpass);
		}
		file.close();
	}

	public static int getErrorCount()
	{
		return errorcards.size();
	}

	public static String fetchCardID(String name)
	{
		return pokedex.get(name);
	}
}
