import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  idea
  id("io.ratpack.ratpack-groovy") version "1.6.1"
  id("com.github.johnrengelman.plugin-shadow") version "2.0.3"
  id("com.github.ben-manes.versions") version "0.21.0"
}

repositories { jcenter() }

dependencies {
  compile(ratpack.dependency("rx"))

  runtime("org.slf4j:slf4j-simple:1.7.21")

  compile("org.jsoup:jsoup:1.11.3")
  compile("com.github.ben-manes.caffeine:caffeine:2.7.0")

  testCompile(ratpack.dependency("test"))
  testCompile("org.spockframework:spock-core:1.3-groovy-2.4")
  testCompile("net.bytebuddy:byte-buddy:1.9.12")
  testCompile("org.objenesis:objenesis:3.0.1")
}

tasks.withType<Test> {
  testLogging { exceptionFormat = TestExceptionFormat.FULL }
}
