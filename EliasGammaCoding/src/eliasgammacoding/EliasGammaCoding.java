package eliasgammacoding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class EliasGammaCoding {

    static final String PATH_ARQUIVO_CODIFICADO = "C://leia//encode";
    static final String PATH_ARQUIVO_NAO_CODIFICADO = "C://leia//teste.txt";
    static final String PATH_ARQUIVO_CODIFICADO_DICIONARIO = "C://leia//dicionario";

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

    private static Map criarAlfabeto(String caracteres) {
        Map<Integer, Integer> alfabeto = new HashMap<Integer, Integer>();
        int d;
        for (int i = 0; i < caracteres.length(); i++) {
            d = ((int) caracteres.charAt(i));
            int cont = 1;
            if (alfabeto.containsKey(d)) {
                cont = alfabeto.get(d) + 1;
            }
            alfabeto.put(d, cont);
        }
        ValueComparator alf = new ValueComparator(alfabeto);
        TreeMap<Integer, Integer> sorted_map = new TreeMap<Integer, Integer>(alf);
        sorted_map.putAll(alfabeto);

        Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        int representation = 0;

        for (Map.Entry<Integer, Integer> entry : sorted_map.entrySet()) {
            Integer k = entry.getKey();
            result.put(k, representation);
            representation++;
        }
        return result;
    }

    private static String lerArquivo(Path path) throws IOException {
        Stream<String> linhas = Files.lines(path);
        String dadosArquivo = "";
        for (Object linha : linhas.toArray()) {
            dadosArquivo += linha.toString();
        }
        return dadosArquivo;
    }

    private static void encodeDicionario(Map<Integer, Integer> dicionario) throws FileNotFoundException, IOException {
        FileOutputStream inD = new FileOutputStream(new File(PATH_ARQUIVO_CODIFICADO_DICIONARIO));
        BitSet bsDic = new BitSet();
        int t = 0, p = 0;
        for (Map.Entry<Integer, Integer> entry : dicionario.entrySet()) {
            Integer k = entry.getKey();
            Integer v = entry.getValue();

            /*encode key*/
            BitSet bsD = egEncode(k);
            t = egSize(k);
            for (int j = 0; j < t; j++) {
                bsDic.set(p, bsD.get(j));
                p++;
            }
            /*encode value*/
            bsD = egEncode(v);
            t = egSize(v);
            for (int j = 0; j < t; j++) {
                bsDic.set(p, bsD.get(j));
                p++;
            }
        }
        inD.write(bsDic.toByteArray());
    }

    private static Map<Integer, Integer> decodeDicionario() throws IOException {
        File dicionario = new File(PATH_ARQUIVO_CODIFICADO_DICIONARIO);
        byte[] ans = Files.readAllBytes(Paths.get(dicionario.getAbsolutePath()));
        BitSet newBS = BitSet.valueOf(ans);
        /* percorrer bitset adicionando no dicionario primeiro o valor, depois a chave */
        Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        int q = 0, j, t = 0;
        Integer valorValor = null, valorChave = null;
        while (q < newBS.length()) {
            BitSet bsD = new BitSet();
            j = newBS.nextSetBit(q) - q;
            if (j == 0) {
                t = 2;
            } else {
                t = j * 2 + 1;
            }
            for (int k = 0; k < t; k++) {
                bsD.set(k, newBS.get(q + k));
            }
            q += t;
            if (valorChave == null) {
                valorChave = egDecode(bsD);
                continue;
            }

            if (valorValor == null) {
                valorValor = egDecode(bsD);
            }
            if (valorValor != null && valorChave != null) {
                result.put(valorChave, valorValor);
                System.out.println("Caracter " + (char)valorChave.intValue() + " representada por " + valorValor + ": (byte)" + bitSetSequence(egEncode(valorValor), egSize(valorValor)));
                valorValor = null;
                valorChave = null;
            }
        }
        return result;
    }

    static class ValueComparator implements Comparator<Integer> {

        Map<Integer, Integer> base;

        public ValueComparator(Map<Integer, Integer> base) {
            this.base = base;
        }

        public int compare(Integer a, Integer b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            }
        }

    }

    public static void main(String[] args) throws IOException {
        Path teste = Paths.get(new File(PATH_ARQUIVO_NAO_CODIFICADO).getAbsolutePath());
        String dadosArquivo = lerArquivo(teste);

        int i, d, t, p = 0;
        BitSet bsD = new BitSet(), bsCodificado = new BitSet();

        Map<Integer, Integer> alfabeto = criarAlfabeto(dadosArquivo);
        System.out.println(alfabeto);

        for (i = 0; i < dadosArquivo.length(); i++) {
            Integer k = (int) dadosArquivo.charAt(i);
            d = alfabeto.get(k);

            bsD = egEncode(d);
            t = egSize(d);
            for (int j = 0; j < t; j++) {
                bsCodificado.set(p, bsD.get(j));
                p++;
            }
        }

        File encode = new File(PATH_ARQUIVO_CODIFICADO);
        FileOutputStream in = new FileOutputStream(encode);
        in.write(bsCodificado.toByteArray());

        /* cria um arquivo dicionario com o alfabeto passado por parametro */
        encodeDicionario(alfabeto);

        Map<Integer, Integer> alfabeto2 = decodeDicionario();

        byte[] ans = Files.readAllBytes(Paths.get(encode.getAbsolutePath()));
        BitSet newBS = BitSet.valueOf(ans);
        int q = 0, j;
        t = 0;
        String novaString = "";
        while (q < newBS.length()) {
            bsD = new BitSet();
            j = newBS.nextSetBit(q) - q;
            if (j == 0) {
                t = 2;
            } else {
                t = j * 2 + 1;
            }
            for (int k = 0; k < t; k++) {
                bsD.set(k, newBS.get(q + k));
            }
            q += t;
            d = egDecode(bsD);

            for (Map.Entry<Integer, Integer> entry : alfabeto2.entrySet()) {
                int k = entry.getKey();
                Integer v = entry.getValue();
                if (v == d) {
                    novaString += (char) k;
                    break;
                }
            }
        }
        System.out.println(novaString);
    }

}
