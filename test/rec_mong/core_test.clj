(ns rec-mong.core-test
  (:use clojure.test
        rec-mong.core
        [monger.result :only [ok?]])
  (:require [monger.core :as mg]
            [monger.conversion :as mc]))

(defrecord Foo [bar baz])

(defn cleanup [f]
  (f)
  (remove-all Foo)
  (let [qs (query :kind Foo)]
    (is (empty? qs))
    ))

(use-fixtures :each cleanup)

(def uri "mongodb://localhost:27017/srt")
(defonce conn (mg/connect-via-uri! uri))

(defn new-foo [bar baz] (->Foo bar baz))

(deftest rec-mong-test
  (let [foo1 (new-foo "1" "2")
        save-foo1 (save foo1)
        id1 (id save-foo1)]
    (is (= foo1 (dissoc save-foo1 :_id)))
    (let [qs (query :kind Foo)]
      (is (= 1 (count qs)))
      (is (= id1 (id (first qs))))
      (let [foo2 (new-foo "3" "4")
            save-foo2 (save foo2)
            id2 (id save-foo2)]
        (is (= foo2 (dissoc save-foo2 :_id)))
        (let [qs (query :kind Foo)]
          (is (= 2 (count qs)))
          (is (= (hash-set id1 id2) (set (map id qs))))
          (let [read-foo1 (retrieve Foo id1)
                read-foo2 (retrieve Foo id2)]
            (is (= save-foo1 read-foo1))
            (is (= save-foo2 read-foo2))
            (let [ids (list id1 id2)
                  read-foos (retrieve Foo (list id1 id2))]
              (is (= ids (map id read-foos)))
              (is (= (hash-set save-foo1 save-foo2) (set read-foos)))
              (let [ids (list (str id2) "012345678901234567890123")
                    ids (map mc/to-object-id ids)
                    read-foos (retrieve Foo ids)]
                (is (= 1 (count read-foos)))
                (is (= (list save-foo2) read-foos))
                ))))))))

(deftest test-multi-record-save-and-read
  (let [foo1 (new-foo 1 2)
        foo2 (new-foo 3 4)]
    (is (= true (save [foo1 foo2])))
    (let [ids (map id (query :kind Foo))]
      (is (= 2 (count ids)))
      (let [foos (retrieve Foo ids)]
        (is (= 2 (count foos)))
        (is (= ids (map id foos)))
  ))))

(deftest test-multi-record-save-with-update
  (let [foo1 (save (new-foo 1 2))
        foo2 (save (new-foo 3 4))]
    (let [ids (map id (query :kind Foo))]
      (is (= 2 (count ids)))
      (let [foo1 (assoc foo1 :bar "b")]
        (is (save (vector foo1 foo2)))
        (let [foos (query :kind Foo)]
          (is (= 2 (count foos)))
          (is (= foo1 (first foos)))
    )))))

(deftest test-single-record-save-with-update
  (let [foo1 (save (new-foo 1 2))
        foo2 (save (new-foo 3 4))]
    (let [ids (map id (query :kind Foo))]
      (is (= 2 (count ids)))
      (let [foo1 (assoc foo1 :bar "b")]
        (is (save foo1 ))
        (let [foos (query :kind Foo)]
          (is (= 2 (count foos)))
          (is (= foo1 (first foos)))
    )))))
