name         := "bridges"
organization := "com.davegurnell"

ThisBuild / scalaVersion       := "2.13.5"
ThisBuild / crossScalaVersions := Seq("2.12.13", "2.13.5")

val stdOptions = Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xfatal-warnings"
)
def extraOptions(scalaVersion: String) =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 12)) => Seq(
        "-Ypartial-unification"
      )
    case _ => Seq()
  }

scalacOptions ++= stdOptions ++ extraOptions(scalaVersion.value)

libraryDependencies ++= Seq(
  "com.chuusai"       %% "shapeless"          % "2.3.7",
  "com.davegurnell"   %% "unindent"           % "1.6.0",
  "org.apache.commons" % "commons-text"       % "1.9",
  "org.scalatest"     %% "scalatest"          % "3.2.2" % Test,
  "eu.timepit"        %% "refined"            % "0.9.24" % Provided,
  "eu.timepit"        %% "refined-shapeless"  % "0.9.24" % Provided
)

// Versioning

// A lot of the versioning, publishing, and Travis-related code below is adapted from:
//
//   - https://alexn.org/blog/2017/08/16/automatic-releases-sbt-travis.html
//   - http://caryrobbins.com/dev/sbt-publishing/

enablePlugins(GitVersioning)
enablePlugins(GitBranchPrompt)

// Use "1.2.3-4-aabbccdde-SNAPSHOT" versnining:
git.useGitDescribe := true

// Put "-SNAPSHOT" on a commit if it's not a tag:
git.gitUncommittedChanges := git.gitCurrentTags.value.isEmpty

// This is what release tags look like:
val ReleaseTag = """^([\d\.]+)$""".r

git.gitTagToVersionNumber := {
  case ReleaseTag(v) => Some(v)
  case _             => None
}

// Publishing

publishMavenStyle := true

isSnapshot := version.value endsWith "SNAPSHOT"

publishTo := sonatypePublishTo.value

usePgpKeyHex("2D2E2B8B8BBA48B5")

pgpPublicRing := baseDirectory.value / "project" / ".gnupg" / "pubring.gpg"
pgpSecretRing := baseDirectory.value / "project" / ".gnupg" / "secring.gpg"

licenses += ("Apache-2.0", url("http://apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("https://github.com/davegurnell/bridges"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/davegurnell/bridges.git"),
    "scm:git@github.com:davegurnell/bridges.git"))

developers := List(
  Developer(
    id    = "davegurnell",
    name  = "Dave Gurnell",
    email = "dave@underscore.io",
    url   = url("https://twitter.com/davegurnell")))

// Travis

// Sonatype credentials are on Travis in a secret:
credentials ++= {
  val travisCredentials = for {
    user <- sys.env.get("SONATYPE_USER")
    pass <- sys.env.get("SONATYPE_PASS")
  } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)

  travisCredentials.toSeq
}

// Password to the PGP certificate is on Travis in a secret:
pgpPassphrase := sys.env.get("PGP_PASS").map(_.toArray)

addCommandAlias("ci", ";clean ;coverage ;compile ;test ;coverageReport ;package")
addCommandAlias("release", ";+publishSigned ;sonatypeReleaseAll")

// Formatting

scalafmtOnCompile := true
