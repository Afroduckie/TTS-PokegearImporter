package andrielgaming.ui;

import static java.lang.System.out;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import andrielgaming.Pokegear;
import andrielgaming.parsing.TabletopParser;

public class PokegearWindow
{
	private static Thread parse;
	protected Shell shlPokegearDeckImporter;
	private Label lbl_helpTab;
	private Text txtThisProgramWas;
	private Text txtIfYouAre;
	private Text txtForTheDecks;
	private Text txtAsdf;
	private Text text;
	private static ProgressBar progressBar;
	public static List guiDeckList;
	// protected Font fireRed = new Font(new GC(new Shell(new Display())).getDevice(), new FontData("/fonts/pokemon_fire_red"));

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args)
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
		Display display = Display.getDefault();
		createContents();
		shlPokegearDeckImporter.open();
		shlPokegearDeckImporter.layout();
		while (!shlPokegearDeckImporter.isDisposed())
			if (!display.readAndDispatch()) display.sleep();
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents()
	{
		shlPokegearDeckImporter = new Shell();
		shlPokegearDeckImporter.setMaximized(true);
		shlPokegearDeckImporter.setBackgroundImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/background.jpg"));
		shlPokegearDeckImporter.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		shlPokegearDeckImporter.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/pokegearicon.png"));
		shlPokegearDeckImporter.setBackground(SWTResourceManager.getColor(0, 0, 153));
		shlPokegearDeckImporter.setSize(1228, 853);
		shlPokegearDeckImporter.setText("Pokegear Deck Importer");
		GridLayout gl_shlPokegearDeckImporter = new GridLayout(1, false);
		gl_shlPokegearDeckImporter.marginTop = 25;
		gl_shlPokegearDeckImporter.marginBottom = 25;
		gl_shlPokegearDeckImporter.marginRight = 25;
		gl_shlPokegearDeckImporter.marginLeft = 25;
		shlPokegearDeckImporter.setLayout(gl_shlPokegearDeckImporter);

		CLabel lblNewLabel = new CLabel(shlPokegearDeckImporter, SWT.NONE);
		lblNewLabel.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/dancingpikachu_resized.gif"));
		lblNewLabel.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 30, SWT.BOLD));
		lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblNewLabel.setText("  Pokegear Deck Importer for Tabletop Simulator   ");

		new Label(shlPokegearDeckImporter, SWT.NONE);

		TabFolder tabFolder = new TabFolder(shlPokegearDeckImporter, SWT.NONE);
		tabFolder.setBackground(SWTResourceManager.getColor(0, 0, 51));
		tabFolder.setVisible(true);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
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
		new Label(composite_1, SWT.NONE);

		text = new Text(composite_1, SWT.BORDER);
		text.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 12, SWT.NORMAL));
		GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
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

		// Loading bar- indexed to N/60 cards with URLs parsed
		progressBar = new ProgressBar(composite_1, SWT.SMOOTH);
		progressBar.setVisible(false);
		progressBar.setMaximum(61);
		progressBar.setSelection(0);
		GridData gd_progressBar = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd_progressBar.widthHint = 300;
		progressBar.setLayoutData(gd_progressBar);

		CLabel label = new CLabel(composite_1, SWT.NONE);
		label.setImage(null);
		label.setText("");

		Button btnClearAll = new Button(composite_1, SWT.NONE);
		btnClearAll.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 24, SWT.BOLD));
		GridData gd_btnClearAll = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_btnClearAll.heightHint = 42;
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

		Button btnPasteFromClipboard = new Button(composite_1, SWT.NONE);
		btnPasteFromClipboard.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 24, SWT.BOLD));
		GridData gd_btnPasteFromClipboard = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
		gd_btnPasteFromClipboard.widthHint = 263;
		gd_btnPasteFromClipboard.heightHint = 42;
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

		Button btnNewButton = new Button(composite_1, SWT.NONE);
		GridData gd_btnNewButton = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
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
				forwardToParser(name, defPath, decklist);
			}
		});
		btnNewButton.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 24, SWT.BOLD));
		btnNewButton.setText("Import Deck");

		TabItem tab_options = new TabItem(tabFolder, SWT.NONE);
		tab_options.setToolTipText("Configure Pokegear, add filepath, change settings");
		tab_options.setText("Options");

		Composite composite_2 = new Composite(tabFolder, SWT.NONE);
		composite_2.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		tab_options.setControl(composite_2);
		composite_2.setLayout(new GridLayout(2, false));

		CLabel lblLocationOfYour = new CLabel(composite_2, SWT.NONE);
		lblLocationOfYour.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));
		lblLocationOfYour.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		lblLocationOfYour.setText("Location of your TTS \"Saved Objects\" folder:");

		txtAsdf = new Text(composite_2, SWT.BORDER);
		txtAsdf.setText(Pokegear.getPath());
		txtAsdf.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

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
		Image deckExample = SWTResourceManager.getImage(PokegearWindow.class, "/images/helpitem_decklist.png");
		ImageData scaledDeckExample = deckExample.getImageData();
		scaledDeckExample.scaledTo(80, 80);
	}

	public Image getLbl_helpTabImage()
	{
		return lbl_helpTab.getImage();
	}

	public void setLbl_helpTabImage(Image image)
	{
		lbl_helpTab.setImage(image);
	}

	// Send data from text and name fields to parser class for processing
	private void forwardToParser(String name, String defPath, String decklist)
	{
		parse = new Thread(new TabletopParser());
		progressBar.setVisible(true);
		out.println("Paste your deck list into this text file, save it, and close it.");
		try
		{
			out.println("Pokegear will now attempt to parse this decklist! \n NOTE:: Please be patient! As a courtesy, there is a pre-programmed cushion to prevent the program from hugging the server to death.");
			// TabletopParser.setPath(txtAsdf.getText());
			TabletopParser.resetDeck();
			TabletopParser.setParseVars(decklist, defPath, name, true, progressBar, guiDeckList);
			Display.getDefault().syncExec(parse);
		} catch (Exception e)
		{
			out.println("Oopsie poopsie doopsie I did a fucky wucky, sorry about that! I committed the following war-crime:: " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
