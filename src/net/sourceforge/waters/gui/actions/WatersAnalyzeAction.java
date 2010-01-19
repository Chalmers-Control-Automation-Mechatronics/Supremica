package net.sourceforge.waters.gui.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import net.sourceforge.waters.analysis.monolithic.MonolithicModelVerifierFactory;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

public abstract class WatersAnalyzeAction
  extends WatersAction
{

  // #######################################################################
  // # Constructor
  protected WatersAnalyzeAction(final IDE ide)
  {
    super(ide);
    ide.attach(this);
    this.setEnabled(true);
    putValue(Action.NAME, getCheckName() + " check");
    putValue(Action.SHORT_DESCRIPTION, "Check for " + getCheckName() + " issues");
  }

  // ###################################################################
  // # Auxillary Methods

  public void updateProductDES()
  {
    if (getIDE() != null)
    {
      final DocumentContainer container = getIDE().getActiveDocumentContainer();
      if (container == null || !(container instanceof ModuleContainer)) {
        des = null;
        return;
      }
      final ModuleContainer mContainer = (ModuleContainer)container;
      try {
        des = mContainer.recompile();
      } catch (final EvalException exception) {
        return;
      }
    }
    else
      des = null;
  }

  // ##############################################################################
  // # Abstract Methods

  protected void updateEnabledStatus()
  {
    setEnabled(true);
  }
  public void update(final EditorChangedEvent event)
  {
    setEnabled(true);
  }

  protected abstract String getCheckName();
  protected abstract String getFailureDescription();
  protected abstract String getSuccessDescription();
  protected abstract ModelVerifier getModelVerifier(ModelVerifierFactory factory, ProductDESProxyFactory desFactory);

  public void actionPerformed(final ActionEvent e)
  {
    updateProductDES();
    if (des != null)
    {
      final AnalyzerDialog dialog = new AnalyzerDialog();
      dialog.setVisible(true);
    }
    else
    {
      getIDE().error("ERROR: DES was not able to be successfully compiled");
    }
  }

  // ##############################################################################
  // # Inner class

  private class AnalyzerDialog extends JDialog
  {
    // #######################################################################
    // # Constructor

    public AnalyzerDialog()
    {
      this.setTitle(getCheckName() + " Check");
      topPanel = new JPanel();
      bottomPanel = new JPanel();
      informationLabel = new WrapperLabel(this);
      //informationLabel = new JLabel();
      informationLabel.setText(getCheckName() + " Check is running...");
      informationLabel.setHorizontalAlignment(SwingConstants.CENTER);
      cancelButton = new JButton("Abort");
      cancelButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          runner.abort();
          AnalyzerDialog.this.dispose();
        }
      });
      final Border border = BorderFactory.createRaisedBevelBorder();
      topPanel.setBorder(border);
      bottomPanel.add(cancelButton, BorderLayout.WEST);
      topPanel.add(informationLabel, BorderLayout.NORTH);
      //majorPanel.setLayout(new GridLayout(2, 1));
      //majorPanel.add(topPanel);
      //majorPanel.add(bottomPanel);
      this.getContentPane().add(topPanel, BorderLayout.CENTER);
      this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
      this.setSize(DEFAULT_DIALOG_SIZE);
      this.setLocation(DEFAULT_DIALOG_LOCATION);
      run();
    }

    public void succeed()
    {
      informationLabel.setText("Model " + des.getName() + " " + getSuccessDescription());
      cancelButton.setText("OK");
      cancelButton.removeActionListener(cancelButton.getActionListeners()[0]);
      cancelButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          AnalyzerDialog.this.dispose();
        }
      });
      this.validate();
    }

    public void fail()
    {
      cancelButton.setText("OK");
      cancelButton.removeActionListener(cancelButton.getActionListeners()[0]);
      traceButton = new JButton("Show Trace");
      cancelButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          AnalyzerDialog.this.dispose();
        }
      });
      traceButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          final TraceProxy counterexample = verifier.getCounterExample();
          ((ModuleContainer)getIDE().getActiveDocumentContainer()).getTabPane().setSelectedIndex(1);
          ((ModuleContainer)getIDE().getActiveDocumentContainer()).getSimulatorPanel().switchToTraceMode(counterexample);
          AnalyzerDialog.this.dispose();
        }
      });
      if (verifier.getCounterExample().getComment() == null)
        informationLabel.setText("Model " + des.getName() + " " + getFailureDescription());
      else if (verifier.getCounterExample().getComment().compareTo("") == 0)
        informationLabel.setText("Model " + des.getName() + " " + getFailureDescription());
      else
        informationLabel.setText(verifier.getCounterExample().getComment());
      bottomPanel.add(traceButton, BorderLayout.EAST);
      this.validate();
    }

    public void error(final AnalysisException exception)
    {
      informationLabel.setText("ERROR: " + exception.getMessage());
      cancelButton.setText("OK");
      cancelButton.removeActionListener(cancelButton.getActionListeners()[0]);
      cancelButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          AnalyzerDialog.this.dispose();
        }
      });
    }

    private void run()
    {
      runner = new AnalyzerThread();
      runner.setPriority(Thread.MIN_PRIORITY);
      runner.start();
    }

    // ######################################################################
    // # Auxillary Methods

    private String HTMLinize(final String raw)
    {
      return "<html><P STYLE=\"text-align:center;word-wrap:break-word;width:100%;left:0\">" + raw + "</p></html>";
      //return raw;
    }

    // ######################################################################
    // # Inner Classes

    private class WrapperLabel extends JLabel implements ComponentListener
    {
      // ######################################################################
      // # Constructor

      public WrapperLabel(final AnalyzerDialog parent)
      {
        super();
        this.parent = parent;
        parent.addComponentListener(this);
      }
      @SuppressWarnings("unused")
      public WrapperLabel(final String e, final AnalyzerDialog parent)
      {
        super(HTMLinize(e));
        this.parent = parent;
        parent.addComponentListener(this);
      }

      public void setText(final String e)
      {
        super.setText(HTMLinize(e));
      }

      // ######################################################################
      // # Interface ComponentListener

      public void componentHidden(final ComponentEvent e)
      {
        // Do Nothing
      }

      public void componentMoved(final ComponentEvent e)
      {
        // Do nothing
      }

      public void componentResized(final ComponentEvent e)
      {
        this.setPreferredSize(new Dimension(((int)parent.getSize().getWidth() - 20), (((int)parent.getSize().getHeight() * 2 / 3) - 20)));
      }

      public void componentShown(final ComponentEvent e)
      {
        // Do nothing
      }

      // #####################################################################
      // # Data Members

      private final AnalyzerDialog parent;

      // #####################################################################
      // # Class Constants
      private static final long serialVersionUID = -6693747793242415495L;
    }


    private class AnalyzerThread extends Thread
    {
      public AnalyzerThread()
      {
        final ProductDESProxyFactory  desfactory = ProductDESElementFactory.getInstance();
        final ModelVerifierFactory vfactory = MonolithicModelVerifierFactory.getInstance();
        verifier = getModelVerifier(vfactory, desfactory);
      }

        public void run()
        {
          super.run();
          boolean fatalError = false;
          verifier.setModel(des);
          try {
            verifier.run();
          }
          catch (final AbortException exception)
          {
            // Do nothing: Aborted
            fatalError = true;
          } catch (final AnalysisException exception) {
            SwingUtilities.invokeLater(new Runnable(){public void run(){error(exception);}});
            fatalError = true;
          }
          if (!fatalError)
          {
            final boolean result = verifier.isSatisfied();
            if (result) {
              SwingUtilities.invokeLater(new Runnable(){public void run(){succeed();}});
            } else {
              SwingUtilities.invokeLater(new Runnable(){public void run(){fail();}});
            }
          }
        }

        public boolean abort()
        {
          if (verifier != null)
          {
            verifier.requestAbort();
            return true;
          }
          else
          {
            return false;
          }
        }

    }

    // ######################################################################
    // # Data Members

    AnalyzerThread runner;
    ModelVerifier verifier;
    JPanel topPanel;
    JPanel bottomPanel;
    JButton cancelButton;
    JButton traceButton;
    WrapperLabel informationLabel;
    //JLabel informationLabel;

    // #####################################################################
    // # Class Constants

    private final Dimension DEFAULT_DIALOG_SIZE = new Dimension(290, 190);
    private final Point DEFAULT_DIALOG_LOCATION = new Point(250, 150);
    private static final long serialVersionUID = -2478548485525996982L;
  }

  // ##############################################################################
  // # Data Members

  ProductDESProxy des;

  // ##############################################################################
  // # Class Constants
  protected static final String ANALYZE_CONFLICT = "CONFLICT";
  protected static final String ANALYZE_CONTROLLABLE = "CONTROLLABLE";
  protected static final String ANALYZE_CONTROL_LOOP = "CONTROL LOOP";
  private static final long serialVersionUID = -3797986885054648213L;
}
