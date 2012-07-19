;; Custom schema and emitter
(ns hemlock.example2
  (:require [hemlock.core :as hemlock]
            [hemlock.utils :as utils]
            [clojure.java.io :as io]
            [clojure.data.xml :as xml]
            [datomic.api :refer [db q] :as d]))

(def myschema
  [{:db/id #db/id[:db.part/db]
    :db/ident :author
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :genre
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :price
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :publish-date
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :description
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}])
   
(def xml-db (utils/empty-db "ex2" (concat hemlock/schema myschema)))

;; When a :book tag is encountered we do custom parsing/emitting
;; We ignore the parent id and idx and instead use our custom attributes
(defmethod hemlock/emit-element :book [element pid idx tempid]
  (let [[author
         title
         genre
         price
         publish-date
         description] (map (comp first :content)
                           (:content element))]
    [{:db/id (tempid)
      :author author
      :title title
      :price (read-string price)
      :publish-date publish-date
      :description description}]))

(def books-db (d/with xml-db
                      (-> "resources/books.xml"
                          slurp
                          xml/parse-str
                          hemlock/emit-tx-data)))

;; Much easier to query this database than the one in
;; the previous example.
(q '[:find ?title
     :where
     [?book :price ?price]
     [?book :title ?title]
     [(< ?price 5)]]
   books-db)

