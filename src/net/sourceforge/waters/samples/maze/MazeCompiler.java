//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.samples.maze
//# CLASS:   MazeCompiler
//###########################################################################
//# $Id: MazeCompiler.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################


package net.sourceforge.waters.samples.maze;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ProxyMarshaller;
import net.sourceforge.waters.model.base.UnexpectedWatersException;
import net.sourceforge.waters.model.expr.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.EventListProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleMarshaller;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.xsd.base.EventKind;


public class MazeCompiler
{

  //#########################################################################
  //# Constructors
  public MazeCompiler(final File inputdir,
		      final File outputdir)
    throws JAXBException
  {
    this(inputdir, outputdir, true, new ModuleMarshaller());
  }

  public MazeCompiler(final File inputdir,
		      final File outputdir,
		      final boolean bfs)
    throws JAXBException
  {
    this(inputdir, outputdir, bfs, new ModuleMarshaller());
  }

  public MazeCompiler(final File inputdir,
		      final File outputdir,
		      final ProxyMarshaller marshaller)
  {
    this(inputdir, outputdir, true, marshaller);
  }

  public MazeCompiler(final File inputdir,
		      final File outputdir,
		      final boolean bfs,
		      final ProxyMarshaller marshaller)
  {
    mInputDir = inputdir;
    mOutputDir = outputdir;
    mBFS = bfs;
    mMarshaller = marshaller;
    mReader = new MazeReader();
    mCopiedModules = new HashSet(16);
  }


  //#########################################################################
  //# Parameters
  public void setInputDir(final File inputdir)
  {
    mInputDir = inputdir;
    mCopiedModules.clear();
  }

  public void setOutputDir(final File outputdir)
  {
    mOutputDir = outputdir;
    mCopiedModules.clear();
  }

  public void setBFS(final boolean bfs)
  {
    mBFS = bfs;
  }


  //#########################################################################
  //# Compiling Maze Files
  public ModuleProxy compile(final String name)
    throws JAXBException, IOException, ModelException
  {
    final String extname = name + MAZEEXT;
    final File inputfile = new File(mInputDir, extname);
    final Maze maze = mReader.load(inputfile, name);
    if (mBFS) {
      maze.locateRocks();
    }
    maze.createActions();
    return createModule(maze);
  }

  private ModuleProxy createModule(final Maze maze)
    throws JAXBException, IOException, ModelException
  {
    final String name = maze.getName();
    final String extname = name + mMarshaller.getDefaultExtension();
    final File outputfile = new File(mOutputDir, extname);
    final ModuleProxy module = new ModuleProxy(name, outputfile);
    final List complist = module.getComponentList();
    final Collection squares = maze.getSquares();
    final Iterator iter = squares.iterator();
    while (iter.hasNext()) {
      final Square square = (Square) iter.next();
      final InstanceProxy inst = createInstance(module, square);
      complist.add(inst);
    }
    return module;
  }

  private InstanceProxy createInstance(final ModuleProxy module,
                                       final Square square)
    throws JAXBException, IOException, ModelException
  {
    final String templname = square.getTemplateName();
    copyModule(templname);
    final String name = square.getName();
    final SimpleIdentifierProxy ident = new SimpleIdentifierProxy(name);
    final InstanceProxy inst = new InstanceProxy(ident, templname);
    final List bindings = inst.getBindingList();
    final Iterator iter = square.getActionKinds().iterator();
    while (iter.hasNext()) {
      final Integer ikind = (Integer) iter.next();
      final int kind = ikind.intValue();
      final String param = Action.getTemplateName(kind);
      final Collection actions = square.getActions(kind);
      final ExpressionProxy value = createEventList(module, actions);
      final ParameterBindingProxy binding =
        new ParameterBindingProxy(param, value);
      bindings.add(binding);
    }
    return inst;
  }

  private ExpressionProxy createEventList(final ModuleProxy module,
                                          final Collection actions)
  {
    final Iterator iter = actions.iterator();
    if (actions.size() == 1) {
      final Action action = (Action) iter.next();
      return createEvent(module, action);
    } else {
      final EventListProxy eventlist = new EventListProxy();
      while (iter.hasNext()) {
        final Action action = (Action) iter.next();
        final ExpressionProxy event = createEvent(module, action);
        eventlist.add(event);
      }
      return new EventListExpressionProxy(eventlist);
    }
  }

  private ExpressionProxy createEvent(final ModuleProxy module,
                                      final Action action)
  {
    final String name = action.getName();
    if (module.getEventDeclaration(name) == null) {
      try {
        final EventDeclProxy decl =
          new EventDeclProxy(name, EventKind.UNCONTROLLABLE);
        module.insertEventDeclaration(decl);
      } catch (final DuplicateNameException exception) {
        throw new UnexpectedWatersException(exception);
      }
    }
    return new SimpleIdentifierProxy(name);
  }

  private void copyModule(final String name)
    throws JAXBException, IOException, ModelException
  {
    if (!mCopiedModules.contains(name)) {
      final String extname = name + mMarshaller.getDefaultExtension();
      final File source = new File(mInputDir, extname);
      final DocumentProxy module = mMarshaller.unmarshal(source);
      final File target = new File(mOutputDir, extname);
      mMarshaller.marshal(module, target);
      mCopiedModules.add(name);
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mBFS;
  private File mInputDir;
  private File mOutputDir;

  private final ProxyMarshaller mMarshaller;
  private final MazeReader mReader;
  private final Set mCopiedModules;

  private static final String MAZEEXT = ".txt";
}
