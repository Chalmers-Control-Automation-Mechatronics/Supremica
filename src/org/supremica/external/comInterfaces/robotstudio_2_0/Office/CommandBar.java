package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface CommandBar Declaration
public interface CommandBar extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoOleAccDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0304,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public boolean getBuiltIn() throws com.inzoom.comjni.ComJniException;
  public String getContext() throws com.inzoom.comjni.ComJniException;
  public void setContext(String pbstrContext) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls getControls() throws com.inzoom.comjni.ComJniException;
  public void delete() throws com.inzoom.comjni.ComJniException;
  public boolean getEnabled() throws com.inzoom.comjni.ComJniException;
  public void setEnabled(boolean pvarfEnabled) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag,com.inzoom.comjni.Variant Visible,com.inzoom.comjni.Variant Recursive) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag,com.inzoom.comjni.Variant Visible) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl() throws com.inzoom.comjni.ComJniException;
  public int getHeight() throws com.inzoom.comjni.ComJniException;
  public void setHeight(int pdy) throws com.inzoom.comjni.ComJniException;
  public int getIndex() throws com.inzoom.comjni.ComJniException;
  public int getInstanceId() throws com.inzoom.comjni.ComJniException;
  public int getLeft() throws com.inzoom.comjni.ComJniException;
  public void setLeft(int pxpLeft) throws com.inzoom.comjni.ComJniException;
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pbstrName) throws com.inzoom.comjni.ComJniException;
  public String getNameLocal() throws com.inzoom.comjni.ComJniException;
  public void setNameLocal(String pbstrNameLocal) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public int getPosition() throws com.inzoom.comjni.ComJniException;
  public void setPosition(int ppos) throws com.inzoom.comjni.ComJniException;
  public int getRowIndex() throws com.inzoom.comjni.ComJniException;
  public void setRowIndex(int piRow) throws com.inzoom.comjni.ComJniException;
  public int getProtection() throws com.inzoom.comjni.ComJniException;
  public void setProtection(int pprot) throws com.inzoom.comjni.ComJniException;
  public void reset() throws com.inzoom.comjni.ComJniException;
  public void showPopup(com.inzoom.comjni.Variant x,com.inzoom.comjni.Variant y) throws com.inzoom.comjni.ComJniException;
  public void showPopup(com.inzoom.comjni.Variant x) throws com.inzoom.comjni.ComJniException;
  public void showPopup() throws com.inzoom.comjni.ComJniException;
  public int getTop() throws com.inzoom.comjni.ComJniException;
  public void setTop(int pypTop) throws com.inzoom.comjni.ComJniException;
  public int getType() throws com.inzoom.comjni.ComJniException;
  public boolean getVisible() throws com.inzoom.comjni.ComJniException;
  public void setVisible(boolean pvarfVisible) throws com.inzoom.comjni.ComJniException;
  public int getWidth() throws com.inzoom.comjni.ComJniException;
  public void setWidth(int pdx) throws com.inzoom.comjni.ComJniException;
  public boolean getAdaptiveMenu() throws com.inzoom.comjni.ComJniException;
  public void setAdaptiveMenu(boolean pvarfAdaptiveMenu) throws com.inzoom.comjni.ComJniException;
}
