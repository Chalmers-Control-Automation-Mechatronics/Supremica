//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT:
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFAState
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import gnu.trove.map.hash.THashMap;

import java.util.ArrayList;
import java.util.List;
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
    mAttributes = new THashMap<>(node.getAttributes());
    final PlainEventListProxy propositions = node.getPropositions();
    mPropositions = propositions != null ? propositions
                    : mFactory.createPlainEventListProxy();
    mHelper = new SimpleEFAHelper(mFactory);
    mIsMarked = mHelper.containsMarkingProposition(mPropositions);
    mIsForbidden = mHelper.containsForbiddenProposition(mPropositions);
    mInitialArrowGeometry = node.getInitialArrowGeometry();
    mLabelGeometry = node.getLabelGeometry();
    mPointGeometry = node.getPointGeometry();
    mStateValue = "";
  }

  public SimpleEFAState(final SimpleEFAState state)
  {
    mFactory = ModuleSubjectFactory.getInstance();
    mName = state.getName();
    mIsInitial = state.isInitial();
    final THashMap<String, String> att = state.getAttributes();
    if (att != null) {
      mAttributes = new THashMap<>(state.getAttributes());
    } else {
      mAttributes = new THashMap<>();
    }

    final PlainEventListProxy propositions = state.getPropositions();
    mPropositions = propositions != null ? propositions
                    : mFactory.createPlainEventListProxy();
    mHelper = new SimpleEFAHelper(mFactory);
    mIsMarked = mHelper.containsMarkingProposition(mPropositions);
    mIsForbidden = mHelper.containsForbiddenProposition(mPropositions);
    mInitialArrowGeometry = state.getSimpleNode().getInitialArrowGeometry();
    mLabelGeometry = state.getSimpleNode().getLabelGeometry();
    mPointGeometry = state.getSimpleNode().getPointGeometry();
    mStateValue = state.getStateValue();
  }

  public SimpleEFAState(final String name,
                        final boolean isInitial, final boolean isMarked,
                        final boolean isForbidden,
                        final THashMap<String, String> attributes,
                        final ModuleProxyFactory factory) {
    super();
    mName = name;
    mIsInitial = isInitial;
    mIsMarked = isMarked;
    mIsForbidden = isForbidden;
    mFactory = factory != null ? factory : ModuleSubjectFactory.getInstance();
    mHelper = new SimpleEFAHelper(mFactory);
    mPropositions = mFactory.createPlainEventListProxy();
    mAttributes = attributes != null ? attributes
                  : new THashMap<String, String>();
    mStateValue = "";
}

  public SimpleEFAState(final String name,
                        final boolean isInitial,
                        final boolean isMarked)
  {
    this(name, isInitial, isMarked, false, null, null);
  }

  public SimpleEFAState(final String name,
                        final boolean isInitial, final boolean isMarked,
                        final boolean isForbidden,
                        final THashMap<String, String> attributes)
  {
    this(name, isInitial, isMarked, isForbidden, attributes, null);
  }

  public String getName()
  {
    return mName;
  }

  public void setName(final String name)
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

  public THashMap<String, String> getAttributes()
  {
    return mAttributes.isEmpty() ? null : mAttributes;
  }

  public void setAttributes(final THashMap<String, String> attributes)
  {
    mAttributes = attributes;
  }

  private PlainEventListProxy createPropositions()
  {
    final List<Proxy> list = new ArrayList<>();
    final List<Proxy> eventIdentifierList = mPropositions.getEventIdentifierList();
    if (eventIdentifierList != null) {
      list.addAll(eventIdentifierList);
    }
    if (mIsForbidden && !mHelper.containsForbiddenProposition(mPropositions)) {
      list.add(mHelper.getForbiddenIdentifier());
    }
    if (!mIsForbidden) {
      list.remove(mHelper.getForbiddenIdentifier());
    }
    if (mIsMarked && !mHelper.containsMarkingProposition(mPropositions)) {
      list.add(mHelper.getMarkingIdentifier());
    }
    if (!mIsMarked) {
      list.remove(mHelper.getMarkingIdentifier());
    }
    if (list.isEmpty()) {
      return null;
    } else {
      return mFactory.createPlainEventListProxy(mFactory.getCloner()
              .getClonedList(list));
    }
  }

  private PlainEventListProxy getPropositions()
  {
    return createPropositions();
  }

  public void setPropositions(final PlainEventListProxy propositions)
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

  public void setStateValue(final String value)
  {
    mStateValue = value;
  }

  public String getStateValue()
  {
    return mStateValue;
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
    if (mAttributes != null && !value.isEmpty() && mAttributes.containsKey(key)) {
      String oValue = mAttributes.get(key);
      final String[] values = value.split(separator);
      for (final String v : values) {
        if (!oValue.contains(v)) {
          oValue += separator + v;
        }
      }
      addToAttribute(key, oValue);
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
  public boolean equals(final Object obj)
  {
    final SimpleNodeProxy thisNode = getSimpleNode();
    if (obj instanceof SimpleNodeProxy) {
      final SimpleNodeProxy otherNode = (SimpleNodeProxy) obj;
      if (otherNode.compareTo(thisNode) == 0) {
        return true;
      }
    } else if (obj instanceof SimpleEFAState) {
      final SimpleNodeProxy otherNode = ((SimpleEFAState) obj).getSimpleNode();
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
    hash = 97 * hash + Objects.hashCode(mName);
    return hash;
  }

  private ModuleProxyFactory mFactory;
  private boolean mIsInitial;
  private boolean mIsMarked;
  private boolean mIsForbidden;
  private THashMap<String, String> mAttributes;
  private PlainEventListProxy mPropositions;
  private String mName;
  private final SimpleEFAHelper mHelper;
  private PointGeometryProxy mInitialArrowGeometry;
  private LabelGeometryProxy mLabelGeometry;
  private PointGeometryProxy mPointGeometry;
  private String mStateValue;
}
