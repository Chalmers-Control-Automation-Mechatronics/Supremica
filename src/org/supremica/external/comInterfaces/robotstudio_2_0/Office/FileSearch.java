package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface FileSearch Declaration
public interface FileSearch extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0332,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public boolean getSearchSubFolders() throws com.inzoom.comjni.ComJniException;
  public void setSearchSubFolders(boolean SearchSubFoldersRetVal) throws com.inzoom.comjni.ComJniException;
  public boolean getMatchTextExactly() throws com.inzoom.comjni.ComJniException;
  public void setMatchTextExactly(boolean MatchTextRetVal) throws com.inzoom.comjni.ComJniException;
  public boolean getMatchAllWordForms() throws com.inzoom.comjni.ComJniException;
  public void setMatchAllWordForms(boolean MatchAllWordFormsRetVal) throws com.inzoom.comjni.ComJniException;
  public String getFileName() throws com.inzoom.comjni.ComJniException;
  public void setFileName(String FileNameRetVal) throws com.inzoom.comjni.ComJniException;
  public int getFileType() throws com.inzoom.comjni.ComJniException;
  public void setFileType(int FileTypeRetVal) throws com.inzoom.comjni.ComJniException;
  public int getLastModified() throws com.inzoom.comjni.ComJniException;
  public void setLastModified(int LastModifiedRetVal) throws com.inzoom.comjni.ComJniException;
  public String getTextOrProperty() throws com.inzoom.comjni.ComJniException;
  public void setTextOrProperty(String TextOrProperty) throws com.inzoom.comjni.ComJniException;
  public String getLookIn() throws com.inzoom.comjni.ComJniException;
  public void setLookIn(String LookInRetVal) throws com.inzoom.comjni.ComJniException;
  public int execute(int SortBy,int SortOrder,boolean AlwaysAccurate) throws com.inzoom.comjni.ComJniException;
  public int execute(int SortBy,int SortOrder) throws com.inzoom.comjni.ComJniException;
  public int execute(int SortBy) throws com.inzoom.comjni.ComJniException;
  public int execute() throws com.inzoom.comjni.ComJniException;
  public void newSearch() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.FoundFiles getFoundFiles() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTests getPropertyTests() throws com.inzoom.comjni.ComJniException;
}
