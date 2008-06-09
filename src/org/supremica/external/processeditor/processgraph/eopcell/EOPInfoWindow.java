package org.supremica.external.processeditor.processgraph.eopcell;


import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.FlowLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

import org.supremica.manufacturingTables.xsd.eop.EOP;
import org.supremica.manufacturingTables.xsd.eop.Action;
import org.supremica.manufacturingTables.xsd.eop.ZoneState;
import org.supremica.manufacturingTables.xsd.eop.SensorValue;
import org.supremica.manufacturingTables.xsd.eop.InitialState;
import org.supremica.manufacturingTables.xsd.eop.VariableValue;
import org.supremica.manufacturingTables.xsd.eop.ActuatorValue;
import org.supremica.manufacturingTables.xsd.eop.ObjectFactory;
import org.supremica.manufacturingTables.xsd.eop.InitialStateCheck;
import org.supremica.manufacturingTables.xsd.eop.ExternalComponentValue;


import org.supremica.external.processeditor.SOCFrame;
import org.supremica.external.processeditor.processgraph.table.TextInputPane;
import org.supremica.external.processeditor.xml.Loader;

/**
 * Displays the Execution of operation info window, which allow 
 * the user to edit the execution of operation
 */
public class EOPInfoWindow
						extends 
							JFrame 
						implements 
							ActionListener 
{ 
	private static final String TITLE = "Execution Of Operation";
	
    private JButton jbOk = null;
    private JButton jbCancel = null;
    private JButton jbAction = null;
    
    private TextInputPane textInputPane = null;
    private JPanel topPanel, bottomPanel = null;
    
    private EOPTableGroupPane tableGroup = null;
    
    private EOP eop = null;
    private ExecutionOfOperationCell eopCell = null;
    
    
    private static final String EOP_ID = "Id:";
    private static final String COMMENT = "Comment:";
    private static final String ALARMDELAY = "Alarm delay:";
    private static final String ALARMTYPE = "Alarm type:";
    
    private JMenuItem jmiSave, jmiSaveAs, jmiOpen, jmiExit;
    
    private JCheckBoxMenuItem jcbmiShowInt,
    						  jcbmiShowExt,
    						  jcbmiShowZone,
    						  jcbmiShowRowHeader;
    
    private File file = null; 
    private JFileChooser fc = null;
    
    public EOPInfoWindow(ExecutionOfOperationCell eopCell){
    	this(eopCell.getEOP());
    	
    	this.eopCell = eopCell;
    	file = eopCell.getFile();
    }
    
    public EOPInfoWindow( EOP eop ){
    	super(TITLE);
    	
    	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	
    	//SOC icon
    	this.setIconImage(Toolkit.getDefaultToolkit().
  			  getImage(SOCFrame.class.getClass().
  				   getResource("/icons/processeditor/icon.gif")));	
    	
    	getContentPane().setLayout(new BorderLayout());
    	
    	if(eop == null){
    		getContentPane().add(new JLabel("Error! No Execution op operation"),
    				             BorderLayout.CENTER);
    		getContentPane().add(jbCancel = new JButton("Ok"),
    				                                    BorderLayout.PAGE_END);
    		jbCancel.addActionListener(this);
    		
    		pack();
    		
    		setLocation((Toolkit.getDefaultToolkit().
    				                         getScreenSize().
    				                                 width-getWidth())/2,
       		                 (Toolkit.getDefaultToolkit().
       		    		                     getScreenSize().
       		    		                             height-getHeight())/2);
    		
    		return;
    	}
    	
    	this.eop = eop;
    	
    	bottomPanel = new JPanel();
    	bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    	
    	tableGroup = new EOPTableGroupPane(eop);
    	
    	bottomPanel.add(jbAction = new JButton("New action"));
    	bottomPanel.add(jbOk = new JButton("OK")); 	    	    	    	    
    	bottomPanel.add(jbCancel = new JButton("Cancel"));	    
    	
    	
    	jbOk.addActionListener(this);
    	jbCancel.addActionListener(this);
    	jbAction.addActionListener(this);
    	
    	textInputPane = new TextInputPane(null,
    			                          new String[]{EOP_ID,
    			                                       COMMENT,
    			                                       ALARMTYPE,
    			                                       ALARMDELAY});
    	
    	//Get value from EOP
    	textInputPane.setText( EOP_ID, eop.getId() );
    	textInputPane.setText( COMMENT, eop.getComment() );
    	
    	if( null != eop.getInitialState() ){
    		if( null != eop.getInitialState().getInitialStateCheck() ){
    			textInputPane.setText(ALARMDELAY,
    					                  eop.getInitialState().
    					                     getInitialStateCheck().
    					                         getAlarmDelay());
    			textInputPane.setText(ALARMTYPE,
    					              eop.getInitialState().
    					                  getInitialStateCheck().
    					                      getAlarmType());
    		}
    	}
    	
    	topPanel = new JPanel();
    	topPanel.setLayout(new BorderLayout());
    	topPanel.setBorder(BorderFactory.
    			               createTitledBorder("Execution of OPeration data"));
    	
    	topPanel.add(textInputPane, BorderLayout.LINE_START);
    	
    	initMenu();
    	
    	//sync tableGroup with the menu
    	tableGroup.showInternalTable(jcbmiShowInt.isSelected());
    	tableGroup.showExternalTable(jcbmiShowExt.isSelected());
    	tableGroup.showZoneTable(jcbmiShowZone.isSelected());
    	tableGroup.setRowHeaderVisible(jcbmiShowRowHeader.isSelected());
    	
    	getContentPane().add(topPanel, BorderLayout.PAGE_START);
    	getContentPane().add(tableGroup, BorderLayout.CENTER);
    	getContentPane().add(bottomPanel, BorderLayout.PAGE_END);
    	
    	pack();
    	
    	if(getSize().height < 400 || getSize().height < 500){
    		setSize(400, 500);
    	}
    	
    	setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-getWidth())/2,
    		     (Toolkit.getDefaultToolkit().getScreenSize().height-getHeight())/2);
    }
    
    protected void initMenu(){
    	
    	//menu bar
    	JMenuBar menuBar = new JMenuBar();
    	
    	//file menu
    	JMenu fileMenu = new JMenu("File");
    	
    	jmiSave = new JMenuItem("Save");
    	jmiSaveAs = new JMenuItem("Save as");
    	jmiOpen = new JMenuItem("Open");
    	jmiExit = new JMenuItem("Exit");
    	
    	jmiSave.setAccelerator(KeyStroke.getKeyStroke('S', ActionEvent.CTRL_MASK));
    	
    	jmiSave.addActionListener(this);
    	jmiSaveAs.addActionListener(this);
    	jmiOpen.addActionListener(this);
    	jmiExit.addActionListener(this);
    	
    	fileMenu.add(jmiSave);
    	fileMenu.add(jmiSaveAs);
    	fileMenu.add(jmiOpen);
    	
    	fileMenu.addSeparator();
    	
    	fileMenu.add(jmiExit);
    	
    	menuBar.add(fileMenu);
    	
    	//table menu
    	JMenu tableMenu = new JMenu("Table");
    	JMenu showMenu = new JMenu("Show");
    	tableMenu.add(showMenu);
    	menuBar.add(tableMenu);
    	
    	jcbmiShowInt = new JCheckBoxMenuItem("Internal components");
    	jcbmiShowExt = new JCheckBoxMenuItem("External components");
    	jcbmiShowZone = new JCheckBoxMenuItem("Zone");
    	
    	jcbmiShowRowHeader = new JCheckBoxMenuItem("Row header");
    	
    	jcbmiShowInt.setSelected(true);
    	jcbmiShowExt.setSelected(false);
    	jcbmiShowZone.setSelected(true);
    	
    	jcbmiShowRowHeader.setSelected(true);
    	
    	jcbmiShowInt.addActionListener(this);
    	jcbmiShowExt.addActionListener(this);
    	
    	jcbmiShowZone.addActionListener(this);
    	jcbmiShowRowHeader.addActionListener(this);
    	
    	showMenu.add(jcbmiShowInt);
    	showMenu.add(jcbmiShowZone);
    	showMenu.add(jcbmiShowExt);
    	
    	showMenu.addSeparator();
    	
    	showMenu.add(jcbmiShowRowHeader);
    	
    	setJMenuBar(menuBar);
    }
    
    public void setFile(File file){
    	this.file = file;
    	if(null != eopCell){
    		eopCell.setFile(file);
    	}
    	
    	setTitle(TITLE+" "+file.getAbsolutePath());
    }
    
    public File getFile(){
    	return file;
    }
    
    public void save(){
    	if(file == null){
    		saveAs();
    	}
    	
    	updateEOP();
    	
    	Loader loader = new Loader();
		loader.saveEOP(eop, file);
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
        }else{
        	return;
        }
        
        //restore selection mode
        fc.setFileSelectionMode(tmp);
        
        //open file
        loader = new Loader();
        o = loader.openEOP(tmpFile);
        
        if(o instanceof EOP){
        	setFile(tmpFile);
        	setEOP((EOP) o);
        }else{
        	JOptionPane.showMessageDialog(this, "File contains no EOP",
        			                            "File error",
        			                            JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void setEOP(EOP eop){
    	if(null == eop){
    		return;
    	}
    	
    	this.eop = eop;
    	
    	//Get value from EOP
    	textInputPane.setText(EOP_ID, eop.getId() );
    	textInputPane.setText(COMMENT, eop.getComment() );
    	
    	if( null != eop.getInitialState() ){
    		if( null != eop.getInitialState().getInitialStateCheck() ){
    			textInputPane.setText(ALARMDELAY, eop.getInitialState().getInitialStateCheck().getAlarmDelay());
    			textInputPane.setText(ALARMTYPE, eop.getInitialState().getInitialStateCheck().getAlarmType());
    		}
    	}
    	
    	getContentPane().remove(tableGroup);
    	tableGroup = new EOPTableGroupPane(eop);
    	
    	//sync tableGroup with the menu
    	tableGroup.showInternalTable(jcbmiShowInt.isSelected());
    	tableGroup.showExternalTable(jcbmiShowExt.isSelected());
    	tableGroup.showZoneTable(jcbmiShowZone.isSelected());
    	tableGroup.setRowHeaderVisible(jcbmiShowRowHeader.isSelected());
    	
    	getContentPane().add(tableGroup);
    	
    	validate();
    }
    
    /**
     * Reads information from tables and update EOP.
     */
    private void updateEOP(){
    	
    	//---------------------------------------------------------------------
    	//Attributes
    	//---------------------------------------------------------------------
    	eop.setId( textInputPane.getText( EOP_ID ) );
    	
    	eop.setComment( textInputPane.getText( COMMENT ) );
    	
    	//---------------------------------------------------------------------
    	//External components
    	//---------------------------------------------------------------------
    	eop.setExternalComponents(((EOPTableGroupPane)tableGroup).
    			                                      getExternalComponents());
    	
    	//---------------------------------------------------------------------
    	//Internal components
    	//---------------------------------------------------------------------
    	eop.setInternalComponents(((EOPTableGroupPane)tableGroup).
    			                                      getInternalComponents());
    	
    	//---------------------------------------------------------------------
    	//Zones
    	//---------------------------------------------------------------------
    	eop.setZones(((EOPTableGroupPane)tableGroup).getZones());
    	
    	Action[] actions = ((EOPTableGroupPane)tableGroup).getActions();
    	
    	
    	//---------------------------------------------------------------------
    	//Initial state
    	//---------------------------------------------------------------------
    	InitialState initial = (new ObjectFactory()).createInitialState();
    	for(ActuatorValue val : actions[0].getActuatorValue()){
    		initial.getActuatorValue().add(val);
    	}
    	
    	for(SensorValue val : actions[0].getSensorValue()){
    		initial.getSensorValue().add(val);
    	}
    	
    	for(VariableValue val : actions[0].getVariableValue()){
    		initial.getVariableValue().add(val);
    	}
    	
    	for(ZoneState val : actions[0].getZoneState()){
    		initial.getZoneState().add(val);
    	}
    	
    	ExternalComponentValue[] extCompVal = 
    		                       ((EOPTableGroupPane)tableGroup)
    		                              .getExternalComponentsInitialValue();
    	
    	for(int i = 0; i < extCompVal.length; i++){
    		initial.getExternalComponentValue().add(extCompVal[i]);
    	}
    	
    	InitialStateCheck initialCheck = (new ObjectFactory()).createInitialStateCheck();
    	initialCheck.setAlarmDelay(textInputPane.getText(ALARMDELAY));
    	initialCheck.setAlarmType(textInputPane.getText(ALARMTYPE));
    	
    	initial.setInitialStateCheck(initialCheck);
    	
    	eop.setInitialState(initial);
    	
    	//-------------------------------
    	//Actions
    	//-------------------------------
    	eop.getAction().clear();
    	for(int i = 1; i < actions.length; i++){
    		eop.getAction().add(actions[i]);
    	}
    }
    
    public void actionPerformed( ActionEvent e ){
    	
    	//--------------------------------------
    	//	Buttons
    	//--------------------------------------
    	if( e.getSource().equals( jbOk ) ){
    		
    		updateEOP();
    		
    		if( null != eopCell ){
    			eopCell.setEOP( eop );
    		}
    		
    		setVisible( false );
    		dispose();
    		
    	}else if( e.getSource().equals( jbCancel ) ){
    		
    		setVisible( false );
    		dispose();
    		
    	}else if( e.getSource().equals( jbAction ) ){
    		tableGroup.addActionRow();
    	}
    	
    	//----------------------------------------
    	//	MenuItems
    	//----------------------------------------
    	if( e.getSource() instanceof JMenuItem ){
    		if( e.getSource().equals( jmiSave) ){
    			save();
    		}else if( e.getSource().equals( jmiSaveAs )){
    			saveAs();
    		}else if( e.getSource().equals( jmiOpen )){
    			open();
    		}else if( e.getSource().equals( jmiExit )){
    			setVisible(false);
    			dispose();
    		}else if( e.getSource().equals( jcbmiShowInt )){
    			updateEOP();
    			setEOP(eop);
    		}else if( e.getSource().equals( jcbmiShowExt )){
    			updateEOP();
    			setEOP(eop);
    		}else if( e.getSource().equals( jcbmiShowZone )){
    			updateEOP();
    			setEOP(eop);
    		}else if( e.getSource().equals( jcbmiShowRowHeader )){
    			updateEOP();
    			setEOP(eop);
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
    	EOP eop = factory.createEOP();
    	
    	//eop = null;
    	
    	EOPInfoWindow ilInfoWin = new EOPInfoWindow(eop);
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
