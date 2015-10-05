package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base;

import javax.swing.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.converter.*;
import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.*;

import java.awt.event.*;
import java.awt.*;




public final class Transition
						extends InfoCell {
	
	/*
	*	
	*
	*/
	private boolean editable;
	
    //constructor
    public Transition() {    
        super(new TransitionPanel(), new InfoPanel());
		
        name = new String("T"+id);
        exp = name.toLowerCase();
        
        set(name,exp);
        
        pack();
    }
	public Transition(String name, String exp){
		super(new TransitionPanel(), new InfoPanel());
		
		set(name,exp);
		pack();
	}
    
    private void set(String name, String exp){
        this.name = name;
        this.exp = exp;
        
		setEditable(false);
        setText(exp);
		
		infoPanel.addInfoPanelListener(this);
    }
    public void setEditable(boolean editable){
		this.editable = editable;
	}
	
    public void setExp(String exp){
        this.exp = exp;
        setText(exp);
        pack();
    }
    
    public void setText(String text){
        infoPanel.setText(text);
        pack();
    }
    
    public void paintComponent(Graphics g) {}
	
	protected BaseCell clone() {
        return new Transition(name,exp);
    }
	
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 2) {
			editText(true);
		}else{
			super.mouseClicked(e);
		}
	}
	
	public void textChanged(){
		setExp(infoPanel.getText());
		editText(false);
		super.textChanged();
    }
	
	public String getExp(){
		if(PPN.containsNoOperations(PPN.toInternalExp(exp))){
			return exp;
		}
		
		return "(" + exp + ")";
	}
	
	public Object getRelation(){
		ObjectFactory factory = new ObjectFactory();
		
		Position pos = factory.createPosition();
		pos.setXCoordinate(getX());
		pos.setYCoordinate(getY());
		
		//create Activity
		if(PPN.containsNoOperations(PPN.toInternalExp(exp))){
			Activity activity = Converter.createActivity(exp);
			activity.setOperation(exp);			
			activity.setPosition(pos);
			return activity;
		}
		
		//create Relation
		Relation r = Converter.createRelation(PPN.toInternalExp(exp));
		r.setPosition(pos);
		return r;
    }
	
	//override
	public void drawSourceLines(boolean draw){
		if(cellPanel instanceof TransitionPanel){
			((TransitionPanel)cellPanel).drawArrow(draw);
		}
	}
	
	/* Handle popup menu */
	protected void makePopupCellMenu(){
		JMenuItem menuItem = new JMenuItem( "Evaluate" );
		popupMenu.add( menuItem );
		menuItem.addActionListener( this );
		super.makePopupCellMenu();
	}
	
	public void actionPerformed( ActionEvent event ){
		if(event.getActionCommand().equals("Evaluate")){
			if(!PPN.containsNoOperations(PPN.toInternalExp(exp))){
				super.replace(Converter.createBaseCell(exp));
			}
		}else{
			super.actionPerformed(event);
		}
	}
}
