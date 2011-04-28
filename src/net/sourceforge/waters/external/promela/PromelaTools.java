package net.sourceforge.waters.external.promela;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.external.promela.ast.*;

import net.sourceforge.waters.external.promela.parser.PromelaLexer;
import net.sourceforge.waters.external.promela.parser.PromelaParser;
import net.sourceforge.waters.model.base.ProxyTools;

import org.anarres.cpp.InputLexerSource;
import org.anarres.cpp.LexerException;
import org.anarres.cpp.LexerSource;
import org.anarres.cpp.Preprocessor;
import org.anarres.cpp.PreprocessorListener;
import org.anarres.cpp.Source;
import org.anarres.cpp.Token;
import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;

public class PromelaTools {

    private boolean isSyntacticallyCorrect = false;
    private final ArrayList<PromelaParserError> errors = new ArrayList<PromelaParserError>();
    private String preprocessedCode = null;
    //hashtable to store chan informations
    private final Hashtable<String, ChanInfo> chan = new Hashtable<String,ChanInfo>();
    private final Hashtable<String, ArrayList<List<String>>> component = new Hashtable<String,ArrayList<List<String>>>();
    // ---------------------------------------------------------------

    public void parseString(final String promelaCode) throws IOException {
        Preprocessor preProcessor = null;
        try {
            preProcessor = new Preprocessor(new InputLexerSource(new ByteArrayInputStream(promelaCode.getBytes())));
            parseInternal(preProcessor);
        } catch (final RecognitionException e) {
            final PromelaParserError error = new PromelaParserError(-1, -1, "Promela code cannot be recognized: " + e.getMessage(), true);
            errors.add(error);
        } catch (final LexerException e) {
            // is handled by the preprocessorlistener
        }
    }

    // ---------------------------------------------------------------

    public CommonTree parseStream(final InputStream input)
      throws IOException, LexerException, RecognitionException
    {
      // final File f = new File(promelaFilename);
      // final InputStream stream = new FileInputStream(f);
      final Reader reader = new InputStreamReader(input);
      final LexerSource source = new LexerSource(reader,true);
      final Preprocessor preProcessor = new Preprocessor(source);
      return parseInternal(preProcessor);
    }

    public void parseFile(final String promelaFilename) throws IOException {
        Preprocessor preProcessor = null;
        try {
            final File f = new File(promelaFilename);
            final InputStream stream = new FileInputStream(f);
            final Reader reader = new InputStreamReader(stream);
            final LexerSource source = new LexerSource(reader,true);
            preProcessor = new Preprocessor(source);
            parseInternal(preProcessor);
        } catch (final RecognitionException e) {
            final PromelaParserError error = new PromelaParserError(-1, -1, "Promela code cannot be recognized: " + e.getMessage(), true);
            errors.add(error);
        } catch (final LexerException e) {
            // is handled by the preprocessorlistener
        }
    }

    // ---------------------------------------------------------------
    @SuppressWarnings("unused")
    private void printTree(final CommonTree t, final int indent) {
    if ( t != null ) {

        StringBuffer sb = new StringBuffer(indent);
        //System.out.println(sb.toString() + t.toString());
        for ( int i = 0; i < indent; i++ ){
            sb = sb.append("   ");
        }

        System.out.println(sb.toString() + t.token.getType() + ":" +
                           t.getText() + " <" + ProxyTools.getShortClassName(t) + ">");

        for ( int i = 0; i < t.getChildCount(); i++ ) {

            //System.out.println(sb.toString() + t.getChild(i).toString());
            printTree((CommonTree)t.getChild(i), indent+1);
        }
    }
    }
 ///////////

    @SuppressWarnings("unused")
    private void getdata(final StringBuilder s,final CommonTree t,final ArrayList<String> a){
        if(!t.isNil()){
            s.append(t.getText());
            a.add(t.getText());

            if(t.getChildCount()>0){
                s.append(",");
                getdata(s,(CommonTree)t.getChild(0),a);
            }else{
                s.append("]");
            }
        }

    }

    public Hashtable<String,ChanInfo> getchan(){
        return chan;
    }
    private void collectMsg(final CommonTree t){


      if(t!=null){

          if(t instanceof TypeTreeNode && t.getText().equals("chan")){
              final CommonTree tr1 = (CommonTree)t.getChild(1);
              final String name = t.getChild(0).getText();
              final int length = Integer.parseInt(tr1.getChild(0).getText());
              final int datalength = tr1.getChildCount()-2;
              chan.put(name,new ChanInfo(name, length, datalength));


          }

          if(t instanceof ProctypeTreeNode){

            final ArrayList<List<String>> componentLabels = new ArrayList<List<String>>();
            final String proctypeName = t.getText();
            if(!component.containsKey(proctypeName)){
              component.put(proctypeName,componentLabels);
            }
              for(int b =0;b<t.getChildCount();b++){

                  if(t.getChild(b).toString().equals("STATEMENT")){
                  final CommonTree tr = (CommonTree)t.getChild(b);

                  for(int a=0;a<tr.getChildCount();a++){
                    final CommonTree childA = (CommonTree) tr.getChild(a);
                   // if(childA.toString().equals("Exchange")){
                    if(childA instanceof ExchangeTreeNode){
                        if(childA.getText().equals("!")|| tr.getChild(a).getText().equals("!!")){


                        final ArrayList<String> data =new ArrayList<String>();

                       // final StringBuilder sb = new StringBuilder();
                       // sb.append(childA.getChild(0).getText());
                        final String n = childA.getChild(0).getText();

                        chan.get(childA.getChild(0).getText()).incSendnumber();
                        final CommonTree msgargs = (CommonTree) childA.getChild(1); //message statement

                        final ArrayList<String> labels = new ArrayList<String>();
                        //add event name
                        labels.add(n);

                        for(int y = 0; y <msgargs.getChildCount();y++){
                          final CommonTree childY = (CommonTree) msgargs.getChild(y);

                          if(childY instanceof ConstantTreeNode){
                          //  sb.append(childY.getText());
                            data.add(childY.getText());
                            System.out.println(childY.getText());
                            //add all event data
                            labels.add(childY.getText());
                          }
                        }

                        //store proctype name and relevant data into hashtable
                        chan.get(childA.getChild(0).getText()).storeMsg(data);

                        //add this event info to event list
                        componentLabels.add(labels);
                        }

                        //still need to handle recieve statement
                        if(childA.getText().equals("?")|| tr.getChild(a).getText().equals("??")){
                          chan.get(childA.getChild(0).getText()).incRecnumber();
                          //final ArrayList<String> recEverything = new ArrayList<String>();
                          componentLabels.add(null);
                        }
                    }
                  }
                    component.put(proctypeName, componentLabels);
                  }
              }
          }

          if(t instanceof InitialTreeNode){
            final CommonTree childI = (CommonTree) t.getChild(0);
            if(childI.getChild(0).getText().equals("atomic")){
              final ArrayList<String> temp = new ArrayList<String>();
              temp.add("init");
              //insert this particular event into first place of event label list, for each component
              for (final Map.Entry<String,ArrayList<List<String>>> entry : component.entrySet()) {
                entry.getValue().add(0,temp);
              }
            }
          }

          for(int i = 0; i < t.getChildCount();i++){
              collectMsg((CommonTree)t.getChild(i));
          }
      }
  }
    @SuppressWarnings("unused")
    private void print_chan(){
       // System.out.println(chan.get("name").getValue().toString());
      //System.out.println(chan);
      final Enumeration<String> e = chan.keys();
      while(e.hasMoreElements()){
        final String name = (String)e.nextElement();
        System.out.println(name+" "+chan.get(name).getValue());
      }
    }
    @SuppressWarnings("unused")
    private void print_label(){
      for(final Map.Entry<String,ArrayList<List<String>>> entry: component.entrySet()){
        System.out.println(entry.getKey()+": "+entry.getValue());
      }
    }
 ///////////
    private CommonTree parseInternal(final Preprocessor preProcessor) throws IOException,
            LexerException, RecognitionException {
        preProcessor.setListener(new PreprocessorListener() {
            public void handleError(final Source source, final int line, final int column, final String msg) {
                final PromelaParserError error = new PromelaParserError(line, column, msg, true);
                errors.add(error);
            }

            public void handleWarning(final Source source, final int line, final int column, final String msg) {
                final PromelaParserError error = new PromelaParserError(line, column, msg, false);
                errors.add(error);
            }
        });
        final StringBuffer preprocessedText = new StringBuffer();
        Token tok = preProcessor.token();
        do {
            preprocessedText.append(tok.getText());
            tok = preProcessor.token();
        } while (tok.getText() != null);

        this.preprocessedCode = preprocessedText.toString();

        if (errors.size() > 0)
            return null;

        final PromelaLexer lexer = new PromelaLexer(new ANTLRReaderStream(
                new StringReader(preprocessedText.toString())));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final PromelaParser parser = new PromelaParser(tokens);
        final PromelaParser.specRule_return r = parser.specRule();
        final CommonTree t = (CommonTree)r.getTree();

    /*  CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        nodes.setTokenStream(tokens);
        PromelaWalker walker = new PromelaWalker(nodes);
        walker.specRule();
    */
        if (parser.isSyntacticallyCorrect()) {
            isSyntacticallyCorrect = true;
            collectMsg(t);
           // print_label();
            //print_chan();
            //printTree(t,0);

            //System.out.println(t.toStringTree());
        //modified
        /*
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        nodes.setTokenStream(tokens);
        PromelaWalker walker = new PromelaWalker(nodes);
        walker.specRule();
        */

        } else {
            errors.addAll(parser.getErrors());
        }
        return t;
    }

    // ---------------------------------------------------------------

    public boolean isSyntacticallyCorrect() {
        return isSyntacticallyCorrect;
    }

    // ---------------------------------------------------------------

    public ArrayList<PromelaParserError> getErrors() {
        return errors;
    }

    // ---------------------------------------------------------------

    public String getPreprocessedCode() {
        return preprocessedCode;
    }

}
