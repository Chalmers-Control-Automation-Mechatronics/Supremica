package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface AnswerWizard Implementation
public class AnswerWizardJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.AnswerWizard {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.AnswerWizard getAnswerWizardFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new AnswerWizardJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.AnswerWizard getAnswerWizardFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new AnswerWizardJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.AnswerWizard getAnswerWizardFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new AnswerWizardJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.AnswerWizard convertComPtrToAnswerWizard(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new AnswerWizardJCW(comPtr,true,releaseComPtr); }
  protected AnswerWizardJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected AnswerWizardJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.AnswerWizard.IID); }
  protected AnswerWizardJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected AnswerWizardJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.AnswerWizard.IID); }
  protected AnswerWizardJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected AnswerWizardJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.AnswerWizard.IID,releaseComPtr);}
  protected AnswerWizardJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.AnswerWizard.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.AnswerWizardFiles getFiles() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.AnswerWizard.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.AnswerWizardFiles rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.AnswerWizardFilesJCW.getAnswerWizardFilesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void clearFileList() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.AnswerWizard.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void resetFileList() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.AnswerWizard.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
