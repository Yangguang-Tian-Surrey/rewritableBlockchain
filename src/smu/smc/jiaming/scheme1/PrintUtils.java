package smu.smc.jiaming.scheme1;

import java.util.Arrays;

public final class PrintUtils {
    private PrintUtils(){}

    public static String bytes2Hex(byte[] bytes){
        if(bytes == null)
            return "";
        if(bytes.length == 0)
            return "";
        String res = "";
        int s;
        for(int i = 0; i < 2*bytes.length; i++){
            s = bytes[i/2]>>(4*((i+1)%2)) & 0x0f;
            res += s<10?(char)((int)'0'+s):(char)((int)'A'+s-10);
        }
        return res;
    }

    public static byte[] hex2Bytes(String hex){
        if(hex == null)
            return null;
        if(hex.length() == 0)
            return null;
        int offset = hex.length()%2;
        hex = offset==0?hex:"0"+hex;
        int len = hex.length()/2;
        byte[] res = new byte[len];
        Arrays.fill(res, (byte) 0);
        int s;
        for(int i = 0; i < hex.length(); i++){
            s = hex.charAt(i);
            s = s>='0'&&s<='9'?s-'0':s-'A'+10;
            res[i/2] += (byte)(s<<(4*((i+1)%2)));
        }
        return res;
    }

    public static void main(String[] args){
        byte[] b = new byte[]{(byte)0xf1, (byte)0xf2};
        String hex = bytes2Hex(b);
        System.out.println(hex);
        b = hex2Bytes(hex);
        hex = bytes2Hex(b);
        System.out.println(hex);
    }
}
