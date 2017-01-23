// This is a generated file. Not intended for manual editing.
package org.ballerinalang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.ballerinalang.psi.BallerinaTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.ballerinalang.psi.*;

public class BallerinaExpressionImpl extends ASTWrapperPsiElement implements BallerinaExpression {

  public BallerinaExpressionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitExpression(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<BallerinaActionInvocation> getActionInvocationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BallerinaActionInvocation.class);
  }

  @Override
  @NotNull
  public List<BallerinaArgumentList> getArgumentListList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BallerinaArgumentList.class);
  }

  @Override
  @NotNull
  public List<BallerinaBacktickString> getBacktickStringList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BallerinaBacktickString.class);
  }

  @Override
  @NotNull
  public List<BallerinaExpressionList> getExpressionListList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BallerinaExpressionList.class);
  }

  @Override
  @NotNull
  public List<BallerinaFunctionName> getFunctionNameList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BallerinaFunctionName.class);
  }

  @Override
  @NotNull
  public List<BallerinaLiteralValue> getLiteralValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BallerinaLiteralValue.class);
  }

  @Override
  @NotNull
  public List<BallerinaMapInitKeyValueList> getMapInitKeyValueListList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BallerinaMapInitKeyValueList.class);
  }

  @Override
  @NotNull
  public List<BallerinaPackageName> getPackageNameList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BallerinaPackageName.class);
  }

  @Override
  @Nullable
  public BallerinaTypeName getTypeName() {
    return findChildByClass(BallerinaTypeName.class);
  }

  @Override
  @NotNull
  public List<BallerinaVariableReference> getVariableReferenceList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BallerinaVariableReference.class);
  }

}
