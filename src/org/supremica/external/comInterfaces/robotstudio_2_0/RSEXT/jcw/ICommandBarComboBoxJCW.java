package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw;

// interface ICommandBarComboBox Implementation
public class ICommandBarComboBoxJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox getICommandBarComboBoxFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarComboBoxJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox getICommandBarComboBoxFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarComboBoxJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox getICommandBarComboBoxFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ICommandBarComboBoxJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox convertComPtrToICommandBarComboBox(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarComboBoxJCW(comPtr,true,releaseComPtr); }
  protected ICommandBarComboBoxJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ICommandBarComboBoxJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox.IID); }
  protected ICommandBarComboBoxJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected ICommandBarComboBoxJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox.IID); }
  protected ICommandBarComboBoxJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected ICommandBarComboBoxJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox.IID,releaseComPtr);}
  protected ICommandBarComboBoxJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void addItem(String Text,int Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Text,false),
      new com.inzoom.comjni.Variant(Index,false)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void addItem(String Text) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Text,false),
      new com.inzoom.comjni.Variant((int)-1,com.inzoom.comjni.enum.VarType.I4,false)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void removeItem(int Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,false)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void clear() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getListCount() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getListIndex() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setListIndex(int pIndex) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pIndex,false)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getList(int Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,false),
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox.IID);
    String rv = _v[1].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
