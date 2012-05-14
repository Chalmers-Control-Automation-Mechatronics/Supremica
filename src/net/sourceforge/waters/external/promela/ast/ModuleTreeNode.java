package net.sourceforge.waters.external.promela.ast;

import java.util.ArrayList;

import net.sourceforge.waters.external.promela.PromelaVisitor;
import org.antlr.runtime.*;

public class ModuleTreeNode extends PromelaTree
{
  public ModuleTreeNode(final Token token)
  {
    super(token);
    mEx = token.getText();
  }

  public ModuleTreeNode(final int token)
  {
    this((Token)new CommonToken(token,"Root"));
    mEx = "Root";
  }

  public String toString()
  {
      return "Module";
  }

  private ArrayList<String> mMtypes;
  private String mEx;

  public String getValue()
  {
      return mEx;
  }

  public ArrayList<String> getMtypes()
  {
    if(mMtypes == null)
      mMtypes = new ArrayList<String>();

    return mMtypes;
  }

  public void addMtype(final String mtype)
  {
    if(mMtypes == null)
      mMtypes = new ArrayList<String>();

    mMtypes.add(mtype);
  }

  public Object acceptVisitor(final PromelaVisitor visitor)
  {
    return  visitor.visitModule(this);
  }
 }
