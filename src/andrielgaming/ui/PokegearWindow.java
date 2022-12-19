package andrielgaming.ui;

import static java.lang.System.out;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.SwingWorker;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
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
import andrielgaming.parsing.TabletopParser;
import andrielgaming.ui.panels.AnimatedCanvas;
import andrielgaming.ui.panels.CompositeDialog;
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
	private Text txtSavedObjsPath;
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
	public static List errorPreview;
	public static List consoleOutputList;
	public static List pokemonList;
	public static List energyList;
	public static List trainerList;
	public static Composite composite_3;
	public static Image cardPreviewImage;
	public static boolean cardPreviewChanged = false;
	public static TabItem importProgress;
	public static ArrayList<String> debugMessages = new ArrayList<String>();
	public static ArrayList<String> pokemonMsgs = new ArrayList<String>();
	public static ArrayList<String> trainerMsgs = new ArrayList<String>();
	public static ArrayList<String> energyMsgs = new ArrayList<String>();
	public static ArrayList<String> errorMessages = new ArrayList<String>();
	public static AnimatedCanvas animatedCanvas;
	public static int cardCount = 0;
	public static Label cardCounter;
	private static Listener shellListener;
	private boolean maximized = false;
	private Text txtHereYouCan;
	private Text txtIfTheFilepath;
	public static String TtsFilepath = PokegearWindow.getPath();
	public static int imgDefWidth;
	public static int imgDefHeight;
	public static double shellRatio;

	// Default file path with system user injected, usually works
	private static String usr = System.getProperty("user.name");
	private static Scanner s = new Scanner(System.in);

	public static String getPath()
	{
		String defPath = "";
		//	If Linux
		if (System.getProperty("os.name").contains("nix") || System.getProperty("os.name").contains("nux") || System.getProperty("os.name").contains("aix"))
			defPath = "/home/" + usr + "/.local/share/Tabletop Simulator/Saves/Saved Objects/";
		// 	If Windows
		else if (System.getProperty("os.name").contains("Windows"))
			defPath = new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
		return defPath;
	}
	
	public static void main(String[] args) throws Exception
	{
		/* 	Here is where I would put my OS-dependent runs... if I had any lol
		 * 
		 */
		shellListener = new Listener()
		{
			public void handleEvent(Event e)
			{
				// Left here as object with switch-case for future expandability if needed
				switch (e.type)
				{
					case SWT.Resize:
						// shlPokegearDeckImporter.setSize(shlPokegearDeckImporter.getClientArea().width, shlPokegearDeckImporter.getClientArea().height);
						shlPokegearDeckImporter.setMinimumSize(800, 600);
						shlPokegearDeckImporter.layout(true, true);
						shlPokegearDeckImporter.redraw();
						Rectangle screenSize = Display.getCurrent().getPrimaryMonitor().getBounds();
						shellRatio = screenSize.height / screenSize.width;
						break;
					case SWT.DragDetect:
						out.println("Drag Detected");
						shlPokegearDeckImporter.setLocation(e.x, e.y);
						break;
				}
			}
		};

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

			shellRatio = screenSize.height / screenSize.width;

			Menu menu = new Menu(shlPokegearDeckImporter, SWT.BAR);
			shlPokegearDeckImporter.setMenuBar(menu);
			menu.addListener(SWT.DragDetect, shellListener);

			MenuItem mntmExit = new MenuItem(menu, SWT.CASCADE);
			mntmExit.setText("Menu");

			Menu menu_1 = new Menu(mntmExit);
			mntmExit.setMenu(menu_1);

			MenuItem mntmNewItem = new MenuItem(menu_1, SWT.NONE);
			mntmNewItem.setText("Exit PokeGear");

			MenuItem mntmMaximizeWindow = new MenuItem(menu_1, SWT.NONE);
			mntmMaximizeWindow.setEnabled(false);
			mntmMaximizeWindow.setText("Maximize Window");
			new Label(shlPokegearDeckImporter, SWT.NONE);
			new Label(shlPokegearDeckImporter, SWT.NONE);
			new Label(shlPokegearDeckImporter, SWT.NONE);
			mntmMaximizeWindow.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					Rectangle screenSize = display.getPrimaryMonitor().getBounds();
					if (!maximized)
					{
						/*shlPokegearDeckImporter.setSize(screenSize.x, screenSize.y);
						shlPokegearDeckImporter.layout();*/
						shlPokegearDeckImporter.setBounds(screenSize);
						mntmMaximizeWindow.setText("Minimize Window");
						maximized = true;
					}
					else if (maximized)
					{
						shlPokegearDeckImporter.setSize((int) (screenSize.width / 1.5), (int) (screenSize.height / 1.5));
						shlPokegearDeckImporter.setLocation((screenSize.width - shlPokegearDeckImporter.getBounds().width) / 2, (screenSize.height - shlPokegearDeckImporter.getBounds().height) / 2);
						shlPokegearDeckImporter.layout();
						mntmMaximizeWindow.setText("Maximize Window");
						maximized = false;
					}
				}
			});

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
		shlPokegearDeckImporter.setModified(true);
		animatedPikachu = SWTResourceManager.getImage(PokegearWindow.class, "/images/dancingpikachu_resized.gif");
		// shlPokegearDeckImporter.setMaximized(true);
		shlPokegearDeckImporter.setBackgroundImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/background.jpg"));
		shlPokegearDeckImporter.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		shlPokegearDeckImporter.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/pokegearicon.png"));
		shlPokegearDeckImporter.setBackground(SWTResourceManager.getColor(0, 0, 153));
		Rectangle screenSize = Display.getDefault().getPrimaryMonitor().getBounds();
		shlPokegearDeckImporter.setSize(1579, 964);
		shlPokegearDeckImporter.setText("Pokegear Deck Importer");
		GridLayout gl_shlPokegearDeckImporter = new GridLayout(5, true);

		gl_shlPokegearDeckImporter.marginTop = 25;
		gl_shlPokegearDeckImporter.marginBottom = 25;
		gl_shlPokegearDeckImporter.marginRight = 25;
		gl_shlPokegearDeckImporter.marginLeft = 25;
		shlPokegearDeckImporter.setLayout(gl_shlPokegearDeckImporter);
		shlPokegearDeckImporter.addListener(SWT.Resize, shellListener);

		CLabel lblNewLabel = new CLabel(shlPokegearDeckImporter, SWT.NONE | ~SWT.RESIZE);

		lblNewLabel.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/pokegearicon.png"));
		lblNewLabel.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 30, SWT.BOLD));
		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.FILL, false, false, 2, 1);
		gd_lblNewLabel.heightHint = 92;
		lblNewLabel.setLayoutData(gd_lblNewLabel);
		lblNewLabel.setText("  Pokegear Deck Importer for Tabletop Simulator   ");
		new Label(shlPokegearDeckImporter, SWT.NONE);
		new Label(shlPokegearDeckImporter, SWT.NONE);
		new Label(shlPokegearDeckImporter, SWT.NONE);

		tabFolder = new TabFolder(shlPokegearDeckImporter, SWT.NONE);
		tabFolder.setBackground(SWTResourceManager.getColor(0, 0, 51));
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tabFolder.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 15, SWT.NORMAL));

		TabItem tab_import = new TabItem(tabFolder, SWT.NONE);
		tab_import.setToolTipText("The main tool, import your decks here.");
		tab_import.setText("Import");

		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		composite_1.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 14, SWT.NORMAL));
		tab_import.setControl(composite_1);
		composite_1.setLayout(new GridLayout(8, false));

		final StyledText styledText = new StyledText(composite_1, SWT.BORDER | SWT.FULL_SELECTION | SWT.WRAP);
		GridData gd_styledText = new GridData(SWT.FILL, SWT.FILL, true, true, 5, 19);
		gd_styledText.heightHint = 558;
		gd_styledText.widthHint = 842;
		styledText.setLayoutData(gd_styledText);
		styledText.setSelectionForeground(SWTResourceManager.getColor(255, 255, 255));
		styledText.setMarginColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		styledText.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		styledText.setBackground(SWTResourceManager.getColor(102, 153, 255));

		CLabel lblNewLabel_1 = new CLabel(composite_1, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblNewLabel_1.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.BOLD));
		lblNewLabel_1.setText("Deck Name:");

		text = new Text(composite_1, SWT.BORDER);
		text.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		GridData gd_text = new GridData(SWT.LEFT, SWT.FILL, true, false, 1, 1);
		gd_text.widthHint = 440;
		text.setLayoutData(gd_text);

		CLabel lblNewLabel_2 = new CLabel(composite_1, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblNewLabel_2.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.BOLD));
		lblNewLabel_2.setImage(null);
		lblNewLabel_2.setText("Errors From Last Import:");
		new Label(composite_1, SWT.NONE);

		guiDeckList = new List(composite_1, SWT.BORDER | SWT.V_SCROLL);
		guiDeckList.setToolTipText("Cards parsed into the new deck show up here.");
		guiDeckList.setDragDetect(false);
		guiDeckList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		guiDeckList.setBackground(SWTResourceManager.getColor(192, 192, 192));
		GridData gd_guiDeckList = new GridData(SWT.FILL, SWT.FILL, false, false, 3, 17);
		gd_guiDeckList.heightHint = 448;
		gd_guiDeckList.widthHint = 332;
		guiDeckList.setLayoutData(gd_guiDeckList);

		CLabel label = new CLabel(composite_1, SWT.NONE);
		label.setImage(null);
		label.setText("");

		Button btnClearAll = new Button(composite_1, SWT.CENTER);
		btnClearAll.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 24, SWT.BOLD));
		GridData gd_btnClearAll = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
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
		GridData gd_btnPasteFromClipboard = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
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
		GridData gd_btnNewButton = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_btnNewButton.widthHint = 367;
		btnNewButton.setLayoutData(gd_btnNewButton);
		btnNewButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				String decklist = styledText.getText();
				String defPath = PokegearWindow.getPath();
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
		composite_2.setLayout(new GridLayout(6, false));

		txtIfTheFilepath = new Text(composite_2, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.CENTER | SWT.MULTI);
		txtIfTheFilepath.setVisible(true);
		txtIfTheFilepath.setEnabled(false);
		txtIfTheFilepath.setText("If the filepath that PokeGear found is incorrect, you can change it below using the \"Locate or Change Folder\" button.\r\nJust browse to the 'Saved Objects' folder and press 'Select Folder'. Use the 'Help' tab for more information.\r\n");
		txtIfTheFilepath.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.BOLD));
		txtIfTheFilepath.setEditable(false);
		txtIfTheFilepath.setDragDetect(false);
		txtIfTheFilepath.setDoubleClickEnabled(false);
		GridData gd_txtIfTheFilepath = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		gd_txtIfTheFilepath.heightHint = 48;
		gd_txtIfTheFilepath.widthHint = 404;
		txtIfTheFilepath.setLayoutData(gd_txtIfTheFilepath);
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);

		Label label_3 = new Label(composite_2, SWT.BORDER | SWT.SEPARATOR | SWT.HORIZONTAL);
		label_3.setVisible(true);
		label_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 6, 1));

		CLabel lblLocationOfYour = new CLabel(composite_2, SWT.NONE);
		lblLocationOfYour.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.BOLD));
		lblLocationOfYour.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1));
		lblLocationOfYour.setText("Save Decks To: ");

		txtSavedObjsPath = new Text(composite_2, SWT.BORDER | SWT.READ_ONLY);
		txtSavedObjsPath.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.BOLD));
		txtSavedObjsPath.setEditable(true);
		txtSavedObjsPath.setText(PokegearWindow.getPath());
		txtSavedObjsPath.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 3, 1));
		txtSavedObjsPath.addVerifyListener(new VerifyListener()
		{
			@Override
			public void verifyText(VerifyEvent e)
			{
				PokegearWindow.TtsFilepath = e.text;
			}
		});

		Button btnSetFolder = new Button(composite_2, SWT.NONE);
		btnSetFolder.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnSetFolder.setText("Locate or Change Folder");
		btnSetFolder.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.BOLD | SWT.ITALIC));
		new Label(composite_2, SWT.NONE);
		btnSetFolder.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				DirectoryDialog dialog = new DirectoryDialog(shlPokegearDeckImporter, SWT.OPEN);
				dialog.setText("Locate the \"Saved Games\" folder");
				if (txtSavedObjsPath.getText() != null)
				{
					String fpath = txtSavedObjsPath.getText();
					dialog.setFilterPath(fpath);
				}
				String dir = dialog.open();
				if (dir != null)
				{
					txtSavedObjsPath.setText(dir);
				}
			}
		});

		Label label_3_1 = new Label(composite_2, SWT.BORDER | SWT.SEPARATOR | SWT.HORIZONTAL);
		label_3_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1));
		label_3_1.setVisible(true);

		CLabel lblCardSleeveback = new CLabel(composite_2, SWT.NONE);
		lblCardSleeveback.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.BOLD));
		lblCardSleeveback.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		lblCardSleeveback.setText("Card Sleeve (Back)");

		CLabel lblImagePreview = new CLabel(composite_2, SWT.NONE);
		lblImagePreview.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 1));
		lblImagePreview.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.BOLD));
		lblImagePreview.setText("Image Preview");

		CLabel lblCurrentSelection = new CLabel(composite_2, SWT.NONE);
		lblCurrentSelection.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 1));
		lblCurrentSelection.setText("Current Selection");
		lblCurrentSelection.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.BOLD));

		Label label_4 = new Label(composite_2, SWT.SEPARATOR | SWT.VERTICAL);
		label_4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 5));
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);

		List list = new List(composite_2, SWT.BORDER | SWT.V_SCROLL);
		list.setItems(new String[]
		{ "Default", "ArceusAnniversary", "Celebi", "Chespin1", "Chespin2", "Chespin3", "Darkrai", "DeoxysFullColor", "DruddigonClawMarks", "EeveeSilhouette", "EeveelutionsEspeon1", "EeveelutionsEspeon2", "EeveelutionsEspeon3", "EeveelutionsFlareon1", "EeveelutionsFlareon2", "EeveelutionsFlareon3", "EeveelutionsGlaceon1", "EeveelutionsGlaceon2", "EeveelutionsGlaceon3", "EeveelutionsJolteon1", "EeveelutionsJolteon2", "EeveelutionsJolteon3", "EeveelutionsLeafeon1", "EeveelutionsLeafeon2", "EeveelutionsLeafeon3", "EeveelutionsSylveon1", "EeveelutionsSylveon2", "EeveelutionsSylveon3", "EeveelutionsUmbreon1", "EeveelutionsUmbreon2", "EeveelutionsUmbreon3", "EeveelutionsVaporeon1", "EeveelutionsVaporeon2", "EeveelutionsVaporeon3", "EnergyDarkness", "EnergyDragon", "EnergyFairy", "EnergyFighting", "EnergyFire", "EnergyGrass", "EnergyLightning", "EnergyMetal", "EnergyPsychic", "EnergyWater", "Fennekin1", "Fennekin2", "Fennekin3", "Fennekin4", "Froakie1", "Froakie2", "Froakie3", "GarchompSilhouette", "GenesectSilhouette", "GengarHalloween", "GoldSleeve1Pikachu", "GoldSleeve2PikachuCoin", "GoldSleeve3PikachuAnniversary", "GoldSleeve4CharizardFullGold", "GoldSleeve5BlastoiseBlueTrim", "GoldSleeve6CharizardRedTrim", "GoldSleeve7VenusaurGreenTrim", "GoldSleeve8BlastoiseFullGold", "GoldSleeve9VenusaurFullGold", "GourgeistPokeball", "Halloween2014", "Jirachi", "Manaphy", "Mew", "MewtwoFullColor", "MegaGengarFullColor", "MegaCharizardX", "MegaCharizardY", "MegaBlastoise", "MegaVenusaur", "MegaMewtwoDuo", "MiloticGlassArt", "NewFriendsLeageFennekin", "Pax2014Blue", "ParallelLeagueChikorita", "PikachuSilhouette", "PokemonClub", "PyroarSilhouetteFlames", "RaichuSleeve", "Shaymin", "SteamLeagueYveltal", "TeamAqua", "TeamMagma", "ThunderousFullColor", "TrevenantStylizedSilhouette", "TrainerBoxGenerations", "TrainerBoxGroudon", "TrainerBoxGyarados", "TrainerBoxHoopa", "TrainerBoxKyogre", "TrainerBoxMegaAlakazam", "TrainerBoxMewtwoX", "TrainerBoxMewtwoY", "TrainerBoxRayquaza", "TrainerBoxVolcanion", "Worlds2013", "Worlds2014", "Worlds2015", "Worlds2015Alt", "Worlds2016", "VictiniFullArt", "VictiniWithTrim", "Xerneas1", "Xerneas2", "Xerneas3", "Yveltal1", "Yveltal2", "Yveltal3" });
		list.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		list.setBackground(SWTResourceManager.getColor(100, 149, 237));
		GridData gd_list = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 4);
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
					img = new Image(Display.getCurrent(), img.getImageData().scaledTo(imgDefWidth, imgDefHeight));
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
		label_1.setAlignment(SWT.CENTER);
		label_1.setForeground(SWTResourceManager.getColor(135, 206, 250));
		label_1.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/ptcg_back.png"));
		imgDefWidth = label_1.getImage().getBounds().width;
		imgDefHeight = label_1.getImage().getBounds().height;
		GridData gd_label_1 = new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 1);
		gd_label_1.heightHint = 461;
		label_1.setLayoutData(gd_label_1);
		label_1.setText("");
		imgDefWidth = (int) (imgDefWidth * 0.75);

		CLabel label_2 = new CLabel(composite_2, SWT.RIGHT);
		label_2.setRightMargin(1);
		label_2.setLeftMargin(1);
		label_2.setAlignment(SWT.CENTER);
		GridData gd_label_2 = new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 1);
		gd_label_2.heightHint = 443;
		label_2.setLayoutData(gd_label_2);
		label_2.setBottomMargin(5);
		label_2.setRightMargin(1);
		label_2.setLeftMargin(1);
		label_2.setForeground(SWTResourceManager.getColor(127, 255, 212));
		label_2.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/ptcg_back.png"));
		label_2.setText("");

		txtHereYouCan = new Text(composite_2, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		txtHereYouCan.setVisible(true);
		txtHereYouCan.setEnabled(false);
		txtHereYouCan.setTabs(4);
		txtHereYouCan.setText("Here you can view and set custom card sleeves for your deck.\r\n\r\nThe sleeve set here is only applied to the current deck you're importing. Once you \r\nstart a new import, the program should reset to the default sleeve.\r\n\r\nClick a sleeve name below to see what it looks like. It will show on screen afterwards.\r\nThese sleeves are saved online and not downloaded to your machine at any time.\r\n\r\nOnce you find one you like, click \"Change Card Sleeve\" to set it.");
		txtHereYouCan.setDoubleClickEnabled(false);
		txtHereYouCan.setDragDetect(false);
		txtHereYouCan.setEditable(false);
		txtHereYouCan.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 20, SWT.BOLD));
		GridData gd_txtHereYouCan = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 3);
		gd_txtHereYouCan.horizontalIndent = 6;
		gd_txtHereYouCan.widthHint = 281;
		gd_txtHereYouCan.heightHint = 88;
		txtHereYouCan.setLayoutData(gd_txtHereYouCan);
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);

		Button btnChangeCardSleeve = new Button(composite_2, SWT.NONE);
		btnChangeCardSleeve.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnChangeCardSleeve.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 24, SWT.BOLD | SWT.ITALIC));
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
					img = new Image(Display.getCurrent(), img.getImageData().scaledTo(imgDefWidth, imgDefHeight));
					label_2.setImage(img);
				} catch (Exception ie)
				{
				}
			}
		});

		Button btnResetToDefault = new Button(composite_2, SWT.NONE);
		btnResetToDefault.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnResetToDefault.setText("Reset to Default");
		btnResetToDefault.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 24, SWT.BOLD | SWT.ITALIC));
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
		tab_help.setText("Help (Disabled- Broken in Linux)");
		
		Composite composite = new Composite(tabFolder, SWT.NO_BACKGROUND);
		composite.setForeground(SWTResourceManager.getColor(51, 0, 0));
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		composite.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));
		tab_help.setControl(composite);
		composite.setLayout(new GridLayout(1, false));
		composite.setEnabled(false);

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
		// xpndtmNewExpanditem_1.setExpanded(true);
		xpndtmNewExpanditem_1.setText("What deck list formats will work with Pokegear?");

		txtThisProgramWas = new Text(expandBar, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		txtThisProgramWas.setEnabled(true);
		txtThisProgramWas.setText("This program works with any standard deck-list format, so long as they have the following information:\r\n\r\n[Number of Cards] [Card Name] [Set Abbreviation] [Set Number]\r\n\r\nAs an example, take this export for 3 copies of Arceus VSTAR from Brilliant Stars: \r\n\t\n3 Arceus VSTAR BRS 123\r\n\r\nAny deck-list that follows this format will work, but there are certain symbols that the parser is set to ignore.\r\nFor example, Pokemon TCG Online would export the above Arceus VSTAR with an asterisk in front:\r\n\n\t* 3 Arceus VSTAR BRS 123\r\n\r\nThis is fine, because the program will ignore the symbol. Additionally, it will ignore any lines that don't start with either a delimeter or a number (indicating a quantity of cards). For example:\r\n\r\nPokémon (23)\r\n\n4 Arceus V BRS 122\r\n\n* 3 Arceus VSTAR BRS 123\r\n\r\nThis is acceptable. It will skip the first line and import the next 2.");
		txtThisProgramWas.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));
		txtThisProgramWas.setEditable(false);
		txtThisProgramWas.setDragDetect(false);
		txtThisProgramWas.setDoubleClickEnabled(false);
		xpndtmNewExpanditem_1.setControl(txtThisProgramWas);
		xpndtmNewExpanditem_1.setHeight(472);

		ExpandItem xpndtmNewExpanditem_2 = new ExpandItem(expandBar, SWT.NONE);
		xpndtmNewExpanditem_2.setText("Where can I get a deck list?");

		txtIfYouAre = new Text(expandBar, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		txtIfYouAre.setEnabled(true);
		txtIfYouAre.setText("If you are seeking to make one yourself, you can use online tools or even Pokemon TCG Online, which is free. PTCGO allows you to create decklists even if you use unowned cards, you just need to click the \"Show Not Owned\" checkbox. For pre-made decklists, LimitlessTCG is my personal choice.\r\n\r\nIn the future, PokeGear may include a tool allowing you to build a deck right here in the program instead. Until then, use one of the following sites or tools:\r\n\r\nLimitlessTCG.com\r\nJustinbasil.com\r\nPokemon TCG Online\r\nPokemoncard.io/deck-search/");
		txtIfYouAre.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));
		txtIfYouAre.setEditable(false);
		txtIfYouAre.setDragDetect(false);
		txtIfYouAre.setDoubleClickEnabled(false);
		xpndtmNewExpanditem_2.setControl(txtIfYouAre);
		xpndtmNewExpanditem_2.setHeight(220);

		ExpandItem xpndtmNewExpanditem_3 = new ExpandItem(expandBar, SWT.NONE);
		// xpndtmNewExpanditem_3.setExpanded(true);
		xpndtmNewExpanditem_3.setText("How do I use my new deck in Tabletop Simulator?");

		txtForTheDecks = new Text(expandBar, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		txtForTheDecks.setEnabled(true);
		txtForTheDecks.setText("For the decks Pokegear creates to be spawnable in Tabletop Simulator, you need to make sure your file path is correctly configured. If you are running this on a Windows system, PokeGear likely has already found the file path on its own. You can check this in the 'Options' tab. If you are not on Windows or this path is incorrect, follow the instructions there to change it.\r\n\r\nThe folder you are looking for is 'Saved Objects'.\r\n\r\nFor Windows systems, find it at ~/Documents/My Games/Tabletop Simulator/Saves/Saved Objects\r\nFor Linux systems, find it at ~/. local/share/Tabletop Simulator/Saves/Saved Objects \r\nFor Mac systems, find it at ~/Library/Tabletop Simulator/Saves/Saved Objects");
		txtForTheDecks.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));
		txtForTheDecks.setEditable(false);
		txtForTheDecks.setDragDetect(false);
		txtForTheDecks.setDoubleClickEnabled(false);
		xpndtmNewExpanditem_3.setControl(txtForTheDecks);
		xpndtmNewExpanditem_3.setHeight(228);

		importProgress = new TabItem(tabFolder, SWT.NONE);
		importProgress.setText("Deck List");

		composite_3 = new Composite(tabFolder, SWT.EMBEDDED);
		composite_3.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		composite_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		composite_3.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		importProgress.setControl(composite_3);
		GridLayout g = new GridLayout(5, true);
		GridData gdata = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdata.widthHint = (int) (composite_3.getSize().x * 0.2);
		// gdata.heightHint = (int)(shlPokegearDeckImporter.getSize().y * 0.2);
		composite_3.setLayout(g);
		composite_3.setLayoutData(gdata);
		composite_3.setVisible(true);

		animatedCanvas = new AnimatedCanvas(composite_3, SWT.NO_BACKGROUND | SWT.NO_MERGE_PAINTS);
		animatedCanvas.setLayoutDeferred(true);
		animatedCanvas.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		animatedCanvas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		animatedCanvas.setVisible(true);
		GridData gd_animatedCanvas = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 2);
		gd_animatedCanvas.heightHint = 287;
		gd_animatedCanvas.widthHint = (int) (composite_3.getSize().x * 0.2);
		animatedCanvas.setScale(400, 292);
		animatedCanvas.setLayoutData(gd_animatedCanvas);
		animatedCanvas.frameTimer = 30;
		animatedCanvas.setImageClasspath("/images/charmander.gif");
		animatedCanvas.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/charmander.gif"));
		animatedCanvas.setVisible(true);

		Label lblNewLabel_3_1 = new Label(composite_3, SWT.NONE);
		GridData gd_lblNewLabel_3_1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_lblNewLabel_3_1.widthHint = (int) (composite_3.getSize().x * 0.2);
		lblNewLabel_3_1.setLayoutData(gd_lblNewLabel_3_1);
		lblNewLabel_3_1.setText("Card Preview");
		lblNewLabel_3_1.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		lblNewLabel_3_1.setAlignment(SWT.CENTER);
		lblNewLabel_3_1.setVisible(true);

		Label lblCardsGenerated = new Label(composite_3, SWT.CENTER);
		GridData gd_lblCardsGenerated = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_lblCardsGenerated.widthHint = (int) (composite_3.getSize().x * 0.2);
		lblCardsGenerated.setLayoutData(gd_lblCardsGenerated);
		lblCardsGenerated.setText("Pokemon");
		lblCardsGenerated.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));
		lblCardsGenerated.setVisible(true);

		Label lblTrainerCards = new Label(composite_3, SWT.CENTER);
		GridData gd_lblTrainerCards = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_lblTrainerCards.widthHint = (int) (composite_3.getSize().x * 0.2);
		lblTrainerCards.setLayoutData(gd_lblTrainerCards);
		lblTrainerCards.setVisible(true);
		lblTrainerCards.setText("Trainer Cards");
		lblTrainerCards.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));

		Label lblErrors = new Label(composite_3, SWT.CENTER);
		GridData gd_lblErrors = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_lblErrors.widthHint = (int) (composite_3.getSize().x * 0.4);
		lblErrors.setLayoutData(gd_lblErrors);
		lblErrors.setVisible(true);
		lblErrors.setText("Basic and Special Energy");
		lblErrors.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));

		// NOTE-- Code for the output reporting labels for card, sleeve, and errors, with cardPreview accessible by outside #TabletopParser class
		cardPreview = new Label(composite_3, SWT.NO_BACKGROUND | SWT.CENTER);
		cardPreview.setSize(new Point((int) (composite_3.getBounds().width), composite_3.getBounds().height));
		cardPreview.setImage(null);
		cardPreview.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		GridData gd_cardPreview = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_cardPreview.heightHint = 248;
		gd_cardPreview.widthHint = (int) (composite_3.getSize().x * 0.2);
		cardPreview.setLayoutData(gd_cardPreview);
		cardPreview.setVisible(true);
		cardPreview.addPaintListener(new PaintListener()
		{
			public void paintControl(PaintEvent ev)
			{
				if (cardPreviewChanged)
				{
					cardPreviewImage = new Image(Display.getDefault(), cardPreviewImage.getImageData());
					cardPreviewChanged = false;
					cardPreview.setImage(cardPreviewImage);
					cardPreview.redraw();
					cardPreview.update();
					composite_3.redraw();
					composite_3.update();
					System.out.println("Redraw event triggered in cardPreview.PaintControl");
				}
			}
		});

		// NOTE-- Accessed by #TabletopParser as each #ParsingThread completes its work; Each new card will be displayed here alongside an image preview in the cardPreview Label object
		pokemonList = new List(composite_3, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		pokemonList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		pokemonList.setBackground(SWTResourceManager.getColor(0, 191, 255));
		GridData gd_list_1_1 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_list_1_1.widthHint = (int) (composite_3.getSize().x * 0.2);
		gd_list_1_1.heightHint = 276;
		pokemonList.setLayoutData(gd_list_1_1);
		pokemonList.setVisible(true);

		trainerList = new List(composite_3, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_trainerList = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_trainerList.heightHint = 190;
		gd_trainerList.widthHint = (int) (composite_3.getSize().x * 0.2);
		trainerList.setLayoutData(gd_trainerList);
		trainerList.setVisible(true);
		trainerList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		trainerList.setBackground(SWTResourceManager.getColor(0, 191, 255));

		energyList = new List(composite_3, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_energyList = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_energyList.heightHint = 213;
		gd_energyList.widthHint = (int) (composite_3.getSize().x * 0.2);
		energyList.setLayoutData(gd_energyList);
		energyList.setVisible(true);
		energyList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		energyList.setBackground(SWTResourceManager.getColor(0, 191, 255));

		Label lblProvidedDeckList = new Label(composite_3, SWT.CENTER);
		lblProvidedDeckList.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblProvidedDeckList.setAlignment(SWT.LEFT);
		lblProvidedDeckList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 22, SWT.NORMAL));
		GridData gd_lblProvidedDeckList = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
		gd_lblProvidedDeckList.widthHint = (int) (composite_3.getSize().x * 0.4);
		gd_lblProvidedDeckList.heightHint = 30;
		lblProvidedDeckList.setLayoutData(gd_lblProvidedDeckList);
		lblProvidedDeckList.setVisible(true);
		lblProvidedDeckList.setText("   Importing your deck, please wait!");
		new Label(composite_3, SWT.NONE);

		cardCounter = new Label(composite_3, SWT.NONE);
		cardCounter.setAlignment(SWT.CENTER);
		cardCounter.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		cardCounter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		cardCounter.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 20, SWT.BOLD));
		cardCounter.setText("  Cards Imported: " + PokegearWindow.cardCount);
		new Label(composite_3, SWT.NONE);

		// NOTE-- Accessed by #TabletopParser before JSON parsing but after plaintext is processed; Adds the imported list line-by-line, items being removed as each processed
		consoleOutputList = new List(composite_3, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		consoleOutputList.setDragDetect(false);
		consoleOutputList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 13, SWT.NORMAL));
		consoleOutputList.setBackground(SWTResourceManager.getColor(0, 191, 255));
		GridData gd_list_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 5);
		gd_list_1.heightHint = 78;
		gd_list_1.widthHint = (int) (composite_3.getSize().x * 0.6);
		consoleOutputList.setVisible(true);
		consoleOutputList.setLayoutData(gd_list_1);

		errorPreview = new List(composite_3, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		GridData gd_errorPreview = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 5);
		gd_errorPreview.heightHint = 66;
		gd_errorPreview.widthHint = (int) (composite_3.getSize().x * 0.4);
		errorPreview.setLayoutData(gd_errorPreview);
		errorPreview.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		errorPreview.setBackground(SWTResourceManager.getColor(70, 130, 180));
		errorPreview.setVisible(true);

		progressBar = new ProgressBar(composite_3, SWT.SMOOTH);
		GridData gd_progressBar_1 = new GridData(SWT.FILL, SWT.TOP, true, false, 5, 1);
		gd_progressBar_1.heightHint = 22;
		gd_progressBar_1.widthHint = (int) (composite_3.getSize().x);
		progressBar.setLayoutData(gd_progressBar_1);
		progressBar.setVisible(true);

		// NOTE- Sets the import progress TabItem to include an instance of the new custom widget AnimatedCanvas
		Image deckExample = SWTResourceManager.getImage(PokegearWindow.class, "/images/helpitem_decklist.png");
		ImageData scaledDeckExample = deckExample.getImageData();
		scaledDeckExample.scaledTo(80, 80);

		importProgress.dispose();
	}

	// NOTE-- Here for anchor to method
	public void initLoadingScreen()
	{
		importProgress = new TabItem(tabFolder, SWT.NONE);
		importProgress.setText("Deck List");

		composite_3 = new Composite(tabFolder, SWT.EMBEDDED);
		composite_3.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		composite_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		composite_3.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		importProgress.setControl(composite_3);
		GridLayout g = new GridLayout(5, true);
		GridData gdata = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdata.widthHint = (int) (composite_3.getSize().x * 0.2);
		// gdata.heightHint = (int)(shlPokegearDeckImporter.getSize().y * 0.2);
		composite_3.setLayout(g);
		composite_3.setLayoutData(gdata);
		composite_3.setVisible(true);

		animatedCanvas = new AnimatedCanvas(composite_3, SWT.NO_BACKGROUND | SWT.NO_MERGE_PAINTS);
		animatedCanvas.setLayoutDeferred(true);
		animatedCanvas.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		animatedCanvas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		animatedCanvas.setVisible(true);
		GridData gd_animatedCanvas = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 2);
		gd_animatedCanvas.heightHint = 287;
		gd_animatedCanvas.widthHint = (int) (composite_3.getSize().x * 0.2);
		animatedCanvas.setScale(400, 292);
		animatedCanvas.setLayoutData(gd_animatedCanvas);
		animatedCanvas.frameTimer = 30;
		animatedCanvas.setImageClasspath("/images/charmander.gif");
		animatedCanvas.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/charmander.gif"));
		animatedCanvas.setVisible(true);

		Label lblNewLabel_3_1 = new Label(composite_3, SWT.NONE);
		GridData gd_lblNewLabel_3_1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_lblNewLabel_3_1.widthHint = (int) (composite_3.getSize().x * 0.2);
		lblNewLabel_3_1.setLayoutData(gd_lblNewLabel_3_1);
		lblNewLabel_3_1.setText("Card Preview");
		lblNewLabel_3_1.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		lblNewLabel_3_1.setAlignment(SWT.CENTER);
		lblNewLabel_3_1.setVisible(true);

		Label lblCardsGenerated = new Label(composite_3, SWT.CENTER);
		GridData gd_lblCardsGenerated = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_lblCardsGenerated.widthHint = (int) (composite_3.getSize().x * 0.2);
		lblCardsGenerated.setLayoutData(gd_lblCardsGenerated);
		lblCardsGenerated.setText("Pokemon");
		lblCardsGenerated.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));
		lblCardsGenerated.setVisible(true);

		Label lblTrainerCards = new Label(composite_3, SWT.CENTER);
		GridData gd_lblTrainerCards = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_lblTrainerCards.widthHint = (int) (composite_3.getSize().x * 0.2);
		lblTrainerCards.setLayoutData(gd_lblTrainerCards);
		lblTrainerCards.setVisible(true);
		lblTrainerCards.setText("Trainer Cards");
		lblTrainerCards.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));

		Label lblErrors = new Label(composite_3, SWT.CENTER);
		GridData gd_lblErrors = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_lblErrors.widthHint = (int) (composite_3.getSize().x * 0.4);
		lblErrors.setLayoutData(gd_lblErrors);
		lblErrors.setVisible(true);
		lblErrors.setText("Basic and Special Energy");
		lblErrors.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));

		// NOTE-- Code for the output reporting labels for card, sleeve, and errors, with cardPreview accessible by outside #TabletopParser class
		cardPreview = new Label(composite_3, SWT.NO_BACKGROUND | SWT.CENTER);
		cardPreview.setSize(new Point((int) (composite_3.getBounds().width), composite_3.getBounds().height));
		cardPreview.setImage(null);
		cardPreview.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		GridData gd_cardPreview = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_cardPreview.heightHint = 248;
		gd_cardPreview.widthHint = (int) (composite_3.getSize().x * 0.2);
		cardPreview.setLayoutData(gd_cardPreview);
		cardPreview.setVisible(true);
		cardPreview.addPaintListener(new PaintListener()
		{
			public void paintControl(PaintEvent ev)
			{
				if (cardPreviewChanged)
				{
					cardPreviewImage = new Image(Display.getDefault(), cardPreviewImage.getImageData());
					cardPreviewChanged = false;
					cardPreview.setImage(cardPreviewImage);
					cardPreview.redraw();
					cardPreview.update();
					composite_3.redraw();
					composite_3.update();
					System.out.println("Redraw event triggered in cardPreview.PaintControl");
				}
			}
		});

		// NOTE-- Accessed by #TabletopParser as each #ParsingThread completes its work; Each new card will be displayed here alongside an image preview in the cardPreview Label object
		pokemonList = new List(composite_3, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		pokemonList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		pokemonList.setBackground(SWTResourceManager.getColor(0, 191, 255));
		GridData gd_list_1_1 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_list_1_1.widthHint = (int) (composite_3.getSize().x * 0.2);
		gd_list_1_1.heightHint = 276;
		pokemonList.setLayoutData(gd_list_1_1);
		pokemonList.setVisible(true);

		trainerList = new List(composite_3, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_trainerList = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_trainerList.heightHint = 190;
		gd_trainerList.widthHint = (int) (composite_3.getSize().x * 0.2);
		trainerList.setLayoutData(gd_trainerList);
		trainerList.setVisible(true);
		trainerList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		trainerList.setBackground(SWTResourceManager.getColor(0, 191, 255));

		energyList = new List(composite_3, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_energyList = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_energyList.heightHint = 213;
		gd_energyList.widthHint = (int) (composite_3.getSize().x * 0.2);
		energyList.setLayoutData(gd_energyList);
		energyList.setVisible(true);
		energyList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		energyList.setBackground(SWTResourceManager.getColor(0, 191, 255));

		Label lblProvidedDeckList = new Label(composite_3, SWT.CENTER);
		lblProvidedDeckList.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblProvidedDeckList.setAlignment(SWT.LEFT);
		lblProvidedDeckList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 22, SWT.NORMAL));
		GridData gd_lblProvidedDeckList = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
		gd_lblProvidedDeckList.widthHint = (int) (composite_3.getSize().x * 0.4);
		gd_lblProvidedDeckList.heightHint = 30;
		lblProvidedDeckList.setLayoutData(gd_lblProvidedDeckList);
		lblProvidedDeckList.setVisible(true);
		lblProvidedDeckList.setText("   Importing your deck, please wait!");
		new Label(composite_3, SWT.NONE);

		cardCounter = new Label(composite_3, SWT.NONE);
		cardCounter.setAlignment(SWT.CENTER);
		cardCounter.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		cardCounter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		cardCounter.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 20, SWT.BOLD));
		cardCounter.setText("  Cards Imported: " + PokegearWindow.cardCount);
		new Label(composite_3, SWT.NONE);

		// NOTE-- Accessed by #TabletopParser before JSON parsing but after plaintext is processed; Adds the imported list line-by-line, items being removed as each processed
		consoleOutputList = new List(composite_3, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		consoleOutputList.setDragDetect(false);
		consoleOutputList.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 13, SWT.NORMAL));
		consoleOutputList.setBackground(SWTResourceManager.getColor(0, 191, 255));
		GridData gd_list_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 5);
		gd_list_1.heightHint = 78;
		gd_list_1.widthHint = (int) (composite_3.getSize().x * 0.6);
		consoleOutputList.setVisible(true);
		consoleOutputList.setLayoutData(gd_list_1);

		errorPreview = new List(composite_3, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		GridData gd_errorPreview = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 5);
		gd_errorPreview.heightHint = 66;
		gd_errorPreview.widthHint = (int) (composite_3.getSize().x * 0.4);
		errorPreview.setLayoutData(gd_errorPreview);
		errorPreview.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		errorPreview.setBackground(SWTResourceManager.getColor(70, 130, 180));
		errorPreview.setVisible(true);

		progressBar = new ProgressBar(composite_3, SWT.SMOOTH);
		GridData gd_progressBar_1 = new GridData(SWT.FILL, SWT.TOP, true, false, 5, 1);
		gd_progressBar_1.heightHint = 22;
		gd_progressBar_1.widthHint = (int) (composite_3.getSize().x);
		progressBar.setLayoutData(gd_progressBar_1);
		progressBar.setVisible(true);
	}

	// Parsing message functions
	public static void setCardPreviewImage(String imageurl)
	{
		try
		{
			cardPreviewImage = new Image(Display.getCurrent(), new URL(imageurl).openStream());
			cardPreviewImage = new Image(Display.getDefault(), cardPreviewImage.getImageData().scaledTo((int) (0.64 * cardPreview.getBounds().width), (int) (0.89 * cardPreview.getBounds().height)));
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
		PokegearWindow.consoleOutputList.setItems(debugMessages.toArray(String[]::new));
		consoleOutputList.select(consoleOutputList.getItemCount() - 1);
		consoleOutputList.showSelection();
	}

	public static void addErrorInformation(String outp)
	{
		errorMessages.add(outp);
		PokegearWindow.errorPreview.setItems(errorMessages.toArray(String[]::new));
		errorPreview.select(errorPreview.getItemCount() - 1);
		errorPreview.showSelection();
	}

	public static void addPokemonInfo(String outp)
	{
		pokemonMsgs.add(outp);
		PokegearWindow.pokemonList.setItems(pokemonMsgs.toArray(String[]::new));
		pokemonList.select(pokemonList.getItemCount() - 1);
		pokemonList.showSelection();
	}

	public static void addTrainerInfo(String outp)
	{
		trainerMsgs.add(outp);
		PokegearWindow.trainerList.setItems(trainerMsgs.toArray(String[]::new));
		trainerList.select(trainerList.getItemCount() - 1);
		trainerList.showSelection();
	}

	public static void addEnergyInfo(String outp)
	{
		energyMsgs.add(outp);
		PokegearWindow.energyList.setItems(energyMsgs.toArray(String[]::new));
		energyList.select(energyList.getItemCount() - 1);
		energyList.showSelection();
	}

	// Send pre-parsing data to TabletopParser, start execution, and listen for changes to UI
	private void forwardToParser(String name, String defPath, String decklist) throws Exception
	{
		// Reset the lists for debug output and card categories
		debugMessages = new ArrayList<String>();
		pokemonMsgs = new ArrayList<String>();
		trainerMsgs = new ArrayList<String>();
		energyMsgs = new ArrayList<String>();
		errorMessages = new ArrayList<String>();

		// Re-create the TabItem for the Import Progress screen
		initLoadingScreen();
		composite_3.setEnabled(true);
		composite_3.setVisible(true);
		importProgress.setControl(composite_3);
		cardCount = 0;
		cardCounter.setText("  Cards Imported: " + PokegearWindow.cardCount);
		cardCounter.redraw();
		tabFolder.setSelection(importProgress);

		// Set the execution boolean and start the parsing job SwingWorker in TabletopParser
		TabletopParser.execfinished = false;
		TabletopParser.parse(decklist, defPath, name, true, progressBar, guiDeckList, shlPokegearDeckImporter.getDisplay());

		while (!TabletopParser.runComplete())
		{
			Display.getDefault().readAndDispatch();
		}

		// Call the parser's finalization code once its worker threads are done
		tabFolder.setSelection(0);
		shlPokegearDeckImporter.setActive();
		importProgress.dispose();

		// Open a dialog to let user know the deck was imported
		String imgpath = "/images/pikachuohyeah.gif";
		String text = "PokeGear is done importing your deck!\nCheck the \'Import\' tab for any errors.\nIf none are present, your full deck can be found in Tabletop Simulator.";
		CompositeDialog importDone = new CompositeDialog(shlPokegearDeckImporter, SWT.ICON_INFORMATION | SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM, "Import Complete!", text, imgpath);
		importDone.open();
		importDone.loopOnce();
	}

	public boolean flushDispatchQueue()
	{
		return Display.getCurrent().readAndDispatch();
	}

	public static void incrementCounter(int count)
	{
		cardCount += count;
		cardCounter.setText("  Cards Imported: " + PokegearWindow.cardCount);
		cardCounter.redraw();
	}

	public static void terminate()
	{
		String[] errs = errorMessages.toArray(String[]::new);
		guiDeckList.setItems(errs);
		tabFolder.setSelection(0);
		importProgress.dispose();
	}
}