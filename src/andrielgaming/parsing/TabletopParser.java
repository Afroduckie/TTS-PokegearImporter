package andrielgaming.parsing;

import static java.lang.System.out;

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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.ProgressBar;
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
@JsonIgnoreProperties(value =
{ "deckIDs", "deckids", "cardset", "customDeck", "nickname", "cardid", "cardId", "cardID" })
public class TabletopParser implements Runnable
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
	private static ArrayList<String[]> decklist = new ArrayList<>();														// ArrayList containing string arrays with each line of input from decklist
	private static ArrayList<String> cardlist = new ArrayList<>();
	public static LinkedTreeMap<Integer, Integer> instanceIDs = new LinkedTreeMap<Integer, Integer>();								// Key is the UNIQUE ID, value is the CARD ID
	public static LinkedTreeMap<Integer, ArrayList<String>> instanceURLs = new LinkedTreeMap<Integer, ArrayList<String>>();

	// Map for reverse lookup of IDs by nickname
	private static TreeMap<String, String> pokedex = new TreeMap<>();
	// String for regex processing illegal characters
	private static String[] illegalRegex =
	{ "�", "{.}", "é", "^", "[�{*}é]" };
	private static String[] replacements =
	{ "e", "", "e", "" };

	private static String filePath = new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + "\\My Games\\Tabletop Simulator\\Saves\\Saved Objects\\";
	private static String deckName;
	private static ProgressBar progressBar;
	public static List guiDeckList;
	public int index;

	@SuppressWarnings("rawtypes")
	public static void setParseVars(String f, String fpath, String name, boolean showDebugLogs, ProgressBar p, List decklistGUI) throws StreamWriteException, DatabindException, IOException, InterruptedException
	{
		// Send the file off to the formatter to pretty it up
		// NOTE:: Changed from 'File f' to 'String f' for decoupling, issues may crop up
		parseInputFile(f);
		filePath = new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + "\\My Games\\Tabletop Simulator\\Saves\\Saved Objects\\";
		deckName = name;
		progressBar = p;
		guiDeckList = decklistGUI;
		out.println("Input file has been processed, starting the parsing step!");

		// Set a few important variables before starting
		errorcards = new ArrayList<>();									// Arraylist containing any cards that failed to parse so user can be notified
		mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);	// Create the mapper and enable that stupid indenter I didn't know I needed
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);				// Workaround to trick Jackson into serializing an empty object
	}

	public static void setPath(String path)
	{
		filePath = path;
	}

	public int getIndex()
	{
		return index;
	}

	// Basically just sets all variables back to null
	public static void resetDeck()
	{
		cardset = new TreeMap<>();
		mapper = null;
		writ = null;
		decklist = new ArrayList<>();														// ArrayList containing string arrays with each line of input from decklist
		cardlist = new ArrayList<String>();
		errorcards = new ArrayList<String>();
		instanceIDs = new LinkedTreeMap<Integer, Integer>();								// Key is the UNIQUE ID, value is the CARD ID
		instanceURLs = new LinkedTreeMap<Integer, ArrayList<String>>();
		pokedex = new TreeMap<>();
		filePath = null;
		deckName = null;
		progressBar = null;
		guiDeckList = null;
	}

	// Parsing functionality moved into a runnable, executed from GUI class
	@Override
	public void run()
	{
		index = 0;
		// Display var for sending data to GUI
		Display d = Display.getDefault();

		// String faceurl = "";
		int cardindex = 1;
		boolean thumb = false;
		// Send the decklist to the GUI container for display
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				progressBar.setMaximum(decklist.size());
				progressBar.setSelection(0);
				progressBar.setVisible(true);
			}
		});

		for (String[] query : decklist)
		{
			String faceurl = "";
			String flag = query[0];
			String count = query[1];
			String name = query[2];
			String set = query[3];
			String num = query[4];

			// Send a tick to the progress bar
			ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
			scheduler.schedule(() ->
			{
    			Display.getDefault().asyncExec(new Runnable()
    			{
    				@Override
    				public void run()
    				{
    					progressBar.setSelection(index += 1);
    				}
    			});
			}, 2000, TimeUnit.MILLISECONDS);
			
			boolean specialEnergyFlag = false;

			// Check for energy card
			if (flag.equals("E"))
			{
				if (name.equals("Fire")) 		faceurl = ENERGYFIRE;
				if (name.equals("Grass")) 		faceurl = ENERGYGRASS;
				if (name.equals("Water")) 		faceurl = ENERGYWATER;
				if (name.equals("Darkness")) 	faceurl = ENERGYDARK;
				if (name.equals("Fighting")) 	faceurl = ENERGYFIGHTING;
				if (name.equals("Fairy")) 		faceurl = ENERGYFAIRY;
				if (name.equals("Lightning")) 	faceurl = ENERGYELECTRIC;
				if (name.equals("Metal")) 		faceurl = ENERGYSTEEL;
				if (name.equals("Psychic"))		faceurl = ENERGYPSYCHIC;
				else
				{
					out.println("[WARN] Card flagged as energy but not caught, will run through normal parsing instead:: " + name + ", " + set + " " + num + " [Count : " + count + "]");
					specialEnergyFlag = true;
				}
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
						// NOTE:: Switched the logic to use only set and setnum, should help with some edge-case failures
						if (!specialEnergyFlag)
							pkmncards = Jsoup.connect(cardDB + tempname + "+" + set + "+" + num).get();
						else
							pkmncards = Jsoup.connect(cardDB + set + "+" + num).get();
					} catch (Exception e)
					{
						// Almost certainly an 'agnostic promo' if it falls through here, so try fetch again and replace vars if it succeeds
						if (promo) if ((pkmncards = Jsoup.connect(cardDB + tempname.replaceAll("+PR", "") + "+" + "PROMO" + "+" + num).get()) != null)
						{
							tempname = tempname.replaceAll("+PR", "");
							set = "PROMO";
						}
					}

					// TODO -- Replace with a less jank fucking rate throttle because you want to believe you're better than this

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
					if (!thumb) // In a nutshell- rip the image, decode it, re-encode it, save to disk
						try
					{
						URL urlImage = new URL(faceurl);
						InputStream in = urlImage.openStream();
						byte[] buffer = new byte[4096];
						int n = -1;
						OutputStream os = new FileOutputStream(filePath + "\\" + deckName + ".png");
						while ((n = in.read(buffer)) != -1)
							os.write(buffer, 0, n);
						os.close();
						out.println("Image saved to disk as thumbnail.");
						thumb = true;
					} catch (IOException e)
					{
						e.printStackTrace();
					}
				}

				// NOTE:: Do NOT ever change this line! EVERYTHING WILL BREAK AND I DON'T KNOW WHY
				int id = cardindex * 100;
				for (int i = 1; i <= Integer.parseInt(count); i++)
				{
					// Loop through deck to assign deck & card IDs for all parsed cards
					int instID = id + i * 10;
					instanceIDs.put(instID, id);
					ArrayList<String> temp = new ArrayList<>();
					temp.add(faceurl);
					temp.add(name);
					instanceURLs.put(instID, temp);
				}
				// Increment cardindex by number of this card present in deck
				cardindex += Integer.parseInt(count);
			} catch (Exception n)
			{
				out.print("Whoops, I pressed the 'war crime' button, and the U.N. will soon convene to strongly condemn the following affront to humanity:: ");
				n.printStackTrace();
				out.println();
				errorcards.add(temp2 + "+" + set + "+" + num);
			}
		}

		// Once basic data parsed, get
		Object json = null;																					// Blank Object instance needed to trick Jackson into behaving itself (and/or to make up for my own ignorance)
		ArrayList<Integer> deckids = new ArrayList<>();												// Arraylist of "deck IDs" for serialization to the final JSON file
		DeckDefaults defs = new DeckDefaults(deckids, cardset);												// Container class that serves as a 'template' for the JSON format TTS uses for Custom Deck objects

		// Pass all of our generated IDs to the container classes
		for (Entry e : instanceIDs.entrySet())
			deckids.add((int) e.getValue());
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
		try
		{
			jsondefaults = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(defs);
			json = mapper.readValue(jsondefaults, Object.class);
			writ.writeValue(Paths.get(filePath + "\\" + deckName + ".json").toFile(), json);
			jsondefaults = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(defs);
		} catch (Exception e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Confirm success and stop the progress bar.
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if(!errorcards.isEmpty())
				{
					String[] errors = new String[errorcards.size()];
					int i = 0;
					for(String s : errorcards)
					{
						errors[i] = s;
						i++;
					}
					guiDeckList.setItems(errors);
				}
				else
				{
					guiDeckList.setItems("");
					guiDeckList.add("No errors found!");
				}
				guiDeckList.setVisible(true);
				progressBar.setVisible(false);
			}
		});
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

	public static void parseInputFile(String f) throws FileNotFoundException
	{
		Scanner file = new Scanner(f);
		while (file.hasNext())
		{
			String line = file.nextLine().trim();
			String white = "" /* dummy empty string for homogeneity */
					+ "\\u0009" // CHARACTER TABULATION
					+ "\\u000A" // LINE FEED (LF)
					+ "\\u000B" // LINE TABULATION
					+ "\\u000C" // FORM FEED (FF)
					+ "\\u000D" // CARRIAGE RETURN (CR)
					+ "\\u0020" // SPACE
					+ "\\u0085" // NEXT LINE (NEL)
					+ "\\u00A0" // NO-BREAK SPACE
					+ "\\u1680" // OGHAM SPACE MARK
					+ "\\u180E" // MONGOLIAN VOWEL SEPARATOR
					+ "\\u2000" // EN QUAD
					+ "\\u2001" // EM QUAD
					+ "\\u2002" // EN SPACE
					+ "\\u2003" // EM SPACE
					+ "\\u2004" // THREE-PER-EM SPACE
					+ "\\u2005" // FOUR-PER-EM SPACE
					+ "\\u2006" // SIX-PER-EM SPACE
					+ "\\u2007" // FIGURE SPACE
					+ "\\u2008" // PUNCTUATION SPACE
					+ "\\u2009" // THIN SPACE
					+ "\\u200A" // HAIR SPACE
					+ "\\u2028" // LINE SEPARATOR
					+ "\\u2029" // PARAGRAPH SEPARATOR
					+ "\\u202F" // NARROW NO-BREAK SPACE
					+ "\\u205F" // MEDIUM MATHEMATICAL SPACE
					+ "\\u3000" // IDEOGRAPHIC SPACE
			;
			// Check for lines to skip
			if ((line.length() <= 3) || (!(line.charAt(0) + "").matches("[0-9]") && !line.substring(0, 2).equals("* "))) continue;

			line = line.replaceAll("&", "%26");
			line = line.replaceAll("PR-BLW", "PR+BWP");
			line = line.replaceAll("PR-", " PR+");
			line = line.replaceAll("\\{.\\}", "");
			line = line.replaceAll("\\* ", "");
			line = line.replaceAll("é", "e");
			line = line.replaceAll("[^a-zA-Z0-9%&-{\\s+}]", ""); 	// FIXME:: This SHOULD remove any non-compliant characters, test with the Vikavolt V list with the Prism Star Tapu Koko in it

			// Report to console
			out.println("Input string processed:: " + line);

			// Use 2 string arrays and a split to compress the name into 1 index. Array secondpass has 5 indices to hold an energy flag, count, card name, set ID, and set number
			String[] firstpass = line.split(" ");
			String[] secondpass = new String[5];

			// Skip line if firstpass splits to 3 or less, indicating a required field is missing
			if (firstpass.length <= 3) continue;

			for (int i = 0; i < firstpass.length; i++)
				firstpass[i] = firstpass[i].trim();
			for (int i = 0; i < 5; i++)
				secondpass[i] = "";

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
				for (int i = 0; i < firstpass.length; i++)
					secondpass[i + 1] = firstpass[i];
			out.println("[[DEBUG]] Contents of SECONDPASS array:: ");
			for (String s : secondpass)
				out.println(s);
			out.println("------");
			decklist.add(secondpass);
			cardlist.add(secondpass[1].replaceAll("%26", "&") + " " + secondpass[2] + " " + secondpass[3]);
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
