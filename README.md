# nfview2

## Description

Since Asterixdb is NoSQL, some fields of a Dataset can be nested. This tool gives people a chance to view the Datasets in a traditional SQL way.

## Installation

For Windows: Run the nfview2.exe file in the root directory and the software will be installed in your C:\Program Files.
(Note: I used jpackage, but the generated software does not work properly, so I did not upload this installer for now)

For other OS: it is not supported at the moment. I'll add the installers once the issue above is solved.

## Usage

```
nfview2 [-h] [-r <arg>] [-w <arg>]
Create flat views for all nested fields.
-h         help
-r <arg>   format: server port dataverseName datasetName fileName
-w <arg>   format: server port dataverseName datasetName fileName
```

Firstly, use the flag *-w*. The structure of the Dataset specified can be presented in the format of JSON inside the file specified.

Secondly, modify the *primaryKey* fields you want inside that JSON file. It could be unmodified at all (the positions will be the Primary Keys of the nested fields)

Thirdly, use the flag *-r*, and enter the same arguments. The program will read the modified JSON file back and generate the views.

## Q & A

Q. What's the point of creating the structure of the Dataset?

A. It makes specifying the Primary Keys of the nested fields easier.

Q. Why do we need to specify the Primary Keys for the Datatypes?

A. Because views do not have the Primary Key constraints. For example, if the Dataset has a field *a: \[nestedType\]*, the generated view cannot enforce the uniqueness of the elements in the list, unless the user guarantees it. What's more, the specified Primary Key will also be served as the Foreign Key for the nested fields of the *nestedType*. 

Q. What if the elements of a list will be repeated?

A. We will use the index of the elements as the Primary Key. Check the example below for more details.

Q. Suppose I can guarantee the uniqueness of the list elements, and I wish to declare the list elements of the field *a: \[int\]* as the Primary Key. However, there's no field name for *int*. What do I do?

A. You just put *int64* inside the *primaryKey*. Similarly, if the type is *string* or others, you put *string* or others inside the *primaryKey*.

## Examples

Please check [Examples.md](https://github.com/phaqueue/nfview2/edit/main/Examples.md) for more info about how and why this software works.

## Credits

I wish to thank Dr.Wail Alkowaileet and Ian Maxon for their help, and Prof. Michael Carey for his guidance.

## License

Licensed under the MIT license.

## Feedback

Please contact me via email: yunfanq2@uci.edu
(Note this email will be invalid soon because I will graduate in June. I'll update my email by then)

## Tests

You can find the tests at ./src/test/java/edu/uci/ics/asterix/ClientTest.java

Here's also a link to my user-friendly tests on GoogleDoc: https://docs.google.com/document/d/101G7ngrilXnRMQLT7qLPf87RsmW7h2QSc2EtbhbQCUw/edit

## Future Improvements

1. The names *_Anon* and *_pos* are ugly. We should have some naming convention, or allow the users to decide the names.
2. Print tabular views instead, which is more user-friendly.
3. Figure out the installation for Windows and other OS.
4. We can only create views for one Dataset at a time. This software should be able to create views for all Datasets in the specified Dataverse.
5. Maybe we can present the views in a graphical way, similar to Neo4j.
