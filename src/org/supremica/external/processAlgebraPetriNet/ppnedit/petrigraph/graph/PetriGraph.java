package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph;

/*
*	Extends BaseGraph whit PetriGraph features
*
*	David Millares 2007-03-26 
*/

/*
 * To Do:
 *
 */

import javax.swing.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.converter.*;
import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.OpePetriCell;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.RopCell;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.util.BaseCellArray;
import org.supremica.external.processeditor.xgraph.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Point;

import java.awt.event.*;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;




//PetriGraph
//	|_
//	BaseGraph
//  	     |__ Graph
//      	        |__ JPanel
public class PetriGraph 
				extends BaseGraph {
	
	private JMenuItem menuNewPlace;
	private JMenuItem menuNewTransition;
	private JMenuItem menuConnect;
	
    //constructor
    public PetriGraph(){
		super();
	}
	
	public PetriGraph(Worksheet sheat){
		super();
		
		BaseCell[] newcells;
		
		PetriCells petriCells = sheat.getPetriCells();
		
		DEScells descells = petriCells.getDEScells();
		
		
		newcells = desCellsToBaseCells(descells);
		if(newcells != null && newcells.length != 0){
			for(int i=0; i < newcells.length; i++){
				if(newcells[i] != null){
					insert(newcells[i]);
				}
			}
		}
		
		//must be here not before inserting other cells
		PlaceCells placecells = petriCells.getPlaceCells();
		
		newcells = placeCellsToPlaces(placecells);
		if(newcells != null && newcells.length != 0){
			for(int i=0; i < newcells.length; i++){
				if(newcells[i] != null){
					insert(newcells[i]);
				}
			}
		}
	}
	
	private BaseCell[] desCellsToBaseCells(DEScells desCells){
	
		List list = desCells.getDEScell();
		Object[] array = list.toArray();
		
		BaseCell[] baseCells = new BaseCell[array.length]; 
		
		//loop over all petricells
		for(int i=0; i < array.length; i++){
			if(array[i] instanceof DEScell){
				DEScell cell = (DEScell) array[i];
				
				BaseCell newCell = null;
				if(cell.getRelation() != null){
					newCell = Converter.createBaseCell(cell.getRelation());				
				}else if(cell.getROP() != null){
					newCell = Converter.createBaseCell(cell.getROP());
				}else if(cell.getActivity() != null){
					newCell = Converter.createBaseCell(cell.getActivity());
				}
				
				if(newCell != null){
					baseCells[i] = newCell;
				}else{
					baseCells[i] = null;
				}
            }
		}
		return baseCells;
	}
	
	/**
	*	function to get all places from PlaceCells
	*	
	*	OBS cells must hold target and source
	*	cells in corect order
	*
	*/
	private BaseCell[] placeCellsToPlaces(PlaceCells placecells){
		
		List list = placecells.getPlaceCell();
		Object[] array = list.toArray();
		
		BaseCell[] basecells = new Place[array.length]; 
		
		//loop over all
		for(int i=0; i < array.length; i++){
			if(array[i] instanceof PlaceCell){
				basecells[i] = placeCellToPlace((PlaceCell)array[i], cells);
            }
		}
		return basecells;
	}
	
	/**
	*	placeCellToPlace makes a Place from PlaceCells and takes
	*	source and targets cells from cells.
	*
	*
	*/
	private BaseCell placeCellToPlace(PlaceCell placecell, GraphCell[] cells){
		BaseCell cell = new Place();
		
		List targets = placecell.getTargetNr().getNumber();
		Object[] array  = targets.toArray();
		
		for(int i=0; i < array.length; i++){
			if(array[i] instanceof Integer){
				int index = ((Integer)array[i]).intValue();
				if(cells.length > index && cells[index] instanceof BaseCell){
					cell.addTargetCell((BaseCell)cells[index]);
				}
			}
		}
		
		List sources = placecell.getSourceNr().getNumber();
		array  = sources.toArray();
		
		for(int i=0; i < array.length; i++){
			if(array[i] instanceof Integer){
				int index = ((Integer)array[i]).intValue();
				if(cells.length > index && cells[index] instanceof BaseCell){
					cell.addSourceCell((BaseCell)cells[index]);
				}
			}
		}
		
		((Place)cell).setToken(placecell.isToken());
		
		Position pos = placecell.getPosition();
		cell.setPos(new Point(pos.getXCoordinate(),pos.getYCoordinate()));
		
		return cell;
	}
	
	public Worksheet toWorksheet(){
		if(cells == null || cells.length == 0){
			return null;
		}
		
		ObjectFactory factory = new ObjectFactory();
		Worksheet sheat = factory.createWorksheet();
		
		DEScells newcells = factory.createDEScells();
		PlaceCells places = factory.createPlaceCells();
		
		LinkedList cellList = new LinkedList();
		
		//add Basecells not places
		for(int i=0; i < cells.length; i++){
			if(cells[i] instanceof BaseCell && !(cells[i] instanceof Place)){
				
				cellList.add(cells[i]);
					
				DEScell cell = factory.createDEScell();
				
				Object o = null;
				
				if(cells[i] instanceof RopCell){
					o = ((RopCell)cells[i]).getROP();
				}else{
					o = ((BaseCell)cells[i]).getRelation();
				}
				
				//set relation
				if(o instanceof ROP){
					cell.setROP((ROP)o);
				}else if(o instanceof Relation){
					cell.setRelation((Relation)o);
				}else if(o instanceof Activity){
					cell.setActivity((Activity)o);
				}
					
				//add 
				newcells.getDEScell().add(cell);
			}
		}
		
		
		//add places
		for(int i=0; i < cells.length; i++){
			if(cells[i] instanceof Place){
				BaseCell[] bcells = null;
				
				TargetNr target = factory.createTargetNr();
				SourceNr source = factory.createSourceNr();
				
				PlaceCell cell = factory.createPlaceCell();
				
				//add conection cells
				bcells = ((Place)cells[i]).getTargetCells();
				if(bcells != null && bcells.length != 0){
					for(int ii = 0; ii < bcells.length; ii++){
						if(cellList.contains(bcells[ii])){
							target.getNumber().add(cellList.indexOf(bcells[ii]));
						}
					}
				}
				cell.setTargetNr(target);
				
				bcells = ((Place)cells[i]).getSourceCells();
				if(bcells != null && bcells.length != 0){
					for(int ii = 0; ii < bcells.length; ii++){
						if(cellList.contains(bcells[ii])){
							source.getNumber().add(cellList.indexOf(bcells[ii]));
						}
					}
				}
				cell.setSourceNr(source);
				
				//add pos
				Position pos = factory.createPosition();
				pos.setXCoordinate(cells[i].getX());
				pos.setYCoordinate(cells[i].getY());
				
				cell.setPosition(pos);
				
				//add token
				cell.setToken(((Place)cells[i]).getToken());
				
				//add to places
				places.getPlaceCell().add(cell);
				
			}
		}
		
		
		PetriCells petricells = factory.createPetriCells();
		
		petricells.setDEScells(newcells);
		petricells.setPlaceCells(places);
		
		sheat.setPetriCells(petricells);
		return sheat;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g;
		
        //connect all Places
        for(int i = 0; i < cells.length; i++){
            if(cells[i] instanceof Place){
                Arc.drawCellLines(g2,(Place)cells[i]);
            }
        }
    }
	
	/** 
    *
    *	function to add popupmenu features
    *	for PetriGraph
	*
    */
	protected void makePopupMenu(){
		//alter popupMenu here
		
		/* new menu item */
		menuNewPlace = new JMenuItem( "Place" );
		menuNewPlace.addActionListener(this);
		 
		menuNewTransition = new JMenuItem( "Transition" );
		menuNewTransition.addActionListener(this);
		
		menuConnect = new JMenuItem( "Connect" );
		menuConnect.addActionListener(this);
		
		/* add item to popupmenu */
		popupMenu.add(menuNewPlace);
		popupMenu.add(menuNewTransition);
		popupMenu.add(menuConnect);
		
		popupMenu.add(new JSeparator());
		
		/* dont forget to call super*/
		super.makePopupMenu();
	}
	
	private void connect(){
	
		GraphCell[] cells = ((BaseSelection)selection).getSelected(); 
		if( cells == null || cells.length != 2){
			return;
		}
		
		if(cells[0] instanceof Place && !(cells[1] instanceof Place)){
			((Place)cells[0]).addTargetCell((BaseCell)cells[1]);
		}else if(cells[1] instanceof Place && !(cells[0] instanceof Place)){
			((Place)cells[1]).addSourceCell((BaseCell)cells[0]);
		}
	}
	
	/** 
    *
    *	function to manipulate popupmenu items
    *	before shown
	*
    */
	protected void showPopupMenu(Point pos){
		//manipulate menuItem here before
		//shown
		super.showPopupMenu(pos);
	}
	
	/* ------------ Override Action Listener -------------*/
	public void actionPerformed( ActionEvent event ){
		if(event.getActionCommand().equals("Place")){
			paste(new Place());
			repaint();
		}else if(event.getActionCommand().equals("Transition")){
			paste(new Transition());
			repaint();
		}else if(event.getActionCommand().equals("Connect")){
			connect();
			repaint();
		}else{
			//not here send to super
			super.actionPerformed(event); 
		}
	}
	/* ------------ End Action Listener -------------*/
}
