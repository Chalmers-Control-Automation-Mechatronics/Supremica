//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   ModuleWindow
//###########################################################################
//# $Id: ModuleWindow.java,v 1.4 2005-02-17 19:59:55 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.base.*;
import net.sourceforge.waters.model.module.*;
import java.util.ArrayList;
import java.beans.*;
import net.sourceforge.waters.xsd.base.ComponentKind;
import java.util.Date;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.IdentifierProxy;
import net.sourceforge.waters.model.expr.SimpleIdentifierProxy;
import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.automata.Project;

/** <p>The primary module-loading window.</p>
 *
 * <p>This is the main window of Waters, from which module files can be loaded
 * and with which components can be added/removed from modules.</p>
 *
 * @author Gian Perrone
 */
public class ModuleWindow extends JFrame implements ActionListener, FocusListener {
    private ModuleProxy module = null;
	private JFileChooser fileChooser = new JFileChooser(".");
    private JFileChooser fileSaveChooser = new JFileChooser(".");
    private JMenuItem FileNewMenu;
    private JMenuItem FileOpenMenu;
    private JMenuItem FileSaveMenu;
    private JMenuItem FileExitMenu;
    private JMenuItem analysisExportSupremicaMenu;
    private JTree ModuleSelectTree = null;
    private static Languages WLang;
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

    public ModuleWindow(String title) {
        this.setTitle(title);
        JFrame.setDefaultLookAndFeelDecorated(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
       	module = new ModuleProxy("Unnamed Module", null);
		
		debugPane = createDebugPane();
		
        constructWindow();
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
    }

    /** Load a Waters module into the module viewer.
     * @param wmodf The file to load the module from
     */
    public void loadWmodFile( File wmodf ) {
	logEntry("Attempting to load: " + wmodf);
        try {
            final ProxyMarshaller marshaller =  new ModuleMarshaller();
            module = (ModuleProxy) marshaller.unmarshal(wmodf);
        } catch (final JAXBException exception) {
            // Something bad happened
            JOptionPane.showMessageDialog(this,
                                          "Error loading module file! (JAXBException)");
	    logEntry("JAXBException - Failed to load: " + wmodf);
	    //exception.printStackTrace(System.err);
        } catch (final ModelException exception) {
            JOptionPane.showMessageDialog(this,
                                          "Error loading module file! (ModelException)");
	    logEntry("ModelException - Failed to load: " + wmodf);
        }
    }

    public void saveWmodFile( File wmodf ) {
	logEntry("Saving module to: " + wmodf);
        try {
            final ProxyMarshaller marshaller =  new ModuleMarshaller();
	    module.setLocation(wmodf);
            marshaller.marshal(module,wmodf);
        } catch (final JAXBException exception) {
            // Something bad happened
            JOptionPane.showMessageDialog(this,
                                          "Error saving module file! (JAXBException)");
	    logEntry("JAXBException - Failed to save: " + wmodf);
        } catch (final java.io.IOException exception) {
	    JOptionPane.showMessageDialog(this,
                                          "Error saving module file! (IOException)");
	    logEntry("IOException - Failed to save: " + wmodf);
	}
    }

	public void exportToSupremica()
	{
		//System.err.println("exportToSupremica");
		ModuleProxy currModule = getModuleProxy();

		ProjectBuildFromWaters builder = new ProjectBuildFromWaters();
		Project supremicaProject = builder.build(currModule);

		if (supremica == null)
		{
			// Initializes some properties
			org.supremica.apps.Supremica dummySupremica = new org.supremica.apps.Supremica();

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

    public JPanel createEventsPane() {
        final ArrayList l;

	data =  new DefaultListModel();

	if(module != null) {
	    for(int i = 0; i<module.getEventDeclList().size(); i++) {
		data.addElement(((EventDeclProxy)(module.getEventDeclList().get(i))));
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

	p.setLayout(new GridLayout(1,1));
        return p;
    }

    public JPanel createParametersPane() {
        final ArrayList l;

	paramData =  new DefaultListModel();

	if(module != null) {
	    for(int i = 0; i<module.getParameterList().size(); i++) {
		paramData.addElement(((ParameterProxy)(module.getParameterList().get(i))));
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

	p.setLayout(new GridLayout(1,1));
        return p;
    }

    public DefaultListModel getEventDataList() {
	return data;
    }

    public DefaultListModel getParameterDataList() {
	return paramData;
    }

    public JPanel createDebugPane() {
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
	jp.setLayout(new GridLayout(1,1));
	logEntry("Logging started.");
	return jp;
    }

    public void logEntry(String entry) {
	Date d = new Date();
	debugText += ("(" + d.toString() + ") - " + entry + "\n");
	// TODO - Add logging to file
    }

    private DefaultMutableTreeNode makeTreeFromComponent( ElementProxy e ) {

	if(e instanceof SimpleComponentProxy) {
	    final Object userobject = new ComponentInfo
	      (((IdentifiedElementProxy)e).getName(), e);
	    return new DefaultMutableTreeNode(userobject, false);
	} else if (e instanceof InstanceProxy) {
	    InstanceProxy i = (InstanceProxy)e;
	    String name = "<html><b>Instance </b><i>" + i.getName() +
		"</i> = <i>" + i.getModuleName() + "</i></html>";
	    final Object userobject = new ComponentInfo(name, e);
	    DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(userobject, true);
	    for(int j = 0; j<i.getBindingList().size(); j++) {
		tmp.add(makeTreeFromComponent((ElementProxy)(i.getBindingList().get(j))));
	    }
	    return tmp;
	} else if (e instanceof ParameterBindingProxy) {
	    ParameterBindingProxy i = (ParameterBindingProxy)e;
	    String name = "<html><b>Binding: </b><i>" + i.getName() +
		"</i> = <i>" + i.getExpression().toString() + "</i></html>";
	    final Object userobject = new ComponentInfo(name, e);
	    return new DefaultMutableTreeNode(userobject, false);
	} else {
	    String name;
	    ForeachProxy v = (ForeachProxy)e;
	    ElementProxy range = v.getRange();
	    ElementProxy guard = v.getGuard();
	    name = "<html><b>Foreach </b><i>" + ((NamedProxy)e).getName() + "</i>";
	    if(range != null) {
		name += " <b>in<b> <i>" + range.toString() + "</i>";
	    }
	    if(guard != null) {
		name += " <b>Where</b> <i>" + guard.toString() + "</i></html>";
	    }
	    final Object userobject = new ComponentInfo(name, e);
	    final DefaultMutableTreeNode tn =
	      new DefaultMutableTreeNode(userobject, true);
	    for(int i = 0; i<v.getBody().size(); i++) {
		tn.add(makeTreeFromComponent((ElementProxy)(v.getBody().get(i))));
	    }

	    return tn;
	}
    }

    public JPanel createContentPane() {
        final ArrayList l;
	DefaultMutableTreeNode treeNode = null;

        if(module != null) {
            l = new ArrayList(module.getComponentList());
	    treeNode = new DefaultMutableTreeNode(new ComponentInfo("Module: " + module.getName(), null));
        }

        else {
            l = new ArrayList();
	    treeNode = null;
        }

	for(int i = 0; i<l.size(); i++) {
	    treeNode.add(makeTreeFromComponent((ElementProxy)(l.get(i))));
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

	ModuleSelectTree.setCellRenderer(new ModuleTreeRenderer(foreachIcon,
								plantIcon,
								propertyIcon,
								specIcon,
								instanceIcon,
								bindingIcon));
	ModuleSelectTree.setEditable(false);

	ModuleSelectTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	DefaultTreeCellRenderer renderer =
	    new DefaultTreeCellRenderer();
	//renderer.setLeafIcon(simpleIcon);
	//renderer.setOpenIcon(null);
	//renderer.setClosedIcon(null);
	//ModuleSelectTree.setCellRenderer(renderer);

	MouseListener ml = new MouseAdapter() {
		EditorWindow ed = null;

		public void mousePressed(MouseEvent e) {
		    int selRow = ModuleSelectTree.getRowForLocation(e.getX(), e.getY());
		    TreePath selPath = ModuleSelectTree.getPathForLocation(e.getX(), e.getY());
		    if(selRow != -1) {
			if(e.getClickCount() == 2) {
			    DefaultMutableTreeNode node =
				(DefaultMutableTreeNode)ModuleSelectTree.getLastSelectedPathComponent();
			    if (node == null) return;

			    Object nodeInfo = node.getUserObject();
			    if (node.isLeaf()) {
				SimpleComponentProxy scp = (SimpleComponentProxy)(((ComponentInfo)nodeInfo).getComponent());
				ed = new EditorWindow(scp.getName() +
						      " - Waters Editor",
						      module,
						      scp);
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

	if(module == null) {
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
	content.setLayout(new GridLayout(1,1));


        return content;
    }

    public JMenuBar createMenuBar() 
	{
        JMenuBar menuBar = new JMenuBar();
		
        JMenu menu = new JMenu(WLang.FileMenu);
		
		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription(
                                                             "The File menu");
		menuBar.add(menu);
		
		JMenuItem menuItem = new JMenuItem(WLang.FileNewMenu,
										   KeyEvent.VK_O);
		menuItem.addActionListener(this);
		FileNewMenu = menuItem;
		menu.add(menuItem);
		
		menuItem = new JMenuItem(WLang.FileOpenMenu,
								 KeyEvent.VK_O);
		menuItem.addActionListener(this);
		FileOpenMenu = menuItem;
		menu.add(menuItem);
		
		menuItem = new JMenuItem(WLang.FileSaveMenu,
								 KeyEvent.VK_S);
		FileSaveMenu = menuItem;
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		menuItem = new JMenuItem(WLang.FileSaveAsMenu,
								 KeyEvent.VK_A);
        menu.add(menuItem);
		
        menu.addSeparator();
		
        menuItem = new JMenuItem(WLang.FilePageSetupMenu,
								 KeyEvent.VK_G);
		menu.add(menuItem);
		
        menuItem = new JMenuItem(WLang.FilePrintMenu,
								 KeyEvent.VK_P);
		menu.add(menuItem);
		
        menu.addSeparator();
		
		menuItem = new JMenuItem(WLang.FileExitMenu,
								 KeyEvent.VK_X);
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

    public void constructWindow() {
	// Construct the window
	tabbedPane = new JTabbedPane();

	tabbedPane.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

	tabbedPane.addTab("Parameters", null, createParametersPane(),
			  "");

	tabbedPane.addTab("Events", null, createEventsPane(),
			  "Create events");

	tabbedPane.addTab("Aliases", null, new JPanel(),
			  "Create aliases");

	tabbedPane.addTab("Components", null, createContentPane(),
			  "Create components");

	tabbedPane.addTab("Compile", null, new JPanel(),
			  "Compilation");

	tabbedPane.addTab("Debug", null, debugPane,
			  "Debug information");
	debugPane.addFocusListener(this);

	// Components selected by default
	tabbedPane.setSelectedIndex(3);

	this.setJMenuBar(createMenuBar());

	JPanel background = new JPanel();
	background.setOpaque(true);

	background.add(tabbedPane);
	setContentPane(background);
	background.setLayout(new GridLayout(1,1));

	pack();
	repaint();
    }

    public void actionPerformed(ActionEvent e) 
	{
		if("newsimple".equals(e.getActionCommand())) {
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

		if("newforeach".equals(e.getActionCommand())) {
			EditorForeachDialog diag = new EditorForeachDialog(this);
			logEntry("New Foreach Component requested");
		}
		
		if("newinstance".equals(e.getActionCommand())) {
			InstanceEditorDialog diag = new InstanceEditorDialog(this);
			logEntry("New Instance Component requested");
		}
		
		if("newbinding".equals(e.getActionCommand())) {
			BindingEditorDialog diag = new BindingEditorDialog(this);
			logEntry("New Binding requested");
		}
		
		if("newevent".equals(e.getActionCommand())) {
			EventEditorDialog diag = new EventEditorDialog(this);
			logEntry("New event requested");
		}
		
		if("delevent".equals(e.getActionCommand())) {
			int index = dataList.getSelectedIndex();
			if(index != -1) {
				module.getEventDeclList().remove(data.get(index));
				data.remove(index);
			}
			
			logEntry("New event requested");
		}
		
		if("neweventparam".equals(e.getActionCommand())) {
			EventParameterEditorDialog diag = new EventParameterEditorDialog(this);
			logEntry("New event parameter requested");
		}
		
		if("newparam".equals(e.getActionCommand())) {
			SimpleParameterEditorDialog diag = new SimpleParameterEditorDialog(this);
			logEntry("New simple parameter requested");
		}
		
		if("SaveDebug".equals(e.getActionCommand())) {
			JFileChooser fc = new JFileChooser(".");
			int returnVal = fc.showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					FileOutputStream saveStream = new FileOutputStream(fc.getSelectedFile());
					saveStream.write(debugArea.getText().getBytes());
				} catch(Exception exception) {
					logEntry("Could not save debug log: " + exception.getMessage());
					JOptionPane.showMessageDialog(this,
												  "Could not save debug log: " + exception.getMessage());
				}
			}
		}
		
		if(e.getSource() == FileNewMenu) {
			String modName = JOptionPane.showInputDialog(this, "Module Name?");
			try {
				final ExpressionParser parser = new ExpressionParser();
				ExpressionProxy ep = parser.parse(modName);
				if(ep instanceof SimpleIdentifierProxy) {
					module = new ModuleProxy(modName,null);
					constructWindow();
					logEntry("New module created: " + modName);
				}
				else {
					logEntry("Invalid module identifier: " + modName);
					JOptionPane.showMessageDialog(this,
												  "Invalid module identifier");
				}
			} catch(final ParseException exception) {
				ErrorWindow ew = new ErrorWindow("Parse error in identifier: " + exception.getMessage(),
												 modName,
												 exception.getPosition());
				//constructWindow();
				ModuleWindow w = new ModuleWindow("Waters");
			}
		}
		
		if(e.getSource() == FileOpenMenu) {
            int returnVal = fileChooser.showOpenDialog(this);
			
            if (returnVal == JFileChooser.APPROVE_OPTION) {
            	File file = fileChooser.getSelectedFile();
                loadWmodFile(file);
				logEntry("File opened: " + file);
				modified = false;
				constructWindow();
			} else {
                // Open cancelled...  do nothing
            }
		}
		
		if(e.getSource() == FileSaveMenu) {
            int returnVal = fileSaveChooser.showSaveDialog(this);
			
            if (returnVal == JFileChooser.APPROVE_OPTION) {
            	File file = fileSaveChooser.getSelectedFile();
                saveWmodFile(file);
				modified = false;
				logEntry("File saved: " + file);
			} else {
				// Open cancelled...  do nothing
			}
		}
		
		if(e.getSource() == FileExitMenu) 
		{
			System.exit(0);						
		}

		if(e.getSource() == analysisExportSupremicaMenu)
		{
			exportToSupremica();
		}
		
    }
	
    public void addComponent(Object o) {
	logEntry("addComponent: " + ((ElementProxy)o).toString());
	if(module != null) {
	    modified = true;
	    DefaultMutableTreeNode parentNode = null;
	    TreePath parentPath = ModuleSelectTree.getSelectionPath();

	    if (parentPath == null) {
		//There's no selection. Default to the root node.
		parentNode = rootNode;
		ModuleSelectTree.expandPath(new TreePath(rootNode.getPath()));
	    } else {
		parentNode = (DefaultMutableTreeNode)
		    (parentPath.getLastPathComponent());
	    }

	    ComponentInfo ci = (ComponentInfo)(parentNode.getUserObject());

	    logEntry("addComponent: Parent: " + parentNode.toString());

	    if(ci.getComponent() instanceof ForeachProxy) {
		((ForeachProxy)ci.getComponent()).getBody().add(o);
	    }

	    else {
		module.getComponentList().add(o);
	    }

	    // We don't want InstanceProxy components having children
	    if(!(ci.getComponent() instanceof ForeachProxy) && !(o instanceof ParameterBindingProxy)) {
		parentNode = rootNode;
	    }

	    if(!(ci.getComponent() instanceof InstanceProxy) && (o instanceof ParameterBindingProxy)) {
		return;
	    }

	    //constructWindow();
	    DefaultMutableTreeNode newChild = makeTreeFromComponent((ElementProxy)o);

	    treeModel.insertNodeInto(newChild,
				     parentNode,
				     treeModel.getChildCount(parentNode));

	    if(o instanceof ForeachProxy) {
		ModuleSelectTree.expandPath(new TreePath(newChild.getPath()));
	    }

	    if((o instanceof SimpleComponentProxy)) {
		SimpleComponentProxy scp = (SimpleComponentProxy)o;
		logEntry("Adding SimpleComponentProxy: " + scp.getName());
		EditorWindow ed = new EditorWindow(scp.getName() +
						   " - Waters Editor",
						   module,
						   scp);
	    }

	    ModuleSelectTree.expandPath(new TreePath(parentNode.getPath()));
	}
    }

    public void focusGained(FocusEvent e) {
	if(e.getComponent() == debugPane) {
	    debugArea.setText(debugText);
	}
    }

    public void focusLost(FocusEvent e) {
	;
    }

    public ModuleProxy getModuleProxy()
    {
	return module;
    }

    public static void main(String[] args) {
	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} catch (Exception e) {
	    System.out.println("UIManager.setLookAndFeel() failed");
	}

	// Default to using English
	WLang = new Languages();
	LanguagesEN.createLanguage(WLang);

	//TODO: Put this inside a thread
        ModuleWindow editor = new ModuleWindow("Waters");
    }
}
