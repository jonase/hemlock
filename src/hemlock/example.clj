;; First example: default emitting and schema
(ns hemlock.example
  (:require [hemlock.core :as hemlock]
            [hemlock.utils :as utils]
            [clojure.data.xml :as xml]
            [datomic.api :refer [db q] :as d]))

(def xml-db (utils/empty-db "ex1" hemlock/schema))

;; Create a database of books
(def books-db (d/with xml-db
                      (-> "resources/books.xml"
                          slurp
                          xml/parse-str
                          hemlock/emit-tx-data)))

;; Find every book title and price. Complicated query
(q '[:find ?title ?price
     :where
     [?price-tag :hemlock/tag :price]
     [?price-id :hemlock/parent ?price-tag]
     [?price-id :hemlock/text ?price]

     [?title-tag :hemlock/tag :title]
     [?title-id :hemlock/parent ?title-tag]
     [?title-id :hemlock/text ?title]

     [?price-tag :hemlock/parent ?book]
     [?title-tag :hemlock/parent ?book]]
   books-db)

;; Some rules to make querying simpler
(def rules
  '[[[value ?book ?tag ?val]
     [?e :hemlock/parent ?book]
     [?e :hemlock/tag ?tag]
     [?e* :hemlock/parent ?e]
     [?e* :hemlock/text ?val]]
    [[price ?book ?price]
     [value ?book :price ?price*]
     [(read-string ?price*) ?price]]
    [[title ?book ?title]
     [value ?book :title ?title]]
    [[author ?book ?author]
     [value ?book :author ?author]]])

;; Same as above
(q '[:find ?title ?price
     :in $ %
     :where
     [title ?book ?title]
     [price ?book ?price]]
   books-db rules)

;; Find book by author
(q '[:find ?title
     :in $ % ?author
     :where
     [title ?book ?title]
     [author ?book ?author]]
   books-db rules "O'Brien, Tim")

;; Find title less than price
(q '[:find ?title
     :in $ % ?price
     :where
     [title ?book ?title]
     [price ?book ?p]
     [(< ?p ?price)]]
   books-db rules 5)