package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface CommandBarPopup Declaration
public interface CommandBarPopup extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C030A,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar getCommandBar() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls getControls() throws com.inzoom.comjni.ComJniException;
  public int getOLEMenuGroup() throws com.inzoom.comjni.ComJniException;
  public void setOLEMenuGroup(int pomg) throws com.inzoom.comjni.ComJniException;
}
