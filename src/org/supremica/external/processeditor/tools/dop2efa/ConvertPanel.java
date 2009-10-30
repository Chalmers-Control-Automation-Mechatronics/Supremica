package org.supremica.external.processeditor.tools.dop2efa;

import java.io.*;
import java.util.List;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.supremica.external.avocades.common.Module;
import org.supremica.external.avocades.dop2efa.DOPtoEFA;

public class ConvertPanel 
					extends JPanel 
							implements ActionListener {
	
    private static final long serialVersionUID = 1L;
	private static final String WATER_MODULE_EXTENSION = ".wmod";
	
	private ROPtablePane leftPane;
	private JPanel rigthPane;
	private JPanel bottomRigthPane;
	
	private FilePathPane outputPane;
	private JPanel moduleNamePane;
	
	private ParameterPane configPane;
	private JPanel buttonPane;
	
    private JButton jbToFile;
    private JButton jbExit;
    
    private JTextField tfModuleName;
    
    private JFrame dialogReferenceFrame;
    
    private ActionListener l;
    
    public static final String EXIT = "exit"; 
    
    //constructor
    public ConvertPanel() {
        super(new GridLayout(0,2));
        
        leftPane = new ROPtablePane();
    	rigthPane = new JPanel(new GridLayout(2,0));
    	
    	bottomRigthPane = new JPanel();
    	
    	outputPane = new FilePathPane();
    	outputPane.setFileExtension(WATER_MODULE_EXTENSION);
    	
    	configPane = new ParameterPane();
    	buttonPane = new JPanel();
    	
    	moduleNamePane  = new JPanel();
    	tfModuleName = new JTextField(10);
    	tfModuleName.setText("Module");
    	
        leftPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("SOC files"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        
        outputPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Output file"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        
        configPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Config"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        
        moduleNamePane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Module name"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        
        jbToFile = new JButton("Write file");
        jbToFile.addActionListener(this);
        
        jbExit = new JButton("Close");
        jbExit.addActionListener(this);
        
        //add components to moduleNamePane
        tfModuleName.setToolTipText("Name of waters module");
        moduleNamePane.setLayout(new BoxLayout(moduleNamePane,BoxLayout.X_AXIS));
        moduleNamePane.add(tfModuleName);
        
        //add components to buttonPane
        buttonPane.add(jbExit);
        buttonPane.add(jbToFile);
        
        //add components to bottoRigthPane
        bottomRigthPane.setLayout(new BoxLayout(bottomRigthPane,BoxLayout.PAGE_AXIS));
        bottomRigthPane.add(moduleNamePane);
        bottomRigthPane.add(outputPane);
        bottomRigthPane.add(buttonPane);
        bottomRigthPane.add(Box.createVerticalGlue());
        
        configPane.addOption("Block stop events with no start event", "block", false, 0);
        
        rigthPane.add(configPane);
        rigthPane.add(bottomRigthPane);
        
        //Add Components to this panel.
        add(leftPane);
        add(rigthPane);
    }
    
    public void setFrame(JFrame frame) {
    	dialogReferenceFrame = frame;
    }
    
    public void setInputFileChooser(JFileChooser fc) {
    	leftPane.setFileChooser(fc);
    }
    
    public void setOutputFileChooser(JFileChooser fc) {
    	outputPane.setFileChooser(fc);
    }
    
    public void addActionListener(ActionListener l) {
    	this.l = l;
    }
    
    public void refreshTable(){
    	leftPane.refresh();
    }
    
    public void addFile(File file){
    	leftPane.addFile(file);
    }
    
    /**
     *	Take care of action
     */
    public void actionPerformed(ActionEvent evt) {
    	
        Object o = evt.getSource();
        
        if(o == jbToFile){
        	convert();
        }else if(o == jbExit){
        	if(l != null){
        		l.actionPerformed(new ActionEvent(o,0,EXIT));
        	}
        }else{
        	System.err.println("unknown source " + o);
        }
    }
    
    private void convert(){
    	
    	String outFile;
    	String moduleName;
    	
    	List<String> filePathList;
    	
    	if(checkInputToConvertAndInformUser()){
    		filePathList = ((ROPtablePane)leftPane).getMarkedFilePathList();
    		outFile = ((FilePathPane)outputPane).getFilePath();
    		moduleName = tfModuleName.getText();
    		
    		//convert
    		Module mod = DOPtoEFA.buildModule(filePathList,
    							 moduleName,
    							 configPane.getValueOption("block"));
    		if(mod != null){
    			mod.writeToFile(new File(outFile));
    		}else{
    			JOptionPane.
				showMessageDialog(dialogReferenceFrame,
								  "Couldn't create module",
								  "Problem",
								  JOptionPane.ERROR_MESSAGE);
    		}
    	}
    }
    
    private boolean checkInputToConvertAndInformUser(){
    	
    	ROPtablePane ropTablePane;
    	
    	String noFileTitle = "No rop";
    	String noFileMessage = "No rop to convert";
    	
    	String cantCreateFileTitle = "Error";
    	String cantCreateFileMessage = "Unable to create file";
    	
    	String createFileQuestionTitle = "File exist";
    	String createFileQuestion = "Do you want to owerwhrite existing file?";
    	
    	String noModuleNameTitle = "No Module name";
    	String noModuleNameMessage = "You must specify a module name"; 
		 
    	/*
    	 * Do we have somewhere to get files from
    	 */
    	if(!(leftPane instanceof ROPtablePane)){
    		System.err.println("No ROPtablePane");
    		return false;
    	}
    	
    	ropTablePane = (ROPtablePane) leftPane;
    	
    	/*
    	 * Do we have some files
    	 */
    	if(ropTablePane.getMarkedFilePathList() == null ||
    	   ropTablePane.getMarkedFilePathList().size() == 0){
    		
    		if(dialogReferenceFrame == null){
    			System.out.println(noFileMessage);
    		}else{
    			JOptionPane.
    				showMessageDialog(dialogReferenceFrame,
    								  noFileMessage,
    								  noFileTitle,
    								  JOptionPane.ERROR_MESSAGE);
    	    	
    		}
    		return false;
    	}
    	
    	/*
    	 * Do we have a module name
    	 */
    	if(tfModuleName.getText() == null ||
    	   tfModuleName.getText().length() == 0){
    		
    		if(dialogReferenceFrame == null){
    			System.out.println("No module name");
    		}else{
    			JOptionPane.
				showMessageDialog(dialogReferenceFrame,
								  noModuleNameMessage,
								  noModuleNameTitle,
								  JOptionPane.ERROR_MESSAGE);
    		}
    		
    		tfModuleName.requestFocus();
    		return false;
    	}
    	
    	
    	/*
    	 * Do we have a file to write to
    	 */
    	if(outputPane instanceof FilePathPane){
    		File tmpFile = new File(((FilePathPane)outputPane).getFilePath());
    		
    		if(tmpFile == null){
    			if(dialogReferenceFrame == null){
        			System.out.println(cantCreateFileMessage);
        		}else{
        			JOptionPane.
        				showMessageDialog(dialogReferenceFrame,
        								  cantCreateFileMessage,
        								  cantCreateFileTitle,
        								  JOptionPane.ERROR_MESSAGE);
        	    	
        		}
    		}
    		
    		if(tmpFile.exists()){
    			if(dialogReferenceFrame == null){
        			System.out.println(cantCreateFileMessage);
        		}else{
        			//Custom button text
        			Object[] options = {"Yes","No"};
        			int n = JOptionPane.
        						showOptionDialog(dialogReferenceFrame,
        										createFileQuestion,
        										createFileQuestionTitle,
        										JOptionPane.YES_NO_OPTION,
        										JOptionPane.QUESTION_MESSAGE,
        										null,
        										options,
        										options[1]);
        			if(n == 1){
        				return false;
        			}
        		}
    		}else{
    			try{
    				tmpFile.createNewFile();
    			}catch(Exception e){
    				if(dialogReferenceFrame == null){
            			System.out.println(cantCreateFileMessage);
            		}else{
            			JOptionPane.
            				showMessageDialog(dialogReferenceFrame,
            								  cantCreateFileMessage,
            								  cantCreateFileTitle,
            								  JOptionPane.ERROR_MESSAGE);
            	    	
            		}
    				return false;
    			}
    		}
    	}else{
    		System.err.println("No FilePathPane");
    		return false;
    	}
    	
    	
    	
    	return true;
    }
    
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Convert");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add contents to the window.
        ConvertPanel cPanel = new ConvertPanel();
        cPanel.setFrame(frame);
        
        frame.add(cPanel);
        
        //Place frame in center of screen
        frame.pack();
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        
        
        Point c = new Point(screenSize.width/2, screenSize.height/2);
        c.translate(-frameSize.width/2, -frameSize.height/2);
        frame.setLocation(c);
        
        //Display the window.
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

}

