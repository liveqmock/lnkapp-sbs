package org.fbi.sbs.helper;

import java.io.*;

/**
 * Created by lenovo on 2014-4-11 0011.
 */
public class SchemaGen {
    public static void main(String... args) {

        try {
            String srcFile = "D:\\workspace\\pco-sbs\\ac\\book\\ACKT908";
            String formCode = "Toa908";
            FileInputStream fis = new FileInputStream(new File(srcFile));
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            File avscFile = new File("src/lnkapp-sbs/main/resources/schemas/" + formCode + ".avsc");
            if (avscFile.exists()) {
                System.out.println(avscFile.getAbsolutePath() + avscFile.getName() + " 已存在");
                return;
            } else {
                avscFile.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(avscFile)));
            StringBuffer buf = new StringBuffer("{\n" +
                    " \"namespace\": \"org.fbi.sbs.domain.toa\",\n" +
                    " \"type\": \"record\",\n" +
                    " \"name\": \"" + formCode + "\",\n" +
                    " \"fields\": [");
            String line = null;
            String varPrefix = formCode + "-";
            while ((line = br.readLine()) != null) {
                if (line.contains(varPrefix)) {
                    int index = line.indexOf(varPrefix) + varPrefix.length();
                    String var = line.substring(index, index + 6).toLowerCase();
                    buf.append("\n     {\"name\": \"" + var + "\", \"type\": \"string\"},");
                }
            }
            buf.deleteCharAt(buf.length() - 1);
            buf.append("\n ]\n" +
                    "}");
            bw.write(buf.toString());
            bw.flush();
            System.out.println(buf.toString());
            System.out.println(formCode + "模板文件已生成");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
