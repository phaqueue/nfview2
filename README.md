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

Let's use the DonCData as an example:

```
-- Create and use a fresh dataverse

DROP DATAVERSE DonCDataSchema IF EXISTS;
CREATE DATAVERSE DonCDataSchema;
USE DonCDataSchema;

-- Create a set of complete datatypes

CREATE TYPE customersType AS {
    custid: string,
    name: string,
    address: {
        street: string,
        city: string,
        zipcode: string?
    },
    rating: int?
};

CREATE TYPE ordersType AS {
    orderno: int,
    custid: string,
    order_date: string,
    ship_date: string?,
    items: [{
        itemno: int,
        qty: int,
        price: double
    }]
};

CREATE TYPE productsType AS {
    itemno: int,
    category: string,
    name: string,
    descrip: string?,
    manuf: string,
    listprice: int
};

-- Create a set of corresponding datasets

CREATE DATASET customers(customersType)
    PRIMARY KEY custid;

CREATE DATASET orders(ordersType)
    PRIMARY KEY orderno;

CREATE DATASET products(productsType)
    PRIMARY KEY itemno;

-- Populate the datasets

INSERT INTO customers ([
{"custid": "C13", "name": "T. Cruise", "address": { "street": "201 Main St.", "city": "St. Louis, MO", "zipcode": "63101" }, "rating": 750 },
{"custid": "C25", "name": "M. Streep", "address": { "street": "690 River St.", "city": "Hanover, MA", "zipcode": "02340" }, "rating": 690 },
{"custid": "C31", "name": "B. Pitt", "address": { "street": "360 Mountain Ave.", "city": "St. Louis, MO", "zipcode": "63101" } },
{"custid": "C35", "name": "J. Roberts", "address": { "street": "420 Green St.", "city": "Boston, MA", "zipcode": "02115" }, "rating": 565 },
{"custid": "C37", "name": "T. Hanks", "address": { "street": "120 Harbor Blvd.", "city": "Boston, MA", "zipcode": "02115" }, "rating": 750 },
{"custid": "C41", "name": "R. Duvall", "address": { "street": "150 Market St.", "city": "St. Louis, MO", "zipcode": "63101" }, "rating": 640 },
{"custid": "C47", "name": "S. Loren", "address": { "street": "Via del Corso", "city": "Rome, Italy" }, "rating": 625 }
]);

INSERT INTO orders ([
{ "orderno": 1001, "custid": "C41", "order_date": "2017-04-29", "ship_date": "2017-05-03", "items": [ { "itemno": 347, "qty": 5, "price": 19.99 }, { "itemno": 193, "qty": 2, "price": 28.89 } ] },
{ "orderno": 1002, "custid": "C13", "order_date": "2017-05-01", "ship_date": "2017-05-03", "items": [ { "itemno": 460, "qty": 95, "price": 100.99 }, { "itemno": 680, "qty": 150, "price": 8.75 } ] },
{ "orderno": 1003, "custid": "C31", "order_date": "2017-06-15", "ship_date": "2017-06-16", "items": [ { "itemno": 120, "qty": 2, "price": 88.99 }, { "itemno": 460, "qty": 3, "price": 99.99 } ] },
{ "orderno": 1004, "custid": "C35", "order_date": "2017-07-10", "ship_date": "2017-07-15", "items": [ { "itemno": 680, "qty": 6, "price": 9.99 }, { "itemno": 195, "qty": 4, "price": 35.00 } ] },
{ "orderno": 1005, "custid": "C37", "order_date": "2017-08-30", "items": [ { "itemno": 460, "qty": 2, "price": 99.98 }, { "itemno": 347, "qty": 120, "price": 22.00 }, { "itemno": 780, "qty": 1, "price": 1500.00  }, { "itemno": 375, "qty": 2, "price": 149.98 } ] },
{ "orderno": 1006, "custid": "C41", "order_date": "2017-09-02", "ship_date": "2017-09-04", "items": [ { "itemno": 680, "qty": 51, "price": 25.98 }, { "itemno": 120, "qty": 65, "price": 85.00 }, { "itemno": 460, "qty": 120, "price": 99.98 } ] },
{ "orderno": 1007, "custid": "C13", "order_date": "2017-09-13", "ship_date": "2017-09-20", "items": [ { "itemno": 185, "qty": 5, "price": 21.99 }, { "itemno": 680, "qty": 1, "price": 20.50 } ] },
{ "orderno": 1008, "custid": "C13", "order_date": "2017-10-13", "items": [ { "itemno": 460, "qty": 20, "price": 99.99 } ] }
]);

INSERT INTO products ([
{ "itemno": 120, "category": "computer", "name": "16TB External SDD", "descrip": "16TB storage add-on for Holo Cow tablet", "manuf": "El Cheapo", "listprice": 99.00 },
{ "itemno": 185, "category": "office", "name": "Stapler", "descrip": "Stapler for up to 25 sheets of paper", "manuf": "Office Min", "listprice": 21.99 },
{ "itemno": 193, "category": "office", "name": "Super Stapler", "descrip": "Stapler for up to 250 sheets of paper", "manuf": "Office Min", "listprice": 28.89 },
{ "itemno": 195, "category": "computer", "name": "Laptop Charger", "manuf": "El Cheapo", "listprice": 49.00 },
{ "itemno": 347, "category": "essentials", "name": "Beer Cooler Backpack", "manuf": "Robo Brew", "listprice": 25.95 },
{ "itemno": 375, "category": "music", "name": "Stratuscaster Guitar", "manuf": "Fender Bender", "listprice": 149.99 },
{ "itemno": 460, "category": "music", "name": "Fender Bender Flight Case", "descrip": "Sturdy flight case for Fender Bender guitars", "manuf": "Fender Bender", "listprice": 109.99 },
{ "itemno": 680, "category": "essentials", "name": "Automatic Beer Opener", "description": "Robotic beer bottle opener", "manuf": "Robo Brew", "listprice": 29.95 },
{ "itemno": 780, "category": "computer", "name": "Holo Cow Tablet", "descrip": "Tablet computer with 3D holographic display", "manuf": "El Cheapo", "listprice": 1999.00 }
]);
```

Run the above query in Asterixdb (Suppose we use the local server and the default port). 

Let's test on Dataset *customers* first. As we can see, the Datatype it is based on has a nested field *address*, which is a *RECORD*. If you check the Datatypes Tab on the right of Asterixdb, you can also find that *customersType_address*, a new Datatype that containing *street*, *city*, and *zipcode*, is created. To flatten this Dataset, the view should select all of the fields inside the *address* along with remaining fields of the *customersType*.

We run the below command in your Powershell to generate the structure of *customers*. Here *-w* is the flag for generating the structure of the Dataset, *localhost* is the host, *19002* is the port, *DonCDataSchema* is the Dataverse, *customers* is the Dataset, and *json.txt* is the file which the structure of the Dataset will be write to.

```
java -jar ./target/nfview-0.1-SNAPSHOT-jar-with-dependencies.jar -w localhost 19002 DonCDataSchema customers json.txt
```

*json.txt* at the root directory now shows the structure of the Dataset:

```
{
	"name": "customers",
	"type": "customersType",
	"primaryKey": ["custid"],
	"nestedFields": []
}
```
Here you can see the basic structure of the Dataset *customers*. Based on whether it is the outer layer or an inner layer (the structure can be recursive), these four fields may have different meanings. Here's the meaning of the outer layer.
1. *name*: the name of the current Dataset.
2. *type*: the Datatype that the current Dataset is based on.
3. *primaryKey*: the user specified Primary Key when creating the Dataset.
4. *nestedFields*: the nested fields of this Dataset whose Primary Keys need to be specified. To refer to a nested field, again we will specify its *name*, *type*, *primaryKey*, and *nestedFields*.

Here's the meaning of the inner layers. Please refer to the example of *orders* below, which will include the inner layers. For the example of *customers*, there's only one outer layer.
1. *name*: the alias of the current Datatype that we will be using.
2. *type*: the "actual" name of the Datatype, according to Metadata.
3. *primaryKey*: the "Primary Key of the Datatype" that is waiting to be specified by the user. If you wonder why we need to specify the Primary Keys for Datatypes, please refer to the Q&A section of [readme.md](https://github.com/phaqueue/nfview2/edit/main/README.md).
4. *nestedFields*: the nested fields of this Datatype whose Primary Keys need to be specified.

Let's refer back to the example of *customers*. The name of the Dataset is *customers*, the Datatype *customers* is based on is *customersType*, the *primaryKey* specified by the user when creating the Dataset is *custid*. You may find it confusing that the *nestedFields* is empty, even though *address* is a nested field. The reason is that there's no need to specify its Primary Key, since we will simply select every field of that *RECORD*. Thus, we do not include this field in *nestedFields*.

The next step is to specify the *primaryKey* for the *nestedFields* of *customer*. However, since the *nestedFields* is empty, we cannot specify it (which is also pointless). Let's skip this step and do not modify the JSON file for now.

```
{
	"name": "customers",
	"type": "customersType",
	"primaryKey": ["custid"],
	"nestedFields": []
}
```

The last step is to run the following command to generate the flat views of *customers*. Everything is identical to the first step, except we change the flag to *-r*. 

```
java -jar ./target/nfview-0.1-SNAPSHOT-jar-with-dependencies.jar -r localhost 19002 DonCDataSchema customers json.txt
```

Below are the generated statements that can create flat views for *customers*:

```
USE DonCDataSchema;

CREATE OR REPLACE VIEW customersView AS
        SELECT customers.custid, customers.name, customers.address.street, customers.address.city, customers.address.zipcode, customers.rating
        FROM customers;
```

As you can see, we selected everything of *customer*, including the nested field *address*. Let's run this statement in Asterixdb, as well as the following statement:

```
USE DonCDataSchema;

SELECT *
FROM customersView;
```

And here's the result:

```
[
        {
                "customersView": {
                        "custid": "C41",
                        "name": "R. Duvall",
                        "street": "150 Market St.",
                        "city": "St. Louis, MO",
                        "zipcode": "63101",
                        "rating": 640
                }
        },
        {
                "customersView": {
                        "custid": "C47",
                        "name": "S. Loren",
                        "street": "Via del Corso",
                        "city": "Rome, Italy",
                        "rating": 625
                }
        },
        {
                "customersView": {
                        "custid": "C13",
                        "name": "T. Cruise",
                        "street": "201 Main St.",
                        "city": "St. Louis, MO",
                        "zipcode": "63101",
                        "rating": 750
                }
        },
        {
                "customersView": {
                        "custid": "C25",
                        "name": "M. Streep",
                        "street": "690 River St.",
                        "city": "Hanover, MA",
                        "zipcode": "02340",
                        "rating": 690
                }
        },
        {
                "customersView": {
                        "custid": "C31",
                        "name": "B. Pitt",
                        "street": "360 Mountain Ave.",
                        "city": "St. Louis, MO",
                        "zipcode": "63101"
                }
        },
        {
                "customersView": {
                        "custid": "C35",
                        "name": "J. Roberts",
                        "street": "420 Green St.",
                        "city": "Boston, MA",
                        "zipcode": "02115",
                        "rating": 565
                }
        },
        {
                "customersView": {
                        "custid": "C37",
                        "name": "T. Hanks",
                        "street": "120 Harbor Blvd.",
                        "city": "Boston, MA",
                        "zipcode": "02115",
                        "rating": 750
                }
        }
]
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

## Future Improvements

1. The names *_Anon* and *_pos* are not user-friendly. Maybe we can have some naming conventions.
2. We can only create views for one Dataset at a time. This software should be able to create views for all Datasets in the specified Dataverse.
3. Maybe we can present the views in a graphical way, similar to Neo4j.
