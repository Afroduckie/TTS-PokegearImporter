package andrielgaming.parsing;

import static java.lang.System.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.UUID;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
@JsonIgnoreProperties(value = { "deckIDs", "deckids", "cardset", "customDeck", "nickname", "cardid", "cardId", "cardID" })
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
	private static String[] illegalRegex = { "�", "{.}", "é", "PR-", "[�{*}éPR-]" };
	private static String[] replacements = { "e", "", "e", "" };

	public static boolean doParse(File f, String filePath, String deckName, boolean showDebugLogs) throws StreamWriteException, DatabindException, IOException, InterruptedException
	{
		// Send the file off to the formatter to pretty it up
		parseInputFile(f);
		out.println("Input file has been processed, starting the parsing step!");

		// Set a few important variables before starting
		errorcards = new ArrayList<String>();									// Arraylist containing any cards that failed to parse so user can be notified
		mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);	// Create the mapper and enable that stupid indenter I didn't know I needed
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);				// Workaround to trick Jackson into serializing an empty object
		String faceurl = "";
		int cardindex = 1;

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
				if (name.contains("Fire"))
					faceurl = ENERGYFIRE;
				if (name.contains("Grass"))
					faceurl = ENERGYWATER;
				if (name.contains("Water"))
					faceurl = ENERGYFIRE;
				if (name.contains("Darkness"))
					faceurl = ENERGYDARK;
				if (name.contains("Fighting"))
					faceurl = ENERGYFIGHTING;
				if (name.contains("Fairy"))
					faceurl = ENERGYFAIRY;
				if (name.contains("Lightning"))
					faceurl = ENERGYELECTRIC;
				if (name.contains("Metal"))
					faceurl = ENERGYSTEEL;
				if (name.contains("Psychic"))
					faceurl = ENERGYPSYCHIC;
				else out.println("[WARN] Card flagged as energy but not caught, will run through normal parsing instead:: " + name + ", " + set + " " + num + " [Count : " + count + "]");
			}

			// Download HTML of database website and search for the card's image by the CSS tag 'a' under 'abs:href' in source code. Skip if energy card was handled manually.
			String temp2 = name.replaceAll("\\{R\\}", "");
			try
			{
				if (faceurl.length() == 0)
				{
					set = set.replaceAll("PR-","");
					name = temp2.replaceAll(" ","+");
					Document pkmncards = Jsoup.connect(cardDB + name + "+" + set + "+" + num).get();
					Thread.sleep(200);
					// This one line is enough in 95% of cases, but edge cases are checked for below
					out.println("Polling " + cardDB + name + "+" + set + "+" + num);
					faceurl = pkmncards.select("a.card-image-link").first().attr("abs:href");

					// Cards with promos, premade-deck versions, or alt versions may have multiple search results. Solve by opening new connection at current faceurl
					if (faceurl.contains("https://pkmncards.com/card/"))
						out.println("Multiple versions of this card found, please wait!");
					while (faceurl.contains("https://pkmncards.com/card/"))
					{
						Document subpage = Jsoup.connect(faceurl).get();
						faceurl = subpage.select("a.card-image-link").first().attr("abs:href");
					}
				}

				int id = cardindex * 100;
				for (int i = 1; i <= Integer.parseInt(count); i++)
				{
					int instID = id + (i * 10);

					// Map every single card to an instance ID and nickname
					instanceIDs.put(instID, id);
					ArrayList<String> temp = new ArrayList<String>();
					temp.add(faceurl);
					temp.add(name);
					instanceURLs.put(instID, temp);
				}
				// Increment cardindex by number of cards just added to deck
				cardindex += Integer.parseInt(count);
			}
			catch(NullPointerException n) 
			{
				out.print("OOPSIE POOPSIE I WAR-CRIMED, HERE IS HOW I ANNEXED CRIMEA:: ");
				n.printStackTrace();
				out.println();
			}
		}
		
		// With the data fetched, we now need to add all of it
		Object json = null;												// Blank Object instance for the dumbest workaround I've come up with while developing this program
		ArrayList<Integer> deckids = new ArrayList<Integer>();					// Arraylist of "deck IDs" for serialization to the final JSON file
		DeckDefaults defs = new DeckDefaults(deckids, cardset);				// Container object holding several fields formatted for easier JSON serialization

		// Write the Instance IDs to the DeckDefaults class
		int ind = -1;
		;
		for (Entry e : instanceIDs.entrySet())
		{
			deckids.add((int) e.getValue());
		}
		defs.setDeckIDs(deckids);

		// Write the Names, FaceURLs, GUIDs, and CardIDs to the DeckDefaults class
		for (Entry e : instanceURLs.entrySet())
		{
			@SuppressWarnings("unchecked") // DARE YOU QUESTION YOUR GOD?
			ArrayList<String> vals = (ArrayList<String>) e.getValue();
			defs.insertSerialValues(("" + UUID.randomUUID()).substring(0, 6), vals.get(0), DEFAULTCARDBACK, vals.get(1), (int) e.getKey());
		}

		// Set up the JSON Writer and Parser
		DefaultPrettyPrinter p = new DefaultPrettyPrinter().withoutSpacesInObjectEntries();
		DefaultPrettyPrinter.Indenter indent = new DefaultIndenter(" ", "\n");
		p.indentArraysWith(indent);
		p.indentObjectsWith(indent);
		mapper.setDefaultPrettyPrinter(p);
		writ = mapper.writer(p).withRootValueSeparator("");
		
		// Serialize DeckDefaults and write it to a file
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


	public static String processIllegalCharacters(String inp)
	{
		out.println("Checking for and fixing any illegal characters in input...");
		String outp = inp;
		// Accent E character '�'
		if (outp.matches(illegalRegex[0]))
			outp = outp.replaceAll(illegalRegex[0], replacements[0]);
		// Element symbol (common in special energies like Heat {R} Energy)
		else if (outp.matches(illegalRegex[1]))
			outp = outp.replaceAll(illegalRegex[1], replacements[1]);
		// Common corrupt encodings of accent E
		else if (outp.matches(illegalRegex[2]))
			outp = outp.replaceAll(illegalRegex[2], replacements[2]);
		// Promo sub-set tag, ie Alakazam V PR-SW 83 which can be fetched under SW 83 instead
		else if (outp.matches(illegalRegex[3]))
			outp = outp.replaceAll(illegalRegex[3], replacements[3]);
		return outp;
	}


	public static void parseInputFile(File f) throws FileNotFoundException
	{
		Scanner file = new Scanner(f);
		while (file.hasNext())
		{
			String line = file.nextLine().trim();

			// Check for lines to skip
			if (line.length() <= 3)
				continue;
			if (line.substring(0, 3).equals("***") || line.contains("***"))		// Using equals() AND contains() both because my old parser was missing the first line for some reason
				continue;
			if (line.substring(0, 3).equals("##") || line.contains("##"))
				continue;
			if (line.contains("Total"))
				continue;

			// Filter out illegal characters by special case first
			if (line.matches(illegalRegex[4]))
			{
				line = line.replaceAll(illegalRegex[0], replacements[0]);
				line = line.replaceAll(illegalRegex[1], replacements[1]);
				line = line.replaceAll(illegalRegex[2], replacements[2]);
				line = line.replaceAll(illegalRegex[3], replacements[3]);
			}
			// Filter out any other special characters and trim
			line = line.replaceAll("!@#$%&*()-=+'\"", "").trim();
			
			if(line.substring(0, 2).equals("* "))
				line = line.substring(2);
			
			out.println("Input string processed:: " + line);

			// Use 2 string arrays and a split to compress the name into 1 index
			String[] firstpass = line.split(" ");	// Cut down string from before
			String[] secondpass = new String[5];	// 5 indices to hold an energy flag, count, card name, set ID, and set number
			for (int i = 0; i < firstpass.length; i++)				// Trim each string in array for consistency
			{
				firstpass[i] = firstpass[i].trim();
			}
			for(int i = 0;i<5;i++) { secondpass[i] = ""; }
			
			
			if (line.toUpperCase().contains("ENERGY"))
				secondpass[0] = "E";
			else if(!line.toUpperCase().contains("ENERGY"))
				secondpass[0] = "N";
			
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
					secondpass[i+1] = firstpass[i];
			}
			decklist.add(secondpass);
		}
	}

	public static int getErrorCount()
	{ return errorcards.size(); }

	public static String fetchCardID(String name)
	{
		return pokedex.get(name);
	}
}


/*
 * while (decklist.hasNext())
 * {
 * out.println("Preparing request...");
 * // Auto-skips any lines starting with an asterisk
 * String raw;
 * String faceurl = "";
 * try
 * {
 * raw = decklist.nextLine().trim();
 * out.println("Processing input line " + raw);
 * // Hard-skip title line if present
 * if (raw.substring(0, 7).equals("******"))
 * {
 * out.println("\n[DEBUG] Title Line Case Break");
 * continue;
 * }
 * raw = processIllegalCharacters(raw);
 * if(raw.substring(0,2).equals("##"))
 * continue;
 * else if(raw.substring(0,2).equals("* "))
 * {
 * raw = raw.substring(2);
 * }
 * else
 * continue;
 * // Copy raw input into a test string to make checking for energy easier
 * String nrg = raw.replaceAll(" [0-9]+", "");
 * out.println("[[DEBUG]] Replaced string input for energy checking:: " + nrg);
 * if (raw.contains("Energy"))
 * {
 * out.println("Possible Basic Energy card found with " + raw + ", checking...");
 * if (raw.contains("Fire"))
 * faceurl = ENERGYFIRE;
 * if (raw.contains("Grass"))
 * faceurl = ENERGYWATER;
 * if (raw.contains("Water"))
 * faceurl = ENERGYFIRE;
 * if (raw.contains("Darkness"))
 * faceurl = ENERGYDARK;
 * if (raw.contains("Fighting"))
 * faceurl = ENERGYFIGHTING;
 * if (raw.contains("Fairy"))
 * faceurl = ENERGYFAIRY;
 * if (raw.contains("Lightning"))
 * faceurl = ENERGYELECTRIC;
 * if (raw.contains("Metal"))
 * faceurl = ENERGYSTEEL;
 * if (raw.contains("Psychic"))
 * faceurl = ENERGYPSYCHIC;
 * if (faceurl.length() > 0)
 * {
 * out.println("A Basic Energy (" + raw + ") was found and manually serialized to avoid errors.");
 * }
 * }
 * }
 * catch (Exception n)
 * {
 * continue;
 * }
 * // Some cards have long multi-word names (ie. Battle Compressor Team Flare Gear), so a single split() wont be enough to properly format all cards
 * String[] firstpass = raw.split(" "); // Raw input from nextLine()
 * String[] secondpass = new String[4]; // 4 indices to hold count, card name, set ID, and set number
 * // String faceurl = "";
 * out.println("Line in:: " + raw);
 * // Run through a formatting step if card name is longer than 1 word
 * if (firstpass.length > 4)
 * {
 * // Check for basic energy.
 * out.println(firstpass[2].trim());
 * // Luckily the set number and name are never more than 1 word, so we can just grab the last 2 elements
 * secondpass[3] = firstpass[firstpass.length - 1];
 * secondpass[2] = firstpass[firstpass.length - 2];
 * secondpass[0] = firstpass[0];
 * String reformat = "";
 * // Collapse the extra indices in firstpass into one string
 * for (int i = 1; i < firstpass.length - 2; i++) reformat += firstpass[i] + " ";
 * secondpass[1] = reformat.trim();
 * }
 * else secondpass = firstpass;
 * if (showDebugLogs)
 * out.println("[FETCHREQUEST] Parsed request:: " + secondpass[1] + " - " + secondpass[2] + " " + secondpass[3] + ", Count - " + secondpass[0]);
 * // Prepare the search term for jSoup using plus signs so it can be inserted as a search term field in the URL
 * String qry = secondpass[1] + "+" + secondpass[2] + "+" + secondpass[3];
 * if (qry.contains("PR-"))
 * qry = qry.replace("PR-", "");
 * Document pkmncards = Jsoup.connect(cardDB + qry).get();
 * // Search the HTML source for the CSS tag correlating to the card's full-resolution image link
 * if (faceurl.length() == 0)
 * {
 * try
 * {
 * // Tells jSoup to search the webpage source for a CSS class with the "a" tag named "card-image-link" and save the contents of "href" as a string
 * faceurl = pkmncards.select("a.card-image-link").first().attr("abs:href");
 * // Cards with promos, premade-deck versions, or alt versions may have multiple search results. Solve by opening new connection at current faceurl
 * if (faceurl.contains("https://pkmncards.com/card/"))
 * out.println("Multiple versions of this card found, please wait!");
 * while (faceurl.contains("https://pkmncards.com/card/"))
 * {
 * Document subpage = Jsoup.connect(faceurl).get();
 * faceurl = subpage.select("a.card-image-link").first().attr("abs:href");
 * }
 * }
 * catch (Exception e)
 * {
 * // Simple error report if above web-crawler fails. Usually means the card doesn't exist, isn't in the pkmncards database, or someone can't spell
 * if (secondpass[1].contains("Energy"))
 * {
 * if (showDebugLogs)
 * out.println("Found set-agnostic energy card " + qry + " with no database entry, will be set manually.");
 * faceurl = "";
 * }
 * else
 * {
 * if (showDebugLogs)
 * out.println("Error parsing card with query " + qry + ", fetched URL was malformed!");
 * errorcards.add("" + secondpass[1] + " " + secondpass[2] + " " + secondpass[3]);
 * }
 * }
 * }
 * // Necessary reassignment of the number to avoid it being overwritten
 * int tempcardcount = cardcount;
 * int countId = (cardcount);
 * for (int y = 0; y < Integer.parseInt(secondpass[0]); y++)
 * {
 * deckids.add(countId);
 * out.println("Generated CardID of " + ((int) tempcardcount + y));
 * pokedex.put(secondpass[1], "" + (countId));
 * ++cardcount;
 * defs.insertSerialValues("" + tempcardcount, faceurl, DEFAULTCARDBACK.trim(), secondpass[1].trim(), "" + tempcardcount);
 * }
 * // Prevent the program from hugging the website to death, uwu
 * Thread.sleep(250);
 * }
 * // Create the master jsonroot object
 * DefaultPrettyPrinter p = new DefaultPrettyPrinter().withoutSpacesInObjectEntries();
 * DefaultPrettyPrinter.Indenter ind = new DefaultIndenter(" ", "\n");
 * p.indentArraysWith(ind);
 * p.indentObjectsWith(ind);
 * mapper.setDefaultPrettyPrinter(p);
 * writ = mapper.writer(p).withRootValueSeparator("");
 * // Serialize DeckDefaults without root nesting
 * String jsondefaults = "";
 * jsondefaults = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(defs);
 * json = mapper.readValue(jsondefaults, Object.class);
 * writ.writeValue(Paths.get(filePath + "\\" + deckName + ".json").toFile(), json);
 * if (getErrorCount() > 0)
 * {
 * out.println("\nSome parsing errors were detected. Here are the cards that could not be parsed into the deck: ");
 * for (String s : errorcards) out.println(s);
 * }
 * out.println("JSON file parsing succeeded, starting image stitching...");
 */
