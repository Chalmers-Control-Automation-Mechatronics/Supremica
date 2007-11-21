package org.supremica.external.processeditor.processgraph.opcell;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Font.*;
import java.io.*;

import org.supremica.manufacturingTables.xsd.processeditor.Properties;
import org.supremica.manufacturingTables.xsd.processeditor.Activity;
import org.supremica.manufacturingTables.xsd.processeditor.Attribute;
import org.supremica.manufacturingTables.xsd.processeditor.ObjectFactory;
import org.supremica.manufacturingTables.xsd.processeditor.OperationReferenceType;
import org.supremica.manufacturingTables.xsd.processeditor.Precondition;

/**
 * Displays the operation info window, which allow the user to edit 
 * the operation, predecessor and attribute information.
 */
public class OperationCellInfoWindow extends JDialog implements ActionListener {
    private int sizeX = 800;
    private int sizeY = 300;
    private  JTextField label;
    private JToolBar toolBar;
    private JToggleButton nameChange;
    private JButton deleteOp, ok, cancel;

    private JCheckBox checkE,checkR, checkP,checkZ,checkW,
	checkOwn,checkOther,checkExtra;
   
    private JPanel operationPanel,tablePanel, edgeOptionsChBoxes ;
    
    public String opName = "";

    public Object[] predecessorHeaders = { "Operation", "At Resource"};
    public int[] preColumnPreferWidths = {200,200};
    public int[] preColumnMinWidths = {75,75};
    public Object[][] predecessorInfo; 
    private Image noUpperIndIcon;
    private Image upperIndIcon;
    private Image noLowerIndIcon;
    private Image lowerIndIcon;
    public Object[] attributeHeaders;
    public int[] attColumnPreferWidths = {150,150,20,20,20,20,50,20};
    public int[] attColumnMinWidths = {50,50,15,15,15,15,25,15};	
    public Object[][] attributeInfo;   
    Activity operand = null;
    PredecessorWindow predecessorWindow;
    AttributeWindow attributeWindow;
    
    public static final int APPROVE_OPTION = 1;
    public static final int CANCEL_OPTION = 2;
    public static final int DELETE_OPTION = 3;
    public static final int ERROR_OPTION = 0;
    private int option = ERROR_OPTION;

    private OperationCell myOwner;

    private ObjectFactory objectFactory = new ObjectFactory();

    /** 
     * Creates a new instance of the class.
     * 
     * @param a the object that is to be edit by this info window
     * @param c the operation cell that launched this info window
     */
    public OperationCellInfoWindow(Activity a, OperationCell c)
    {	
	//DEBUG
	//System.out.println("OperationCellInfoWindow()");
	//END DEBUG	
	myOwner = c;
	operand = a;
	       	
	noUpperIndIcon = Toolkit.getDefaultToolkit().
	    getImage(OperationCellInfoWindow.class.getClass().getResource("/icons/processeditor/NoUI.gif"));
	upperIndIcon = Toolkit.getDefaultToolkit().
	    getImage(OperationCellInfoWindow.class.getClass().getResource("/icons/processeditor/UI.gif"));
	noLowerIndIcon = Toolkit.getDefaultToolkit().
	    getImage(OperationCellInfoWindow.class.getClass().getResource("/icons/processeditor/NoLI.gif"));
	lowerIndIcon = Toolkit.getDefaultToolkit().
	    getImage(OperationCellInfoWindow.class.getClass().getResource("/icons/processeditor/LI.gif"));
	
	Object[] tmpAttributeHeaders = {"Value", 
					"At Attribute Type", 
					new JLabel(new ImageIcon(noUpperIndIcon)),
					new JLabel(new ImageIcon(upperIndIcon)),
					new JLabel(new ImageIcon(noLowerIndIcon)),
					new JLabel(new ImageIcon(lowerIndIcon)),
					"Color",
					"Visible"};
	attributeHeaders = tmpAttributeHeaders;
	
	try {

	    //----- SET OPERATION -----
	    opName = operand.getOperation();
	    
	    //----- SET PRECONDITION -----
	    try {		
		predecessorInfo = new Object[operand.getPrecondition().getPredecessor().size()][predecessorHeaders.length];
		Iterator predIterator = operand.getPrecondition().getPredecessor().iterator();
		int i = 0;
		while(predIterator.hasNext()) {
		    Object tmp = predIterator.next();
		    if(tmp instanceof OperationReferenceType) {			
			predecessorInfo[i][0] = ((OperationReferenceType)tmp).getOperation();
			predecessorInfo[i++][1] = ((OperationReferenceType)tmp).getMachine();
		    }
		}
	    }catch(Exception ex) {		
		predecessorInfo = new Object[0][0];
	    }	    

	    //----- SET ATTRIBUTE -----
	    try {	       
		attributeInfo = new Object[operand.getProperties().getAttribute().size()][attributeHeaders.length];	      				
		Iterator attIterator = operand.getProperties().getAttribute().iterator();	      
		int i = 0;
		while(attIterator.hasNext()) {
		    Object tmp = attIterator.next();
		    if(tmp instanceof Attribute) {
			attributeInfo[i][0] = ((Attribute)tmp).getAttributeValue();
			attributeInfo[i][1] = ((Attribute)tmp).getType();			
			try {			  
			    attributeInfo[i][3] = ((Attribute)tmp).getUpperIndicator().isIndicatorValue();	
			    attributeInfo[i][2] = false;
			}catch(Exception ex) {			  			        
			    attributeInfo[i][2] = true;
			    attributeInfo[i][3] = false;
			}
			try {
			    attributeInfo[i][5] = ((Attribute)tmp).getLowerIndicator().isIndicatorValue();			    
			    attributeInfo[i][4] = false;
			}catch(Exception ex) {			    
			    attributeInfo[i][4] = true;
			    attributeInfo[i][5] = false;
			}
			//------ SET COLOR FOR THIS ATTRIBUTE ------
			int index = -1;
			for(int j = 0; j < myOwner.uniqueAttributes.length; j++) {
			    if(((Attribute)tmp).getType().equals(myOwner.uniqueAttributes[j])) {
				index = j;
				break;
			    }
			}
			if(index != -1) {
			    attributeInfo[i][6] = myOwner.uniqueAttributesColor[index];			
			}else {
			    attributeInfo[i][6] = Color.white;
			}

			//----- SET VISIBLE FOR THIS ATTRIBUTE ------
			try {
			    attributeInfo[i][7] = !((Attribute)tmp).isInvisible();
			}catch(Exception ex) {
			    attributeInfo[i][7] = false;
			}

			i++;
		    }
		}		
	    }catch(Exception ex) {
		//DEBUG
		//System.out.println("ERROR! in OperationCellInfoWindow() while setting attributes");
		//END DEBUG
		attributeInfo = new Object[0][0];
	    }
	    try {
		description.setText(((Activity)operand).getOperation());
	    }catch(Exception ex) {
		//DEBUG
		System.out.println("ERROR! in OperationCellInfoWindow() while setting description");
		//END DEBUG
		description.setText("");
	    }	
	}catch(Exception ex) {	   
	    opName = "";
	    predecessorInfo = new Object[0][0];
	    attributeInfo = new Object[0][0];
	}	       	

	setLayout( new BorderLayout());
	operationPanel();
	createToolBar();	

	JPanel west = new JPanel(new BorderLayout());
	west.add(operationPanel,BorderLayout.CENTER);
	//*************************NEW CODE************************************
	descriptionPanel();
	west.add(descriptionPanel,BorderLayout.SOUTH);
	//*************************END NEW CODE*******************************	

	predecessorWindow = new PredecessorWindow();
	predecessorWindow.setBorder(new TitledBorder(" Predecessors "));

	attributeWindow = new AttributeWindow();
	attributeWindow.setBorder(new TitledBorder(" Attributes "));
	
	tablePanel = new JPanel();
	tablePanel.setLayout(new BoxLayout(tablePanel,BoxLayout.Y_AXIS));
	tablePanel.add(predecessorWindow);
	tablePanel.add(attributeWindow);
	tablePanel.setMinimumSize(new Dimension(sizeX/3,sizeY));		
	
	add(BorderLayout.SOUTH, toolBar);	
	add(BorderLayout.WEST, west);
	add(BorderLayout.CENTER,tablePanel );
      	
	setSize(sizeX, sizeY);    		
	setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-sizeX)/2,
		    (Toolkit.getDefaultToolkit().getScreenSize().height-sizeY)/2);
	setModal(true);
    }     
    private void operationPanel()
    {
	operationPanel = new JPanel(new BorderLayout());
	operationPanel.setBorder(new TitledBorder("  Operation  "));
		
	label = new JTextField(10);
	label.setText(opName);
	label.setHorizontalAlignment(JTextField.LEFT);
	label.setFont(new Font("Serif", Font.BOLD, sizeX/20));
	label.setEditable(false);

	JPanel south = new JPanel(new FlowLayout());
	south.add(nameChange = new JToggleButton("Change name"));
	nameChange.addActionListener(this);
	south.add(deleteOp = new JButton("Delete Operation"));
	deleteOp.addActionListener(this);
	
	operationPanel.add(label,BorderLayout.WEST);
	operationPanel.add(south, BorderLayout.SOUTH);
    }
    //************************NEW CODE*********************************
    private JPanel descriptionPanel;
    private JTextArea description = new JTextArea(3,10);
    private JScrollPane descriptionScrollPane;
    private JToggleButton modifyDescription;
    private JButton clearDescription;

    private void descriptionPanel()
    {
	descriptionPanel = new JPanel(new BorderLayout());
	descriptionPanel.setBorder(new TitledBorder("  Description  "));	
	description.setFont(new Font("Serif", Font.BOLD, sizeX/60));
	description.setEditable(false);	
	descriptionScrollPane = new JScrollPane(description);
	
	JPanel south = new JPanel(new FlowLayout());
	south.add(modifyDescription = new JToggleButton("Change Description"));
	modifyDescription.addActionListener(this);
	//south.add(clearDescription = new JButton("  Clear  "));
	//clearDescription.addActionListener(this);


	descriptionPanel.add(descriptionScrollPane,BorderLayout.CENTER);
	descriptionPanel.add(south,BorderLayout.SOUTH);
    }   
    //**********************END NEW CODE ********************************     
    private void createToolBar()
    {
        toolBar = new JToolBar();
	toolBar.setFloatable(false);
	toolBar.add(ok = new JButton("OK"));
	toolBar.addSeparator();
	toolBar.add(cancel = new JButton("Cancel"));    
	ok.addActionListener(this);
	cancel.addActionListener(this);
    }
    /**
     * Popup the info window and returns the window state on popdown.
     *
     * @return the return state of this info window on popdown:
     * <ul>
     * <li>OperationCellInfoWindow.APPROVE_OPTION</li>
     * <li>OperationCellInfoWindow.CANCEL_OPTION</li>
     * <li>OperationCellInfoWindow.DELETE_OPTION</li>
     * <li>OperationCellInfoWIndow.ERROR_OPTION</li>
     * </ul>
     */
    public int showDialog() {
	show();
	return option;
    }
    private Activity getActivity() {
    	return operand;
    }
    /**
     * Is invoked when an action has occured.
     */
    public void actionPerformed(ActionEvent e)
    {
	if (e.getSource()instanceof JToggleButton)
	    {
		if("Modify".equals(e.getActionCommand())){
		    if(modifyDescription.isSelected())
			description.setEditable(true);
		    /*
		    else if(!modifyDescription.isSelected())
			description.setEditable(false); 
		    descriptionScrollPane.validate();
		    **/
		}
		else if("Change Description".equals(e.getActionCommand())) {
		    if (modifyDescription.isSelected())
			description.setEditable(true);
		    else if (!modifyDescription.isSelected())
			description.setEditable(false);
		}		
		else if("Change name".equals(e.getActionCommand())){
		    if (nameChange.isSelected())					    
			label.setEditable(true);			
		    else if (!nameChange.isSelected())					       
			label.setEditable(false);
		}
	    }
	else if(e.getSource() instanceof JButton) 
	    {
		if("OK".equals(e.getActionCommand())) {	    		    
		    operand.setOperation(label.getText());    		       
		    operand.setPrecondition(predecessorWindow.getPrecondition());  
		    operand.setProperties(attributeWindow.getProperties());    
		    try {
			if(!description.getText().equals("")) {
			    operand.setDescription(description.getText());
			}else {
			    operand.setDescription(null);
			}
		    }catch(Exception ex) {}					   
		    option = APPROVE_OPTION;
		    dispose();
		}else if("Cancel".equals(e.getActionCommand())) {	    
		    option = CANCEL_OPTION;
		    dispose();
		}else if("Delete Operation".equals(e.getActionCommand())) {
		    option = DELETE_OPTION;
		    dispose();
		}
		/*
		else if("Clear".equals(e.getActionCommand())){
		    //decription.selectAll();
		    //description.cut();
		    description.setText("");			    
		}
		**/
	    }
    }
	//***************************END NEW CODE *********************************
    public class PredecessorWindow extends JPanel implements ActionListener
    {
    	private JButton add = new JButton("Add");
    	private JButton modify = new JButton("Modify");
    	private JButton remove = new JButton("Remove");
    	private int xSize = 100;
    	private int ySize = 100;
    	private DefaultTableModel model;
    	private JTable table ;
	
    	public PredecessorWindow()
    	{	   
    		setLayout(new BorderLayout());	    	    
    		JPanel buttonPanel = new JPanel();
    		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
    		
    		add.addActionListener(this);
    		modify.addActionListener(this);	
    		remove.addActionListener(this); 
	    
    		buttonPanel.add(add);
    		buttonPanel.add(Box.createRigidArea(new Dimension(1,10)));
    		buttonPanel.add(modify);
    		buttonPanel.add(Box.createRigidArea(new Dimension(1,10)));
    		buttonPanel.add(remove);	    	    	   
	    
    		model = new DefaultTableModel(predecessorInfo, predecessorHeaders);	   
    		table = new JTable(model);	    
    		table.setShowHorizontalLines(true);  
    		table.setShowVerticalLines(true); 
    		table.setGridColor(Color.lightGray);
    		table.setSize(((xSize*4)/5),ySize);
    		table.setRowHeight(20);
	    
    		Object colModel = table.getColumnModel();
	    
    		//Setting column widths
    		for(int i = 0;i<((DefaultTableColumnModel)colModel).getColumnCount()-1;i++)
    		{
    			((DefaultTableColumnModel)colModel).getColumn(i).
    						setPreferredWidth(preColumnPreferWidths[i]);
    			((DefaultTableColumnModel)colModel).getColumn(i).
    						setMinWidth(preColumnMinWidths[i]);
    		}	  
	    	    
    		JScrollPane scrollPane = new JScrollPane(table);	    	    	    
	    
    		add(scrollPane, BorderLayout.CENTER);
    		add(buttonPanel, BorderLayout.EAST);	    
    		setSize(xSize, ySize);	   
    	}
	
    	public Precondition getPrecondition()
    	{	    
    		try
    		{
    			Precondition newPrecondition = null;
    			for(int i = 0; i < model.getRowCount(); i++)
    			{
    				if(i == 0)
    				{
    					//newPrecondition = new PreconditionImpl();
    					newPrecondition = objectFactory.createPrecondition();
    				}
    				try
    				{
    					//OperationReferenceType newOpRef = new OperationReferenceTypeImpl();		    
    					OperationReferenceType newOpRef = objectFactory.createOperationReferenceType();
    					newOpRef.setOperation((String)model.getValueAt(i,0));
    					newOpRef.setMachine((String)model.getValueAt(i,1));
    					newPrecondition.getPredecessor().add(newOpRef);
    				}catch(Exception ex) {}
    			}
    			return newPrecondition;
    		}
    		catch(Exception ex)
    		{
    			return null;
    		}	    
    	}
    	
    	public void actionPerformed(ActionEvent e)
    	{
    		if( e.getSource() == add)
    		{
    			String newData = JOptionPane.showInputDialog(this, 
						      	 "Enter Name @ Resource");
    			if(newData != null)
    			{
    				String tmpName = "";
    				String tmpResource = "";
			    
    				try {
    					tmpName = newData.substring(0,newData.indexOf("@"));
    					tmpResource = newData.substring(newData.indexOf("@")+1,
    													newData.length());
    				}catch(Exception ex){
    					tmpName = newData;
    				}
			    
    				Object[] newRowData = {tmpName,tmpResource,""};
    				model.addRow(newRowData);
    			}
    			else{table.clearSelection();}
    		}
    		else if( e.getSource() == modify)
    		{		    
    			if(table.getSelectedRow() != -1)
    			{
    				Object t = model.getValueAt(table.getSelectedRow(),0)+"@"+
    				model.getValueAt(table.getSelectedRow(),1);		     
    				String inData = JOptionPane.showInputDialog(this, 
														"Enter Name",(Object)t );
    				if(inData != null)
    				{
    					String tmpName = "";
    					String tmpResource = "";
    					try {
    						tmpName = inData.substring(0, inData.indexOf("@"));
    						tmpResource = inData.substring(inData.indexOf("@")+1, 
    								inData.length());
    					}catch(Exception ex) {
    						tmpName = inData;
    					}
    					model.setValueAt(tmpName,
    							table.getSelectedRow(),0);
    					model.setValueAt(tmpResource,
    							table.getSelectedRow(),1);
    				}
    				else{table.clearSelection();}
    			}
    			else
    			{
    				JOptionPane.showMessageDialog(this,
    							"You must select a row",
    							"Modify",
    							JOptionPane.ERROR_MESSAGE);
    			} 
    		}
    		else if( e.getSource() == remove)
    		{		    
    			if(table.getSelectedRow() != -1)
    			{		       
    				model.removeRow(table.getSelectedRow())	;
    			}
    			else
    			{
    				JOptionPane.showMessageDialog(this,
							  "You must select a row",
							  "Remove",
							  JOptionPane.ERROR_MESSAGE);
    			}
    		}
    	}
    }
    
    public class AttributeWindow extends JPanel implements ActionListener
    {
	private JButton add=new JButton("Add"),
	    modify=new JButton("Modify"),
	    remove=new JButton("Remove");
	private AttributeTableModel model = new AttributeTableModel();
	private JTable table;

	public AttributeWindow() 
	{	    
	    setLayout(new BorderLayout());	    
	    table = new JTable(model);	    
	    table.setPreferredScrollableViewportSize(new Dimension(sizeX/2,sizeY/2));
	    Object colModel = table.getColumnModel(); 

	    //Setting column widhts AND Re-Renderers the Headers of Indicators
	    for(int i = 0;i<((DefaultTableColumnModel)colModel).getColumnCount()-1;i++)	{
		((DefaultTableColumnModel)colModel).getColumn(i).
		    setPreferredWidth(attColumnPreferWidths[i]);
		((DefaultTableColumnModel)colModel).getColumn(i).
		    setMinWidth(attColumnMinWidths[i]);
		if(i==2 || i==3 || i==4 || i==5 )
		    {	
			TableColumn column = ((DefaultTableColumnModel)colModel).getColumn(i);
			TableCellRenderer renderer = new JComponentTableCellRenderer();
			column.setHeaderRenderer(renderer);
			((JLabel)attributeHeaders[i]).setBorder(new SoftBevelBorder(
									BevelBorder.RAISED));
			column.setHeaderValue(attributeHeaders[i]);
		    }
	    }	    	   
	    JScrollPane scrollPane = new JScrollPane(table);
	    	    
	    table.setDefaultRenderer(Color.class,
				     new ColorRenderer(true));	    

	    table.setDefaultRenderer(Boolean.class, new CheckBoxRenderer(true));	      	 

	    table.setDefaultEditor(Color.class,
				   new AttributeColorChanger());

	    JPanel buttonPanel = new JPanel();
	    buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
	    add.addActionListener(this);
	    modify.addActionListener(this);	
	    remove.addActionListener(this); 
	    
	    buttonPanel.add(add);
	    buttonPanel.add(Box.createRigidArea(new Dimension(1,10)));
	    buttonPanel.add(modify);
	    buttonPanel.add(Box.createRigidArea(new Dimension(1,10)));
	    buttonPanel.add(remove);
	    	    
	    add(scrollPane, BorderLayout.CENTER);
	    add(buttonPanel, BorderLayout.EAST);
	}

	public Properties getProperties() {	    
	    try{
		Properties newProperties = null;
		for(int i = 0; i < model.getRowCount(); i++) {
		    if(i == 0) {
			//newProperties = new PropertiesImpl();
			newProperties = objectFactory.createProperties();
		    }
		    try{			
			//Attribute newAttribute = new AttributeImpl();    
			Attribute newAttribute = objectFactory.createAttribute();
			newAttribute.setType((String)model.getValueAt(i,1));
			newAttribute.setAttributeValue((String)model.getValueAt(i,0));			
			if(!(Boolean)model.getValueAt(i,2)) {
			    //newAttribute.setUpperIndicator(new UpperIndicatorImpl());
			    newAttribute.setUpperIndicator(objectFactory.createUpperIndicator());
			    newAttribute.getUpperIndicator().setIndicatorValue((Boolean)model.getValueAt(i,3));
			}		       
			if(!(Boolean)model.getValueAt(i,4)) {
			    //newAttribute.setLowerIndicator(new LowerIndicatorImpl());
			    newAttribute.setLowerIndicator(objectFactory.createLowerIndicator());
			    newAttribute.getLowerIndicator().setIndicatorValue((Boolean)model.getValueAt(i,5));
			}			
			newAttribute.setInvisible(!(Boolean)model.getValueAt(i,7));
			newProperties.getAttribute().add(newAttribute);
		    }catch(Exception ex) {}
		}
		return newProperties;
	    }catch(Exception ex) {
		return null;
	    }	    		
	}
	public void actionPerformed(ActionEvent e)
	{
	    if( e.getSource() == add) {
		String inData = JOptionPane.showInputDialog(this, 
							     "Enter Name @ Attribute Type");
		if(inData != null){			   
		    String tmpValue = "";
		    String tmpType = "";
		    try {
			tmpValue = inData.substring(0, inData.indexOf("@"));
			tmpType = inData.substring(inData.indexOf("@")+1, 
						   inData.length());
		    }catch(Exception ex) {
			tmpValue = inData;
		    }
		    Object[] newRowData = {tmpValue,
					   tmpType,
					   true,
					   false,
					   true,
					   false,
					   Color.white,
					   true};
		    model.addRow(newRowData);			   		    
		}
		else{table.clearSelection();}
	    }
	    else if( e.getSource() == modify){		
		if(table.getSelectedRow() != -1) {		    
		    Object t = model.getValueAt(table.getSelectedRow(),0)+"@"+
			model.getValueAt(table.getSelectedRow(),1);		     
		    String inData = JOptionPane.showInputDialog(this, 
								"Enter Name",
								(Object)t );		    
		    if(inData != null){			   
			String tmpValue = "";
			String tmpType = "";
			try {
			    tmpValue = inData.substring(0, inData.indexOf("@"));
			    tmpType = inData.substring(inData.indexOf("@")+1, 
						       inData.length());
			}catch(Exception ex) {
			    tmpValue = inData;
			}
			model.setValueAt(tmpValue,
					 table.getSelectedRow(),0);		      	    
			model.setValueAt(tmpType,
					 table.getSelectedRow(), 1);
		    }
		    else{
			table.clearSelection();
		    }
		}
		else{ JOptionPane.showMessageDialog(this,
						    "You must select a row",
						    "Modify",
						    JOptionPane.ERROR_MESSAGE);
		
		}		    
	    }
	    else if( e.getSource() == remove)
		{		   
		    if(table.getSelectedRow() != -1) {		       
			model.removeRow(table.getSelectedRow());
		    }
		    else { JOptionPane.showMessageDialog(this,
							 "You must select a row",
							 "Remove",
							 JOptionPane.ERROR_MESSAGE);
		    }
		}
	}   		
    class JComponentTableCellRenderer extends DefaultTableCellRenderer 
	implements TableCellRenderer
    {
	public Component getTableCellRendererComponent(JTable table, Object value,
						       boolean isSelected,boolean hasFocus,
						       int row, int column)
	{
	    return (JComponent) value;
	}
    }    
    class AttributeTableModel extends AbstractTableModel
    {
	//Overwrights some of the methods in parentclass AbstractTableModel
	public void addRow(Object[] newRow)
	{
	    Object[][] temp = new Object[attributeInfo.length+1][attributeHeaders.length];
	    for(int i=0;i< attributeInfo.length;i++)
		{		   
			System.arraycopy(attributeInfo[i],0,temp[i],0,
					 attributeInfo[i].length);		
		}
	    System.arraycopy(newRow,0,temp[temp.length-1],0,newRow.length);
	    attributeInfo = temp;	 	    
	  
	    fireTableRowsInserted(getRowCount(),getRowCount()+1); 

	}
	public int getColumnCount() {
	    return attributeHeaders.length;
	}
	
	public int getRowCount() {
	    return attributeInfo.length;
	}
	
	public String getColumnName(int col)
	{
	    if(col != 2 && col != 3 && col != 4 && col != 5){
		return (String)attributeHeaders[col];
	    } else { 
		return "";
	    }
	}	
	public Object getValueAt(int row, int col) {
	    return attributeInfo[row][col];
	}	    		 
	public Class getColumnClass(int c)
	{
	    /*
	    try {	
		if(c == 2 || c == 3 || c == 5) {		
		    return Boolean.class;
		}else {
		    return getValueAt(0, c).getClass();		
		}
	    }catch(Exception ex) {
		return null;
	    }
	    **/
	    return getValueAt(0, c).getClass();
	    
	}	    	
	public boolean isCellEditable(int row, int col) 
	{		    
	    if (col == 6) {
		return false;
	    } else {
		return true;
	    }
	}	
	public void removeRow(int rowNr)
	{
	    Object[][] temp = new Object[attributeInfo.length-1][attributeHeaders.length];
	    int j = 0;
	    for(int i=0;i< attributeInfo.length;i++)
		{
		    if(i!=rowNr){
			System.arraycopy(attributeInfo[i],0,temp[j],0,
					 attributeInfo[i].length);
			j++;		
		    }
		}
	    attributeInfo = temp;	 
	    fireTableRowsDeleted(rowNr,rowNr);	     
	}	    
	public void setValueAt(Object value, int row, int col)
	{	    	    
	    attributeInfo[row][col] = value;
	    fireTableCellUpdated(row, col);	    	    
	}		
    }        
    class ColorRenderer extends JLabel implements TableCellRenderer
    {
	Border unselectedBorder = null;
	Border selectedBorder = null;
	boolean hasBorder = true;
	
	public ColorRenderer(boolean s)
	{
	    this.hasBorder = s ;
	    setOpaque(true); 
	}	
	public Component getTableCellRendererComponent(
						       JTable table, Object color,
						       boolean isSelected, 
						       boolean hasFocus,
						       int row, int column)
	{
	    Color newColor = (Color)color;
	    setBackground(newColor);
	    if (hasBorder)
		{
		    if (isSelected)
			{
			    if (selectedBorder == null)
				{
				    selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
							         table.getSelectionBackground());
				}
			    setBorder(selectedBorder);
			} 
		    else
			{
			    if (unselectedBorder == null)
				{
				    unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
								        table.getBackground());
				}
			    setBorder(unselectedBorder);
			}
		}	    	    
	    return this;
	}
    }     
    class CheckBoxRenderer extends JCheckBox implements TableCellRenderer
    {
	private Color selectedBackground;
	private Color unSelectedBackground;
	private boolean isDefined = false;		
	private boolean firstTime = true;	

	public CheckBoxRenderer(boolean s)
	{		        
	    this.setOpaque(true);
	    this.setHorizontalAlignment(SwingConstants.CENTER); 	    
	}
	public Component getTableCellRendererComponent(
						       JTable table, Object value,
						       boolean isSelected, 
						       boolean hasFocus,
						       int row, int column)
	{		    	   
	    if(column == 2) {
		this.setSelected((Boolean)value);
		this.setEnabled(true);
		table.setValueAt(table.getValueAt(row,3),row,3);
	    }else if(column == 3) {
		this.setSelected((Boolean)value);
		this.setEnabled(!(Boolean)table.getValueAt(row,2));
	    }else if(column == 4) {
		this.setSelected((Boolean)value);
		this.setEnabled(true);
		table.setValueAt(table.getValueAt(row,5),row,5);
	    }else if(column == 5) {
		this.setSelected((Boolean)value);
		this.setEnabled(!(Boolean)table.getValueAt(row,4));
	    }else {
		this.setSelected((Boolean)value);
		this.setEnabled(true);
	    }	   	    
	    return this;	    	    		  
	}
    } 
    class AttributeColorChanger extends AbstractCellEditor implements TableCellEditor,
							 ActionListener
    {
	Color presentColor;
	JButton button;
	JColorChooser colorChooser;
	JDialog dialog;
	protected static final String EDIT = "edit";
	
	public AttributeColorChanger()
	{	    
	    button = new JButton();
	    button.setActionCommand(EDIT);
	    button.addActionListener(this);
	    button.setBorderPainted(false);
	    
	    colorChooser = new JColorChooser();
	    dialog = JColorChooser.createDialog(button,
						"Pick a Color",
						true, colorChooser,
						this, null); 
	}
	public void actionPerformed(ActionEvent e)
	{
	    if (EDIT.equals(e.getActionCommand()))
		{
		    //The user has clicked the cell, so
		    //bring up the dialog.
		    button.setBackground(presentColor);
		    colorChooser.setColor(presentColor);
		    dialog.setVisible(true);
		    
		    //Make the renderer reappear.
		    fireEditingStopped();
		    
		} 
	    else 
		{ //User pressed dialog's "OK" button.
		    presentColor = colorChooser.getColor();
		}
	}
	
	//Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue()
	{
	    return presentColor;
	}
	
	//Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(JTable table,
						     Object value,
						     boolean isSelected,
						     int row,
						     int column)
	{
	    presentColor = (Color)value;
	    return button;
	}
    }	
    }
}
