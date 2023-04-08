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

## Example1: *customers*

Let's test on Dataset *customers* first. As we can see, the Datatype it is based on has a nested field *address*, which is a *RECORD*. If you check the Datatypes Tab on the right of Asterixdb, you can also find that *customersType_address*, a new Datatype that contains *street*, *city*, and *zipcode*, is created. To flatten this Dataset, the view should select all of the fields inside the *address* along with the remaining fields of the *customersType*.

We run the below command in your Powershell to generate the structure of *customers*. Here *-w* is the flag for generating the structure of the Dataset, *localhost* is the host, *19002* is the port, *DonCDataSchema* is the Dataverse, *customers* is the Dataset, and *json.txt* is the file which the structure of the Dataset will write to.

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
Here you can see the basic structure of the Dataset *customers*. These four fields may have different meanings depending on whether it is the outer layer or an inner layer (the structure can be recursive). Here's the meaning of the outer layer.
1. *name*: the name of the current Dataset.
2. *type*: the Datatype that the current Dataset is based on.
3. *primaryKey*: the user-specified Primary Key when creating the Dataset.
4. *nestedFields*: the nested fields of this Dataset whose Primary Keys need to be specified. To refer to a nested field, again we will specify its *name*, *type*, *primaryKey*, and *nestedFields*.

Here's the meaning of the inner layers. Please refer to the example of *orders* below, which will include the inner layers. There's only one outer layer for the example of *customers*.
1. *name*: the alias of the current Datatype that we will be using.
2. *type*: the "actual" name of the Datatype, according to Metadata.
3. *primaryKey*: the "Primary Key of the Datatype" that is waiting to be specified by the user. If you wonder why we need to specify the Primary Keys for Datatypes, please refer to the Q&A section of [readme.md](https://github.com/phaqueue/nfview2/edit/main/README.md).
4. *nestedFields*: the nested fields of this Datatype whose Primary Keys need to be specified.

Let's refer back to the example of *customers*. The name of the Dataset is *customers*, the Datatype *customers* is based on is *customersType*, and the *primaryKey* specified by the user when creating the Dataset is *custid*. You may find it confusing that the *nestedFields* is empty, even though *address* is a nested field. The reason is that there's no need to specify its Primary Key since we will simply select every field of that *RECORD*. Thus, we do not include this field in *nestedFields*.

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

## Example 2: *orders*

As for *orders*, there's a nested field, *items*, which is a list of *itemno*, *qty*, and *price*. According to the Metadata, the Datatype of *items* is *ordersType_items*, whose only field is a list of *ordersType_items_Item*, which is consisted of *itemno*, *qty*, and *price*. For *orders*, we will create 2 views: one is everything except *items*, and the other one includes the elements inside *items*, the optional index of the corresponding *ordersType_items_Item* (if no Primary Key is specified; this will be explained later), and the foreign key *orderno*.

Run the following command:

```
java -jar ./target/nfview-0.1-SNAPSHOT-jar-with-dependencies.jar -w localhost 19002 DonCDataSchema orders json.txt
```

Here's the generated structure of *orders*:

```
{
	"name": "orders",
	"type": "ordersType",
	"primaryKey": ["orderno"],
	"nestedFields": [
		{
			"name": "items",
			"type": "ordersType_items_Item",
			"primaryKey": [],
			"nestedFields": []
		}
	]
}
```

We can see that *orders* has a nested field, *items*. Its type is *ordersType_items_Item*, its primaryKey has yet to be filled out, and its fields are all flat.

The next step is to fill in the *primaryKey*. Let's fill in *itemno* for now:

```
{
	"name": "orders",
	"type": "ordersType",
	"primaryKey": ["orderno"],
	"nestedFields": [
		{
			"name": "items",
			"type": "ordersType_items_Item",
			"primaryKey": ["itemno"],
			"nestedFields": []
		}
	]
}
```

And run the command:

```
java -jar ./target/nfview-0.1-SNAPSHOT-jar-with-dependencies.jar -r localhost 19002 DonCDataSchema orders json.txt
```

Here's the generated views for *orders*:

```
USE DonCDataSchema;

CREATE OR REPLACE VIEW ordersView AS
        SELECT orders.orderno, orders.custid, orders.order_date, orders.ship_date
        FROM orders;

CREATE OR REPLACE VIEW _Anon1View AS
        SELECT orders.orderno, _Anon1.itemno, _Anon1.qty, _Anon1.price
        FROM orders, orders.items _Anon1 AT _pos1;
```

The software did mostly what I have explained. You might wonder what *orders.items _Anon1 AT _pos1* does. It's for renaming *orders.items* (you cannot refer to something like *orders.items.itemno* directly) and getting the index info (though it is not needed for this example).

Let's run these statements and check if they work well:

*ordersView*:

```
USE DonCDataSchema;

SELECT *
FROM ordersView;
```

```
[
        {
                "ordersView": {
                        "orderno": 1002,
                        "custid": "C13",
                        "order_date": "2017-05-01",
                        "ship_date": "2017-05-03"
                }
        },
        {
                "ordersView": {
                        "orderno": 1005,
                        "custid": "C37",
                        "order_date": "2017-08-30"
                }
        },
        {
                "ordersView": {
                        "orderno": 1007,
                        "custid": "C13",
                        "order_date": "2017-09-13",
                        "ship_date": "2017-09-20"
                }
        },
        {
                "ordersView": {
                        "orderno": 1001,
                        "custid": "C41",
                        "order_date": "2017-04-29",
                        "ship_date": "2017-05-03"
                }
        },
        {
                "ordersView": {
                        "orderno": 1003,
                        "custid": "C31",
                        "order_date": "2017-06-15",
                        "ship_date": "2017-06-16"
                }
        },
        {
                "ordersView": {
                        "orderno": 1004,
                        "custid": "C35",
                        "order_date": "2017-07-10",
                        "ship_date": "2017-07-15"
                }
        },
        {
                "ordersView": {
                        "orderno": 1006,
                        "custid": "C41",
                        "order_date": "2017-09-02",
                        "ship_date": "2017-09-04"
                }
        },
        {
                "ordersView": {
                        "orderno": 1008,
                        "custid": "C13",
                        "order_date": "2017-10-13"
                }
        }
]
```

*_Anon1View*:

```
USE DonCDataSchema;

SELECT *
FROM _Anon1View;
```

```
[
        {
                "_Anon1View": {
                        "orderno": 1002,
                        "itemno": 460,
                        "qty": 95,
                        "price": 100.99
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1002,
                        "itemno": 680,
                        "qty": 150,
                        "price": 8.75
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1005,
                        "itemno": 460,
                        "qty": 2,
                        "price": 99.98
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1005,
                        "itemno": 347,
                        "qty": 120,
                        "price": 22
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1005,
                        "itemno": 780,
                        "qty": 1,
                        "price": 1500
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1005,
                        "itemno": 375,
                        "qty": 2,
                        "price": 149.98
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1007,
                        "itemno": 185,
                        "qty": 5,
                        "price": 21.99
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1007,
                        "itemno": 680,
                        "qty": 1,
                        "price": 20.5
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1001,
                        "itemno": 347,
                        "qty": 5,
                        "price": 19.99
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1001,
                        "itemno": 193,
                        "qty": 2,
                        "price": 28.89
                }
        }
]
```

Above is the example of specifying *primaryKey* of the *nestedFields*. What if we don't, i.e., there can be two identical (*itemno*, *qty*, *price*) inside the list?

So we do not modify the JSON file:

```
{
	"name": "orders",
	"type": "ordersType",
	"primaryKey": ["orderno"],
	"nestedFields": [
		{
			"name": "items",
			"type": "ordersType_items_Item",
			"primaryKey": [],
			"nestedFields": []
		}
	]
}
```

And run the command:

```
java -jar ./target/nfview-0.1-SNAPSHOT-jar-with-dependencies.jar -r localhost 19002 DonCDataSchema orders json.txt
```

We get:

```
USE DonCDataSchema;

CREATE OR REPLACE VIEW ordersView AS
        SELECT orders.orderno, orders.custid, orders.order_date, orders.ship_date
        FROM orders;

CREATE OR REPLACE VIEW _Anon1View AS
        SELECT orders.orderno, _pos1, _Anon1.itemno, _Anon1.qty, _Anon1.price
        FROM orders, orders.items _Anon1 AT _pos1;
```

We can see that *_Anon1View* is almost identical, except it also selected *_pos1*, which is the index of (*itemno*, *qty*, *price*). Since the index is always unique, it does a good job as a Primary Key.

If we test the *_Anon1View* again:

```
[
        {
                "_Anon1View": {
                        "orderno": 1002,
                        "_pos1": 1,
                        "itemno": 460,
                        "qty": 95,
                        "price": 100.99
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1002,
                        "_pos1": 2,
                        "itemno": 680,
                        "qty": 150,
                        "price": 8.75
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1005,
                        "_pos1": 1,
                        "itemno": 460,
                        "qty": 2,
                        "price": 99.98
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1005,
                        "_pos1": 2,
                        "itemno": 347,
                        "qty": 120,
                        "price": 22
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1005,
                        "_pos1": 3,
                        "itemno": 780,
                        "qty": 1,
                        "price": 1500
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1005,
                        "_pos1": 4,
                        "itemno": 375,
                        "qty": 2,
                        "price": 149.98
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1007,
                        "_pos1": 1,
                        "itemno": 185,
                        "qty": 5,
                        "price": 21.99
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1007,
                        "_pos1": 2,
                        "itemno": 680,
                        "qty": 1,
                        "price": 20.5
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1001,
                        "_pos1": 1,
                        "itemno": 347,
                        "qty": 5,
                        "price": 19.99
                }
        },
        {
                "_Anon1View": {
                        "orderno": 1001,
                        "_pos1": 2,
                        "itemno": 193,
                        "qty": 2,
                        "price": 28.89
                }
        }
]
```

## Example 3: *products*

Since this Dataset is flat, the generated view is pretty simple: you just select everything in the Dataset:

```
USE DonCDataSchema;

CREATE OR REPLACE VIEW productsView AS
        SELECT products.itemno, products.category, products.name, products.descrip, products.manuf, products.listprice
        FROM products;
```

And if you want to test the view:

```
[
        {
                "productsView": {
                        "itemno": 185,
                        "category": "office",
                        "name": "Stapler",
                        "descrip": "Stapler for up to 25 sheets of paper",
                        "manuf": "Office Min",
                        "listprice": 21
                }
        },
        {
                "productsView": {
                        "itemno": 195,
                        "category": "computer",
                        "name": "Laptop Charger",
                        "manuf": "El Cheapo",
                        "listprice": 49
                }
        },
        {
                "productsView": {
                        "itemno": 780,
                        "category": "computer",
                        "name": "Holo Cow Tablet",
                        "descrip": "Tablet computer with 3D holographic display",
                        "manuf": "El Cheapo",
                        "listprice": 1999
                }
        },
        {
                "productsView": {
                        "itemno": 120,
                        "category": "computer",
                        "name": "16TB External SDD",
                        "descrip": "16TB storage add-on for Holo Cow tablet",
                        "manuf": "El Cheapo",
                        "listprice": 99
                }
        },
        {
                "productsView": {
                        "itemno": 193,
                        "category": "office",
                        "name": "Super Stapler",
                        "descrip": "Stapler for up to 250 sheets of paper",
                        "manuf": "Office Min",
                        "listprice": 28
                }
        },
        {
                "productsView": {
                        "itemno": 347,
                        "category": "essentials",
                        "name": "Beer Cooler Backpack",
                        "manuf": "Robo Brew",
                        "listprice": 25
                }
        },
        {
                "productsView": {
                        "itemno": 375,
                        "category": "music",
                        "name": "Stratuscaster Guitar",
                        "manuf": "Fender Bender",
                        "listprice": 149
                }
        },
        {
                "productsView": {
                        "itemno": 460,
                        "category": "music",
                        "name": "Fender Bender Flight Case",
                        "descrip": "Sturdy flight case for Fender Bender guitars",
                        "manuf": "Fender Bender",
                        "listprice": 109
                }
        },
        {
                "productsView": {
                        "itemno": 680,
                        "category": "essentials",
                        "name": "Automatic Beer Opener",
                        "manuf": "Robo Brew",
                        "listprice": 29
                }
        }
]
```
