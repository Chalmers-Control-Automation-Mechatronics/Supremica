package org.supremica.external.processeditor.processgraph.ilcell;

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

import org.supremica.manufacturingTables.xsd.il.*;
import org.supremica.external.processeditor.xml.*;

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
    private JButton jbOk, jbCancel;
    
    private TextInputPane topPanel;
    private JPanel bottomPanel;
    
    private ILStructureGroupPane tableGroup;
    
    private IL il = null;
    
    private static final String ID = "Id:";
    private static final String COMMENT = "Comment:";
    private static final String ACTUATOR = "Actuator:";
    private static final String OPERATION = "Operation:";
    
    private ObjectFactory factory = new ObjectFactory();
    
    private JMenuItem jmiSave, jmiSaveAs;
    
    private JCheckBoxMenuItem jcbmiShowInt, jcbmiShowExt,
    						  jcbmiShowOperation, jcbmiShowZone;
    
    private File file = null; 
    private JFileChooser fc = null;
    /** 
     * Creates a new instance of the class.
     * 
     * @param a the object that is to be edit by this info window
     * @param c the operation cell that launched this info window
     */
    public ILInfoWindow(IL il){
    	super();
    	
    	//change then used
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	//setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	
    	getContentPane().setLayout(new BorderLayout());
    	
    	if(il == null){
    		getContentPane().add(new JLabel("Error! No InterLock"), BorderLayout.CENTER);
    		getContentPane().add(jbCancel = new JButton("Cancel"), BorderLayout.PAGE_END);
    		jbCancel.addActionListener(this);
    		
    		pack();
    		
    		setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-getWidth())/2,
       		     (Toolkit.getDefaultToolkit().getScreenSize().height-getHeight())/2);
    		
    		return;
    	}
    	
    	this.il = il;
    	
    	bottomPanel = new JPanel();
    	bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    	
    	tableGroup = new ILStructureGroupPane(il.getILStructure());
    	
    	bottomPanel.add(jbOk = new JButton("OK")); 	    	    	    	    
    	bottomPanel.add(jbCancel = new JButton("Cancel"));	    
    	
    	jbOk.addActionListener(this);
    	jbCancel.addActionListener(this);
    	
    	topPanel = new TextInputPane("InterLock", new String[]{ID,COMMENT,ACTUATOR,OPERATION});
    	
    	topPanel.setText(ID, il.getId());
    	topPanel.setText(COMMENT, il.getComment());
    	topPanel.setText(ACTUATOR, il.getActuator());
    	topPanel.setText(OPERATION, il.getOperation());
    	
    	getContentPane().add(topPanel, BorderLayout.PAGE_START);
    	getContentPane().add(tableGroup, BorderLayout.CENTER);
    	getContentPane().add(bottomPanel, BorderLayout.PAGE_END);
    	
    	initMenu();
    
    	pack();
    	
    	setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-getWidth())/2,
    		     (Toolkit.getDefaultToolkit().getScreenSize().height-getHeight())/2);
    }
    
    protected void initMenu(){
    	//menu bar
    	JMenuBar menuBar = new JMenuBar();
    	setJMenuBar(menuBar);
    
    	//file menu
    	JMenu fileMenu = new JMenu("File");
    	
    	jmiSave = new JMenuItem("Save");
    	jmiSaveAs = new JMenuItem("Save as");
    	
    	jmiSave.setAccelerator(KeyStroke.getKeyStroke('S', ActionEvent.CTRL_MASK));
    	
    	jmiSave.addActionListener(this);
    	jmiSaveAs.addActionListener(this);
    	
    	fileMenu.add(jmiSave);
    	fileMenu.add(jmiSaveAs);
    	menuBar.add(fileMenu);
    	
    	//table menu
    	JMenu tableMenu = new JMenu("Table");
    	JMenu showMenu = new JMenu("Show");
    	tableMenu.add(showMenu);
    	menuBar.add(tableMenu);
    	
    	jcbmiShowInt = new JCheckBoxMenuItem("Internal components");
    	jcbmiShowExt = new JCheckBoxMenuItem("External components");
    	
    	jcbmiShowOperation = new JCheckBoxMenuItem("Operation");
    	jcbmiShowZone = new JCheckBoxMenuItem("Zone");
    	
    	jcbmiShowInt.setSelected(true);
    	jcbmiShowExt.setSelected(true);
    	jcbmiShowOperation.setSelected(true);
    	jcbmiShowZone.setSelected(true);
    	
    	
    	jcbmiShowInt.addActionListener(this);
    	jcbmiShowExt.addActionListener(this);
    	jcbmiShowOperation.addActionListener(this);
    	jcbmiShowZone.addActionListener(this);
    	
    	showMenu.add(jcbmiShowInt);
    	showMenu.add(jcbmiShowExt);
    	showMenu.add(jcbmiShowOperation);
    	showMenu.add(jcbmiShowZone);
    	
    }
    
    public void setFile(File file){
    	this.file = file;
    }
    
    public void save(){
    	if(file == null){
    		saveAs();
    	}
    	
    	updateIL();
    	
    	Loader loader = new Loader();
		loader.save(il, file);
    }
    
    public void saveAs(){
    	
    	if(fc == null){
			fc = new JFileChooser();
		}
		
        //store selection mode
        int tmp = fc.getFileSelectionMode();
        
        //set selection mode
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
    	int returnVal = fc.showOpenDialog(this);

        if( returnVal == JFileChooser.APPROVE_OPTION ){
            
        	file = fc.getSelectedFile();
        	save();
        }
        
        //restore selection mode
        fc.setFileSelectionMode(tmp);
    }
    
    private void updateIL(){
    	il.setId(topPanel.getText(ID));
		il.setActuator(topPanel.getText(ACTUATOR));
		il.setComment(topPanel.getText(COMMENT));
		il.setOperation(topPanel.getText(OPERATION));
		
		ILStructure ils = ((ILStructureGroupPane)tableGroup).getILStructure();
		il.setILStructure(ils);
    }
    
    public void actionPerformed(ActionEvent e){
    	if( e.getSource().equals(jbOk) ){
    		updateIL();
    		setVisible(false);
    		dispose();
    	}else if(e.getSource().equals(jbCancel)){
    		setVisible(false);
    		dispose();
    	}
    	
    	if(e.getSource() instanceof JMenuItem){
    		if( e.getSource().equals(jmiSave) ){
    			save();
    		}else if( e.getSource().equals(jmiSaveAs )){
    			saveAs();
    		}else if( e.getSource().equals(jcbmiShowInt )){
    			tableGroup.
    				showInternalTable(jcbmiShowInt.isSelected());
    		}else if( e.getSource().equals(jcbmiShowExt )){
    			tableGroup.
					showExternalTable(jcbmiShowExt.isSelected());
    		}else if( e.getSource().equals( jcbmiShowOperation )){
    			tableGroup.
					showOperationTable(jcbmiShowOperation.isSelected());
    		}else if( e.getSource().equals( jcbmiShowZone )){
    			tableGroup.
    				showZoneTable(jcbmiShowZone.isSelected());
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
    	
    	il.setId("152Y18");
    	il.setComment("go to work position");
    	il.setOperation("FIX152");
    	il.setActuator("152Y18YE2");
    	
    	//internal components
    	il.setILStructure(factory.createILStructure());
    	il.getILStructure().setInternalComponents(factory.createInternalComponents());
    	
    	il.getILStructure().getInternalComponents().getActuator().add("A1");
    	il.getILStructure().getInternalComponents().getActuator().add("A2");
    	
    	il.getILStructure().getInternalComponents().getSensor().add("S1");
    	il.getILStructure().getInternalComponents().getSensor().add("S2");
    	
    	il.getILStructure().getInternalComponents().getVariable().add("V1");
    	il.getILStructure().getInternalComponents().getVariable().add("V2");
    	
    	ILInfoWindow ilInfoWin = new ILInfoWindow(il);
    	ilInfoWin.setVisible(true);
        
        //Loader loader = new Loader();
		//loader.save(il, new File("iltest.xml"));
    }

    public static void main(String[] args) {
    	
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
