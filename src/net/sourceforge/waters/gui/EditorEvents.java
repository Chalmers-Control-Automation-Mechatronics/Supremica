//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorEvents
//###########################################################################
//# $Id: EditorEvents.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.base.*;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.model.expr.IdentifierProxy;

/** <p>The Events window which sits to the right of the editor window.</p>
 *
 * <p>This is used to view the module events which have been selected for use with this
 * particular component, and selecting other events from the module for use with this
 * component</p>
 *
 * @author Gian Perrone
 */
public class EditorEvents extends JPanel {
    ModuleProxy module;
    GraphProxy graph;
    EditorWindow root;
    IdentifierProxy buffer;
    JList dataList;
    DefaultListModel data;
	
    private JScrollPane createContentPane() {
	data = new DefaultListModel();
	dataList = new JList(data);
	dataList.setCellRenderer(new SimpleExpressionCell(module));
	Set set = new TreeSet();

	if(graph != null) {	  
	    for(int i = 0; i < graph.getEdges().size(); i++) {
		EdgeProxy e = (EdgeProxy)graph.getEdges().get(i);
		if (e.getLabelBlock() == null){
		    continue;
		}
                set.addAll(e.getLabelBlock());
	    }
	    Iterator i = graph.getNodes().iterator();
	    while (i.hasNext()){
		NodeProxy n = (NodeProxy)i.next();
		if (n.getPropositions() == null){
		    continue;
		}
		set.addAll(n.getPropositions());
	    }
	    i = set.iterator();
	    while(i.hasNext()){
		data.addElement(i.next());
	    }
	}
	
	JScrollPane scroll = new JScrollPane(dataList);
	dataList.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {		    
		    int index = dataList.locationToIndex(e.getPoint());
		    //		    JOptionPane.showMessageDialog(this, "This should launch the editor for an item");
		    if (index >= 0 && index < data.size()){
			buffer = (IdentifierProxy)data.elementAt(index);
		    }                    
                }

		public void mouseClicked(MouseEvent e){
		    System.out.println("adding event");
		    if (e.getClickCount() == 2) {
			int index = dataList.locationToIndex(e.getPoint());
			EditorLabelGroup l = root.getControlledSurface().getSelectedLabelGroup();
			if (l != null) {
			    l.addEvent((IdentifierProxy)data.elementAt(index));
			}
			EditorNode n = root.getControlledSurface().getSelectedNode();
			if (n != null) {
			    n.getPropGroup().addEvent((IdentifierProxy)data.elementAt(index));
			}
		    }
		}
            });
        //table.setMinimumSize(new Dimension(160, 600));
        //dataList.setPreferredScrollableViewportSize(new Dimension(160, 600));
        return scroll;
    }

    public void setBuffer(IdentifierProxy i){
	buffer = i;
    }

    public IdentifierProxy getBuffer(){
	return buffer;
    }

    public EditorEvents(ModuleProxy m, ElementProxy e, EditorWindow r) {
        module = m;
	root = r;
	if (e instanceof SimpleComponentProxy){
	    graph = ((SimpleComponentProxy)e).getGraph();
	}
	else{
	    graph = new GraphProxy();
	}
        this.setLayout(new BorderLayout());
        Box h = new Box(BoxLayout.PAGE_AXIS);
        h.setPreferredSize(new Dimension(160,600));
        h.add(createContentPane(), BorderLayout.NORTH);
        Box b = new Box(BoxLayout.LINE_AXIS);
        b.add(new JButton("Add"));
        b.add(new JButton("Remove"));
        
        this.add(h, BorderLayout.NORTH);
        this.add(b, BorderLayout.SOUTH);
    }
}
