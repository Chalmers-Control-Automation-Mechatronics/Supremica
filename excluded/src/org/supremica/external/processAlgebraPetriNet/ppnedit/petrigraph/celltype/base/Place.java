package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public final class Place 
						extends InfoCell {
    
    private static int radius = 10;
    
    public Place() {
        super(new PlacePanel(2*radius),new InfoPanel());
		
        radius = cellPanel.getSize().width/2;
		infoPanel.setText(name);
        infoPanel.addInfoPanelListener(this);
		
		showInfo(false);
        pack();
    }
    public static int getRadius(){
        return radius;
    }
	
	public void setToken(boolean token){
        ((PlacePanel)cellPanel).drawToken(token);
		repaint();
    }
	
	public boolean getToken(){
        return ((PlacePanel)cellPanel).getToken();
    }
	
    //tell the source cell that we connect to it
    public void addTargetCell(BaseCell cell){
        cell.addSourceCell(this);
        super.addTargetCell(cell);
    }
    
    public void addSourceCell(BaseCell cell){
        cell.addTargetCell(this);
        super.addSourceCell(cell);
    }
	
    public void removeTargetCell(BaseCell cell){
        cell.removeSourceCell(this);
        super.removeTargetCell(cell);
    }
    
    public void removeSourceCell(BaseCell cell){
        cell.removeTargetCell(this);
        super.removeSourceCell(cell);
    }
	
    public String getExp(){
        return "";  //no expression
    }
    public void setText(String text){
        infoPanel.setText(text);
    }
    public void paintComponent(Graphics g) {}
	
	//overide editableCell
	protected BaseCell clone() {
        return new Place();
    }
	
	/* Handel popup menu */
	protected void makePopupCellMenu(){
		JMenuItem menuToken = new JMenuItem( "Token" );
		popupMenu.add( menuToken );
		menuToken.addActionListener( this );
		super.makePopupCellMenu();
	}
	
	public void actionPerformed( ActionEvent event ){
		if(event.getActionCommand().equals("Token")){
			setToken(!getToken());
		}else{
			super.actionPerformed(event);
		}
	}
}
