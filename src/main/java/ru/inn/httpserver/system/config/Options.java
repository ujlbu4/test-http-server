package ru.inn.httpserver.system.config;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.inn.httpserver.server.Constants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class Options {

    private static String partnerEndPoint;

    private Options() {
        if (OptionsLoader.INSTANCE != null) {
            throw new IllegalStateException("Environment already instantiated!");
        }
    }

    private static class OptionsLoader {
        private static final Options INSTANCE = new Options();
    }

    public static Options getInstance() {
        return OptionsLoader.INSTANCE;
    }

    public void loadConfiguration() {
        File configFolder = new File(Constants.CONFIGS_PATH);
//		File[] availableConfigFiles = configFolder.listFiles(new FileFilter() {
//			public boolean accept(File pathname) {
//				return pathname.isFile() && pathname.getName().contains("env");
//			}
//		});
        File[] availableConfigFiles = configFolder.listFiles();

        if (availableConfigFiles == null) {
            throw new RuntimeException("Can't find configs in " + Constants.CONFIGS_PATH);
        }

        for (int i = 0; i < availableConfigFiles.length; i++) {
            try {
                loadConfigurationFile(availableConfigFiles[i]);
            } catch (Exception e) {
                throw new RuntimeException("Exception while parsing file [" + availableConfigFiles[i].getAbsolutePath() + "]! Stopping service", e);
            }
        }


    }

    private void loadConfigurationFile(File file) throws Exception {

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(file);

        doc.getDocumentElement().normalize();
        Element rootElement = doc.getDocumentElement();

        NodeList currentNodeTypeList;
        Element currentElement;

        //partner endpoint location
        currentNodeTypeList = rootElement.getElementsByTagName("partnerEndPoint");
        currentElement = (Element) currentNodeTypeList.item(0);
        partnerEndPoint = currentElement.getChildNodes().item(0).getNodeValue();

    }

    public String getPartnerEndPoint() {
        return partnerEndPoint;
    }
}
