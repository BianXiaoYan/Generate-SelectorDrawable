package com.guohe.selector;

import com.guohe.selector.utils.*;
import com.guohe.selector.utils.config.Config;
import com.guohe.selector.utils.config.ResourceConfig;
import com.guohe.selector.utils.file.FileOperation;
import com.guohe.selector.utils.file.SelectorDrawableGenerator;
import com.guohe.selector.model.DrawableFile;
import com.guohe.selector.model.DrawableStatus;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;



public class SelectDrawableAction extends BaseAction
{
    List<DrawableFile> drawableFileList = new ArrayList<>();

    boolean isDirectory = false;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent)
    {
        if (anActionEvent == null)
            return;
        if (isDirectory)
        {
            String string = Config.getString(ResourceConfig.PLEASE_DONT_SELECT_FOLDER);
            showInfoDialog(string, anActionEvent);
            return;
        }

        final AnActionEvent finalAnActionEvent = anActionEvent;
        final Project project = anActionEvent.getProject();

        String title = Config.getString(ResourceConfig.SET_TITLE);
        String message = Config.getString(ResourceConfig.PLEASE_ENTER_SELECTORDRAWABLE_NAME);
        if (PropertiesUtil.getCustomFileNameState()) {
            do
            {
                if (selectorDrawableName == null)
                {
                } else if ("".equals(selectorDrawableName.trim()))
                {
                    String string = Config.getString(ResourceConfig.PLEASE_ENTER_SELECTORDRAWABLE_NAME);
                    showErrorDialog(string, anActionEvent);
                } else if (FileOperation.isFindChild(secondParent, FileOperation.addSuffixXml(selectorDrawableName)))
                {
                    String string = Config.getString(ResourceConfig.FILE_ALREADY_EXISTS);
                    showErrorDialog(string, anActionEvent);
                } else if (!FileOperation.isValidFileName(FileOperation.addSuffixXml(selectorDrawableName)))
                {
                    String string = Config.getString(ResourceConfig.FILE_NAME_INVALID);
                    showErrorDialog(string, anActionEvent);
                }

                selectorDrawableName = Messages.showInputDialog(project, message, title, Messages.getQuestionIcon(), newFileName, null);
            }
            while ((selectorDrawableName != null)
                    && ("".equals(selectorDrawableName.trim())
                    || (FileOperation.isFindChild(secondParent, FileOperation.addSuffixXml(selectorDrawableName)))
                    || (!FileOperation.isValidFileName(FileOperation.addSuffixXml(selectorDrawableName)))));
        }else {
            selectorDrawableName = newFileName;
        }

        if (selectorDrawableName == null)
            return;

        selectorDrawableName = FileOperation.addSuffixXml(selectorDrawableName);

        Collections.sort(drawableFileList);

        ApplicationManager.getApplication().runWriteAction(new Runnable()
        {
            @Override
            public void run()
            {
                //创建drawable 文件夹
                VirtualFile virtualFile = null;
                try
                {
                    virtualFile = FileOperation.creteDir(secondParent, Constants.DRAWABLE);
                } catch (IOException e)
                {
                    String string = Config.getString(ResourceConfig.CREATE_DRAWABLE_DIR_FAILED);
                    showErrorDialog(string, finalAnActionEvent);
                    e.printStackTrace();
                    return;
                }
                //创建 selector 文件
                VirtualFile selectorVirtualFile = null;
                try
                {
                    selectorVirtualFile = FileOperation.creteFile(virtualFile, selectorDrawableName);
                } catch (IOException e)
                {
                    String string = Config.getString(ResourceConfig.CREATE_SELECTORDRAWABLE_FILE_FAILED);
                    showErrorDialog(string, finalAnActionEvent);
                    e.printStackTrace();
                    return;
                }
                //生成selector文件内容
                try
                {
                    SelectorDrawableGenerator.generate(drawableFileList, selectorVirtualFile);
                } catch (IOException e)
                {
                    String string = Config.getString(ResourceConfig.GENERATE_SELECTORDRAWABLE_FILE_CONTENT_FAIL);
                    showErrorDialog(string, finalAnActionEvent);
                    e.printStackTrace();
                    return;
                }

                //打开文件
                if (PropertiesUtil.getAutoOpenFileCheckBox()) {
                    FileOperation.openFile(project, selectorVirtualFile);
                }
            }
        });
    }

    @Override
    public void update(AnActionEvent e)
    {
        drawableFileList.clear();
        selectorDrawableName = null;
        isDirectory = false;

        VirtualFile[] virtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        DrawableFile drawableFile = new DrawableFile();
        for (int i = 0; i < virtualFiles.length; i++)
        {
            VirtualFile virtualFile = virtualFiles[i];

            //如果是文件夹，则不可用
            if (virtualFile.isDirectory())
            {
                isDirectory = true;
                break;
            }

            VirtualFile firstParent = virtualFile.getParent();
            if ((!firstParent.exists()) || (!firstParent.isDirectory()))
            {
                e.getPresentation().setEnabled(false);
                break;
            }

            String name = firstParent.getName();
            Matcher matcher = Constants.VALID_FOLDER_PATTERN.matcher(name);
            if (!matcher.matches())
            {
                e.getPresentation().setEnabled(false);
                return;
            }

            secondParent = firstParent.getParent();
            if (secondParent == null || (!secondParent.isDirectory()) || (!secondParent.getName().equals(Constants.RES)))
            {
                e.getPresentation().setEnabled(false);
                break;
            }

            String simpleName = virtualFile.getNameWithoutExtension();
            String replacePoint9Name = simpleName.replace(Constants.POINT9SUFFX, "");
            int index = replacePoint9Name.lastIndexOf("_");
            if (index <= 0) {
                e.getPresentation().setEnabled(false);
                return;
            }
            newFileName = replacePoint9Name.substring(0, index);
            DrawableStatus drawableStatusByName = DrawableStatus.getDrawableStatusByName(replacePoint9Name);

            DrawableFile clone = (DrawableFile) drawableFile.clone();
            clone.setSimpleName(replacePoint9Name);
            clone.setStatus(true);
            clone.setFullPathName(virtualFile.getPresentableUrl());
            clone.setDrawableStatus(drawableStatusByName);

            if (!drawableFileList.contains(clone))
                drawableFileList.add(clone);
        }
    }

    private void showInfoDialog(String text, AnActionEvent e)
    {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar((Project) DataKeys.PROJECT.getData(e.getDataContext()));

        if (statusBar != null)
            JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(text, MessageType.INFO, null).setFadeoutTime(10000L).createBalloon().show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
    }

    private void showErrorDialog(String text, AnActionEvent e)
    {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar((Project) DataKeys.PROJECT.getData(e.getDataContext()));

        if (statusBar != null)
            JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(text, MessageType.ERROR, null).setFadeoutTime(10000L).createBalloon().show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
    }
}
