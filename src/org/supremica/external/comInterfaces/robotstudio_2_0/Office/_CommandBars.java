package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface _CommandBars Declaration
public interface _CommandBars extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0302,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl getActionControl() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar getActiveMenuBar() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar add(com.inzoom.comjni.Variant Name,com.inzoom.comjni.Variant Position,com.inzoom.comjni.Variant MenuBar,com.inzoom.comjni.Variant Temporary) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar add(com.inzoom.comjni.Variant Name,com.inzoom.comjni.Variant Position,com.inzoom.comjni.Variant MenuBar) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar add(com.inzoom.comjni.Variant Name,com.inzoom.comjni.Variant Position) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar add(com.inzoom.comjni.Variant Name) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar add() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public boolean getDisplayTooltips() throws com.inzoom.comjni.ComJniException;
  public void setDisplayTooltips(boolean pvarfDisplayTooltips) throws com.inzoom.comjni.ComJniException;
  public boolean getDisplayKeysInTooltips() throws com.inzoom.comjni.ComJniException;
  public void setDisplayKeysInTooltips(boolean pvarfDisplayKeys) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag,com.inzoom.comjni.Variant Visible) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar getItem(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public boolean getLargeButtons() throws com.inzoom.comjni.ComJniException;
  public void setLargeButtons(boolean pvarfLargeButtons) throws com.inzoom.comjni.ComJniException;
  public int getMenuAnimationStyle() throws com.inzoom.comjni.ComJniException;
  public void setMenuAnimationStyle(int pma) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public void releaseFocus() throws com.inzoom.comjni.ComJniException;
  public int getIdsString(int ids,String[] pbstrName) throws com.inzoom.comjni.ComJniException;
  public int getTmcGetName(int tmc,String[] pbstrName) throws com.inzoom.comjni.ComJniException;
  public boolean getAdaptiveMenus() throws com.inzoom.comjni.ComJniException;
  public void setAdaptiveMenus(boolean pvarfAdaptiveMenus) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls findControls(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag,com.inzoom.comjni.Variant Visible) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls findControls(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls findControls(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls findControls(com.inzoom.comjni.Variant Type) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls findControls() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar addEx(com.inzoom.comjni.Variant TbidOrName,com.inzoom.comjni.Variant Position,com.inzoom.comjni.Variant MenuBar,com.inzoom.comjni.Variant Temporary,com.inzoom.comjni.Variant TbtrProtection) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar addEx(com.inzoom.comjni.Variant TbidOrName,com.inzoom.comjni.Variant Position,com.inzoom.comjni.Variant MenuBar,com.inzoom.comjni.Variant Temporary) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar addEx(com.inzoom.comjni.Variant TbidOrName,com.inzoom.comjni.Variant Position,com.inzoom.comjni.Variant MenuBar) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar addEx(com.inzoom.comjni.Variant TbidOrName,com.inzoom.comjni.Variant Position) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar addEx(com.inzoom.comjni.Variant TbidOrName) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar addEx() throws com.inzoom.comjni.ComJniException;
  public boolean getDisplayFonts() throws com.inzoom.comjni.ComJniException;
  public void setDisplayFonts(boolean pvarfDisplayFonts) throws com.inzoom.comjni.ComJniException;
}
