package andrielgaming;

import static java.lang.System.out;
import java.io.File;
import java.util.Scanner;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import andrielgaming.parsing.TabletopParser;

/* FUNCTIONALITY:: Core routines all seem to work properly
 * 
 */
@JsonIgnoreProperties(value =
{ "deckIDs", "cardset", "customDeck", "nickname", "cardid", "cardId", "cardID" })
public class Pokegear
{
	// Default file path with system user injected, usually works
	private static String usr = System.getProperty("user.name");
	private static String defPath = "C:\\Users\\" + usr + "\\Documents\\My Games\\Tabletop Simulator\\Saves\\Saved Objects\\"; // Prompt for different filepath &
	private static Scanner s = new Scanner(System.in);

	public static void main(String[] args)
	{
		// OS detection removed from this version- to be reimplemented in GUI class
		out.println("Welcome to PokeGear CLI Client!\nTo help the future transition to a full GUI, all input has changed into single-character inputs.\nPlease only enter the whole number matching what you want to do or we cannot double-check it before running. ");
		File dirs = new File(defPath);

		// Simplified filepath checking
		if (dirs.exists())
		{
			startDeckImport();
		}
		else
		{
			out.println("Default path " + defPath + " cannot be opened, it either does not exist or is access-protected.");
			out.println("Double check the file path to your TTS 'Saved Objects' folder or enter an alternate location to save the deck list to.");
			out.println("Enter full path and press <ENTER> when done:: ");
			String correction = s.nextLine().trim();
			dirs = new File(correction);
		}
	}

	// Method to start the parsing process
	public static void startDeckImport()
	{
		// out.println("\rWould you like to import a deck? {Y/N} ");
		// char ans = s.nextLine().trim().toUpperCase().charAt(0);
		// String inp = "";
		char ans = 'Y';
		if (ans == 'Y')
		{
			// Pokemon deck shouldn't have any more than 60 cards, so this will loop 60
			// times.
			// First loop iteration includes JSON file setup.
			out.println("\rPlease enter a name for this deck:: ");
			String name = s.nextLine().trim();
			
			out.println("Paste your deck list into this text file, save it, and close it.");
			try
			{
				String nameWithExtension = name + ".txt";
				File temp = new File(defPath + nameWithExtension);
				temp.createNewFile();
				java.awt.Desktop.getDesktop().edit(temp);
				out.println("Once you are done, click into this window and hit ENTER to continue...");
				System.in.read();
				
				out.print("\rVerifying file was saved...");
				if (temp.exists())
					out.println("\rVerified that file exists on drive!");
				else
				{
					out.println("\rAn error has occurred. Either you did not save the text file, I don't have access privileges, or the path is wrong on my end.");
					System.exit(1);
				}
				

				/*String temp = "";
				if (temp.length() <= 1)
				{
					out.println("Paste your deck list into this window and hit <ENTER>...");
					//System.in.read();
					while (s.hasNextLine())
					{
						temp = s.nextLine().trim() + "\n";
					}
				}*/

				out.println("Pokegear will now attempt to parse this decklist! \n NOTE:: Please be patient! As a courtesy, there is a pre-programmed cushion to prevent the program from hugging the server to death.");
				boolean success = TabletopParser.doParse(temp, defPath, name, true);

				if (success)
				{
					// TODO - Add option to parse another deck
					out.println("Deck list parsed! Check in your Saved Objects in Tabletop Simulator for your new deck.");
					System.exit(0);
				}
				else
				{
					out.println("The parser detected that not all cards were successfully imported. " + TabletopParser.getErrorCount() + " cards were marked as errors or could not be fetched.\nBelow is a list of the imports not recognized or fetched: ");
				}
			} catch (Exception e)
			{
				out.println("Oopsie poopsie doopsie I did a fucky wucky, sorry about that! I committed the following war-crime:: " + e.toString());
				e.printStackTrace();
				System.exit(1);

				out.println("Thanks for using Pokegear!");
				System.exit(0);
			}
		}
		else
		{
			out.println("Well... fine then!");
			System.exit(0);
		}
	}
}
