


package org.supremica.util.BDD;

import java.awt.*;
import java.awt.event.*;

public class GrowFrame 
    extends Frame 
    implements ActionListener
	    
{

    private IntArray vars;
    private GrowCanvas canvas;
    private long start_time, end_time;
    private Button bQuit;
    private Label status;
    public GrowFrame(String txt) {
	super(txt);
	vars = new IntArray();


	Panel pNorth = new Panel(new FlowLayout(FlowLayout.LEFT));
	add(pNorth, BorderLayout.NORTH);
	pNorth.add(bQuit = new Button("Close"));
	bQuit.addActionListener(this);

      
	add(status = new Label(), BorderLayout.SOUTH);

	canvas = new GrowCanvas();
	add(canvas, BorderLayout.CENTER);
	start_time = -1;
	pack();
	setVisible(true);
	startTimer();
    }

    public void startTimer() {	
	start_time = System.currentTimeMillis();
    }

    public void stopTimer() {
	showTime();
    }

    private void showTime() {
	end_time = System.currentTimeMillis();
	status.setText("Time " + ( end_time - start_time) + " [ms]");
    }

    public void add(int value) {
	showTime();

	vars.add(value);
	canvas.repaint();
    }

    public void actionPerformed(ActionEvent e) {
	Object src = e.getSource();
	if(src == bQuit) dispose();
    }
    private class GrowCanvas 
	extends Canvas
    {
	public GrowCanvas() {
	    this.resize(400,300);
	}
	public void paint(Graphics g) {
	    int min = vars.getMin();
	    if(min > 0) min = 0;
	    int max = vars.getMax();
	    Dimension dims = this.size();

	    int size_x = vars.getSize();
	    int size_y = (max - min);

	    int marg_y = (dims.height * 1) / 10;
	    dims.height -= 2 * marg_y;

	    int old_x = -1, old_y = -1; // initilized to junk (or jikes will complain)

	    if(size_x == 0) return; // no values yet

	    for(int i = 0; i < size_x; i++) {
		int x = (i * dims.width) / size_x;
		int y = dims.height + marg_y - (vars.get(i)  * dims.height) / size_y;

		if(i != 0) 
		    g.drawLine(old_x, old_y, x, y );
		
		old_x = x;
		old_y = y;

		
	    }

	    g.drawString("" + size_x + " points, max " +max +", last:" + vars.get(size_x - 1),
			10,10);
	}
    }
}
