package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface Adjustments Declaration
public interface Adjustments extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0310,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public float getItem(int Index) throws com.inzoom.comjni.ComJniException;
  public void setItem(int Index,float Val) throws com.inzoom.comjni.ComJniException;
}
