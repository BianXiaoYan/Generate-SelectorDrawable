package com.guohe.selector.settings;

import com.guohe.selector.utils.PropertiesUtil;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by wff on 2017/10/26.
 */
public class PluginSetting implements Configurable{
    private JCheckBox customFileNameCheckBox;
    private JCheckBox autoOpenFileCheckBox;
    private JPanel panel;

    @Nls
    @Override
    public String getDisplayName() {
        return "Selector Generator Plus";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        reset();
        return panel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {


        PropertiesUtil.saveAutoOpenFileCheckBox(autoOpenFileCheckBox.isSelected());
        PropertiesUtil.saveCustomFileNameState(customFileNameCheckBox.isSelected());

    }

    @Override
    public void reset() {
        autoOpenFileCheckBox.setSelected(PropertiesUtil.getAutoOpenFileCheckBox());
        customFileNameCheckBox.setSelected(PropertiesUtil.getCustomFileNameState());
    }
}
