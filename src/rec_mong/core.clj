(ns rec-mong.core
  (:refer-clojure :exclude [remove])
  (:require [monger.core :as mg])
  (:use [monger.collection :only [insert insert-and-return find-maps find-one-as-map find-map-by-id
                                  remove insert-batch]]
        [monger.result :only [ok?]]
        [monger.operators :only [$in]]
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
  "Save a record or records to database under a new id
  for single record returns the record with :_id
  for multiple records returns if the save went ok"
  [record]
  (if (sequential? record)
    (let [kind (.getName (class (first record)))]
      (ok? (insert-batch kind record)))
    (let [kind (.getName (class record))]
      (when-let [m (insert-and-return kind (assoc record :_id (ObjectId.)))]
        ((map->record (class record)) m)))))

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
    ;(map #(retrieve kind %) id)
    (map (map->record kind) (find-maps (.getName kind) {:_id {$in id}}))
    (if-let [m (find-map-by-id (.getName kind) (db-id id))]
      ((map->record kind) m))))

(defn query [& {:keys [kind filter]}]
  (if filter
    (do
      (prn filter)
      (map (map->record kind) (find-maps (.getName kind) filter)))
    (map (map->record kind) (find-maps (.getName kind)))))


(defn remove-all [kind]
  (remove (.getName kind)))
