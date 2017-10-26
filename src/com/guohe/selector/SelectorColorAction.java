package com.guohe.selector;

import com.guohe.selector.model.ColorPart;
import com.guohe.selector.model.DrawableFile;
import com.guohe.selector.model.DrawableStatus;
import com.guohe.selector.utils.ColorSaxHandler;
import com.guohe.selector.utils.PropertiesUtil;
import com.guohe.selector.utils.config.Config;
import com.guohe.selector.utils.Constants;
import com.guohe.selector.utils.config.ResourceConfig;
import com.guohe.selector.utils.file.FileOperation;
import com.guohe.selector.utils.file.SelectorDrawableGenerator;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.awt.RelativePoint;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wff on 2017/10/25.
 */
public class SelectorColorAction extends BaseAction {

    List<DrawableFile> drawableFileList = new ArrayList<>();
    private ColorSaxHandler colorSaxHandler;
    private List<ColorPart> mColorPartList;

    private String contentStr;

    @Override
    public void update(AnActionEvent event) {
        PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            event.getPresentation().setEnabled(false);
            return;
        }
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) {
            event.getPresentation().setEnabled(false);
            return;
        }
        if (!virtualFile.getName().contains("color")) {
            event.getPresentation().setEnabled(false);
            return;
        }
        secondParent = psiFile.getVirtualFile().getParent().getParent();
        contentStr = psiFile.getText();
        if (contentStr == null || contentStr.isEmpty()) {
            event.getPresentation().setEnabled(false);
            return;
        }
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        if (!getColorList(anActionEvent)) {
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
                }

                selectorDrawableName = Messages.showInputDialog(project, message, title, Messages.getQuestionIcon(), newFileName, null);
            }
            while ((selectorDrawableName != null)
                    && "".equals(selectorDrawableName.trim()));
        }else {
            selectorDrawableName = newFileName;
        }

        if (selectorDrawableName == null)
            return;

        selectorDrawableName = FileOperation.addSuffixXml(selectorDrawableName);

        ApplicationManager.getApplication().runWriteAction(new Runnable()
        {
            @Override
            public void run()
            {
                //创建drawable 文件夹
                VirtualFile virtualFile = null;
                try
                {
                    virtualFile = FileOperation.creteDir(secondParent, Constants.COLOR);
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
                    SelectorDrawableGenerator.generate(drawableFileList, selectorVirtualFile, Constants.COLOR);
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


    /**
     * 获取View列表
     *
     * @param event 触发事件
     */
    private boolean getColorList(AnActionEvent event) {

        try {
            colorSaxHandler = new ColorSaxHandler();
            colorSaxHandler.createViewList(contentStr);
            mColorPartList = colorSaxHandler.getColorPartList();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mColorPartList == null || mColorPartList.size() == 0) {
            return false;
        }
        drawableFileList.clear();
        DrawableFile drawableFile = new DrawableFile();

        for (ColorPart colorPart : mColorPartList) {
            int index = colorPart.name.lastIndexOf("_");
            if (index <= 0) {
                return false;
            }
            newFileName = colorPart.name.substring(0, index);

            DrawableStatus drawableStatusByName = DrawableStatus.getDrawableStatusByName(colorPart.name);
            DrawableFile clone = (DrawableFile) drawableFile.clone();
            clone.setSimpleName(colorPart.name);
            clone.setStatus(true);
            clone.setDrawableStatus(drawableStatusByName);

            if (!drawableFileList.contains(clone)){
                drawableFileList.add(clone);
            }
        }
        return true;
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
