//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: 
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFAState
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.plain.module.SimpleNodeElement;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

/**
 *
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAState
{

  public SimpleEFAState(final SimpleNodeProxy node)
  {
    mFactory = null;
    if (node instanceof SimpleNodeSubject) {
      mFactory = ModuleSubjectFactory.getInstance();
    } else if (node instanceof SimpleNodeElement) {
      mFactory = ModuleElementFactory.getInstance();
    }
    mName = node.getName();
    mIsInitial = node.isInitial();
    mAttributes = new HashMap<>(node.getAttributes());
    PlainEventListProxy propositions = node.getPropositions();
    mPropositions = propositions != null ? propositions
                    : mFactory.createPlainEventListProxy();
    mHelper = new EFAHelper(mFactory);
    mIsMarked = mHelper.containsMarkingProposition(mPropositions);
    mIsForbidden = mHelper.containsForbiddenProposition(mPropositions);
    mInitialArrowGeometry = node.getInitialArrowGeometry();
    mLabelGeometry = node.getLabelGeometry();
    mPointGeometry = node.getPointGeometry();
}

  public SimpleEFAState(final String name,
                        final boolean isInitial, final boolean isMarked,
                        final boolean isForbidden,
                        final Map<String, String> attributes,
                        final ModuleProxyFactory factory)
  {
    mName = name;
    mIsInitial = isInitial;
    mIsMarked = isMarked;
    mIsForbidden = isForbidden;
    mHelper = new EFAHelper(factory);
    mFactory = factory != null ? factory : ModuleSubjectFactory.getInstance();
    mAttributes = attributes != null ? attributes
                  : new HashMap<String, String>();
  }

  public SimpleEFAState(final String name,
                        final boolean isInitial,
                        final boolean isMarked)
  {
    this(name, isInitial, isMarked, false, null, null);
  }

  public String getName()
  {
    return mName;
  }

  public void setName(String name)
  {
    mName = name;
  }

  public void setInitial(final boolean isInitial)
  {
    mIsInitial = isInitial;
  }

  public boolean isInitial()
  {
    return mIsInitial;
  }

  public void setMarked(final boolean isMarked)
  {
    mIsMarked = isMarked;
  }

  public boolean isMarked()
  {
    return mIsMarked;
  }

  public void setForbidden(final boolean isForbidden)
  {
    mIsForbidden = isForbidden;
  }

  public boolean isForbidden()
  {
    return mIsForbidden;
  }

  public Map<String, String> getAttributes()
  {
    return mAttributes.isEmpty() ? null : mAttributes;
  }

  public void setAttributes(Map<String, String> attributes)
  {
    mAttributes = attributes;
  }

  private PlainEventListProxy createPropositions(PlainEventListProxy proposition)
  {
    List<Proxy> list = new ArrayList<>(proposition.getEventIdentifierList());
    if (mIsForbidden && !mHelper.containsForbiddenProposition(proposition)) {
      list.add(mHelper.getForbiddenIdentifier());
    }
    if (mIsMarked && !mHelper.containsMarkingProposition(proposition)) {
      list.add(mHelper.getMarkingIdentifier());
    }
    if (list.isEmpty()) {
      return null;
    } else {
      return mFactory.createPlainEventListProxy(list);
    }
  }

  private PlainEventListProxy getPropositions()
  {
    return createPropositions(mPropositions);
  }

  public void setPropositions(PlainEventListProxy propositions)
  {
    mPropositions = propositions;
  }

  public SimpleNodeProxy getSimpleNode()
  {
    return mFactory.createSimpleNodeProxy(mName, getPropositions(),
                                          getAttributes(), mIsInitial,
                                          mPointGeometry, mInitialArrowGeometry,
                                          mLabelGeometry);
  }

  public void addToAttribute(final String key, final String value)
  {
    mAttributes.put(key, value);
  }

  public String getAttribute(final String key)
  {
    return mAttributes.get(key);
  }

  public void mergeToAttribute(final String key, final String value,
                               final String separator)
  {
    if (mAttributes != null && mAttributes.containsKey(key)) {
      final String oValue = mAttributes.get(key);
      final String nValue = oValue + separator + value;
      addToAttribute(key, nValue);
    } else {
      addToAttribute(key, value);
    }
  }

  @Override
  public String toString()
  {
    return getSimpleNode().toString();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof SimpleNodeProxy) {
      SimpleNodeProxy otherNode = (SimpleNodeProxy) obj;
      SimpleNodeProxy thisNode = getSimpleNode();
      if (otherNode.compareTo(thisNode) == 0) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 97 * hash + Objects.hashCode(this.mName);
    return hash;
  }

  private ModuleProxyFactory mFactory;
  private boolean mIsInitial = false;
  private boolean mIsMarked = false;
  private boolean mIsForbidden = false;
  private Map<String, String> mAttributes;
  private PlainEventListProxy mPropositions;
  private String mName;
  private final EFAHelper mHelper;
  private PointGeometryProxy mInitialArrowGeometry = null;
  private LabelGeometryProxy mLabelGeometry = null;
  private PointGeometryProxy mPointGeometry = null;
}
