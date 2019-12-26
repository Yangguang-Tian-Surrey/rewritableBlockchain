package smu.smc.jiaming.elgamal;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IOUtils {

    public static Map<String, BigInteger> loadParams(String path){
        File f = new File(path);
        Map<String, BigInteger> res = null;
        FileReader fr = null;
        BufferedReader br = null;
        try {
            res = new HashMap<>();
            fr = new FileReader(f);
            br = new BufferedReader(fr);
            String[] s;
            String temp;
            BigInteger bi;
            while((temp = br.readLine()) != null) {
                s = temp.trim().split(" ", 2);
                if(s.length != 2)
                    continue;
                s[0] = s[0].trim();
                if("".equals(s[0]))
                    continue;
                s[1] = s[1].trim();
                if("".equals(s[1]))
                    continue;
                try {
                    bi = new BigInteger(s[1]);
                    res.put(s[0], bi);
                } catch (NumberFormatException e){
                    e.printStackTrace();
                    System.clearProperty(temp);
                    continue;
                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            res = null;
        } catch (IOException e) {
            e.printStackTrace();
            res = null;
        } finally {
            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public static void saveParams(String path, Map<String, BigInteger> params){
        File f = new File(path);
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(f);
            bw = new BufferedWriter(fw);
            Set<String> keySet = params.keySet();
            for (String key: keySet) {
                bw.write(key + " " + params.get(key).toString());
                bw.newLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
