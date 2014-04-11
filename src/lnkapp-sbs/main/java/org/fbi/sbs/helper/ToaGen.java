package org.fbi.sbs.helper;

import java.io.*;

/**
 * Created by lenovo on 2014-4-11 0011.
 */
public class ToaGen {
    public static void main(String... args) {

        try {
            String srcFile = "D:\\workspace\\pco-sbs\\ac\\book\\ACKT908";
            String formCode = "T908";
            FileInputStream fis = new FileInputStream(new File(srcFile));
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            File javaFile = new File("src/lnkapp-sbs/main/java/org/fbi/sbs/domain/Toa" + formCode + ".java");
            if(javaFile.exists()) {
                System.out.println(javaFile.getAbsolutePath()+ javaFile.getName() + " 已存在");
                return;
            } else {
                javaFile.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(javaFile)));
            StringBuffer buf = new StringBuffer("package org.fbi.sbs.domain;\n" +
                    "\n" +
                    "public class Toa" + formCode + " extends Toa {\n");
            String line = null;
            String varPrefix = formCode + "-";
            while((line = br.readLine()) != null) {
                if(line.contains(varPrefix)) {
                    int index = line.indexOf(varPrefix) + varPrefix.length();
                    String var = line.substring(index, index + 6).toLowerCase();
                    buf.append("\n    public String " + var + ";");
                }
            }
            buf.append("\n }");
            bw.write(buf.toString());
            bw.flush();
            System.out.println(buf.toString());
            System.out.println(formCode + "Java Toa文件已生成");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
