package andrielgaming.ui.panels;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

import andrielgaming.ui.PokegearWindow;

/**		
 * @author - Andrew Kluttz, 2022
 * Child widget of CLabel in Eclipse SWT designed to hold an animated GIF and support drawing said animation
 *
 */
public class AnimatedCanvas extends Canvas
{
	public Composite parent;
	public int style;
	private static final int DEFAULT_MARGIN = 3;
	private int align = SWT.CENTER;
	private int leftMargin = DEFAULT_MARGIN;
	private int topMargin = DEFAULT_MARGIN;
	private int rightMargin = DEFAULT_MARGIN;
	private int bottomMargin = DEFAULT_MARGIN;
	private Image image;
	private String imageClassPath;
	private boolean ignoreDispose;
	// Set the background color and whether the animation engine should redraw background for GIFs with transparency
	private Color background;
	private boolean redrawBg;
	// Boolean if image imported should be automatically resized to fit provided parent composite dimensions
	private boolean autoResize;
	// Operative variables- NOT to be customized in WindowBuilder
	public GC gc;
	public ImageLoader loader;
	public int imageNumber = 0;
	// Integer value denoting the approximate framerate GIF should be drawn at
	public int frameTimer;
	private static int DRAW_FLAGS = SWT.DRAW_MNEMONIC | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER;
	public int gifx,
			gify = 100;
	private boolean runOnce = false;
	private boolean runDone = false;

	// Generic constructor, takes basic reqs for superclass
	public AnimatedCanvas(Composite parent, int style)
	{
		super(parent, style);
		this.parent = parent;
		this.style = style;
		parent.setBackground(new Color(255, 255, 255));

		addPaintListener(new PaintListener()
		{
			public void paintControl(PaintEvent event)
			{
				paintAnimations(event);
			}
		});
	}

	void paintAnimations(PaintEvent event)
	{
		if ((runOnce && !runDone) || !runOnce)
		{
			gc = new GC(this);
			parent.update();
			imageNumber = imageNumber == loader.data.length - 1 ? 0 : imageNumber + 1;
			ImageData nextFrameData = loader.data[imageNumber];
			gc.setBackground(new Color(255, 255, 255));
			Image frameImage = new Image(Display.getCurrent(), nextFrameData);
			gc.drawImage(frameImage, nextFrameData.x, nextFrameData.y);
			frameImage.dispose();

			// NOTE -- Must remain in async call or it will soft-block UI thread and cause stutters
			parent.getDisplay().asyncExec(() ->
			{
				this.redraw();
				try
				{
					Thread.sleep(frameTimer);
				} catch (InterruptedException e)
				{
				}
			});
			if (imageNumber == loader.data.length - 1)
			{
				runDone = true;
			}
		}
		else
		{
			ImageData nextFrameData = loader.data[loader.data.length - 1];
			Image frameImage = new Image(Display.getCurrent(), nextFrameData);
			gc.drawImage(frameImage, nextFrameData.x, nextFrameData.y);
			frameImage.dispose();
		}
	}

	public void setSingleLoop()
	{
		runOnce = true;
		runDone = false;
	}

	public void setScale(int x, int y)
	{
		gifx = x;
		gify = y;
	}

	public void setImageClasspath(String imgc)
	{
		this.imageClassPath = imgc;
		this.setImage(SWTResourceManager.getImage(AnimatedCanvas.class, imageClassPath));
	}

	// Set animated GIF to be rendered on this panel
	public void setImage(Image image)
	{
		super.checkWidget();
		if (image != null)
		{
			this.image = image;
		}

		// Initialize the image
		loader = new ImageLoader();
		loader.load(image.getClass().getResourceAsStream(imageClassPath));
		Image[] images = new Image[loader.data.length];

		// Load frames into loader to calculate frame count
		for (int i = 0; i < loader.data.length; ++i)
		{
			ImageData nextFrameData = loader.data[i];
			nextFrameData = nextFrameData.scaledTo(gifx, gify);
			images[i] = new Image(Display.getDefault(), nextFrameData);
		}

		gc = new GC(this);
		gc.setAdvanced(true);
		redraw();
	}

}
