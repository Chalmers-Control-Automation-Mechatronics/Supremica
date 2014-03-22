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

import java.util.Objects;

import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

/**
 *
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAState
{

  public SimpleEFAState(final SimpleNodeProxy node)
  {
    mName = node.getName();
    mIsInitial = node.isInitial();
    mAttributes = new THashMap<>(node.getAttributes());
    mPropositions = node.getPropositions();
    final SimpleEFAHelper helper = new SimpleEFAHelper();
    mIsMarked = helper.containsMarkingProposition(mPropositions);
    mIsForbidden = helper.containsForbiddenProposition(mPropositions);
    mStateValue = "";
  }

  public SimpleEFAState(final SimpleEFAState state)
  {
    mName = state.mName;
    mIsInitial = state.mIsInitial;
    final THashMap<String, String> att = state.getAttributes();
    if (att != null) {
      mAttributes = new THashMap<>(state.getAttributes());
    } else {
      mAttributes = new THashMap<>();
    }
    mPropositions = state.mPropositions;
    final SimpleEFAHelper helper = new SimpleEFAHelper();
    mIsMarked = helper.containsMarkingProposition(mPropositions);
    mIsForbidden = helper.containsForbiddenProposition(mPropositions);
    mStateValue = state.mStateValue;
  }

  public SimpleEFAState(final String name,
                        final boolean isInitial,
                        final boolean isMarked,
                        final boolean isForbidden,
                        final THashMap<String, String> attributes) {
    mName = name;
    mIsInitial = isInitial;
    mIsMarked = isMarked;
    mIsForbidden = isForbidden;
    mPropositions = null;
    mAttributes = attributes != null ? attributes : new THashMap<String, String>();
    mStateValue = "";
}

  public SimpleEFAState(final String name,
                        final boolean isInitial,
                        final boolean isMarked)
  {
    this(name, isInitial, isMarked, false, null);
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

  public PlainEventListProxy getPropositions()
  {
    return mPropositions;
  }

  public void setPropositions(final PlainEventListProxy propositions)
  {
    mPropositions = propositions;
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
    if ((mAttributes != null) && !value.isEmpty() && mAttributes.containsKey(key)) {
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
    return mName;
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (obj instanceof SimpleEFAState) {
      final SimpleEFAState otherNode = (SimpleEFAState) obj;
      if (otherNode.mName.equalsIgnoreCase(mName)) {
        return true;
      }
    } else if (obj instanceof SimpleNodeProxy) {
      final SimpleNodeProxy otherNode = (SimpleNodeProxy) obj;
      if (otherNode.getName().equalsIgnoreCase(mName)) {
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

  private boolean mIsInitial;
  private boolean mIsMarked;
  private boolean mIsForbidden;
  private THashMap<String, String> mAttributes;
  private PlainEventListProxy mPropositions;
  private String mName;
  private String mStateValue;
}
