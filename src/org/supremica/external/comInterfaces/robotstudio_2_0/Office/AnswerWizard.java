package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface AnswerWizard Declaration
public interface AnswerWizard extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0360,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.AnswerWizardFiles getFiles() throws com.inzoom.comjni.ComJniException;
  public void clearFileList() throws com.inzoom.comjni.ComJniException;
  public void resetFileList() throws com.inzoom.comjni.ComJniException;
}
