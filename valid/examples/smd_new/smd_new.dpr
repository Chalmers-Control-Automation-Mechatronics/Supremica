@project smd_new {
@events {
repeat {
  @uncontrollable
  @generateCodeFragment
}
stop {
  @uncontrollable
  @generateCodeFragment
}
scan {
  @controllable
  @generateCodeFragment
}
donerecover {
  @uncontrollable
  @generateCodeFragment
}
errorscan {
  @uncontrollable
  @generateCodeFragment
}
reset {
  @controllable
  @generateCodeFragment
}
counterequals12 {
  @uncontrollable
  @generateCodeFragment
}
doneturn {
  @uncontrollable
  @generateCodeFragment
}
counter {
  @controllable
  @generateCodeFragment
}
donevacuum {
  @uncontrollable
  @generateCodeFragment
}
errorvacuum {
  @uncontrollable
  @generateCodeFragment
}
turn {
  @controllable
  @generateCodeFragment
}
vacuum {
  @controllable
  @generateCodeFragment
}
recover {
  @controllable
  @generateCodeFragment
}
errorturn {
  @uncontrollable
  @generateCodeFragment
}
counterlessthan12 {
  @uncontrollable
  @generateCodeFragment
}
operatorinput {
  @controllable
  @generateCodeFragment
}
donescan {
  @uncontrollable
  @generateCodeFragment
}
}
@use {
  scantask
  scan
  recoverytask
  vacuumtask
  counter
  turn
  vacuum
  operator
}
@spec scantask {
  @cloning {scantask}
}
@plant scan {
  @cloning {scan}
}
@spec recoverytask {
  @cloning {recoverytask}
}
@spec vacuumtask {
  @cloning {vacuumtask}
}
@plant counter {
  @cloning {counter}
}
@plant turn {
  @cloning {turn}
}
@plant vacuum {
  @cloning {vacuum}
}
@plant operator {
  @cloning {operator}
}
}

