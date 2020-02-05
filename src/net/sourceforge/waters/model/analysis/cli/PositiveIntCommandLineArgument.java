package net.sourceforge.waters.model.analysis.cli;

import java.util.Collection;
import java.util.ListIterator;

import net.sourceforge.waters.analysis.options.Configurable;
import net.sourceforge.waters.analysis.options.Option;

public class PositiveIntCommandLineArgument extends CommandLineArgument<Integer>
{

  public PositiveIntCommandLineArgument(final CommandLineOptionContext context,
                                    final Option<Integer> option)
  {
    super(context, option);
  }

  //#######################################################################
  //# Simple Access
  @Override
  protected String getArgumentTemplate()
  {
    return "<n>";
  }

  //#######################################################################
  //# Parsing
  @Override
  public void parse(final CommandLineOptionContext context,
                    final Collection<Configurable> configurables,
                    final ListIterator<String> iter)
  {
    iter.remove();
    if (iter.hasNext()) {
      final String text = iter.next();
      getOption().set(text);
      iter.remove();
      setUsed(true);
    } else {
      failMissingValue();
    }
  }



}
