package andrielgaming.parsing;

import static java.lang.System.out;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.SwingWorker;

// Eclipse SWT and Core Runtime Imports
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.ProgressBar;
// JSoup Imports
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

// Jackson and GSON Imports
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.internal.LinkedTreeMap;

// PokeGear Imports
import andrielgaming.parsing.jsonroots.DeckDefaults;
import andrielgaming.ui.PokegearWindow;
import andrielgaming.utils.LinkEnums;

// Somewhat unnecessary but sanitycheck-level annotation in MOST classes to prevent invalid root fields
@JsonIgnoreProperties(value =
{ "deckIDs", "deckids", "cardset", "customDeck", "nickname", "cardid", "cardId", "cardID" })
public class TabletopParser
{
	// Card faces and other hard-coded links moved to #LinkEnums
	// User can change this in GUI as of version 1.4
	public static String chosenCardBack = null;
	public static BufferedWriter threadComWriter = new BufferedWriter(new OutputStreamWriter(System.out));
	public static String name;
	public static String num;
	public static String count;
	public static String set;

	public static boolean execfinished = true;

	// Generalized URL for jSoup to fill in search terms with
	private static final String cardDB = "https://pkmncards.com/?s=";

	// Jackson JSON Parser Setup
	public static TreeMap<String, LinkedTreeMap<String, Object>> cardset = new TreeMap<>();
	public static ObjectMapper mapper;
	public static ObjectWriter writ;
	public static ArrayList<String> errorcards;

	// Collections needed for building the deck
	public static ArrayList<String[]> decklist = new ArrayList<>();														// ArrayList containing string arrays with each line of input from decklist
	public static ArrayList<String> cardlist = new ArrayList<>();
	public static LinkedTreeMap<Integer, Integer> instanceIDs = new LinkedTreeMap<Integer, Integer>();								// Key is the UNIQUE ID, value is the CARD ID
	public static LinkedTreeMap<Integer, ArrayList<String>> instanceURLs = new LinkedTreeMap<Integer, ArrayList<String>>();

	// Map for reverse lookup of IDs by nickname
	public static TreeMap<String, String> pokedex = new TreeMap<>();
	// String for regex processing illegal characters
	public static String[] illegalRegex =
	{ "�", "{.}", "é", "^", "[�{*}é]" };
	public static String[] replacements =
	{ "e", "", "e", "" };

	public static String filePath = PokegearWindow.getPath(); //new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + "\\My Games\\Tabletop Simulator\\Saves\\Saved Objects\\";
	public static String deckName;
	public static ProgressBar progressBar;
	public static List guiDeckList;
	public static int index;
	public static String line;

	public static Queue<ParsingThread> workers;
	public static int cardindex;
	static boolean thumb = false;
	public static int loadBarIndex;
	private static Display parent;
	public static boolean threadFlag = false;
	public static ArrayList<String> tooltips;

	/***
	 * 	Creates an ArrayList of ParsingThreads, one per each unique card, and enqueues each in-order to regulate threaded work for the UI threads.
	 * 	Does NOT start the work, just sets work up
	 */
	@SuppressWarnings("rawtypes")
	public static void parse(String f, String fpath, String name, boolean showDebugLogs, ProgressBar p, List decklistGUI, Display p2) throws StreamWriteException, DatabindException, IOException, InterruptedException
	{
		if(chosenCardBack == null)
		{
			chosenCardBack = LinkEnums.DEFAULTCARDBACK;
		}
		filePath = PokegearWindow.getPath();
		tooltips = new ArrayList<String>();
		loadBarIndex = 1;
		TabletopParser.parent = p2;
		execfinished = false;
		Scanner file = new Scanner(f);
		while (file.hasNext())
		{
			line = file.nextLine().trim();
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
			line = line.replaceAll("[^a-zA-Z0-9%&-{\\s+}]", "");

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
			decklist.add(secondpass);
			cardlist.add(secondpass[1].replaceAll("%26", "&") + " " + secondpass[2] + " " + secondpass[3]);
		}

		// Initialize the values needed to start parsing all the worker threads
		filePath = PokegearWindow.getPath();
		deckName = name;
		progressBar = p;
		guiDeckList = decklistGUI;
		out.println("Input file has been processed, starting the parsing step!");
		errorcards = new ArrayList<>();									// Arraylist containing any cards that failed to parse so user can be notified
		mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);	// Create the mapper and enable that stupid indenter I didn't know I needed
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);				// Workaround to trick Jackson into serializing an empty object

		parent.asyncExec(() ->
		{
			PokegearWindow.addOutputInformation("Found TTS filepath at following dir-- " + filePath);
		});

		// Creating a new class to run the parsing, each to a thread. Starts by adding each decklist query as a ParsingThread object

		// out.println("Worker Queue is finished with size " + workers.size());
		index = 0;
		Display d = Display.getDefault();
		cardindex = 1;
		thumb = false;

		parent.asyncExec(() ->
		{
			PokegearWindow.progressBar.setMaximum(decklist.size());
			PokegearWindow.progressBar.setSelection(0);
			PokegearWindow.progressBar.setVisible(true);
			out.println("UI elements initialized and set in async UI thread, queueing the worker threads now.");
		});

		// System.out.println("Is SwingWorker UI thread running? - " + updateProgBar.getState());

		run();
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
		thumb = false;
	}

	// Parsing functionality moved into a runnable, executed from GUI class
	public static void run() throws InterruptedException, IOException
	{
		out.println("Begin processing worker threads---");
		workers = new LinkedList<ParsingThread>();
		ParsingThread prev = null;
		out.println("DEBUG------- Query Size: " + decklist.size());
		for (String[] query : decklist)
		{
			if (prev == null)
			{
				prev = new ParsingThread(query, parent);
				workers.add(prev);
			}
			else
			{
				ParsingThread temp = new ParsingThread(query, parent, prev);
				workers.add(temp);
				prev = temp;
			}
		}
		// Set up each thread as a SwingWorker so that it can run, return results, and allow UI thread to unblock and update
		// out.println("Checking state of top worker in queue:: " + workers.peek().getState());
		SwingWorker runner = new SwingWorker()
		{
			@Override
			public Object doInBackground()
			{
				int size = workers.size();
				ParsingThread current = null;
				ParsingThread prev = null;
				out.println("DEBUG---------- Worker queue size " + workers.size());
				while(!workers.isEmpty())
				{
					current = workers.poll();
					if(prev != null)
					{
						synchronized(current)
						{
							try
							{
								prev.join();
							} catch (InterruptedException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					if(current.getState() == Thread.State.NEW)
					{
						current.start();
						while(current.isAlive())
						{}
					}
					prev = current;
				}
				return null;
			}

			@Override
			public void done()
			{
				TabletopParser.finalizeParsingRun();
			}
		};
		runner.execute();

		/* ||--------------------------------------------------------------------------------------------||
		 * || Once above block is done, all SwingWorkers should have finished their work and terminated. ||
		 * || SwingWorker above is intended to call the finalizer method once its runs are completed.    ||
		 * ||--------------------------------------------------------------------------------------------||
		 */
	}

	public static void finalizeParsingRun()
	{
		// Using a blank object (even a null one) allows the Jackson ObjectMapper to better map existing objects, so here we have a blank object
		Object json = null;
		// Next 2 vars are a list of "deck IDs" that will allow the JSON deck to place and reference cards as JSON objects
		ArrayList<Integer> deckids = new ArrayList<>();
		// DeckDefaults is a template class containing the identifying info TTS needs to know the JSON object is a deck of cards
		DeckDefaults defs = new DeckDefaults(deckids, cardset);

		// Pass all of our generated IDs to the container classes and make the appropriate assignments
		for (Entry e : instanceIDs.entrySet())
			deckids.add((int) e.getValue());
		defs.setDeckIDs(deckids);

		// Write the info for each distinct card, including a random GUID that for some reason still works in TTS
		int tooltipindex = 0;
		Entry prev = null;
		for (Entry e : instanceURLs.entrySet())
		{
			if(tooltipindex == 0)
			{
				prev = e;
			}
			
			@SuppressWarnings("unchecked")
			ArrayList<String> vals = (ArrayList<String>) e.getValue();
			out.println("Tooltips list size:: " + tooltips.size());
			defs.insertSerialValues(("" + UUID.randomUUID()).substring(0, 6), vals.get(0), chosenCardBack, vals.get(1), (int) e.getKey(), tooltips.get(tooltipindex));
			if(!e.equals(prev))
			{
				tooltipindex++;
				prev = e;
			}
		}

		// Configure JSON printer and set it to print more-readable line indents.
		DefaultPrettyPrinter p = new DefaultPrettyPrinter().withoutSpacesInObjectEntries();
		DefaultPrettyPrinter.Indenter indent = new DefaultIndenter(" ", "\n");	// Without this line everything is on one line
		p.indentArraysWith(indent);
		p.indentObjectsWith(indent);
		mapper.setDefaultPrettyPrinter(p);
		// Following line is necessary if you want any chance in hell of reading the output file without developing a migraine
		writ = mapper.writer(p).withRootValueSeparator("");

		// Serialize DeckDefaults and write it to our JSON file
		String jsondefaults = "";
		try
		{
			// Writes the DeckDefaults object containing all of our imported data by first writing it to a String and then writing to JSON file
			jsondefaults = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(defs);
			json = mapper.readValue(jsondefaults, Object.class);
			writ.writeValue(Paths.get(filePath + "/" + deckName + ".json").toFile(), json);
			jsondefaults = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(defs);
		} catch (Exception e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Error catching mechanism takes its final step to report any parsing issues encountered to the UI thread
		if (!errorcards.isEmpty())
		{
			String[] errors = new String[errorcards.size()];
			int i = 0;
			for (String s : errorcards)
			{
				// Send card line as a string with 'e|' command flag so UI thread denotes it as an error
				parent.asyncExec(() ->
				{
					PokegearWindow.addErrorInformation(s);
				});
				synchronized (PokegearWindow.composite_3)
				{
					// FIXME - This will probably throw SWTException for "invalid thread access", but we gotta try lol
					PokegearWindow.composite_3.getShell().getDisplay().readAndDispatch();
				}
			}
		}
		else
		{
			parent.asyncExec(() ->
			{
				PokegearWindow.addErrorInformation("[ No Errors Reported ]");
			});
		}

		// Last step is to notify the UI that we've finished execution
		parent.asyncExec(() ->
		{
			PokegearWindow.addOutputInformation("[ FINISHED ] Deck has been imported into Tabletop Simulator! Check the error log to the right in case any cards are missing.");
			PokegearWindow.progressBar.setVisible(false);
		});
		execfinished = true;
		chosenCardBack = null;
	}

	// Static function for any class to filter invalid unicode text
	public static String filterString(String input)
	{
		String formatted = input;
		formatted = formatted.replaceAll("Pok.", "Poke");				// Remove invalid accent 'e' in Pokemon
		formatted = formatted.replaceAll("opponent.s", "opponent's");	// PKMNCards.com uses an invalid unicode apostrophe
		formatted = formatted.replaceAll("n.t", "n't");				
		formatted = formatted.replaceAll("Pok.mon.s", "Pokemon's");
		return formatted;
	}
	
	public static boolean runComplete()
	{
		return execfinished;
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
}
