package com.guohe.selector;

import com.guohe.selector.utils.config.Config;
import com.guohe.selector.utils.config.ResourceConfig;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by wff on 2017/10/26.
 */
public abstract class BaseAction extends AnAction {

    protected String selectorDrawableName;
    protected String newFileName;
    protected VirtualFile secondParent = null;


    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

    }
}
