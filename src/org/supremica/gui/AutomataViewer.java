
/******************** AutomataViewer.java *************/
package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;
import org.supremica.log.*;
import org.supremica.gui.treeview.*;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;

class AutomataViewerPanel
	extends JPanel
// implements AutomatonListener
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.createLogger(AutomataViewerPanel.class);
	private Automata automata;
	private JTree theTree = new JTree();
	private JScrollPane scrollPanel = new JScrollPane(theTree);

	public AutomataViewerPanel(Automata automata, boolean showalpha, boolean showstates)
	{
		this.automata = automata;

		setLayout(new BorderLayout());
		add(scrollPanel, BorderLayout.CENTER);
		build(showalpha, showstates);
		theTree.setCellRenderer(new SupremicaTreeCellRenderer());    // EventNodeRenderer());
	}

	private AutomataViewerPanel(Automata automata)
	{
		this(automata, true, true);
	}

	public void build(boolean showalpha, boolean showstates)
	{
		SupremicaTreeNode root = new SupremicaTreeNode();
		Iterator autit = automata.iterator();

		while (autit.hasNext())
		{
			root.add(new AutomatonSubTree((Automaton) autit.next(), showalpha, showstates));
		}

		DefaultTreeModel treeModel = new DefaultTreeModel(root);

		theTree.setModel(treeModel);
		theTree.setRootVisible(false);
		theTree.setShowsRootHandles(true);

		// theTree.setExpanded(new TreePath(node));
		revalidate();
	}

	/*
	public void setVisible(boolean toVisible)
	{
		super.setVisible(toVisible);
	}
	*/
}

public class AutomataViewer
	extends JFrame
{
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JMenuBar menuBar = new JMenuBar();
	private AutomataViewerPanel viewerPanel;

	public AutomataViewer(Automata automata)
	{
		this(automata, true, true);
	}

	public AutomataViewer(Automata automata, boolean showalpha, boolean showstates)
	{
		setTitle("Automata Viewer");
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				setVisible(false);
			}
		});

		/* Center the window
		setSize(200, 500);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();

		if (frameSize.height > screenSize.height)
		{
				frameSize.height = screenSize.height;
		}

		if (frameSize.width > screenSize.width)
		{
				frameSize.width = screenSize.width;
		}

		setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		setIconImage(Supremica.cornerImage);*/
		Utility.setupFrame(this, 200, 500);
		initMenubar();

		this.viewerPanel = new AutomataViewerPanel(automata, showalpha, showstates);
		contentPane = (JPanel) getContentPane();

		contentPane.add(viewerPanel, BorderLayout.CENTER);
	}

	private void initMenubar()
	{
		setJMenuBar(menuBar);

		// File.Close
		JMenuItem menuFileClose = new JMenuItem();
		menuFileClose.setText("Close");
		menuFileClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);

				//dispose();
			}
		});

		// File menu
		JMenu menuFile = new JMenu();
		menuFile.setText("File");
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuFile.add(menuFileClose);
		menuBar.add(menuFile);

		/*
		// View.Union (default, therefore initially checked)
		JRadioButtonMenuItem viewMenuUnion = new JRadioButtonMenuItem("Union", true);
		viewMenuUnion.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {}
		});

		// View.Intersection
		JRadioButtonMenuItem viewMenuIntersection = new JRadioButtonMenuItem("Intersection");
		viewMenuIntersection.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {}
		});

		// Radio button functionality?
		ButtonGroup buttongroup = new ButtonGroup();
		buttongroup.add(viewMenuUnion);
		buttongroup.add(viewMenuIntersection);

		// View menu
		JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic(KeyEvent.VK_V);
		viewMenu.add(viewMenuUnion);
		viewMenu.add(viewMenuIntersection);
		menuBar.add(viewMenu);
		*/
	}

	public void initialize() {}
}
