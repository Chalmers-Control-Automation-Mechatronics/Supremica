package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashSet;

import net.sourceforge.waters.external.promela.ast.*;

/**
 * This class is the symbol table used for storing variables (TODO: and channels) in
 * @author Ethan Duff
 */
public class SymbolTable
{
  //This hash map stores all of the variables in it
  private final HashMap<String, PromelaTree> mVariableTable;

  //This has all of the names of the variables in it
  //The keys in the hash map are not used, as the order is not guaranteed
  private final ArrayList<String> mVariableNames;

  private final SymbolTable mParentTable;//This is the parent table (or null if the current table is the root table)
  private final HashMap<String, SymbolTable> mChildTables;//The child tables for the current table. They are stored using the name of the scope they are in

  String mName;//The name for the current symbol table this is used to retrieve the symbol table from its parent

  /**
   * The default constructor for the symbol table class
   */
  public SymbolTable()
  {
    mVariableTable = new HashMap<String,PromelaTree>();
    mVariableNames = new ArrayList<String>();

    mParentTable = null;
    mChildTables = new HashMap<String, SymbolTable>();

    //Now, add in the integer constant values into the variable table
    mVariableTable.put("bit", new IntegerTreeNode(1));
    mVariableTable.put("byte", new IntegerTreeNode(255));
    mVariableTable.put("short", new IntegerTreeNode(65535));
    mVariableTable.put("int", new IntegerTreeNode(0));//TODO Work out how this works
  }

  /**
   * The constructor for a child symbol table
   * @param parent The parent symbol table for this table
   */
  private SymbolTable(final SymbolTable parent)
  {
    mVariableTable = new HashMap<String,PromelaTree>();
    mVariableNames = new ArrayList<String>();

    mParentTable = parent;
    mChildTables = new HashMap<String,SymbolTable>();
  }

  /**
   * A method to get the parent symbol table for the current table
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
  public boolean put(final String name, final PromelaTree classification)
  {
    //Check if the name is taken
    boolean nameFree = true;

    nameFree = isNameFree(name);

    if(nameFree)
    {
      //The name was free
      //Now add the variable into the table
      mVariableTable.put(name, classification);

      //Now add the name into the array list
      mVariableNames.add(name);

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
  public boolean containsKey(final String name)
  {
    if(mVariableTable.containsKey(name))
    {
      return true;
    }
    else if(mParentTable != null)
    {
      return mParentTable.containsKey(name);
    }
    else
    {
      return false;
    }
  }

  /**
   * A method to retrieve the value matching the given key
   * @param name The name of the key that is being retrieved
   * @return The PromelaTree containing the value,, or null if the key does not exist
   */
  public PromelaTree get(final String name)
  {
    if(mVariableTable.containsKey(name))
    {
      //The key is contained, so return the value matching it
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
   * A method to get all of the names of the keys for this level in the symbol table
   * @return An ArrayList of Strings containing the keys
   */
  public ArrayList<String> getLocalKeys()
  {
    return mVariableNames;
  }

  /**
   * A method to get all of the entries in the symbol table
   * @return A Set containing all of the entries in the symbol table
   */
  public Set<Entry<String,PromelaTree>> getEntrySet()
  {
    final HashSet<Entry<String, PromelaTree>> returnSet = new HashSet<Entry<String,PromelaTree>>();
    returnSet.addAll(mVariableTable.entrySet());
    if(mParentTable != null)
    {
      final Set<Entry<String,PromelaTree>> parentEntrySet = mParentTable.getEntrySet();
      returnSet.addAll(parentEntrySet);
    }
    return returnSet;
  }

  /**
   * A method to check if a given name is free for use
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
