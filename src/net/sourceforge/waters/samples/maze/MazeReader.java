//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.samples.maze;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


class MazeReader
{

  //#########################################################################
  //# Constructors
  MazeReader()
  {
    SquareCreator creator;
    mSquareCreatorMap = new HashMap<String,SquareCreator>(16);
    creator = new SimpleSquareCreator() {
	Square createSquare(final Point pos) {return new SquareFree(pos);}
      };
    mSquareCreatorMap.put("", creator);
    creator = new SimpleSquareCreator() {
	Square createSquare(final Point pos) {return new SquareExit(pos);}
      };
    mSquareCreatorMap.put("exit", creator);
    creator = new SimpleSquareCreator() {
	Square createSquare(final Point pos) {return new SquareHero(pos);}
      };
    mSquareCreatorMap.put("hero", creator);
    creator = new SimpleSquareCreator() {
	Square createSquare(final Point pos) {return new SquareRock(pos);}
      };
    mSquareCreatorMap.put("rock", creator);
    creator = new KeySquareCreator() {
	Square createSquare(final Point pos, final String key)
	{return new SquareKey(pos, key);}
      };
    mSquareCreatorMap.put("key", creator);
    creator = new KeySquareCreator() {
	Square createSquare(final Point pos, final String key)
	{return new SquareDoor(pos, key);}
      };
    mSquareCreatorMap.put("door", creator);
    creator = new KeySquareCreator() {
	Square createSquare(final Point pos, final String key)
	{return new SquareGate(pos, key);}
      };
    mSquareCreatorMap.put("gate", creator);
  }


  //#########################################################################
  //# Parsing Maze Files
  Maze load(final URI uri, final String name) throws IOException
  {
    mURL = uri.toURL();
    final InputStream stream = mURL.openStream();
    final Reader raw = new InputStreamReader(stream);
    final Reader reader = new BufferedReader(raw);
    try {
      return load(reader, name);
    } finally {
      reader.close();
      mURL = null;
    }
  }

  Maze load(final Reader reader, final String name) throws IOException
  {
    try {
      mReader = reader;
      mMinX = Integer.MAX_VALUE;
      mMinY = Integer.MAX_VALUE;
      mLastChar = 0;
      mLineNo = 1;
      mSquares = new HashMap<Point,Square>();
      while (hasMoreSquares()) {
        final String description = nextSquare();
        parseSquare(description);
      }
      final Collection<Square> squares = mSquares.values();
      if (mMinX < 0 || mMinY < 0) {
        for (final Square square : squares) {
          final Point pos = square.getPosition();
          pos.x -= mMinX;
          pos.y -= mMinY;
        }
      }
      return new Maze(name, squares);
    } finally {
      mSquares = null;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean hasMoreSquares() throws IOException
  {
    try {
      do {
	getNextCharacter();
      } while (Character.isWhitespace(mLastChar));
      return true;
    } catch (final EOFException exception) {
      return false;
    }
  }

  private String nextSquare() throws IOException
  {
    final StringBuilder buffer = new StringBuilder();
    while (mLastChar != '.') {
      buffer.append(mLastChar);
      getNextCharacter();
    }
    return buffer.toString();
  }


  private Square parseSquare(final String description)
    throws MazeSyntaxException
  {
    int dashpos = description.indexOf('-');
    String typename;
    if (dashpos < 0) {
      dashpos = description.length();
      typename = "";
    } else {
      typename = extractWord(description, dashpos + 1);
    }
    int slashpos = description.indexOf('/');
    if (slashpos < 0 || slashpos > dashpos) {
      throw createSyntaxError("No '/' character found in square position");
    }
    final String xstr = extractWord(description, 0, slashpos);
    final String ystr = extractWord(description, slashpos + 1, dashpos);
    int x;
    try {
      x = Integer.parseInt(xstr);
    } catch (final NumberFormatException exception) {
      throw createSyntaxError("Bad x coordinate '" + xstr + "'", exception);
    }
    int y;
    try {
      y = Integer.parseInt(ystr);
    } catch (final NumberFormatException exception) {
      throw createSyntaxError("Bad y coordinate '" + ystr + "'", exception);
    }
    final Square square = createSquare(typename, x, y);
    if (x < mMinX) {
      mMinX = x;
    }
    if (y < mMinY) {
      mMinY = y;
    }
    return square;	
  }

  private Square createSquare(final String fullname, final int x, final int y)
    throws MazeSyntaxException
  {
    String kind;
    String key;
    final int openbrace = fullname.indexOf('(');
    if (openbrace < 0) {
      kind = fullname;
      key = null;
    } else {
      final int closebrace = fullname.indexOf(')', openbrace + 1);
      if (closebrace < 0) {
	throw createSyntaxError("Missing closing brace ')'");
      }
      kind = extractWord(fullname, 0, openbrace);
      key = extractWord(fullname, openbrace + 1, closebrace);
    }
    final SquareCreator creator = getSquareCreator(kind);
    final Point pos = new Point(x, y);
    if (mSquares.containsKey(pos)) {
      throw createSyntaxError("Duplicate square at " + x + "/" + y);
    }
    final Square square = creator.getSquare(pos, key);
    final Point clonedpos = new Point(pos.x, pos.y);
    mSquares.put(clonedpos, square);
    return square;
  }


  private String extractWord(final String description,
			     final int startindex)
  {
    return extractWord(description, startindex, description.length());
  }

  private String extractWord(final String description,
			     int startindex,
			     int endindex)
  {
    while (Character.isWhitespace(description.charAt(startindex))) {
      startindex++;
    }
    while (Character.isWhitespace(description.charAt(endindex - 1))) {
      endindex--;
    }
    return description.substring(startindex, endindex);
  }

  private char getNextCharacter()
    throws IOException
  {
    final int ch = mReader.read();
    if (ch == -1) {
      throw new EOFException("Unexpected end of maze file!");
    }
    if (ch == '\n') {
      mLineNo++;
    }
    mLastChar = (char) ch;
    return mLastChar;
  }

  private MazeSyntaxException createSyntaxError(final String msg)
  {
    return createSyntaxError(msg, null);
  }


  private MazeSyntaxException createSyntaxError(final String msg,
						final Throwable cause)
  {
    final StringBuilder buffer = new StringBuilder(msg);
    buffer.append(" in line ");
    buffer.append(mLineNo);
    if (mURL != null) {
      buffer.append(" of '");
      buffer.append(mURL.toString());
      buffer.append("'");
    }
    buffer.append(".");
    return new MazeSyntaxException(buffer.toString(), cause);
  }


  //#########################################################################
  //# Square Creator Map
  private SquareCreator getSquareCreator(final String kind)
    throws MazeSyntaxException
  {
    final SquareCreator creator = mSquareCreatorMap.get(kind);
    if (creator == null) {
      throw createSyntaxError("Unknown square type '" + kind + "'");
    }
    return creator;
  }


  //#########################################################################
  //# Data Members
  private Reader mReader;
  private int mMinX;
  private int mMinY;
  private char mLastChar;
  private URL mURL;
  private int mLineNo;
  private Map<Point,Square> mSquares;

  private final Map<String,SquareCreator> mSquareCreatorMap;


  //#########################################################################
  //# Local Class SquareCreator
  private abstract class SquareCreator
  {
    abstract Square getSquare(Point pos, String key)
      throws MazeSyntaxException;
  }


  //#########################################################################
  //# Local Class SimpleSquareCreator
  private abstract class SimpleSquareCreator extends SquareCreator
  {
    abstract Square createSquare(Point pos);

    Square getSquare(final Point pos, final String key)
      throws MazeSyntaxException
    {
      if (key == null) {
	return createSquare(pos);
      } else {
	throw createSyntaxError
	  ("Unexpected key specification '(" + key + ")");
      }
    }
  }


  //#########################################################################
  //# Local Class KeySquareCreator
  private abstract class KeySquareCreator extends SquareCreator
  {
    abstract Square createSquare(Point pos, final String key);

    Square getSquare(final Point pos, final String key)
      throws MazeSyntaxException
    {
      if (key != null) {
	return createSquare(pos, key);
      } else {
	throw createSyntaxError("Missing key specification");
      }
    }
  }

}
