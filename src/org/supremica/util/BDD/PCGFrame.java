

package org.supremica.util.BDD;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class PCGFrame 
    extends Frame
    implements ActionListener
{
    private PCGNode [] nodes;
    private int [] perm;
    private int size;
    private Button bUp, bDown, bDone;
    private java.awt.List order;
    private Object lock = new Object();
    
    public PCGFrame(int [] perm, PCGNode []  nodes) {
	super("[PCGFrame]");
	this.perm  = perm;
	this.nodes = nodes;
	this.size  = perm.length - 1;
	
	Panel pNorth = new Panel();
	add(pNorth, BorderLayout.NORTH);
	pNorth.add( bDone = new Button("Done"));
	
	Panel pEast = new Panel(new GridLayout(6,1));
	add(pEast, BorderLayout.EAST);
	pEast.add(new Label());
	pEast.add(new Label());
	pEast.add(bUp = new Button("Up"));
	pEast.add(bDown = new Button("Down"));		  
	pEast.add(new Label());
	pEast.add(new Label());
	
	add(order = new java.awt.List(25,false));
	build_list();	

	bUp.addActionListener(this);
	bDown.addActionListener(this);
	bDone.addActionListener(this);
	
	add(new Label("reorder PCG, then press done"), 
	    BorderLayout.SOUTH);
		      
    }
    
    public void getUserPermutation() {
	pack();
	setVisible(true);
	synchronized(lock) {
	    try {
		lock.wait();
	    } catch(InterruptedException ignored) { }
	}
    }

    public void actionPerformed(ActionEvent e) {
	Object src = e.getSource();
	if(src == bDown) onDown();
	else if(src == bUp) onUp();
	else if(src == bDone) onDone();
    }

    private void onDown() {
	int sel = order.getSelectedIndex();
	if(sel != -1 && sel != size-1) {
	    int tmp = perm[sel];
	    perm[sel] = perm[sel+1];
	    perm[sel+1] = tmp;	    
	    build_list();
	    order.select(sel+1);
	}
    }
    private void onUp() {
	int sel = order.getSelectedIndex();
	if(sel != -1 && sel != 0) {
	    int tmp = perm[sel];
	    perm[sel] = perm[sel-1];
	    perm[sel-1] = tmp;	    
	    build_list();
	    order.select(sel-1);

	}
    }
    private void onDone() {
	dispose();
	synchronized(lock) {
	    lock.notify();
	}
    }

    private void build_list() {
	order.clear();

	for(int i = 0; i < size; i++) {
	    int p = perm[i];
	    order.add(nodes[p].getName());
	}
	
    }
}
