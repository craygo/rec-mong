(ns rec-mong.core
  (:refer-clojure :exclude [remove])
  (:require [monger.core :as mg])
  (:use [monger.collection :only [insert insert-and-return find-maps find-one-as-map find-map-by-id
                                  remove remove-by-id insert-batch save-and-return]]
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

(defn id 
  "return the database id of a record"
  [record]
  (:_id record))

(defn save 
  "Save or update a record or records to database
  for single record returns the record with :_id
  for multiple records returns if the save went ok"
  [record]
  (if (sequential? record)
    (let [kind (.getName (class (first record)))
          new-ones (filter (comp nil? id) record)
          existing (filter id record)]
      ;(prn "save " new-ones existing)
      (and (or (empty? new-ones) (ok? (insert-batch kind new-ones)))
           (or (empty? existing)
               (not (contains? (set (doall (map #(ok? (monger.collection/save kind %)) existing))) false))))
      )
    (let [kind (.getName (class record))]
      (if (:_id record)
        (if-let [m (save-and-return kind record)]
          ((map->record (class record)) m))
        (if-let [m (insert-and-return kind (assoc record :_id (ObjectId.)))]
          ((map->record (class record)) m))))))

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
    (map (map->record kind) (find-maps (.getName kind) filter))
    (map (map->record kind) (find-maps (.getName kind)))))

(defn remove-all [kind]
  (remove (.getName kind)))

(defn delete [kind id]
  (remove-by-id (.getName kind) (db-id id)))

(defmethod print-method org.bson.types.ObjectId
  [oid out]
  (.write out (str `(org.bson.types.ObjectId. ~(str oid)))))
