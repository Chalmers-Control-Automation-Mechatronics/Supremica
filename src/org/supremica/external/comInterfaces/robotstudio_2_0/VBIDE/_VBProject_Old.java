package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface _VBProject_Old Declaration
public interface _VBProject_Old extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._ProjectTemplate {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x0002E160,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public String getHelpFile() throws com.inzoom.comjni.ComJniException;
  public void setHelpFile(String lpbstrHelpFile) throws com.inzoom.comjni.ComJniException;
  public int getHelpContextID() throws com.inzoom.comjni.ComJniException;
  public void setHelpContextID(int lpdwContextID) throws com.inzoom.comjni.ComJniException;
  public String getDescription() throws com.inzoom.comjni.ComJniException;
  public void setDescription(String lpbstrDescription) throws com.inzoom.comjni.ComJniException;
  public int getMode() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.References getReferences() throws com.inzoom.comjni.ComJniException;
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String lpbstrName) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProjects getCollection() throws com.inzoom.comjni.ComJniException;
  public int getProtection() throws com.inzoom.comjni.ComJniException;
  public boolean getSaved() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponents getVBComponents() throws com.inzoom.comjni.ComJniException;
}
