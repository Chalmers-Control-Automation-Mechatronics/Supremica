//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorWindow
//###########################################################################
//# $Id: EditorWindow.java,v 1.3 2005-02-17 19:59:55 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.module.IdentifiedElementProxy;
import net.sourceforge.waters.model.base.ProxyMarshaller;
import net.sourceforge.waters.model.module.ModuleMarshaller;
import net.sourceforge.waters.model.module.*;
import java.util.ArrayList;
import net.sourceforge.waters.model.expr.IdentifierProxy;

class EditorWindow extends JFrame {
    private EditorToolbar toolbar;
    private ControlledSurface surface;
    private EditorEvents events;
    private EditorMenu menu;
    private SimpleComponentProxy element = null;
    private ModuleProxy module = null;
    private boolean isSaved = false;
    
    public EditorWindow(String title, ModuleProxy module, SimpleComponentProxy element)
    {
        JFrame.setDefaultLookAndFeelDecorated(true);
	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle(title);

	toolbar = new EditorToolbar();

	surface = new ControlledSurface(toolbar, this);
        surface.setPreferredSize(new Dimension(500,500));
        surface.setMinimumSize(new Dimension(0,0));

        events = new EditorEvents(module, element, this);
        menu = new EditorMenu(surface, this);

	final Container panel = getContentPane();
	final GridBagLayout gridbag = new GridBagLayout();
	final GridBagConstraints constraints = new GridBagConstraints();
	constraints.gridy = 0;
	constraints.weighty = 1.0;
	constraints.anchor = GridBagConstraints.NORTH;
	panel.setLayout(gridbag);

	gridbag.setConstraints(toolbar, constraints);
	panel.add(toolbar);

	final JScrollPane scrollsurface = new JScrollPane(surface);

	final JScrollPane scrollevents = new JScrollPane(events);

	final JSplitPane split =
	  new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		         scrollsurface, scrollevents);
	split.setResizeWeight(1.0);
	constraints.weightx = 1.0;
	constraints.fill = GridBagConstraints.BOTH;
	gridbag.setConstraints(split, constraints);
	panel.add(split);
	
        setJMenuBar(menu);
        pack();
        setVisible(true);

        this.module = module;
        this.element = element;

        if(element != null && module != null) {
            surface.loadElement(module, element);
        }
	surface.createOptions(this);
    }

    public IdentifierProxy getBuffer(){
	return events.getBuffer();
    }

    public void setBuffer(IdentifierProxy i){
	events.setBuffer(i);
    }

    public boolean isSaved() {
	return isSaved;
    }

    public void setSaved(boolean s) {
	isSaved = s;
    }

    public java.util.List getEventDeclList(){
	return module.getEventDeclList();
    }

    public JFrame getFrame()
    {
	return (JFrame)this;
    }

    public ControlledSurface getControlledSurface()
    {
	return surface;
    }


}

    
