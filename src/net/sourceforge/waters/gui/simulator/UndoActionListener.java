package net.sourceforge.waters.gui.simulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;

import org.supremica.gui.ide.ModuleContainer;

public class UndoActionListener implements ActionListener, Observer
{

  public UndoActionListener(final JTable parent, final ModuleContainer module)
  {
    this.sim = ((SimulationTable) parent.getModel()).getSim();
    this.parent = parent;
    module.attach(this);
  }

  public void actionPerformed(final ActionEvent e)
  {
    this.sim.reverseSingleStep();
  }

  //#################################################################################################
  //## Interface Observer
  public void update()
  {
    this.sim = ((SimulationTable) parent.getModel()).getSim();
  }

  public void update(final EditorChangedEvent e)
  {
    this.sim = ((SimulationTable) parent.getModel()).getSim();
  }

  Simulation sim;
  JTable parent;

}
