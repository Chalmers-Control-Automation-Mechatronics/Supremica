
package org.supremica.util.BDD.graphs;


import java.awt.*;
import java.awt.event.*;


public class LevelGraph extends Frame implements WindowListener {
	private static final int HEIGHT = 100;
	private static final int WIDTH = 128;

	private int max;
	private LevelCanvas can11, can12;
	private LevelCanvas can21, can22;
	private Label msg;

	public LevelGraph(int max) {
		super("Level Graph");
		setLayout( new BorderLayout(20,20) );
		this.max = max;


		add (msg = new Label(), BorderLayout.NORTH);

		Panel mid = new Panel( new GridLayout(2,2, 5,5) );
		add(mid, BorderLayout.CENTER);

		mid.add( can12 = new LevelCanvas(WIDTH, HEIGHT));
		mid.add( can11 = new LevelCanvas(WIDTH, HEIGHT));

		mid.add( can22 = new LevelCanvas(WIDTH, HEIGHT));
		mid.add( can21 = new LevelCanvas(WIDTH, HEIGHT));


		can12.setTitle("" + can11.length()  + "-moving average");
		can22.setTitle("" + can21.length()  + "-moving average");

		can11.setTitle("Workset");
		can21.setTitle("H1(Workset)");


		addWindowListener(this);
		pack();
		show();
	}

	// ---------------------------------------------------
	public void setLabel(String label) {
		msg.setText(label);
	}
	// ---------------------------------------------------

	public void add_workset(int n) {
		if(n < 0 || n> max) return; // XXX: we should warn

		if( can11.addOne((HEIGHT * n) / max) ) {
			int avg = can11.getAverage();
			can12.addOne(avg);
			can12.force_repaint();
		}
		can11.force_repaint();
	}

	public void add_h1(int n) {
		if(n < 0 || n> max) return; // XXX: we should warn

		if( can21.addOne((HEIGHT * n) / max) ) {
			int avg = can21.getAverage();
			can22.addOne(avg);
			can22.force_repaint();
		}
		can21.force_repaint();
	}
/*
	public void add_h2(int n) {
		if(n < 0 || n> max) return; // XXX: we should warn

		if( can31.addOne((HEIGHT * n) / max) ) {
			int avg = can31.getAverage();
			can32.addOne(avg);
			can32.force_repaint();
		}
		can31.force_repaint();
	}
	*/
	// ---------------------------------------------------
	public void windowActivated(WindowEvent e) { }
	public void windowClosed(WindowEvent e) { }
	public void windowClosing(WindowEvent e) { setVisible(true); dispose(); }
	public void windowDeactivated(WindowEvent e) { }
	public void windowDeiconified(WindowEvent e) { }
	public void windowIconified(WindowEvent e) { }
	public void windowOpened(WindowEvent e)  { }
}




class LevelCanvas extends Canvas {
	private static final int W_GAP = 5;
	private static final int H_GAP = 5;

	private int w, h, tot_x, tot_y, offset;
	private int [] data;
	private String title;

	public LevelCanvas(int w, int h) {
		this.w = w;
		this.h = h;
		this.data = new int[w];
		this.offset = 0;

		this.tot_x = w + 2 * W_GAP;
		this.tot_y = h + H_GAP;
		this.title = null;

		for(int i = 0; i < w; i++) data[i] = -1; // invalid
	}

	// ----------------------------------------------------------------
	public boolean addOne(int value) {
		data[offset] = value;
		offset = (offset + 1) % w;
		return (offset == 0);
	}

	public int getAverage() {
		int sum = 0;
		for(int i = 0; i < w; i++) sum += data[i];
		return sum / w; // TODO: round
	}

	// ----------------------------------------------------------------

	public int length() {
		return w;
	}

	public void setTitle(String ttl) {
		title = ttl;
	}

	// ----------------------------------------------------------------
	public void paint(Graphics g) {

		g.setColor( Color.red);
		g.drawRect(W_GAP, H_GAP, W_GAP + w -1,	 h-1);

		g.setColor( Color.black);
		for(int i = 0; i < w; i++) {
			int val = data[ (i + offset) % w ];
			if(val != -1) {
				int x = W_GAP + i;
				int y = h - val;
				g.drawLine(x, y, x, h);
			}
		}

		if(title != null) {
			g.setColor( Color.red);
			g.drawString(title,W_GAP + 3,20); // NOTE: hard-coded coordinates!
		}
	}


	public void force_repaint() {
	Graphics g = getGraphics();
		if (g != null) {
			g.clearRect(0, 0, tot_x, tot_y);
			paint(g);
		}
	}
	// ----------------------------------------------------------------
	public Dimension getPreferredSize() { return new Dimension(tot_x+1, tot_y+1); }
	public Dimension getMinimumSize() { return getPreferredSize(); }
	public Dimension getMaximumSize() { return getPreferredSize(); }

}
