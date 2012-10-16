package net.sourceforge.waters.analysis.po;

import java.util.Arrays;


public enum PartialOrderEventDependencyKind
{

  NONCOMMUTING, COMMUTING, EXCLUSIVE;

  public String value()
  {
    return name();
  }

  public static PartialOrderEventDependencyKind fromValue(final String v)
  {
    return valueOf(v);
  }

  public static PartialOrderEventDependencyKind[][] arrayOfDefault(final int length)
  {
    final PartialOrderEventDependencyKind[][] result =
      new PartialOrderEventDependencyKind[length][length];
    for (final PartialOrderEventDependencyKind[] row : result){
      Arrays.fill(row, NONCOMMUTING);
    }
    return result;
  }


}
