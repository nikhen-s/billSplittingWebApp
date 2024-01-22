lazy val billSplitting = (crossProject(JVMPlatform, JSPlatform) in file("."))
  .settings(name := "billSplitting", scalaVersion := "3.3.1")
  .jsSettings(test / aggregate := false, Test / test := {}, Test / testOnly := {})

lazy val client = project in file("./../../lib")
lazy val billSplittingJS = billSplitting.js.dependsOn(client)

lazy val server = project in file("./../../lib")
lazy val billSplittingJVM = billSplitting.jvm.dependsOn(server)
