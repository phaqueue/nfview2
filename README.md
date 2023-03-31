# nfview2

usage: nfview2 [-h] [-r <arg>] [-w <arg>]

Create flat views for all nested fields.

-h   &emsp;&emsp;&emsp;&emsp;  help

-r \<arg\> &emsp;   format: server port dataverseName datasetName fileName
           
-w \<arg\> &ensp;  format: server port dataverseName datasetName fileName

<br /><br />

Use Guide:

Firstly, use the flag "-w". The structure of the Dataset specified can be presented in the format of JSON inside the file specified.

Secondly, modify the "PrimaryKeys" fields you want inside that JSON file. It could be unmodified at all (the positions will be the Primary Keys of the nested fields)

Thirdly, use the flag "-r", and enter the same arguments. The program will read the modified JSON file back and generate the views.

Link for my user-friendly tests on GoogleDoc: https://docs.google.com/document/d/101G7ngrilXnRMQLT7qLPf87RsmW7h2QSc2EtbhbQCUw/edit
