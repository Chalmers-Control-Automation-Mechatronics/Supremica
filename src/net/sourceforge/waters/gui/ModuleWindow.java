//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ModuleWindow
//###########################################################################
//# $Id: ModuleWindow.java,v 1.23 2006-02-20 22:20:21 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.GridLayout;
import java.awt.Dimension;
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
import net.sourceforge.waters.gui.observer.Subject;
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
	implements ActionListener, FocusListener, UndoInterface
{

	//########################################################################
	//# Constructor
	public ModuleWindow(String title)
	{
		setTitle(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

	public void exportToSupremica()
	{
		//System.err.println("exportToSupremica");
		ModuleSubject currModule = getModuleSubject();
		ProjectBuildFromWaters builder = new ProjectBuildFromWaters();
		Project supremicaProject = builder.build(currModule);

		if (supremica == null)
		{
			// Initializes some properties
			org.supremica.apps.Supremica dummySupremica =
				new org.supremica.apps.Supremica();
			supremica = org.supremica.apps.SupremicaWithGui.startSupremica();
		}
		else
		{
			supremica.setVisible(true);
		}

		//supremica.
		System.err.println(supremicaProject.toString());

		int nbrOfAddedAutomata = supremica.addProject(supremicaProject);

		System.err.println(nbrOfAddedAutomata);
	}

	public JPanel createEventsPane()
	{
		final ArrayList l;

		data = new DefaultListModel();

		if (module != null)
		{
			for (final EventDeclProxy event : module.getEventDeclList()) {
				data.addElement(event);
			}
		}

		dataList = new JList(data);

		dataList.setCellRenderer(new EventListCell());

		JButton NewEventButton = new JButton("New Event");

		NewEventButton.setActionCommand("newevent");
		NewEventButton.addActionListener(this);

		JButton DeleteEventButton = new JButton("Delete Event");

		DeleteEventButton.setActionCommand("delevent");
		DeleteEventButton.addActionListener(this);

		Box jp = new Box(BoxLayout.PAGE_AXIS);
		JPanel p = new JPanel();

		p.add(NewEventButton);
		p.add(DeleteEventButton);
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

		ModuleSelectTree = new JTree(treeModel);

		ModuleSelectTree.setCellRenderer(new ModuleTreeRenderer(foreachIcon, plantIcon, propertyIcon, specIcon, instanceIcon, bindingIcon));
		ModuleSelectTree.setEditable(false);
		ModuleSelectTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

		//renderer.setLeafIcon(simpleIcon);
		//renderer.setOpenIcon(null);
		//renderer.setClosedIcon(null);
		//ModuleSelectTree.setCellRenderer(renderer);
		MouseListener ml = new MouseAdapter()
		{
			EditorWindow ed = null;

			public void mousePressed(MouseEvent e)
			{
				int selRow = ModuleSelectTree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = ModuleSelectTree.getPathForLocation(e.getX(), e.getY());

				if (selRow != -1)
				{
					if (e.getClickCount() == 2)
					{
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) ModuleSelectTree.getLastSelectedPathComponent();

						if (node == null)
						{
							return;
						}

						Object nodeInfo = node.getUserObject();

						if (node.isLeaf())
						{
							SimpleComponentSubject scp = (SimpleComponentSubject) (((ComponentInfo) nodeInfo).getComponent());

							if (scp != null)
							{
								ed = new EditorWindow(scp.getName() + " - Waters Editor", module, scp, ModuleWindow.this, ModuleWindow.this);
							}
						}
					}
				}
			}
		};

		ModuleSelectTree.addMouseListener(ml);

		JButton NewSimpleButton = new JButton("New Simple Component");

		NewSimpleButton.addActionListener(this);
		NewSimpleButton.setActionCommand("newsimple");

		JButton NewForeachButton = new JButton("New Foreach Component");

		NewForeachButton.addActionListener(this);
		NewForeachButton.setActionCommand("newforeach");

		JButton DeleteComponentButton = new JButton("Remove Component");

		if (module == null)
		{
			NewSimpleButton.setEnabled(false);
			NewForeachButton.setEnabled(false);
			DeleteComponentButton.setEnabled(false);
		}

		JPanel content = new JPanel();
		Box b = new Box(BoxLayout.PAGE_AXIS);

		b.add(new JScrollPane(ModuleSelectTree));

		JPanel buttonpanel = new JPanel();

		buttonpanel.add(NewSimpleButton);
		buttonpanel.add(NewForeachButton);
		buttonpanel.add(newInstanceButton = new JButton("New Instance"));
		buttonpanel.add(newBindingButton = new JButton("New Binding"));
		buttonpanel.add(DeleteComponentButton);
		newInstanceButton.setActionCommand("newinstance");
		newInstanceButton.addActionListener(this);
		newBindingButton.setActionCommand("newbinding");
		newBindingButton.addActionListener(this);
		b.add(buttonpanel);
		content.add(b);
		content.setLayout(new GridLayout(1, 1));

		return content;
	}

	public JMenuBar createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu(WLang.FileMenu);

		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription("The File menu");
		menuBar.add(menu);

		JMenuItem menuItem = new JMenuItem(WLang.FileNewMenu, KeyEvent.VK_O);

		menuItem.addActionListener(this);

		FileNewMenu = menuItem;

		menu.add(menuItem);

		menuItem = new JMenuItem(WLang.FileOpenMenu, KeyEvent.VK_O);

		menuItem.addActionListener(this);

		FileOpenMenu = menuItem;

		menu.add(menuItem);

		menuItem = new JMenuItem(WLang.FileSaveMenu, KeyEvent.VK_S);
		menuItem.addActionListener(this);
		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);
		FileSaveMenu = menuItem;

		menuItem = new JMenuItem(WLang.FileSaveAsMenu, KeyEvent.VK_A);
		menuItem.addActionListener(this);
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

		JMenu editMenu = new JMenu("Edit");

		menuBar.add(editMenu);

		JMenu analysisMenu = new JMenu("Analysis");

		menuBar.add(analysisMenu);

		menuItem = new JMenuItem("Export To Supremica");

		analysisMenu.add(menuItem);
		menuItem.addActionListener(this);

		analysisExportSupremicaMenu = menuItem;

		return menuBar;
	}

	public void constructWindow()
	{

		// Construct the window
		tabbedPane = new JTabbedPane();

		tabbedPane.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		tabbedPane.addTab("Parameters", null, createParametersPane(), "");
		tabbedPane.addTab("Events", null, createEventsPane(), "Create events");
		tabbedPane.addTab("Aliases", null, new JPanel(), "Create aliases");
		tabbedPane.addTab("Components", null, createContentPane(), "Create components");
		tabbedPane.addTab("Compile", null, new JPanel(), "Compilation");
		tabbedPane.addTab("Debug", null, debugPane, "Debug information");
		debugPane.addFocusListener(this);

		// Components selected by default
		tabbedPane.setSelectedIndex(3);
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
			TreePath parentPath = ModuleSelectTree.getSelectionPath();

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
			int returnVal = fileSaveChooser.showSaveDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = fileSaveChooser.getSelectedFile();

				saveWmodFile(file);

				modified = false;

				logEntry("File saved: " + file);
			}
			else
			{
				// SaveAs cancelled...  do nothing
			}
		}

		if (e.getSource() == FileExitMenu)
		{
			System.exit(0);
		}

		if (e.getSource() == analysisExportSupremicaMenu)
		{
			exportToSupremica();
		}
	}

	public void addComponent(final AbstractSubject o)
	{
		logEntry("addComponent: " + o);

		if (module != null)
		{
			modified = true;

			DefaultMutableTreeNode parentNode = null;
			TreePath parentPath = ModuleSelectTree.getSelectionPath();

			if (parentPath == null)
			{

				//There's no selection. Default to the root node.
				parentNode = rootNode;

				ModuleSelectTree.expandPath(new TreePath(rootNode.getPath()));
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
				ModuleSelectTree.expandPath(new TreePath(newChild.getPath()));
			}

			if ((o instanceof SimpleComponentSubject))
			{
				SimpleComponentSubject scp = (SimpleComponentSubject) o;

				logEntry("Adding SimpleComponentSubject: " + scp.getName());

				EditorWindow ed = new EditorWindow(scp.getName() + " - Waters Editor", module, scp, this, this);
			}

			ModuleSelectTree.expandPath(new TreePath(parentNode.getPath()));
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
			System.out.println(row);
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
	private JTree ModuleSelectTree = null;
	private JButton NewSimpleButton;
	private JButton NewForeachButton;
	private JButton DeleteComponentButton;
	private JButton NewEventButton;
	private JButton DeleteEventButton;
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
