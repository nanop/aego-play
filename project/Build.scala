import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "aego-play"
  val appVersion = "1.0-SNAPSHOT"
  val scalaVersion = "2.10.0"

  val appDependencies = Seq(
    "org.reactivemongo" %% "play2-reactivemongo" % "0.9"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    javaOptions in Test += "-Dconfig.file=conf/test.local.conf"
  )

}
