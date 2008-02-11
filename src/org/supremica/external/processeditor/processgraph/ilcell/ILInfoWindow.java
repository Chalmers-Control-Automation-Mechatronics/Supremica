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
    private JButton ok, cancel;
    
    private TextInputPane topPanel;
    private JPanel bottomPanel;
    
    private TableGroupPane tableGroup;
    
    IL il = null;
    
    private static final String ID = "Id:";
    private static final String COMMENT = "Comment:";
    private static final String ACTUATOR = "Actuator:";
    private static final String OPERATION = "Operation:";
    
    private ObjectFactory factory = new ObjectFactory();

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
    		getContentPane().add(cancel = new JButton("Cancel"), BorderLayout.PAGE_END);
    		cancel.addActionListener(this);
    		
    		pack();
    		
    		setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-getWidth())/2,
       		     (Toolkit.getDefaultToolkit().getScreenSize().height-getHeight())/2);
    		
    		return;
    	}
    	
    	this.il = il;
    	
    	bottomPanel = new JPanel();
    	bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    	
    	tableGroup = new ILStructureGroupPane(il.getILStructure());
    	
    	bottomPanel.add(ok = new JButton("OK")); 	    	    	    	    
    	bottomPanel.add(cancel = new JButton("Cancel"));	    
    	
    	ok.addActionListener(this);
    	cancel.addActionListener(this);
    	
    	topPanel = new TextInputPane("InterLock", new String[]{ID,COMMENT,ACTUATOR,OPERATION});
    	
    	topPanel.setText(ID, il.getId());
    	topPanel.setText(COMMENT, il.getComment());
    	topPanel.setText(ACTUATOR, il.getActuator());
    	topPanel.setText(OPERATION, il.getOperation());
    	
    	getContentPane().add(topPanel, BorderLayout.PAGE_START);
    	getContentPane().add(tableGroup, BorderLayout.CENTER);
    	getContentPane().add(bottomPanel, BorderLayout.PAGE_END);
    	
    	JMenuBar menuBar = new JMenuBar();
    	
    	JMenu fileMenu = new JMenu("File");
    	
    	JMenuItem jmiCopy = new JMenuItem("Copy");
    	JMenuItem jmiPaste = new JMenuItem("Paste");
    	
    	jmiCopy.setAccelerator(KeyStroke.getKeyStroke('C', ActionEvent.CTRL_MASK));
    	jmiPaste.setAccelerator(KeyStroke.getKeyStroke('V', ActionEvent.CTRL_MASK));
    	
    	jmiCopy.addActionListener(this);
    	jmiPaste.addActionListener(this);
    	
    	fileMenu.add(jmiCopy);
    	fileMenu.add(jmiPaste);
    	
    	menuBar.add(fileMenu);
    	
    	setJMenuBar(menuBar);
    	
    	pack();
    	
    	setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-getWidth())/2,
    		     (Toolkit.getDefaultToolkit().getScreenSize().height-getHeight())/2);
    }
    
    public void actionPerformed(ActionEvent e){
    	if(e.getSource().equals(ok)){
    		
    		il.setId(topPanel.getText(ID));
    		il.setActuator(topPanel.getText(ACTUATOR));
    		il.setComment(topPanel.getText(COMMENT));
    		il.setOperation(topPanel.getText(OPERATION));
    		
    		ILStructure ils = ((ILStructureGroupPane)tableGroup).getILStructure();
    		il.setILStructure(ils);
    		
    		Loader loader = new Loader();
    		loader.save(il, new File("iltest.xml"));
    		
    	}else if(e.getSource().equals(cancel)){
    		System.exit(0);
    	}
    	
    	if(e.getSource() instanceof JMenuItem){
    		if(e.getActionCommand().equals("Copy")){
    			System.out.println("Copy");
    		}else if(e.getActionCommand().equals("Paste")){
    			System.out.println("Paste");
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
