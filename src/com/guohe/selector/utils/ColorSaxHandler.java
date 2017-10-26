package com.guohe.selector.utils;

import com.guohe.selector.model.ColorPart;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ColorSaxHandler extends DefaultHandler{

    private String mTagName;
    private ColorPart colorPart;
    private List<ColorPart> mColorPartList;

    public static void main(String[] args){
        String str = "<resources>\n" +
                "    <color name=\"main_n\">#FF4081</color>\n" +
                "    <color name=\"main_p\">#FF4081</color>\n" +
                "</resources>";

        ColorSaxHandler colorSaxHandler = new ColorSaxHandler();
        try {
            colorSaxHandler.createViewList(str);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<ColorPart> colorParts = colorSaxHandler.getColorPartList();
        for (ColorPart colorPart : colorParts) {
            System.out.println(colorPart.toString());
        }
    }

    public void createViewList(String string) throws ParserConfigurationException, SAXException, IOException {
        InputStream xmlStream = new ByteArrayInputStream(string.getBytes("UTF-8"));
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse(xmlStream, this);
    }

    @Override
    public void startDocument() throws SAXException {
        mColorPartList = new ArrayList<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {



        if (qName != null && qName.equals("color")) {
            String name = attributes.getValue("name");
            if (name != null && name.length() != 0) {
                int index = name.lastIndexOf("_");
                if (index > 0) {
                    colorPart = new ColorPart();
                    colorPart.name = name;
                    mTagName = qName;
                }
            }
        }


    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (mTagName != null) {
            String color = new String(ch, start, length);
            if (colorPart != null) {
                colorPart.color = color;
                mColorPartList.add(colorPart);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        mTagName = null;
        colorPart = null;
    }


    public List<ColorPart> getColorPartList() {
        return mColorPartList;
    }
}
