(ns hemlock.core
  (:require [clojure.data.xml :as xml]
            [datomic.api :as d])
  (:import [clojure.data.xml Element]))

(defmulti emit (fn [o _ _ _] (class o)))
(defmulti emit-element (fn [e _ _ _] (:tag e)))

;; TODO
(defn emit-attributes [attr-map])

(defmethod emit Element [element pid idx tempid]
  (emit-element element pid idx tempid))

(defmethod emit String [string pid idx tempid]
  (let [id (tempid)]
    [[:db/add id :hemlock/text string]
     [:db/add id :hemlock/parent pid]
     [:db/add id :hemlock/idx idx]]))

(defmethod emit-element :default [element pid idx tempid]
  (let [id (tempid)
        tag (:tag element)
        attr (:attr element)
        children (:content element)
        child-count (count children)]
    (concat [[:db/add id :hemlock/tag tag]
             [:db/add id :hemlock/idx idx]
             [:db/add id :hemlock/child-count child-count]]
            (when pid [[:db/add id :hemlock/parent pid]])
            (emit-attributes attr)
            (mapcat emit children (repeat id) (range) (repeat tempid)))))

(defn emit-tx-data [element-root]
  (emit element-root nil 0 #(d/tempid :db.part/user)))

(def schema
  [{:db/id #db/id[:db.part/db]
    :db/ident :hemlock/tag
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :hemlock/parent
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :hemlock/idx
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :hemlock/child-count
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   
   {:db/id #db/id[:db.part/db]
    :db/ident :hemlock/text
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}])
