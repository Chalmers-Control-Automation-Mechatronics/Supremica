//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ModuleWindow
//###########################################################################
//# $Id: ModuleWindow.java,v 1.44 2006-05-08 20:17:50 flordal Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.xml.bind.JAXBException;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.UndoableCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.UndoRedoEvent;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.NamedSubject;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.InstanceSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.ParameterBindingSubject;
import net.sourceforge.waters.subject.module.ParameterSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;

import net.sourceforge.waters.subject.base.ListSubject;

//EFA-----------------
import net.sourceforge.waters.subject.module.*;

import net.sourceforge.waters.xsd.base.ComponentKind;

import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;

import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ForeachEventProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.EventParameterSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ParameterSubject;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.automata.Project;

import org.supremica.properties.Config;

/**
 * <p>The primary module-loading window.</p>
 *
 * <p>This is the main window of Waters, from which module files can be loaded
 * and with which components can be added/removed from modules.</p>
 *
 * @author Gian Perrone
 */
public class ModuleWindow
	extends JFrame
	implements ActionListener, FocusListener, UndoInterface, WindowListener
{
	/**
	 * Limits the functionality of Waters to just drawing simple components.
	 * Should be a property (will be a property in the IDE...).
	 */
	public static final boolean DES_COURSE_VERSION = false;

	/**
	 * Constructor.
	 */
	public ModuleWindow(String title)
	{
		// Don't close the window without doing some stuff first...
		//setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(this);

		setTitle(title);
		module = new ModuleSubject(title, null);
		debugPane = createDebugPane();

		final ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
		final OperatorTable optable = CompilerOperatorTable.getInstance();
		mExpressionParser = new ExpressionParser(factory, optable);
		mPrinter = new HTMLPrinter();

		constructWindow();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * Load a Waters module into the module viewer.
	 * @param wmodf The file to load the module from
	 */
	public void loadWmodFile(final File wmodf)
	{
		logEntry("Attempting to load: " + wmodf);
		try {
			final ModuleProxyFactory factory =
				ModuleSubjectFactory.getInstance();
			final OperatorTable optable = CompilerOperatorTable.getInstance();
			final ProxyUnmarshaller<ModuleProxy> unMarshaller =
				new JAXBModuleMarshaller(factory, optable);
			final URI uri = wmodf.toURI();
			module = (ModuleSubject) unMarshaller.unmarshal(uri);
			clearList();
		} catch (final JAXBException exception) {
			JOptionPane.showMessageDialog(this,
										  "Error loading module file:" +
										  exception.getMessage());
			logEntry("JAXBException - Failed to load  '" + wmodf + "'!");
		} catch (final WatersUnmarshalException exception) {
			JOptionPane.showMessageDialog(this,
										  "Error loading module file:" +
										  exception.getMessage());
			logEntry("WatersUnmarshalException - Failed to load  '" +
					 wmodf + "'!");
		} catch (final IOException exception) {
			JOptionPane.showMessageDialog(this,
										  "Error loading module file:" +
										  exception.getMessage());
			logEntry("IOException - Failed to load  '" + wmodf + "'!");
		}
	}

	public void saveWmodFile(File wmodf)
	{
		logEntry("Saving module to: " + wmodf);
		try	{
			final ModuleProxyFactory factory =
				ModuleSubjectFactory.getInstance();
			final OperatorTable optable = CompilerOperatorTable.getInstance();
			final ProxyMarshaller<ModuleProxy> marshaller =
				new JAXBModuleMarshaller(factory, optable);
			marshaller.marshal(module, wmodf);
		} catch (final JAXBException exception) {
			JOptionPane.showMessageDialog(this,
										  "Error saving module file:" +
										  exception.getMessage());
			logEntry("JAXBException - Failed to save  '" + wmodf + "'!");
		} catch (final WatersMarshalException exception) {
			JOptionPane.showMessageDialog(this,
										  "Error saving module file:" +
										  exception.getMessage());
			logEntry("WatersMarshalException - Failed to save  '" +
					 wmodf + "'!");
		} catch (final IOException exception) {
			JOptionPane.showMessageDialog(this,
										  "Error saving module file:" +
										  exception.getMessage());
			logEntry("IOException - Failed to save  '" + wmodf + "'!");
		}
	}

	/**
	 * Translates the current module into a Supremica project and open
	 * the project in Supremica.
	 */
	public void exportToSupremica()
	{
		//The project in Supremica format
		Project supremicaProject;
		try
		{
			ModuleSubject currModule = getModuleSubject();
			ProjectBuildFromWaters builder = new ProjectBuildFromWaters();
			supremicaProject = builder.build(currModule);
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error exporting to Supremica", 
										  JOptionPane.ERROR_MESSAGE); 
			ex.printStackTrace();
			return;
		}
			
		// Show Supremica window...
		if (supremica == null)
		{
			org.supremica.apps.Supremica dummySupremica =
				new org.supremica.apps.Supremica();
			supremica = org.supremica.apps.SupremicaWithGui.startSupremica();
			// Initializes some properties
			// Why doesn't this Supremica instance log stuff in the log-window?
		}
		else
		{
			supremica.setVisible(true);
		}

		// Don't kill the JVM when Supremica is closed! Just close the window...
		supremica.setDefaultCloseOperation(supremica.DISPOSE_ON_CLOSE);

		//supremica.
		System.out.println(supremicaProject.toString());
		int nbrOfAddedAutomata = supremica.addProject(supremicaProject);
		System.err.println(nbrOfAddedAutomata);

		//EFA question: 
		/*export everything to supremica 
		and translate to single automata there?*/
	}

	public JPanel createEventsPane()
	{
		final ArrayList l;

		data = new DefaultListModel();
		if (module != null)
		{
			for (final EventDeclProxy event : module.getEventDeclList()) 
			{
				data.addElement(event);
			}
		}

		dataList = new JList(data);
		dataList.setCellRenderer(new EventListCell());

		JButton newEventButton = new JButton("New Event");
		newEventButton.setActionCommand("newevent");
		newEventButton.addActionListener(this);

		JButton deleteEventButton = new JButton("Delete Event");
		deleteEventButton.setActionCommand("delevent");
		deleteEventButton.addActionListener(this);

		Box jp = new Box(BoxLayout.PAGE_AXIS);
		JPanel p = new JPanel();

		p.add(newEventButton);
		p.add(deleteEventButton);
		jp.add(new JScrollPane(dataList));
		jp.add(p);

		p = new JPanel();

		p.add(jp);
		p.setLayout(new GridLayout(1, 1));
		
		mDragSource = DragSource.getDefaultDragSource();
		mDGListener = new DGListener(dataList);
		mDSListener = new DSListener();
		
		// component, action, listener
		mDragSource.createDefaultDragGestureRecognizer(dataList,
													   mDragAction,
													   mDGListener);
		mDragSource.addDragSourceListener(mDSListener);

		return p;
	}

	public JPanel createParametersPane()
	{
		final ArrayList l;

		paramData = new DefaultListModel();

		if (module != null)
		{
			for (final ParameterProxy param : module.getParameterList())
			{
				paramData.addElement(param);
			}
		}
		paramdataList = new JList(paramData);

		paramdataList.setCellRenderer(new ParameterListCell());

		JButton newParamButton = new JButton("New Simple Parameter");

		newParamButton.setActionCommand("newparam");
		newParamButton.addActionListener(this);

		JButton deleteParamButton = new JButton("Delete Parameter");

		deleteParamButton.setActionCommand("delparam");
		deleteParamButton.addActionListener(this);

		JButton newEventParamButton = new JButton("New Event Parameter");

		newEventParamButton.setActionCommand("neweventparam");
		newEventParamButton.addActionListener(this);

		Box jp = new Box(BoxLayout.PAGE_AXIS);
		JPanel p = new JPanel();

		p.add(newParamButton);
		p.add(newEventParamButton);
		p.add(deleteParamButton);
		jp.add(new JScrollPane(paramdataList));
		jp.add(p);

		p = new JPanel();

		p.add(jp);
		p.setLayout(new GridLayout(1, 1));

		return p;
	}

	public DefaultListModel getEventDataList()
	{
		return data;
	}

	public DefaultListModel getParameterDataList()
	{
		return paramData;
	}

	public JPanel createDebugPane()
	{
		debugArea = new JTextArea("");

		debugArea.setText(debugText);
		debugArea.setEditable(false);
		debugArea.addFocusListener(this);

		JPanel jp = new JPanel();
		Box b = new Box(BoxLayout.PAGE_AXIS);

		b.add(new JScrollPane(debugArea));

		JPanel buttonPanel = new JPanel();

		b.add(buttonPanel);

		JButton b1 = new JButton("Save...");

		b1.addActionListener(this);
		b1.setActionCommand("SaveDebug");
		buttonPanel.add(b1);
		jp.add(b);
		jp.setLayout(new GridLayout(1, 1));
		logEntry("Logging started.");

		return jp;
	}

	public void logEntry(String entry)
	{
		Date d = new Date();

		debugText += ("(" + d.toString() + ") - " + entry + "\n");

		// TODO - Add logging to file
	}

	private DefaultMutableTreeNode makeTreeFromComponent(AbstractSubject e)
	{
		final String text = mPrinter.toString(e);
		final Object userobject = new ComponentInfo(text, e);
		if (e instanceof SimpleComponentSubject) {
			return new DefaultMutableTreeNode(userobject, false);
		} else if (e instanceof InstanceSubject) {
			final InstanceSubject inst = (InstanceSubject) e;
			final DefaultMutableTreeNode tmp =
				new DefaultMutableTreeNode(userobject, true);
			final List<ParameterBindingSubject> bindings =
				inst.getBindingListModifiable() ;
			for (final ParameterBindingSubject binding : bindings) {
				tmp.add(makeTreeFromComponent(binding));
			}
			return tmp;
		} else if (e instanceof ParameterBindingSubject) {
			return new DefaultMutableTreeNode(userobject, false);
		} else if (e instanceof ForeachSubject) {
			final ForeachSubject foreach = (ForeachSubject) e;
			final DefaultMutableTreeNode tn =
				new DefaultMutableTreeNode(userobject, true);
			final List<AbstractSubject> body = foreach.getBodyModifiable();
			for (final AbstractSubject item : body) {
				tn.add(makeTreeFromComponent(item));
			}
			return tn;
		} else {
			throw new IllegalArgumentException
				("Don't know how to make tree from subject of type " +
				 e.getClass().getName() + "!");
		}
	}

	public JPanel createContentPane()
	{
		final ArrayList l;
		DefaultMutableTreeNode treeNode = null;

		if (module != null)
		{
			l = new ArrayList(module.getComponentList());
			treeNode = new DefaultMutableTreeNode(new ComponentInfo("Module: " + module.getName(), null));
		}
		else
		{
			l = new ArrayList();
			treeNode = null;
		}

		for (int i = 0; i < l.size(); i++)
		{
			treeNode.add(makeTreeFromComponent((AbstractSubject) (l.get(i))));
		}

		rootNode = treeNode;
		treeModel = new DefaultTreeModel(rootNode);

		//TODO: Put some proper icons in place!
		final ImageIcon plantIcon = new ImageIcon(ModuleWindow.class.getResource("/icons/waters/plant.gif"));
		final ImageIcon specIcon = new ImageIcon(ModuleWindow.class.getResource("/icons/waters/spec.gif"));
		final ImageIcon propertyIcon = new ImageIcon(ModuleWindow.class.getResource("/icons/waters/property.gif"));
		final ImageIcon foreachIcon = null;
		final ImageIcon instanceIcon = new ImageIcon(ModuleWindow.class.getResource("/icons/waters/instance.gif"));
		final ImageIcon bindingIcon = null;

		moduleSelectTree = new JTree(treeModel);

		moduleSelectTree.setCellRenderer(new ModuleTreeRenderer(foreachIcon, plantIcon, propertyIcon, specIcon, instanceIcon, bindingIcon));
		moduleSelectTree.setEditable(false);
		moduleSelectTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

		//renderer.setLeafIcon(simpleIcon);
		//renderer.setOpenIcon(null);
		//renderer.setClosedIcon(null);
		//moduleSelectTree.setCellRenderer(renderer);
		MouseListener ml = new MouseAdapter()
		{
			EditorWindow ed = null;

			public void mousePressed(MouseEvent e)
			{
				int selRow = moduleSelectTree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = moduleSelectTree.getPathForLocation(e.getX(), e.getY());

				if (selRow != -1)
				{
					if (e.getClickCount() == 2)
					{
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) moduleSelectTree.getLastSelectedPathComponent();

						if (node == null)
						{
							return;
						}

						Object nodeInfo = node.getUserObject();
						AbstractSubject abstractComponent = (((ComponentInfo) nodeInfo).getComponent());
						if (abstractComponent instanceof SimpleComponentSubject)
						{
							SimpleComponentSubject scp = (SimpleComponentSubject) abstractComponent;

							if (scp != null)
							{
								ed = new EditorWindow(scp.getName() + " - Waters Editor", module, scp, ModuleWindow.this, ModuleWindow.this);
							}
						}
					}
				}
			}
		};

		moduleSelectTree.addMouseListener(ml);

		JPanel buttonpanel = new JPanel();

		JButton newSimpleButton = new JButton("New Simple Component");
		newSimpleButton.addActionListener(this);
		newSimpleButton.setActionCommand("newsimple");
		buttonpanel.add(newSimpleButton);

		JButton newForeachButton = new JButton("New Foreach Component");
		newForeachButton.addActionListener(this);
		newForeachButton.setActionCommand("newforeach");
		newForeachButton.setEnabled(!DES_COURSE_VERSION);
		buttonpanel.add(newForeachButton);

		buttonpanel.add(newInstanceButton = new JButton("New Instance"));
		newInstanceButton.setActionCommand("newinstance");
		newInstanceButton.addActionListener(this);
		newInstanceButton.setEnabled(!DES_COURSE_VERSION);

		buttonpanel.add(newBindingButton = new JButton("New Binding"));
		newBindingButton.setActionCommand("newbinding");
		newBindingButton.addActionListener(this);
		newBindingButton.setEnabled(!DES_COURSE_VERSION);

		JButton deleteComponentButton = new JButton("Remove Component");
		deleteComponentButton.addActionListener(this);
		deleteComponentButton.setActionCommand("remove component");
		buttonpanel.add(deleteComponentButton);		

		JPanel content = new JPanel();
		Box b = new Box(BoxLayout.PAGE_AXIS);
		b.add(new JScrollPane(moduleSelectTree));
		b.add(buttonpanel);
		content.add(b);
		content.setLayout(new GridLayout(1, 1));

		return content;
	}

	public JMenuBar createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();

		// New menu
		JMenu menu = new JMenu(WLang.FileMenu);		
		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription("The File menu");
		menuBar.add(menu);

		JMenuItem menuItem = new JMenuItem(WLang.FileNewMenu, KeyEvent.VK_N);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		FileNewMenu = menuItem;

		menuItem = new JMenuItem(WLang.FileOpenMenu, KeyEvent.VK_O);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		FileOpenMenu = menuItem;

		menuItem = new JMenuItem(WLang.FileSaveMenu, KeyEvent.VK_S);
		menuItem.addActionListener(this);
		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);
		FileSaveMenu = menuItem;

		menuItem = new JMenuItem(WLang.FileSaveAsMenu, KeyEvent.VK_A);
		menuItem.addActionListener(this);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		menu.add(menuItem);
		FileSaveAsMenu = menuItem;

		menu.addSeparator();

		menuItem = new JMenuItem(WLang.FilePageSetupMenu, KeyEvent.VK_G);
		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);

		menuItem = new JMenuItem(WLang.FilePrintMenu, KeyEvent.VK_P);

		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);
		menu.addSeparator();

		menuItem = new JMenuItem(WLang.FileExitMenu, KeyEvent.VK_X);

		menu.add(menuItem);
		menuItem.addActionListener(this);

		FileExitMenu = menuItem;

		//fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(new WmodFileFilter());

		// Next menu
		menu = new JMenu("Edit");
		menu.setMnemonic(KeyEvent.VK_E);
		menuBar.add(menu);

		menuItem = new JMenuItem("New Simple Component", KeyEvent.VK_N);
		menuItem.addActionListener(this);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		menuItem.setActionCommand("newsimple");
		menu.add(menuItem);

		menuItem = new JMenuItem("Remove component", KeyEvent.VK_R);
		menuItem.addActionListener(this);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		menuItem.setActionCommand("remove component");
		menu.add(menuItem);

		// Next menu
		menu = new JMenu("Analysis");
		menu.setMnemonic(KeyEvent.VK_A);
		menuBar.add(menu);

		menuItem = new JMenuItem("Export To Supremica", KeyEvent.VK_E);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		analysisExportSupremicaMenu = menuItem;

		return menuBar;
	}

	public void constructWindow()
	{
		// Construct the window
		tabbedPane = new JTabbedPane();
		Component defaultTab;
		tabbedPane.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		if (!DES_COURSE_VERSION)
			tabbedPane.addTab("Parameters", null, createParametersPane(), "");
		tabbedPane.addTab("Events", null, createEventsPane(), "Create events");
		if (!DES_COURSE_VERSION)
			tabbedPane.addTab("Aliases", null, new JPanel(), "Create aliases");
		tabbedPane.addTab("Components", null, defaultTab = createContentPane(), "Create components");
		if (!DES_COURSE_VERSION)
			tabbedPane.addTab("Compile", null, new JPanel(), "Compilation");
		if (!DES_COURSE_VERSION)
			tabbedPane.addTab("Debug", null, debugPane, "Debug information");
		debugPane.addFocusListener(this);

		// Components selected by default
		tabbedPane.setSelectedComponent(defaultTab);
		this.setJMenuBar(createMenuBar());

		JPanel background = new JPanel();
		background.setOpaque(true);
		background.add(tabbedPane);
		setContentPane(background);
		background.setLayout(new GridLayout(1, 1));
		pack();
		repaint();
	}

	public void actionPerformed(ActionEvent e)
	{
		if ("newsimple".equals(e.getActionCommand()))
		{
			DefaultMutableTreeNode parentNode = null;
			TreePath parentPath = moduleSelectTree.getSelectionPath();

			/*if (parentPath == null) {
			//There's no selection. Default to the root node.
			parentNode = rootNode;
			} else {
			parentNode = (DefaultMutableTreeNode)
			(parentPath.getLastPathComponent());
			}*/
			EditorNewDialog diag = new EditorNewDialog(this, parentNode);

			logEntry("New Simple Component requested");
		}

		if ("newforeach".equals(e.getActionCommand()))
		{
			EditorForeachDialog diag = new EditorForeachDialog(this);

			logEntry("New Foreach Component requested");
		}

		if ("remove component".equals(e.getActionCommand()))
		{
			TreePath currentSelection = moduleSelectTree.getSelectionPath();
			if (currentSelection != null) 
			{
				// Find the depth of the component...
				int depth = currentSelection.getPathCount()-1;
				// Get the node in the tree
				DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
				// Find the way to the component in the module
				ListSubject<AbstractSubject> rootList = module.getComponentListModifiable();
				ListSubject<AbstractSubject> currentList = rootList;
				for (int i=1; i<depth; i++)
				{
					// We're (depth-i) levels too deep
					DefaultMutableTreeNode currentNode = targetNode;
					for (int j=0; j<(depth-i); j++)
					{
						currentNode = (DefaultMutableTreeNode) (currentNode.getParent());
					}
					// Find currentNode (a ForeachSubject) in currentList and unfold a new ListSubject
					ForeachSubject foreachSubject = (ForeachSubject) currentList.get(currentList.indexOf(((ComponentInfo) currentNode.getUserObject()).getComponent()));
					currentList = foreachSubject.getBodyModifiable();
				}
				// I just realised there's a nicer way to do this... well, well.

				// Remove component from module
				currentList.remove(((ComponentInfo) targetNode.getUserObject()).getComponent());

				// Remove the component visually
				MutableTreeNode parent = (MutableTreeNode) (targetNode.getParent());
				if (parent != null) 
				{
					treeModel.removeNodeFromParent(targetNode);
					return;
				}
			}
			
			logEntry("Remove Component requested");
		}

		if ("newinstance".equals(e.getActionCommand()))
		{
			InstanceEditorDialog diag = new InstanceEditorDialog(this);

			logEntry("New Instance Component requested");
		}

		if ("newbinding".equals(e.getActionCommand()))
		{
			BindingEditorDialog diag = new BindingEditorDialog(this);

			logEntry("New Binding requested");
		}

		if ("newevent".equals(e.getActionCommand()))
		{
			EventEditorDialog diag = new EventEditorDialog(this);

			logEntry("New event requested");
		}

		if ("delevent".equals(e.getActionCommand()))
		{
			int index = dataList.getSelectedIndex();

			if (index != -1)
			{
				module.getEventDeclListModifiable().remove(data.get(index));
				data.remove(index);
			}

			logEntry("New event requested");
		}

		if ("neweventparam".equals(e.getActionCommand()))
		{
			EventParameterEditorDialog diag = new EventParameterEditorDialog(this);

			logEntry("New event parameter requested");
		}

		if ("newparam".equals(e.getActionCommand()))
		{
			SimpleParameterEditorDialog diag = new SimpleParameterEditorDialog(this);

			logEntry("New simple parameter requested");
		}

		if ("SaveDebug".equals(e.getActionCommand()))
		{
			JFileChooser fc = new JFileChooser(".");
			int returnVal = fc.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				try
				{
					FileOutputStream saveStream = new FileOutputStream(fc.getSelectedFile());

					saveStream.write(debugArea.getText().getBytes());
				}
				catch (Exception exception)
				{
					logEntry("Could not save debug log: " + exception.getMessage());
					JOptionPane.showMessageDialog(this, "Could not save debug log: " + exception.getMessage());
				}
			}
		}

		if (e.getSource() == FileNewMenu)
		{
			String modName = JOptionPane.showInputDialog(this, "Module Name?");

			if (modName != null)
			{
				try
				{
					final IdentifierProxy ident =
						mExpressionParser.parseIdentifier(modName);
					if (ident instanceof SimpleIdentifierProxy) {
						module = new ModuleSubject(modName, null);
						constructWindow();
						logEntry("New module created: " + modName);
					}
					else
					{
						logEntry("Invalid module name: " + modName);
						JOptionPane.showMessageDialog
							(this, "Invalid module name");
					}
				}
				catch (final ParseException exception)
				{
					ErrorWindow.askRevert(exception,  modName);
					//constructWindow();
					ModuleWindow w = new ModuleWindow("Waters");
				}
			}
		}

		if (e.getSource() == FileOpenMenu)
		{
			int returnVal = fileChooser.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = fileChooser.getSelectedFile();

				loadWmodFile(file);
				logEntry("File opened: " + file);

				modified = false;

				constructWindow();
			}
			else
			{
				// Open cancelled...  do nothing
			}
		}

		if (e.getSource() == FileSaveAsMenu)
		{
			saveAs();
		}

		if (e.getSource() == FileExitMenu)
		{
			//System.exit(0);
			processWindowEvent(new WindowEvent(this,WindowEvent.WINDOW_CLOSING));
		}

		if (e.getSource() == analysisExportSupremicaMenu)
		{
			exportToSupremica();
		}
	}

	private void saveAs()
	{
		WmodFileFilter filter = new WmodFileFilter();
		fileSaveChooser.setFileFilter(filter);			
		int returnVal = fileSaveChooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File file = fileSaveChooser.getSelectedFile();
			if (!filter.accept(file))
			{
				file = new File(file.getPath() + "." + WmodFileFilter.WMOD);
			}
			
			saveWmodFile(file);
			
			modified = false;
			
			logEntry("File saved: " + file);
		}
		else
		{
			// SaveAs cancelled...  do nothing
		}
	}

	public void addComponent(final AbstractSubject o)
	{
		logEntry("addComponent: " + o);

		if (module != null)
		{
			modified = true;

			DefaultMutableTreeNode parentNode = null;
			TreePath parentPath = moduleSelectTree.getSelectionPath();

			if (parentPath == null)
			{
				//There's no selection. Default to the root node.
				parentNode = rootNode;

				moduleSelectTree.expandPath(new TreePath(rootNode.getPath()));
			}
			else
			{
				parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
			}

			ComponentInfo ci = (ComponentInfo) (parentNode.getUserObject());

			logEntry("addComponent: Parent: " + parentNode.toString());

			if (ci.getComponent() instanceof ForeachSubject)
			{
				((ForeachSubject) ci.getComponent()).getBodyModifiable().add(o);
			}
			else
			{
				module.getComponentListModifiable().add(o);
			}

			// We don't want InstanceSubject components having children
			if (!(ci.getComponent() instanceof ForeachSubject) &&!(o instanceof ParameterBindingSubject))
			{
				parentNode = rootNode;
			}

			if (!(ci.getComponent() instanceof InstanceSubject) && (o instanceof ParameterBindingSubject))
			{
				return;
			}

			//constructWindow();
			DefaultMutableTreeNode newChild = makeTreeFromComponent((AbstractSubject) o);

			treeModel.insertNodeInto(newChild, parentNode, treeModel.getChildCount(parentNode));

			if (o instanceof ForeachSubject)
			{
				moduleSelectTree.expandPath(new TreePath(newChild.getPath()));
			}

			if ((o instanceof SimpleComponentSubject))
			{
				SimpleComponentSubject scp = (SimpleComponentSubject) o;
				
				logEntry("Adding SimpleComponentSubject: " + scp.getName());

				EditorWindow ed = new EditorWindow(scp.getName() + " - Waters Editor", module, scp, this, this);
			}

			moduleSelectTree.expandPath(new TreePath(parentNode.getPath()));
		}
	}

	public void focusGained(FocusEvent e)
	{
		if (e.getComponent() == debugPane)
		{
			debugArea.setText(debugText);
		}
	}

	public void focusLost(FocusEvent e)
	{
		;
	}

	public ModuleSubject getModuleSubject()
	{
		return module;
	}

	public void addUndoable(UndoableEdit e)
	{
		mUndoManager.addEdit(e);
		fireEditorChangedEvent(new UndoRedoEvent());
	}

	public void executeCommand(Command c)
	{
		c.execute();
//		if (c instanceof UndoableEdit) {
		addUndoable(new UndoableCommand(c));
//		}
	}

	public boolean canRedo()
	{
		return mUndoManager.canRedo();
	}

	public boolean canUndo()
	{
		return mUndoManager.canUndo();
	}

	public void clearList()
	{
		mUndoManager.discardAllEdits();
		fireEditorChangedEvent(new UndoRedoEvent());
	}

    public String getRedoPresentationName()
	{
		return mUndoManager.getRedoPresentationName();
	}

    public String getUndoPresentationName()
	{
		return mUndoManager.getUndoPresentationName();
	}

	public void redo() throws CannotRedoException
	{
		mUndoManager.redo();
		fireEditorChangedEvent(new UndoRedoEvent());
	}

	public void undo() throws CannotUndoException
	{
		mUndoManager.undo();
		fireEditorChangedEvent(new UndoRedoEvent());
	}

	public void attach(Observer o)
	{
		mObservers.add(o);
	}
	
	public void detach(Observer o)
	{
		mObservers.remove(o);
	}

	public void fireEditorChangedEvent(EditorChangedEvent e)
	{
		for (Observer o : mObservers) {
			o.update(e);
		}
	}

	//////////////////////////////
	// WindowListener interface //
	//////////////////////////////

	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowClosing(WindowEvent e)
	{
		// If modified, opt to save changes!
		// "modified" does not work properly as of yet.
		//if (modified)
		{
			int yesNo = JOptionPane.showConfirmDialog(this, "Do you want to save the module before exiting?", "Save before exit?", JOptionPane.YES_NO_OPTION);
			if (yesNo == JOptionPane.YES_OPTION)
				saveAs(); 
		}
		System.exit(0); 
	}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}

	//########################################################################
	//# Access the Module
	public ExpressionParser getExpressionParser()
	{
		return mExpressionParser;
	}

	public ProxyPrinter getPrinter()
	{
		return mPrinter;
	}
	
	private class IdentifierTransfer implements Transferable
	{
		// the IdentifierSubject transferred by this Object
		Object ip_;
		DataFlavor data_;
		
		/**
		 * creates a new transferable object containing the specified IdentifierSubject
		 *
		 * @param ip the IdentifierSubject being transferred
		 */

		public IdentifierTransfer(Object ip)
		{
			ip_ = ip;
			data_ = new DataFlavor(ip.getClass(), ip.getClass().getName());
		}
		
		public Object getTransferData(DataFlavor f)
			throws UnsupportedFlavorException
		{
			if (isDataFlavorSupported(f))
				return ip_;
			else
				throw new UnsupportedFlavorException(f);
		}
	   
		public DataFlavor[] getTransferDataFlavors()
		{
			DataFlavor[] d = new DataFlavor[1];
			d[0] = data_;
			return d;
		}

		public boolean isDataFlavorSupported(DataFlavor f)
		{
			return f.getRepresentationClass().isAssignableFrom(data_.getRepresentationClass());
		}
	}

	private class DSListener extends DragSourceAdapter
	{	  		
		public void dragOver(DragSourceDragEvent e)
		{
			if (e.getTargetActions() == DnDConstants.ACTION_COPY) {
				e.getDragSourceContext().setCursor
					(DragSource.DefaultCopyDrop);
			} else {
				e.getDragSourceContext().setCursor
					(DragSource.DefaultCopyNoDrop);
			}
		}
	}

	private class DGListener implements DragGestureListener
	{
		final JList mList;
		
		public DGListener(JList list)
		{
			mList = list;
		}
		
		private EventKind guessEventKind(final IdentifierSubject ident)
		{		
			if (ident == null || module == null) {
				return null;
			}
			final String name = ident.getName();
			final IndexedList<EventDeclSubject> decls =
				module.getEventDeclListModifiable();
			final EventDeclSubject decl = decls.get(name);
			if (decl != null) {
				return decl.getKind();
			}
			final IndexedList<ParameterSubject> params =
				module.getParameterListModifiable();
			final ParameterSubject param = params.get(name);
			if (param != null && param instanceof EventParameterSubject) {
				final EventParameterSubject eparam = (EventParameterSubject) param;
				final EventDeclSubject edecl = eparam.getEventDecl();
				return edecl.getKind();
			}
			return null;
		}
		
		public void dragGestureRecognized(DragGestureEvent e)
		{		
			final int row = mList.locationToIndex(e.getDragOrigin());
			//System.out.println(row);
			if (row == -1) {
				return;
			}
			EventDeclProxy ident = (EventDeclProxy)mList.getModel().getElementAt(row);
			final Transferable t = new IdentifierTransfer
										(new IdentifierWithKind(
										new SimpleIdentifierSubject(ident.getName()),
										ident.getKind()));
			try {
				e.startDrag(DragSource.DefaultCopyDrop, t);
			} catch (InvalidDnDOperationException idoe) {
				throw new IllegalArgumentException(idoe);
			}
		}
	}


	//########################################################################
	//# Main Routine
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			System.out.println("UIManager.setLookAndFeel() failed");
		}

		// Default to using English
		WLang = new Languages();

		LanguagesEN.createLanguage(WLang);

		ModuleWindow editor = new ModuleWindow("Waters");
	}


	//########################################################################
	//# Data Members
	private Set<Observer> mObservers = new HashSet();
	private ModuleSubject module = null;
	private JFileChooser fileChooser = new JFileChooser(".");
	private JFileChooser fileSaveChooser = new JFileChooser(".");
	private JMenuItem FileNewMenu;
	private JMenuItem FileOpenMenu;
	private JMenuItem FileSaveMenu;
	private JMenuItem FileSaveAsMenu;
	private JMenuItem FileExitMenu;
	private JMenuItem analysisExportSupremicaMenu;
	private JTree moduleSelectTree = null;
	private JButton newSimpleButton;
	private JButton newForeachButton;
	private JButton deleteComponentButton;
	private JButton newEventButton;
	private JButton deleteEventButton;
	private JButton newInstanceButton;
	private JButton newBindingButton;
	private JButton newParamButton;
	private JButton newEventParamButton;
	private JButton deleteParamButton;
	private JTabbedPane tabbedPane;
	private JTextArea debugArea;
	private JPanel debugPane = null;
	private String debugText = "";
	private DefaultMutableTreeNode rootNode = null;
	private JList dataList = null;
	private DefaultListModel data = null;
	private DefaultTreeModel treeModel = null;
	private boolean modified = true;
	private JList paramdataList = null;
	private DefaultListModel paramData = null;

	private org.supremica.gui.Supremica supremica = null;
	private final ExpressionParser mExpressionParser;
	private final ProxyPrinter mPrinter;
	private final UndoManager mUndoManager = new UndoManager();

	private static Languages WLang;
	
	private DragSource mDragSource;
	private DragGestureListener mDGListener;
	private DragSourceListener mDSListener;
	private int mDragAction = DnDConstants.ACTION_COPY;
}
