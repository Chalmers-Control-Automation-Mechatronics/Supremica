//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ModuleWindow
//###########################################################################
//# $Id: ModuleWindow.java,v 1.61 2006-11-17 00:20:09 martin Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.UndoableCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.UndoRedoEvent;
import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.IndexedListSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.EventParameterSubject;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.ParameterSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.VariableSubject;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.automata.Project;
import org.xml.sax.SAXException;


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
  implements ActionListener, FocusListener, UndoInterface,
             WindowListener, ModuleWindowInterface
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
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    addWindowListener(this);

    setTitle(title);
    mModule = new ModuleSubject(title, null);
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
      final ProxyUnmarshaller<ModuleProxy> unMarshaller =
        getJAXBMarshaller();
      final URI uri = wmodf.toURI();
      mModule = (ModuleSubject) unMarshaller.unmarshal(uri);
      clearList();
      mChanged = false;
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
      final ProxyMarshaller<ModuleProxy> marshaller =
        getJAXBMarshaller();
      marshaller.marshal(mModule, wmodf);
      mChanged = false;
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

  private JAXBModuleMarshaller getJAXBMarshaller()
  {
    try {
      final ModuleProxyFactory factory =
        ModuleSubjectFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      return new JAXBModuleMarshaller(factory, optable);
    } catch (final JAXBException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final SAXException exception) {
      throw new WatersRuntimeException(exception);
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
    mEventList = new EventDeclListView(mModule);
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
    jp.add(new JScrollPane(mEventList));
    jp.add(p);

    p = new JPanel();
    p.add(jp);
    p.setLayout(new GridLayout(1, 1));
    return p;
  }

  public JPanel createParametersPane()
  {
    final IndexedListSubject<ParameterSubject> parameters =
      mModule.getParameterListModifiable();
    mParameterListModel = new IndexedListModel<ParameterSubject>(parameters);
    mParameterList = new JList(mParameterListModel);
    mParameterList.setCellRenderer(new ParameterListCell());

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
    jp.add(new JScrollPane(mParameterList));
    jp.add(p);

    p = new JPanel();

    p.add(jp);
    p.setLayout(new GridLayout(1, 1));

    return p;
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
	
  public JPanel createContentPane()
  {
    moduleSelectTree = new ModuleTree(this);

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
		
    if("add variable".equals(e.getActionCommand())) {
      //add new variable
      logEntry("New variable requested");
      TreePath currentSelection = moduleSelectTree.getSelectionPath();
      if (currentSelection != null) 
        {
          // Get the node in the tree
          DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode)
            (currentSelection.getLastPathComponent());
          Subject component = ((ComponentInfo) 
                               targetNode.getUserObject()).getComponent();
				
          if(component instanceof VariableSubject) {
            component = component.getParent().getParent();
            EditorEditVariableDialog.showDialog(null,
                                                (SimpleComponentSubject) component, moduleSelectTree);
          } else if(component instanceof SimpleComponentSubject) {
            EditorEditVariableDialog.showDialog(null,
                                                (SimpleComponentSubject) component, moduleSelectTree);
          } else {
            System.err.println("ModuleWindow.actionPerformed(): " +
                               "'add variable' performed by illegal node type");
          }
        }
    }
		
    if("edit variable".equals(e.getActionCommand())) {
      //edit existing variable
      logEntry("Editing of variable requested");
      TreePath currentSelection = moduleSelectTree.getSelectionPath();
      if (currentSelection != null) 
        {
          // Get the node in the tree
          DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode)
            (currentSelection.getLastPathComponent());
          Subject component = ((ComponentInfo) 
                               targetNode.getUserObject()).getComponent();
				
          if(component instanceof VariableSubject) {
            Subject parent = component.getParent().getParent();
            EditorEditVariableDialog.showDialog((VariableSubject) component,
                                                (SimpleComponentSubject) parent, moduleSelectTree);
          } else {
            System.err.println("ModuleWindow.actionPerformed(): " +
                               "'edit variable' performed by illegal node type");
          }
        }
    }
    
    if ("delete variable".equals(e.getActionCommand()))
    {
    	  TreePath currentSelection = moduleSelectTree.getSelectionPath();
    	  if (currentSelection != null) 
    	  {
    		// Get the node in the tree
    		DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
    		
    		//Special treatment for variables
    		VariableSubject variable = (VariableSubject) ((ComponentInfo) 
    				targetNode.getUserObject()).getComponent();
    		
    		//remove from model (take getParent()x2 because a variableSubject is the child of a list.)
    		((SimpleComponentSubject) variable.getParent().getParent())
    		.getVariablesModifiable().remove(variable);
    		
    		//remove from module tree view
    		moduleSelectTree.removeCurrentNode();
    		return;
    	  }
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
				
            //Special treatment for variables
            AbstractSubject component = ((ComponentInfo) 
                                         targetNode.getUserObject()).getComponent();
            if(component instanceof VariableSubject) {
              //remove from model (take getParent()x2 because a variableSubject is the child of a list.)
              ((SimpleComponentSubject) component.getParent().getParent())
                .getVariablesModifiable().remove(component);
					
              //remove from module tree view
              moduleSelectTree.removeCurrentNode();
              return;
            }
				
            // Find the way to the component in the module
            ListSubject<AbstractSubject> rootList = mModule.getComponentListModifiable();
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
            moduleSelectTree.removeCurrentNode();
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
        new EventEditorDialog(this, !DES_COURSE_VERSION, false);
        logEntry("New event requested");
      }

    if ("delevent".equals(e.getActionCommand())) {
      final int index = mEventList.getSelectedIndex();
      if (index >= 0) {
        final EventDeclSubject victim =
          (EventDeclSubject) mEventListModel.getElementAt(index);
        final IndexedListSubject<EventDeclSubject> events =
          mModule.getEventDeclListModifiable();
        events.remove(victim);
      }
    }

    if ("neweventparam".equals(e.getActionCommand()))
      {
        new EventEditorDialog(this, !DES_COURSE_VERSION, true);
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
                  mModule = new ModuleSubject(modName, null);
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
        if (mChanged)
        {
          int yesNo = 
            JOptionPane.showConfirmDialog(this,
                                          "Do you want to save the module before loading?",
                                          "Save before loading?",
                                          JOptionPane.YES_NO_OPTION);
          if (yesNo == JOptionPane.YES_OPTION)
            saveAs();
        }
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

    if (mModule != null)
      {
        modified = true;

        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = moduleSelectTree.getSelectionPath();

        if (parentPath == null)
          {
            //There's no selection. Default to the root node.
            parentNode = ((ModuleTree) moduleSelectTree).getRoot();

            moduleSelectTree.expandPath(new TreePath(parentNode.getPath()));
          }
        else
          {
            parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
          }

        ComponentInfo ci = (ComponentInfo) (parentNode.getUserObject());

        logEntry("addComponent: Parent: " + parentNode.toString());

        if (ci.getComponent() instanceof ForeachSubject) {
          ((ForeachSubject) ci.getComponent()).getBodyModifiable().add(o);
        } else {
          mModule.getComponentListModifiable().add(o);
        }
        if (o instanceof SimpleComponentSubject) {
          SimpleComponentSubject scp = (SimpleComponentSubject) o;
          logEntry("Adding SimpleComponentSubject: " + scp.getName());
          try {
            EditorWindow ed =
              new EditorWindow(scp.getName() + " - Waters Editor", mModule,
                               scp, this, this);
          } catch (GeometryAbsentException g) {
            // this should never happen
            g.printStackTrace();
          }
        }
        //Add node to module tree
        ((ModuleTree) moduleSelectTree).addComponent(o);
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

  public void addUndoable(UndoableEdit e)
  {
    if (e.isSignificant())
      {
        mChanged = true;
        mInsignificant.end();
        mUndoManager.addEdit(mInsignificant);
        mInsignificant = new CompoundEdit();
        mUndoManager.addEdit(e);
        fireEditorChangedEvent(new UndoRedoEvent());
      }
    else
      {
        mInsignificant.addEdit(e);
      }
  }
  
  public void showComment()
  {

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
    mChanged = true;
    mInsignificant.end();
    mInsignificant.undo();
    mInsignificant = new CompoundEdit();
    mUndoManager.redo();
    fireEditorChangedEvent(new UndoRedoEvent());
  }

  public void undo() throws CannotUndoException
  {
    mChanged = true;
    mInsignificant.end();
    mInsignificant.undo();
    mInsignificant = new CompoundEdit();
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
  //# Interface java.awt.event.WindowListener
  public void windowActivated(WindowEvent e) {}
  public void windowClosed(WindowEvent e) {}
  public void windowClosing(WindowEvent e)
  {
    if (mChanged)
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
  //# Interface net.sourceforge.waters.gui.ModuleWindowInterface
  public UndoInterface getUndoInterface()
  {
    return this;
  }

  public ModuleSubject getModuleSubject()
  {
    return mModule;
  }

  public ExpressionParser getExpressionParser()
  {
    return mExpressionParser;
  }

  public EventKind guessEventKind(final IdentifierProxy ident)
  {
    final String name = ident.getName();
    final IndexedList<EventDeclSubject> decls =
      mModule.getEventDeclListModifiable();
    final EventDeclSubject decl = decls.get(name);
    if (decl != null) {
      return decl.getKind();
    }
    final IndexedList<ParameterSubject> params =
      mModule.getParameterListModifiable();
    final ParameterSubject param = params.get(name);
    if (param != null && param instanceof EventParameterSubject) {
      final EventParameterSubject eparam = (EventParameterSubject) param;
      final EventDeclSubject edecl = eparam.getEventDecl();
      return edecl.getKind();
    }
    return null;
  }

  public Frame getRootWindow()
  {
    return this;
  }

  public EditorWindowInterface showEditor(final SimpleComponentSubject comp)
    throws GeometryAbsentException
  {
    return new EditorWindow(comp.getName(), mModule, comp, this, this);
  }

  // to be added to interface?
  public ProxyPrinter getPrinter()
  {
    return mPrinter;
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
  private ModuleSubject mModule = null;
  private boolean modified = true;

  private JFileChooser fileChooser = new JFileChooser(".");
  private JFileChooser fileSaveChooser = new JFileChooser(".");
  private JMenuItem FileNewMenu;
  private JMenuItem FileOpenMenu;
  private JMenuItem FileSaveMenu;
  private JMenuItem FileSaveAsMenu;
  private JMenuItem FileExitMenu;
  private JMenuItem analysisExportSupremicaMenu;
  private ModuleTree moduleSelectTree = null;
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

  private JList mEventList = null;
  private ListModel mEventListModel = null;
  private JList mParameterList = null;
  private ListModel mParameterListModel = null;

  private org.supremica.gui.Supremica supremica = null;
  private final ExpressionParser mExpressionParser;
  private final ProxyPrinter mPrinter;
  private final UndoManager mUndoManager = new UndoManager();
  private CompoundEdit mInsignificant = new CompoundEdit();

  private static Languages WLang;
  
  private boolean mChanged = false;

}
