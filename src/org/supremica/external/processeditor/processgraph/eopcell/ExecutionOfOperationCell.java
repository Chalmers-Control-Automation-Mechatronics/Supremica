package org.supremica.external.processeditor.processgraph.eopcell;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import org.supremica.external.processeditor.processgraph.*;
import org.supremica.external.processeditor.processgraph.resrccell.*;

import org.supremica.manufacturingTables.xsd.eop.*;

import java.io.File;


public class ExecutionOfOperationCell 
									extends 
										ResourceCell
{
	
	private static final Color bgColor = new Color(0,0,100,50);
	private static final Color fgColor = Color.BLACK;
	
	private static final String TEXT = "EOP";
	
	EOPInfoWindow eopInfoWin = null;
	File eopFile = null;
	
	public ExecutionOfOperationCell(EOP eop){
		this();
		if(null != eop){
			setEOP(eop);
		}
	}
	
	public ExecutionOfOperationCell(){
		super();
		setEOP((new ObjectFactory()).createEOP());
		setSize(30,30);
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
	
	public void setEOP(EOP eop){
		setFunction(eop);
	}
	
	public EOP getEOP(){
		Object o = getFunction();
		if(o instanceof EOP){
			return (EOP) o;
		}
		return null;
	}
	
	public void setFile(File file) {
		super.setFile(file);
		setToolTipText(file.getAbsolutePath());
	}
	
	public void mouseClicked(MouseEvent e) {
		
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if(e.getClickCount() > 1){
			
			if(null == eopInfoWin){
				eopInfoWin = new EOPInfoWindow(this);
			}else{
				eopInfoWin.setEOP(getEOP());
			}
			
			eopInfoWin.setVisible(true);
			if(null != getFile()){
	    		eopInfoWin.setFile(getFile());
	    	}
			
		}else{
			super.mouseReleased(e);
		}
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
		
		EOP originalEOP = getEOP();
		EOP cloneEOP = factory.createEOP();
		
		cloneEOP.setExternalComponents(originalEOP.getExternalComponents());
		cloneEOP.setInitialState(originalEOP.getInitialState());
		cloneEOP.setInternalComponents(originalEOP.getInternalComponents());
		cloneEOP.setZones(originalEOP.getZones());
		
		cloneEOP.getAction().addAll(originalEOP.getAction());
		
		ExecutionOfOperationCell eopCell = new ExecutionOfOperationCell();
		eopCell.setEOP(cloneEOP);
		
    	return eopCell;
    }
}
