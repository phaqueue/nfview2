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

    // The newlines can be different in different environments.
    private static String sub(String s) {
        s = s.replaceAll("\\r\\t", "\t");
        return s.replaceAll("\\r\\n", "\n");
    }

    // Insert the specified PKs into the JSON file.
    private static boolean modify(int pos, String newStr) {
        //https://blog.csdn.net/yinghuacao_dong/article/details/79578081

        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile("JSON.txt", "rw");
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
                System.out.println("Cannot close the file.");
            }
        }
        return true;
    }

    // Test -h
    @Test
    public void _h() throws IOException {
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));
        String[] args = new String[]{"-h"};
        Client.main(args);
        final String standardOutput = myOut.toString();
        Assert.assertEquals("usage: nfview2 [-h] [-r <arg>] [-w <arg>]\n" +
                "Create flat views for all nested fields.\n" +
                "\n" +
                " -h         Help\n" +
                " -r <arg>   format: server port dataverseName datasetName fileName\n" +
                "            Read the user specified PKs.\n" +
                " -w <arg>   format: server port dataverseName datasetName fileName\n" +
                "            Write a JSON file for the user.\n", sub(standardOutput));
    }

    // Test flags that do not exist
    @Test
    public void wrongFlag() throws IOException {
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));
        String[] args = new String[]{"-s"};
        Client.main(args);
        final String standardOutput = myOut.toString();
        Assert.assertEquals("Invalid arguments!\n", sub(standardOutput));
    }

    // Test wrong length of argument.
    @Test
    public void wrongLength() throws IOException {
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));
        String[] args = new String[]{"-r", "localhost", "19002"};
        Client.main(args);
        final String standardOutput = myOut.toString();
        Assert.assertEquals("Invalid arguments!\n", sub(standardOutput));
    }

    /*
     * Here are the three Datasets from the famous DonCData.
     * */
    @Test
    public void customers() throws IOException {

        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");
        String query = "DROP DATAVERSE DonCDataSchema IF EXISTS;" +
                "CREATE DATAVERSE DonCDataSchema;" +
                "USE DonCDataSchema;" +
                "CREATE TYPE customersType AS {" +
                "    custid: string," +
                "    name: string," +
                "    address: {" +
                "        street: string," +
                "        city: string," +
                "        zipcode: string?" +
                "    }," +
                "    rating: int?" +
                "};" +
                "CREATE DATASET customers(customersType)" +
                "    PRIMARY KEY custid;";
        myClient.exec(query);

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"-w", "localhost", "19002", "DonCDataSchema", "customers", "json.txt"};
        Client.main(args1);

        String[] args2 = new String[]{"-r", "localhost", "19002", "DonCDataSchema", "customers", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        Assert.assertEquals(sub("USE DonCDataSchema;\n\n" +
                "CREATE OR REPLACE VIEW customersView AS\n" +
                "\tSELECT customers.custid, customers.name, customers.address.street, customers.address.city, customers.address.zipcode, customers.rating\n" +
                "\tFROM customers;\n\n"), sub(standardOutput));
    }

    @Test
    public void ordersPKSpecified() throws IOException {

        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");
        String query = "DROP DATAVERSE DonCDataSchema IF EXISTS;" +
                "CREATE DATAVERSE DonCDataSchema;" +
                "USE DonCDataSchema;" +
                "CREATE TYPE ordersType AS {" +
                "    orderno: int," +
                "    custid: string," +
                "    order_date: string," +
                "    ship_date: string?," +
                "    items: [{" +
                "        itemno: int," +
                "        qty: int," +
                "        price: double" +
                "    }]" +
                "};" +
                "CREATE DATASET orders(ordersType)" +
                "    PRIMARY KEY orderno;";
        myClient.exec(query);

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"-w", "localhost", "19002", "DonCDataSchema", "orders", "json.txt"};
        Client.main(args1);

        modify(9, "\t\t\t\"primaryKey\": [\"itemno\"],\n");

        String[] args2 = new String[]{"-r", "localhost", "19002", "DonCDataSchema", "orders", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        Assert.assertEquals(sub("USE DonCDataSchema;\n\n" +
                "CREATE OR REPLACE VIEW ordersView AS\n" +
                "\tSELECT orders.orderno, orders.custid, orders.order_date, orders.ship_date\n" +
                "\tFROM orders;\n\n" +
                "CREATE OR REPLACE VIEW _Anon1View AS\n" +
                "\tSELECT orders.orderno, _Anon1.itemno, _Anon1.qty, _Anon1.price\n" +
                "\tFROM orders, orders.items _Anon1 AT _pos1;\n\n"), sub(standardOutput));
    }

    @Test
    public void ordersNoPK() throws IOException {

        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");
        String query = "DROP DATAVERSE DonCDataSchema IF EXISTS;" +
                "CREATE DATAVERSE DonCDataSchema;" +
                "USE DonCDataSchema;" +
                "CREATE TYPE ordersType AS {" +
                "    orderno: int," +
                "    custid: string," +
                "    order_date: string," +
                "    ship_date: string?," +
                "    items: [{" +
                "        itemno: int," +
                "        qty: int," +
                "        price: double" +
                "    }]" +
                "};" +
                "CREATE DATASET orders(ordersType)" +
                "    PRIMARY KEY orderno;";
        myClient.exec(query);

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"-w", "localhost", "19002", "DonCDataSchema", "orders", "json.txt"};
        Client.main(args1);

        String[] args2 = new String[]{"-r", "localhost", "19002", "DonCDataSchema", "orders", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        Assert.assertEquals(sub("USE DonCDataSchema;\n\n" +
                "CREATE OR REPLACE VIEW ordersView AS\n" +
                "\tSELECT orders.orderno, orders.custid, orders.order_date, orders.ship_date\n" +
                "\tFROM orders;\n\n" +
                "CREATE OR REPLACE VIEW _Anon1View AS\n" +
                "\tSELECT orders.orderno, _pos1, _Anon1.itemno, _Anon1.qty, _Anon1.price\n" +
                "\tFROM orders, orders.items _Anon1 AT _pos1;\n\n"), sub(standardOutput));
    }

    @Test
    public void products() throws IOException {

        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");
        String query = "DROP DATAVERSE DonCDataSchema IF EXISTS;" +
                "CREATE DATAVERSE DonCDataSchema;" +
                "USE DonCDataSchema;" +
                "CREATE TYPE productsType AS {" +
                "    itemno: int," +
                "    category: string," +
                "    name: string," +
                "    descrip: string?," +
                "    manuf: string," +
                "    listprice: int" +
                "};" +
                "CREATE DATASET products(productsType)" +
                "    PRIMARY KEY itemno;";
        myClient.exec(query);

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"-w", "localhost", "19002", "DonCDataSchema", "products", "json.txt"};
        Client.main(args1);

        String[] args2 = new String[]{"-r", "localhost", "19002", "DonCDataSchema", "products", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        Assert.assertEquals(sub("USE DonCDataSchema;\n\n" +
                "CREATE OR REPLACE VIEW productsView AS\n" +
                "\tSELECT products.itemno, products.category, products.name, products.descrip, products.manuf, products.listprice\n" +
                "\tFROM products;\n\n"), sub(standardOutput));
    }

    /*
     * Below is a simple test. The Datatype include an integer "id" and a list of integer, "a".
     * We simply need to flatten the list "a", so we only need a view that pairs each (index, a[index]) with "id".
     * Because duplicated names are forbidden, I name the view "_Anon1View", the index "_pos1", and a[index] "_Anon1".
     * */
    @Test
    public void simple() throws IOException {

        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");
        String query = "DROP DATAVERSE ClientTest IF EXISTS;" +
                "CREATE DATAVERSE ClientTest;" +
                "USE ClientTest;" +
                "CREATE TYPE List1Type AS {" +
                "   id: int," +
                "   a: [int]" +
                "};" +
                "CREATE DATASET List1Set (List1Type)" +
                "   PRIMARY KEY id;";
        myClient.exec(query);

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"-w", "localhost", "19002", "ClientTest", "List1Set", "json.txt"};
        Client.main(args1);

        String[] args2 = new String[]{"-r", "localhost", "19002", "ClientTest", "List1Set", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        Assert.assertEquals(sub("USE ClientTest;\n\n" +
                "CREATE OR REPLACE VIEW _Anon1View AS\n" +
                "\tSELECT List1Set.id, _pos1, _Anon1\n" +
                "\tFROM List1Set, List1Set.a _Anon1 AT _pos1;\n\n"), sub(standardOutput));
    }

    /*
     * Here is another simple test: there is no list at all, but a record.
     * Thus, to flatten it, we simply change "stuff {a, b}" to "{stuff.a, stuff.b}"
     * Note that there is no need to rename "stuff.a" or "stuff.b".
     * */
    @Test
    public void noList() throws IOException {

        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");
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
        myClient.exec(query);

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"-w", "localhost", "19002", "ClientTest", "NestedSet", "json.txt"};
        Client.main(args1);

        String[] args2 = new String[]{"-r", "localhost", "19002", "ClientTest", "NestedSet", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        Assert.assertEquals(sub("USE ClientTest;\n\n" +
                "CREATE OR REPLACE VIEW NestedSetView AS\n" +
                "\tSELECT NestedSet.id, NestedSet.stuff.a, NestedSet.stuff.b\n" +
                "\tFROM NestedSet;\n\n"), sub(standardOutput));
    }

    /*
     * Here the case is a bit more complicated. The list "lis2" in the Datatype "ListType" is a list of "NestedType".
     * To flatten it, we need to pair (index, lis2[index].id, lis2[index].stuff.a, lis2[index].stuff.b) with ListType's id.
     * Similarly, I name the index "_pos2", lis2[index] "_Anon2",
     * lis2[index].id "id2" (because it may be potentially used, for example, if "NestedType" has a list),
     * Also of course, we need to create another view for "lis1", which is a list of int.
     */
    @Test
    public void juxtaposedListAndNonAnonymousTypeAndNoPK() throws IOException {

        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");
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
        myClient.exec(query);

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"-w", "localhost", "19002", "ClientTest", "ListSet", "json.txt"};
        Client.main(args1);

        String[] args2 = new String[]{"-r", "localhost", "19002", "ClientTest", "ListSet", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        Assert.assertEquals(sub("USE ClientTest;\n\n" +
                "CREATE OR REPLACE VIEW _Anon1View AS\n" +
                "\tSELECT ListSet.id, _pos1, _Anon1\n" +
                "\tFROM ListSet, ListSet.lis1 _Anon1 AT _pos1;\n\n" +
                "CREATE OR REPLACE VIEW _Anon2View AS\n" +
                "\tSELECT ListSet.id, _pos2, _Anon2.id AS id2, _Anon2.stuff.a, _Anon2.stuff.b\n" +
                "\tFROM ListSet, ListSet.lis2 _Anon2 AT _pos2;\n\n"), sub(standardOutput));
    }

    /*
     * This one is pretty similar to the previous one.
     * The only difference is that the user specified the PK for "lis1" and "lis2" of "ListType".
     * In this case, we no longer need the index to guarantee the uniqueness.
     */
    @Test
    public void juxtaposedListAndNonAnonymousTypeAndPKSpecified() throws IOException {

        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");
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
        myClient.exec(query);

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"-w", "localhost", "19002", "ClientTest", "ListSet", "json.txt"};
        Client.main(args1);

        //Modify the PKs
        modify(9, "\t\t\t\"primaryKey\": [\"int64\"],\n");
        modify(15, "\t\t\t\"primaryKey\": [\"id\"],\n");

        String[] args2 = new String[]{"-r", "localhost", "19002", "ClientTest", "ListSet", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        Assert.assertEquals(sub("USE ClientTest;\n\n" +
                "CREATE OR REPLACE VIEW _Anon1View AS\n" +
                "\tSELECT ListSet.id, _Anon1\n" +
                "\tFROM ListSet, ListSet.lis1 _Anon1 AT _pos1;\n\n" +
                "CREATE OR REPLACE VIEW _Anon2View AS\n" +
                "\tSELECT ListSet.id, _Anon2.id AS id2, _Anon2.stuff.a, _Anon2.stuff.b\n" +
                "\tFROM ListSet, ListSet.lis2 _Anon2 AT _pos2;\n\n"), sub(standardOutput));
    }

    /*
     * In this case, we want to see how we can flatten a super-nested Datatype.
     * Here, we need to specify 5 indexes to refer to that "int" of list "a":
     * a[index1][index2][index3][index4][index5].
     * To make it work, we need to rename index1 to "_pos1", a[index1] to "_Anon1",
     * index2 to "_pos2", a[index1][index2] = _Anon1[index2] to "_Anon2",
     * index3 to "_pos3", a[index1][index2][index3] = _Anon2[index3] to "_Anon3"...
     * Finally, we select the id of the Master Datatype, along with _pos1 ~ _pos5 and _Anon5,
     * which refers to that "int" of list "a".
     */
    @Test
    public void List5AndNoPK() throws IOException {

        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");
        String query = "DROP DATAVERSE ClientTest IF EXISTS;" +
                "CREATE DATAVERSE ClientTest;" +
                "USE ClientTest;" +
                "CREATE TYPE List5Type AS {" +
                "   id: int," +
                "   a: [[[[[int]]]]]" +
                "};" +

                "CREATE DATASET List5Set (List5Type)" +
                "   PRIMARY KEY id;";
        myClient.exec(query);

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"-w", "localhost", "19002", "ClientTest", "List5Set", "json.txt"};
        Client.main(args1);

        String[] args2 = new String[]{"-r", "localhost", "19002", "ClientTest", "List5Set", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        Assert.assertEquals(sub("USE ClientTest;\n\n" +
                "CREATE OR REPLACE VIEW _Anon5View AS\n" +
                "\tSELECT List5Set.id, _pos1, _pos2, _pos3, _pos4, _pos5, _Anon5\n" +
                "\tFROM List5Set, List5Set.a _Anon1 AT _pos1, _Anon1 _Anon2 AT _pos2, _Anon2 _Anon3 AT _pos3, _Anon3 _Anon4 AT _pos4, _Anon4 _Anon5 AT _pos5;\n\n"), sub(standardOutput));
    }

    /*
     * In this case, every flat field is specified as part of PK by the user.
     * Thus, when creating a view for "PKType.c[index]", we also need to include the flat fields in the record "PKType.b".
     */
    @Test
    public void everyFlatFieldIsPKAndListOfListOfRecordAndDupedFieldNames() throws IOException {

        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");
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
        myClient.exec(query);

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"-w", "localhost", "19002", "ClientTest", "PKSet", "json.txt"};
        Client.main(args1);

        //Modify the PKs
        modify(9, "\t\t\t\"primaryKey\": [\"int64\"],\n");
        modify(20, "\t\t\t\t\t\"primaryKey\": [\"a\", \"b\"],\n");

        String[] args2 = new String[]{"-r", "localhost", "19002", "ClientTest", "PKSet", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        Assert.assertEquals(sub("USE ClientTest;\n\n" +
                "CREATE OR REPLACE VIEW _Anon1View AS\n" +
                "\tSELECT PKSet.id, PKSet.a, PKSet.b.a AS a2, PKSet.b.b, _Anon1\n" +
                "\tFROM PKSet, PKSet.b.c _Anon1 AT _pos1;\n\n" +
                "CREATE OR REPLACE VIEW _Anon3View AS\n" +
                "\tSELECT PKSet.id, PKSet.a, PKSet.b.a AS a2, PKSet.b.b, _pos2, _Anon3.a AS a3, _Anon3.b AS b2\n" +
                "\tFROM PKSet, PKSet.c _Anon2 AT _pos2, _Anon2 _Anon3 AT _pos3;\n\n"), sub(standardOutput));
    }

    /*
     * This is to test the performance and correctness of the software when the Datatype is super-super-nested.
     */
    private void superNestedInit() throws IOException {
        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");
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
        myClient.exec(query);

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        String[] args1 = new String[]{"-w", "localhost", "19002", "ClientTest", "SuperNestedSet", "json.txt"};
        Client.main(args1);


        //Modify the PKs
        modify(14, "\t\t\t\t\t\"primaryKey\": [\"a\"],\n");
        modify(22, "\t\t\t\"primaryKey\": [\"b\"],\n");

        String[] args2 = new String[]{"-r", "localhost", "19002", "ClientTest", "SuperNestedSet", "json.txt"};
        Client.main(args2);

        final String standardOutput = myOut.toString();
        String substandardOutput = sub(standardOutput);
        myClient.exec(substandardOutput);

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
        myClient.exec(insertion);
    }

    @Test
    public void superNested0() throws IOException {
        superNestedInit();

        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");

        String result = myClient.getResult("USE ClientTest; SELECT * FROM SuperNestedSetView;");
        Assert.assertEquals(sub("[{\"SuperNestedSetView\":{\"id\":1,\"a\":1,\"a2\":1,\"a3\":1,\"a4\":1,\"d\":1,\"e\":1,\"f\":1,\"h\":1,\"i\":1,\"c\":1}},{\"SuperNestedSetView\":{\"id\":2,\"a\":2,\"a2\":4,\"a3\":4,\"a4\":4,\"d\":4,\"e\":4,\"f\":4,\"h\":4,\"i\":4,\"c\":2}}]"), sub(result));
    }

    @Test
    public void superNested1() throws IOException {
        superNestedInit();

        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");

        String result = myClient.getResult("USE ClientTest; SELECT * FROM _Anon2View;");
        Assert.assertEquals(sub("[{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":1,\"a4\":1}},{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":1,\"a4\":2}},{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":1,\"a4\":3}},{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":2,\"a4\":2}},{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":2,\"a4\":3}},{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":2,\"a4\":4}},{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":3,\"a4\":3}},{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":3,\"a4\":4}},{\"_Anon2View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos1\":3,\"a4\":5}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":1,\"a4\":4}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":1,\"a4\":5}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":1,\"a4\":6}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":2,\"a4\":5}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":2,\"a4\":6}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":2,\"a4\":7}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":3,\"a4\":6}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":3,\"a4\":7}},{\"_Anon2View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos1\":3,\"a4\":8}}]"), sub(result));
    }

    @Test
    public void superNested2() throws IOException {
        superNestedInit();

        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");

        String result = myClient.getResult("USE ClientTest; SELECT * FROM _Anon4View;");
        Assert.assertEquals(sub("[{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":1,\"_pos4\":1,\"_Anon4\":4}},{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":1,\"_pos4\":2,\"_Anon4\":5}},{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":1,\"_pos4\":3,\"_Anon4\":6}},{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":2,\"_pos4\":1,\"_Anon4\":5}},{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":2,\"_pos4\":2,\"_Anon4\":6}},{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":2,\"_pos4\":3,\"_Anon4\":7}},{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":3,\"_pos4\":1,\"_Anon4\":6}},{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":3,\"_pos4\":2,\"_Anon4\":7}},{\"_Anon4View\":{\"a\":1,\"a2\":1,\"a3\":1,\"b\":3,\"_pos4\":3,\"_Anon4\":8}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":2,\"_pos4\":1,\"_Anon4\":7}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":2,\"_pos4\":2,\"_Anon4\":8}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":2,\"_pos4\":3,\"_Anon4\":9}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":3,\"_pos4\":1,\"_Anon4\":8}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":3,\"_pos4\":2,\"_Anon4\":9}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":3,\"_pos4\":3,\"_Anon4\":10}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":4,\"_pos4\":1,\"_Anon4\":9}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":4,\"_pos4\":2,\"_Anon4\":10}},{\"_Anon4View\":{\"a\":2,\"a2\":4,\"a3\":4,\"b\":4,\"_pos4\":3,\"_Anon4\":11}}]"), sub(result));
    }

    @Test
    public void superNested3() throws IOException {
        superNestedInit();

        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");

        String result = myClient.getResult("USE ClientTest; SELECT * FROM _Anon7View;");
        Assert.assertEquals(sub("[{\"_Anon7View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos5\":1,\"_pos6\":1,\"_pos7\":1,\"_Anon7\":1}},{\"_Anon7View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos5\":1,\"_pos6\":1,\"_pos7\":2,\"_Anon7\":2}},{\"_Anon7View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos5\":1,\"_pos6\":2,\"_pos7\":1,\"_Anon7\":2}},{\"_Anon7View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos5\":1,\"_pos6\":2,\"_pos7\":2,\"_Anon7\":3}},{\"_Anon7View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos5\":2,\"_pos6\":1,\"_pos7\":1,\"_Anon7\":3}},{\"_Anon7View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos5\":2,\"_pos6\":1,\"_pos7\":2,\"_Anon7\":4}},{\"_Anon7View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos5\":2,\"_pos6\":2,\"_pos7\":1,\"_Anon7\":4}},{\"_Anon7View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos5\":2,\"_pos6\":2,\"_pos7\":2,\"_Anon7\":5}},{\"_Anon7View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos5\":1,\"_pos6\":1,\"_pos7\":1,\"_Anon7\":6}},{\"_Anon7View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos5\":1,\"_pos6\":1,\"_pos7\":2,\"_Anon7\":7}},{\"_Anon7View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos5\":1,\"_pos6\":2,\"_pos7\":1,\"_Anon7\":7}},{\"_Anon7View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos5\":1,\"_pos6\":2,\"_pos7\":2,\"_Anon7\":8}},{\"_Anon7View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos5\":2,\"_pos6\":1,\"_pos7\":1,\"_Anon7\":8}},{\"_Anon7View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos5\":2,\"_pos6\":1,\"_pos7\":2,\"_Anon7\":9}},{\"_Anon7View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos5\":2,\"_pos6\":2,\"_pos7\":1,\"_Anon7\":9}},{\"_Anon7View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos5\":2,\"_pos6\":2,\"_pos7\":2,\"_Anon7\":10}}]"), sub(result));
    }

    @Test
    public void superNested4() throws IOException {
        superNestedInit();

        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");

        String result = myClient.getResult("USE ClientTest; SELECT * FROM _Anon8View;");
        Assert.assertEquals(sub("[{\"_Anon8View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos8\":1,\"_Anon8\":1}},{\"_Anon8View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos8\":2,\"_Anon8\":2}},{\"_Anon8View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos8\":3,\"_Anon8\":3}},{\"_Anon8View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos8\":1,\"_Anon8\":4}},{\"_Anon8View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos8\":2,\"_Anon8\":5}},{\"_Anon8View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos8\":3,\"_Anon8\":6}}]"), sub(result));
    }

    @Test
    public void superNested5() throws IOException {
        superNestedInit();

        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");

        String result = myClient.getResult("USE ClientTest; SELECT * FROM _Anon9View;");
        Assert.assertEquals(sub("[{\"_Anon9View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos9\":1,\"_Anon9\":1}},{\"_Anon9View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos9\":2,\"_Anon9\":2}},{\"_Anon9View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos9\":3,\"_Anon9\":3}},{\"_Anon9View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos9\":1,\"_Anon9\":4}},{\"_Anon9View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos9\":2,\"_Anon9\":5}},{\"_Anon9View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos9\":3,\"_Anon9\":6}}]"), sub(result));
    }

    @Test
    public void superNested6() throws IOException {
        superNestedInit();

        HttpAPIClient myClient = new HttpAPIClient("localhost", "19002");

        String result = myClient.getResult("USE ClientTest; SELECT * FROM _Anon10View;");
        Assert.assertEquals(sub("[{\"_Anon10View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos10\":1,\"_Anon10\":1}},{\"_Anon10View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos10\":2,\"_Anon10\":2}},{\"_Anon10View\":{\"a\":1,\"a2\":1,\"a3\":1,\"_pos10\":3,\"_Anon10\":3}},{\"_Anon10View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos10\":1,\"_Anon10\":7}},{\"_Anon10View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos10\":2,\"_Anon10\":8}},{\"_Anon10View\":{\"a\":2,\"a2\":4,\"a3\":4,\"_pos10\":3,\"_Anon10\":9}}]"), sub(result));
    }
}