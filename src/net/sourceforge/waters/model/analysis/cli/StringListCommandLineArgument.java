package net.sourceforge.waters.model.analysis.cli;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import net.sourceforge.waters.analysis.options.Configurable;
import net.sourceforge.waters.analysis.options.Option;

public class StringListCommandLineArgument extends CommandLineArgument<List<String>>
{

  public StringListCommandLineArgument(final CommandLineOptionContext context,
                                    final Option<List<String>> option)
  {
    super(context, option);
  }

  //#######################################################################
  //# Simple Access
  @Override
  protected String getArgumentTemplate()
  {
    return "<name>";
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
      final String value = iter.next();
      getValue().add(value);
      iter.remove();
      setUsed(true);
    } else {
      failMissingValue();
    }
  }

}
