package org.supremica.external.processAlgebraPetriNet.ppnedit.gui;

import javax.swing.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.gui.PetriProTextField;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.PetriPro;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;

import java.util.*;


public class TextPanel 
				extends JPanel 
							implements ActionListener {
    
    public TextPanel() {
        
      setLayout(new GridLayout(0,1));
      
      JTextField jtf = null;
      PetriPro pro = null;
      ListIterator listIterator = null;
      
      LinkedList list = PetriPro.getAllPetriPro();
      
      if(list != null){
        listIterator = list.listIterator();
      }else{
        System.out.println("NUll !!");
      }
      
      if(listIterator != null){
          while(listIterator.hasNext()){
              pro = (PetriPro)listIterator.next();
              
              jtf = new PetriProTextField(pro);
              jtf.setText(pro.toString());
              System.out.println(pro.toString());
              
              add(jtf);
              
              jtf.addActionListener(this); 
              
          }
      }else{
        System.out.println("NUll !!");
      }
    }
    
    // Show text when user presses ENTER. 
    public void actionPerformed(ActionEvent ae) { 
      Object o = ae.getSource();
      
      if(o instanceof PetriProTextField){
          PetriProTextField pptf = (PetriProTextField) o;
          
          String[] text = (pptf.getText()).split(PetriPro.EQUAL);
          if(text.length == 2){
              PetriPro pro = pptf.getPetriPro();
              
              text[0].replace(" ","");
              pro.setName(text[0]);
              
              text[1].replace(" ","");
              pro.setExp(text[1]);
              
              System.out.println(pro.toString());
          }else{
              System.out.println("To many " + PetriPro.EQUAL);
          }
      }
    } 
}
