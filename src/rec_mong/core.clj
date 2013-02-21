(ns rec-mong.core
  (:refer-clojure :exclude [remove])
  (:require [monger.core :as mg])
  (:use [monger.collection :only [insert insert-and-return find-maps find-one-as-map find-map-by-id
                                  remove]]
        [clojure.pprint :only [pprint]]
        [clojure.tools.logging])
  (:import [com.mongodb MongoOptions ServerAddress]
           [org.bson.types ObjectId]))

(defn- map->record [typ]
  (let [sn (.getSimpleName typ)
        cn (.getCanonicalName typ)
        li (.lastIndexOf cn ".")
        cn (.substring cn 0 li)
        cn (.replaceAll cn "_" "-")
        mfst (str "map->" sn)
        mfs (symbol mfst)
        mf (ns-resolve (symbol cn) mfs)]
    mf))

(defn save 
  "Save a record to database under a new id
  returns the record with :_id"
  [record]
  (let [kind (.getName (class record))
        id (ObjectId.)]
    (when-let [m (insert-and-return kind (assoc record :_id id))]
      ((map->record (class record)) m))))

(defn id 
  "return the database id of a record"
  [record]
  (:_id record))

(defn db-id [id]
  (condp instance? id
    ObjectId id
    String (ObjectId. id)))

(defn retrieve 
  "Read single or multiple records from database
  when id is sequential returns a sequence of the records found
  otherwise return a single record for the document with the given kind and id"
  [kind id]
  (if (sequential? id)
    (map #(retrieve kind %) id)
    ;(map (map->record kind) (find-maps (.getName kind) (vec (map db-id id))))
    (if-let [m (find-map-by-id (.getName kind) (db-id id))]
      ((map->record kind) m))))

(defn query [& {:keys [kind]}]
  (map (map->record kind) (find-maps (.getName kind))))

(defn remove-all [kind]
  (remove (.getName kind)))
