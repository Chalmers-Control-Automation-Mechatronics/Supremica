package net.sourceforge.waters.external.promela.ast;

import java.util.List;

import net.sourceforge.waters.external.promela.PromelaMType;
import net.sourceforge.waters.external.promela.PromelaVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

import org.antlr.runtime.*;

public class ModuleTreeNode extends PromelaTree
{
  public ModuleTreeNode(final Token token, final PromelaMType mtype)
  {
    super(token);
    mEx = token.getText();
    mMTypes = mtype;
  }

  public ModuleTreeNode(final int token, final PromelaMType mtype)
  {
    this((Token)new CommonToken(token,"Root"), mtype);
    mEx = "Root";
  }

  public String toString()
  {
      return "Module";
  }

  private final PromelaMType mMTypes;
  private String mEx;

  public String getValue()
  {
      return mEx;
  }

  public SimpleExpressionProxy getMtypeRange(final ModuleProxyFactory factory)
  {
    return mMTypes.getRangeExpression(factory);
  }

  public List<String> getMtypes()
  {
    return mMTypes.getMTypes();
  }

  public void addMtype(final String mtype)
  {
    mMTypes.addMType(mtype);
  }

  public Object acceptVisitor(final PromelaVisitor visitor)
  {
    return  visitor.visitModule(this);
  }
 }
