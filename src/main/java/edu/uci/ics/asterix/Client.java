/*
Author:
    Yunfan Qiao

With the help of:
    Wail Alkowaileet,
    Ian Maxon,
    Prof. Mike Carey
*/

package edu.uci.ics.asterix;

import com.fasterxml.jackson.core.type.TypeReference;
import edu.uci.ics.asterix.api.HttpAPIClient;
import edu.uci.ics.asterix.result.HashObject;
import edu.uci.ics.asterix.result.ResultObject;
import edu.uci.ics.asterix.result.Tuple;
import edu.uci.ics.asterix.result.metadata.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static edu.uci.ics.asterix.AsterixUtil.OBJECT_MAPPER;

public class Client {
    private static final String DATATYPE_QUERY = "SELECT VALUE x FROM Metadata.`Datatype` x;";
    private static final String DATASET_QUERY = "SELECT VALUE x FROM Metadata.`Dataset` x;";
    private static final String FORMAT = "server port dataverseName datasetName (-r)|(-w) fileName"; // Currently, cannot omit datasetName

    public static void main(String[] args) throws IOException {

        // -----------------------------Init---------------------------------

        if (args.length == 0 || args.length > 6) {
            System.out.println("Invalid number of arguments!");
            return;
        }

        // "-h" means that the user wants to know the format of the arguments.
        if (args[0].equals("-h")) {
            System.out.println(FORMAT);
            return;
        }

        HttpAPIClient myClient = new HttpAPIClient(args[0], args[1]);
        ResultObject<Datatype> datatypeObject = myClient.getDatatypes(DATATYPE_QUERY);
        ResultObject<Dataset> datasetObject = myClient.getDatasets(DATASET_QUERY);

        String dataverseName = args[2];

        //Hash the Datasets and Datatypes
        for (Datatype datatype : datatypeObject.getResults())
            if (dataverseName.equals(datatype.getDataverseName())) {
                Tuple t = new Tuple(dataverseName, datatype.getDatatypeName());
                // Create a map with DatatypeName as key (we don't need to care about DatasetName because it won't change)
                HashObject.setResult(t, datatype);

                // We don't want to print the same Datatype twice, so we want to know if this Datatype is already found and printed.
                HashObject.setFound(t, false);
            }
        for (Dataset dataset : datasetObject.getResults())
            if (dataverseName.equals(dataset.getDataverseName())) {
                Tuple t = new Tuple(dataverseName, dataset.getDatasetName());
                HashObject.setSettotype(t, dataset.getDatatypeName());
            }

        String datasetName = null;
        int start = 3;
        if (args[3].charAt(0) != '-') {
            datasetName = args[3];
            start++;
        }
        String fileName = null;
        boolean rFlag = false;
        boolean wFlag = false;

        try {
            for (int i = start; i < args.length; i++)
                if (args[i].charAt(0) == '-') {
                    if (args[i].equals("-r")) {
                        if (rFlag || wFlag) throw new Exception();
                        rFlag = true;
                        fileName = args[++i];

                    } else if (args[i].equals("-w")) {
                        if (rFlag || wFlag) throw new Exception();
                        wFlag = true;
                        fileName = args[++i];

                    } else {
                        System.out.println("Command not found!");
                        return;
                    }

                    // the commands must start with flags
                } else throw new Exception();
        } catch (Exception e) {
            System.out.println("Invalid arguments!");
            return;
        }

        // Check if Dataverse exists
        boolean bo = false;
        for (Dataset dataset : datasetObject.getResults())
            if (dataverseName.equals(dataset.getDataverseName())) {
                bo = true;
                break;
            }
        if (!bo) {
            System.out.println("Dataverse not found!");
            return;
        }

        // Check if Dataset exists
        String datatypeName = null;
        for (Dataset dataset : datasetObject.getResults())
            if (dataverseName.equals(dataset.getDataverseName()) && dataset.getDatasetName().equals(datasetName))
                datatypeName = dataset.getDatatypeName();
        if (datatypeName == null) {
            System.out.println("Dataset not found!");
            return;
        }

        // -----------------------------Deal with Flags---------------------------------

        if (wFlag) printJSON(dataverseName, datasetName, datatypeName, fileName, datasetObject);

        if (rFlag) {
            Nested nestedObject = readJSON(fileName);
            if (nestedObject == null)
                return;
            try {
                // Will need to check if the PK fields exist here.
                System.out.println("USE " + dataverseName + ";\n");
                List<String> from = new ArrayList<>();
                from.add(datasetName);
                createView(dataverseName, datatypeName, datasetName, nestedObject, new ArrayList<>(), from, 0);
            } catch (Exception e) {
                System.out.println("Error with JSON file!");
                return;
            }
        }

    }

    // Currently, this software does not support flattening every Dataset in a Dataverse. It can only do one at a time.
    private static void printJSON(String dataverseName, String datasetName, String datatypeName, String fileName, ResultObject<Dataset> datasetObject) {

        try {
            File myFile = new File(fileName);
            myFile.createNewFile();
            FileWriter myWriter = new FileWriter(myFile, false);

            // Format of the JSON file: for each dataset, specify its name, the Datatype it is based on, its PK, and
            myWriter.write("{\n");
            myWriter.write("\t\"Name\": \"" + datasetName + "\",\n");
            myWriter.write("\t\"Type\": \"" + datatypeName + "\",\n");
            myWriter.write("\t\"Primary_Key\": [");

            // Get the PrimaryKey of the Dataset we want
            for (Dataset dataset : datasetObject.getResults())
                if (dataverseName.equals(dataset.getDataverseName()) && datasetName.equals(dataset.getDatasetName())) {
                    myWriter.write("\"");
                    boolean comma = false;
                    for (List<String> i : dataset.getInternalDetails().getPrimaryKey()) {
                        if (comma) myWriter.write("\", \"");
                        else comma = true;
                        boolean period = false;
                        for (String j : i) {
                            if (period) myWriter.write(".");
                            else period = true;
                            myWriter.write(j);
                        }
                    }
                    myWriter.write("\"");
                }

            myWriter.write("],\n");
            myWriter.write("\t\"NestedFields\": [");
            if (hasList(dataverseName, datatypeName)) {
                myWriter.write("\n");

                Tuple myTuple = new Tuple(dataverseName, datatypeName);
                Derived derived = HashObject.getResult(myTuple).getDerived();
                List<Fields> fields = derived.getRecord().getFields();
                boolean first_comma = true;

                for (Fields field : fields) {
                    String FieldType = field.getFieldType();
                    String FieldName = field.getFieldName();

                    if (!hasList(dataverseName, FieldType)) continue;
                    printJSONhelper(dataverseName, field.getFieldType(), field.getFieldName(), "\t", myWriter, "", 1, first_comma);
                    first_comma = false;
                }

                myWriter.write("\n\t");
            }
            myWriter.write("]\n");
            myWriter.write("}");
            myWriter.close();

        } catch (Exception e) {
            System.out.println("Error with file operations!");
            return;
        }
    }

    private static Nested readJSON(String fileName) {
        // Read JSON back

        // Let's have a convention here: if the "Primary_Key" field is empty, it means that it's just a list of a flat type.

        String JSONString = "";
        Nested nestedObject = null;

        try {
            File JSONFile = new File(fileName);
            Scanner reader = new Scanner(JSONFile);
            while (reader.hasNextLine())
                JSONString = JSONString.concat(reader.nextLine());

            nestedObject = OBJECT_MAPPER.readValue(JSONString, new TypeReference<>() {
            });

        } catch (Exception e) {
            System.out.println("Error with file operations!");
            return null;
        }
        return nestedObject;
    }

    private static int createView(String DataverseName, String DatatypeName, String prefix, Nested NestedObject, List<String> PrimaryKey, List<String> From, int PosNum) {
        // This returns the current PosNum: If a view has an alias (e.g. _Anon1), the other view cannot have the same alias.

        int addedPK = 0;
        //int addedFrom = 0;
        Map<String, Integer> rename = new HashMap<>();

        // The Primary Keys after renaming
        List<String> newPrimaryKey = new ArrayList<>();

        // Init the rename... SELECT id, xxx.id AS id2, yyy.id AS id3
        for (String s : PrimaryKey) {
            int pos = s.lastIndexOf('.');
            String temp = s;
            if (pos != -1)
                temp = s.substring(pos + 1);
            if (rename.containsKey(temp)) {
                rename.put(temp, rename.get(temp) + 1);
                newPrimaryKey.add(s + " AS " + temp + rename.get(temp));
            } else {
                rename.put(temp, 1);
                newPrimaryKey.add(s);
            }
        }

        boolean addedPosNum = false;

        // If the user did not enter any fields, then the position will be the PK
        if (NestedObject.getPrimaryKey().size() == 0) {
            PrimaryKey.add("_pos" + PosNum);
            newPrimaryKey.add("_pos" + PosNum);
            addedPK++;
        }

        // Check: if the number of Primary Keys defined by the user is equal to the total number of flat fields (meaning they are all part of PK)
        // and it does have NestedFields, then this current VIEW should not be printed.

        // I simplified ViewName here...
        if (!(NestedObject.getPrimaryKey().size() == countFlat(DataverseName, DatatypeName) && NestedObject.getNested().size() != 0)) {
            System.out.println("CREATE OR REPLACE VIEW " + prefix.replaceAll("\\.", "_") + "View AS");
            System.out.print("\tSELECT ");

            boolean comma = false;
            // Print all PK
            for (String s : newPrimaryKey)
                comma = printComma(comma, s);

            if (isFlattenedType(DatatypeName))
                comma = printComma(comma, prefix);

            // Print all flat fields, after flattening those {}
            if (getTag(DataverseName, DatatypeName).equals("RECORD"))
                comma = findFlat(DataverseName, DatatypeName, prefix, comma, rename);
            else if (!getTag(DataverseName, DatatypeName).equals("FLAT"))
                comma = findFlat(DataverseName, getListType(DataverseName, DatatypeName), prefix, comma, rename);
            System.out.println();

            // Print "FROM"
            System.out.print("\tFROM ");
            boolean comma2 = false;
            for (String s : From)
                comma2 = printComma(comma2, s);
            System.out.println(";\n");
        }

        // Append the current PK to the PK list
        for (String s : NestedObject.getPrimaryKey()) {
            String temp = s;
            if (!prefix.equals("")) {
                String temp2 = s;
                if (rename.containsKey(s)) {
                    rename.replace(s, rename.get(s) + 1);
                    temp2 += rename.get(s);
                } else
                    rename.put(s, 1);
                temp = prefix + "." + temp2;
            }
            PrimaryKey.add(temp);
            addedPK++;
        }

        // Create views for all List fields
        for (Nested i : NestedObject.getNested()) {
            if (getTag(DataverseName, DatatypeName).equals("RECORD")) {
                //Should I care about duped names here? Probably not
                From.add(prefix + "." + fixPrefix(i.getName()) + " _Anon" + (PosNum + 1) + " AT " + "_pos" + (PosNum + 1));
            } else
                From.add(prefix + " _Anon" + (PosNum + 1) + " AT " + "_pos" + (PosNum + 1));
            PosNum = createView(DataverseName, i.getType(), "_Anon" + (PosNum + 1), i, PrimaryKey, From, PosNum + 1);
            From.remove(From.size() - 1);
        }

        for (int i = 0; i < addedPK; i++)
            PrimaryKey.remove(PrimaryKey.size() - 1);

        return PosNum;
    }

    private static String fixPrefix(String s) {
        return s.replaceAll("_InnerList.*\\.", "");
    }

    // Count the number of flat fields of the current Datatype
    private static int countFlat(String DataverseName, String DatatypeName) {
        if (isFlattenedType(DatatypeName))
            return 1;

        Tuple myTuple = new Tuple(DataverseName, DatatypeName);
        Derived derived = HashObject.getResult(myTuple).getDerived();
        if (!derived.getTag().equals("RECORD"))
            return 0;

        List<Fields> fields = derived.getRecord().getFields();

        int total = 0;
        for (Fields field : fields) {
            String FieldType = field.getFieldType();

            if (isFlattenedType(FieldType)) {
                total++;
                continue;
            }

            Tuple t = new Tuple(DataverseName, FieldType);
            Derived derived2 = HashObject.getResult(t).getDerived();
            if (derived2.getTag().equals("RECORD"))
                total += countFlat(DataverseName, FieldType);
        }
        return total;
    }

    // Print all flat fields for the SELECT statement
    private static boolean findFlat(String DataverseName, String DatatypeName, String prefix, boolean comma, Map<String, Integer> rename) {

        // [int] ???
        if (isFlattenedType(DatatypeName))
            return printComma(comma, prefix + "." + DatatypeName);

        Tuple myTuple = new Tuple(DataverseName, DatatypeName);
        Derived derived = HashObject.getResult(myTuple).getDerived();

        // If it's a record there's no flat fields.
        if (!derived.getTag().equals("RECORD"))
            //System.out.print("This should not happen!");
            return comma;

        List<Fields> fields = derived.getRecord().getFields();

        for (Fields field : fields) {
            String FieldName = field.getFieldName();
            String FieldType = field.getFieldType();
            if (isFlattenedType(FieldType)) {
                String temp = FieldName;
                if (rename.containsKey(FieldName)) {
                    rename.put(FieldName, rename.get(FieldName) + 1);
                    temp += " AS " + FieldName + rename.get(FieldName);
                } else
                    rename.put(FieldName, 1);

                comma = printComma(comma, prefix + "." + temp);
                continue;
            }

            if (!getTag(DataverseName, FieldType).equals("RECORD")) continue;

            comma = findFlat(DataverseName, FieldType, prefix + "." + FieldName, comma, rename);
        }
        return comma;
    }

    private static boolean isFlattenedType(String s) {
        return s.equals("binary") || s.equals("boolean") || s.equals("circle") || s.equals("date") || s.equals("datetime") || s.equals("day-time-duration") ||
                s.equals("double") || s.equals("duration") || s.equals("float") || s.equals("geometry") || s.equals("int16") || s.equals("int32") ||
                s.equals("int64") || s.equals("int8") || s.equals("interval") || s.equals("line") || s.equals("missing") || s.equals("null") ||
                s.equals("point") || s.equals("point3d") || s.equals("polygon") || s.equals("rectangle") || s.equals("shortwithouttypeinfo") || s.equals("string") ||
                s.equals("time") || s.equals("uuid") || s.equals("year-month-duration");
    }

    // Tag of a Datatype: either "flat", RECORD, ORDEREDLIST, or UNORDEREDLIST
    private static String getTag(String DataverseName, String DatatypeName) {
        if (isFlattenedType(DatatypeName))
            return "FLAT";
        Tuple t = new Tuple(DataverseName, DatatypeName);
        Derived derived = HashObject.getResult(t).getDerived();
        return derived.getTag();
    }

    // Get the type inside an ORDEREDLIST or UNORDEREDLIST
    private static String getListType(String DataverseName, String DatatypeName) {
        Tuple t = new Tuple(DataverseName, DatatypeName);
        Derived derived = HashObject.getResult(t).getDerived();
        if (derived.getTag().equals("ORDEREDLIST"))
            return derived.getOrderedList();
        else
            return derived.getUnorderedList();
    }

    // I hate comma
    private static boolean printComma(boolean comma, String s) {
        if (!comma)
            comma = true;
        else
            System.out.print(", ");
        System.out.print(s);
        return comma;
    }

    // If there's a list inside a Datatype. If not then it's a pretty flat Datatype (the RECORD is flat in my program)
    private static boolean hasList(String DataverseName, String DatatypeName) {
        if (isFlattenedType(DatatypeName))
            return false;

        Tuple myTuple = new Tuple(DataverseName, DatatypeName);
        Derived derived = HashObject.getResult(myTuple).getDerived();
        if (!derived.getTag().equals("RECORD"))
            return true;

        List<Fields> fields = derived.getRecord().getFields();

        for (Fields field : fields) {
            String FieldType = field.getFieldType();

            // We only care about list here
            if (isFlattenedType(FieldType)) continue;
            Tuple t = new Tuple(DataverseName, FieldType);
            Derived derived2 = HashObject.getResult(t).getDerived();
            if (!derived2.getTag().equals("RECORD"))
                return true;

            if (hasList(DataverseName, FieldType))
                return true;
        }
        return false;
    }


    private static int printJSONhelper(String DataverseName, String DatatypeName, String currentName, String indentation, FileWriter myWriter, String prefix, int ListNum, boolean first_comma) {
        /* Return value:
            0: good;
            -1: error
         */

        if (isFlattenedType(DatatypeName))
            return 0;
        try {
            Tuple myTuple = new Tuple(DataverseName, DatatypeName);
            Derived derived = HashObject.getResult(myTuple).getDerived();

            // Datatype is Record
            if (derived.getTag().equals("RECORD")) {
                List<Fields> fields = derived.getRecord().getFields();

                boolean bo = hasList(DataverseName, DatatypeName); // If the "Nested" field is not empty, then we want stuff like "[\n]" instead of "[]"

                if (!bo) {
                    //System.out.println("YES");
                    return 0;
                }

                for (Fields field : fields) {
                    String FieldName = field.getFieldName();
                    String FieldType = field.getFieldType();

                    if (isFlattenedType(FieldType)) continue;
                    String prefixTemp = currentName;
                    if (!prefix.equals("")) prefixTemp = prefix + "." + prefixTemp;
                    int temp = printJSONhelper(DataverseName, FieldType, FieldName, indentation, myWriter, prefixTemp, ListNum, first_comma);
                    if (temp == -1)
                        return -1;
                    first_comma = false;
                }
            }

            // Datatype is List
            else {
                if (!first_comma)
                    myWriter.write(",\n");

                String ListType;
                if (derived.getTag().equals("ORDEREDLIST"))
                    ListType = derived.getOrderedList();
                else
                    ListType = derived.getUnorderedList();

                myWriter.write(indentation + "\t{\n");
                if (prefix.equals(""))
                    myWriter.write(indentation + "\t\t\"Name\": \"" + currentName + "\",\n");
                else
                    myWriter.write(indentation + "\t\t\"Name\": \"" + prefix + "." + currentName + "\",\n");
                myWriter.write(indentation + "\t\t\"Type\": \"" + ListType + "\",\n");
                myWriter.write(indentation + "\t\t\"Primary_Key\": [],\n");
                myWriter.write(indentation + "\t\t\"NestedFields\": [");

                if (hasList(DataverseName, ListType)) {
                    myWriter.write("\n");
                    printJSONhelper(DataverseName, ListType, "_InnerList" + ListNum, indentation + "\t\t", myWriter, "", ListNum + 1, true);
                    myWriter.write("\n" + indentation + "\t\t");
                }

                myWriter.write("]\n");
                myWriter.write(indentation + "\t}");

            }

            return 0;


        } catch (Exception e) {
            System.out.println("Error with file operations!");
            return -1;
        }

    }

}