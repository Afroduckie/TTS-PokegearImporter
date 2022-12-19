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
import org.eclipse.swt.widgets.Display;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
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
	public int category = 0; // Will be 0 for "Not Checked", 1 for "Pokemon", 2 for "Trainer", and 3 for "Energy"
	private static String cardDB = "https://pkmncards.com/?s=";
	private String hovertext;

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
		String flag = query[0];
		String count = query[1];
		String name = query[2];
		String set = query[3];
		String num = query[4];
		int cardindex = 1;
		boolean thumb = false;

		// System.out.print("Began worker thread for query- ");
		for (String q : query)
			out.print(q + " ");
		out.println();

		try
		{
			Thread.sleep(1000);
		} catch (InterruptedException e1)
		{
		}

		specialEnergyFlag = true;
		url = "";

		// Attempt to parse the card
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

		Display.getDefault().wake();
		Display.getDefault().syncExec(() ->
		{
			switch (category)
			{
				case 1:
					PokegearWindow.addPokemonInfo(sender);
					break;
				case 2:
					PokegearWindow.addTrainerInfo(sender);
					break;
				case 3:
					PokegearWindow.addEnergyInfo(sender);
					break;
				default:
					PokegearWindow.addEnergyInfo(sender);
					break;
			}
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
			PokegearWindow.incrementCounter(Integer.parseInt(count));
		});

		/* ||-------------------------------------------||
		 * || This block is where the thread terminates ||
		 * ||-------------------------------------------||
		 */
		try
		{
			Thread.sleep(750);
		} catch (InterruptedException e1)
		{
		}
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

			// out.println("Worker thread found " + name + " " + " Count {" + count + "} from " + set + " : " + num);

			// TODO:: This check is for set promos, so I need to figure that shit out. This is just a wild-ass shot in the dark for now.
			boolean promo = false;
			if (name.contains("PR-") || tempname.contains("PR-"))
			{
				tempname = tempname.replaceAll("PR-", "+PR");
				promo = true;
			}

			String address = ("https://pkmncards.com/card/" + name.replaceAll(" ", "-") + "-" + set + "-" + num).toLowerCase();
			Document pkmncards = null;

			out.println("Polling this address:: " + address);
			try
			{
				pkmncards = Jsoup.connect(address).get();

				faceurl = pkmncards.select("a.card-image-link").first().absUrl("href");

				// Loop to search for lowest level subpage for any card with multiple results. Shouldn't ever loop more than once but serves as a good sanitycheck this way
				if (faceurl.contains("https://pkmncards.com/card/"))
				{
					Display.getDefault().wake();
					Display.getDefault().asyncExec(() ->
					{
						PokegearWindow.addOutputInformation("Multiple versions of this card found, will check for and select a valid version.");
					});

					while (faceurl.contains("https://pkmncards.com/card/"))
					{
						faceurl = pkmncards.select("a.card-image-link").first().absUrl("href");
					}
				}
			} catch (MalformedURLException | NullPointerException | HttpStatusException e)
			{
				// out.println("A retry is necessary for this card. Running query at " + cardDB + set + "+" + num);
				Display.getDefault().asyncExec(() ->
				{
					PokegearWindow.addOutputInformation("A retry is necessary for this card either because no result was found or the card is unique.");
				});
				pkmncards = Jsoup.connect(cardDB + set + "+" + num).get();
				faceurl = pkmncards.select("a.card-image-link").first().absUrl("href");
			}

			// Find this card's category
			String tempcat = pkmncards.select("div.type-evolves-is").select("span").select("a").first().text().replaceAll("Pok.mon", "Pokemon");
			out.println("Temp category found:: " + tempcat);
			if (tempcat.equals("Pokemon"))
				category = 1;
			else if (tempcat.equals("Trainer"))
				category = 2;
			else if (tempcat.equals("Energy")) category = 3;

			// After category is set, grab tooltips
			hovertext = setTooltipInfo(pkmncards, category);

			// Set thumbnail if not set already
			if (!TabletopParser.thumb) // In a nutshell- rip the image, decode it, re-encode it, save to disk
			{
				try
				{
					URL urlImage = new URL(faceurl);
					InputStream in = urlImage.openStream();
					byte[] buffer = new byte[4096];
					int n = -1;
					OutputStream os = new FileOutputStream(PokegearWindow.getPath() + "\\" + TabletopParser.deckName + ".png");
					while ((n = in.read(buffer)) != -1)
						os.write(buffer, 0, n);
					os.close();

					Display.getDefault().wake();
					Display.getDefault().asyncExec(() ->
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
		// Increment cardindex by number of this card present in deck and send the tooltip to TabletopParser
		TabletopParser.cardindex += Integer.parseInt(count);
		TabletopParser.tooltips.add(hovertext);
		return faceurl;
	}

	private String setTooltipInfo(Document page, int category)
	{
		// Grabs the document passed to it, filters out the tool tip information from it, and returns a formatted text block
		String separator = "----------";
		String tooltips = "\n";

		// pkmncards.select("div.type-evolves-is").select("span").select("a").first().text().replaceAll("Pok.mon", "Pokemon");

		// Pokemon-specific tool tips
		if (category == 1)
		{
			// Add the name, HP, and type
			String name = page.select("div.name-hp-color").select("span").first().select("a").first().text();
			String hp = page.select("div.name-hp-color").select("span").first().nextElementSibling().text();
			String type = page.select("div.name-hp-color").select("span").first().nextElementSibling().nextElementSibling().select("a").first().select("abbr").first().attr("title");
			String subclass = "";
			try
			{
				subclass = page.select("div.type-evolves-is").select("span.is").select("a").first().text();
			} catch (Exception e)
			{
			}
			tooltips += "Base HP: " + hp + "\n" + "Type(s): " + type + "\n";
			
			if(subclass.length() > 0)
			{
				tooltips += "Classification: " + subclass + " Pokemon\n";
			}
			tooltips += separator + "\n";

			// Use a try-catch to see if any ability, Poke-BODY, etc is present. Failure means it is not.
			boolean hasAbility = false;
			Element abilityElement = null;
			String ability = "None";
			try
			{
				// ability = page.select("div.card-tabs").select("div.text").first().select("p")
				// VSTAR and some other special Pokemon classes have abilities at the bottom, so we have to iterate the <p> tags to check all of them
				for (Element e : page.select("div.card-tabs").select("div.text").first().select("p"))
				{
					try
					{
						ability = e.select("a").text();
						abilityElement = e;
					} catch (NullPointerException nn)
					{

					}
					if (ability.length() > 0)
					{
						hasAbility = true;
						break;
					}
				}
			} catch (NullPointerException n)
			{
				hasAbility = false;
			}

			// If ability found, parse it out
			if (hasAbility)
			{
				String abilityType = ability.replaceAll("Pok.", "Poke");
				String abilityName = abilityElement.before("br").html().split("<br>")[0];
				abilityName = abilityName.substring(abilityName.lastIndexOf('>') + 4);

				// Add ability type and name to the tooltip
				tooltips += abilityType + ": " + abilityName + "\n";

				// NOTE - Ability text may require additional regex if more contractions with invalid unicode crop up. Use of "Pok." regex instead of whole "Pokemon" will hopefully catch for "Poke-BODY" and similar
				String abilityText = abilityElement.text().substring(abilityType.length() + abilityName.length() + 3).replaceAll("Pok.", "Poke").replaceAll("can.t", "can't");
				tooltips += abilityText + "\n" + separator + "\n";
			}

			// Parse moveset. Will need to check and avoid re-parsing ability information if ability is present
			for (Element m : page.select("div.card-tabs").select("div.text").first().select("p"))
			{
				// To prevent re-parsing ability info, check for the element and equivalency to variable. Won't trip if no element was set (meaning no ability)
				String moveCost = "";
				String moveName = "";
				String moveDamage = "";
				String moveText = "";

				// Set move cost symbols. Save last <abbr> tag we parse for later.
				Element lastAbbr = null;
				for (Element m2 : m.select("abbr"))
				{
					moveCost += m2.text();
					lastAbbr = m2;
				}

				// NOTE -- PKMNCards.com uses '@' to denote free-cost moves but MIGHT use them for other things too
				if (moveCost.contains("@"))
				{
					moveCost = "{ No Energy Cost }";
				}

				// Set move name
				moveName = m.select("span").not("span.class").not("a.href").text();
				moveName = moveName.substring(moveName.lastIndexOf("}") + 1);
				moveText = m.text().replaceAll("Pok.", "Poke");
				moveDamage = moveText.substring(moveText.indexOf(":") + 2);

				out.println("Move Damage: " + moveDamage);

				moveText = moveDamage.substring(moveDamage.indexOf(" ") + 1);
				try
				{
					moveDamage = moveDamage.substring(0, moveDamage.indexOf(" "));
				} catch (StringIndexOutOfBoundsException s)
				{
					moveText = "";
				}

				if (moveText.contains(moveName))
				{
					moveText = moveText.substring(moveName.length() + 2);
				}

				// External TabletopParser function does not seem to work on this field, so do it here
				moveText = moveText.replaceAll("n.t", "n't").replaceAll("opponent.s", "opponent's");

				try
				{
					Integer.parseInt(moveDamage);
				} catch (NumberFormatException ne)
				{
					moveDamage = "";
				}

				if (moveName.length() > 1)
				{
					// Add to tooltip
					tooltips += moveName.trim() + " " + moveCost.trim() + "\n";
					if (moveText.length() > 1)
					{
						tooltips += " " + moveText + "\n";
					}
					if (moveDamage.length() > 1)
					{
						tooltips += "Damage: " + moveDamage + "\n";
					}
				}
			}
			tooltips += separator + "\n";
		}
		// Trainer-specific tool tips
		else if (category == 2)
		{
			tooltips += separator + "\n";
			String trainerType = page.select("div.type-evolves-is").select("span.sub-type").text();
			String note = "";
			if (trainerType.equals("Supporter"))
			{
				note = "Rule:\nOnly 1 Supporter may be played on your turn.\nIf it is turn 1 and you went first, you can't play a Supporter until turn 2.";
			}
			else if (trainerType.equals("Stadium"))
			{
				note = "Rule:\nStadiums stay in play after being placed and are discarded when new Stadiums are played.\nIf another Stadium with this name is in play, you may not play this card.";
			}
			else if (trainerType.equals("Tool"))
			{
				note = "Rule:\nYou may attach this card to a Pokemon to apply this effect to it.\nUnless otherwise stated, all Pokemon can only have 1 Tool.\nDiscard this card when the Pokemon is knocked out.";
			}
			else if (trainerType.equals("Item"))
			{
				note = "Rule:\nYou may play any number of Item cards on your turn, unless an Ability, opponent's Trainer Card, or Attack effect prevents you.";
			}
			trainerType += " Card";
			String trainerText = "";
			for (Element p : page.select("div.card-tabs").select("div.text").select("p"))
			{
				trainerText += TabletopParser.filterString(p.text()) + "\n";
			}

			try
			{
				for (Element l : page.select("div.card-tabs").select("div.text").select("ul").select("li"))
				{
					trainerText += "- " + l.text();
					if (trainerText.length() > 3)
					{
						trainerText += "\n";
					}
				}
			} catch (Exception e)
			{
			}

			tooltips += trainerType + "\n";
			if (note.length() > 1)
			{
				tooltips += note + "\n";
			}
			tooltips += separator + "\n" + trainerText + separator + "\n";
			tooltips = TabletopParser.filterString(tooltips);
		}

		// Energy-specific tool tips
		else if (category == 3)
		{
			tooltips += separator + "\n";

			// Check if special or basic energy
			String type = page.select("div.type-evolves-is").select("span.sub-type").text();
			String energyText = "";
			tooltips += type + "\n" + separator + "\n";

			if (type.equals("Special Energy"))
			{
				for (Element p : page.select("div.card-tabs").select("div.text").select("p"))
				{
					energyText += TabletopParser.filterString(p.text()) + "\n";
				}
			}
			else if (type.equals("Basic Energy"))
			{
				String name = page.select("div.name-hp-color").select("span").first().select("a").first().text().split(" ")[0];
				energyText = "Provides 1 " + name + " energy to the Pokemon this is attached to.";
			}
			tooltips += energyText + separator + "\n";
		}

		tooltips = TabletopParser.filterString(tooltips).trim();

		out.println("Fully Formatted ToolTip:: \n\n" + tooltips);
		return tooltips;
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

		// out.println("Found probable basic energy card of name: " + name);
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
