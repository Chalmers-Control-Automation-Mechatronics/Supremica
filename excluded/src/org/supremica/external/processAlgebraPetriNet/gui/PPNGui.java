package org.supremica.external.processAlgebraPetriNet.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.w3c.dom.Document;

public class PPNGui extends JFrame {
    String newline = "\n";

    XMLTree tree;

    JPanel treepanel, ppntreepanel, automatatreepanel, pntreepanel;

    Dimension d = new Dimension(200, 200);

    JSplitPane splitPane, splitPane2, splitPane3;
    Document document;

    Container contentPane = getContentPane();

    public PPNGui() {

	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void CreatePPNGui() {
        JMenuBar menuBar;
        JMenu menu, submenu;
        JMenuItem menuItem;

		addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

		try {
		UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		}
        catch (Exception e) { }

		//BorderLayout.
        contentPane.setLayout(new BorderLayout());

        //Create an instance of treepanel
		treepanel = new JPanel();
		treepanel.setMinimumSize(d);
		treepanel.setLayout(new BorderLayout());
		JLabel l = new JLabel("             STEP MODEL");
		treepanel.add(l, BorderLayout.NORTH);

        //Create an instance of treepanel
		ppntreepanel = new JPanel();
		ppntreepanel.setMinimumSize(d);
		ppntreepanel.setLayout(new BorderLayout());
		JLabel la = new JLabel("             PPN MODEL (PA)");
		ppntreepanel.add(la, BorderLayout.NORTH);

        //Create an instance of treepanel
		automatatreepanel = new JPanel();
		automatatreepanel.setMinimumSize(d);
		automatatreepanel.setLayout(new BorderLayout());
		JLabel lab = new JLabel("             AUTOMATA MODEL");
		automatatreepanel.add(lab, BorderLayout.NORTH);

        //Create an instance of treepanel
		pntreepanel = new JPanel();
		pntreepanel.setMinimumSize(d);
		pntreepanel.setLayout(new BorderLayout());
		JLabel labe = new JLabel("             PPN MODEL (PN)");
		pntreepanel.add(labe, BorderLayout.NORTH);

		//Create the toolbar.
		ToolBar T = new ToolBar();
		T.sattRef(this);

		//Add tools to north
        contentPane.add(T, BorderLayout.NORTH);

        //Add splitpane and splitpane2 in to a new splitpane.
 		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treepanel, ppntreepanel);
 		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(300);

        //Add splitpane and splitpane2 in to a new splitpane.
 		splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, automatatreepanel, pntreepanel);
 		splitPane2.setOneTouchExpandable(true);
		splitPane2.setDividerLocation(300);

        //Add splitpane and splitpane2 in to a new splitpane.
 		splitPane3 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPane, splitPane2);
 		splitPane3.setOneTouchExpandable(true);
		splitPane3.setDividerLocation(600);

		contentPane.add(splitPane3, BorderLayout.CENTER);

        //Create the menu bar.
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
    }


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void UppdateraTree(XMLTree tree) {
			treepanel.removeAll();
			JLabel l = new JLabel("             STEP MODEL");
			treepanel.add(l, BorderLayout.NORTH);
			treepanel.add(tree, BorderLayout.CENTER);
			splitPane.setDividerLocation(300);
	}

	public void UppdateraPPNTree(XMLTree tree) {
			ppntreepanel.removeAll();
			JLabel la = new JLabel("             PPN MODEL (PA)");
			ppntreepanel.add(la, BorderLayout.NORTH);
			ppntreepanel.add(tree, BorderLayout.CENTER);
			splitPane.setDividerLocation(300);
	}

	public void UppdateraAutomataTree(XMLTree tree) {
			automatatreepanel.removeAll();
			JLabel lab = new JLabel("             AUTOMATA MODEL");
			automatatreepanel.add(lab, BorderLayout.NORTH);
			automatatreepanel.add(tree, BorderLayout.CENTER);
			splitPane2.setDividerLocation(300);
	}

	public void UppdateraPNTree(XMLTree tree) {
			pntreepanel.removeAll();
			JLabel lab = new JLabel("             PPN MODEL (PN)");
			pntreepanel.add(lab, BorderLayout.NORTH);
			pntreepanel.add(tree, BorderLayout.CENTER);
			splitPane2.setDividerLocation(300);
	}

}