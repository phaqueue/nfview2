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
    public void simple() throws IOException {

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
        Assert.assertEquals(sub(standardOutput), sub(
                "USE ClientTest;\n\n" +
                        "CREATE OR REPLACE VIEW _Anon1View AS\n" +
                        "\tSELECT List1Set.id, _pos1, _Anon1\n" +
                        "\tFROM List1Set, List1Set.a _Anon1 AT _pos1;\n\n"));
    }

    @Test
    public void noList() throws IOException {

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
        Assert.assertEquals(sub(standardOutput), sub(
                "USE ClientTest;\n\n" +
                        "CREATE OR REPLACE VIEW NestedSetView AS\n" +
                        "\tSELECT NestedSet.id, NestedSet.stuff.a, NestedSet.stuff.b\n" +
                        "\tFROM NestedSet;\n\n"));
    }

    @Test
    public void juxtaposedListAndNonAnonymousTypeAndNoPK() throws IOException {

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
        Assert.assertEquals(sub(standardOutput), sub(
                "USE ClientTest;\n\n" +
                        "CREATE OR REPLACE VIEW _Anon1View AS\n" +
                        "\tSELECT ListSet.id, _pos1, _Anon1\n" +
                        "\tFROM ListSet, ListSet.lis1 _Anon1 AT _pos1;\n\n" +
                        "CREATE OR REPLACE VIEW _Anon2View AS\n" +
                        "\tSELECT ListSet.id, _pos2, _Anon2.id AS id2, _Anon2.stuff.a, _Anon2.stuff.b\n" +
                        "\tFROM ListSet, ListSet.lis2 _Anon2 AT _pos2;\n\n"));
    }

    @Test
    public void juxtaposedListAndNonAnonymousTypeAndPKSpecified() throws IOException {

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
        Assert.assertEquals(sub(standardOutput), sub(
                "USE ClientTest;\n\n" +
                        "CREATE OR REPLACE VIEW _Anon1View AS\n" +
                        "\tSELECT ListSet.id, _Anon1\n" +
                        "\tFROM ListSet, ListSet.lis1 _Anon1 AT _pos1;\n\n" +
                        "CREATE OR REPLACE VIEW _Anon2View AS\n" +
                        "\tSELECT ListSet.id, _Anon2.id AS id2, _Anon2.stuff.a, _Anon2.stuff.b\n" +
                        "\tFROM ListSet, ListSet.lis2 _Anon2 AT _pos2;\n\n"));
    }

    @Test
    public void List5AndNoPK() throws IOException {

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
        Assert.assertEquals(sub(standardOutput), sub(
                "USE ClientTest;\n\n" +
                        "CREATE OR REPLACE VIEW _Anon5View AS\n" +
                        "\tSELECT List5Set.id, _pos1, _pos2, _pos3, _pos4, _pos5, _Anon5\n" +
                        "\tFROM List5Set, List5Set.a _Anon1 AT _pos1, _Anon1 _Anon2 AT _pos2, _Anon2 _Anon3 AT _pos3, _Anon3 _Anon4 AT _pos4, _Anon4 _Anon5 AT _pos5;\n\n"));
    }

    @Test
    public void everyFlatFieldIsPKAndListOfListOfRecordAndDupedFieldNames() throws IOException {

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
        Assert.assertEquals(sub(standardOutput), sub(
                "USE ClientTest;\n\n" +
                        "CREATE OR REPLACE VIEW _Anon1View AS\n" +
                        "\tSELECT PKSet.id, PKSet.a, PKSet.b.a AS a2, PKSet.b.b, _Anon1\n" +
                        "\tFROM PKSet, PKSet.b.c _Anon1 AT _pos1;\n\n" +
                        "CREATE OR REPLACE VIEW _Anon3View AS\n" +
                        "\tSELECT PKSet.id, PKSet.a, PKSet.b.a AS a2, PKSet.b.b, _pos2, _Anon3.a AS a3, _Anon3.b AS b2\n" +
                        "\tFROM PKSet, PKSet.c _Anon2 AT _pos2, _Anon2 _Anon3 AT _pos3;\n\n"));
    }

    private void SuperNestedInit() throws IOException {
        HttpAPIClient WailClient = new HttpAPIClient("localhost", "19002");
        String query = "DROP DATAVERSE ClientTest IF EXISTS;" +
                "CREATE DATAVERSE ClientTest;" +
                "USE ClientTest;" +

                "CREATE TYPE SuperNestedType AS {" +
                "   id: int," +
                "   a: {a: [[{a:int}]], b: [{a: [int], b: int}], c: {a: int}}," +
                "   b: {a: int, b: {a: int, b: [[[int]]]}, c: {a: {a: {a: int, b: [int]}}}, d: int, e: int, f: int, g: [int], h: int, i: int, j: [int]}," +
                "   c: int" +
                "};" +

                "CREATE DATASET SuperNestedSet (SuperNestedType)" +
                "   PRIMARY KEY a.c.a, b.a, b.c.a.a.a;";
        String idk = WailClient.exec(query).toString();

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"ClientTest", "SuperNestedSet", "-w", "json.txt"};
        Client.main(args1);


        //Modify the PKs
        modify(14, "\t\t\t\t\t\"PrimaryKeys\": [\"a\"],\n");
        modify(22, "\t\t\t\"PrimaryKeys\": [\"b\"],\n");

        String[] args2 = new String[]{"ClientTest", "SuperNestedSet", "-r", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        String substandardOutput = sub(standardOutput);
        idk = WailClient.exec(substandardOutput).toString();

        final String insertion = "USE ClientTest;" +
                "INSERT INTO SuperNestedSet (" +
                "[" +
                "   {" +
                "       \"id\": 1," +
                "       \"a\":" +
                "       {" +
                "           \"a\": [[{\"a\": 1}, {\"a\": 2}, {\"a\": 3}], [{\"a\": 2}, {\"a\": 3}, {\"a\": 4}], [{\"a\": 3}, {\"a\": 4}, {\"a\": 5}]]," +
                "           \"b\": [{\"a\": [4, 5, 6], \"b\": 1}, {\"a\": [5, 6, 7], \"b\": 2}, {\"a\": [6, 7, 8], \"b\": 3}]," +
                "           \"c\": {\"a\": 1}" +
                "       }," +
                "       \"b\":" +
                "       {" +
                "           \"a\": 1," +
                "           \"b\": {\"a\": 1, \"b\": [[[1, 2], [2, 3]], [[3, 4], [4, 5]]]}," +
                "           \"c\": {\"a\": {\"a\": {\"a\": 1, \"b\": [1, 2, 3]}}}," +
                "           \"d\": 1, \"e\": 1, \"f\": 1, \"g\": [1, 2, 3], \"h\": 1, \"i\": 1, \"j\": [1, 2, 3]" +
                "       }," +
                "       \"c\": 1" +
                "   }," +
                "   {" +
                "       \"id\": 2," +
                "       \"a\":" +
                "       {" +
                "           \"a\": [[{\"a\": 4}, {\"a\": 5}, {\"a\": 6}], [{\"a\": 5}, {\"a\": 6}, {\"a\": 7}], [{\"a\": 6}, {\"a\": 7}, {\"a\": 8}]]," +
                "           \"b\": [{\"a\": [7, 8, 9], \"b\": 2}, {\"a\": [8, 9, 10], \"b\": 3}, {\"a\": [9, 10, 11], \"b\": 4}]," +
                "           \"c\": {\"a\": 2}" +
                "       }," +
                "       \"b\":" +
                "       {" +
                "           \"a\": 4," +
                "           \"b\": {\"a\": 4, \"b\": [[[6, 7], [7, 8]], [[8, 9], [9, 10]]]}," +
                "           \"c\": {\"a\": {\"a\": {\"a\": 4, \"b\": [4, 5, 6]}}}," +
                "           \"d\": 4, \"e\": 4, \"f\": 4, \"g\": [4, 5, 6], \"h\": 4, \"i\": 4, \"j\": [7, 8, 9]" +
                "       }," +
                "       \"c\": 2" +
                "   }" +
                "])";
        WailClient.exec(insertion);
    }

    @Test
    public void SuperNested0() throws IOException {
        SuperNestedInit();

        HttpAPIClient WailClient = new HttpAPIClient("localhost", "19002");

        String result = WailClient.getResult("USE ClientTest; SELECT * FROM SuperNestedSetView;");
        Assert.assertEquals(sub("[{\"SuperNestedSetView\":{\"id\":1,\"a\":1,\"a2\":1,\"a3\":1,\"a4\":1,\"d\":1,\"e\":1,\"f\":1,\"h\":1,\"i\":1,\"c\":1}},{\"SuperNestedSetView\":{\"id\":2,\"a\":2,\"a2\":4,\"a3\":4,\"a4\":4,\"d\":4,\"e\":4,\"f\":4,\"h\":4,\"i\":4,\"c\":2}}]"), sub(result));
    }

    @Test
    public void SuperNested1() throws IOException {
        SuperNestedInit();

        HttpAPIClient WailClient = new HttpAPIClient("localhost", "19002");

        String result = WailClient.getResult("USE ClientTest; SELECT * FROM _Anon2View;");
        Assert.assertEquals(sub("[{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":1,\"a4\":1}},{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":1,\"a4\":2}},{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":1,\"a4\":3}},{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":2,\"a4\":2}},{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":2,\"a4\":3}},{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":2,\"a4\":4}},{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":3,\"a4\":3}},{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":3,\"a4\":4}},{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":3,\"a4\":5}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":1,\"a4\":4}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":1,\"a4\":5}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":1,\"a4\":6}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":2,\"a4\":5}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":2,\"a4\":6}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":2,\"a4\":7}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":3,\"a4\":6}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":3,\"a4\":7}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":3,\"a4\":8}}]"), sub(result));
    }

    @Test
    public void SuperNested2() throws IOException {
        SuperNestedInit();

        HttpAPIClient WailClient = new HttpAPIClient("localhost", "19002");

        String result = WailClient.getResult("USE ClientTest; SELECT * FROM _Anon4View;");
        Assert.assertEquals(sub("[{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":1,\"_pos4\":1,\"_Anon4\":4}},{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":1,\"_pos4\":2,\"_Anon4\":5}},{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":1,\"_pos4\":3,\"_Anon4\":6}},{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":2,\"_pos4\":1,\"_Anon4\":5}},{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":2,\"_pos4\":2,\"_Anon4\":6}},{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":2,\"_pos4\":3,\"_Anon4\":7}},{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":3,\"_pos4\":1,\"_Anon4\":6}},{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":3,\"_pos4\":2,\"_Anon4\":7}},{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":3,\"_pos4\":3,\"_Anon4\":8}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":2,\"_pos4\":1,\"_Anon4\":7}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":2,\"_pos4\":2,\"_Anon4\":8}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":2,\"_pos4\":3,\"_Anon4\":9}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":3,\"_pos4\":1,\"_Anon4\":8}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":3,\"_pos4\":2,\"_Anon4\":9}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":3,\"_pos4\":3,\"_Anon4\":10}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":4,\"_pos4\":1,\"_Anon4\":9}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":4,\"_pos4\":2,\"_Anon4\":10}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":4,\"_pos4\":3,\"_Anon4\":11}}]"), sub(result));
    }

    @Test
    public void SuperNested3() throws IOException {
        SuperNestedInit();

        HttpAPIClient WailClient = new HttpAPIClient("localhost", "19002");

        String result = WailClient.getResult("USE ClientTest; SELECT * FROM _Anon7View;");
        Assert.assertEquals(sub("[{\"_Anon7View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos5\":1,\"_pos6\":1,\"_pos7\":1,\"_Anon7\":1}},{\"_Anon7View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos5\":1,\"_pos6\":1,\"_pos7\":2,\"_Anon7\":2}},{\"_Anon7View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos5\":1,\"_pos6\":2,\"_pos7\":1,\"_Anon7\":2}},{\"_Anon7View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos5\":1,\"_pos6\":2,\"_pos7\":2,\"_Anon7\":3}},{\"_Anon7View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos5\":2,\"_pos6\":1,\"_pos7\":1,\"_Anon7\":3}},{\"_Anon7View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos5\":2,\"_pos6\":1,\"_pos7\":2,\"_Anon7\":4}},{\"_Anon7View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos5\":2,\"_pos6\":2,\"_pos7\":1,\"_Anon7\":4}},{\"_Anon7View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos5\":2,\"_pos6\":2,\"_pos7\":2,\"_Anon7\":5}},{\"_Anon7View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos5\":1,\"_pos6\":1,\"_pos7\":1,\"_Anon7\":6}},{\"_Anon7View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos5\":1,\"_pos6\":1,\"_pos7\":2,\"_Anon7\":7}},{\"_Anon7View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos5\":1,\"_pos6\":2,\"_pos7\":1,\"_Anon7\":7}},{\"_Anon7View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos5\":1,\"_pos6\":2,\"_pos7\":2,\"_Anon7\":8}},{\"_Anon7View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos5\":2,\"_pos6\":1,\"_pos7\":1,\"_Anon7\":8}},{\"_Anon7View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos5\":2,\"_pos6\":1,\"_pos7\":2,\"_Anon7\":9}},{\"_Anon7View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos5\":2,\"_pos6\":2,\"_pos7\":1,\"_Anon7\":9}},{\"_Anon7View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos5\":2,\"_pos6\":2,\"_pos7\":2,\"_Anon7\":10}}]"), sub(result));
    }

    @Test
    public void SuperNested4() throws IOException {
        SuperNestedInit();

        HttpAPIClient WailClient = new HttpAPIClient("localhost", "19002");

        String result = WailClient.getResult("USE ClientTest; SELECT * FROM _Anon8View;");
        Assert.assertEquals(sub("[{\"_Anon8View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos8\":1,\"_Anon8\":1}},{\"_Anon8View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos8\":2,\"_Anon8\":2}},{\"_Anon8View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos8\":3,\"_Anon8\":3}},{\"_Anon8View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos8\":1,\"_Anon8\":4}},{\"_Anon8View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos8\":2,\"_Anon8\":5}},{\"_Anon8View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos8\":3,\"_Anon8\":6}}]"), sub(result));
    }

    @Test
    public void SuperNested5() throws IOException {
        SuperNestedInit();

        HttpAPIClient WailClient = new HttpAPIClient("localhost", "19002");

        String result = WailClient.getResult("USE ClientTest; SELECT * FROM _Anon9View;");
        Assert.assertEquals(sub("[{\"_Anon9View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos9\":1,\"_Anon9\":1}},{\"_Anon9View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos9\":2,\"_Anon9\":2}},{\"_Anon9View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos9\":3,\"_Anon9\":3}},{\"_Anon9View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos9\":1,\"_Anon9\":4}},{\"_Anon9View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos9\":2,\"_Anon9\":5}},{\"_Anon9View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos9\":3,\"_Anon9\":6}}]"), sub(result));
    }

    @Test
    public void SuperNested6() throws IOException {
        SuperNestedInit();

        HttpAPIClient WailClient = new HttpAPIClient("localhost", "19002");

        String result = WailClient.getResult("USE ClientTest; SELECT * FROM _Anon10View;");
        Assert.assertEquals(sub("[{\"_Anon10View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos10\":1,\"_Anon10\":1}},{\"_Anon10View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos10\":2,\"_Anon10\":2}},{\"_Anon10View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos10\":3,\"_Anon10\":3}},{\"_Anon10View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos10\":1,\"_Anon10\":7}},{\"_Anon10View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos10\":2,\"_Anon10\":8}},{\"_Anon10View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos10\":3,\"_Anon10\":9}}]"), sub(result));
    }
}
