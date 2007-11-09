package org.supremica.external.processAlgebraPetriNet.ppnedit.gui;

import javax.swing.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.PetriPro;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.PetriProListener;

import java.awt.*;


public class PetriProTextField extends JTextField  implements PetriProListener{
    
    PetriPro pro = null;
    
    public PetriProTextField(PetriPro pro) {
        super();
        this.pro = pro;
        setText(pro.toString());
        pro.addPetriProListener(this);
    }
    
    public void expChanged(PetriPro pp){
        setText(pro.toString());
    }
    
    public PetriPro getPetriPro(){
        return pro;
    }
}
