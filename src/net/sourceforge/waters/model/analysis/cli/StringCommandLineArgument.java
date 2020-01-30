package net.sourceforge.waters.model.analysis.cli;

import java.util.Collection;
import java.util.ListIterator;

import net.sourceforge.waters.analysis.options.Configurable;
import net.sourceforge.waters.analysis.options.Option;

public class StringCommandLineArgument extends CommandLineArgument<String>
{

  public StringCommandLineArgument(final CommandLineOptionContext context,
                                    final Option<String> option)
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
      final String text = iter.next();
      getOption().set(text);
      iter.remove();
      setUsed(true);
    } else {
      failMissingValue();
    }
  }



}
