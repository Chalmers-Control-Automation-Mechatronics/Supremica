package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface CalloutFormat Declaration
public interface CalloutFormat extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0311,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public void automaticLength() throws com.inzoom.comjni.ComJniException;
  public void customDrop(float Drop) throws com.inzoom.comjni.ComJniException;
  public void customLength(float Length) throws com.inzoom.comjni.ComJniException;
  public void presetDrop(int DropType) throws com.inzoom.comjni.ComJniException;
  public int getAccent() throws com.inzoom.comjni.ComJniException;
  public void setAccent(int Accent) throws com.inzoom.comjni.ComJniException;
  public int getAngle() throws com.inzoom.comjni.ComJniException;
  public void setAngle(int Angle) throws com.inzoom.comjni.ComJniException;
  public int getAutoAttach() throws com.inzoom.comjni.ComJniException;
  public void setAutoAttach(int AutoAttach) throws com.inzoom.comjni.ComJniException;
  public int getAutoLength() throws com.inzoom.comjni.ComJniException;
  public int getBorder() throws com.inzoom.comjni.ComJniException;
  public void setBorder(int Border) throws com.inzoom.comjni.ComJniException;
  public float getDrop() throws com.inzoom.comjni.ComJniException;
  public int getDropType() throws com.inzoom.comjni.ComJniException;
  public float getGap() throws com.inzoom.comjni.ComJniException;
  public void setGap(float Gap) throws com.inzoom.comjni.ComJniException;
  public float getLength() throws com.inzoom.comjni.ComJniException;
  public int getType() throws com.inzoom.comjni.ComJniException;
  public void setType(int Type) throws com.inzoom.comjni.ComJniException;
}
