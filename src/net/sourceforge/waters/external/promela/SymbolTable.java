package net.sourceforge.waters.external.promela;

import java.util.HashMap;

import net.sourceforge.waters.external.promela.ast.*;

/**
 * This class is the symbol table used for storing variables (TODO: and channels) in
 * @author Ethan Duff
 */
public class SymbolTable
{
  private final HashMap<String, VardefTreeNode> mVariableTable;//This hash map stores all of the variables in it

  private final SymbolTable mParentTable;//This is the parent table (or null if the current table is the root table)
  private final HashMap<String, SymbolTable> mChildTables;//The child tables for the current table. They are stored using the name of the scope they are in

  String mName;//The name for the current symbol table this is used to retrieve the symbol table from its parent

  /**
   * The constructor for the symbol table class
   * @author Ethan Duff
   */
  public SymbolTable()
  {
    mVariableTable = new HashMap<String,VardefTreeNode>();
    mParentTable = null;
    mChildTables = new HashMap<String, SymbolTable>();
  }

  /**
   * The constructor for a child symbol table
   * @param parent The parent symbol table for this table
   */
  private SymbolTable(final SymbolTable parent)
  {
    mVariableTable = new HashMap<String,VardefTreeNode>();
    mParentTable = parent;
    mChildTables = new HashMap<String,SymbolTable>();
  }

  /**
   * A method to get the parent symbol table for the current table
   * @author Ethan Duff
   * @return The parent symbol table to the current table. May be null if there is no parent
   */
  public SymbolTable getParentTable()
  {
    return mParentTable;
  }

 /**
  * A method to add a new table onto the current table
  * @param scopeName The name of the current scope, which is used to access the table at a later time
  * @return The new table if the table was successfully added, and null if the name was already taken
  */
  public SymbolTable addNewSymbolTable(final String scopeName)
  {
    if(mChildTables.containsKey(scopeName))
    {
      return null;//The name for the scope is already taken
    }
    else
    {
      //Create a new symbol table, and attach it to the current table
      final SymbolTable newTable = new SymbolTable(this);
      mChildTables.put(scopeName, newTable);
      newTable.mName = scopeName;
      return newTable;
    }
  }

  /**
   * A method to get a child table of the current table
   * @param name The name of the table to retrieve
   * @return The table that is requested, or null if the table does not exist
   */
  public SymbolTable getChildTable(final String name)
  {
    if(mChildTables.containsKey(name))
    {
      return mChildTables.get(name);
    }
    else
    {
      return null;
    }
  }

  /**
   * A method to add a variable to this Symbol table.
   * The scope of the variable will be the same as the scope of this table.
   * @param name The name of the variable that is being created
   * @param classification The VardefTreeNode that has the variable to be created as its child
   * @return A return value of false indicates that the variable was unable to be created because the name was already taken.
   */
  public boolean put(final String name, final VardefTreeNode classification)
  {
    //Check if the name is taken
    boolean nameFree = true;

    nameFree = isNameFree(name);

    if(nameFree)
    {

      //The name was free
      //Now, add the variable into the table
      mVariableTable.put(name, classification);

      return true;
    }
    else
    {
      //The name is not free, so return false
      return false;
    }
  }

  /**
   * A method to check if a variable is contained in the symbol table
   * @param name The name of the possible variable
   * @return True if the variable does exist, false if it does not exist
   */
  public boolean contains(final String name)
  {
    if(mVariableTable.containsKey(name))
    {
      return true;
    }
    else if(mParentTable != null)
    {
      return mParentTable.contains(name);
    }
    else
    {
      return false;
    }
  }

  /**
   * A method to retrieve the classification of a variable
   * @author Ethan Duff
   * @param name The name of the variable that is being retrieved
   * @return The VardefTreeNode containing the variable classification, or null if the variable does not exist
   */
  public VardefTreeNode get(final String name)
  {
    if(mVariableTable.containsKey(name))
    {
      return mVariableTable.get(name);
    }
    else if(mParentTable != null)
    {
      //This table does not contain the variable, so check the parent table to see if it has it
      return mParentTable.get(name);
    }
    else
    {
      //This table does not contain the variable, so it does not exist, return null
      return null;
    }
  }

  /**
   * A method to check if a given name is free for use
   * @author Ethan Duff
   * @param name The name that is requested to be used
   * @return True if the name is free, false if the name is already taken
   */
  private boolean isNameFree(final String name)
  {
    boolean nameTaken = true;

    //Check if the name is free in the current context
    nameTaken = mVariableTable.containsKey(name);//TODO Add check for channel table in here

    if(nameTaken)
      return false;
    else
    {
      if(mParentTable == null)
        return true;//This is the root table, so the name is free
      else
        return mParentTable.isNameFree(name);//Check the parent table to see if the name is free
    }
  }
}
