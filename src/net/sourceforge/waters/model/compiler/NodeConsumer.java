//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   NodeConsumer
//###########################################################################
//# $Id: NodeConsumer.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;


interface NodeConsumer {

  public void processNode(CompiledNode entry);

}
