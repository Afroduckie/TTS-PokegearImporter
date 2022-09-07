package andrielgaming.parsing;

import static java.lang.System.out;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import andrielgaming.ui.PokegearWindow;
import andrielgaming.utils.LinkEnums;

public class ParsingThread extends Thread
{
	public String[] query;
	public boolean error = false;
	public boolean specialEnergyFlag = false;
	public String url = "";
	public Display parent;
	private static String cardDB = "https://pkmncards.com/?s=";

	/**
	 * Threaded UI classes assume an instruction character using a pipe character | as a delimiter:
	 * 		m | <contents>
	 * 				UI class takes <contents> to be a normal output message
	 *		r | <contents>
	 *				UI class takes <contents> to be a resultSet and prints to the CardList UI element
	 */

	public ParsingThread(String[] query, Display parent)
	{
		this.query = query;
		System.out.print("Created worker thread for query- ");
		for (String q : query)
			out.print(q + " ");
		out.println("\nAdding this query to PreParseList...");

		String q = "";
		for (String iq : query)
			q += " " + iq;
	}

	public ParsingThread(String[] query, Display parent, ParsingThread ancestor)
	{
		this.query = query;
		System.out.print("Created worker thread for query- ");
		for (String q : query)
			out.print(q + " ");
		out.println("\nAdding this query to PreParseList...");
		this.parent = Display.getDefault();

		String q = "";
		for (String iq : query)
			q += " " + iq;

		try
		{
			ancestor.join();
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String[] getQuery()
	{
		return query;
	}

	@Override
	public void run()
	{
		Display.getDefault().wake();
		Display.getDefault().syncExec(() ->
		{
			Display.getDefault().readAndDispatch();
		});

		String flag = query[0];
		String count = query[1];
		String name = query[2];
		String set = query[3];
		String num = query[4];
		int cardindex = 1;
		boolean thumb = false;

		System.out.print("Began worker thread for query- ");
		for (String q : query)
			out.print(q + " ");
		out.println();

		try
		{
			Thread.sleep(3000);
		} catch (InterruptedException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		specialEnergyFlag = true;
		url = "";

		// Attempt to parse the card

		Display.getDefault().wake();
		Display.getDefault().asyncExec(() ->
		{
			// Grab URL and thumbnail, if applicable
			try
			{
				url = getImageSourceUrl(query, specialEnergyFlag, thumb);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String outp = "";
			if (count.trim().equalsIgnoreCase("1"))
			{
				outp += "1 Copy of ";
			}
			else
			{
				outp += "" + count + " Copies of ";
			}
			outp += name + ", Set " + set + "[" + num + "]";
			final String sender = outp;

			PokegearWindow.addCardInformation(sender);
			PokegearWindow.addOutputInformation("Processing this card returned image URL: " + url);
		});

		// FIXME - Card preview image is not redrawing
		Display.getDefault().asyncExec(() ->
		{
			PokegearWindow.setCardPreviewImage(url);
		});

		Display.getDefault().wake();
		Display.getDefault().asyncExec(() ->
		{
			// Update the progressBar
			PokegearWindow.progressBar.setSelection(TabletopParser.loadBarIndex++);
		});

		/* ||-------------------------------------------||
		 * || This block is where the thread terminates ||
		 * ||-------------------------------------------||
		 */
		synchronized (this)
		{
			// Terminate
			this.interrupt();
		}
	}

	public String getImageSourceUrl(String[] query, boolean specialEnergyFlag, boolean thumb) throws IOException
	{
		String faceurl = "!";
		String set = query[3];
		String num = query[4];
		String reg = "++";
		String name = query[2];
		String flag = query[0];
		String count = query[1];
		String tempname = name.replaceAll(" ", "+").replaceAll(Pattern.quote(reg), "+");

		if (name.contains("Energy") || set.contains("Energy"))
		{
			faceurl = getBasicEnergy(query);
		}

		out.println("Did getBasicEnergy() return anything?? --- " + faceurl);
		if (faceurl.equals("!"))
		{
			name = query[2]; // .replaceAll(" ","+");

			out.println("Worker thread found " + name + " " + " Count {" + count + "} from " + set + " : " + num);

			// TODO:: This check is for set promos, so I need to figure that shit out. This is just a wild-ass shot in the dark for now.
			boolean promo = false;
			if (name.contains("PR-") || tempname.contains("PR-"))
			{
				tempname = tempname.replaceAll("PR-", "+PR");
				promo = true;
			}

			String address = ("https://pkmncards.com/card/" + name.replaceAll(" ", "-") + "-" + set + "-" + num).toLowerCase();
			Document pkmncards = null;
			while (pkmncards == null)
			{
				try
				{
					// FIXME - Occasional cards are being skipped or fetched entirely incorrectly
					// Current solution mostly works but is throwing issues with 'Radiant Charizard PGO 11' for unknown reasons
					out.println("Polling this address:: " + address);
					pkmncards = Jsoup.connect(address).get();

					if (pkmncards == null) out.println("For some reason, the webcrawler is unable to find a valid HTML Webpage to download. Please check the source website.");
				} catch (Exception e)
				{
					out.println("DEBUG-------- Probable Set Agnostic Promo Card Found");
					// Almost certainly an 'agnostic promo' if it falls through here, so try fetch again and replace vars if it succeeds
					// if (promo)

					if ((pkmncards = Jsoup.connect(address.replaceAll("-PR", "-PROMO")).get()) != null)
					{
						out.println("Throwing out previous address, replacing with promo-agnostic URL:: " + address.replaceAll("-PR", "-PROMO"));
						tempname = tempname.replaceAll("+PR", "");
						set = "PROMO";
					}
				}
				if (pkmncards.select("a.card-image-link").first() == null)
				{
					out.println("Will need to re-try after appropriate waiting period of 5 seconds.");
					try
					{
						Thread.sleep(5000);
					} catch (InterruptedException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}

			// FIXME - This selector MIGHT be to blame, so lets check if 'href' works better than 'abs:href'
			// New solution will be to grab ALL elements matching 'a.card-image-link' and checking for the pokemon name
			faceurl = pkmncards.select("a.card-image-link").first().absUrl("href");

			// Loop to search for lowest level subpage for any card with multiple results. Shouldn't ever loop more than once but serves as a good sanitycheck this way
			if (!faceurl.contains("https://pkmncards.com/card/"))
			{
				Display.getDefault().wake();
				Display.getDefault().syncExec(() ->
				{
					PokegearWindow.addOutputInformation("Multiple versions of this card found, will check for and select a valid version.");
				});

				while (!faceurl.contains("https://pkmncards.com/card/"))
				{
					faceurl = pkmncards.select("a.card-image-link").first().absUrl("href");
				}
			}

			// Set thumbnail if not set already
			if (!TabletopParser.thumb) // In a nutshell- rip the image, decode it, re-encode it, save to disk
			{
				try
				{
					URL urlImage = new URL(faceurl);
					InputStream in = urlImage.openStream();
					byte[] buffer = new byte[4096];
					int n = -1;
					OutputStream os = new FileOutputStream(TabletopParser.filePath + "\\" + TabletopParser.deckName + ".png");
					while ((n = in.read(buffer)) != -1)
						os.write(buffer, 0, n);
					os.close();

					Display.getDefault().wake();
					Display.getDefault().syncExec(() ->
					{
						PokegearWindow.addOutputInformation("Deck thumbnail chosen and saved to disk.");
					});

					TabletopParser.thumb = true;
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		// NOTE:: Do NOT ever change this line! EVERYTHING WILL BREAK AND I DON'T KNOW WHY
		int id = TabletopParser.cardindex * 100;
		for (int i = 1; i <= Integer.parseInt(count); i++)
		{
			// Loop through deck to assign deck & card IDs for all parsed cards
			int instID = id + i * 10;
			TabletopParser.instanceIDs.put(instID, id);
			ArrayList<String> temp = new ArrayList<>();
			temp.add(faceurl);
			temp.add(name);
			TabletopParser.instanceURLs.put(instID, temp);
		}
		// Increment cardindex by number of this card present in deck
		TabletopParser.cardindex += Integer.parseInt(count);
		/*} catch (Exception n)
		{
			out.print("Whoops, I pressed the 'war crime' button, and the U.N. will soon convene to strongly condemn the following affront to humanity:: ");
			n.printStackTrace();
			out.println();
			TabletopParser.errorcards.add(name + "+" + set + "+" + num);
		}*/
		out.println("Worker thread requested a face url, found " + faceurl);
		return faceurl;
	}

	public String getBasicEnergy(String[] query) throws IOException
	{
		String faceurl = "!";
		String set = query[3];
		String num = query[4];
		String reg = "++";
		String name = query[2];
		String flag = query[0];
		String count = query[1];
		String tempname = name.replaceAll(" ", "+").replaceAll(Pattern.quote(reg), "+");

		Display.getDefault().wake();
		Display.getDefault().syncExec(() ->
		{
			PokegearWindow.addOutputInformation("Found probable basic energy card of name: " + name);
		});

		out.println("Found probable basic energy card of name: " + name);
		if (name.equals("Fire") && !(name.split(" ")[0].equals("Energy"))) faceurl = LinkEnums.ENERGYFIRE;
		if (name.equals("Grass") && !(name.split(" ")[0].equals("Energy"))) faceurl = LinkEnums.ENERGYGRASS;
		if (name.equals("Water") && !(name.split(" ")[0].equals("Energy"))) faceurl = LinkEnums.ENERGYWATER;
		if (name.equals("Darkness") && !(name.split(" ")[0].equals("Energy"))) faceurl = LinkEnums.ENERGYDARK;
		if (name.equals("Fighting") && !(name.split(" ")[0].equals("Energy"))) faceurl = LinkEnums.ENERGYFIGHTING;
		if (name.equals("Fairy") && !(name.split(" ")[0].equals("Energy"))) faceurl = LinkEnums.ENERGYFAIRY;
		if (name.equals("Lightning") && !(name.split(" ")[0].equals("Energy"))) faceurl = LinkEnums.ENERGYELECTRIC;
		if (name.equals("Metal") && !(name.split(" ")[0].equals("Energy"))) faceurl = LinkEnums.ENERGYSTEEL;
		if (name.equals("Psychic") && !(name.split(" ")[0].equals("Energy"))) faceurl = LinkEnums.ENERGYPSYCHIC;

		return faceurl;
	}

}
