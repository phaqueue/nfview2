# nfview2
Create flat (1NF) view for a nested Dataset

Format: DataverseName (DatasetName)? (-r)|(-w) FileName

Use Guide:

Firstly, specify which Dataset in which Dataverse that you want to be flattened. Then use the flag "-w" along with a filename, so that the structure of the Dataset can be presented in the format of JSON inside the file specified.

Secondly, modify the "PrimaryKeys" fields you want inside that JSON file. It could be unmodified at all (the positions will be the Primary Keys of the nested fields)

Thirdly, enter the same DataverseName, DatasetName and filename, but change the flag to "-r". The program will read the modified JSON file back and generate the views.


Link for my tests on GoogleDoc: https://docs.google.com/document/d/101G7ngrilXnRMQLT7qLPf87RsmW7h2QSc2EtbhbQCUw/edit

P.S. 
  1. I don't know why there's a "2" after the name "nfview". 
  2. If you are wondering about what my tuples do in my code, you can just skip it... I copied it from my last project, and it is of no use in this project.
  3. Flattening all Datasets is currently unsupported, so the format is actually "DataverseName DatasetName (-r)|(-w) FileName".
