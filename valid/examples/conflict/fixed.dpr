@project fixed {
@events {
User1Done {
  @uncontrollable
  @generateCodeFragment
}
User1TakesA {
  @controllable
  @generateCodeFragment
}
User1TakesB {
  @controllable
  @generateCodeFragment
}
User2TakesA {
  @controllable
  @generateCodeFragment
}
User2TakesB {
  @controllable
  @generateCodeFragment
}
User2Done {
  @uncontrollable
  @generateCodeFragment
}
}
@use {
  user1
  user2
  resourceA
  resourceB
  coordinator
}
@plant user1 {
  @cloning {user1}
}
@plant user2 {
  @cloning {user2}
}
@plant resourceA {
  @cloning {resourceA}
}
@plant resourceB {
  @cloning {resourceB}
}
@spec coordinator {
  @cloning {coordinator}
}
}

