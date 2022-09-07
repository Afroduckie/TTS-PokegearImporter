package andrielgaming.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.SwingWorker;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.jsoup.nodes.Document;

import andrielgaming.Pokegear;
import andrielgaming.parsing.TabletopParser;
import andrielgaming.ui.panels.AnimatedCanvas;
import andrielgaming.utils.LinkEnums;

/**
 * 
 * @author Andrew Kluttz - https://github.com/Afroduckie
 * 
 * #PokegearWindow -- GUI Container Class and Runner for the Pokegear parsing software
 * 	
 * 		Holds the GUI for Pokegear and services user input by passing any input to the appropriate method in #TabletopParser
 * 		Utilizes Java Threads to run thread-based instances of #TabletopParser as a SwingWorker.
 * 		Code skeleton constructed in Eclipse SWT WindowBuilder with a strong handful of manual changes.
 * 
 * Changelog---
 * 		[Version R:1.4.1] 
 * 			-Transitioned from simple Runnable implementation to a SwingWorker for the parser class.
 */

public class PokegearWindow
{
	private static Thread parse;
	private static LinkEnums linker = new LinkEnums();
	public static Shell shlPokegearDeckImporter;
	private Label lbl_helpTab;
	private Text txtThisProgramWas;
	private Text txtIfYouAre;
	private Text txtForTheDecks;
	private Text txtAsdf;
	private Text text;
	private CLabel label_1;
	private Image animatedPikachu;
	public static ProgressBar progressBar;
	public static List guiDeckList;
	public static int imageNumber = 0;
	public static SwingWorker dialog;
	public static String previewCardSleeve = "";
	static boolean threadActive = false;
	public static TabFolder tabFolder;
	public static Label cardPreview;
	public static Label sleevePreview;
	public static List errorPreview;
	public static List preParseList;
	public static List postParseList;
	public static Composite composite_3;
	public Image sleevePreviewImage;
	public static Image cardPreviewImage;
	public boolean sleevePreviewChanged = false;
	public static boolean cardPreviewChanged = false;
	public static TabItem importProgress;
	public static ArrayList<String> debugMessages = new ArrayList<String>();
	public static ArrayList<String> cardNameMessages = new ArrayList<String>();
	public static ArrayList<String> errorMessages = new ArrayList<String>();
	public static AnimatedCanvas animatedCanvas;

	public static void main(String[] args) throws Exception
	{
		try
		{
			PokegearWindow window = new PokegearWindow();
			window.open();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open()
	{
		try
		{
			Display display = Display.getDefault();
			createContents();
			shlPokegearDeckImporter.open();
			shlPokegearDeckImporter.layout();
			Rectangle screenSize = display.getPrimaryMonitor().getBounds();
			shlPokegearDeckImporter.setLocation((screenSize.width - shlPokegearDeckImporter.getBounds().width) / 2, (screenSize.height - shlPokegearDeckImporter.getBounds().height) / 2);

			Menu menu = new Menu(shlPokegearDeckImporter, SWT.BAR);
			shlPokegearDeckImporter.setMenuBar(menu);

			MenuItem mntmExit = new MenuItem(menu, SWT.CASCADE);
			mntmExit.setText("Menu");

			Menu menu_1 = new Menu(mntmExit);
			mntmExit.setMenu(menu_1);

			MenuItem mntmNewItem = new MenuItem(menu_1, SWT.NONE);
			mntmNewItem.setText("Exit PokeGear");
			mntmNewItem.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					System.exit(0);
				}
			});
			while (!shlPokegearDeckImporter.isDisposed())
			{
				if (!display.readAndDispatch()) display.sleep();
				if (!composite_3.isDisposed())
				{
					composite_3.getDisplay().readAndDispatch();

				}
				Display.getCurrent().readAndDispatch();
			}
		} catch (Exception e)
		{
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents()
	{
		shlPokegearDeckImporter = new Shell(SWT.APPLICATION_MODAL);
		animatedPikachu = SWTResourceManager.getImage(PokegearWindow.class, "/images/dancingpikachu_resized.gif");
		shlPokegearDeckImporter.setMaximized(true);
		shlPokegearDeckImporter.setBackgroundImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/background.jpg"));
		shlPokegearDeckImporter.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		shlPokegearDeckImporter.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/pokegearicon.png"));
		shlPokegearDeckImporter.setBackground(SWTResourceManager.getColor(0, 0, 153));
		shlPokegearDeckImporter.setSize(1228, 853);
		shlPokegearDeckImporter.setText("Pokegear Deck Importer");
		GridLayout gl_shlPokegearDeckImporter = new GridLayout(2, false);
		gl_shlPokegearDeckImporter.marginTop = 25;
		gl_shlPokegearDeckImporter.marginBottom = 25;
		gl_shlPokegearDeckImporter.marginRight = 25;
		gl_shlPokegearDeckImporter.marginLeft = 25;
		shlPokegearDeckImporter.setLayout(gl_shlPokegearDeckImporter);

		CLabel lblNewLabel = new CLabel(shlPokegearDeckImporter, SWT.NONE);
		lblNewLabel.setImage(null);
		lblNewLabel.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 30, SWT.BOLD));
		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_lblNewLabel.heightHint = 77;
		lblNewLabel.setLayoutData(gd_lblNewLabel);
		lblNewLabel.setText("  Pokegear Deck Importer for Tabletop Simulator   ");
		new Label(shlPokegearDeckImporter, SWT.NONE);

		new Label(shlPokegearDeckImporter, SWT.NONE);
		new Label(shlPokegearDeckImporter, SWT.NONE);

		tabFolder = new TabFolder(shlPokegearDeckImporter, SWT.NONE);
		tabFolder.setBackground(SWTResourceManager.getColor(0, 0, 51));
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tabFolder.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));

		TabItem tab_import = new TabItem(tabFolder, SWT.NONE);
		tab_import.setToolTipText("The main tool, import your decks here.");
		tab_import.setText("Import");

		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		composite_1.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 14, SWT.NORMAL));
		tab_import.setControl(composite_1);
		composite_1.setLayout(new GridLayout(8, false));

		final StyledText styledText = new StyledText(composite_1, SWT.BORDER | SWT.FULL_SELECTION | SWT.WRAP);
		GridData gd_styledText = new GridData(SWT.LEFT, SWT.TOP, true, true, 5, 19);
		gd_styledText.heightHint = 558;
		gd_styledText.widthHint = 842;
		styledText.setLayoutData(gd_styledText);
		styledText.setSelectionForeground(SWTResourceManager.getColor(255, 255, 255));
		styledText.setMarginColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		styledText.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		styledText.setBackground(SWTResourceManager.getColor(102, 153, 255));

		CLabel lblNewLabel_1 = new CLabel(composite_1, SWT.NONE);
		lblNewLabel_1.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		lblNewLabel_1.setText("Deck Name:");

		text = new Text(composite_1, SWT.BORDER);
		text.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1);
		gd_text.widthHint = 178;
		text.setLayoutData(gd_text);

		CLabel lblNewLabel_2 = new CLabel(composite_1, SWT.NONE);
		lblNewLabel_2.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.BOLD));
		lblNewLabel_2.setImage(null);
		lblNewLabel_2.setText("Errors (If Present):");
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);

		guiDeckList = new List(composite_1, SWT.BORDER | SWT.V_SCROLL);
		guiDeckList.setToolTipText("Cards parsed into the new deck show up here.");
		guiDeckList.setDragDetect(false);
		guiDeckList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		guiDeckList.setBackground(SWTResourceManager.getColor(192, 192, 192));
		GridData gd_guiDeckList = new GridData(SWT.LEFT, SWT.TOP, false, false, 3, 17);
		gd_guiDeckList.heightHint = 448;
		gd_guiDeckList.widthHint = 343;
		guiDeckList.setLayoutData(gd_guiDeckList);

		CLabel label = new CLabel(composite_1, SWT.NONE);
		label.setImage(null);
		label.setText("");

		Button btnClearAll = new Button(composite_1, SWT.CENTER);
		btnClearAll.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 24, SWT.BOLD));
		GridData gd_btnClearAll = new GridData(SWT.CENTER, SWT.FILL, false, false, 1, 1);
		gd_btnClearAll.heightHint = 63;
		gd_btnClearAll.widthHint = 167;
		btnClearAll.setLayoutData(gd_btnClearAll);
		btnClearAll.setText("Clear All");
		btnClearAll.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				TabletopParser.resetDeck();
				text.setText("");
				styledText.setText("");
				guiDeckList.setItems("");
			}
		});

		Button btnPasteFromClipboard = new Button(composite_1, SWT.CENTER);
		btnPasteFromClipboard.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 24, SWT.BOLD));
		GridData gd_btnPasteFromClipboard = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnPasteFromClipboard.widthHint = 263;
		gd_btnPasteFromClipboard.heightHint = 61;
		btnPasteFromClipboard.setLayoutData(gd_btnPasteFromClipboard);
		btnPasteFromClipboard.setText("Paste From Clipboard");
		btnPasteFromClipboard.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
					styledText.setText(data);
				} catch (Exception iex)
				{
				}
			}
		});
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);

		Button btnNewButton = new Button(composite_1, SWT.NONE);
		GridData gd_btnNewButton = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton.widthHint = 160;
		btnNewButton.setLayoutData(gd_btnNewButton);
		btnNewButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				String decklist = styledText.getText();
				String defPath = Pokegear.getPath();
				String name = text.getText();

				// Send info over to the parser
				animatedPikachu = SWTResourceManager.getImage(PokegearWindow.class, "/images/dancingpikachu_resized.gif");
				ImageData pika = animatedPikachu.getImageData();
				try
				{
					forwardToParser(name, defPath, decklist);
				} catch (Exception e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnNewButton.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 24, SWT.BOLD));
		btnNewButton.setText("Import Deck");

		TabItem tab_options = new TabItem(tabFolder, SWT.NONE);
		tab_options.setToolTipText("Configure Pokegear, add filepath, change settings");
		tab_options.setText("Options");

		Composite composite_2 = new Composite(tabFolder, SWT.NONE);
		tab_options.setControl(composite_2);
		composite_2.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		composite_2.setLayout(new GridLayout(3, false));

		CLabel lblLocationOfYour = new CLabel(composite_2, SWT.NONE);
		lblLocationOfYour.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		lblLocationOfYour.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		lblLocationOfYour.setText("Location of your TTS \"Saved Objects\" folder:");
		new Label(composite_2, SWT.NONE);

		txtAsdf = new Text(composite_2, SWT.BORDER);
		txtAsdf.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		txtAsdf.setEditable(false);
		txtAsdf.setText(Pokegear.getPath());
		txtAsdf.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		CLabel lblCardSleeveback = new CLabel(composite_2, SWT.NONE);
		lblCardSleeveback.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.BOLD));
		lblCardSleeveback.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		lblCardSleeveback.setText("Card Sleeve (Back)");

		CLabel lblImagePreview = new CLabel(composite_2, SWT.NONE);
		lblImagePreview.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		lblImagePreview.setText("Image Preview");

		CLabel lblCurrentSelection = new CLabel(composite_2, SWT.NONE);
		lblCurrentSelection.setText("Current Selection");
		lblCurrentSelection.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));

		List list = new List(composite_2, SWT.BORDER | SWT.V_SCROLL);
		list.setItems(new String[]
		{ "Default", "ArceusAnniversary", "Celebi", "Chespin1", "Chespin2", "Chespin3", "Darkrai", "DeoxysFullColor", "DruddigonClawMarks", "EeveeSilhouette", "EeveelutionsEspeon1", "EeveelutionsEspeon2", "EeveelutionsEspeon3", "EeveelutionsFlareon1", "EeveelutionsFlareon2", "EeveelutionsFlareon3", "EeveelutionsGlaceon1", "EeveelutionsGlaceon2", "EeveelutionsGlaceon3", "EeveelutionsJolteon1", "EeveelutionsJolteon2", "EeveelutionsJolteon3", "EeveelutionsLeafeon1", "EeveelutionsLeafeon2", "EeveelutionsLeafeon3", "EeveelutionsSylveon1", "EeveelutionsSylveon2", "EeveelutionsSylveon3", "EeveelutionsUmbreon1", "EeveelutionsUmbreon2", "EeveelutionsUmbreon3", "EeveelutionsVaporeon1", "EeveelutionsVaporeon2", "EeveelutionsVaporeon3", "EnergyDarkness", "EnergyDragon", "EnergyFairy", "EnergyFighting", "EnergyFire", "EnergyGrass", "EnergyLightning", "EnergyMetal", "EnergyPsychic", "EnergyWater", "Fennekin1", "Fennekin2", "Fennekin3", "Fennekin4", "Froakie1", "Froakie2", "Froakie3", "GarchompSilhouette", "GenesectSilhouette", "GengarHalloween", "GoldSleeve1Pikachu", "GoldSleeve2PikachuCoin", "GoldSleeve3PikachuAnniversary", "GoldSleeve4CharizardFullGold", "GoldSleeve5BlastoiseBlueTrim", "GoldSleeve6CharizardRedTrim", "GoldSleeve7VenusaurGreenTrim", "GoldSleeve8BlastoiseFullGold", "GoldSleeve9VenusaurFullGold", "GourgeistPokeball", "Halloween2014", "Jirachi", "Manaphy", "Mew", "MewtwoFullColor", "MegaGengarFullColor", "MegaCharizardX", "MegaCharizardY", "MegaBlastoise", "MegaVenusaur", "MegaMewtwoDuo", "MiloticGlassArt", "NewFriendsLeageFennekin", "Pax2014Blue", "ParallelLeagueChikorita", "PikachuSilhouette", "PokemonClub", "PyroarSilhouetteFlames", "RaichuSleeve", "Shaymin", "SteamLeagueYveltal", "TeamAqua", "TeamMagma", "ThunderousFullColor", "TrevenantStylizedSilhouette", "TrainerBoxGenerations", "TrainerBoxGroudon", "TrainerBoxGyarados", "TrainerBoxHoopa", "TrainerBoxKyogre", "TrainerBoxMegaAlakazam", "TrainerBoxMewtwoX", "TrainerBoxMewtwoY", "TrainerBoxRayquaza", "TrainerBoxVolcanion", "Worlds2013", "Worlds2014", "Worlds2015", "Worlds2015Alt", "Worlds2016", "VictiniFullArt", "VictiniWithTrim", "Xerneas1", "Xerneas2", "Xerneas3", "Yveltal1", "Yveltal2", "Yveltal3" });
		list.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		list.setBackground(SWTResourceManager.getColor(100, 149, 237));
		GridData gd_list = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 4);
		gd_list.widthHint = 262;
		gd_list.heightHint = 495;
		list.setLayoutData(gd_list);
		list.addListener(SWT.MouseDown, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				String item = "";
				int itemTop = 0;
				for (int i = 0; i < list.getItemCount(); i++)
				{
					if (event.y >= itemTop && event.y <= itemTop + list.getItemHeight())
					{
						item = list.getItem(list.getTopIndex() + i);
					}
					itemTop += list.getItemHeight();
				}

				// Update the image preview after list item selected
				System.out.println("List Item Clicked! " + item);
				String imagePreviewURL = LinkEnums.Sleeves.get(item);
				String imageLink = "";
				Document document;
				try
				{
					Image img = new Image(Display.getCurrent(), new URL(LinkEnums.Sleeves.get(item)).openStream());
					img.setBackground(new Color(0, 0, 0));
					img = new Image(Display.getCurrent(), img.getImageData().scaledTo(274, 374));
					label_1.setImage(img);
					previewCardSleeve = LinkEnums.Sleeves.get(item);
				} catch (Exception E)
				{
					System.out.print("Error generating image preview- ");
					E.printStackTrace();
				}
			}
		});

		label_1 = new CLabel(composite_2, SWT.NONE);
		label_1.setRightMargin(1);
		label_1.setLeftMargin(1);
		label_1.setBackground(SWTResourceManager.getColor(65, 105, 225));
		label_1.setAlignment(SWT.CENTER);
		label_1.setForeground(SWTResourceManager.getColor(135, 206, 250));
		label_1.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/ptcg_back.png"));
		GridData gd_label_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_label_1.heightHint = 498;
		label_1.setLayoutData(gd_label_1);
		label_1.setText("");

		CLabel label_2 = new CLabel(composite_2, SWT.CENTER);
		label_2.setBottomMargin(5);
		label_2.setTopMargin(5);
		label_2.setAlignment(SWT.CENTER);
		label_2.setRightMargin(1);
		label_2.setLeftMargin(1);
		label_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		label_2.setForeground(SWTResourceManager.getColor(127, 255, 212));
		label_2.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/ptcg_back.png"));
		label_2.setText("");
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);

		Button btnChangeCardSleeve = new Button(composite_2, SWT.NONE);
		btnChangeCardSleeve.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnChangeCardSleeve.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 14, SWT.ITALIC));
		btnChangeCardSleeve.setText("Change Card Sleeve");
		btnChangeCardSleeve.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					TabletopParser.chosenCardBack = previewCardSleeve;
					Image img = new Image(Display.getCurrent(), new URL(previewCardSleeve).openStream());
					img.setBackground(new Color(0, 0, 0));
					img = new Image(Display.getCurrent(), img.getImageData().scaledTo(274, 374));
					label_2.setImage(img);
					sleevePreviewChanged = true;
					sleevePreviewImage = img;
				} catch (Exception ie)
				{
				}
			}
		});

		Button btnResetToDefault = new Button(composite_2, SWT.NONE);
		btnResetToDefault.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnResetToDefault.setText("Reset to Default");
		btnResetToDefault.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 14, SWT.ITALIC));
		btnResetToDefault.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				TabletopParser.chosenCardBack = LinkEnums.DEFAULTCARDBACK;
				label_2.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/ptcg_back.png"));
				label_1.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/ptcg_back.png"));
			}
		});
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);

		TabItem tab_help = new TabItem(tabFolder, SWT.NONE);
		tab_help.setToolTipText("How to use Pokegear and important information");
		tab_help.setText("Help");

		Composite composite = new Composite(tabFolder, SWT.NO_BACKGROUND);
		composite.setForeground(SWTResourceManager.getColor(51, 0, 0));
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		composite.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));
		tab_help.setControl(composite);
		composite.setLayout(new GridLayout(1, false));

		lbl_helpTab = new Label(composite, SWT.NONE);
		lbl_helpTab.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lbl_helpTab.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		lbl_helpTab.setBackground(SWTResourceManager.getColor(0, 0, 51));
		lbl_helpTab.setImage(null);
		lbl_helpTab.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 32, SWT.BOLD));
		lbl_helpTab.setText("   Guides and Important Info  ");

		ExpandBar expandBar = new ExpandBar(composite, SWT.BORDER);
		expandBar.setVisible(true);
		expandBar.setForeground(SWTResourceManager.getColor(0, 0, 0));
		expandBar.setBackground(SWTResourceManager.getColor(0, 153, 204));
		// expandBar.setForeground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		expandBar.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 22, SWT.NORMAL));
		GridData gd_expandBar = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_expandBar.heightHint = 560;
		gd_expandBar.widthHint = 1137;
		expandBar.setLayoutData(gd_expandBar);

		ExpandItem xpndtmNewExpanditem_1 = new ExpandItem(expandBar, SWT.NONE);
		xpndtmNewExpanditem_1.setText("What deck list formats will work with Pokegear?");

		txtThisProgramWas = new Text(expandBar, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		txtThisProgramWas.setText("This program was built with Pokemon TCG Online format in mind but should work with most decklist formats. The important required factor is the style, meaning every card entry must follow the order of count, name, set abbreviation, set number, like 4 Bidoof PRC 117. Any delimiters, headers, blank lines, or any other information is automatically ignored.");
		txtThisProgramWas.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));
		txtThisProgramWas.setEditable(false);
		txtThisProgramWas.setDragDetect(false);
		txtThisProgramWas.setDoubleClickEnabled(false);
		xpndtmNewExpanditem_1.setControl(txtThisProgramWas);
		xpndtmNewExpanditem_1.setHeight(72);

		ExpandItem xpndtmNewExpanditem_2 = new ExpandItem(expandBar, SWT.NONE);
		xpndtmNewExpanditem_2.setText("Where can I get a deck list?");

		txtIfYouAre = new Text(expandBar, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		txtIfYouAre.setText("If you are seeking to make one yourself, you can use online tools or even Pokemon TCG Online, which is free. PTCGO allows you to create decklists even if you use unowned cards, you just need to click the \"Show Not Owned\" checkbox. For pre-made decklists, LimitlessTCG is my personal choice.");
		txtIfYouAre.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));
		txtIfYouAre.setEditable(false);
		txtIfYouAre.setDragDetect(false);
		txtIfYouAre.setDoubleClickEnabled(false);
		xpndtmNewExpanditem_2.setControl(txtIfYouAre);
		xpndtmNewExpanditem_2.setHeight(64);

		ExpandItem xpndtmNewExpanditem_3 = new ExpandItem(expandBar, SWT.NONE);
		xpndtmNewExpanditem_3.setText("How do I use my new deck in Tabletop Simulator?");

		txtForTheDecks = new Text(expandBar, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		txtForTheDecks.setText("For the decks Pokegear creates to be spawnable in Tabletop Simulator, you need to make sure your file path is correctly configured. Pokegear should have asked you about this on first boot, but typically you will find the TTS Saved Objects folder in My Documents > Saved Games > Tabletop Simulator > Saves > Saved Objects. The decks are JSON filetype, and each deck has a thumbnail. If the autodetect doesn't show this file path or you have your Documents library on a drive other than C drive, please change this in \"Options\". If this file path is correct, then no need to fear. Just import a deck, and you should see it within TTS when you click 'Objects' then 'Saved Objects'. You should see the first card in the deck as the thumbnail.");
		txtForTheDecks.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));
		txtForTheDecks.setEditable(false);
		txtForTheDecks.setDragDetect(false);
		txtForTheDecks.setDoubleClickEnabled(false);
		xpndtmNewExpanditem_3.setControl(txtForTheDecks);
		xpndtmNewExpanditem_3.setHeight(128);

		importProgress = new TabItem(tabFolder, SWT.NONE);
		importProgress.setText("Deck List");

		composite_3 = new Composite(tabFolder, SWT.COMPOSITION_CHANGED);
		composite_3.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		composite_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		composite_3.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 14, SWT.NORMAL));
		importProgress.setControl(composite_3);
		composite_3.setLayout(new GridLayout(6, false));
		composite_3.setVisible(true);
		new Label(composite_3, SWT.NONE);

		Label lblNewLabel_3 = new Label(composite_3, SWT.CENTER);
		lblNewLabel_3.setVisible(true);
		lblNewLabel_3.setAlignment(SWT.CENTER);
		lblNewLabel_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		lblNewLabel_3.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 22, SWT.NORMAL));
		lblNewLabel_3.setText("Soft-Resetting for Shinies...");

		Label lblNewLabel_3_1 = new Label(composite_3, SWT.NONE);
		lblNewLabel_3_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		lblNewLabel_3_1.setText("Card Preview");
		lblNewLabel_3_1.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		lblNewLabel_3_1.setAlignment(SWT.CENTER);
		lblNewLabel_3_1.setVisible(true);
		new Label(composite_3, SWT.NONE);

		Label lblCardsGenerated = new Label(composite_3, SWT.CENTER);
		GridData gd_lblCardsGenerated = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_lblCardsGenerated.widthHint = 342;
		lblCardsGenerated.setLayoutData(gd_lblCardsGenerated);
		lblCardsGenerated.setText("Cards Generated");
		lblCardsGenerated.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		lblCardsGenerated.setVisible(true);
		new Label(composite_3, SWT.NONE);

		animatedCanvas = new AnimatedCanvas(composite_3, SWT.NO_BACKGROUND | SWT.NO_MERGE_PAINTS);
		animatedCanvas.setLayoutDeferred(true);
		animatedCanvas.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		animatedCanvas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		animatedCanvas.setVisible(true);
		GridData gd_animatedCanvas = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
		gd_animatedCanvas.heightHint = 277;
		gd_animatedCanvas.widthHint = 390;
		animatedCanvas.setScale(400, 292);
		animatedCanvas.setLayoutData(gd_animatedCanvas);
		animatedCanvas.frameTimer = 30;
		animatedCanvas.setImageClasspath("/images/charmander.gif");
		animatedCanvas.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/charmander.gif"));
		animatedCanvas.setVisible(true);

		// NOTE-- Code for the output reporting labels for card, sleeve, and errors, with cardPreview accessible by outside #TabletopParser class
		cardPreview = new Label(composite_3, SWT.NO_BACKGROUND | SWT.CENTER);
		cardPreview.setSize(new Point(50, 30));
		cardPreview.setImage(null);
		cardPreview.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		GridData gd_cardPreview = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_cardPreview.heightHint = 224;
		gd_cardPreview.widthHint = 345;
		cardPreview.setLayoutData(gd_cardPreview);
		cardPreview.setVisible(true);
		cardPreview.addPaintListener(new PaintListener()
		{
			public void paintControl(PaintEvent ev)
			{
				if (cardPreviewChanged)
				{
					cardPreviewImage = new Image(Display.getDefault(), cardPreviewImage.getImageData().scaledTo((int) (0.5 * sleevePreview.getBounds().width), (int) (0.89 * sleevePreview.getBounds().height)));
					cardPreviewChanged = false;
					cardPreview.setImage(sleevePreviewImage);
					cardPreview.redraw();
					cardPreview.update();
					composite_3.redraw();
					composite_3.update();
					System.out.println("Redraw event triggered in SleevePreview.PaintControl");
				}
			}
		});
		new Label(composite_3, SWT.NONE);

		// NOTE-- Accessed by #TabletopParser as each #ParsingThread completes its work; Each new card will be displayed here alongside an image preview in the cardPreview Label object
		postParseList = new List(composite_3, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		postParseList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		postParseList.setBackground(SWTResourceManager.getColor(0, 191, 255));
		GridData gd_list_1_1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_list_1_1.widthHint = 204;
		gd_list_1_1.heightHint = 224;
		postParseList.setLayoutData(gd_list_1_1);
		postParseList.setVisible(true);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);

		Label lblProvidedDeckList = new Label(composite_3, SWT.CENTER);
		lblProvidedDeckList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 22, SWT.NORMAL));
		GridData gd_lblProvidedDeckList = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_lblProvidedDeckList.heightHint = 36;
		lblProvidedDeckList.setLayoutData(gd_lblProvidedDeckList);
		lblProvidedDeckList.setVisible(true);
		lblProvidedDeckList.setText("Program Output");
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);

		Label lblErrorsIfPresent = new Label(composite_3, SWT.CENTER);
		lblErrorsIfPresent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		lblErrorsIfPresent.setText("Errors, If Present");
		lblErrorsIfPresent.setVisible(true);
		lblErrorsIfPresent.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 22, SWT.NORMAL));
		new Label(composite_3, SWT.NONE);

		// NOTE-- Accessed by #TabletopParser before JSON parsing but after plaintext is processed; Adds the imported list line-by-line, items being removed as each processed
		preParseList = new List(composite_3, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		preParseList.setDragDetect(false);
		preParseList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		preParseList.setBackground(SWTResourceManager.getColor(0, 191, 255));
		GridData gd_list_1 = new GridData(SWT.FILL, SWT.FILL, false, false, 3, 11);
		gd_list_1.widthHint = 182;
		preParseList.setVisible(true);
		preParseList.setLayoutData(gd_list_1);
		new Label(composite_3, SWT.NONE);

		errorPreview = new List(composite_3, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		errorPreview.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 11));
		errorPreview.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		errorPreview.setBackground(SWTResourceManager.getColor(70, 130, 180));
		errorPreview.setVisible(true);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);

		progressBar = new ProgressBar(composite_3, SWT.SMOOTH);
		GridData gd_progressBar_1 = new GridData(SWT.CENTER, SWT.CENTER, true, true, 6, 3);
		gd_progressBar_1.heightHint = 28;
		gd_progressBar_1.widthHint = 1150;
		progressBar.setLayoutData(gd_progressBar_1);
		progressBar.setVisible(true);

		// NOTE- Sets the import progress TabItem to include an instance of the new custom widget AnimatedCanvas
		Image deckExample = SWTResourceManager.getImage(PokegearWindow.class, "/images/helpitem_decklist.png");
		ImageData scaledDeckExample = deckExample.getImageData();
		scaledDeckExample.scaledTo(80, 80);

		importProgress.dispose();
	}

	// NOTE-- Here for anchor to method
	private void initLoadingScreen()
	{
		importProgress = new TabItem(tabFolder, SWT.NONE);
		importProgress.setText("Deck List");

		composite_3 = new Composite(tabFolder, SWT.COMPOSITION_CHANGED);
		composite_3.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		composite_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		composite_3.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 14, SWT.NORMAL));
		importProgress.setControl(composite_3);
		composite_3.setLayout(new GridLayout(6, false));
		composite_3.setVisible(true);
		new Label(composite_3, SWT.NONE);

		Label lblNewLabel_3 = new Label(composite_3, SWT.CENTER);
		lblNewLabel_3.setVisible(true);
		lblNewLabel_3.setAlignment(SWT.CENTER);
		lblNewLabel_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		lblNewLabel_3.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 22, SWT.NORMAL));
		lblNewLabel_3.setText("Soft-Resetting for Shinies...");

		Label lblNewLabel_3_1 = new Label(composite_3, SWT.NONE);
		lblNewLabel_3_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		lblNewLabel_3_1.setText("Card Preview");
		lblNewLabel_3_1.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		lblNewLabel_3_1.setAlignment(SWT.CENTER);
		lblNewLabel_3_1.setVisible(true);
		new Label(composite_3, SWT.NONE);

		Label lblCardsGenerated = new Label(composite_3, SWT.CENTER);
		GridData gd_lblCardsGenerated = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_lblCardsGenerated.widthHint = 342;
		lblCardsGenerated.setLayoutData(gd_lblCardsGenerated);
		lblCardsGenerated.setText("Cards Generated");
		lblCardsGenerated.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		lblCardsGenerated.setVisible(true);
		new Label(composite_3, SWT.NONE);

		animatedCanvas = new AnimatedCanvas(composite_3, SWT.NO_BACKGROUND | SWT.NO_MERGE_PAINTS);
		animatedCanvas.setLayoutDeferred(true);
		animatedCanvas.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		animatedCanvas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		animatedCanvas.setVisible(true);
		GridData gd_animatedCanvas = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
		gd_animatedCanvas.heightHint = 277;
		gd_animatedCanvas.widthHint = 390;
		animatedCanvas.setScale(400, 292);
		animatedCanvas.setLayoutData(gd_animatedCanvas);
		animatedCanvas.frameTimer = 30;
		animatedCanvas.setImageClasspath("/images/charmander.gif");
		animatedCanvas.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/charmander.gif"));
		animatedCanvas.setVisible(true);

		// NOTE-- Code for the output reporting labels for card, sleeve, and errors, with cardPreview accessible by outside #TabletopParser class
		cardPreview = new Label(composite_3, SWT.NO_BACKGROUND | SWT.CENTER);
		cardPreview.setSize(new Point(50, 30));
		cardPreview.setImage(null);
		cardPreview.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		GridData gd_cardPreview = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_cardPreview.heightHint = 224;
		gd_cardPreview.widthHint = 345;
		cardPreview.setLayoutData(gd_cardPreview);
		cardPreview.setVisible(true);
		cardPreview.addPaintListener(new PaintListener()
		{
			public void paintControl(PaintEvent ev)
			{
				if (cardPreviewChanged)
				{
					cardPreviewImage = new Image(Display.getDefault(), cardPreviewImage.getImageData().scaledTo((int) (0.5 * cardPreview.getBounds().width), (int) (0.89 * cardPreview.getBounds().height)));
					cardPreviewChanged = false;
					cardPreview.setImage(cardPreviewImage);
					cardPreview.redraw();
					cardPreview.update();
					composite_3.redraw();
					composite_3.update();
					System.out.println("Redraw event triggered in SleevePreview.PaintControl");
				}
			}
		});
		new Label(composite_3, SWT.NONE);

		// NOTE-- Accessed by #TabletopParser as each #ParsingThread completes its work; Each new card will be displayed here alongside an image preview in the cardPreview Label object
		postParseList = new List(composite_3, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		postParseList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		postParseList.setBackground(SWTResourceManager.getColor(0, 191, 255));
		GridData gd_list_1_1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_list_1_1.widthHint = 204;
		gd_list_1_1.heightHint = 224;
		postParseList.setLayoutData(gd_list_1_1);
		postParseList.setVisible(true);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);

		Label lblProvidedDeckList = new Label(composite_3, SWT.CENTER);
		lblProvidedDeckList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 22, SWT.NORMAL));
		GridData gd_lblProvidedDeckList = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_lblProvidedDeckList.heightHint = 36;
		lblProvidedDeckList.setLayoutData(gd_lblProvidedDeckList);
		lblProvidedDeckList.setVisible(true);
		lblProvidedDeckList.setText("Program Output");
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);

		Label lblErrorsIfPresent = new Label(composite_3, SWT.CENTER);
		lblErrorsIfPresent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		lblErrorsIfPresent.setText("Errors, If Present");
		lblErrorsIfPresent.setVisible(true);
		lblErrorsIfPresent.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 22, SWT.NORMAL));
		new Label(composite_3, SWT.NONE);

		// NOTE-- Accessed by #TabletopParser before JSON parsing but after plaintext is processed; Adds the imported list line-by-line, items being removed as each processed
		preParseList = new List(composite_3, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		preParseList.setDragDetect(false);
		preParseList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		preParseList.setBackground(SWTResourceManager.getColor(0, 191, 255));
		GridData gd_list_1 = new GridData(SWT.FILL, SWT.FILL, false, false, 3, 11);
		gd_list_1.widthHint = 182;
		preParseList.setVisible(true);
		preParseList.setLayoutData(gd_list_1);
		new Label(composite_3, SWT.NONE);

		errorPreview = new List(composite_3, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		errorPreview.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 11));
		errorPreview.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		errorPreview.setBackground(SWTResourceManager.getColor(70, 130, 180));
		errorPreview.setVisible(true);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);

		progressBar = new ProgressBar(composite_3, SWT.SMOOTH);
		GridData gd_progressBar_1 = new GridData(SWT.CENTER, SWT.CENTER, true, true, 6, 3);
		gd_progressBar_1.heightHint = 28;
		gd_progressBar_1.widthHint = 1150;
		progressBar.setLayoutData(gd_progressBar_1);
		progressBar.setVisible(true);

		// NOTE- Sets the import progress TabItem to include an instance of the new custom widget AnimatedCanvas
		Image deckExample = SWTResourceManager.getImage(PokegearWindow.class, "/images/helpitem_decklist.png");
		ImageData scaledDeckExample = deckExample.getImageData();
		scaledDeckExample.scaledTo(80, 80);
	}

	// Parsing message functions
	public static void setCardPreviewImage(String imageurl)
	{
		try
		{
			cardPreviewImage = new Image(Display.getCurrent(), new URL(imageurl).openStream());
			cardPreviewImage = new Image(Display.getDefault(), cardPreviewImage.getImageData().scaledTo((int) (0.5 * cardPreview.getBounds().width), (int) (0.89 * cardPreview.getBounds().height)));
			cardPreview.setImage(cardPreviewImage);
			cardPreviewChanged = true;
			cardPreview.redraw();
			System.out.println("Redraw event triggered");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void addOutputInformation(String outp)
	{
		debugMessages.add(outp);
		PokegearWindow.preParseList.setItems(debugMessages.toArray(String[]::new));
		preParseList.select(preParseList.getItemCount());
		preParseList.showSelection();
	}

	public static void addErrorInformation(String outp)
	{
		errorMessages.add(outp);
		PokegearWindow.errorPreview.setItems(errorMessages.toArray(String[]::new));
		errorPreview.select(errorPreview.getItemCount());
		errorPreview.showSelection();
	}

	public static void addCardInformation(String outp)
	{
		cardNameMessages.add(outp);
		PokegearWindow.postParseList.setItems(cardNameMessages.toArray(String[]::new));
		postParseList.select(postParseList.getItemCount());
		postParseList.showSelection();
	}

	// Send pre-parsing data to TabletopParser, start execution, and listen for changes to UI
	private void forwardToParser(String name, String defPath, String decklist) throws Exception
	{
		// Idea here is to re-create the TabItem once parsing starts. TabItem is originally disposed so user can't click to it prematurely
		initLoadingScreen();
		composite_3.setEnabled(true);
		composite_3.setVisible(true);
		importProgress.setControl(composite_3);
		// animatedCanvas.setEnabled(true);
		// composite_3.layout();
		tabFolder.setSelection(importProgress);
		TabletopParser.execfinished = false;
		// TabletopParser will enqueue each job as a SwingWorker so that its worker threads won't block asyncExec calls
		TabletopParser.parse(decklist, defPath, name, true, progressBar, guiDeckList, shlPokegearDeckImporter.getDisplay());

		while (!TabletopParser.runComplete())
		{
			Display.getDefault().readAndDispatch();
		}

		// Call the parser's finalization code once its worker threads are done
		TabletopParser.finalizeParsingRun();
	}

	public boolean flushDispatchQueue()
	{
		return Display.getCurrent().readAndDispatch();
	}

	public static void terminate()
	{
		String[] errs = errorMessages.toArray(String[]::new);
		guiDeckList.setItems(errs);
		tabFolder.setSelection(0);
		importProgress.dispose();
	}
}