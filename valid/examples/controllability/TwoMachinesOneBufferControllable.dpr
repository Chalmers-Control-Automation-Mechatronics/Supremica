@project TwoMachinesOneBufferControllable {
@events {
Break1 {
  @uncontrollable
  @generateCodeFragment
}
Break2 {
  @uncontrollable
  @generateCodeFragment
}
Repair1 {
  @controllable
  @generateCodeFragment
}
Repair2 {
  @controllable
  @generateCodeFragment
}
Start1 {
  @controllable
  @generateCodeFragment
}
Start2 {
  @controllable
  @generateCodeFragment
}
Finish1 {
  @uncontrollable
  @generateCodeFragment
}
Finish2 {
  @uncontrollable
  @generateCodeFragment
}
}
@use {
  machine1
  machine2
  modifiedBufferSpec
}
@plant machine1 {
  @cloning {machine1}
}
@plant machine2 {
  @cloning {machine2}
}
@spec modifiedBufferSpec {
  @cloning {modifiedBufferSpec}
}
}

