package andrielgaming.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.graphics.Point;

public class PokegearWindow
{

	protected Shell shlPokegearDeckImporter;
	private Label lbl_helpTab;
	private Text txtDecklistsCanBe;
	private Text text;
	// protected Font fireRed = new Font(new GC(new Shell(new Display())).getDevice(), new FontData("/fonts/pokemon_fire_red.ttf"));

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
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
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
		shlPokegearDeckImporter.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/background.jpg"));
		shlPokegearDeckImporter.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		shlPokegearDeckImporter.setSize(1228, 853);
		shlPokegearDeckImporter.setText("Pokegear Deck Importer");
		GridLayout gl_shlPokegearDeckImporter = new GridLayout(1, false);
		gl_shlPokegearDeckImporter.marginTop = 25;
		gl_shlPokegearDeckImporter.marginBottom = 25;
		gl_shlPokegearDeckImporter.marginRight = 25;
		gl_shlPokegearDeckImporter.marginLeft = 25;
		shlPokegearDeckImporter.setLayout(gl_shlPokegearDeckImporter);

		CLabel lblNewLabel = new CLabel(shlPokegearDeckImporter, SWT.BORDER | SWT.SHADOW_IN | SWT.SHADOW_OUT | SWT.SHADOW_NONE);
		lblNewLabel.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/pokegearicon.png"));
		lblNewLabel.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 36, SWT.BOLD));
		lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblNewLabel.setText("  Pokegear Deck Importer for Tabletop Simulator   ");

		new Label(shlPokegearDeckImporter, SWT.NONE);

		TabFolder tabFolder = new TabFolder(shlPokegearDeckImporter, SWT.BORDER);
		tabFolder.setVisible(true);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabFolder.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 16, SWT.NORMAL));

		TabItem tab_import = new TabItem(tabFolder, SWT.NONE);
		tab_import.setToolTipText("The main tool, import your decks here.");
		tab_import.setText("Import A Deck");

		TabItem tab_options = new TabItem(tabFolder, SWT.NONE);
		tab_options.setToolTipText("Configure Pokegear, add filepath, change settings");
		tab_options.setText("Options");

		TabItem tab_help = new TabItem(tabFolder, SWT.NONE);
		tab_help.setToolTipText("How to use Pokegear and important information");
		tab_help.setText("Help");

		Composite composite = new Composite(tabFolder, SWT.V_SCROLL | SWT.EMBEDDED);
		composite.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));
		tab_help.setControl(composite);
		composite.setLayout(new GridLayout(1, false));

		lbl_helpTab = new Label(composite, SWT.NONE);
		lbl_helpTab.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lbl_helpTab.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		lbl_helpTab.setBackground(SWTResourceManager.getColor(0, 0, 51));
		lbl_helpTab.setImage(null);
		lbl_helpTab.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 32, SWT.BOLD));
		lbl_helpTab.setText("   Guides and Important Info");

		ExpandBar expandBar = new ExpandBar(composite, SWT.NONE);
		expandBar.setBackgroundMode(SWT.INHERIT_FORCE);
		expandBar.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		expandBar.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		expandBar.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 22, SWT.NORMAL));
		GridData gd_expandBar = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_expandBar.widthHint = 1137;
		expandBar.setLayoutData(gd_expandBar);

		ExpandItem xpndtmNewExpanditem = new ExpandItem(expandBar, SWT.NONE);
		xpndtmNewExpanditem.setExpanded(false);
		xpndtmNewExpanditem.setText("How do I import a deck?");

		txtDecklistsCanBe = new Text(expandBar, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		txtDecklistsCanBe.setDragDetect(false);
		txtDecklistsCanBe.setDoubleClickEnabled(false);
		txtDecklistsCanBe.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));
		txtDecklistsCanBe.setText("You will need a decklist in text form, your file path set in \"Options\", and a name for the deck. Decklists can be copied to your clipboard and pasted into the text field found in the \"Import\" tab at the top left. If your file path is set and you've given the deck a name, all you need to do is click \"GO\".");
		txtDecklistsCanBe.setEditable(false);
		xpndtmNewExpanditem.setControl(txtDecklistsCanBe);
		xpndtmNewExpanditem.setHeight(50);

		ExpandItem xpndtmNewExpanditem_1 = new ExpandItem(expandBar, SWT.NONE);
		xpndtmNewExpanditem_1.setExpanded(false);
		xpndtmNewExpanditem_1.setText("What deck list formats will work with Pokegear?");

		Group group = new Group(expandBar, SWT.NONE);
		group.setVisible(true);
		group.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));
		group.setBackgroundMode(SWT.INHERIT_FORCE);
		group.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		group.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		group.setSize(new Point(0, 128));
		xpndtmNewExpanditem_1.setControl(group);
		group.setLayout(new GridLayout(2, false));

		CLabel lblNewLabel_1 = new CLabel(group, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 1));
		lblNewLabel_1.setImage(SWTResourceManager.getImage(PokegearWindow.class, "/images/deckguide_help.png"));
		lblNewLabel_1.setText("");

		text = new Text(group, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		text.setText("You will need a decklist in text form, your file path set in \"Options\", and a name for the deck. Decklists can be copied to your clipboard and pasted into the text field found in the \"Import\" tab at the top left. If your file path is set and you've given the deck a name, all you need to do is click \"GO\".");
		text.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.NORMAL));
		text.setEditable(false);
		text.setDragDetect(false);
		text.setDoubleClickEnabled(false);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		xpndtmNewExpanditem_1.setHeight(88);

		ExpandItem xpndtmNewExpanditem_2 = new ExpandItem(expandBar, SWT.NONE);
		xpndtmNewExpanditem_2.setText("Where can I get a deck list?");

		ExpandItem xpndtmNewExpanditem_3 = new ExpandItem(expandBar, SWT.NONE);
		xpndtmNewExpanditem_3.setText("How do I use my new deck in Tabletop Simulator?");
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
}
