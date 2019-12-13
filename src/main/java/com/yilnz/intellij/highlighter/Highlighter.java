package com.yilnz.intellij.highlighter;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Element;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class Highlighter implements Annotator {

    final static Key<Set<PsiIdentifier>> k = new Key<>("localvariables");

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

    private void walk(PsiElement element, Consumer<PsiElement> child){
        final PsiElement[] children = element.getChildren();
        for (PsiElement psiElement : children) {
            child.accept(element);
            walk(psiElement, child);
        }
    }

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        highlight(element, holder);
    }

    private void highlight(@NotNull PsiElement element, @NotNull AnnotationHolder holder){
        final EditorColorsScheme globalScheme = EditorColorsManager.getInstance().getGlobalScheme();
        final Color bg = globalScheme.getDefaultBackground();
        final PsiManager manager = element.getManager();
        Set<PsiIdentifier> identifierList = new HashSet<>();
        final Set<PsiIdentifier> userData = manager.getUserData(k);
        if (userData != null) {
            identifierList = userData;
        }else{
            manager.putUserData(k, identifierList);
        }
        //Get LocalVariableList;
       /* PsiElement parent = null;
        do {
            parent = element.getParent();
            if (parent instanceof PsiMethod ) {
                psiMethods.add((PsiMethod) parent);
                walk(parent, c -> {
                    if (c instanceof PsiDeclarationStatement) {
                        PsiIdentifier identifier = null;
                        A:
                        for (PsiElement child : c.getChildren()) {
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
                        if (identifier != null && !identifier.textMatches(identifier.getText())) {
                            identifierList.add(identifier);
                        }
                    }
                });
            }
        } while (parent != null && parent instanceof PsiJavaFile);*/

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


        /*//Add Color to LocalVariable and Its references
        if (element instanceof PsiIdentifier) {
            boolean flag = false;
            if (element.getParent() instanceof PsiReferenceExpression) {
                final PsiElement prevSibling = element.getPrevSibling();
                if (prevSibling != null && prevSibling instanceof PsiReferenceParameterList) {
                    boolean refered = false;
                    for (PsiIdentifier identifier : identifierList) {
                        if (element.textMatches(identifier)) {
                            refered = true;
                            break;
                        }
                    }
                    if (refered) {
                        flag = true;
                    }
                }
            } else if (element.getParent() instanceof PsiLocalVariable) {
                flag = true;
            }
            if (flag) {
                TextRange range = new TextRange(element.getTextRange().getStartOffset(),
                        element.getTextRange().getEndOffset());
                Annotation annotation = holder.createInfoAnnotation(range, null);
                annotation.setEnforcedTextAttributes(Colors.getRanColor(element.getText(), bg));
            }
        }*/
        
        if (element instanceof PsiLocalVariable || element instanceof PsiExpressionStatement) {
            traverse(element, holder, bg, identifierList);
        }
    }
}
