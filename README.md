# rec-mong

A Clojure library designed to provide a persistence API like appengine-magic for records
with an implementation through Monger to write to Mongo DB

## Usage

Use in conjuntion with Monger and Mongo DB  
The API is similar to what appengine-magic provides to the Google appengine datastore:

    (save! rec)
    (retrieve Foo rec-id)
    (query :kind Foo :filter {:name "bar"})
    (delete! rec)

A record defined with defrecord in your Clojure code will be saved to a document 
with the save! function where the document type is taken from the qualified name of the record type.
so 

    (ns myapp.model)

    (defrecord Foo [attr1 attr2])

will be saved to a Mongo document under name "myapp.Foo"

The library is still very much alpha but it is used in my mchfc-template to easily start a new web-app
project where the model is records and Mongo DB is used as convenient schema-free storage

## License

Copyright © 2013 Harry Binnendijk

Distributed under the Eclipse Public License, the same as Clojure.
