package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base;

import javax.swing.border.*;
import javax.swing.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.*;

import java.awt.*;


import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;

public class InfoCell extends EditableCell
                              implements InfoPanelListener,
                              			 FocusListener{
    
    private boolean infoPanelAtHorizont = true;
    private boolean infoPanelRightUpp = true;
    private boolean showInfoPanel = true;
    
    protected InfoPanel infoPanel;
    protected JPanel cellPanel;
    
    public InfoCell(JPanel cell, InfoPanel info) {
		super();
        setLayout(null);
        
        cellPanel = cell;
        infoPanel = info;
		
        add(cellPanel);
        add(infoPanel);
	   
        infoPanel.setText("?");
        infoPanel.repaint();
    }
	
	public void editText(boolean bool){
		if(!showInfoPanel){
			return;
		}
		
		if(bool){
			remove(infoPanel);
			
			infoPanel = new EditInfoPanel(infoPanel.getText());
			infoPanel.addInfoPanelListener(this);
			
			infoPanel.addFocusListener(this);
			
			add(infoPanel);
			pack();
			
			infoPanel.requestFocus();
			sizeChanged();
			
		}else{
			remove(infoPanel);
			
			infoPanel = new InfoPanel(infoPanel.getText());
			infoPanel.addInfoPanelListener(this);
			
			add(infoPanel);
			pack();
		}
	}
	
    public void setInfoAtHorizont(boolean horizont){
        infoPanelAtHorizont = horizont;
    }
    public void setInfoRightUpp(boolean rightupp){
        infoPanelRightUpp = rightupp;
    }
    public void showInfo(boolean show){
        showInfoPanel = show;
        
        if(showInfoPanel){
            add(infoPanel);
			
			pack();
			
			if(cellListener != null) {
	    		cellListener.upPack();
			}
        }else{
            remove(infoPanel);
			
			pack();
			
			if(cellListener != null) {
	    		cellListener.downPack();
			}
        }
        
    }
    
    public void pack(){
        int height = 0;
        int width = 0;
		
		int margin = 0;
        
        if(showInfoPanel){
            if(infoPanelAtHorizont){
                height = Math.max(cellPanel.getSize().height,
                                 infoPanel.getSize().height);
                
                width = cellPanel.getSize().width + 
                        infoPanel.getSize().width;
            }else{
                height = cellPanel.getSize().height + 
                        infoPanel.getSize().height;
                width = Math.max(cellPanel.getSize().width,
                                 infoPanel.getSize().width);
            }	
        }else{
            height = cellPanel.getSize().height;
            width = cellPanel.getSize().width;
        }
        
		//add margin space
		height = height + 2*margin; 
		width = width + 2*margin;
			
        setSize(width,height);
        
        if(showInfoPanel){
            if(infoPanelAtHorizont){        
                if(infoPanelRightUpp){
                    cellPanel.setLocation(new Point(margin, margin));
                    infoPanel.setLocation(new Point(cellPanel.getSize().width+margin, margin));
                }else{
                    infoPanel.setLocation(new Point(margin,margin));
                    cellPanel.setLocation(new Point(infoPanel.getSize().width+margin, margin));
                }
            }else{                 
                if(infoPanelRightUpp){
                    infoPanel.setLocation(new Point(margin, margin));
                    cellPanel.setLocation(new Point(margin, infoPanel.getSize().height+margin));
                }else{
                    cellPanel.setLocation(new Point(margin, margin));
                    infoPanel.setLocation(new Point(margin, cellPanel.getSize().height+margin));
                }
            }
        }else{
            cellPanel.setLocation(new Point(margin, margin));
        }
    }
    
    //overides getAnchorPos in GraphCell
    public Point getAnchorPos(int i) {	
	
        Point pos = cellPanel.getLocation();
        
        int moveX = 0;
		int moveY = 0;
        
		switch(i) {
			case CENTER: 	    
	    		moveX = cellPanel.getSize().width/2;
	    		moveY = cellPanel.getSize().height/2;
	    		break;
			case UPPER_LEFT:
				break;
			case UPPER_CENTER: 	    
	    		moveX = cellPanel.getSize().width/2;
	    		break;
			case UPPER_RIGHT:	    
	    		moveX = cellPanel.getSize().width;
	    		break;
			case RIGHT_CENTER:	    
	    		moveX = cellPanel.getSize().width;
	    		moveY = cellPanel.getSize().height/2;	    
	    		break;
			case LOWER_RIGHT:	    
	    		moveX = cellPanel.getSize().width;
	    		moveY = cellPanel.getSize().height;
	    		break;
			case LOWER_CENTER:	    
	    		moveX = cellPanel.getSize().width/2;
	    		moveY = cellPanel.getSize().height;
	    		break;
			case LOWER_LEFT:	    
	    		moveY = cellPanel.getSize().height;
	    		break;
			default:
	    		System.out.println("ERROR! in method InfoCell.getAnchorPos(int i)"); 
		}
        pos.translate(moveX, moveY);
		return pos;			
    }
	
	public void setSelected(boolean selected){
		if(selected){
			cellPanel.setBorder(new SelectedCellBorder());
		}else{
			cellPanel.setBorder(BorderFactory.createEmptyBorder());
		}
	}   
    
    public void paintComponent(Graphics g) {}
    
    /*--------------------------InfoPanelListener------------------------*/
    public void sizeChanged(){
        if(showInfoPanel){
            pack();
        }
		
		if(cellListener != null) {
	    	cellListener.upPack();
		}
		
		repaint();
    }
    
    public void textChanged(){
        exp = infoPanel.getText();
		modified();
    }
    /*--------------------------End InfoPanelListener--------------------*/
    
    
    /*--------------------------FocusListener------------------------*/
    public void focusGained(FocusEvent e){}
    public void focusLost(FocusEvent e){
    	if(e.isTemporary()){
    		editText(false);
    	}
    }
    /*--------------------------End FocusListener------------------------*/
}
