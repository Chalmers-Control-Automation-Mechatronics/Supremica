package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface LanguageSettings Declaration
public interface LanguageSettings extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0353,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public int getLanguageID(int Id) throws com.inzoom.comjni.ComJniException;
  public boolean getLanguagePreferredForEditing(int lid) throws com.inzoom.comjni.ComJniException;
}
