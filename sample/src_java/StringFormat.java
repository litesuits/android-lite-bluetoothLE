import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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
        hex = String.format("%8s", hex);
        System.out.println(hex);

        String[] a = new String[]{"a", "b"};
        String[] c = new String[]{"c", "d"};
        String[] e = new String[4];
        System.arraycopy(a, 0, e, 0, a.length);
        System.arraycopy(c, 0, e, a.length, c.length);
        System.out.println(Arrays.toString(e));

        ArrayList<A> list = new ArrayList<A>();
        list.add(new A("1"));
        list.add(new A("2"));
        list.add(new A("3"));
        a(list);
        b(list.toArray());
        b(list);
        c(list.toArray());
    }

    static class A {
        A(String a) {
            this.a = a;
        }

        public String a;

        @Override
        public String toString() {
            return "A{" +
                    "a='" + a + '\'' +
                    '}';
        }
    }

    static void a(Collection<?> os) {
        System.out.println(os.size() + ": " +os);
    }

    static void b(Object... os) {
        System.out.println(os.length + ": " +Arrays.toString(os));
    }

    static void c(Object[] os) {
        System.out.println(os.length + ": " +Arrays.toString(os));
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
