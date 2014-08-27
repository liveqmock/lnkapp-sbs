package org.fbi.sbs.internal;

import org.fbi.sbs.processor.AbstractTxnProcessor;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Processor扫描器
 */
public class ProcessorScanner {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Hashtable<String, AbstractTxnProcessor> scan(String packageName) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        Hashtable<String, AbstractTxnProcessor> classMap = new Hashtable<>();
        String packageDirName = packageName.replace('.', '/');
        Enumeration<URL> resources = this.getClass().getClassLoader().getResources(packageDirName);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            String pro = url.getProtocol();
            String urlPath = URLDecoder.decode(url.getFile(), "UTF-8");
            if ("file".equals(pro)) {
                scanDir(packageName, urlPath, classMap);
            } else if ("bundle".equals(pro)) {
                    BundleContext bundleContext = AppActivator.getBundleContext();

                    Enumeration<URL> classFileEntries = bundleContext.getBundle().findEntries(urlPath, "*.class", true);
                    if (classFileEntries == null || !classFileEntries.hasMoreElements()) {
                        throw new RuntimeException(String.format("Bundle[%s] not exist Java class @", bundleContext.getBundle().getSymbolicName()));
                    }
                    while (classFileEntries.hasMoreElements()) {
                        String bundleOneClassName = classFileEntries.nextElement().getPath();
                        bundleOneClassName = bundleOneClassName.replace("/", ".").substring(0, bundleOneClassName.lastIndexOf("."));
                        while (bundleOneClassName.startsWith(".")) {
                            bundleOneClassName = bundleOneClassName.substring(1);
                        }
                        Class<?> bundleOneClass = null;
                        bundleOneClass = bundleContext.getBundle().loadClass(bundleOneClassName);
                        if (bundleOneClass.isAnnotationPresent(Processor.class)) {
                            Processor processor = (Processor) bundleOneClass.getAnnotation(Processor.class);
                            String txnCode = processor.txnCode();

                            if (classMap.get(txnCode) != null) {
                                logger.error("交易号" + txnCode + "重复,Class:" + bundleOneClassName);
                                throw new RuntimeException("交易号" + txnCode + "重复,Class:" + bundleOneClassName);
                            } else
                                classMap.put(txnCode, (AbstractTxnProcessor) bundleOneClass.newInstance());
                        }
                    continue;
                }
            }
        }
        return classMap;
    }

    // 递归扫描所有的子包
    private void scanDir(String packageName, String filePath, Hashtable<String, AbstractTxnProcessor> classMap) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        File dir = new File(filePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 过滤出包和字节码文件
            public boolean accept(File file) {
                return (true && file.isDirectory())
                        || (file.getName().endsWith(".class"));
            }
        });
        for (File file : dirfiles) {
            System.out.println(file.getName());
            if (file.isDirectory()) {
                scanDir(packageName + "." + file.getName(), file.getPath(), classMap);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                Class clazz = Class.forName(packageName + "." + className);
                if (clazz.isAnnotationPresent(Processor.class)) {
                    Processor processor = (Processor) clazz.getAnnotation(Processor.class);
                    String txnCode = processor.txnCode();

                    if (classMap.get(txnCode) != null) {
                        logger.error("交易号" + txnCode + "重复,Class:" + packageName + "." + className);
                        throw new RuntimeException("交易号" + txnCode + "重复,Class:" + packageName + "." + className);
                    } else
                        classMap.put(txnCode, (AbstractTxnProcessor) clazz.newInstance());
                }
            }
        }
    }

}
