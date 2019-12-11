package com.yilnz.intellij.highlighter;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Highlighter implements Annotator {

    private Set<PsiIdentifier> identifierList = new HashSet<>();

    public void traverse(PsiElement psiElement, AnnotationHolder holder, Color bg, Set<PsiIdentifier> identifierList) {
        final PsiElement[] children = psiElement.getChildren();
        if (children != null) {
            for (PsiElement child : children) {
                if (child instanceof PsiIdentifier) {
                    boolean flag = false;
                    if (child.getParent() instanceof PsiReferenceExpression) {
                        final PsiElement prevSibling = child.getPrevSibling();
                        if (prevSibling != null && prevSibling instanceof PsiReferenceParameterList) {
                            boolean refered = false;
                            for (PsiIdentifier identifier : identifierList) {
                                if (child.textMatches(identifier)) {
                                    refered = true;
                                    break;
                                }
                            }
                            if (refered) {
                                flag = true;
                            }
                        }
                    } else if (child.getParent() instanceof PsiLocalVariable) {
                        flag = true;
                    }
                    if (flag) {
                        TextRange range = new TextRange(child.getTextRange().getStartOffset(),
                                child.getTextRange().getEndOffset());
                        Annotation annotation = holder.createInfoAnnotation(range, null);
                        annotation.setEnforcedTextAttributes(Colors.getRanColor(child.getText(), bg));
                    }
                }
                traverse(child, holder, bg, identifierList);
            }
        }
    }

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        highlight(element, holder);
    }

    private void highlight(@NotNull PsiElement element, @NotNull AnnotationHolder holder){
        final EditorColorsScheme globalScheme = EditorColorsManager.getInstance().getGlobalScheme();
        final Color bg = globalScheme.getDefaultBackground();
        if (element instanceof PsiDeclarationStatement) {
            PsiIdentifier identifier = null;
            A:
            for (PsiElement child : element.getChildren()) {
                if (child instanceof PsiLocalVariable) {
                    final PsiElement[] children = child.getChildren();
                    for (PsiElement psiElement : children) {
                        if (psiElement instanceof PsiIdentifier) {
                            identifier = (PsiIdentifier) psiElement;
                            break A;
                        }
                    }
                }
            }
            if (identifier != null) {
                identifierList.add(identifier);
            }
        }
        if (element instanceof PsiLocalVariable || element instanceof PsiExpressionStatement) {
            traverse(element, holder, bg, identifierList);
        }
    }
}
