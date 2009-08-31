/*
 * Copyright (c) 2004 JetBrains s.r.o. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * -Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduct the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of JetBrains or IntelliJ IDEA
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. JETBRAINS AND ITS LICENSORS SHALL NOT
 * BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT
 * OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL JETBRAINS OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN
 * IF JETBRAINS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 */
package com.intellij.openapi.module;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.util.ArrayList;

public class JavaModuleType extends ModuleType<JavaModuleBuilder> {

  public JavaModuleType() {
    this("JAVA_MODULE");
  }

  protected JavaModuleType(@NonNls String id) {
    super(id);
  }

  public JavaModuleBuilder createModuleBuilder() {
    return new JavaModuleBuilder();
  }

  public String getName() {
    return ProjectBundle.message("module.type.java.name");
  }

  public String getDescription() {
    return ProjectBundle.message("module.type.java.description");
  }

  public Icon getBigIcon() {
    return getJavaModuleIcon();
  }

  public Icon getNodeIcon(boolean isOpened) {
    return isOpened ? getJavaModuleNodeIconOpen() : getJavaModuleNodeIconClosed();
  }

  public ModuleWizardStep[] createWizardSteps(final WizardContext wizardContext, final JavaModuleBuilder moduleBuilder,
                                              final ModulesProvider modulesProvider) {
    final ProjectWizardStepFactory wizardFactory = ProjectWizardStepFactory.getInstance();
    ArrayList<ModuleWizardStep> steps = new ArrayList<ModuleWizardStep>();
    steps.add(wizardFactory.createSourcePathsStep(wizardContext, moduleBuilder, getWizardIcon(), "reference.dialogs.new.project.fromScratch.source"));
    steps.add(wizardFactory.createProjectJdkStep(wizardContext));
    final ModuleWizardStep supportForFrameworksStep = wizardFactory.createSupportForFrameworksStep(wizardContext, moduleBuilder);
    if (supportForFrameworksStep != null) {
      steps.add(supportForFrameworksStep);
    }
    final ModuleWizardStep[] wizardSteps = steps.toArray(new ModuleWizardStep[steps.size()]);
    return ArrayUtil.mergeArrays(wizardSteps, super.createWizardSteps(wizardContext, moduleBuilder, modulesProvider), ModuleWizardStep.class);
  }

  private static class IconHolder {
    private static final Icon JAVA_MODULE_ICON = IconLoader.getIcon("/modules/javaModule.png");
  }

  private static Icon getJavaModuleIcon() {

    return IconHolder.JAVA_MODULE_ICON;
  }

  private static class JavaModuleOpenIconHolder {
    private static final Icon JAVA_MODULE_NODE_ICON_OPEN = IconLoader.getIcon("/nodes/ModuleOpen.png");
  }

  private static Icon getJavaModuleNodeIconOpen() {
    return JavaModuleOpenIconHolder.JAVA_MODULE_NODE_ICON_OPEN;
  }

  private static class JavaModuleClosedIconHolder {
    private static final Icon JAVA_MODULE_NODE_ICON_CLOSED = IconLoader.getIcon("/nodes/ModuleClosed.png");
  }

  private static Icon getJavaModuleNodeIconClosed() {

    return JavaModuleClosedIconHolder.JAVA_MODULE_NODE_ICON_CLOSED;
  }

  private static class WizardIconHolder {
    private static final Icon WIZARD_ICON = IconLoader.getIcon("/addmodulewizard.png");
  }

  private static Icon getWizardIcon() {

    return WizardIconHolder.WIZARD_ICON;
  }

  @Override
  public boolean isValidSdk(final Module module, final Sdk projectSdk) {
    return isValidJavaSdk(module);
  }

  public static boolean isValidJavaSdk(final Module module) {
    return JavaPsiFacade.getInstance(module.getProject()).findClass(CommonClassNames.JAVA_LANG_OBJECT, 
                                                                    module.getModuleWithLibrariesScope()) != null;
  }
}