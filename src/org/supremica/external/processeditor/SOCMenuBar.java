package org.supremica.external.processeditor;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import org.supremica.external.processeditor.processgraph.*;
import org.supremica.external.processeditor.processgraph.opcell.*;
import org.supremica.external.processeditor.processgraph.resrccell.*;
import org.supremica.external.processeditor.xgraph.*;
import org.supremica.external.processeditor.processgraph.opcell.AttributePanel;
import org.supremica.external.processeditor.processgraph.opcell.OperationCell;
import org.supremica.external.processeditor.processgraph.resrccell.ResourceCell;
import org.supremica.external.processeditor.xml.*;
import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processeditor.xml.Converter;
import org.supremica.external.processeditor.xgraph.GraphCell;
import org.supremica.manufacturingTables.xsd.processeditor.Relation;

/**
 * A class that handles the SOC program menubar.
 */
public class SOCMenuBar extends JMenuBar implements ActionListener,
						    MouseListener,
						    InternalFrameListener{


    protected Border unselectedBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1, getBackground()), new BevelBorder(BevelBorder.LOWERED, Color.white, Color.gray));   
    protected Border selectedBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1, Color.red), new MatteBorder(1, 1, 1, 1, getBackground()));   
    protected Border activeBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1, Color.blue), new MatteBorder(1, 1, 1, 1, getBackground()));                
    
    private JCheckBox[] typesIsVisible = new JCheckBox[0];     
    public JCheckBox[] typesToSave = new JCheckBox[0];

    protected boolean isSelected = false;    

    private ButtonGroup bgWindow = new ButtonGroup();

    private SOCGraphContainer graphContainer = null;
    private SOCToolBar toolbar = null;

    /**
     * Constructs a new menubar.
     */
    public SOCMenuBar(SOCGraphContainer graphContainer) {
	this.graphContainer = graphContainer;
	this.toolbar = graphContainer.toolbar;	
	String[] fileMenu = {"New", 
			     "Open...", 
			     "Import...", 
			     "Close",
			     "SEPARATOR", 
			     "Save", 
			     "Save As...", 
			     "Custom Save",
			     "Export...", 
			     "Export As Image...",
			     "SEPARATOR",
			     "Open DB interface",
			     "SEPARATOR",
			     "Printer Settings...", 
			     "Print", 
			     "SEPARATOR", 
			     "Exit"};		
	String[] editMenu = {"New Resource",
			     "New Operation",
			     "New Relation Type",
			     "New Algebraic...",
			     "New InterLock",
			     "New Execution of operation",
			     "SEPARATOR",
			     "Insert Resource...",
			     "SEPARATOR",
			     "Create Outer Relation",
			     "Remove Outer Relation",
			     "SEPARATOR",
			     "Cut",
			     "Copy",
			     "Paste",
			     "SEPARATOR",
			     "Delete",
			     "Relation Type"};
	String[] optionsMenu = {"Auto Positioning",
				"SEPARATOR",
				"Algebraic",
				"Sum Attribute",
				"SEPARATOR",
				"Resource Info...",
				"Operation Info..."};	
	String[] viewMenu = {"Multi Mode",
			     "Cell Size",
			     "Attributes",
			     "Automata..."};
	String[] buildMenu = {"DOP to EFA",
				  "SEPARATOR",
				  "Synthesis",
			      "Automatas",
			      "Supervisor"};
	String[] windowsMenu = {"Cascade",
				"SEPARATOR"};
	String[] helpMenu = new String[0];
	
	String[] menu = {"File", "Edit", "Options", "View", "Build", "Windows", "Help"};
	String[][] menuItems = new String[menu.length][];
	menuItems[0] = fileMenu;
	menuItems[1] = editMenu;
	menuItems[2] = optionsMenu;
	menuItems[3] = viewMenu;
	menuItems[4] = buildMenu;
	menuItems[5] = windowsMenu;
	menuItems[6] = helpMenu;
	
	for(int i = 0; i < menu.length; i++) {
	    add(createMenu(menu[i], menuItems[i]));
	}
	
	String[] menuItemEnable = {"Save", "Save As...", "Custom Save", 
				   "Export...",
				   "Relation Type", "Create Outer Relation",
				   "Remove Outer Relation", "Cut", "Copy",
				   "Paste", "Delete", "Algebraic",
				   "Resource Info...", "Operation Info...",
				   "Sum Attribute", "Attributes"};
	setEnabled(menuItemEnable, false);
	toolbar.setSaveEnabled(false);
	toolbar.setCutEnabled(false);
	toolbar.setCopyEnabled(false);
	toolbar.setPasteEnabled(false);
	toolbar.setDeleteEnabled(false);
	setAccelerator("New Operation", 'N');
	setAccelerator("New Algebraic...", 'L');
	setAccelerator("Cut", 'X');
	setAccelerator("Copy", 'C');
	setAccelerator("Paste", 'V');
	getMenuItem("Delete").setAccelerator(KeyStroke.getKeyStroke("DELETE"));
    }
    /**
     * Returns a menu.
     *
     * @param menu the name of the menu
     * @param menuItems the names of the menu items
     * @return the created menu
     */
    protected JMenu createMenu(String menu, 
				      String[] menuItems) {
	JMenu tmpMenu = new JMenu(menu);
	tmpMenu.setName(menu);
	for(int i = 0; i < menuItems.length; i++) {
	    if(menuItems[i].equals("SEPARATOR")) {
		tmpMenu.addSeparator();
	    }else {		
		tmpMenu.add(createMenuItem(menuItems[i]));
	    }
	}
	return tmpMenu;
    }
    /**
     * Returns a menu item.
     *
     * @param menuItem the name of the menu item
     * @return the created menu item    
     */
    protected JMenuItem createMenuItem(String menuItem) {	
	if(menuItem.equals("Import...")) {
	    String[] menuItems = {"eRWD", "SDP Station", "SDP Station"};
	    return createMenu(menuItem, menuItems);
	}else if(menuItem.equals("Custom Save")) {
	    JMenu jmCustomSave = new JMenu(menuItem);
	    jmCustomSave.setName(menuItem);
	    String[] menuContent = {"ROP",
				    "-Comment",
				    "Relation",
				    "-Algebraic", 
				    "--Unextended",
				    "Activity",
				    "-Precondition",
				    "-Properties",
				    "--Attribute",
				    "---Upper Indicator",
				    "---Lower Indicator",
				    "---Invisible",
				    "--Unextended"};  	
	    typesToSave = new JCheckBox[menuContent.length]; 
	    
	    int i = 0;
	    while(i < menuContent.length){	    
		String name = menuContent[i]; 
		JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
		if(name.startsWith("-")){
		    while(name.startsWith("-")){
			name = name.substring(1);
			row.add(new JLabel("     "));
		    }	      
		    row.add(typesToSave[i] = new JCheckBox(name,true));
		    typesToSave[i].addMouseListener(this);
		    typesToSave[i].setName(name);
		}
		else{
		    JLabel n = new JLabel("     "+name);
		    n.setFont(new Font("Arial", Font.BOLD, 12));
		    row.add(n);
		}	   	    
		jmCustomSave.add(row);  	   
		i++;
	    }	    
	    jmCustomSave.addSeparator();
	    JMenuItem jmiCustomSaveAs = new JMenuItem("Save As...");	      
	    jmiCustomSaveAs.setName("Custom Save.Save As...");
	    jmiCustomSaveAs.addActionListener(this);
	    jmCustomSave.add(jmiCustomSaveAs);
	    return jmCustomSave;
	}else if(menuItem.equals("New Relation Type")) {
	    String[] menuItems = {"New Sequence", "New Alternative",
				  "New Parallel", "New Arbitrary"};	    
	    return createMenu(menuItem, menuItems);       
	}else if(menuItem.equals("Algebraic")) {
	    JCheckBoxMenuItem jmiAlgebraic = 
		new JCheckBoxMenuItem(menuItem);
	    jmiAlgebraic.setName(menuItem);
	    jmiAlgebraic.addActionListener(this);
	    return jmiAlgebraic;
	}else if(menuItem.equals("Sum Attribute")) {
	    JMenu jmSumAttribute = new JMenu(menuItem);
	    jmSumAttribute.setName(menuItem);
	    return jmSumAttribute;
	}else if(menuItem.equals("Relation Type")) {
	    JMenu jmRelationType = new JMenu(menuItem);
	    jmRelationType.setName(menuItem);	    
	    JRadioButtonMenuItem jrbmiSequence = 
		new JRadioButtonMenuItem("Sequence");
	    jrbmiSequence.setName("Sequence");
	    JRadioButtonMenuItem jrbmiAlternative = 
		new JRadioButtonMenuItem("Alternative");
	    jrbmiAlternative.setName("Alternative");
	    JRadioButtonMenuItem jrbmiParallel = 
		new JRadioButtonMenuItem("Parallel");
	    jrbmiParallel.setName("Parallel");
	    JRadioButtonMenuItem jrbmiArbitrary = 
		new JRadioButtonMenuItem("Arbitrary");
	    jrbmiArbitrary.setName("Arbitrary");

	    jrbmiSequence.addActionListener(this);
	    jrbmiAlternative.addActionListener(this);
	    jrbmiParallel.addActionListener(this);
	    jrbmiArbitrary.addActionListener(this);
	    jmRelationType.add(jrbmiSequence);
	    jmRelationType.add(jrbmiAlternative);
	    jmRelationType.add(jrbmiParallel);
	    jmRelationType.add(jrbmiArbitrary);
	    ButtonGroup bgRelationType = new ButtonGroup();
	    bgRelationType.add(jrbmiSequence);
	    bgRelationType.add(jrbmiAlternative);
	    bgRelationType.add(jrbmiParallel);
	    bgRelationType.add(jrbmiArbitrary);
	    return jmRelationType;
	}else if(menuItem.equals("Multi Mode")) {
	    JCheckBoxMenuItem jmiMultiMode = 
		new JCheckBoxMenuItem(menuItem);
	    jmiMultiMode.setName(menuItem);
	    jmiMultiMode.addActionListener(this);
	    return jmiMultiMode;
	}else if(menuItem.equals("Cell Size")) {
	    String[] menuItems = {"Smaller", "Larger"};
	    return createMenu(menuItem, menuItems);
	}else if(menuItem.equals("Attributes")) {
	    JMenu jmAttributes = new JMenu(menuItem);
	    jmAttributes.setName(menuItem);
	    return jmAttributes;
	}else if(menuItem.equals("Automatas")) {
	    String[] menuItems = {"Complete Model...",
				  "Basic Model...",
				  "Simplified Model..."};
	    return createMenu(menuItem,menuItems);
	}else if(menuItem.equals("Supervisor")) {
	    String[] menuItems = {"Complete State Names...",
				  "Short State Names..."};
	    return createMenu(menuItem,menuItems);
	}else {
	    JMenuItem tmpMenuItem = new JMenuItem(menuItem);
	    tmpMenuItem.setName(menuItem);
	    tmpMenuItem.addActionListener(this);	
	    return tmpMenuItem;
	}	
    }
    /**
     * Returns the menu item named as <code>menuItem</code>. 
     * <p>
     * If there is no menu item named as <code>menuItem</code> this method
     * returns <code>null</code>.
     * 
     *
     * @param menuItem the name of the menu item that is to be returned
     * @return the menu item named as <code>menuItem</code>,
     * otherwise <code>null</code>.
     */
    public JMenuItem getMenuItem(String menuItem) {
	JMenuItem menuItemToReturn = null;
	for(int i = 0; i < getMenuCount(); i++) {
	    JMenu tmpMenu = getMenu(i);	    
	    if(tmpMenu != null) {		
		if(menuItem.equals(tmpMenu.getName())) {
		    menuItemToReturn = tmpMenu;
		}else {
		    menuItemToReturn = getMenuItem(menuItem, tmpMenu);
		}
	    }
	    if(menuItemToReturn != null) {
		break;
	    }
	}
	return menuItemToReturn;
    }
    /**
     * Returns the menu item named as <code>menuItem</code> from the 
     * specified menu.
     * <p>
     * If there is no menu item named as <code>menuItem</code> this method
     * returns <code>null</code>.
     *
     * @param menuItem the name of menu item that is to be returned
     * @param menu the menu that is to be check to include the menu item
     * @return the menu item named as <code>menuItem</code>, 
     * otherwise <code>null</code>.
     */
    protected JMenuItem getMenuItem(String menuItem, JMenu menu) {
	JMenuItem menuItemToReturn = null;
	for(int i = 0; i < menu.getItemCount(); i++) {
	    JMenuItem tmpMenuItem = menu.getItem(i);
	    if(tmpMenuItem != null) {
		if(menuItem.equals(tmpMenuItem.getName())) {
		    menuItemToReturn = tmpMenuItem;		    
		}else if(tmpMenuItem instanceof JMenu) {
		    menuItemToReturn = getMenuItem(menuItem, 
			 			   (JMenu)tmpMenuItem);
		}		
	    }	   
	    if(menuItemToReturn != null) {
		break;
	    }
	}
	return menuItemToReturn;
    }
    /**
     * Sets the menu item named as <code>menuItem</code> enabled or not.
     * 
     * @param menuItem the name of the menu item that is to be set enabled
     * @param b if <code>true</code> the menu item is set enabled, 
     * <code>false</code> otherwise.
     */
    public void setEnabled(String menuItem, boolean b) {
	JMenuItem menuItemToSet = getMenuItem(menuItem);
	if(menuItemToSet != null) {
	    menuItemToSet.setEnabled(b);
	}
    }
    /**
     * Sets the menu item named as <code>menuItem</code> selected or not.
     * 
     * @param menuItem the name of the menu item that is to be set selected
     * @param b if <code>true</code> the menu item is set selected, 
     * <code>false</code> otherwise.
     */
    public void setSelected(String menuItem, boolean b) {
	JMenuItem menuItemToSet = getMenuItem(menuItem);
	if(menuItemToSet != null) {
	    menuItemToSet.setSelected(b);
	}
    }
    /**
     * Sets the state of the checkbox menu item named as <code>menuItem</code>.
     * 
     * @param menuItem the name of the checkbox menu item that is to be 
     * set 
     * @param b sets the checbox menu item either <code>true</code> or 
     * <code>false</code>
     */
    public void setState(String menuItem, boolean b) {
	JMenuItem menuItemToSet = getMenuItem(menuItem);
	if(menuItemToSet != null && 
	   menuItemToSet instanceof JCheckBoxMenuItem) {
	   ((JCheckBoxMenuItem)menuItemToSet).setState(b);
	}
    }
    /**
     * Sets the menu items named as <code>menuItems</code> enabled or not.
     * 
     * @param menuItems the names of the menu items that is to be set enabled
     * @param b if <code>true</code> the menu items are set enabled, 
     * <code>false</code> otherwise.
     */
    public void setEnabled(String[] menuItems, boolean b) {
	for(int i = 0; i < menuItems.length; i++) {
	    setEnabled(menuItems[i], b);
	}
    }
    /**
     * Sets the menu items named as <code>menuItems</code> selected or not.
     * 
     * @param menuItems the names of the menu items that is to be set selected
     * @param b if <code>true</code> the menu items are set selected, 
     * <code>false</code> otherwise.
     */
    public void setSelected(String[] menuItems, boolean b) {
	for(int i = 0; i < menuItems.length; i++) {
	    setSelected(menuItems[i], b);
	}
    }
    /**
     * Sets the states of the checkbox menu items named as
     *  <code>menuItems</code>.
     * 
     * @param menuItems the names of the checkbox menu items that is to be 
     * set 
     * @param b sets the checbox menu items either <code>true</code> or 
     * <code>false</code>
     */
    public void setState(String[] menuItems, boolean b) {
	for(int i = 0; i < menuItems.length; i++) {
	    setState(menuItems[i], b);
	}
    }
    /**
     * Adds an accelerator to the menu item named as <code>menuItem</code>.
     *
     * @param menuItem the name of the menu item
     * @param key the key character that is going to be used as an accelerator
     */
    protected void setAccelerator(String menuItem, char key) {
	JMenuItem tmpMenuItem = getMenuItem(menuItem);
	tmpMenuItem.setAccelerator(KeyStroke.getKeyStroke((key), 
				   ActionEvent.CTRL_MASK));
    }    
    /**
     * Updates the menubar and toolbar by setting concerned items 
     * enable or not.
     */
    public void selectionChanged() {
	int selectedCount = graphContainer.getSelectedCount();
	if(selectedCount == 0) {
	    String[] menuItemsEnable = {"Custom Save", "Save", "Save As...", 
					"Export...", "Create Outer Relation",
					"Remove Outer Relation", "Cut", "Copy",
					"Delete", "Relation Type", 
					"Algebraic", "Resource Info...",
					"Operation Info...", "Attributes", 
					"Sum Attribute"};
	    String[] menuItemSelecte = {"Algebraic"};
	    setEnabled(menuItemsEnable, false);
	    setSelected(menuItemSelecte, false);
	    toolbar.setSaveEnabled(false);		  
	    toolbar.setCutEnabled(false);
	    toolbar.setCopyEnabled(false);	    
	    toolbar.setDeleteEnabled(false);
	}else if(selectedCount == 1) {	  
	    GraphCell selectedCell = graphContainer.getSelectedGraphCell();
	    if(selectedCell instanceof ResourceCell) {
		if(!((ResourceCell)selectedCell).isEmpty()) { 
		    String[] menuItemEnable = {"Custom Save", "Save", 
					       "Save As...", "Export...",
					       "Attributes", "Sum Attribute",
					       "Resource Info..."};
		    setEnabled(menuItemEnable, true);
		    toolbar.setSaveEnabled(true);	     
		    setAttributeMenu();
		}	   
	    }else if(selectedCell instanceof OperationCell) {		
		setEnabled("Operation Info", true);
	    }else if(selectedCell instanceof NestedCell) {		
		Relation relation = ((NestedCell)selectedCell).getRelation();
		if(relation != null) {		    		   
		    setEnabled("Relation Type", true);
		    setSelected(relation.getType().name(), true);		    
		    setEnabled("Algebraic", true);   
		    try {
			setSelected("Algebraic", 
				    relation.getAlgebraic().isCompressed());
		    }catch(Exception ex) {
			setSelected("Algebraic",false);
		    }		    
		}
	    }
	    if((selectedCell instanceof NestedCell)||
	       (selectedCell instanceof OperationCell)) {
		String[] menuItemEnable = {"Create Outer Relation",
					   "Remove Outer Relation",
					   "Cut", "Copy", "Delete"};	 
		setEnabled(menuItemEnable, true);
		toolbar.setCutEnabled(true);
		toolbar.setCopyEnabled(true);		
		toolbar.setDeleteEnabled(true);		
	    }
	}else {
	    String[] menuItemEnable = {"Custom Save", "Save", "Save As...",
				       "Export...", "Create Outer Relation",
				       "Remove Outer Relation", "Cut",
				       "Copy", "Delete", "Relation Type",
				       "Algebraic", "Resource Info...",
				       "Operation Info", "Attributes",
				       "Sum Attribute"};
	    setEnabled(menuItemEnable, false);
	    setSelected("Algebraic", false);
	    toolbar.setSaveEnabled(false);
	    toolbar.setCutEnabled(false);
	    toolbar.setCopyEnabled(false);	    
	    toolbar.setDeleteEnabled(false);
	}
    }       
    /**
     * Update the Menu->"Custom Save" menubar.
     * <p>
     * This method is internally invoked when a change to the 
     * Menu->"Custom Save" menubar has been performed. The method sets the
     * subelements selected or unselected depending on the change made. 
     *
     * @param index indicate to what element that has been changed
     */
    protected void setTypeToSave(int index) {
	//DEBUG
	//System.out.println("SOCGraphContainer.setTypeToSave(): "+
	//		   typesToSave[index].getName());
	//END DEBUG
	if(typesToSave[index].isEnabled()) {
	    typesToSave[index].setSelected(!typesToSave[index].isSelected());  
	    if(typesToSave[index].getName().equals("Algebraic")) {
		typesToSave[index+1].setEnabled(typesToSave[index].isSelected());  
	    }else if(typesToSave[index].getName().equals("Properties")) {
		typesToSave[index+1].setEnabled(typesToSave[index].isSelected());
		typesToSave[index+5].setEnabled(typesToSave[index].isSelected());
		if(!typesToSave[index+1].isEnabled()) {
		    typesToSave[index+2].setEnabled(false);
		    typesToSave[index+3].setEnabled(false);
		    typesToSave[index+4].setEnabled(false);
		}else {
		    typesToSave[index+2].setEnabled(typesToSave[index+1].isSelected());
		    typesToSave[index+3].setEnabled(typesToSave[index+1].isSelected());
		    typesToSave[index+4].setEnabled(typesToSave[index+1].isSelected());		}
	    }else if(typesToSave[index].getName().equals("Attribute")) {
		typesToSave[index+1].setEnabled(typesToSave[index].isSelected());
		typesToSave[index+2].setEnabled(typesToSave[index].isSelected());
		typesToSave[index+3].setEnabled(typesToSave[index].isSelected());
	    }
	}
    }
    /**
     * Update the attribute menu.
     * <p>
     * Click View->Attribute in the menubar to find the attribute menu.
     * This method is internally invoked each time a resource has 
     * been selected.
     */   
    protected void setAttributeMenu() {
	ResourceCell resrcCell = graphContainer.getSelectedResourceCell();
	if(resrcCell != null) {	    
	    try {		       
		JMenu jmAttributes = (JMenu)getMenuItem("Attributes");
		JMenu jmSumAttribute = (JMenu)getMenuItem("Sum Attribute");
		jmAttributes.removeAll();
		jmSumAttribute.removeAll();

		String[] types = resrcCell.getUniqueAttributes();
		Color[] color = resrcCell.getUniqueAttributesColor();
		int[] isVisible = resrcCell.getUniqueAttributesVisible();
		
		String[] headers = {"Attribute Type",
				    "|"+"Visible"+"|",
				    "Default color"};
		JPanel headerPanel = new JPanel();
		headerPanel.add(new JLabel(headers[0]));
		headerPanel.add(new JLabel(headers[1]));
		headerPanel.add(new JLabel(headers[2]));
		JPanel attributePanel = new JPanel();	    		      
		attributePanel.setLayout(new GridLayout(color.length, 3));    
		typesIsVisible = new JCheckBox[types.length]; 
		for(int i=0;i<color.length; i++){
		    JPanel colorPane = new JPanel();
		    colorPane.setName(types[i]);
		    colorPane.setBackground(color[i]);
		    colorPane.setBorder(unselectedBorder);
		    colorPane.addMouseListener(this);
		    
		    attributePanel.add(new JLabel(types[i]));		     
		    attributePanel.add(typesIsVisible[i] = new JCheckBox()); 
		    typesIsVisible[i].setName(types[i]);
		    typesIsVisible[i].addMouseListener(this);		   
		    typesIsVisible[i].setHorizontalAlignment(SwingConstants.
							     CENTER );	
		    if(isVisible[i] == Converter.
		       IS_VISIBLE_TRUE) {
			typesIsVisible[i].setSelected(true);
		    }else if(isVisible[i] == Converter.
			     IS_VISIBLE_FALSE) {
			typesIsVisible[i].setSelected(false);
		    }else if(isVisible[i] == Converter.
			     IS_VISIBLE_TRUE_CHANGED) {
			typesIsVisible[i].setSelected(true);
			typesIsVisible[i].setEnabled(false);
		    }else if(isVisible[i] == Converter.
			     IS_VISIBLE_FALSE_CHANGED) {
			typesIsVisible[i].setSelected(false);	   
			typesIsVisible[i].setEnabled(false);
		    }			    			    
		    
		    attributePanel.add(colorPane);
		    
		    JMenuItem jmSumAttributeType; 
		    if(types[i].equals("")) {
			jmSumAttributeType = new JMenuItem(" ");
		    }else {
			jmSumAttributeType = new JMenuItem(types[i]);
		    }
		    jmSumAttributeType.setName("SumAttribute");		   
		    jmSumAttributeType.setName("SumAttribute"+types[i]); 
		    jmSumAttributeType.addActionListener(this);
		    
		    jmSumAttribute.add(jmSumAttributeType);
		}	
		jmAttributes.add(headerPanel);
		jmAttributes.addSeparator();
		jmAttributes.add(attributePanel);	
		jmAttributes.setEnabled(true);		    	      
	    }catch(Exception ex) {
		//DEBUG
		System.out.println("ERROR in SOCMenuBar.setAttributeMenu()");
		//END DEBUG
	    }
	}
    }
    /**
     * Adds a "new frame" to the Window menu.
     *
     * @param title the title of the frame that is to be added
     */
    public void addFrame(String title) {
    	JRadioButtonMenuItem jrbmi = new JRadioButtonMenuItem(title);
    	jrbmi.addActionListener(this);
    	JMenu jmWindow = (JMenu)getMenuItem("Windows");
    	jmWindow.add(jrbmi);
    	bgWindow.add(jrbmi);
    	jrbmi.setSelected(true);	
    }
    /**
     * Is invoked when an action has occurred.
     * <p>
     * Handles the action events invoked by the menubar.
     *
     * @param e the action event
     */
    public void actionPerformed(ActionEvent e) {
    	//DEBUG
    	//System.out.println("SOCGraphContainer.actionPerformed()");
    	//END DEBUG
    	if(e.getSource() instanceof JMenuItem) {
    		if("New".equals(e.getActionCommand())) {
    			graphContainer.newSheet();	  	    	
    		}else if("Open...".equals(e.getActionCommand())) {
    			graphContainer.open();
    		}else if("eRWD".equals(e.getActionCommand())) {
    			graphContainer.importFromFile(SOCGraphContainer.ERWD);
    		}else if("SDP Station".equals(e.getActionCommand())) {
    			graphContainer.importFromFile(SOCGraphContainer.SDP_STATION);
    		}else if("SDP Database".equals(e.getActionCommand())) {
    			graphContainer.importFromFile(SOCGraphContainer.SDP_DATABASE);
    		}else if("Close".equals(e.getActionCommand())) {
    			graphContainer.close();
    		}else if("Save".equals(e.getActionCommand())) {
    			graphContainer.save();
    		}else if("Save As...".equals(e.getActionCommand())) {
    			
    			if(((JMenu)getMenuItem("Custom Save")).
    						isMenuComponent((JMenuItem)e.getSource())) {
    				graphContainer.saveWithoutSubelements();
    			}else {		   
    				graphContainer.saveAs();
    			}
    			
    		}else if("Export As Image...".equals(e.getActionCommand())) {
    			graphContainer.exportAsImage();
    		}else if("Export...".equals(e.getActionCommand())) {
    			graphContainer.export();	  	   	       
    		//}else if("Solution Extraction...".equals(e.getActionCommand())) {
    			//solutionExtraction(COMPLETE);
    		}else if("Open DB interface".equals(e.getActionCommand())) {
    			graphContainer.openDBConnection();
    		}else if("Printer Settings...".equals(e.getActionCommand())) {
    			JMenuItem tmp = getMenuItem("Custom Save.Save As...");
    		}else if("Print".equals(e.getActionCommand())) {
    			graphContainer.print();
    		}else if("Exit".equals(e.getActionCommand())) {
    			System.exit(0);
    		}else if("New Resource".equals(e.getActionCommand())) {
    			graphContainer.newResource();
    		}else if("New Operation".equals(e.getActionCommand())) { 
    			graphContainer.newOperation();
    		}else if("New Sequence".equals(e.getActionCommand())) {
    			graphContainer.newRelation(RelationType.SEQUENCE);
    		}else if("New Alternative".equals(e.getActionCommand())) {	       
    			graphContainer.newRelation(RelationType.ALTERNATIVE);
    		}else if("New Parallel".equals(e.getActionCommand())) {
    			graphContainer.newRelation(RelationType.PARALLEL);
    		}else if("New Arbitrary".equals(e.getActionCommand())) {
    			graphContainer.newRelation(RelationType.ARBITRARY);
    		}else if("New Algebraic...".equals(e.getActionCommand())) {
    			graphContainer.newAlgebraic();
    		}else if("New InterLock".equals(e.getActionCommand())) {
    			graphContainer.newInterLock();
    		}else if("New Execution of operation".equals(e.getActionCommand())) {
    			graphContainer.newExecutionOfOperation();
    		}else if("Insert Resource...".equals(e.getActionCommand())) {
    			graphContainer.insertResource();
    		}else if("Create Outer Relation".equals(e.getActionCommand())) {
    			graphContainer.createOuterRelation();
    		}else if("Remove Outer Relation".equals(e.getActionCommand())) {
    			graphContainer.removeOuterRelation();
    		}else if("Cut".equals(e.getActionCommand())) {
    			graphContainer.cut();
    		}else if("Copy".equals(e.getActionCommand())) {
    			graphContainer.copy();
    			setEnabled("Paste", true);
    		}else if("Paste".equals(e.getActionCommand())) {
    			graphContainer.paste();
    		}else if("Delete".equals(e.getActionCommand())) {
    			graphContainer.delete();
    		}else if("Sequence".equals(e.getActionCommand())) {
    			graphContainer.changeRelationType(RelationType.SEQUENCE);
    		}else if("Alternative".equals(e.getActionCommand())) {
    			graphContainer.changeRelationType(RelationType.ALTERNATIVE);
    		}else if("Parallel".equals(e.getActionCommand())) {	    
    			graphContainer.changeRelationType(RelationType.PARALLEL);
    		}else if("Arbitrary".equals(e.getActionCommand())) {
    			graphContainer.changeRelationType(RelationType.ARBITRARY);
    		}else if("Auto Positioning".equals(e.getActionCommand())) {	  
    			graphContainer.rebuildAll();
    		}else if("Algebraic".equals(e.getActionCommand())) {
    			graphContainer.
    				setAlgebraic(((JCheckBoxMenuItem)e.getSource()).
    												isSelected());
    		}else if(e.getSource() instanceof JMenuItem && 
    				((JMenuItem)e.getSource()).getName() != null &&	    
    				((JMenuItem)e.getSource()).getName().
    				startsWith("SumAttribute")) {
    				graphContainer.sumAttribute(((JMenuItem)e.getSource()).
    						getName().
    							substring(12, 
    									((JMenuItem)e.
    											getSource()).
    											getName().length()));
    		}else if("Resource Info...".equals(e.getActionCommand())) {
    			graphContainer.resourceInfo();
    		}else if("Operation Info...".equals(e.getActionCommand())) {
    			graphContainer.operationInfo();
    		}else if("Multi Mode".equals(e.getActionCommand())) {
    			graphContainer.setMultiModeView(((JCheckBoxMenuItem)e.
						 		getSource()).getState());  
    		}else if("Smallest".equals(e.getActionCommand())) {
    			AttributePanel.sizeX = 20;	    		
    			graphContainer.rebuildAll();		
    		}else if("Smaller".equals(e.getActionCommand())) {
    			AttributePanel.sizeX = 30;	    	       
    			graphContainer.rebuildAll();		
    		}else if("Small".equals(e.getActionCommand())) {
    			AttributePanel.sizeX = 40;	    		
    			graphContainer.rebuildAll();		
    		}else if("Medium".equals(e.getActionCommand())) {
    			AttributePanel.sizeX = 50;		
    			graphContainer.rebuildAll();		
    		}else if("Large".equals(e.getActionCommand())) {
    			AttributePanel.sizeX = 60;		
    			graphContainer.rebuildAll();		
    		}else if("Larger".equals(e.getActionCommand())) {
    			AttributePanel.sizeX = 70;	     
    			graphContainer.rebuildAll();	       
    		}else if("Largest".equals(e.getActionCommand())) {
    			AttributePanel.sizeX = 80;	    		
    			graphContainer.rebuildAll();	
    		}else if("Synthesis...".equals(e.getActionCommand())) {
    			graphContainer.synthesis();
    		}else if("Automata...".equals(e.getActionCommand())) {
    			graphContainer.viewAutomata();
    		}else if("Complete Model...".equals(e.getActionCommand())) {
    			graphContainer.createAutomatas(SOCGraphContainer.COMPLETE);
    		}else if("Basic Model...".equals(e.getActionCommand())) {
    			graphContainer.createAutomatas(SOCGraphContainer.BASIC);
    		}else if("Simplified Model...".equals(e.getActionCommand())) {
    			graphContainer.createAutomatas(SOCGraphContainer.SIMPLIFIED);
    		}else if("Complete State Names...".equals(e.getActionCommand())) {
    			graphContainer.generateSupervisor(SOCGraphContainer.COMPLETE);
    		}else if("Short State Names...".equals(e.getActionCommand())) {
    			graphContainer.generateSupervisor(SOCGraphContainer.SIMPLIFIED);
    		}else if("DOP to EFA".equals(e.getActionCommand())) {
    			graphContainer.viewDOPtoEFAFrame();
    		}else if("Cascade".equals(e.getActionCommand())) {
    			graphContainer.cascade();
    		}else {
		
    			JInternalFrame[] frames = graphContainer.getAllFrames();
    			for(int i = 0; i < frames.length; i++) {	
    				if(frames[i].getTitle().equals(e.getActionCommand())) {    
    					((SOCGraphFrame)frames[i]).setSelected(true);  	    
    					((SOCGraphFrame)frames[i]).setMaximum(true);
    					break;
    				}
    			}
	    }
	}
    }      
    /**
     * Is invoked when the mouse is pressed.
     * <p>
     * Is invoked when the mouse is pressed over the
     * View->Attribute->"Option panel"  menubar.
     * <i>This method is empty and not in use.</i>
     *
     * @param e the mouse event
     */
    public void mousePressed(MouseEvent e){}
    /**
     * Is invoked when the mouse is clicked.
     * <p>
     * Is invoked when the mouse is clicked over the 
     * View->Attribute->"Option panel" menubar.
     * Manages the user input to set/unset the visiblility and 
     * background colour of attribute types.
     *
     * @param e the mouse event
     */    
    public void mouseClicked(MouseEvent e){	
	//DEBUG
	//System.out.println("SOCGraphContainer.mouseClicked()");
	//END DEBUG
	if(e.getSource() instanceof JCheckBox) {	    	    
	    JCheckBox source = (JCheckBox)e.getSource();	   	       
	    for(int i = 0; i < typesToSave.length; i++) {
		if(source.equals(typesToSave[i])) {		   
		    if(source.isEnabled()) {
			source.setSelected(!source.isSelected());
		    }
		    setTypeToSave(i);
		    break;
		}		
	    }
	    ResourceCell resrcCell = graphContainer.getSelectedResourceCell();
	    if(resrcCell != null) {
		for(int i = 0; i < typesIsVisible.length; i++) {
		    if(source.equals(typesIsVisible[i])) {
			resrcCell.setAttributeTypeVisible(source.getName(), 
							  source.isSelected());
			graphContainer.getSelectedFrame().
			    getGraph().getSelection().update();	
			break;
		    }			
		}		    
	    }	
	}else if(e.getSource() instanceof JPanel) {	    	    
	    Color result = JColorChooser.showDialog((Component)e.getSource(), 
						    "Pick a Color", 
						    ((JPanel)e.getSource()).
						    getBackground());
	    if(result != null) {
		ResourceCell resrcCell = graphContainer.
		    getSelectedResourceCell();		
		resrcCell.setUniqueAttributesColor(((JPanel)e.getSource()).
						   getName(), 
						   result);
		setAttributeMenu();		    		    	    
	    }
	    getMenuItem("Attributes").doClick(10);
	}
    }    
    /**
     * Is invoked when the mouse is released.
     * <p>
     * Is invoked when the mouse is released over the
     * View->Attribute->"Option panel"  menubar.
     * <i>This method is empty and not in use.</i>
     *
     * @param e the mouse event
     */
    public void mouseReleased(MouseEvent e){}    
     /**
     * Is invoked when the mouse is entered.
     * <p>
     * Is invoked when the mouse is entered the 
     * View->Attribute->"Option panel" menubar.
     * Handles the graphical effects.
     *
     * @param e the mouse event
     */
    public void mouseEntered(MouseEvent e) {	
	if(e.getSource() instanceof JPanel) {
	    ((JPanel)e.getSource()).setBorder(activeBorder);	    
	}	
    }     
    /**
     * Is invoked when the mouse is exited.
     * <p>
     * Is invoked when the mouse is exited the
     * View->Attribute->"Option panel" menubar.
     * Handles the graphical effects.
     *
     * @param e the mouse event
     */   
    public void mouseExited(MouseEvent e) {	
	if(e.getSource() instanceof JPanel) {
	    if(isSelected) {
		((JPanel)e.getSource()).setBorder(selectedBorder);
	    }else {
		((JPanel)e.getSource()).setBorder(unselectedBorder);
	    }       
	}
    }   
     /**
     * Is invoked when a internal frame is activated.
     * <p>
     * Sets the concerned menubar and toolbar items enable or not, depending
     * on what and how many object is selcted in the activated worksheet.
     *
     * @param e the internal frame event
     */
    public void internalFrameActivated(InternalFrameEvent e) {
    	SOCGraphFrame frame = (SOCGraphFrame)e.getInternalFrame();
    	JMenu jmWindows = (JMenu)getMenuItem("Windows");
    	JCheckBoxMenuItem jmiMultiMode = 
    				(JCheckBoxMenuItem)getMenuItem("Multi Mode");
    	for(int i = 2; i < jmWindows.getItemCount(); i++) {
    		if(jmWindows.getItem(i).getText().
    				equals(e.getInternalFrame().getTitle())) {
    			jmWindows.getItem(i).setSelected(true);
    			jmiMultiMode.setState(frame.isMultiModeView());
    			break;
    		}
    	}
    	selectionChanged();
    }
    /**
     * Is invoked when a internal frame is closed.
     * <p>
     * Sets the concerned menubar and toolbar items enable or not.
     *
     * @param e the internal frame event
     */
    public void internalFrameClosed(InternalFrameEvent e) {
    	if(graphContainer.getFrameCount() == 0) {
    		setEnabled("Multi Mode", false);
    	}
    }
    /**
     * Is invoked when a internal frame is closing.
     * <p>
     * Update the Windows menubar.
     *
     * @param e the internal frame event
     */
    public void internalFrameClosing(InternalFrameEvent e) {
    	JMenu jmWindows = (JMenu)getMenuItem("Windows");
    	for(int i = 2; i < jmWindows.getItemCount(); i++) {  
    		if(jmWindows.getItem(i).getText().
    				equals(e.getInternalFrame().getTitle())) {
    			jmWindows.remove(i);
    		}
    	}		
    }
    /**
     * Is invoked when a internal frame is deactivated.
     * <p>
     * <i>This mehtod is not in use.</i>
     *
     * @param e the internal frame event
     */
    public void internalFrameDeactivated(InternalFrameEvent e) {}       
    /**
     * Is invoked when a internal frame Deiconified.
     * <p>
     * <i>This method is not in use.</i>
     *
     * @param e the internal frame event
     */
    public void internalFrameDeiconified(InternalFrameEvent e) {}       
    /**
     * Is invoked when a internal frame is iconified.
     * <p>
     * <i>This method is not in use.</i>
     *
     * @param e the internal frame event
     */
    public void internalFrameIconified(InternalFrameEvent e) {}       
    /**
     * Is invoked when a internal frame is opened.
     * <p>
     * <i>This method is not in use.</i>
     */
    public void internalFrameOpened(InternalFrameEvent e) {}
}

