/**
 * @author MaTianyu
 * @date 2015-03-13
 */
public class StringFormat {

    public static void main(String[] args) {

        //System.out.println(String.format("%03d", 101));
        //System.out.println(String.format("%03d", 3));
        //System.out.println(String.format("%03d%03d", 010,5));
        //System.out.println(String.format("%2d%02d", 2015%1000, 3));

        String hex = "123456";
        //00000000 00000000 56 34 12 00
        hex = String.format("%08x", Long.parseLong(hex));
        System.out.println(hex);
        hex = invert(hex);
        System.out.println(hex);
        hex = String.format("%024s", hex);
        System.out.println(hex);

    }

    static String complete(String hex, String cha, int len) {
        int dif = len - hex.length();
        while (dif-- > 0) {
            hex = cha + hex;
        }
        return hex;
    }

    static String invert(String hex) {
        int len = hex.length();
        String newHex = "";
        for (int i = len; i > 0; i = i - 2) {
            newHex += hex.substring(i - 2, i);
        }
        return newHex;
    }

}
