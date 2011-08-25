//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   CommandLineArgumentEnum
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Arrays;
import java.util.List;


/**
 * An enumeration-value command line argument passed to a
 * {@link ModelVerifierFactory}.
 * Enumeration command line arguments are specified on the command line by
 * their name followed by a string that represents of the enumeration
 * object to be selected. The command line argument knows the enumeration
 * class of the value type and uses it to convert the parsed text to an
 * appropriate object, which is stored in the
 * <CODE>CommandLineArgumentEnum</CODE> object for retrieval.
 *
 * @author Robi Malik
 */

public abstract class CommandLineArgumentEnum<E extends Enum<E>>
  extends CommandLineArgumentExtensibleEnum<E>
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an optional command line argument of enumeration type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-heuristic&quot;</CODE>.
   * @param  description   A textual description of the argument.
   * @param  eclass        The class of the enumeration type for the
   *                       argument values.
   */
  protected CommandLineArgumentEnum(final String name,
                                    final String description,
                                    final Class<E> eclass)
  {
    super(name, description, new JavaEnumFactory<E>(eclass));
  }

  /**
   * Creates a command line argument of enumeration type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-heuristic&quot;</CODE>.
   * @param  description   A textual description of the argument.
   * @param  eclass        The class of the enumeration type for the
   *                       argument values.
   * @param  required      A flag indicating whether this is a required
   *                       command line argument. The command line tool
   *                       will not accept command lines that fail to
   *                       specify all required arguments.
   */
  protected CommandLineArgumentEnum(final String name,
                                    final String description,
                                    final Class<E> eclass,
                                    final boolean required)
  {
    super(name, description, new JavaEnumFactory<E>(eclass), required);
  }


  //#########################################################################
  //# Inner Class JavaEnumFactory
  private static class JavaEnumFactory<E extends Enum<E>>
    implements EnumFactory<E>
  {
    //#######################################################################
    //# Constructors
    private JavaEnumFactory(final Class<E> clazz)
    {
      mEnumerationClass = clazz;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.ExtensibleEnumFactory
    public List<? extends E> getEnumConstants()
    {
      final E[] array = mEnumerationClass.getEnumConstants();
      return Arrays.asList(array);
    }

    //#######################################################################
    //# Data Members
    private final Class<E> mEnumerationClass;
  }

}
