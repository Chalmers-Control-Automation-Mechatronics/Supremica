@project modular_recovery {
@events {
RepairInit {
  @controllable
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
error {
  @uncontrollable
  @generateCodeFragment
}
}
@use {
  recovery
  coord
  comp1
  comp2
  error
}
@spec recovery {
  @cloning {recovery}
}
@spec coord {
  @cloning {coord}
}
@plant comp1 {
  @cloning {comp1}
}
@plant comp2 {
  @cloning {comp2}
}
@plant error {
  @cloning {error}
}
}

