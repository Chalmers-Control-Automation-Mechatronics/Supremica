package net.sourceforge.waters.gui;

import net.sourceforge.waters.gui.actions.WatersPopupActionManager;


/**
 * The Aliases Panel which shows under the definitions tab of the module
 * editor ({@link org.supremica.gui.ide.EditorPanel EditorPanel}).
 *
 * @author Carly Hona, Robi Malik
 */
public class ConstantAliasesTree extends AliasesTree
{

  public ConstantAliasesTree(final ModuleWindowInterface root,
                             final WatersPopupActionManager manager)
  {
    super(root, manager, root.getModuleSubject().getConstantAliasListModifiable());
  }


  private static final long serialVersionUID = 1L;

}