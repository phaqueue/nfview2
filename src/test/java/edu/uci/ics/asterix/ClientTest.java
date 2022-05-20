package edu.uci.ics.asterix;

import edu.uci.ics.asterix.api.HttpAPIClient;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class ClientTest {

    private static String sub(String s) {
        s = s.replaceAll("\\r\\t", "\t");
        return s.replaceAll("\\r\\n", "\n");
    }

    private static boolean modify(int pos, String newStr) {
        //https://blog.csdn.net/yinghuacao_dong/article/details/79578081

        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile("D:\\UCI\\ICSH198S\\nfview2\\JSON.txt", "rw");
            String line = null;
            for (int i = 1; i < pos; i++)
                line = f.readLine();
            long p = f.getFilePointer();
            line = f.readLine();
            String s = newStr;
            while ((line = f.readLine()) != null)
                s += line + '\n';
            f.seek(p);
            f.write(s.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            System.out.println("Fail to modify the JSON file");
        } finally {
            try {
                f.close();
            } catch (Exception e) {
                System.out.println("Cannot close the file lol.");
            }
        }
        return true;
    }

    @Test
    public void test0() throws IOException {

        HttpAPIClient WailClient = new HttpAPIClient("localhost", "19002");
        String query = "DROP DATAVERSE ClientTest IF EXISTS;" +
                "CREATE DATAVERSE ClientTest;" +
                "USE ClientTest;" +
                "CREATE TYPE List1Type AS {" +
                "   id: int," +
                "   a: [int]" +
                "};" +
                "CREATE DATASET List1Set (List1Type)" +
                "   PRIMARY KEY id;";
        WailClient.exec(query);

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"ClientTest", "List1Set", "-w", "json.txt"};
        Client.main(args1);

        String[] args2 = new String[]{"ClientTest", "List1Set", "-r", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        Assert.assertEquals(sub(standardOutput), sub("CREATE OR REPLACE VIEW _Anon1View AS\n" +
                "\tSELECT List1Set.id, _pos1, _Anon1\n" +
                "\tFROM List1Set, List1Set.a _Anon1 AT _pos1;\n\n"));
    }

    @Test
    public void test1() throws IOException {

        HttpAPIClient WailClient = new HttpAPIClient("localhost", "19002");
        String query = "DROP DATAVERSE ClientTest IF EXISTS;" +
                "CREATE DATAVERSE ClientTest;" +
                "USE ClientTest;" +
                "CREATE TYPE NestedType AS {" +
                "   id: int," +
                "   stuff: {" +
                "       a: int," +
                "       b: int" +
                "   }" +
                "};" +
                "CREATE DATASET NestedSet (NestedType)" +
                "   PRIMARY KEY stuff.a;";
        WailClient.exec(query);

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"ClientTest", "NestedSet", "-w", "json.txt"};
        Client.main(args1);

        String[] args2 = new String[]{"ClientTest", "NestedSet", "-r", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        Assert.assertEquals(sub(standardOutput), sub("CREATE OR REPLACE VIEW NestedSetView AS\n" +
                "\tSELECT NestedSet.id, NestedSet.stuff.a, NestedSet.stuff.b\n" +
                "\tFROM NestedSet;\n\n"));
    }

    @Test
    public void test2() throws IOException {

        HttpAPIClient WailClient = new HttpAPIClient("localhost", "19002");
        String query = "DROP DATAVERSE ClientTest IF EXISTS;" +
                "CREATE DATAVERSE ClientTest;" +
                "USE ClientTest;" +
                "CREATE TYPE NestedType AS {" +
                "   id: int," +
                "   stuff: {" +
                "       a: int," +
                "       b: int" +
                "   }" +
                "};" +
                "CREATE TYPE ListType AS {" +
                "   id: int," +
                "   lis1: [int]," +
                "   lis2: [NestedType]" +
                "};" +
                "CREATE DATASET ListSet (ListType)" +
                "   PRIMARY KEY id;";
        WailClient.exec(query);

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"ClientTest", "ListSet", "-w", "json.txt"};
        Client.main(args1);

        String[] args2 = new String[]{"ClientTest", "ListSet", "-r", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        Assert.assertEquals(sub(standardOutput), sub("CREATE OR REPLACE VIEW _Anon1View AS\n" +
                "\tSELECT ListSet.id, _pos1, _Anon1\n" +
                "\tFROM ListSet, ListSet.lis1 _Anon1 AT _pos1;\n\n" +
                "CREATE OR REPLACE VIEW _Anon2View AS\n" +
                "\tSELECT ListSet.id, _pos2, _Anon2.id AS id2, _Anon2.stuff.a, _Anon2.stuff.b\n" +
                "\tFROM ListSet, ListSet.lis2 _Anon2 AT _pos2;\n\n"));
    }

    @Test
    public void test3() throws IOException {

        HttpAPIClient WailClient = new HttpAPIClient("localhost", "19002");
        String query = "DROP DATAVERSE ClientTest IF EXISTS;" +
                "CREATE DATAVERSE ClientTest;" +
                "USE ClientTest;" +
                "CREATE TYPE NestedType AS {" +
                "   id: int," +
                "   stuff: {" +
                "       a: int," +
                "       b: int" +
                "   }" +
                "};" +
                "CREATE TYPE ListType AS {" +
                "   id: int," +
                "   lis1: [int]," +
                "   lis2: [NestedType]" +
                "};" +
                "CREATE DATASET ListSet (ListType)" +
                "   PRIMARY KEY id;";
        WailClient.exec(query);

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"ClientTest", "ListSet", "-w", "json.txt"};
        Client.main(args1);

        //Modify the PKs
        modify(9, "\t\t\t\"PrimaryKeys\": [\"int64\"],\n");
        modify(15, "\t\t\t\"PrimaryKeys\": [\"id\"],\n");

        String[] args2 = new String[]{"ClientTest", "ListSet", "-r", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        Assert.assertEquals(sub(standardOutput), sub("CREATE OR REPLACE VIEW _Anon1View AS\n" +
                "\tSELECT ListSet.id, _Anon1\n" +
                "\tFROM ListSet, ListSet.lis1 _Anon1 AT _pos1;\n\n" +
                "CREATE OR REPLACE VIEW _Anon2View AS\n" +
                "\tSELECT ListSet.id, _Anon2.id AS id2, _Anon2.stuff.a, _Anon2.stuff.b\n" +
                "\tFROM ListSet, ListSet.lis2 _Anon2 AT _pos2;\n\n"));
    }

    @Test
    public void test4() throws IOException {

        HttpAPIClient WailClient = new HttpAPIClient("localhost", "19002");
        String query = "DROP DATAVERSE ClientTest IF EXISTS;" +
                "CREATE DATAVERSE ClientTest;" +
                "USE ClientTest;" +
                "CREATE TYPE List5Type AS {" +
                "   id: int," +
                "   a: [[[[[int]]]]]" +
                "};" +

                "CREATE DATASET List5Set (List5Type)" +
                "   PRIMARY KEY id;";
        WailClient.exec(query);

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"ClientTest", "List5Set", "-w", "json.txt"};
        Client.main(args1);

        String[] args2 = new String[]{"ClientTest", "List5Set", "-r", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        Assert.assertEquals(sub(standardOutput), sub("CREATE OR REPLACE VIEW _Anon5View AS\n" +
                "\tSELECT List5Set.id, _pos1, _pos2, _pos3, _pos4, _pos5, _Anon5\n" +
                "\tFROM List5Set, List5Set.a _Anon1 AT _pos1, _Anon1 _Anon2 AT _pos2, _Anon2 _Anon3 AT _pos3, _Anon3 _Anon4 AT _pos4, _Anon4 _Anon5 AT _pos5;\n\n"));
    }

    @Test
    public void test5() throws IOException {

        HttpAPIClient WailClient = new HttpAPIClient("localhost", "19002");
        String query = "DROP DATAVERSE ClientTest IF EXISTS;" +
                "CREATE DATAVERSE ClientTest;" +
                "USE ClientTest;" +
                "CREATE TYPE PKType AS {" +
                "   id: int," +
                "   a: int," +
                "   b: {a: int, b: int, c: [int]}," +
                "   c: [[{a: int, b: int}]]" +
                "};" +
                "CREATE DATASET PKSet (PKType)" +
                "   PRIMARY KEY id, a, b.a, b.b;";
        WailClient.exec(query);

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"ClientTest", "PKSet", "-w", "json.txt"};
        Client.main(args1);

        //Modify the PKs
        modify(9, "\t\t\t\"PrimaryKeys\": [\"int64\"],\n");
        modify(20, "\t\t\t\t\t\"PrimaryKeys\": [\"a\", \"b\"],\n");

        String[] args2 = new String[]{"ClientTest", "PKSet", "-r", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        Assert.assertEquals(sub(standardOutput), sub("CREATE OR REPLACE VIEW _Anon1View AS\n" +
                "\tSELECT PKSet.id, PKSet.a, PKSet.b.a AS a2, PKSet.b.b, _Anon1\n" +
                "\tFROM PKSet, PKSet.b.c _Anon1 AT _pos1;\n\n" +
                "CREATE OR REPLACE VIEW _Anon3View AS\n" +
                "\tSELECT PKSet.id, PKSet.a, PKSet.b.a AS a2, PKSet.b.b, _pos2, _Anon3.a AS a3, _Anon3.b AS b2\n" +
                "\tFROM PKSet, PKSet.c _Anon2 AT _pos2, _Anon2 _Anon3 AT _pos3;\n\n"));
    }
}
