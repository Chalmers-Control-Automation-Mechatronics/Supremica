package org.supremica.util.BDD;

import java.awt.*;
import java.awt.event.*;

public class InteractiveChoice
	extends Dialog
	implements ActionListener
{
	private Button bOK, bRandom;
	private java.awt.List choice;
	private int selected_;
	public InteractiveChoice(String title) {
		super(new Frame(), title, true);

		add( new Label(title), BorderLayout.NORTH );
		add( choice = new java.awt.List(20), BorderLayout.CENTER );


		Panel pSouth = new Panel( new FlowLayout( FlowLayout.LEFT) );
		add( pSouth, BorderLayout.SOUTH );
		pSouth.add( bOK     = new Button("OK"), BorderLayout.SOUTH );
		pSouth.add( bRandom = new Button("Random pick"), BorderLayout.SOUTH );


		bOK.addActionListener( this);
		bRandom.addActionListener( this);
		pack();
	}

	public int getSelected() { return selected_; }
	public void removeAll() { choice.removeAll(); }
	public void add(String str) { choice.add(str); }

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == bOK) {
			selected_ = choice.getSelectedIndex();
			if(selected_ != -1) hide(); // we are done
		} else if(src == bRandom) {
			selected_ = (int)( choice.getItemCount() * Math.random() );
			hide(); // we are done
		}
	}


}