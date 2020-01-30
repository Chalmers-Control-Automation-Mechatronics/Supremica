package net.sourceforge.waters.model.analysis.cli;

import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;

import net.sourceforge.waters.analysis.options.Configurable;
import net.sourceforge.waters.analysis.options.FlagOption;

public class FlagCommandLineArgument extends CommandLineArgument<Boolean>
{

  public FlagCommandLineArgument(final CommandLineOptionContext context,
                                    final FlagOption option)
  {
    super(context, option);
  }

  //#######################################################################
  //# Simple Access
  @Override
  public Boolean getValue()
  {
    return isUsed();
  }

  //#######################################################################
  //# Parsing
  @Override
  public void parse(final CommandLineOptionContext context,
                    final Collection<Configurable> configurables,
                    final ListIterator<String> iter)
  {
    iter.remove();
    setUsed(true);
  }

  @Override
  public String getName()
  {
    final String shortName =
      ((FlagOption)getOption()).getShortCommandLineOption();
    if (shortName != null) return shortName + '|' + super.getName();
    else return super.getName();
  }

  @Override
  public Collection<String> getNames()
  {
    final String shortName =
      ((FlagOption)getOption()).getShortCommandLineOption();
    if (shortName == null) {
      return super.getNames();
    } else {
      final Collection<String> names = new LinkedList<>();
      names.add(shortName);
      names.add(super.getName());
      return names;
    }
  }

}
