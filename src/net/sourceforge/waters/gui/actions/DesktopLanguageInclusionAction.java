package net.sourceforge.waters.gui.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import net.sourceforge.waters.analysis.bdd.BDDLanguageInclusionChecker;
import net.sourceforge.waters.analysis.monolithic.MonolithicModelVerifierFactory;
import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

public class DesktopLanguageInclusionAction extends WatersDesktopAction
{

  protected DesktopLanguageInclusionAction(final IDE ide, final AutomatonProxy autoToCheck)
  {
    super(ide);
    mAutomaton = autoToCheck;
    des = ((ModuleContainer)getIDE().getActiveDocumentContainer()).getCompiledDES();
    putValue(Action.NAME, "Perform Language Inclusion");
    if (mAutomaton != null)
      putValue(Action.SHORT_DESCRIPTION, "Test this specific automaton for language inclusion");
    else
      putValue(Action.SHORT_DESCRIPTION, "Test all specific automata for language inclusion");
    setEnabled(true);
  }

  public void actionPerformed(final ActionEvent e)
  {
    final AnalyzerDialog appearance = new AnalyzerDialog();
    appearance.setVisible(true);
  }

  private ModelVerifier getModelVerifier(final ModelVerifierFactory vfactory,
                                         final ProductDESProxyFactory desfactory)
  {
    if (mAutomaton == null)
      return new BDDLanguageInclusionChecker(desfactory);
    else
    {
      final Simulation sim = ((ModuleContainer)getIDE().getActiveDocumentContainer()).getSimulatorPanel().getSimulation();
      final ArrayList<AutomatonProxy> autoInAnalysis = new ArrayList<AutomatonProxy>();
      for (final AutomatonProxy auto : sim.getCurrentStates().keySet())
      {
        if (auto.getKind() != ComponentKind.PROPERTY || auto == mAutomaton)
          autoInAnalysis.add(auto);
      }
      final HashSet<EventProxy> eventsInAnalysis = new HashSet<EventProxy>();
      for (final AutomatonProxy auto : autoInAnalysis)
      {
        eventsInAnalysis.addAll(auto.getEvents());
      }
      // ProductDESProxy analyzeDES = new ProductDESSubject(??, ??, ??, eventsInAnalysis, autoInAnalysis);
      // return new BDDLanguageInclusionChecker(analyzeDES, desfactory);
      throw new UnsupportedOperationException("Specific Language Inclusion checks are not supported yet");
    }
  }

  private class AnalyzerDialog extends JDialog
  {
    // #######################################################################
    // # Constructor
    public AnalyzerDialog()
    {
      this.setTitle("Language Inclusion Check");
      topPanel = new JPanel();
      bottomPanel = new JPanel();
      informationLabel = new WrapperLabel(topPanel);
      informationLabel.setText("Language Inclusion Check is running...");
      informationLabel.setHorizontalAlignment(SwingConstants.CENTER);
      cancelButton = new JButton("Abort");
      cancelButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          runner.abort();
          AnalyzerDialog.this.dispose();
        }
      });
      final Border outer = BorderFactory.createRaisedBevelBorder();
      final Border inner = BorderFactory.createEmptyBorder(4, 4, 4, 4);
      final Border border = BorderFactory.createCompoundBorder(outer, inner);
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
      repaint();
      runner = new AnalyzerThread();
      runner.setPriority(Thread.MIN_PRIORITY);
      runner.start();
    }

    public void succeed()
    {
      informationLabel.setText("Model " + des.getName() + " is Language Inclusive.");
      cancelButton.setText("OK");
      cancelButton.removeActionListener(cancelButton.getActionListeners()[0]);
      cancelButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          AnalyzerDialog.this.dispose();
        }
      });
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
        informationLabel.setText("Model " + des.getName() + " isn't Language Inclusive");
      else if (verifier.getCounterExample().getComment().compareTo("") == 0)
        informationLabel.setText("Model " + des.getName() + " isn't Language Inclusive");
      else
        informationLabel.setText(verifier.getCounterExample().getComment());
      bottomPanel.add(traceButton, BorderLayout.EAST);
    }

    public void error(final Throwable exception)
    {
      if (exception instanceof OutOfMemoryError)
        informationLabel.setText("ERROR: Out of Memory");
      else
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

      public WrapperLabel(final JPanel parent)
      {
        super();
        this.parent = parent;
        parent.addComponentListener(this);
        this.componentResized(null);
      }
      @SuppressWarnings("unused")
      public WrapperLabel(final String e, final JPanel parent)
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
        this.setPreferredSize(new Dimension(((int)parent.getSize().getWidth()), (((int)parent.getSize().getHeight() - TITLEBAR_HEIGHT))));
      }

      public void componentShown(final ComponentEvent e)
      {
        // Do nothing
      }

      // #####################################################################
      // # Data Members

      private final JPanel parent;

      // #####################################################################
      // # Class Constants

      private static final int TITLEBAR_HEIGHT = 30;
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
        } catch (final OutOfMemoryError error)
        {
          SwingUtilities.invokeLater(new Runnable(){public void run(){error(error);}});
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
  ModelVerifier verifier;
  private final AutomatonProxy mAutomaton;
  private final ProductDESProxy des;
  private static final long serialVersionUID = -1644229513613033199L;
}