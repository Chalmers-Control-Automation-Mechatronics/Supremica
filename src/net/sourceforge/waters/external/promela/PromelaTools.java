package net.sourceforge.waters.external.promela;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;

import net.sourceforge.waters.external.promela.parse.PromelaLexer;
import net.sourceforge.waters.external.promela.parse.PromelaParser;

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

public class PromelaTools {

    private boolean isSyntacticallyCorrect = false;
    private final ArrayList<PromelaParserError> errors = new ArrayList<PromelaParserError>();
    private String preprocessedCode = null;
    //hashtable to store chan informations
    private final Hashtable<String, ChanInfo> chan = new Hashtable<String,ChanInfo>();

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

    public void parseFile(final String promelaFilename) throws IOException {
        Preprocessor preProcessor = null;
        try {
            final File f = new File(promelaFilename);
            preProcessor = new Preprocessor(f);
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

        System.out.println(sb.toString() + t.token.getType() + ":" + t.toString());

        for ( int i = 0; i < t.getChildCount(); i++ ) {

            //System.out.println(sb.toString() + t.getChild(i).toString());
            printTree((CommonTree)t.getChild(i), indent+1);
        }
    }
    }
 ///////////

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
    private void print_Tree(final CommonTree t){
        if(t!=null){

            if(t.getText().equals("chan")){
                final CommonTree tr1 = (CommonTree)t.getChild(1);
                final String name = t.getChild(0).getText();
                final int length = Integer.parseInt(tr1.getChild(0).getText());
                final int datalength = tr1.getChildCount()-2;
                chan.put(name,new ChanInfo(name, length, datalength));


            }

            if(t.toString().equals("Proctype")){
                for(int b =0;b<t.getChildCount();b++){

                    if(t.getChild(b).toString().equals("STATEMENT")){
                    final CommonTree tr = (CommonTree)t.getChild(b);

                    for(int a=0;a<tr.getChildCount();a++){
                      if(tr.getChild(a).toString().equals("Exchange")){
                          if(tr.getChild(a).getText().equals("!")|| tr.getChild(a).getText().equals("!!")){


                          final ArrayList<String> data =new ArrayList<String>();
                          final StringBuilder sb = new StringBuilder();
                          sb.append(tr.getChild(a).getChild(0).getText());
                          sb.append(tr.getChild(a).getText()+"[");
                          chan.get(tr.getChild(a).getChild(0).getText()).incsendnumber();

                          if(tr.getChild(a).getChild(1).toString().equals("Constant") )
                              {
                                  getdata(sb,(CommonTree)tr.getChild(a).getChild(1),data);

                              }
                          //testing event output
                 //         System.out.println(sb.toString());

                          //store proctype name and relevant data into hashtable
                          chan.get(tr.getChild(a).getChild(0).getText()).storeMsg(data);

                          }
                          if(tr.getChild(a).getText().equals("?")|| tr.getChild(a).getText().equals("??")){
                            chan.get(tr.getChild(a).getChild(0).getText()).increcnumber();
                          }
                      }
                    }
                    }
                }

            }
            for(int i = 0; i < t.getChildCount();i++){

                print_Tree((CommonTree)t.getChild(i));

            }
        }
    }
    private void print_chan(){
        System.out.println(chan.get("name").getValue().toString());
    }

 ///////////
    private void parseInternal(final Preprocessor preProcessor) throws IOException,
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
            return;

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
            print_Tree(t);
            print_chan();
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
