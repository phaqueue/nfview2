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
    private static final String FORMAT = "DataverseName (DatasetName)? ((-r)|(-w) FileName)? ";

    public static void main(String[] args) throws IOException {

        // -----------------------------Init---------------------------------
        // There shouldn't be any issue within init.

        if (args.length == 0 || args.length > 4) {
            System.out.println("Invalid number of arguments!");
            return;
        }

        if (args[0].equals("-h")) {
            System.out.println(FORMAT);
            return;
        }

        HttpAPIClient WailClient = new HttpAPIClient("localhost", "19002");
        ResultObject<Datatype> DatatypeObject = WailClient.getDatatypes(DATATYPE_QUERY);
        ResultObject<Dataset> DatasetObject = WailClient.getDatasets(DATASET_QUERY);

        String DataverseName = args[0];

        //Some initialization... Plz help I wish to write an init function in ResultObject.java
        for (Datatype datatype : DatatypeObject.getResults())
            if (DataverseName.equals(datatype.getDataverseName())) {
                Tuple t = new Tuple(DataverseName, datatype.getDatatypeName());
                // Create a map with DatatypeName as key (we don't need to care about DatasetName because it won't change)
                HashObject.setResult(t, datatype);

                // We don't want to print the same Datatype twice, so we want to know if this Datatype is already found and printed.
                HashObject.setFound(t, false);
            }
        for (Dataset dataset : DatasetObject.getResults())
            if (DataverseName.equals(dataset.getDataverseName())) {
                Tuple t = new Tuple(DataverseName, dataset.getDatasetName());
                HashObject.setSettotype(t, dataset.getDatatypeName());
            }

        String DatasetName = null;
        int start = 1;
        if (args[1].charAt(0) != '-') {
            DatasetName = args[1];
            start++;
        }
        String FileName = null;
        boolean rflag = false;
        boolean wflag = false;

        try {
            for (int i = start; i < args.length; i++)
                if (args[i].charAt(0) == '-') {
                    if (args[i].equals("-r")) {
                        if (rflag || wflag) throw new Exception();
                        rflag = true;
                        FileName = args[++i];

                    } else if (args[i].equals("-w")) {
                        if (rflag || wflag) throw new Exception();
                        wflag = true;
                        FileName = args[++i];

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
        for (Dataset dataset : DatasetObject.getResults())
            if (DataverseName.equals(dataset.getDataverseName())) {
                bo = true;
                break;
            }
        if (!bo) {
            System.out.println("Dataverse not found!");
            return;
        }

        // Check if Dataset exists
        String DatatypeName = null;
        for (Dataset dataset : DatasetObject.getResults())
            if (DataverseName.equals(dataset.getDataverseName()) && dataset.getDatasetName().equals(DatasetName))
                DatatypeName = dataset.getDatatypeName();
        if (DatatypeName == null) {
            System.out.println("Dataset not found!");
            return;
        }

        // -----------------------------Deal with Flags---------------------------------

        if (wflag) printJSON(DataverseName, DatasetName, DatatypeName, FileName, DatasetObject);

        if (rflag) {
            Nested NestedObject = readJSON(FileName);
            if (NestedObject == null)
                return;
            try {
                // Will need to check if the PK fields exist here.
                List<String> From = new ArrayList<>();
                From.add(DatasetName);
                createView(DataverseName, DatatypeName, DatasetName, NestedObject, new ArrayList<>(), From, 0);
            } catch (Exception e) {
                System.out.println("Error with JSON file!");
                return;
            }
        }

    }

    private static void printJSON(String DataverseName, String DatasetName, String DatatypeName, String FileName, ResultObject<Dataset> DatasetObject) {
        // Let's assume DatasetName is not NULL for now

        try {
            File myFile = new File(FileName);
            myFile.createNewFile();
            FileWriter myWriter = new FileWriter(myFile, false);

            myWriter.write("{\n");
            myWriter.write("\t\"Name\": \"" + DatasetName + "\",\n");
            myWriter.write("\t\"Type\": \"" + DatatypeName + "\",\n");
            myWriter.write("\t\"PrimaryKeys\": [");

            // Get the PrimaryKey of the Dataset we want
            for (Dataset dataset : DatasetObject.getResults())
                if (DataverseName.equals(dataset.getDataverseName()) && DatasetName.equals(dataset.getDatasetName())) {
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
            if (hasList(DataverseName, DatatypeName)) {
                myWriter.write("\n");

                Tuple myTuple = new Tuple(DataverseName, DatatypeName);
                Derived derived = HashObject.getResult(myTuple).getDerived();
                List<Fields> fields = derived.getRecord().getFields();
                boolean first_comma = true;

                for (Fields field : fields) {
                    String FieldType = field.getFieldType();
                    String FieldName = field.getFieldName();

                    if (!hasList(DataverseName, FieldType)) continue;
                    printJSONhelper(DataverseName, field.getFieldType(), field.getFieldName(), "\t", myWriter, "", 1, first_comma);
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

    private static Nested readJSON(String FileName) {
        // Read JSON back

        // Let's have a convention here: if the "PrimaryKeys" field is empty, it means that it's just a list of a flat type.

        String JSONString = "";
        Nested NestedObject = null;

        try {
            File JSONFile = new File(FileName);
            Scanner reader = new Scanner(JSONFile);
            while (reader.hasNextLine())
                JSONString = JSONString.concat(reader.nextLine());

            NestedObject = OBJECT_MAPPER.readValue(JSONString, new TypeReference<>() {
            });

        } catch (Exception e) {
            System.out.println("Error with file operations!");
            return null;
        }
        return NestedObject;
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

        /*
        for (int i = 0; i < NestedObject.getPrimaryKeys().size(); i++) {
            String s = NestedObject.getPrimaryKeys().get(i);
            int pos = s.lastIndexOf('.');
            String temp = s;
            if (pos != -1)
                temp = s.substring(pos + 1);
            if (rename.containsKey(temp)) {
                rename.put(temp, rename.get(temp) + 1);
                NestedObject.getPrimaryKeys().set(i, s + "AS " + temp + rename.get(temp));
            } else
                rename.put(temp, 1);
        }
         */

        boolean addedPosNum = false;

        // If the user did not enter any fields, then the position will be the PK
        if (NestedObject.getPrimaryKeys().size() == 0) {
            PrimaryKey.add("_pos" + PosNum);
            newPrimaryKey.add("_pos" + PosNum);
            addedPK++;
            //From.add(prefix + " _Anon" + PosNum + " AT " + "_pos" + PosNum);
            /*
            prefix = "_Anon" + PosNum; // should this also change?
            PosNum++;
             */
            addedPosNum = true; // don't add twice.
            //addedFrom++;
        }

        /*
        Wait shouldn't the prefix always be reset?
        // Reset the prefix, if it's not a record
        if (!addedPosNum && !getTag(DataverseName, DatatypeName).equals("RECORD")) {
            From.add(prefix + " _Anon" + PosNum + " AT " + "_pos" + PosNum);
            addedFrom++;
            prefix = "_Anon" + PosNum;
            PosNum++;
        }
         */

        /*
        if (NestedObject.getNested().size() != 0) {
            addedFrom++;
            From.add(prefix + " _Anon" + PosNum + " AT " + "_pos" + PosNum);
        }*/


        // Check: if the number of Primary Keys defined by the user is equal to the total number of flat fields (meaning they are all part of PK)
        // and it does have NestedFields, then this current VIEW should not be printed.

        // I simplified ViewName here...
        if (!(NestedObject.getPrimaryKeys().size() == countFlat(DataverseName, DatatypeName) && NestedObject.getNested().size() != 0)) {
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
        for (String s : NestedObject.getPrimaryKeys()) {
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

        /*
        // this Datatype should always be List... except the one at the beginning...
        if (NestedObject.getNested().size() == 0) {
            for (int i = 0; i < addedPK; i++)
                PrimaryKey.remove(PrimaryKey.size() - 1);
            for (int i = 0; i < addedFrom; i++)
                From.remove(From.size() - 1);
            return PosNum;
        }
         */

        // Create views for all List fields
        for (Nested i : NestedObject.getNested()) {
            /*
            if (getTag(DataverseName, DatatypeName).equals("RECORD"))
                //if (getTag(DataverseName, i.getType()).equals("RECORD"))
                PosNum = createView(DataverseName, i.getType(), prefix + "." + i.getName(), i, PrimaryKey, From, PosNum);
            else
            ??????
             */
            //PosNum = createView(DataverseName, i.getType(), "_Anon" + (PosNum - 1), i, PrimaryKey, From, PosNum);
            if (getTag(DataverseName, DatatypeName).equals("RECORD")) {
                //Should I care about duped names here? Probably not
                From.add(prefix + "." + i.getName() + " _Anon" + (PosNum + 1) + " AT " + "_pos" + (PosNum + 1));
            } else
                From.add(prefix + " _Anon" + (PosNum + 1) + " AT " + "_pos" + (PosNum + 1));
            PosNum = createView(DataverseName, i.getType(), "_Anon" + (PosNum + 1), i, PrimaryKey, From, PosNum + 1);
            From.remove(From.size() - 1);
        }

        for (int i = 0; i < addedPK; i++)
            PrimaryKey.remove(PrimaryKey.size() - 1);
        //for (int i = 0; i < addedFrom; i++)
        //    From.remove(From.size() - 1);

        return PosNum;
        /*
        Tuple myTuple = new Tuple(DataverseName, DatatypeName);
        Derived derived = HashObject.getResult(myTuple).getDerived();

        if (derived.getTag().equals("RECORD")) {
            //List<Fields> fields = derived.getRecord().getFields();
            //for (Fields field : fields) {
            //    findNested(DataverseName, DatatypeName, prefix + "." + field.getFieldName(), NestedObject, PrimaryKey, From, PosNum);
            //}
            findNested(DataverseName, DatatypeName, prefix, NestedObject, PrimaryKey, From, PosNum);
        } else {
            String ListType;
            if (derived.getTag().equals("ORDEREDLIST"))
                ListType = derived.getOrderedList();
            else
                ListType = derived.getUnorderedList();
            findNested(DataverseName, ListType, prefix, NestedObject, PrimaryKey, From, PosNum);
        }*/
    }

    private static void findNested(String DataverseName, String DatatypeName, String prefix, Nested NestedObject, List<String> PrimaryKey, List<String> From, int PosNum) {
        if (isFlattenedType(DatatypeName))
            return;

        Tuple myTuple = new Tuple(DataverseName, DatatypeName);
        Derived derived = HashObject.getResult(myTuple).getDerived();

        // Datatype is Record
        if (derived.getTag().equals("RECORD")) {
            List<Fields> fields = derived.getRecord().getFields();
            for (Fields field : fields) {
                findNested(DataverseName, field.getFieldType(), prefix + "." + field.getFieldName(), NestedObject, PrimaryKey, From, PosNum);
            }
        } else
            createView(DataverseName, DatatypeName, prefix, NestedObject, PrimaryKey, From, PosNum);
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
                myWriter.write(indentation + "\t\t\"PrimaryKeys\": [],\n");
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


/*
// This is a potential JUnit Test
CREATE TYPE NestedType AS {
	id: int,
    stuff: {
    	a: int,
        b: int
    }
}

CREATE DATASET NestedSet(NestedType)
	PRIMARY KEY stuff.a;

INSERT INTO NestedSet (
  [
    {"id": 1, "stuff": {"a": 2, "b": 3}},
    {"id": 4, "stuff": {"a": 5, "b": 6}}
   ]
)

// So basically we want to see:
// SELECT id, stuff.a, stuff.b FROM NestedSet

DROP DATASET ListSet IF EXISTS;
DROP TYPE ListType IF EXISTS;

CREATE TYPE ListType AS {
	id: int,
    lis1: [int],
    lis2: [NestedType]
};

CREATE DATASET ListSet(ListType)
	PRIMARY KEY id;

INSERT INTO ListSet (
  [
    {
    	"id": 1,
    	"lis1": [
          1,
          2,
          3
        ],
    	"lis2": [
          {
          	"id": 1,
          	"stuff":
          	{
          		"a": 2,
          		"b": 3
          	}
          }
        ]
    },
    {"id": 2, "lis1": [4, 5, 6], "lis2": [{"id": 1, "stuff": {"a": 2, "b": 3}}, {"id": 3, "stuff": {"a": 4, "b": 5}}]}
   ]
);

CREATE OR REPLACE VIEW ListTypeView AS
	SELECT L.lis1
	FROM ListSet L;

// Other potential JUnit Tests:
// 1. a flat dataset
// 2. orders in commerce
// 3. super nested dataset
// 4. the datatype contains [int] -- how would the JSON file look like?
// 5. the datatype contains [[int]] -- and also replace that int with other types
// 6. a: {{{{[int]}}}} -- and also replace that int with other types
// 7. [[[]]] -- we only need one view for this
// 8. PrimaryKeys of a RECORD is empty
// 9. {[], []}: should not create a view for it; or if all flat fields are PK but no further nested fields (can simply count and compare).
// 10. [[[{[[]], []}]]]

{
	"Name": "ListType",
	"PrimaryKeys": ["id"],
	"Nesteded": [
		{
			"Name": "lis1",
			"PrimaryKeys": [],
			"Nesteded": []
		},
		{
			"Name": "lis2",
			"PrimaryKeys": ["id"],
			"Nesteded": []
		}
	]
}

CREATE TYPE SimpleListOfListType AS {
	a: [[int]],
    id: int
};
CREATE DATASET SimpleListOfListSet (SimpleListOfListType)
	PRIMARY KEY id;

INSERT INTO SimpleListOfListSet (
    [{"id": 1, "a": [[1, 2, 3], [4, 5, 6]]}]
);

SELECT SimpleListOfListSet.id, pos1, pos2, c
FROM SimpleListOfListSet, SimpleListOfListSet.a b AT pos1, b c AT pos2

CREATE TYPE WierdType AS {
    a: {b:{c:{d:{e: [int]}}}},
    id: int
};
CREATE DATASET WierdSet (WierdType)
	PRIMARY KEY id;

CREATE TYPE ComplicatedType AS {
	a: {b: [[[{c: [[int]], d: {e: [[[int]]], f: int}}]]]},
    id: int
};
CREATE DATASET ComplicatedSet (ComplicatedType)
	PRIMARY KEY id;

CREATE TYPE List5Type AS {
    a: [[[[[int]]]]],
    id: int
};
CREATE DATASET List5Set (List5Type)
	PRIMARY KEY id;
*/