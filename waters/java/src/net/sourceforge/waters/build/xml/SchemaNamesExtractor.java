//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.build.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Collection;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * <P>A tool to extract element and attribute details from an XML schema
 * (<CODE>.xsd</CODE>) file.</P>
 *
 * @author Robi Malik
 */

public class SchemaNamesExtractor
{

  //#########################################################################
  //# Main Method
  public static void main(final String[] args)
  {
    try {
      if (args.length != 4) {
        System.err.println("USAGE");
        System.exit(1);
      }
      final File inputFile = new File(args[0]);
      final File outputFile = new File(args[1]);
      final File headerFile = new File(args[2]);
      final File coreDir = new File(args[3]);
      final SchemaNamesExtractor extractor =
        new SchemaNamesExtractor(headerFile, coreDir);
      extractor.processSchema(inputFile, outputFile);
    } catch (final Throwable exception) {
      System.err.println("FATAL - exception caught in main()!");
      exception.printStackTrace(System.err);
    }
  }


  //#########################################################################
  //# Constructors
  private SchemaNamesExtractor(final File headerFile, final File coreDir)
    throws ParserConfigurationException
  {
    final DocumentBuilderFactory docBuilderFactory =
      DocumentBuilderFactory.newInstance();
    docBuilderFactory.setNamespaceAware(true);
    mHeaderFile = headerFile;
    mCoreClassDirectory = coreDir;
    mCoreClassPackageName = getPackageName(coreDir);
    mBuilder = docBuilderFactory.newDocumentBuilder();
    mElements = new TreeSet<>();
    mAttributes = new TreeSet<>();
    mEnumTypes = new TreeSet<>();
  }


  //#########################################################################
  //#
  private void processSchema(final File inputFile,
                             final File outputFile)
    throws SAXException, IOException
  {
    splitOutputFile(outputFile);
    final InputStream stream = new FileInputStream(inputFile);
    final Document domDocument = mBuilder.parse(stream);
    final Element root = domDocument.getDocumentElement();
    mTargetNameSpace = root.getAttribute("targetNamespace");

    final NodeList list = root.getElementsByTagNameNS(NAMESPACE, "*");
    for (int i = 0; i < list.getLength(); i++) {
      final Node node = list.item(i);
      final Element element = (Element) node;
      processElement(element);
    }

    writeHeader();
    for (final String name : mElements) {
      writePrefixedStringDeclaration("ELEMENT", name);
    }
    mOutput.println();
    for (final AttributeInfo info : mAttributes) {
      info.print();
    }
    if (!mEnumTypes.isEmpty()) {
      mOutput.println();
      for (final EnumInfo enumInfo : mEnumTypes) {
        enumInfo.printDefault();
      }
    }
    writeTrailer();
  }

  private void processElement(final Element element)
  {
    final String name = element.getAttribute("name");
    if (name.length() > 0) {
      mElements.add(name);
    }
    processInnerItem(element);
  }

  private void processInnerItem(final Element item)
  {
    for (Node child = item.getFirstChild();
         child != null;
         child = child.getNextSibling()) {
      if (child instanceof Element) {
        final Element element = (Element) child;
        if (element.getLocalName().equals("attribute")) {
          processAttribute(element);
        } else {
          processInnerItem(element);
        }
      }
    }
  }

  private void processAttribute(final Element attrib)
  {
    final AttributeInfo info;
    final String name = attrib.getAttribute("name");
    final String defaultValue = attrib.getAttribute("default");
    if (defaultValue.length() == 0) {
      info = new AttributeInfo(name);
    } else {
      String type = attrib.getAttribute("type");
      if (type.equals("xs:boolean")) {
        info = new AttributeInfo(name, "boolean", defaultValue);
      } else if (type.equals("xs:int")) {
        info = new AttributeInfo(name, "int", defaultValue);
      } else {
        info = new AttributeInfo(name);
        final int pos = type.lastIndexOf(':');
        if (pos >= 0) {
          type = type.substring(pos + 1);
        }
        registerEnum(type, defaultValue);
      }
    }
    mAttributes.add(info);
  }

  private void registerEnum(final String name, final String defaultValue)
  {
    EnumInfo enumInfo = null;
    for (final EnumInfo info : mEnumTypes) {
      if (info.getName().equals(name)) {
        enumInfo = info;
        break;
      }
    }
    if (enumInfo == null) {
      final File file = new File(mCoreClassDirectory, name + ".java");
      if (file.exists()) {
        enumInfo = new EnumInfo(name);
        mEnumTypes.add(enumInfo);
      }
    }
    if (enumInfo != null) {
      enumInfo.provideDefaultValue(defaultValue);
    }
  }

  private void splitOutputFile(final File outputFile)
  {
    String fileName = outputFile.getName();
    if (fileName.endsWith(".java")) {
      final int pos = fileName.lastIndexOf('.');
      mClassName = fileName.substring(0, pos);
    } else {
      mClassName = fileName;
      fileName += ".java";
    }
    final File parent = outputFile.getParentFile();
    mOutputPackageName = getPackageName(parent);
    if (parent == null) {
      System.err.println("FATAL: Output file " + outputFile +
                         " does not represent a Waters class!");
      System.exit(1);
    }
    try {
      final OutputStream stream = new FileOutputStream(outputFile);
      mOutput = new PrintStream(stream);
    } catch (final IOException exception) {
      reportFatalIOError("Can't open output file", outputFile, exception);
    }
  }

  private String getPackageName(File dir)
  {
    String packName = dir.getName();
    while (dir != null && !dir.getName().equals("net")) {
      dir = dir.getParentFile();
      packName = dir.getName() + "." + packName;
    }
    return packName;
  }

  private void writeHeader()
  {
    mOutput.println("//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-");
    mOutput.println("//###########################################################################");
    mOutput.println("//# DO NOT EDIT - AUTOMATICALLY GENERATED BY:");
    mOutput.print("//# ");
    mOutput.println(getClass().getName());

    try {
      BufferedReader header = null;
      try {
        final Reader reader = new FileReader(mHeaderFile);
        header = new BufferedReader(reader);
        for (String line = header.readLine(); line != null; line =
          header.readLine()) {
          mOutput.println(line);
        }
      } finally {
        if (header != null) {
          header.close();
        }
      }
    } catch (final IOException exception) {
      reportFatalIOError("Can't read header file", mHeaderFile, exception);
    }

    mOutput.println();
    mOutput.print("package ");
    mOutput.print(mOutputPackageName);
    mOutput.println(';');
    mOutput.println();
    boolean hasImport = false;
    for (final EnumInfo enumInfo : mEnumTypes) {
      if (enumInfo.hasPrintableDefault()) {
        enumInfo.printImport();
        hasImport = true;
      }
    }
    if (hasImport) {
      mOutput.println();
    } else {
      mEnumTypes.clear();
    }
    mOutput.println();
    mOutput.print("public class ");
    mOutput.print(mClassName);
    mOutput.println(" {");
    mOutput.println();
    if (mTargetNameSpace.length() > 0) {
      writeStringDeclaration("NAMESPACE", mTargetNameSpace);
    }
    writeIntDeclaration("NUMBER_OF_ELEMENTS", mElements.size());
    mOutput.println();
  }

  private void writePrefixedStringDeclaration(final String prefix, final String name)
  {
    writeStringDeclaration(prefix + "_" + name, name);
  }

  private void writeIntDeclaration(final String name, final int value)
  {
    mOutput.print("  public static final int ");
    mOutput.print(name);
    mOutput.print(" = ");
    mOutput.print(value);
    mOutput.println(';');
  }

  private void writeStringDeclaration(final String name, final String value)
  {
    mOutput.print("  public static final String ");
    mOutput.print(name);
    mOutput.print(" = \"");
    mOutput.print(value);
    mOutput.println("\";");
  }

  private void writeTrailer()
  {
    mOutput.println();
    mOutput.println('}');
  }


  private void reportFatalIOError(final String message,
                                  final File file,
                                  final IOException exception)
  {
    System.err.print("FATAL: ");
    System.err.print(message);
    System.err.print(' ');
    System.err.print(file);
    final String part2 = exception.getMessage();
    if (part2 == null) {
      System.err.println('!');
    } else {
      System.err.print(": ");
      System.err.println(part2);
    }
    System.exit(1);
  }


  //#########################################################################
  //# Inner Class AttributeInfo
  private class AttributeInfo implements Comparable<AttributeInfo>
  {
    //#######################################################################
    //# Constructors
    private AttributeInfo(final String name)
    {
      this(name, null, null);
    }

    private AttributeInfo(final String name,
                          final String type,
                          final String defaultValue)
    {
      mName = name;
      mType = type;
      mDefaultValue = defaultValue;
    }

    //#######################################################################
    //# Interface java.lang.Comparable<AttributeInfo>
    @Override
    public int compareTo(final AttributeInfo attrib)
    {
      return mName.compareTo(attrib.mName);
    }

    //#######################################################################
    //# Auxiliary Methods
    private void print()
    {
      writePrefixedStringDeclaration("ATTRIB", mName);
      if (mDefaultValue != null) {
        mOutput.print("  public static final ");
        mOutput.print(mType);
        mOutput.print(" DEFAULT_");
        mOutput.print(mName);
        mOutput.print(" = ");
        mOutput.print(mDefaultValue);
        mOutput.println(';');
      }
    }

    //#######################################################################
    //# Data Members
    private final String mName;
    private final String mType;
    private final String mDefaultValue;
  }


  //#########################################################################
  //# Inner Class EnumInfo
  private class EnumInfo implements Comparable<EnumInfo>
  {
    //#######################################################################
    //# Constructors
    private EnumInfo(final String name)
    {
      mName = name;
      mDefaultValue = null;
    }

    //#######################################################################
    //# Interface java.lang.Comparable<AttributeInfo>
    @Override
    public int compareTo(final EnumInfo info)
    {
      return mName.compareTo(info.mName);
    }

    //#######################################################################
    //# Auxiliary Methods
    private String getName()
    {
      return mName;
    }

    private void provideDefaultValue(final String defaultValue)
    {
      if (mDefaultAmbiguous) {
        // skip
      } else if (mDefaultValue == null) {
        mDefaultValue = defaultValue;
      } else if (mDefaultValue.equals(defaultValue)) {
        // skip
      } else {
        mDefaultAmbiguous = true;
        mDefaultValue = null;
      }
    }

    private boolean hasPrintableDefault()
    {
      return mDefaultValue != null && !mDefaultAmbiguous;
    }

    private void printImport()
    {
      mOutput.print("import ");
      mOutput.print(mCoreClassPackageName);
      mOutput.print('.');
      mOutput.print(mName);
      mOutput.println(';');
    }

    private void printDefault()
    {
      if (hasPrintableDefault()) {
        mOutput.print("  public static final ");
        mOutput.print(mName);
        mOutput.print(" DEFAULT_");
        mOutput.print(mName);
        mOutput.print(" = ");
        mOutput.print(mName);
        mOutput.print('.');
        mOutput.print(mDefaultValue);
        mOutput.println(';');
      }
    }

    //#######################################################################
    //# Data Members
    private final String mName;
    private String mDefaultValue;
    private boolean mDefaultAmbiguous;
  }


  //#########################################################################
  //# Data Members
  private final File mHeaderFile;
  private final File mCoreClassDirectory;
  private final String mCoreClassPackageName;
  private final DocumentBuilder mBuilder;
  private final Collection<String> mElements;
  private final Collection<AttributeInfo> mAttributes;
  private final Collection<EnumInfo> mEnumTypes;

  private String mOutputPackageName;
  private String mClassName;
  private PrintStream mOutput = System.out;
  private String mTargetNameSpace;


  //#########################################################################
  //# Class Constants
  private static final String NAMESPACE = "http://www.w3.org/2001/XMLSchema";

}
