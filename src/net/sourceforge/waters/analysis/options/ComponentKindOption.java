package net.sourceforge.waters.analysis.options;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.base.ComponentKind;

/**
 * A configurable parameter of a {@link ModelAnalyzer} of
 * <CODE>ComponentKind</CODE> type.
 *
 * @author Brandon Bassett
 */

public class ComponentKindOption extends Option<ComponentKind>
{
  //#########################################################################
  //# Constructor
  public ComponentKindOption(final String id,
                             final String shortName,
                             final String description,
                             final String commandLineOption)
  {
    super(id, shortName, description, commandLineOption, null);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.options.Option
  @Override
  public OptionEditor<ComponentKind> createEditor(final OptionContext context)
  {
    return context.createComponentKindEditor(this);
  }

  @Override
  public boolean isPersistent()
  {
    return false;
  }

}
