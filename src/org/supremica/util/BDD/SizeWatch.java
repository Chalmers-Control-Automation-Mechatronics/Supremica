


package org.supremica.util.BDD;


import java.awt.*;
import java.awt.event.*;

public class SizeWatch
     extends Frame
    implements ActionListener

{

    // ---------------------- singleton stuffs
    private static SizeWatch instance_ = null;
    private static JBDD manager = null;

    private Button bClose, bQuit, bClear, bDump;
    private List list;
    private SizeWatch() {
	super("BDD node counts");
	if(manager == null) {
	    Options.out.println("[SizeWatch.SizeWatch] you should set the BDD manager first");
	    System.exit(20);
	}


	setFont( new Font("Courier", Font.PLAIN, 12));
	Panel pNorth = new Panel( new FlowLayout( FlowLayout.LEFT) );
	add(pNorth, BorderLayout.NORTH);
	pNorth.add(bClose = new Button("Close") );
	pNorth.add(bClear = new Button("Clear") );
	pNorth.add(bDump = new Button("Dump to stdout") );
	pNorth.add(bQuit  = new Button("Quit"));
	bQuit.addActionListener(this);
	bClear.addActionListener(this);
	bDump.addActionListener(this);
	bClose.addActionListener(this);



	pack();


	// to make room
	Dimension dim = getPreferredSize();
	dim.width = Math.max(dim.width, 400);
	dim.height = Math.max(dim.height, 600);
	resize(dim);


	add( list = new List(30), BorderLayout.CENTER);

	setVisible(true);
    }

    private static SizeWatch getInstance() {
	if(!Options.size_watch)
	    return null;

	if(instance_ == null)
	    instance_ = new SizeWatch();
	return instance_;
    }


    // -----------------------------------
    private String owner = "";

    public static void setManager(JBDD manager_) {
	manager = manager_;
    }

    public static void setOwner(String owner) {
		SizeWatch me = getInstance();
		if(me != null) {
			me.owner = owner;

			int add = (50 - owner.length()) / 2;
			if(add < 0) add = 0;
			StringBuffer dumb = new StringBuffer(add + 2);
			for(int i = 0; i < add; i++) dumb.append('-');

			me.addString(dumb.toString()  + owner + dumb.toString() );
		}
	}


    public static void report(int bdd, String what) {
		SizeWatch me = getInstance();
		if(me != null) {
			long size = manager.nodeCount(bdd);
			StringBuffer buf = new StringBuffer(what);
			while(buf.length() < 40) buf.append(' ');
			buf.append(size);
			me.addString(buf.toString());
		}
    }

    private void addString(String str) {
		list.add(str);
    }

	private void onDump()
	{
		Options.out.println("------ [ SizeWatch dump] ---------------\n");
		int len = list.getItemCount();
		for(int i = 0; i < len; i++) {
			Options.out.println(list.getItem(i));
		}
		Options.out.println();
	}

    // --------------------------------------
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if(src == bClose) dispose();
	else if(src == bClear) list.removeAll();
	else if(src == bDump) onDump();
	else if(src == bQuit) System.exit(0);
    }

}
