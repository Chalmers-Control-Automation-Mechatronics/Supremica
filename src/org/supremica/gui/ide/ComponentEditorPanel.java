package org.supremica.gui.ide;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.io.File;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.model.expr.IdentifierProxy;
import org.supremica.gui.GraphicsToClipboard;
import net.sourceforge.waters.gui.EditorToolbar;
import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.ControlledToolbar;
import net.sourceforge.waters.gui.EditorMenu;
import net.sourceforge.waters.gui.EditorEvents;
import net.sourceforge.waters.gui.EditorWindowInterface;



public class ComponentEditorPanel
	extends JPanel
	implements EditorWindowInterface
{
	private static final long serialVersionUID = 1L;

	private ModuleContainer moduleContainer;
	private EditorToolbar toolbar;
	private ControlledSurface surface;
	private EditorEvents events;
	private EditorMenu menu;
	private SimpleComponentProxy element = null;
	private ModuleProxy module = null;
	private boolean isSaved = false;
	private GraphicsToClipboard toClipboard = null;

	public ComponentEditorPanel(ModuleContainer moduleContainer, SimpleComponentProxy element)
	{
		//setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//setTitle(title);
		this.moduleContainer = moduleContainer;
		this.module = moduleContainer.getModuleProxy();
		toolbar = new EditorToolbar();
		if (moduleContainer.getIDE().getToolBar() instanceof ControlledToolbar) {
		    surface = new ControlledSurface(this, (ControlledToolbar)moduleContainer.getIDE().getToolBar());
		} else {
		    surface = new ControlledSurface(this, toolbar);
		}
		
		surface.setPreferredSize(IDEDimensions.rightEditorPreferredSize);
		surface.setMinimumSize(IDEDimensions.rightEditorMinimumSize);

		events = new EditorEvents(module, element);
		menu = new EditorMenu(surface, this);

		// final Container panel = getContentPane();
		final GridBagLayout gridbag = new GridBagLayout();
		final GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridy = 0;
		constraints.weighty = 1.0;
		constraints.anchor = GridBagConstraints.NORTH;

		setLayout(gridbag);
		//gridbag.setConstraints(toolbar, constraints);
		//add(toolbar);

		final JScrollPane scrollsurface = new JScrollPane(surface);
		final JScrollPane scrollevents = new JScrollPane(events);
		final JViewport viewevents = scrollevents.getViewport();
		final JSplitPane split = new JSplitPane
			(JSplitPane.HORIZONTAL_SPLIT, scrollevents, scrollsurface);
		viewevents.setBackground(Color.WHITE);

		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.BOTH;

		gridbag.setConstraints(split, constraints);
		add(split);

		//		split.setUI(new newUI());
		if (events.calculateWidth1() > moduleContainer.getEditorPanel().getRightComponent().getWidth()/2) {
		    split.setDividerLocation((int)moduleContainer.getEditorPanel().getRightComponent().getWidth()/2);
		} else {
		    split.setDividerLocation(events.calculateWidth1());
		}
		System.out.println(split.getDividerLocation());
		System.out.println("MAX :" + split.getMaximumDividerLocation());
		System.out.println("PREF :" + events.getPreferredSize().getWidth());
		System.out.println("WIDTH :" + moduleContainer.getEditorPanel().getRightComponent().getWidth());
		//setJMenuBar(menu);
//		pack();
		setVisible(true);

		this.element = element;

		if ((element != null) && (module != null))
		{
			surface.loadElement(module, element);
		}

		surface.createOptions(this);
	}

	public IdentifierProxy getBuffer()
	{
		return events.getBuffer();
	}

	public void setBuffer(IdentifierProxy i)
	{
		events.setBuffer(i);
	}

	public boolean isSaved()
	{
		return isSaved;
	}

	public void setSaved(boolean s)
	{
		isSaved = s;
	}

	public java.util.List getEventDeclList()
	{
		return module.getEventDeclList();
	}

	public JFrame getFrame()
	{
		return moduleContainer.getFrame();
	}

	public ControlledSurface getControlledSurface()
	{
		return surface;
	}

	public EditorEvents getEventPane()
	{
		return events;
	}

	public void copyAsWMFToClipboard()
	{
		if (toClipboard == null)
		{
			toClipboard = GraphicsToClipboard.getInstance();
		}

		//Rectangle2D bb = surface.getBoundingBox();
		//double minX = bb.getMinX();
		//double maxX = bb.getMaxX();
		//double minY = bb.getMinY();
		//double maxY = bb.getMaxY();
		//logger.debug("minX: " + minX + " maxX: " + maxX + " minY: " + minY + " maxY: " + maxY);
		//create a WMF object
		//int width = (int)(maxX - minX) + 1;
		//int height = (int)(maxY - minY) + 1;
		// Copy a larger area, approx 10 percent, there seems to be
		// a problem with the size of wmf-data
		//width += (int)0.1*width;
		//height += (int)0.1*height;
		Graphics theGraphics = toClipboard.getGraphics(surface.getWidth(), surface.getHeight());

		surface.print(theGraphics);
		toClipboard.copyToClipboard();
	}

	public void createPDF(File f)
	{

	}

    private class newUI extends BasicSplitPaneUI
    {
	public int getMinimumDividerLocation(JSplitPane jc)
	{
	    return 20000;
	}
    }
}
