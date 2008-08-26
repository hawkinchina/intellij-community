/*
 * Copyright 2003-2008 Dave Griffith, Bas Leijdekkers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siyeh.ig.psiutils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.Query;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class ClassUtils {

    /** @noinspection StaticCollection*/
    private static final Set<String> immutableTypes =
            new HashSet<String>(14);

    /** @noinspection StaticCollection*/
    private static final Set<PsiType> primitiveNumericTypes =
            new HashSet<PsiType>(7);

    /** @noinspection StaticCollection*/
    private static final Set<PsiType> integralTypes = new HashSet<PsiType>(5);

    static {
        integralTypes.add(PsiType.LONG);
        integralTypes.add(PsiType.INT);
        integralTypes.add(PsiType.SHORT);
        integralTypes.add(PsiType.CHAR);
        integralTypes.add(PsiType.BYTE);

        primitiveNumericTypes.add(PsiType.BYTE);
        primitiveNumericTypes.add(PsiType.CHAR);
        primitiveNumericTypes.add(PsiType.SHORT);
        primitiveNumericTypes.add(PsiType.INT);
        primitiveNumericTypes.add(PsiType.LONG);
        primitiveNumericTypes.add(PsiType.FLOAT);
        primitiveNumericTypes.add(PsiType.DOUBLE);

        immutableTypes.add("java.lang.Boolean");
        immutableTypes.add("java.lang.Character");
        immutableTypes.add("java.lang.Short");
        immutableTypes.add("java.lang.Integer");
        immutableTypes.add("java.lang.Long");
        immutableTypes.add("java.lang.Float");
        immutableTypes.add("java.lang.Double");
        immutableTypes.add("java.lang.Byte");
        immutableTypes.add("java.lang.String");
        immutableTypes.add("java.awt.Font");
        immutableTypes.add("java.awt.Color");
        immutableTypes.add("java.math.BigDecimal");
        immutableTypes.add("java.math.BigInteger");
        immutableTypes.add("java.math.MathContext");
    }

    private ClassUtils() {
    }

    public static boolean isSubclass(@Nullable PsiClass aClass,
                                     @NonNls String ancestorName) {
        if (aClass == null) {
            return false;
        }
        final PsiManager psiManager = aClass.getManager();
        final Project project = psiManager.getProject();
        final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
      final PsiClass ancestorClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(ancestorName, scope);
        return InheritanceUtil.isCorrectDescendant(aClass, ancestorClass, true);
    }

    public static boolean isPrimitive(PsiType type) {
        return TypeConversionUtil.isPrimitiveAndNotNull(type);
    }

    public static boolean isIntegral(PsiType type) {
        return integralTypes.contains(type);
    }

    public static boolean isImmutable(PsiType type) {
        if(TypeConversionUtil.isPrimitiveAndNotNull(type)) {
            return true;
        }
        if(!(type instanceof PsiClassType)) {
            return false;
        }
        final PsiClassType classType = (PsiClassType) type;
        final String className = classType.getCanonicalText();
        return immutableTypes.contains(className);
    }

    public static boolean inSamePackage(@Nullable PsiElement element1,
                                        @Nullable PsiElement element2) {
        if (element1 == null || element2==null) {
            return false;
        }
        final PsiFile containingFile1 = element1.getContainingFile();
        if (!(containingFile1 instanceof PsiClassOwner)) {
            return false;
        }
        final PsiClassOwner containingJavaFile1 =
                (PsiClassOwner)containingFile1;
        final String packageName1 = containingJavaFile1.getPackageName();
        final PsiFile containingFile2 = element2.getContainingFile();
        if (!(containingFile2 instanceof PsiClassOwner)) {
            return false;
        }
        final PsiClassOwner containingJavaFile2 =
                (PsiClassOwner)containingFile2;
        final String packageName2 = containingJavaFile2.getPackageName();
        return packageName1.equals(packageName2);
    }

    public static boolean isFieldVisible(PsiField field, PsiClass fromClass) {
        final PsiClass fieldClass = field.getContainingClass();
        if (fieldClass == null) {
            return false;
        }
        if (fieldClass.equals(fromClass)) {
            return true;
        }
        if (field.hasModifierProperty(PsiModifier.PRIVATE)) {
            return false;
        }
        if (field.hasModifierProperty(PsiModifier.PUBLIC) ||
                field.hasModifierProperty(PsiModifier.PROTECTED)) {
            return true;
        }
        return inSamePackage(fieldClass, fromClass);
    }

    public static boolean isPrimitiveNumericType(PsiType type) {
        return primitiveNumericTypes.contains(type);
    }

    public static boolean isInnerClass(PsiClass aClass) {
        final PsiClass parentClass = getContainingClass(aClass);
        return parentClass != null;
    }

    @Nullable
    public static PsiClass getContainingClass(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, PsiClass.class);
    }

    public static PsiClass getOutermostContainingClass(PsiClass aClass) {
        PsiClass outerClass = aClass;
        while (true) {
            final PsiClass containingClass = getContainingClass(outerClass);
            if (containingClass != null) {
                outerClass = containingClass;
            } else {
                return outerClass;
            }
        }
    }

    public static boolean isClassVisibleFromClass(PsiClass baseClass,
                                                  PsiClass referencedClass) {
        if (referencedClass.hasModifierProperty(PsiModifier.PUBLIC)) {
            return true;
        } else if (referencedClass.hasModifierProperty(PsiModifier.PROTECTED)) {
            return inSamePackage(baseClass, referencedClass);
        } else if (referencedClass.hasModifierProperty(PsiModifier.PRIVATE)) {
            return PsiTreeUtil.findCommonParent(baseClass, referencedClass) !=
                   null;
        } else {
            return inSamePackage(baseClass, referencedClass);
        }
    }

    public static boolean isOverridden(PsiClass aClass) {
        final Query<PsiClass> query = ClassInheritorsSearch.search(aClass);
        final PsiClass result = query.findFirst();
        return result != null;
    }
}