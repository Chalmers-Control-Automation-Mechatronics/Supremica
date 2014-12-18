package net.sourceforge.waters.model.compiler.efa;

import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

public class EFSMControllabilityException extends EvalException
{
  private static final long serialVersionUID = 5983937125200118084L;

  //#########################################################################
  //# Constructors
  /**
   * Constructs an exception with the error message in the form of the
   * examples below.
   * <p>
   * <STRONG>Examples:</STRONG>
   * <UL>
   * <LI>"Specification '____' attempts to modify the variable '____' on the
   * uncontrollable event '____."</LI>
   * <p>
   * <LI>"Supervisor '____' attempts to modify the variable '____' on the
   * uncontrollable event '____'."</LI>
   * <p>
   * <LI>"Property '____' attempts to modify the variable '____' on the
   * event '____'."</LI></UL>
   *
   * @param component The component of interest
   * @param variable  The variable that is changed
   * @param event     The event that attempts to change the variable
   * @param location  The location where the error occurs
   */
  public EFSMControllabilityException(final SimpleComponentProxy component,
                                      final EFAVariable variable,
                                      final IdentifierProxy event)
  {
    super(component.getKind().toString() + " '" + component.getName() +
          "' attempts to modify the variable '" +
          variable.getVariableName().toString() + "' on the " +
          isUncontrollable(component.getKind()) + "event '" +
          event.toString() + "'!", event); // The location is the event identifier.
  }

  //#########################################################################
  //# Auxiliary Method
  private static String isUncontrollable(final ComponentKind kind)
  {
    if (kind.equals(ComponentKind.PROPERTY))
      return "";
    else
      return "uncontrollable ";
  }
}
