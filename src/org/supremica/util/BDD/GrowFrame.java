package org.supremica.util.BDD;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class GrowFrame
	extends Frame
	implements ActionListener
{
    private static final long serialVersionUID = 1L;
	private final static long SHOW_THRESHOLD = 1000;

	/** how long to wait before next graph is drawn */
	protected IntArray vars;
	private GrowCanvas canvas;
	private long start_time, end_time, last_time;
	private Button bQuit, bDump, bReturn, bAbort;
	protected Label status;
	private TextArea ta;
	private boolean showGraph, graphic;
	private boolean stopped;
	private Marker marker_root, marker_last;
	protected String title;
	protected int min_value, max_value, last_value, count;
	private double sum;


	// ---------------------------------------------------------------

	/** graphic version */
	protected GrowFrame(String txt)
	{
		this(txt, true);
	}

		/** graphic version or simple version */
	protected GrowFrame(String txt, boolean graphic)
	{
		super(txt);

		this.title = txt;
		this.graphic = graphic;


		this.showGraph = true;


		Panel pNorth = new Panel(new FlowLayout(FlowLayout.LEFT));

		add(pNorth, BorderLayout.NORTH);
		pNorth.add(bQuit = new Button("Close"));
		bQuit.addActionListener(this);
		pNorth.add(bAbort = new Button("Abort"));
		bAbort.addActionListener(this);

		if(graphic) {
			this.vars = new IntArray();
			this.canvas = new GrowCanvas();
			add(canvas, BorderLayout.CENTER);

			bAbort.setBackground(Color.red);
			pNorth.add(bDump = new Button("Values"));
			bDump.addActionListener(this);
			pNorth.add(bReturn = new Button("Graph"));
			bReturn.addActionListener(this);
			bReturn.setVisible(false);
		} else {
			vars = null;
			canvas = null;
		}


		this.max_value = Integer.MIN_VALUE;
		this.min_value = Integer.MAX_VALUE;
		this.last_value = 0;
		this.count = 0;
		this.sum = 0.0;

		add(status = new Label(), BorderLayout.SOUTH);

		add(ta = new TextArea(25, 80), BorderLayout.WEST);
		ta.setVisible(false);

		start_time = last_time = -1;
		marker_root = marker_last = null;

		pack();
		setVisible(true);
		startTimer();
	}

	/**
	 * MAY be called to indicate that we just started counting
	 */
	public void startTimer()
	{
		start_time = System.currentTimeMillis();
		stopped = false;
	}

	/**
	 * MUST be called to indicate that we are done!
	 */
	public void stopTimer()
	{

		if(stopped) return; // already stopped

		end_time = System.currentTimeMillis();
		stopped = true;

		// print some stats
		if(Options.profile_on) {
			Options.out.println("[GrowFrame] Fixpoint in " + iterations() + " iterations and " + totalTime() + " [ms]:");
			Options.out.println("[GrowFrame] VALUES: final=" + finalValue() + ", max=" + maxValue() + ", average=" + average() );
		}


		bAbort.setVisible(false);

		// flush();
		repaint();
	}

	public void add(int value)
	{
		end_time = System.currentTimeMillis();

		// remeber it
		sum += value;
		count++;
		last_value = value;

		// put it in the vector, if any
		if(vars != null) vars.add(value);

		if (last_value > max_value)
		{
			max_value = last_value;
		}

		if (last_value < min_value)
		{
			min_value = last_value;
		}

		// dont update toooooo often
		if (end_time < last_time + SHOW_THRESHOLD)
		{
			return;
		}


		// otherwise...
		last_time = end_time;
		update_screen();
	}

	public void flush()
	{
		update_screen();
	}

	private void update_screen()
	{
		long t = (end_time - start_time);

		if (t > 30 * 60 * 1000)
		{
			status.setText("Time " + (t + 500 * 60) / (1000 * 60) + " [min]");
		}
		else if (t > 30 * 1000)
		{
			status.setText("Time " + (t + 500) / 1000 + " [s]");
		}
		else
		{
			status.setText("Time " + t + " [ms]");
		}

		update_canvas();

	}

	protected void update_canvas() {
		if(graphic) canvas.force_repaint();
	}

	// -------------------------------------------------------------
	public int minValue()
	{
		return min_value;
	}

	public int maxValue()
	{
		return max_value;
	}

	public int finalValue()
	{
		return last_value;
	}

	public int iterations()
	{

		return count;
	}

	public long totalTime()
	{
		return end_time - start_time;
	}

	public IntArray data()
	{
		return vars;
	}

	public void save(String filename)
		throws IOException
	{

		FileWriter fw = new FileWriter(filename);
		String text = getText();

		fw.write(text);
		fw.flush();
		fw.close();
	}

	public int average() {
		if(count == 0) return 0;
		else return (int)(sum / count);
	}
	// -------------------------------------------------------------
	private void onDump()
	{
		if(!graphic) return; // only available when graph data available

		String text = getText();
		ta.setText(text);

		canvas.setVisible(false);
		ta.setVisible(true);
		bDump.setVisible(false);
		bReturn.setVisible(true);
		pack();
	}

	private String getText()
	{
		if(!graphic) return   "ERROR: this funcion is only available when graph data is available";

		int size_x = vars.getSize();
		StringBuilder sb = new StringBuilder();

		// get header:
		sb.append("% " + title + "\n\n");

		// get X
		sb.append("x=[1:" + size_x + "];\n");
		sb.append("\n");

		// get Y
		sb.append("y=[");

		for (int i = 0; i < size_x; i++)
		{
			if (i != 0)
			{
				sb.append("; ");
			}

			if ((i % 20) == 0)
			{
				sb.append("\n");
			}

			sb.append(vars.get(i));
		}

		sb.append("];\n");

		// get markes (if any)
		if (marker_root != null)
		{
			Marker current = marker_root;

			sb.append("\n\n% the markers are:\n");

			while (current != null)
			{
				sb.append("% at step " + current.position + ": " + current.text + "\n");

				current = current.next;
			}
		}

		return sb.toString();
	}

	private void onReturn()
	{
		if(!graphic) return; // only available when graph data available

		ta.setVisible(false);
		canvas.setVisible(true);
		bReturn.setVisible(false);
		bDump.setVisible(true);
		bReturn.setVisible(false);
		pack();
	}

	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();

		if (src == bQuit)
		{
			dispose();
		}
		else if (src == bDump)
		{
			showGraph = false;

			onDump();
		}
		else if (src == bReturn)
		{
			showGraph = true;

			onReturn();
		}
		else if (src == bAbort)
		{
			Limiter.stopAll();
			bAbort.setVisible(false);
		}
	}

	// -[ marker stuff ]-------------------------------------------------------

	/** inster a marker at the current position */
	public void mark(String txt)
	{
		if(!graphic) return; // only available when graph data available

		Marker m = new Marker(txt);

		if (marker_root == null)
		{
			marker_root = marker_last = m;
		}
		else
		{
			marker_last.next = m;
			marker_last = m;
		}
	}

	private class Marker
	{
		public int position;
		public String text;
		public Marker next;

		public Marker(String txt)
		{
			text = txt;
			next = null;
			position = vars.getSize();
		}
	}

	// -[ grow canwas ]-------------------------------------------------------
	private class GrowCanvas
		extends Canvas
	{
	    private static final long serialVersionUID = 1L;

		public GrowCanvas()
		{
			this.setSize(400, 300);
		}

		/** this is to force repainting right away even if the AWT thread is busy */
		public void force_repaint()
		{
			if (!showGraph)
			{
				return;
			}

			Graphics g = GrowCanvas.this.getGraphics();

			if (g != null)
			{
				g.clearRect(0, 0, GrowCanvas.this.getWidth(), GrowCanvas.this.getHeight());
				paint(g);
			}
		}

		public void paint(Graphics g)
		{
			if (!showGraph)
			{
				return;
			}

			int min = min_value;
			int max = max_value;

			if (min > 0)
			{
				min = 0;
			}

			Dimension dims = this.getSize();
			int size_x = vars.getSize();
			int size_y = (max - min);
			int marg_y = (dims.height * 1) / 10;

			if (size_y < 1)
			{
				size_y = 1;    // avoid DIV BY ZERO
			}

			dims.height -= 2 * marg_y;

			int old_x = -1, old_y = -1;    // initilized to junk (or jikes will complain)

			if (size_x == 0)
			{
				return;    // no values yet
			}

			if (stopped)
			{

				// we have stoped, and afford to draw a better cuve
				g.setColor(Color.black);

				for (int i = 0; i < size_x; i++)
				{
					int x = (i * dims.width) / size_x;
					int y = dims.height + marg_y - (vars.get(i) * dims.height) / size_y;

					if (i != 0)
					{
						g.drawLine(old_x, old_y, x, y);
					}

					old_x = x;
					old_y = y;
				}

				g.setColor(Color.red);    // for the upcoming drawString
			}
			else
			{

				// this one is "a-bit" faster (could be much faster using _skips_)
				// good if size becomes huge, ~ 10 000 is not very unusuall)
				// get first one:
				old_x = (0 * dims.width) / size_x;
				old_y = dims.height + marg_y - (vars.get(0) * dims.height) / size_y;

				for (int i = 1; i < size_x; i++)
				{
					int x = (i * dims.width) / size_x;

					if (x == old_x)
					{

						// dont draw
					}
					else
					{
						int y = dims.height + marg_y - (vars.get(i) * dims.height) / size_y;

						g.drawLine(old_x, old_y, x, y);

						old_x = x;
						old_y = y;
					}
				}
			}

			g.drawString("" + size_x + " points, max " + max + ", last:" + vars.get(size_x - 1), 10, 10);

			// now draw the markers, if any:
			if (marker_root != null)
			{
				g.setColor(Color.blue);

				Marker current = marker_root;
				int mark_y = 20;
				int mark_delta = Math.max(dims.height / 6, 40);
				FontMetrics fm = g.getFontMetrics();

				while (current != null)
				{
					int x = (current.position * dims.width) / size_x;
					int y = dims.height + marg_y - (vars.get(current.position) * dims.height) / size_y;

					// render string, but make sure the text is visible
					int w = fm.stringWidth(current.text);
					int x2 = x;

					if (x2 + w > dims.width)
					{
						x2 -= w;
					}

					if (x2 < 0)
					{
						x2 = 0;
					}

					g.drawString(current.text, x2 + 4, mark_y);
					g.drawLine(x2, mark_y, x, y);

					mark_y += mark_delta;

					if (mark_y > dims.height)
					{
						mark_y = 20;
					}

					current = current.next;
				}
			}
		}
	}
}
