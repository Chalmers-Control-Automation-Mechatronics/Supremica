package net.sourceforge.waters.gui.simulator;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class SorterButtonPanel extends JPanel implements ComponentListener
{

  public SorterButtonPanel(final EventJTree mEventsTree, final JScrollPane parent)
  {
    mParent = parent;
    typeButton = new SorterButton("Type", mEventsTree, 0);
    nameButton = new SorterButton("Name", mEventsTree, 1);
    enabledButton = new SorterButton("Enb", mEventsTree, 2);
    resizeButtons();
    this.add(typeButton);
    this.add(nameButton);
    this.add(enabledButton);
    parent.addComponentListener(this);
  }


  public void componentHidden(final ComponentEvent e)
  {
    // Do nothing
  }

  public void componentMoved(final ComponentEvent e)
  {
    // Do nothing
  }

  public void componentResized(final ComponentEvent e)
  {
    resizeButtons();
  }

  public void componentShown(final ComponentEvent e)
  {
    resizeButtons();
  }

  private void resizeButtons()
  {
    final GridBagLayout layout = new GridBagLayout();
    final int width = mParent.getWidth();
    final int typeWidth = (int)((width * WIDTH_OF_BUTTON_COLUMNS[0] + bufferRegion * WIDTH_OF_BUTTON_COLUMNS[0]) / (sum(WIDTH_OF_BUTTON_COLUMNS)));
    final int nameWidth = (int)((width * WIDTH_OF_BUTTON_COLUMNS[1] + bufferRegion * WIDTH_OF_BUTTON_COLUMNS[1]) / (sum(WIDTH_OF_BUTTON_COLUMNS)));
    final int enabledWidth = (int)((width * WIDTH_OF_BUTTON_COLUMNS[2] + bufferRegion * WIDTH_OF_BUTTON_COLUMNS[2]) / (sum(WIDTH_OF_BUTTON_COLUMNS)));
    System.out.println("DEBUG: Size of buttons : " + typeWidth + " , " + nameWidth + " , " + enabledWidth);
    System.out.println("DEBUG: Error is: " + (width - typeWidth - nameWidth - enabledWidth));
    enabledButton.setPreferredSize(new Dimension(enabledWidth, rowHeight));
    nameButton.setPreferredSize(new Dimension(nameWidth, rowHeight));
    typeButton.setPreferredSize(new Dimension(typeWidth, rowHeight));
    layout.columnWidths = new int[]{enabledWidth + 2, nameWidth + 2, typeWidth + 2};
    this.setLayout(layout);
    repaint();
  }

  private double sum(final int[] a)
  {
    int o = 0;
    for (int i = 0; i < a.length; i++)
      o+=a[i];
    return o;
  }

  private final JScrollPane mParent;
  private final SorterButton typeButton;
  private final SorterButton nameButton;
  private final SorterButton enabledButton;
  private static final int[] WIDTH_OF_BUTTON_COLUMNS = new int[]{65, 110, 60};
  private static final int bufferRegion = -50;
  private static final int rowHeight = 20;
}
