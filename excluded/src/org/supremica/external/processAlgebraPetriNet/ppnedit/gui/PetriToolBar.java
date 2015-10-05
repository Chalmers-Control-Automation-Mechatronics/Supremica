package org.supremica.external.processAlgebraPetriNet.ppnedit.gui;

import javax.swing.*;
import javax.swing.border.*;

import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.PetriPro;

import java.awt.*;
import java.awt.Font.*;
import java.awt.event.*;
import java.io.*;





public class PetriToolBar
					extends JToolBar 
								implements ActionListener{    
    //string of buttons
    private final String[] buttons ={"Place","Transition",
                                     "PetriPro","Sequence",
									 "Alternative","ArbitaryOrder",
									 "Parallel","ROP"};
    
    //GETTING ICONS AT DIRECTORY searchIconsIn
    private String searchIconsIn;

    private JButton[] jbPetriNet;
        
    private ToolBarListener l;

    public PetriToolBar(){  
	super();
        
	String allClasspath = System.getProperty("java.class.path");
	String[] classpath = allClasspath.split(";");
	for(int i = 0; i < classpath.length; i++) {
	    if((new File(classpath[i]+"\\org\\copvision\\Icons\\")).exists()) {
		searchIconsIn = classpath[i]+"\\org\\copvision\\Icons\\";
	    }
	}       
	
        
        addButtons(buttons);	
    }
    private void addButtons(String[] buttons){
        
        //numbers of Buttons
        int ant = buttons.length; 
        
        //create buttons
        jbPetriNet = new JButton[ant];
        
        //add all buttons
        for(int i = 0; i < buttons.length; i++){
            this.add(jbPetriNet[i] = new JButton(buttons[i]));  //add button
            
            //action
            jbPetriNet[i].setActionCommand(buttons[i]);
            jbPetriNet[i].addActionListener(this);              
        }
    }
    public void addToolBarListener(ToolBarListener tbl) {
		l = tbl;
    }
    public void actionPerformed(ActionEvent e){
        BaseCell cell;
        
	String itemName = e.getActionCommand();
        
	//Check if JButton	
	if (e.getSource()instanceof JButton){
            
            String actionButton = e.getActionCommand();
            
            if(actionButton == "Place"){
                cell = new Place();
                l.insert(cell);
            }else if(actionButton == "Transition"){
                cell = new Transition();
                l.insert(cell);
            }else if(actionButton == "PetriPro"){
                PetriPro pro = new PetriPro();
                cell = new PetriProCell(pro);
                l.insert(cell);    
            }else if(actionButton == "Sequence"){
                cell = new SequenceCell(new String[]{"Op1","Op2","Op3"});
                l.insert(cell);
			}else if(actionButton == "Alternative"){
                cell = new AlternativeCell(new String[]{"Op1","Op2","Op3"});
                l.insert(cell);
			}else if(actionButton == "Synchronize"){
				
				BaseCell[] cells = new SequenceCell[2];
				
				cells[0] = new SequenceCell(new String[]{"Op1","Op2","Op3"});
				cells[1] = new SequenceCell(new String[]{"Op4","Op5","Op6"});
				
                cell = new SynchronizeCell(cells);
                l.insert(cell);
			}else if(actionButton == "Parallel"){
				
				BaseCell[] cells = new SequenceCell[2];
				
				cells[0] = new SequenceCell(new String[]{"Op1","Op2","Op3"});
				cells[1] = new SequenceCell(new String[]{"Op4","Op5","Op6"});
				
                cell = new ParallelCell(cells);
                l.insert(cell);
                
			}else if(actionButton == "ArbitaryOrder"){
				
				BaseCell[] cells = new SequenceCell[2];
				
				cells[0] = new SequenceCell(new String[]{"Op1","Op2","Op3"});
				cells[1] = new SequenceCell(new String[]{"Op4","Op5","Op6"});
				
                cell = new ArbitaryOrderCell(cells);
                l.insert(cell);
            }else if(actionButton == "ROP"){
				ObjectFactory factory = new ObjectFactory();
				
				ROP rop = factory.createROP();
				Relation r = factory.createRelation();
				Activity a = factory.createActivity();
				
				a.setOperation("p");
				
				r.setType(RelationType.SEQUENCE);
				r.getActivityRelationGroup().add(a);
				
				rop.setType(ROPType.ROP);
				rop.setMachine("P");
				rop.setRelation(r);
				
				l.insert(new RopCell(rop));
			}else {
                System.out.println("Not implemented" + actionButton);
            }
        }
    }
}		       
