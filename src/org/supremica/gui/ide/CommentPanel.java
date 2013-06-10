//# -*- tab-width: 4  indent-tabs-mode: null  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   CommentPanel
//###########################################################################
//# $Id$
//###########################################################################


package org.supremica.gui.ide;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.StyledDocument;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.EditModuleCommentCommand;
import net.sourceforge.waters.gui.command.EditModuleNameCommand;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.util.CharacterDocumentFilter;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.ModuleSubject;


/**
 * A panel with editable information about a module. The information is
 * divided into a header (the name of the module) and a body (the comment of
 * the module).
 *
 * @author Knut &Aring;kesson, Robi Malik
 */

class CommentPanel extends JPanel
{

  //#########################################################################
  //# Constructor
  public CommentPanel(final ModuleContainer container)
  {
    mModuleContainer = container;
    setLayout(new BorderLayout());

    // Create title
    final Border titleBorder = BorderFactory.createLoweredBevelBorder();
    final JTextPane titlePane = new JTextPane();
    titlePane.setBorder(titleBorder);
    titlePane.setBackground(EditorColor.BACKGROUNDCOLOR);
    titlePane.setFont(new Font(null, Font.BOLD, 14));
    add(BorderLayout.NORTH, titlePane);
    mTitleHandler = new TitlePaneHandler(titlePane);
    mTitleHandler.loadPane();
    titlePane.addFocusListener(mTitleHandler);

    // Create the comment text
    final Border commentBorder = BorderFactory.createLoweredBevelBorder();
    final JTextPane commentPane = new JTextPane();
    commentPane.setBorder(commentBorder);
    commentPane.setBackground(EditorColor.BACKGROUNDCOLOR);
    commentPane.setFont(new Font(null, Font.PLAIN, 12));
    final JScrollPane scroll = new JScrollPane(commentPane);
    scroll.setHorizontalScrollBarPolicy
      (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    add(BorderLayout.CENTER, scroll);
    final DocumentFilter filter = new XMLDocumentFilter();
    final AbstractDocument doc = (AbstractDocument) commentPane.getDocument();
    doc.setDocumentFilter(filter);
    mCommentHandler = new CommentPaneHandler(commentPane);
    mCommentHandler.loadPane();
    commentPane.addFocusListener(mCommentHandler);

    final ModuleSubject module = container.getModule();
    container.attach(new PendingSaveHandler());
    module.addModelObserver(new ModuleChangeHandler());
  }


  //#########################################################################
  //# Inner Class TextPaneHandler
  private abstract class TextPaneHandler extends FocusAdapter
  {

    //#######################################################################
    //# Constructor
    TextPaneHandler(final JTextPane pane)
    {
      mTextPane = pane;
    }

    //#######################################################################
    //# Interface java.awt.event.FocusListener
    @Override
    public void focusLost(final FocusEvent event)
    {
      commit();
    }

    //#######################################################################
    //# Invocation
    void loadPane()
    {
      try {
        final ModuleSubject module = mModuleContainer.getModule();
        final String text = getCurrentValue(module);
        final StyledDocument doc = mTextPane.getStyledDocument();
        final int len = doc.getLength();
        final String current = doc.getText(0, len);
        if (!text.equals(current)) {
          doc.remove(0, len);
          doc.insertString(0, text, null);
        }
      } catch (final BadLocationException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    void commit()
    {
      try {
        final StyledDocument doc = mTextPane.getStyledDocument();
        final int len = doc.getLength();
        final String text = doc.getText(0, len);
        final ModuleSubject module = mModuleContainer.getModule();
        final String current = getCurrentValue(module);
        if (text.equals(current)) {
          return;
        }
        final Command cmd = createCommand(text);
        mModuleContainer.executeCommand(cmd);
      } catch (final BadLocationException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    //#######################################################################
    //# Abstract Methods
    abstract String getCurrentValue(ModuleSubject module);

    abstract Command createCommand(String text);

    //#######################################################################
    //# Data Members
    private final JTextPane mTextPane;

  }


  //########################################################################
  //# Inner Class XMLDocumentFilter
  private static class XMLDocumentFilter
    extends CharacterDocumentFilter
  {

    //#######################################################################
    //# Overrides for net.sourceforge.waters.gui.CharacterDocumentFilter
    @Override
    protected boolean isAllowedCharacter(final char ch)
    {
      return ch == 0x9 || ch == 0xA || ch == 0xD ||
             ch >= 0x20 && ch <= 0xD7FF ||
             ch >= 0xE000 && ch <= 0xFFFD ||
             ch >= 0x10000 && ch <= 0x10FFFF;
    }

  }


  //#########################################################################
  //# Inner Class TitlePaneHandler
  private class TitlePaneHandler extends TextPaneHandler
  {

    //#######################################################################
    //# Constructor
    TitlePaneHandler(final JTextPane pane)
    {
      super(pane);
    }

    //#######################################################################
    //# Overrides for CommitHandler
    @Override
    Command createCommand(final String text)
    {
      return new EditModuleNameCommand(mModuleContainer, text);
    }

    @Override
    String getCurrentValue(final ModuleSubject module)
    {
      return module.getName();
    }

  }


  //#########################################################################
  //# Inner Class CommentPaneHandler
  private class CommentPaneHandler extends TextPaneHandler
  {

    //#######################################################################
    //# Constructor
    CommentPaneHandler(final JTextPane pane)
    {
      super(pane);
    }

    //#######################################################################
    //# Overrides for CommitHandler
    @Override
    Command createCommand(final String text)
    {
      return new EditModuleCommentCommand(mModuleContainer, text);
    }

    @Override
    String getCurrentValue(final ModuleSubject module)
    {
      final String comment = module.getComment();
      return comment == null ? "" : comment;
    }

  }


  //#########################################################################
  //# Inner Class ModuleChangeHandler
  private class ModuleChangeHandler implements ModelObserver
  {

    @Override
    public void modelChanged(final ModelChangeEvent event)
    {
      if (event.getSource() == mModuleContainer.getModule()) {
        switch (event.getKind()) {
        case ModelChangeEvent.NAME_CHANGED:
          mTitleHandler.loadPane();
          break;
        case ModelChangeEvent.STATE_CHANGED:
          mCommentHandler.loadPane();
          break;
        default:
          break;
        }
      }
    }

    @Override
    public int getModelObserverPriority()
    {
      return ModelObserver.RENDERING_PRIORITY;
    }

  }


  //#########################################################################
  //# Inner Class PendingSaveHandler
  private class PendingSaveHandler implements Observer
  {
    @Override
    public void update(final EditorChangedEvent event)
    {
      if (event.getKind() == EditorChangedEvent.Kind.PENDING_SAVE) {
        mTitleHandler.commit();
        mCommentHandler.commit();
      }
    }
  }


  //#########################################################################
  //# Class Constants
  private final ModuleContainer mModuleContainer;
  private final TitlePaneHandler mTitleHandler;
  private final CommentPaneHandler mCommentHandler;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
