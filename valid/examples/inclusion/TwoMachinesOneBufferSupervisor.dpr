@project TwoMachinesOneBufferSupervisor {
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
  bufferSpec
  machine1
  machine2
  supervisor
}
@spec bufferSpec {
  @cloning {bufferSpec}
}
@plant machine1 {
  @cloning {machine1}
}
@plant machine2 {
  @cloning {machine2}
}
@spec supervisor {
  @cloning {supervisor}
}
}

