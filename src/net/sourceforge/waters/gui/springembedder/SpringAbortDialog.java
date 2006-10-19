package net.sourceforge.waters.gui.springembedder;

import java.util.TimerTask;
import java.util.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JDialog;

public class SpringAbortDialog
  extends JDialog
{
  private final JProgressBar mProgress;
  private final JButton mAbort;
  private final Timer mTimer;
  private final SpringEmbedder mEmbedder;
  
  public SpringAbortDialog(String name, SpringEmbedder embedder)
  {
    super();
    setTitle("name");
    mEmbedder = embedder;
    mProgress = new JProgressBar(0, SpringEmbedder.NUM_PASSES + 1);
    mAbort = new JButton("Abort");
    mAbort.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        mEmbedder.stop();
      }
    });
    Box box = new Box(BoxLayout.Y_AXIS);
    box.add(mProgress);
    box.add(mAbort);
    add(box);
    pack();
    setSize(getPreferredSize());
    mTimer = new Timer(false);
    mTimer.schedule(new TimerTask()
    {
      public void run()
      {
        if (mEmbedder.isFinished()) {
          mTimer.cancel();
          setVisible(false);
        } else {
          mProgress.setValue(mEmbedder.getProgress());
        }
      }
    }, 100, 100);
    setVisible(true);
  }
}
