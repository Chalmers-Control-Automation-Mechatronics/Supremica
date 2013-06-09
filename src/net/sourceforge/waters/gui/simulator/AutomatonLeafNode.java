package net.sourceforge.waters.gui.simulator;

import java.util.Map;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.util.IconLoader;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.supremica.gui.ide.ModuleContainer;

public class AutomatonLeafNode extends DefaultMutableTreeNode
{

  //#########################################################################
  //# Constructor
  public AutomatonLeafNode(final AutomatonProxy aut,
                           final StateProxy overloadedState,
                           final int time)
  {
    super(aut.getName(), false);
    mAutomaton = aut;
    mState = overloadedState;
    mTime = time;
  }


  //#########################################################################
  //# Simple Access
  AutomatonProxy getAutomaton()
  {
    return mAutomaton;
  }

  StateProxy getOverloadedState()
  {
    return mState;
  }

  int getTime()
  {
    return mTime;
  }

  Icon getAutomatonIcon(final Simulation sim)
  {
    return getAutomatonIcon(sim, mAutomaton);
  }


  //#########################################################################
  //# Static Methods
  static Icon getAutomatonIcon(final Simulation sim, final AutomatonProxy aut)
  {
    final ModuleContainer container = sim.getModuleContainer();
    final Map<Object,SourceInfo> infomap = container.getSourceInfoMap();
    final SourceInfo info = infomap.get(aut);
    if (info == null) {
      return null;
    } else if (info.getSourceObject() instanceof VariableComponentProxy) {
      return IconLoader.ICON_VARIABLE;
    } else {
      final ComponentKind kind = aut.getKind();
      return ModuleContext.getComponentKindIcon(kind, false);
    }
  }


  //#########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;
  private final StateProxy mState;
  private final int mTime;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 4785226183311677790L;

}