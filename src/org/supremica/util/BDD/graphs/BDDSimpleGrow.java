package org.supremica.util.BDD.graphs;


import java.awt.*;

import org.supremica.util.BDD.*;

/**
 * just show the interesting data, no graphs!
 */

public class BDDSimpleGrow
	extends GrowFrame
{
    private static final long serialVersionUID = 1L;

	private BDDAutomata manager;
	private SimpleStatCanvas canvas;

	private int curr;
	private boolean working;

	public BDDSimpleGrow(BDDAutomata manager, String title)
	{
		super("NODE/" + title, false);
		this.manager = manager;

		curr = 0;
		working = true;

		setVisible(false);
		add(canvas = new SimpleStatCanvas(), BorderLayout.CENTER);

		// repack!
		pack();
		pack();
		setVisible(true);
	}

	public void add(int bdd)
	{
		curr = manager.nodeCount(bdd);
		super.add(curr);
	}


	protected void update_canvas() {
		canvas.force_repaint();
	}

	public void stopTimer()
	{
		super.stopTimer();

		status.setText(status.getText() + ", DONE");

		working = false;
		update_canvas();

	}


	// ---- [ inner class : SimpleStatCanvas ] ---------------------------------------------------------
	private class SimpleStatCanvas extends Canvas {

	    private static final long serialVersionUID = 1L;

		public SimpleStatCanvas() {
			setSize(300, 130);
			setBackground(Color.lightGray);
			setFont( new Font("Courier", Font.PLAIN, 18) );
		}

		/** this is to force repainting right away even if the AWT thread is busy */
		public void force_repaint() {
			Graphics g = getGraphics();
			if (g != null) {
				g.clearRect(0, 0, getWidth(), getHeight());
				paint(g);
			}
		}

		public void paint(Graphics g) {

			// color depends on if we are done or not
			g.setColor( working ? Color.black : Color.red);

			int count =  iterations();
			g.drawString("Iterations   : " + count, 10,30);
			g.drawString("Last size    : " + finalValue() , 10,50);
			g.drawString("Max size     : " + maxValue() , 10,70);

			if(count > 0) {
				g.drawString("Average Size : " + average() , 10, 90);
			}
		}
	}



}
