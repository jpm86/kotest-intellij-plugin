package io.kotlintest.plugin.intellij.psi

import com.intellij.psi.PsiElement

object FreeSpecStyle : SpecStyle {

  override fun fqn(): String = "io.kotlintest.specs.FreeSpec"

  override fun specStyleName(): String = "FreeSpec"

  override fun isTestElement(element: PsiElement): Boolean = testPath(element) != null

  private fun PsiElement.locateParentTests(): List<String> {
    val test = tryBranch()
    val result = if (test == null) emptyList() else listOf(test)
    // if parent is null then we have hit the end
    return if (parent == null) result else parent.locateParentTests() + result
  }

  private fun PsiElement.tryBranch(): String? = matchInfixFunctionWithStringAndLambaArg(listOf("-"))
  private fun PsiElement.tryLeaf(): String? = matchStringInvoke()
  private fun PsiElement.tryLeafWithConfig(): String? = extractLiteralForStringExtensionFunction(listOf("config"))

  override fun testPath(element: PsiElement): String? {
    if (!element.isInSpecClass()) return null
    val test = element.tryLeaf() ?: element.tryLeafWithConfig() ?: element.tryBranch()
    return if (test == null) null else {
      val tests = element.locateParentTests() + test
      tests.distinct().joinToString(" -- ")
    }
  }
}