package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface AnswerWizardFiles Declaration
public interface AnswerWizardFiles extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0361,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public String getItem(int Index) throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public void add(String FileName) throws com.inzoom.comjni.ComJniException;
  public void delete(String FileName) throws com.inzoom.comjni.ComJniException;
}
