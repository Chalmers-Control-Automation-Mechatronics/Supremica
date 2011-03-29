//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.external.promela
//# CLASS:   PromelaTools
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.external.promela;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.external.promela.parser.PromelaLexer;
import net.sourceforge.waters.external.promela.parser.PromelaParser;

import org.anarres.cpp.InputLexerSource;
import org.anarres.cpp.LexerException;
import org.anarres.cpp.Preprocessor;
import org.anarres.cpp.PreprocessorListener;
import org.anarres.cpp.Source;
import org.anarres.cpp.Token;
import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;


public class PromelaTools
{

  //#########################################################################
  //# Invocation
  public void parseString(final String promelaCode) throws IOException
  {
    Preprocessor preProcessor = null;
    try {
      preProcessor =
          new Preprocessor(new InputLexerSource(new ByteArrayInputStream(
              promelaCode.getBytes())));
      parseInternal(preProcessor);
    } catch (final RecognitionException e) {
      final PromelaParserError error =
          new PromelaParserError(-1, -1, "Promela code cannot be recognized: "
              + e.getMessage(), true);
      mErrors.add(error);
    } catch (final LexerException e) {
      // is handled by the preprocessorlistener
    }
  }

  // ---------------------------------------------------------------

  public void parseFile(final String promelaFilename) throws IOException
  {
    Preprocessor preProcessor = null;
    try {
      final File f = new File(promelaFilename);
      preProcessor = new Preprocessor(f);
      parseInternal(preProcessor);
    } catch (final RecognitionException e) {
      final PromelaParserError error =
          new PromelaParserError(-1, -1, "Promela code cannot be recognized: "
              + e.getMessage(), true);
      mErrors.add(error);
    } catch (final LexerException e) {
      // is handled by the preprocessorlistener
    }
  }


  //#########################################################################
  //# Simple Access
  public boolean isSyntacticallyCorrect()
  {
    return mIsSyntacticallyCorrect;
  }

  public List<PromelaParserError> getErrors()
  {
    return mErrors;
  }

  public String getPreprocessedCode()
  {
    return mPreprocessedCode;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void printTree(final CommonTree t, final int indent)
  {
    if (t != null) {
      StringBuffer sb = new StringBuffer(indent);
      for (int i = 0; i < indent; i++)
        sb = sb.append("   ");
      for (int i = 0; i < t.getChildCount(); i++) {
        System.out.println(sb.toString() + t.getChild(i).toString());
        printTree((CommonTree) t.getChild(i), indent + 1);
      }
    }
  }

  private void parseInternal(final Preprocessor preProcessor)
      throws IOException, LexerException, RecognitionException
  {
    preProcessor.setListener(new PreprocessorListener() {
      public void handleError(final Source source, final int line,
                              final int column, final String msg)
      {
        final PromelaParserError error =
            new PromelaParserError(line, column, msg, true);
        mErrors.add(error);
      }

      public void handleWarning(final Source source, final int line,
                                final int column, final String msg)
      {
        final PromelaParserError error =
            new PromelaParserError(line, column, msg, false);
        mErrors.add(error);
      }
    });
    final StringBuffer preprocessedText = new StringBuffer();
    Token tok = preProcessor.token();
    do {
      preprocessedText.append(tok.getText());
      tok = preProcessor.token();
    } while (tok.getText() != null);

    mPreprocessedCode = preprocessedText.toString();

    if (mErrors.size() > 0)
      return;

    final PromelaLexer lexer =
        new PromelaLexer(new ANTLRReaderStream(new StringReader(
            preprocessedText.toString())));
    final CommonTokenStream tokens = new CommonTokenStream(lexer);
    final PromelaParser parser = new PromelaParser(tokens);
    final CommonTree t = (CommonTree) parser.specRule().getTree();
    if (parser.isSyntacticallyCorrect()) {
      mIsSyntacticallyCorrect = true;
      // CommonTree t = (CommonTree) parser.getTree();
      // ASTFrame af = new ASTFrame("Tree", t);
      // af.setVisible(true);
      printTree(t, 0);
    } else {
      mErrors.addAll(parser.getErrors());
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mIsSyntacticallyCorrect = false;
  private final List<PromelaParserError> mErrors =
      new ArrayList<PromelaParserError>();
  private String mPreprocessedCode = null;

}
