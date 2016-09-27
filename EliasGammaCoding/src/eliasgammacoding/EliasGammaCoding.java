package eliasgammacoding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class EliasGammaCoding {

    public static String bitSetSequence(BitSet bs, int t) {
        String s = "";
        for (int i = 0; i < t; i++) {
            if (i % 8 == 0 && i > 0) {
                s += " ";
            }
            s += (bs.get(i) ? "1" : "0");
        }
        return s;
    }

    public static BitSet egEncode(int n) {
        BitSet codigo;
        if (n == 0 || n == 1) {
            codigo = new BitSet(2);
            codigo.set(0);
            if (n == 1) {
                codigo.set(1);
            }
            return codigo;
        }

        int p1 = (int) (Math.log(n) / Math.log(2));
        int p2 = n - (int) Math.pow(2, p1);
        codigo = new BitSet(p1 * 2 + 1);

        int i;
        for (i = 0; i < p1; i++) {
            codigo.clear(i);
        }

        codigo.set(p1);
        int k = 1 << (p1 - 1);
        for (i = p1 + 1; i < p1 * 2 + 1; i++) {
            codigo.set(i, (p2 & k) != 0);
            k >>= 1;
        }
        return codigo;
    }

    public static int egSize(int n) {
        if (n == 0 || n == 1) {
            return 2;
        }
        return (int) (Math.log(n) / Math.log(2)) * 2 + 1;
    }

    public static int egDecode(BitSet codigo) {
        int separador = codigo.nextSetBit(0);
        if (separador == 0) {
            if (codigo.get(1)) {
                return 1;
            } else {
                return 0;
            }
        }

        int n = (int) Math.pow(2, separador);
        int i = separador + 1;
        int pot = separador - 1;
        while (pot >= 0) {
            if (codigo.get(i)) {
                n += (int) Math.pow(2, pot);
            }
            i++;
            pot--;
        }
        return n;
    }

    public static void main(String[] args) throws IOException {
        Path teste = Paths.get(new File("C://leia/teste.txt").getAbsolutePath());
        Stream<String> linhas = Files.lines(teste);

        String dadosArquivo = "";
        for (Object linha : linhas.toArray()) {
            dadosArquivo += linha.toString();
        }

        int i, d, t, p = 0;
        BitSet bsD = new BitSet(), bsEAN = new BitSet();
        Map<Integer, Integer> alfabeto = new HashMap<Integer, Integer>();

        for (i = 0; i < dadosArquivo.length(); i++) {
            d = ((int) dadosArquivo.charAt(i));
            int cont = 1;
            if (alfabeto.containsKey(d)) {
                cont = alfabeto.get(d) + 1;
            }
            alfabeto.put(d, cont);

            bsD = egEncode(d);
            t = egSize(d);

            System.out.println(d);
            System.out.println(alfabeto);
            System.out.println(dadosArquivo.charAt(i));
            System.out.println(bitSetSequence(bsD, t));
            System.out.println((char) d);

            for (int j = 0; j < t; j++) {
                bsEAN.set(p, bsD.get(j));
                p++;
            }
        }

        File encode = new File("C://leia//encode");
        FileOutputStream in = new FileOutputStream(encode);
        in.write(bsEAN.toByteArray());

        int q = 0, j;
        String novaEAN = "";
        while (q < p) {
            bsD = new BitSet();
            j = bsEAN.nextSetBit(q) - q;
            if (j == 0) {
                t = 2;
            } else {
                t = j * 2 + 1;
            }
            for (int k = 0; k < t; k++) {
                bsD.set(k, bsEAN.get(q + k));
            }
            q += t;
            d = egDecode(bsD);
            novaEAN += d;
        }
        System.out.println(novaEAN);
    }
}
