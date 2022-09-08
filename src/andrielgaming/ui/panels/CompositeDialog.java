package andrielgaming.ui.panels;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import andrielgaming.parsing.TabletopParser;
import andrielgaming.ui.PokegearWindow;
import org.eclipse.swt.widgets.Button;

/**
 * 		@author Andrew Kluttz
 * 		@description
 *			A Dialog window containing a message and an AnimatedCanvas, meant to be a confirmation popup for my main program.
 * 			Current use is as a cutesy "parsing finished" notification.
 */
public class CompositeDialog extends Dialog
{
	private Shell parent;
	private Object result = null;
	private AnimatedCanvas gif;
	private boolean animated = true;
	private String label;
	private String text;
	private String imagePath;

	public CompositeDialog(Shell parent, int style, String label, String text, String imagePath)
	{
		super(parent, style);
		this.parent = parent;
		this.label = label;
		this.text = text;
		this.imagePath = imagePath;
	}

	public Object open()
	{
		Shell parent = getParent();
		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		shell.setText(label);
		shell.setLayout(new FormLayout());
		shell.setSize(749, 473);
		Display display = Display.getDefault();
		Rectangle screenSize = display.getPrimaryMonitor().getBounds();
		shell.setLocation((screenSize.width - shell.getBounds().width) / 2, (screenSize.height - shell.getBounds().height) / 2);

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		composite.setSize(200, 200);
		composite.setLayout(new GridLayout(2, false));
		FormData fd_composite = new FormData();
		fd_composite.top = new FormAttachment(0, 10);
		fd_composite.bottom = new FormAttachment(0, 475);
		fd_composite.right = new FormAttachment(0, 700);
		fd_composite.left = new FormAttachment(0, 10);
		composite.setLayoutData(fd_composite);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		gif = new AnimatedCanvas(composite, SWT.NONE);
		gif.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		gif.frameTimer = 45;
		GridData gd_animatedCanvas = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_animatedCanvas.heightHint = 315;
		gd_animatedCanvas.widthHint = 320;
		gif.setVisible(true);
		gif.setScale(80, 80);
		gif.setSingleLoop();
		gif.setLayoutData(gd_animatedCanvas);
		gif.setImageClasspath(imagePath);
		gif.setVisible(true);

		Label lblNewLabel = new Label(composite, SWT.WRAP | SWT.HORIZONTAL | SWT.CENTER);
		lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		lblNewLabel.setAlignment(SWT.CENTER);
		lblNewLabel.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 22, SWT.BOLD));
		GridData gd_lblNewLabel = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel.heightHint = 54;
		gd_lblNewLabel.widthHint = 360;
		lblNewLabel.setLayoutData(gd_lblNewLabel);
		lblNewLabel.setText(text);
		new Label(composite, SWT.NONE);

		Button backToMenu = new Button(composite, SWT.RIGHT);
		GridData gd_btnNewButton = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton.heightHint = 47;
		backToMenu.setLayoutData(gd_btnNewButton);
		backToMenu.setAlignment(SWT.CENTER);
		backToMenu.setFont(SWTResourceManager.getFont("Pokemon Fire Red", 18, SWT.BOLD));
		backToMenu.setText("Return to Menu");
		backToMenu.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				shell.close();
				shell.dispose();
				composite.dispose();
				gif.dispose();
			}
		});

		shell.open();
		display = parent.getDisplay();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return result;
	}

	public void loopOnce()
	{
		gif.setSingleLoop();
	}
}
