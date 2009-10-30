package org.supremica.external.processeditor.processgraph.ilcell;


import static org.supremica.external.avocades.xml.SpecificationSynthesXML.EVENT_IL;
import static org.supremica.external.avocades.xml.SpecificationSynthesXML.OPERATION_IL;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.supremica.external.processeditor.processgraph.table.TextInputPane;
import org.supremica.external.processeditor.xml.Loader;
import org.supremica.manufacturingTables.xsd.il.IL;
import org.supremica.manufacturingTables.xsd.il.ILStructure;
import org.supremica.manufacturingTables.xsd.il.ObjectFactory;


/**
 * Displays the operation info window, which allow the user to edit 
 * the operation, predecessor and attribute information.
 */
public class ILInfoWindow
						extends 
							JFrame 
						implements 
							ActionListener 
{ 
	private static final long serialVersionUID = 1L;

	private static final String TITLE = "InterLock";
	
    private JButton jbOk = null;
    private JButton jbCancel = null;
    private JButton jbCondition = null;
    
    private TextInputPane textInputPane = null;
    
    private JPanel topPanel = null;
    private JPanel bottomPanel = null;
    
    private ILStructureGroupPane tableGroup = null;
    
    private IL il = null;
    private InterLockCell ilCell = null;
    
    private static final String ID = "Id:";
    private static final String COMMENT = "Comment:";
    private static final String ACTUATOR = "Actuator:";
    private static final String OPERATION = "Operation:";
    
    private JMenuItem jmiSave = null;
    private JMenuItem jmiSaveAs = null;
    private JMenuItem jmiOpen = null;
    private JMenuItem jmiExit = null;
    
    private JCheckBoxMenuItem jcbmiShowInt = null;
    private JCheckBoxMenuItem jcbmiShowExt = null;
    private JCheckBoxMenuItem jcbmiShowOperation = null;
    private JCheckBoxMenuItem jcbmiShowZone = null;
    private JCheckBoxMenuItem jcbmiShowMode = null;
    private JCheckBoxMenuItem jcbmiShowProduct = null;
    private JCheckBoxMenuItem jcbmiShowRowHeader = null;

    private JCheckBoxMenuItem jcbmiOperationType = null;
    private JCheckBoxMenuItem jcbmiEventType = null;
    
    private File file = null; 
    private JFileChooser fc = null;
    
    /** 
     * Creates a new instance of the class.
     * 
     * @param a InterLockCell that is to be edit by this info window
     */
    public ILInfoWindow( InterLockCell ilCell ){
    	this( ilCell.getIL() );
    	this.ilCell = ilCell;
    	
    	file = ilCell.getFile();
    }
    
    /** 
     * Creates a new instance of the class.
     * 
     * @param a the object that is to be edit by this info window
     */
    public ILInfoWindow(IL il){
    	super( TITLE );
    	
    	setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    	getContentPane().setLayout( new BorderLayout() );
    	
    	//display error message
    	if( il == null ){
    		getContentPane().add( new JLabel( "Error! No InterLock" ),
    				              BorderLayout.CENTER );
    		getContentPane().add( jbCancel = new JButton( "Cancel" ), 
    				              BorderLayout.PAGE_END );
    		
    		jbCancel.addActionListener( this );
    		
    		pack();
    		
    		setLocation((Toolkit.getDefaultToolkit().
    				                    getScreenSize().width-getWidth())/2,
       		            (Toolkit.getDefaultToolkit().
       		            		        getScreenSize().height-getHeight())/2);
    		
    		return;
    	}
    	
    	this.il = il;
    	
    	bottomPanel = new JPanel();
    	bottomPanel.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
    	
    	tableGroup = new ILStructureGroupPane( il.getILStructure() );
    	
    	bottomPanel.add( jbCondition = new JButton( "New condition" ) );
    	bottomPanel.add( jbOk = new JButton( "OK" ) ); 	    	    	    	    
    	bottomPanel.add( jbCancel = new JButton( "Cancel" ) );	    
    	
    	
    	jbOk.addActionListener( this );
    	jbCancel.addActionListener( this );
    	jbCondition.addActionListener( this );
    	
    	textInputPane = new TextInputPane( null, new String[]{ID,
    			                                              COMMENT,
    			                                              ACTUATOR,
    			                                              OPERATION} );
    	
    	textInputPane.setText( ID, il.getId() );
    	textInputPane.setText( COMMENT, il.getComment() );
    	textInputPane.setText( ACTUATOR, il.getActuator() );
    	textInputPane.setText( OPERATION, il.getOperation() );
    	
    	topPanel = new JPanel();
    	topPanel.setLayout( new BorderLayout() );
    	topPanel.setBorder( BorderFactory.
    			                      createTitledBorder( "InterLock data" ) );
    	topPanel.add( textInputPane, BorderLayout.LINE_START );
    	
    	initMenu();
    	
    	//sync tableGroup with the menu
    	tableGroup.showInternalTable( jcbmiShowInt.isSelected() );
    	tableGroup.showExternalTable( jcbmiShowExt.isSelected() );
    	tableGroup.showOperationTable( jcbmiShowOperation.isSelected());
    	tableGroup.showZoneTable( jcbmiShowZone.isSelected() );
    	tableGroup.showModeTable( jcbmiShowMode.isSelected() );
    	tableGroup.showProductTable( jcbmiShowProduct.isSelected() );
    	tableGroup.setRowHeaderVisible( jcbmiShowRowHeader.isSelected() );
    	
    	//sync type menu
    	jcbmiEventType.setSelected( EVENT_IL.equals( il.getType() ) );
    	jcbmiOperationType.setSelected( OPERATION_IL.equals( il.getType() ) );
    	
    	
    	getContentPane().add( topPanel, BorderLayout.PAGE_START );
    	getContentPane().add( tableGroup, BorderLayout.CENTER );
    	getContentPane().add( bottomPanel, BorderLayout.PAGE_END );
    	
    	pack();
    	
    	setLocation( (Toolkit.getDefaultToolkit().
    			              getScreenSize().width-getWidth())/2,
    		         (Toolkit.getDefaultToolkit().
    		        		  getScreenSize().height-getHeight())/2 );
    }
    
    protected void initMenu(){
    	
    	//menu bar
    	JMenuBar menuBar = new JMenuBar();
    	
    	//---------------------------------------------------------------------
    	//    File menu
    	//---------------------------------------------------------------------
    	JMenu fileMenu = new JMenu( "File" );
    	
    	jmiSave = new JMenuItem( "Save" );
    	jmiSaveAs = new JMenuItem( "Save as" );
    	jmiOpen = new JMenuItem( "Open" );
    	jmiExit = new JMenuItem( "Exit" );
    	
    	jmiSave.setAccelerator( KeyStroke.
    			                    getKeyStroke('S', ActionEvent.CTRL_MASK) );
    	
    	jmiSave.addActionListener( this );
    	jmiSaveAs.addActionListener( this );
    	jmiOpen.addActionListener( this );
    	jmiExit.addActionListener( this );
    	
    	fileMenu.add( jmiSave );
    	fileMenu.add( jmiSaveAs );
    	fileMenu.add( jmiOpen );
    	
    	fileMenu.addSeparator();
    	
    	fileMenu.add( jmiExit );
    	
    	menuBar.add( fileMenu );
    	
    	//---------------------------------------------------------------------
    	//    Table menu
    	//---------------------------------------------------------------------
    	JMenu tableMenu = new JMenu( "Table" );
    	JMenu showMenu = new JMenu( "Show" );
    	tableMenu.add(showMenu);
    	menuBar.add(tableMenu);
    	
    	jcbmiShowInt = new JCheckBoxMenuItem( "Internal components" );
    	jcbmiShowExt = new JCheckBoxMenuItem( "External components" );
    	
    	jcbmiShowOperation = new JCheckBoxMenuItem( "Operation" );
    	jcbmiShowZone = new JCheckBoxMenuItem( "Zone" );
    	
    	jcbmiShowMode = new JCheckBoxMenuItem( "Mode" );
    	jcbmiShowProduct = new JCheckBoxMenuItem( "Product" );
    	
    	jcbmiShowRowHeader = new JCheckBoxMenuItem( "Row header" );
    	
    	
    	jcbmiShowInt.setSelected( true );
    	jcbmiShowExt.setSelected( true );
    	jcbmiShowZone.setSelected( true );
    	jcbmiShowOperation.setSelected( true );
    	
    	jcbmiShowMode.setSelected( false );
    	jcbmiShowProduct.setSelected( false );
    	
    	jcbmiShowRowHeader.setSelected( false );
    	
    	jcbmiShowInt.addActionListener( this );
    	jcbmiShowExt.addActionListener( this );
    	jcbmiShowOperation.addActionListener( this );
    	jcbmiShowZone.addActionListener( this );
    	jcbmiShowMode.addActionListener( this );
    	jcbmiShowProduct.addActionListener( this );
    	jcbmiShowRowHeader.addActionListener( this );
    	
    	showMenu.add( jcbmiShowMode );
    	showMenu.add( jcbmiShowProduct );
    	
    	showMenu.addSeparator();
    	
    	showMenu.add( jcbmiShowInt );
    	showMenu.add( jcbmiShowExt );
    	showMenu.add( jcbmiShowOperation );
    	showMenu.add( jcbmiShowZone );
    	
    	showMenu.addSeparator();
    	
    	showMenu.add( jcbmiShowRowHeader );
    	
    	//---------------------------------------------------------------------
    	//    Type menu
    	//---------------------------------------------------------------------
    	JMenu typeMenu = new JMenu( "Type" );
    	menuBar.add(typeMenu);
    	
    	jcbmiOperationType = new JCheckBoxMenuItem( "Operation" );
    	jcbmiEventType = new JCheckBoxMenuItem( "Event" );
    	
    	jcbmiOperationType.setSelected( false );
    	jcbmiEventType.setSelected( false );
    	
    	jcbmiOperationType.addActionListener( this );
    	jcbmiEventType.addActionListener( this );
    	
    	typeMenu.add( jcbmiOperationType );
    	typeMenu.add( jcbmiEventType );
    	
    	//add menu
    	setJMenuBar( menuBar );
    }
    
    public void setFile( File file ){
    	this.file = file;
    	if(null != ilCell){
    		ilCell.setFile(file);
    	}
    	
    	setTitle(TITLE+" "+file.getAbsolutePath());
    }
    
    public void save(){
    	if(file == null){
    		saveAs();
    	}
    	
    	updateIL();
    	
    	Loader loader = new Loader();
		loader.saveIL(il, file);
    }
    
    public void saveAs(){
    	
    	if(fc == null){
			fc = new JFileChooser();
		}
		
        //store selection mode
        int tmp = fc.getFileSelectionMode();
        
        //set selection mode
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
    	int returnVal = fc.showSaveDialog(this);

        if( returnVal == JFileChooser.APPROVE_OPTION ){
            
        	setFile(fc.getSelectedFile());
        	save();
        }
        
        //restore selection mode
        fc.setFileSelectionMode(tmp);
    }
    
    public void open(){
    	
    	File tmpFile = null;
    	Loader loader = null;
    	Object o = null;
    	
    	if(fc == null){
			fc = new JFileChooser();
		}
		
        //store selection mode
        int tmp = fc.getFileSelectionMode();
        
        //set selection mode
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
    	int returnVal = fc.showOpenDialog(this);

        if( returnVal == JFileChooser.APPROVE_OPTION ){
        	tmpFile = fc.getSelectedFile();
        }
        
        //restore selection mode
        fc.setFileSelectionMode(tmp);
        
        //open file
        loader = new Loader();
        o = loader.openIL(tmpFile);
        
        if(o instanceof IL){
        	setFile(tmpFile);
        	il = (IL) o;
        	setIL(il);
        }else{
        	JOptionPane.showMessageDialog(this, "File contains no IL","File error",JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    public void setIL(IL il){
    	if(null == il){
    		return;
    	}
    	
    	this.il = il;
    	
    	//sync type menu
    	jcbmiEventType.setSelected( EVENT_IL.equals( il.getType() ) );
    	jcbmiOperationType.setSelected( OPERATION_IL.equals( il.getType() ) );
    	
    	//sync text fields
    	textInputPane.setText( ID, il.getId() );
    	textInputPane.setText( COMMENT, il.getComment() );
    	textInputPane.setText( ACTUATOR, il.getActuator() );
    	textInputPane.setText( OPERATION, il.getOperation() );
    	
    	getContentPane().remove( tableGroup );
    	tableGroup = new ILStructureGroupPane( il.getILStructure() );
    	
    	//sync tableGroup with the menu
    	tableGroup.showInternalTable( jcbmiShowInt.isSelected() );
    	tableGroup.showExternalTable( jcbmiShowExt.isSelected() );
    	tableGroup.showOperationTable( jcbmiShowOperation.isSelected() );
    	tableGroup.showZoneTable( jcbmiShowZone.isSelected() );
    	tableGroup.showModeTable( jcbmiShowMode.isSelected() );
    	tableGroup.showProductTable( jcbmiShowProduct.isSelected() );
    	tableGroup.setRowHeaderVisible( jcbmiShowRowHeader.isSelected() );
    	
    	getContentPane().add( tableGroup );
    	
    	validate();
    }
    
    private void updateIL(){
    	
    	if( jcbmiEventType.isSelected() ){
    		il.setType( EVENT_IL );
    	}else{
    		il.setType( OPERATION_IL );
    	}
    		
    	il.setId( textInputPane.getText( ID ) );
		il.setActuator( textInputPane.getText( ACTUATOR ) );
		il.setComment( textInputPane.getText( COMMENT ) );
		il.setOperation( textInputPane.getText( OPERATION ) );
		
		ILStructure ils = ((ILStructureGroupPane)tableGroup).getILStructure();
		il.setILStructure( ils );
    }
    
    public void actionPerformed(ActionEvent e){
    	
    	//---------------------------------------
    	//	Buttons
    	//--------------------------------------
    	if( e.getSource().equals(jbOk) ){
    		updateIL();
    		
    		if(null != ilCell){
    			ilCell.setIL(il);
    		}
    		
    		if(null != file){
    			save();
    		}
    		
    		setVisible(false);
    		dispose();
    	}else if(e.getSource().equals(jbCancel)){
    		setVisible(false);
    		dispose();
    	}else if(e.getSource().equals(jbCondition)){
    		tableGroup.addConditionRow();
    	}
    	
    	//-------------------------------------------
    	//	MenuItems
    	//-------------------------------------------
    	if(e.getSource() instanceof JMenuItem){
    		if( e.getSource().equals( jmiSave) ){
    			save();
    		}else if( e.getSource().equals( jmiSaveAs )){
    			saveAs();
    		}else if( e.getSource().equals( jmiOpen )){
    			open();
    		}else if( e.getSource().equals( jmiExit )){
    			setVisible( false );
        		dispose();
    		}else if( e.getSource().equals( jcbmiShowInt )){
    			updateIL();
    			setIL( il );
    		}else if( e.getSource().equals( jcbmiShowExt )){
    			updateIL();
    			setIL( il );
    		}else if( e.getSource().equals( jcbmiShowOperation )){
    			updateIL();
    			setIL( il );
    		}else if( e.getSource().equals( jcbmiShowZone )){
    			updateIL();
    			setIL( il );
    		}else if( e.getSource().equals( jcbmiShowMode )){
    			updateIL();
    			setIL( il );
    		}else if( e.getSource().equals( jcbmiShowProduct )){
    			updateIL();
    			setIL( il );
    		}else if( e.getSource().equals( jcbmiShowRowHeader )){
    			updateIL();
    			setIL( il );
    		}else if( e.getSource().equals( jcbmiOperationType )){
    			jcbmiOperationType.setSelected( true );
    			jcbmiEventType.setSelected( false );
    			updateIL();
    			setIL( il );
    		}else if( e.getSource().equals( jcbmiEventType )){
    			jcbmiOperationType.setSelected( false );
    			jcbmiEventType.setSelected( true );
    			updateIL();
    			setIL( il );
    		}
    	}
	}
	
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
    	
    	ObjectFactory factory = new ObjectFactory();
    	IL il = factory.createIL();
    	
    	ILInfoWindow ilInfoWin = new ILInfoWindow(il);
    	ilInfoWin.setVisible(true);
    }

    public static void main(String[] args) {
    	
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	try {
            		UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            	}catch(Exception ex) {}
                createAndShowGUI();
            }
        });
    }
}
