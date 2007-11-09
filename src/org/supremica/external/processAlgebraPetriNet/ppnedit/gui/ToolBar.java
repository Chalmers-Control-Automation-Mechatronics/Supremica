package org.supremica.external.processAlgebraPetriNet.ppnedit.gui;

import javax.swing.*;
import javax.swing.border.*;

import org.supremica.gui.Supremica;


import java.awt.*;
import java.awt.Font.*;
import java.awt.event.*;
import java.io.*;


public class ToolBar extends JToolBar implements ActionListener{    

    //GETTING ICONS AT DIRECTORY searchIconsIn
    private String searchIconsIn;

    private Image fileNew;
    private Image fileOpen;
    private Image fileSave;
    private Image editCut;
    private Image editCopy;
    private Image editPaste;
    private Image editDelete;

    private JButton jbFileNew;
    private JButton jbFileOpen;
    private JButton jbFileSave;
    private JButton jbEditCut;
    private JButton jbEditCopy;
    private JButton jbEditPaste;
    private JButton jbEditDelete;	        

    private JButton jbShowPPframe;
    
    private ToolBarListener l;

    public ToolBar(){  
		super();
		
		fileNew = Toolkit.getDefaultToolkit().	     
	    	getImage(Supremica.class.getResource("/icons/processeditor/FileNew.gif"));	       
		fileOpen = Toolkit.getDefaultToolkit().
	    	getImage(Supremica.class.getResource("/icons/processeditor/FileOpen.gif"));
		fileSave = Toolkit.getDefaultToolkit().
	    	getImage(Supremica.class.getResource("/icons/processeditor/FileSave.gif"));
		editCut = Toolkit.getDefaultToolkit().
	    	getImage(Supremica.class.getResource("/icons/processeditor/EditCut.gif"));
		editCopy = Toolkit.getDefaultToolkit().
	    	getImage(Supremica.class.getResource("/icons/processeditor/EditCopy.gif"));
		editPaste = Toolkit.getDefaultToolkit().
	    	getImage(Supremica.class.getResource("/icons/processeditor/EditPaste.gif"));
		editDelete = Toolkit.getDefaultToolkit().
	    	getImage(Supremica.class.getResource("/icons/processeditor/EditDelete.gif"));
			
		//setLayout(new BoxLayout(BoxLayout.X_AXIS));
		this.add(jbFileNew = new JButton(" New ",new ImageIcon(fileNew)));  	

		this.add(jbFileOpen = new JButton(" Open ",new ImageIcon(fileOpen)));  	
		this.add(jbFileSave = new JButton(" Save ",new ImageIcon(fileSave))); 
		this.addSeparator();
		this.add(jbEditCut = new JButton(" Cut ", new ImageIcon(editCut)));
		this.add(jbEditCopy = new JButton(" Copy ", new ImageIcon(editCopy)));
		this.add(jbEditPaste = new JButton(" Paste ", new ImageIcon(editPaste)));
		this.addSeparator();
		this.add(jbEditDelete = new JButton(" Delete ", new ImageIcon(editDelete)));
		this.addSeparator();
        //this.add(jbShowPPframe = new JButton("PetriProEdit"));

		jbFileNew.addActionListener(this);
		jbFileOpen.addActionListener(this);
		jbFileSave.addActionListener(this);
		jbEditCut.addActionListener(this);
		jbEditCopy.addActionListener(this);
		jbEditPaste.addActionListener(this);
		jbEditDelete.addActionListener(this);
        //jbShowPPframe.addActionListener(this);            	
    }
	
    public void addToolBarListener(ToolBarListener tbl) {
		l = tbl;
    }
    public void actionPerformed(ActionEvent e){
		String itemName = e.getActionCommand();
		//Buttons	
		if (e.getSource()instanceof JButton){
			if (e.getSource() == jbFileNew){			
				l.newSheet();
		    }
			else if (e.getSource() == jbFileOpen){			
				l.openFile();
		    }else if (e.getSource() == jbFileSave){			
				l.saveFile();
		    }else if (e.getSource() == jbEditCut){
				;
		    }else if(e.getSource() == jbEditCopy) {
				;
		    }else if(e.getSource() == jbEditPaste) {

		    }else if(e.getSource() == jbEditDelete) {
                        l.delete();
		    }else if(e.getSource() == jbShowPPframe) {
				PetriProFrame ppframe = new PetriProFrame();
		    }
	    }
    }
}		       
