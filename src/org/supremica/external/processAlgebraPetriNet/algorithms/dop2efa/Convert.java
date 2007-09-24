package org.supremica.external.processAlgebraPetriNet.algorithms.dop2efa;

import java.io.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.filechooser.*;

import org.supremica.apps.*;

public class Convert extends JPanel implements ActionListener {

	private JPanel inputPane;
	private JPanel outputPane;
	private JPanel buttonPane;
	
	private JTextField tfInput;
	private JTextField tfOutput;
    
    private JButton jbInput;
    private JButton jbOutput;
    
    private JButton jbToFile;
    private JButton jbSupremica;
    private JButton jbExit;
    
    private JFileChooser fc;

    //constructor
    public Convert() {
        super(new GridLayout(3,0));
        
        inputPane = new JPanel();
    	outputPane = new JPanel();
    	buttonPane = new JPanel();
    	
        inputPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Input file"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        
        outputPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Output file"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        
        //Create a file chooser
        fc = new JFileChooser();

        tfInput = new JTextField(30);
        tfOutput = new JTextField(30);
        
        jbInput = new JButton("Set");
        jbInput.addActionListener(this);
        
        jbOutput = new JButton("Set");
        jbOutput.addActionListener(this);
        
        jbToFile = new JButton("To file");
        jbToFile.addActionListener(this);
        
        jbSupremica = new JButton("To Supremica");
        jbSupremica.addActionListener(this);
        
        
        jbExit = new JButton("Exit");
        jbExit.addActionListener(this);
        
        //add components to panel
        buttonPane.add(jbToFile);
        buttonPane.add(jbSupremica);
        buttonPane.add(jbExit);
        
        inputPane.add(tfInput);
        inputPane.add(jbInput);
        
        outputPane.add(tfOutput);
        outputPane.add(jbOutput);
        
        //Add Components to this panel.
        add(inputPane);
        add(outputPane);
        add(buttonPane);
    }

    /**
     *	Take care of action
     */
    public void actionPerformed(ActionEvent evt) {
    	
        Object o = evt.getSource();
        
        if(o == jbInput){
        	int returnVal = fc.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                tfInput.setText(file.getAbsolutePath());
            } else {
                ;
            }
        }else if(o == jbOutput){
        	int returnVal = fc.showOpenDialog(this);

            if(returnVal == JFileChooser.APPROVE_OPTION){
                File file = fc.getSelectedFile();
                tfOutput.setText(file.getAbsolutePath());
            }

        }else if(o == jbToFile){
        	convert();
        }else if(o == jbSupremica){
        	openInSupremica();
        }else if(o == jbExit){
        	System.exit(0);
        }else{
        	System.err.println("unknown source " + o);
        }
    }
    
    private void openInSupremica(){ 
    	String in = tfInput.getText();
    	
    	//check in data
    	if(in == null){
    		return;
    	}
    	
    	File inFile = new File(in);
    	
    	if(inFile.exists()){
    		String fileName = "tmpfile";
    		String ext = ".wmod";
    		File outFile = new File(fileName.concat(ext));
    		
    		//unique file name
    		int i = 0;
    		while(outFile.exists()){
    			fileName = "tmpfile" + Integer.toString(i) + ext;
    			outFile = new File(fileName);
    			i = i + 1;
    		}
    		
    		try{
				outFile.createNewFile();
			}
			catch (Exception e){
				e.printStackTrace();
			}
			//help
    		DOPtoEFA.createEFA(inFile, outFile);
    		
    		SupremicaWithGui.startSupremica(new String[]{"test"});
    		
    		outFile.delete();
    	}else{
    		System.err.println("File " + inFile + " dosen't exist.");
    	}
    }

    
    private void convert(){
    	String out = tfOutput.getText(); 
    	String in = tfInput.getText();
    	
    	//check indata
    	if(out == null || in == null){
    		System.err.println("null in convert");
    		return;
    	}
    	
    	File outFile = new File(out);
    	File inFile = new File(in);
    	
    	if(inFile.exists()){
    		if(!outFile.exists()){
    			try{
    				outFile.createNewFile();
    			}
    			catch (Exception e){
    				e.printStackTrace();
    			}
    		}
    		DOPtoEFA.createEFA(inFile, outFile);
    	}else{
    		System.err.println("File " + inFile + " dosen't exist.");
    	}
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
        frame.add(new Convert());
        
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
