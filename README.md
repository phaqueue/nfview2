# nfview2

## Description

Since Asterixdb is NoSQL, some fields of a Dataset can be nested. This tool gives people a chance to view the Datasets in a traditional SQL way.

## Installation

For Windows: Run the nfview2.exe file at the root directory and the software will be installed in your C:\Program Files.
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

Q. Why do we need to specify the Primary Keys?
A. Because views do not have the Primary Key constraints. For example, if the Dataset has a field "a: [nestedType]", the generated view cannot enforce the uniqueness of the elements in the list, unless the user guarantees it. What's more, the specified Primary Key will also be served as Foreign Key for the nested fields of the "nestedType". 

Q. What if the elements of a list will be repeated?
A. We will use the index of the elements as the Primary Key. Check the example below for more details.

## Examples

Let's use the Dataset [GleambookUsers](https://nightlies.apache.org/asterixdb/sqlpp/primer-sqlpp.html) as an example:

```asterixdb
    DROP DATAVERSE TinySocial IF EXISTS;
    CREATE DATAVERSE TinySocial;
    USE TinySocial;

    CREATE TYPE EmploymentType AS {
        organizationName: string,
        startDate: date,
        endDate: date?
    };

    CREATE TYPE GleambookUserType AS {
        id: int,
        alias: string,
        name: string,
        userSince: datetime,
        friendIds: {{ int }},
        employment: [EmploymentType]
    };

    CREATE DATASET GleambookUsers(GleambookUserType)
        PRIMARY KEY id;
```

Run the above query in Asterixdb (Suppose we use the local server and the default port). 

Then run the below command in your Powershell to generate the structure of your Dataset:

```
    java -jar ./target/nfview-0.1-SNAPSHOT-jar-with-dependencies.jar -w localhost 19002 TinySocial GleambookUsers json.txt
```

json.txt at the root directory now shows the structure of the Dataset:

```
    {
	    "name": "GleambookUsers",
	    "type": "GleambookUserType",
	    "primaryKey": ["id"],
	    "nestedFields": [
		    {
			    "name": "friendIds",
			    "type": "int64",
			    "primaryKey": [],
			    "nestedFields": []
		    },
		    {
			    "name": "employment",
			    "type": "EmploymentType",
			    "primaryKey": [],
			    "nestedFields": []
		    }
	    ]
    }
```

Now let's specify the *primaryKey* for *GleambookUsers.friendIds* and "GleambookUsers.employment" by modifying the text file directly:

```
    {
    	    "name": "GleambookUsers",
	    "type": "GleambookUserType",
	    "primaryKey": ["id"],
	    "nestedFields": [
		    {
			    "name": "friendIds",
			    "type": "int64",
			    "primaryKey": ["int64"],
			    "nestedFields": []
		    },
		    {
			    "name": "employment",
			    "type": "EmploymentType",
			    "primaryKey": ["id"],
			    "nestedFields": []
		    }
	    ]
    }
```
Run the following command to generate the flat views:

```
java -jar ./target/nfview-0.1-SNAPSHOT-jar-with-dependencies.jar -r localhost 19002 TinySocial GleambookUsers json.txt
```

Below are the generated statements that can create flat views for the Dataset:
```
    USE TinySocial;

    CREATE OR REPLACE VIEW GleambookUsersView AS
            SELECT GleambookUsers.id, GleambookUsers.alias, GleambookUsers.name, GleambookUsers.userSince
            FROM GleambookUsers;

    CREATE OR REPLACE VIEW _Anon1View AS
            SELECT GleambookUsers.id2, _Anon1
            FROM GleambookUsers, GleambookUsers.friendIds _Anon1 AT _pos1;

    CREATE OR REPLACE VIEW _Anon2View AS
            SELECT GleambookUsers.id2, _Anon2.organizationName, _Anon2.startDate, _Anon2.endDate
            FROM GleambookUsers, GleambookUsers.employment _Anon2 AT _pos2;
```

You can also choose not to specify the PKs. For those cases, we will use the indexes of the elements in the nested fields as PKs:
```
USE TinySocial;

CREATE OR REPLACE VIEW GleambookUsersView AS
        SELECT GleambookUsers.id, GleambookUsers.alias, GleambookUsers.name, GleambookUsers.userSince
        FROM GleambookUsers;

CREATE OR REPLACE VIEW _Anon1View AS
        SELECT GleambookUsers.id2, _pos1, _Anon1
        FROM GleambookUsers, GleambookUsers.friendIds _Anon1 AT _pos1;

CREATE OR REPLACE VIEW _Anon2View AS
        SELECT GleambookUsers.id2, _pos2, _Anon2.organizationName, _Anon2.startDate, _Anon2.endDate
        FROM GleambookUsers, GleambookUsers.employment _Anon2 AT _pos2;
```

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
