
package org.supremica.util.BDD.graphs;


import java.awt.*;
import java.awt.event.*;


public class LevelGraph extends Frame implements WindowListener {
	private static final int HEIGHT = 100;
	private static final int WIDTH = 50;

	private int max;
	private LevelCanvas can1, can2, can3;

	public LevelGraph(int max) {
		super("Level Graph");
		setLayout( new BorderLayout(20,20) );
		this.max = max;


		Panel mid = new Panel( new GridLayout(1,2, 5,5) );
		add(mid, BorderLayout.CENTER);

		mid.add( can3 = new LevelCanvas(WIDTH, HEIGHT));
		mid.add( can2 = new LevelCanvas(WIDTH, HEIGHT));

		add( can1 = new LevelCanvas(100, HEIGHT), BorderLayout.SOUTH);


		can2.setTitle("" + can1.length()  + "-moving average");
		can3.setTitle("" + (can1.length() * can2.length() ) + "-moving average");

		addWindowListener(this);
		pack();
		show();
	}

	// ---------------------------------------------------

	public void add(int n) {
		if(n < 0 || n> max) return; // XXX: we should warn

		if( can1.addOne((HEIGHT * n) / max) ) {
			int avg = can1.getAverage();
			if( can2.addOne(avg) ) {
				avg = can2.getAverage();
				can3.addOne(avg);
				can3.force_repaint();
			}
			can2.force_repaint();
		}
		can1.force_repaint();
	}

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
	private static final int MUL = 4;

	private int w, h, tot_x, tot_y, offset;
	private int [] data;
	private String title;

	public LevelCanvas(int w, int h) {
		this.w = w;
		this.h = h;
		this.data = new int[w];
		this.offset = 0;

		this.tot_x = MUL * w + 2 * W_GAP;
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
		g.drawRect(W_GAP, H_GAP, W_GAP + w * MUL-1,	 h-1);

		g.setColor( Color.black);
		for(int i = 0; i < w; i++) {
			int val = data[ (i + offset) % w ];
			if(val != -1) {
				int x = W_GAP + i * MUL;
				int y = H_GAP + h - val;
				g.fillRect(x, y,MUL,h-y);
			}
		}

		if(title != null)
			g.drawString(title,W_GAP,20); // NOTE: hard-coded coordinates!
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
