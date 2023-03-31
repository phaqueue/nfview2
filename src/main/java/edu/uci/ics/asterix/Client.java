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

        // Deal with datasetName
        String datasetName = null;
        int start = 3;
        if (args[3].charAt(0) != '-') {
            datasetName = args[3];
            start++;
        }

        String fileName = null;
        boolean rFlag = false;
        boolean wFlag = false;

        // Deal with the flag and fileName
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
        boolean found = false;
        for (Dataset dataset : datasetObject.getResults())
            if (dataverseName.equals(dataset.getDataverseName())) {
                found = true;
                break;
            }
        if (!found) {
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

        // if it's "-w", then we print a JSON for the user and allow them to specify PK.
        if (wFlag) printJSON(dataverseName, datasetName, datatypeName, fileName, datasetObject);

        // if it's "-r", then we take the specified PK into consideration, and create the flatten views.
        if (rFlag) {
            Nested nestedObject = readJSON(fileName);
            if (nestedObject == null)
                return;
            try {
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

    /*
     * Currently, this software does not support flattening every Dataset in a Dataverse. It can only do one at a time.
     *
     * This function prints a JSON for the user to specify the primary key for each nested field.
     * If the primary key is empty, then the index will become the primary key.
     */

    private static void printJSON(String dataverseName, String datasetName, String datatypeName, String fileName, ResultObject<Dataset> datasetObject) {

        try {
            File myFile = new File(fileName);
            myFile.createNewFile();
            FileWriter myWriter = new FileWriter(myFile, false);

            // Format of the JSON file: for each dataset, specify its name, the Datatype it is based on, its PK, and the nested fields.
            myWriter.write("{\n");
            myWriter.write("\t\"name\": \"" + datasetName + "\",\n");
            myWriter.write("\t\"type\": \"" + datatypeName + "\",\n");
            myWriter.write("\t\"primaryKey\": [");

            // Get the PrimaryKey of the Dataset we want
            // It is a pain to deal with the commas and periods.
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

            // Deal with nestedFields
            myWriter.write("],\n");
            myWriter.write("\t\"nestedFields\": [");
            if (hasList(dataverseName, datatypeName)) {
                myWriter.write("\n");

                Tuple myTuple = new Tuple(dataverseName, datatypeName);
                Derived derived = HashObject.getResult(myTuple).getDerived();
                List<Fields> fields = derived.getRecord().getFields();
                boolean firstComma = true;

                for (Fields field : fields) {
                    String fieldType = field.getFieldType();

                    // if the fieldType is not a list, then we can skip.
                    if (!hasList(dataverseName, fieldType)) continue;

                    // Otherwise, print the {name, type, primaryKey, and nestedFields} of this nested field.
                    printJSONhelper(dataverseName, field.getFieldType(), field.getFieldName(), "\t", myWriter, "", 1, firstComma);
                    firstComma = false;
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
        // Read JSON back and return a nested Object that represents the JSON file.

        // Let's have a convention here: if the "primaryKey" field is empty, it means that it's just a list of a flat type.

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

    /*
     * The main goal is to identify all nested fields (and also the nested fields inside the nested fields and so on)
     * and create views for them by recursively calling this function.
     * The rest information (SELECT xxx FROM yyy, PK, ...) will be printed for the current view.
     *
     * This function also returns the current PosNum for convenience: If a view has an alias (e.g. _Anon1), the other view cannot have the same alias.
     */
    private static int createView(String dataverseName, String datatypeName, String prefix, Nested nestedObject, List<String> primaryKey, List<String> from, int posNum) {

        int addedPK = 0;
        Map<String, Integer> rename = new HashMap<>();

        // The Primary Keys after renaming
        List<String> newPrimaryKey = new ArrayList<>();

        // Init the rename... SELECT id, xxx.id AS id2, yyy.id AS id3
        // It's syntactical wrong if you don't rename them.
        for (String s : primaryKey) {
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

        // If the user did not enter any fields, then the position will be the PK
        if (nestedObject.getPrimaryKey().size() == 0) {
            primaryKey.add("_pos" + posNum);
            newPrimaryKey.add("_pos" + posNum);
            addedPK++;
        }

        // Check: if the number of Primary Keys defined by the user is equal to the total number of flat fields (meaning they are all part of PK)
        // and it does have nestedFields, then this current VIEW should not be printed.

        // I simplified ViewName here...
        if (!(nestedObject.getPrimaryKey().size() == countFlat(dataverseName, datatypeName) && nestedObject.getNested().size() != 0)) {
            System.out.println("CREATE OR REPLACE VIEW " + prefix.replaceAll("\\.", "_") + "View AS");
            System.out.print("\tSELECT ");

            boolean comma = false;
            // Print all PK
            for (String s : newPrimaryKey)
                comma = printComma(comma, s);

            if (isFlattenedType(datatypeName))
                comma = printComma(comma, prefix);

            // Print all flat fields, after flattening those {}
            if (getTag(dataverseName, datatypeName).equals("RECORD"))
                findFlat(dataverseName, datatypeName, prefix, comma, rename);
            else if (!getTag(dataverseName, datatypeName).equals("FLAT"))
                findFlat(dataverseName, getListType(dataverseName, datatypeName), prefix, comma, rename);
            System.out.println();

            // Print "FROM"
            System.out.print("\tFROM ");
            comma = false;
            for (String s : from)
                comma = printComma(comma, s);
            System.out.println(";\n");
        }

        // Append the current PK to the PK list.
        // Note that there will be more and more fields in PK if you recurse more.
        for (String s : nestedObject.getPrimaryKey()) {
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
            primaryKey.add(temp);
            addedPK++;
        }

        // Create views for all List fields
        for (Nested i : nestedObject.getNested()) {
            if (getTag(dataverseName, datatypeName).equals("RECORD")) {
                //Should I care about duped names here? Probably not
                from.add(prefix + "." + fixPrefix(i.getName()) + " _Anon" + (posNum + 1) + " AT " + "_pos" + (posNum + 1));
            } else
                from.add(prefix + " _Anon" + (posNum + 1) + " AT " + "_pos" + (posNum + 1));
            posNum = createView(dataverseName, i.getType(), "_Anon" + (posNum + 1), i, primaryKey, from, posNum + 1);
            from.remove(from.size() - 1);
        }

        for (int i = 0; i < addedPK; i++)
            primaryKey.remove(primaryKey.size() - 1);

        return posNum;
    }

    private static String fixPrefix(String s) {
        return s.replaceAll("_InnerList.*\\.", "");
    }

    // Count the number of flat fields of the current Datatype
    private static int countFlat(String dataverseName, String datatypeName) {
        if (isFlattenedType(datatypeName))
            return 1;

        Tuple myTuple = new Tuple(dataverseName, datatypeName);
        Derived derived = HashObject.getResult(myTuple).getDerived();
        if (!derived.getTag().equals("RECORD"))
            return 0;

        List<Fields> fields = derived.getRecord().getFields();

        int total = 0;
        for (Fields field : fields) {
            String fieldType = field.getFieldType();

            if (isFlattenedType(fieldType)) {
                total++;
                continue;
            }

            Tuple t = new Tuple(dataverseName, fieldType);
            Derived derived2 = HashObject.getResult(t).getDerived();
            if (derived2.getTag().equals("RECORD"))
                total += countFlat(dataverseName, fieldType);
        }
        return total;
    }

    // Print all flat fields for the SELECT statement
    private static boolean findFlat(String dataverseName, String datatypeName, String prefix, boolean comma, Map<String, Integer> rename) {

        // [int] ???
        if (isFlattenedType(datatypeName))
            return printComma(comma, prefix + "." + datatypeName);

        Tuple myTuple = new Tuple(dataverseName, datatypeName);
        Derived derived = HashObject.getResult(myTuple).getDerived();

        // If it's a record there's no flat fields.
        if (!derived.getTag().equals("RECORD"))
            //System.out.print("This should not happen!");
            return comma;

        List<Fields> fields = derived.getRecord().getFields();

        for (Fields field : fields) {
            String fieldName = field.getFieldName();
            String fieldType = field.getFieldType();
            if (isFlattenedType(fieldType)) {
                String temp = fieldName;

                // If you are still confused about the purpose of rename, please check my comment for its initialization.
                if (rename.containsKey(fieldName)) {
                    rename.put(fieldName, rename.get(fieldName) + 1);
                    temp += " AS " + fieldName + rename.get(fieldName);
                } else
                    rename.put(fieldName, 1);

                comma = printComma(comma, prefix + "." + temp);
                continue;
            }

            if (!getTag(dataverseName, fieldType).equals("RECORD")) continue;

            comma = findFlat(dataverseName, fieldType, prefix + "." + fieldName, comma, rename);
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
    private static String getTag(String dataverseName, String datatypeName) {
        if (isFlattenedType(datatypeName))
            return "FLAT";
        Tuple t = new Tuple(dataverseName, datatypeName);
        Derived derived = HashObject.getResult(t).getDerived();
        return derived.getTag();
    }

    // Get the type inside an ORDEREDLIST or UNORDEREDLIST
    private static String getListType(String dataverseName, String datatypeName) {
        Tuple t = new Tuple(dataverseName, datatypeName);
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
    private static boolean hasList(String dataverseName, String datatypeName) {
        if (isFlattenedType(datatypeName))
            return false;

        Tuple myTuple = new Tuple(dataverseName, datatypeName);
        Derived derived = HashObject.getResult(myTuple).getDerived();
        if (!derived.getTag().equals("RECORD"))
            return true;

        List<Fields> fields = derived.getRecord().getFields();

        for (Fields field : fields) {
            String fieldType = field.getFieldType();

            // We only care about list here
            if (isFlattenedType(fieldType)) continue;
            Tuple t = new Tuple(dataverseName, fieldType);
            Derived derived2 = HashObject.getResult(t).getDerived();
            if (!derived2.getTag().equals("RECORD"))
                return true;

            if (hasList(dataverseName, fieldType))
                return true;
        }
        return false;
    }

    /*
     * This is the helper function of printJSON.
     * It may seem a bit redundant, but the logic of constructing layer 0 and layer 1~n of the JSON file are still too different,
     * so I created this helper function that handles the creation of layer 1~n.
     * The main idea is to print name. type, and primaryKey for the current layer "i" first, and then find all nested fields,
     * create a new layer "i+1" and do the same to every nested field by recursively calling this helper function,
     * and store all output of layer "i+1" inside the nestedFields of layer "i".
     */
    private static int printJSONhelper(String dataverseName, String datatypeName, String currentName, String indentation, FileWriter myWriter, String prefix, int listNum, boolean firstComma) {
        /* Return value:
            0: good;
            -1: error
         */

        if (isFlattenedType(datatypeName))
            return 0;
        try {
            Tuple myTuple = new Tuple(dataverseName, datatypeName);
            Derived derived = HashObject.getResult(myTuple).getDerived();

            // Datatype is Record
            if (derived.getTag().equals("RECORD")) {
                List<Fields> fields = derived.getRecord().getFields();

                boolean bo = hasList(dataverseName, datatypeName); // If the "Nested" field is not empty, then we want stuff like "[\n]" instead of "[]"

                if (!bo) {
                    //System.out.println("YES");
                    return 0;
                }

                for (Fields field : fields) {
                    String fieldName = field.getFieldName();
                    String fieldType = field.getFieldType();

                    if (isFlattenedType(fieldType)) continue;
                    String prefixTemp = currentName;
                    if (!prefix.equals("")) prefixTemp = prefix + "." + prefixTemp;
                    int temp = printJSONhelper(dataverseName, fieldType, fieldName, indentation, myWriter, prefixTemp, listNum, firstComma);
                    if (temp == -1)
                        return -1;
                    firstComma = false;
                }
            }

            // Datatype is List
            else {
                if (!firstComma)
                    myWriter.write(",\n");

                String listType;
                if (derived.getTag().equals("ORDEREDLIST"))
                    listType = derived.getOrderedList();
                else
                    listType = derived.getUnorderedList();

                myWriter.write(indentation + "\t{\n");
                if (prefix.equals(""))
                    myWriter.write(indentation + "\t\t\"name\": \"" + currentName + "\",\n");
                else
                    myWriter.write(indentation + "\t\t\"name\": \"" + prefix + "." + currentName + "\",\n");
                myWriter.write(indentation + "\t\t\"type\": \"" + listType + "\",\n");
                myWriter.write(indentation + "\t\t\"primaryKey\": [],\n");
                myWriter.write(indentation + "\t\t\"nestedFields\": [");

                if (hasList(dataverseName, listType)) {
                    myWriter.write("\n");
                    printJSONhelper(dataverseName, listType, "_InnerList" + listNum, indentation + "\t\t", myWriter, "", listNum + 1, true);
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