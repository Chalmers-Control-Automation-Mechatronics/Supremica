package org.supremica.external.processAlgebraPetriNet.gui;


import java.awt.*;
import javax.swing.*;
import org.w3c.dom.Document;
import javax.swing.border.*;

import org.supremica.external.processAlgebraPetriNet.algorithms.*;

public class XMLTree  extends JPanel {

    static Document document;
    static final int windowHeight = 460;
    static final int leftWidth = 300;
    static final int rightWidth = 340;
    static final int windowWidth = leftWidth + rightWidth;

    public XMLTree() {

		EmptyBorder eb = new EmptyBorder(5,5,5,5);
		BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
		CompoundBorder cb = new CompoundBorder(eb,bb);
		this.setBorder(new CompoundBorder(cb,eb));

		// Create a tree
		JTree tree = new JTree();

		// Tree view
		JScrollPane treeView = new JScrollPane(tree);
		treeView.setPreferredSize( new Dimension( leftWidth, windowHeight ));

		// Add GUI components
		this.setLayout(new BorderLayout());
		this.add("Center", treeView );
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public XMLTree(Document doc) {

		document = doc;

		EmptyBorder eb = new EmptyBorder(5,5,5,5);
		BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
		CompoundBorder cb = new CompoundBorder(eb,bb);
		this.setBorder(new CompoundBorder(cb,eb));

		Dom dom = new Dom(document);

		//Create the tree
		JTree tree = new JTree(dom);

		// Tree view
		JScrollPane treeView = new JScrollPane(tree);
		treeView.setPreferredSize( new Dimension( leftWidth, windowHeight ));

		// Add GUI components
		this.setLayout(new BorderLayout());
		this.add("Center", treeView );
	}
}




