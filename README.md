# rec-mong

A Clojure library designed to provide a persistence API for records
with an initial implementation through Monger to write to Mongo DB

## Usage

Use in conjuntion with Monger and Mongo database
The API is similar to what appengine-magic provides to the Google appengine datastore
so save!, retrieve delete!

A record defined with defrecord in your Clojure code will be saved to a document 
with the save! function where the document type is taken from the (by default) unqualified name of the record type.
so 
(defrecord Foo [attr1 attr2])
will be saved to a Mongo document under name "Foo"

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
