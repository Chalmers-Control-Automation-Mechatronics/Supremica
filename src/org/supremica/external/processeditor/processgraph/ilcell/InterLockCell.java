package org.supremica.external.processeditor.processgraph.ilcell;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.io.File;

import org.supremica.external.processeditor.processgraph.NestedCell;
import org.supremica.external.processeditor.processgraph.resrccell.*;
import org.supremica.manufacturingTables.xsd.il.IL;
import org.supremica.manufacturingTables.xsd.il.ObjectFactory;


public class InterLockCell 
						extends 
							ResourceCell 
{
	private ILInfoWindow ilInfoWin = null;
	
	private int x = 30;
	
	public InterLockCell(){
		super();
		setIL((new ObjectFactory()).createIL());
		this.setSize(x,x);
	}     
	
	public void paintComponent(Graphics g) {
		int diff = 4;
		int x,y;
		
		String txt = "IL";
		
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setColor(new Color(250,119,19,80));
		g2.fillOval(-diff, -diff, getHeight()+diff*2, getWidth()+diff*2);
		
		g2.setColor(Color.black);
		
		x = (getWidth() - g2.getFontMetrics().stringWidth(txt))/2;
		y = getHeight()/2 + g2.getFontMetrics().getHeight()/4;
		
		g2.drawString(txt,x,y);
		
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 2){
			
			if(null == ilInfoWin){
				ilInfoWin = new ILInfoWindow(this);
			}else{
				ilInfoWin.setIL(getIL());
			}
	    	ilInfoWin.setVisible(true);
		}else{
			super.mouseClicked(e);
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {}
	
	public IL getIL(){
		Object o = getFunction();
		if(o instanceof IL){
			return (IL) o;
		}
		return null;
	}
	
	public void setIL(IL il){
		setFunction(il);
	}
	
	@Override
	public void setFile(File file) {
		super.setFile(file);
		setToolTipText(file.getAbsolutePath());
	}
	
	@Override
	public void pack(){};
	
	@Override
	public boolean isEmpty() {
    	return false;
    }
	
	@Override
	protected NestedCell clone() {
		ObjectFactory factory = new ObjectFactory();
		
		IL originalIL = getIL();
		IL cloneIL = factory.createIL();
		
		cloneIL.setActuator(originalIL.getActuator());
		cloneIL.setComment(originalIL.getComment());
		cloneIL.setId(originalIL.getId());
		cloneIL.setOperation(originalIL.getOperation());
		
		cloneIL.setILStructure(originalIL.getILStructure());
		
		InterLockCell ilCell = new InterLockCell();
		ilCell.setIL(cloneIL);
		
    	return ilCell;
    }
}
