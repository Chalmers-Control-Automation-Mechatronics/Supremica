package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface BalloonLabel Declaration
public interface BalloonLabel extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0330,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public String getItem() throws com.inzoom.comjni.ComJniException;
  public String getName() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public void setText(String pbstr) throws com.inzoom.comjni.ComJniException;
  public String getText() throws com.inzoom.comjni.ComJniException;
}
