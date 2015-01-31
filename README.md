sbt-webresource
===============
Downloads files during the build. They can then be used as resources during the build or can be directly included inside the package.

Usage
-----
In **project/plugins.sbt**:
```scala
addSbtPlugin("com.github.mkroli" % "sbt-webresource" % "0.2")
```
To include files directly in the package configure **build.sbt** as follows:
```scala
webResourceSettings

webResources ++= Map(
  "css/bootstrap.min.css" -> "http://netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css",
  "css/bootstrap-theme.min.css" -> "http://netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap-theme.min.css")

resourceGenerators in Compile <+= resolveWebResources

managedResourceDirectories in Compile <+= webResourcesBase 
```

To use the files during the build as sources for the [sbt-less] plugin configure **build.sbt** as follows:
```scala
webResourceSettings

webResources ++= Map(
  "css/bootstrap.min.css" -> "http://netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css",
  "css/bootstrap-theme.min.css" -> "http://netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap-theme.min.css"
)

lessSettings

packageBin in Compile <<= packageBin in Compile dependsOn (LessKeys.less in Compile)

resourceGenerators in Compile <+= LessKeys.less in Compile

LessKeys.less in Compile <<= LessKeys.less in Compile dependsOn (resolveWebResources in Compile)

sourceDirectories in (Compile, LessKeys.less) <<= (sourceDirectory in Compile, webResourcesBase in Compile) { (d1, d2) =>
  Seq(d1 / "less", d2)
}
```

[sbt-less]: https://github.com/untyped/sbt-plugins/tree/develop/sbt-less
