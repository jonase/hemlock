(ns hemlock.utils
  (:require [datomic.api :as d]))

(defn empty-db
  "Creates an empty database value"
  [name schema]
  (let [uri (str "datomic:mem://" name)]
    (d/create-database uri)
    (let [conn (d/connect uri)]
      (d/transact conn schema)
      (d/db conn))))
