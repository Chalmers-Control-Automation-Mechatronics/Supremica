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
	extends ResourceCell 
{
    private static final long serialVersionUID = 1L;
	private static final Color bgColor = new Color(250,119,19,80);
	private static final Color fgColor = Color.BLACK;
	
	private static final String TEXT = "IL";
	
	private ILInfoWindow ilInfoWin = null;
	
	public InterLockCell(IL il){
		this();
		if(null != il){
			setIL(il);
		}
	}  
	
	public InterLockCell(){
		super();
		setIL((new ObjectFactory()).createIL());
		this.setSize(30,30);
	}     
	
	public void paintComponent(Graphics g) {
		int diff = 4;
		int x,y;
		
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setColor(bgColor);
		g2.fillOval(-diff, -diff, getHeight()+diff*2, getWidth()+diff*2);
		
		g2.setColor(fgColor);
		
		x = (getWidth() - g2.getFontMetrics().stringWidth(TEXT))/2;
		y = getHeight()/2 + g2.getFontMetrics().getHeight()/4;
		
		g2.drawString(TEXT,x,y);
		
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if(e.getClickCount() > 1){
			if(null == ilInfoWin){
				ilInfoWin = new ILInfoWindow(this);
			}else{
				ilInfoWin.setIL(getIL());
			}
			
	    	ilInfoWin.setVisible(true);
	    	if(null != getFile()){
	    		ilInfoWin.setFile(getFile());
	    	}
		}else{
			super.mouseReleased(e);
		}
	}
	
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
