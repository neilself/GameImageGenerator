
rootProject.name = "BoardImageGenerator"

sourceControl {
  gitRepository(uri("https://github.com/gradle/native-samples-cpp-library.git")) {
    producesModule("org.gradle.cpp-samples:utilities")
  }
  gitRepository(uri("https://github.com/Keelar/ExprK.git")) {
    producesModule("com.github.keelar.exprk:ExprK")
  }
}