// This is a generated file. Not intended for manual editing.
package org.ballerinalang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface BallerinaConnectorBody extends PsiElement {

  @NotNull
  List<BallerinaActionDefinition> getActionDefinitionList();

  @NotNull
  List<BallerinaConnectorDeclaration> getConnectorDeclarationList();

  @NotNull
  List<BallerinaVariableDeclaration> getVariableDeclarationList();

}
