(ns datomic-tuto.core
  (:require [datomic.client.api :as d]))


(def cfg {:server-type :peer-server
          :access-key "myaccesskey"
          :secret "mysecret"
          :endpoint "localhost:8998"
          :validate-hostnames false})

(def client (d/client cfg))

(def conn (d/connect client {:db-name "hello"}))


(def strive-schema [{:db/ident :workspace/name
                     :db/valueType :db.type/string
                     :db/cardinality :db.cardinality/one
                     :db/doc "The name of the workspace"}

                    {:db/ident :user/email
                     :db/valueType :db.type/string
                     :db/cardinality :db.cardinality/one
                     :db/doc "The email of the user"}

                    {:db/ident :user-tenant-permissions/user
                     :db/valueType :db.type/ref
                     :db/cardinality :db.cardinality/one
                     :db/doc "The ID of the user that has permissions"}
                    
                    {:db/ident :user-tenant-permissions/workspace
                     :db/valueType :db.type/ref
                     :db/cardinality :db.cardinality/one
                     :db/doc "The ID of the workspace that is granted permissions"}])


(d/transact conn {:tx-data strive-schema})

(def workspaces [{:workspace/name "My First Workspace"}
                 {:user/email "joe@doe.com"}])

(d/transact conn {:tx-data workspaces})

(def db (d/db conn))


(def user-ids (->>
                (d/q '[:find ?user
                       :where [?user :user/email]]
                     db)
                (map first)))

(def a-user-id (first user-ids))

(def workspace-ids (->>
                (d/q '[:find ?workspace
                       :where [?workspace :workspace/name]]
                     db)
                (map first)))

(def a-workspace-id (first workspace-ids))

(def permissions [{:user-tenant-permissions/workspace a-workspace-id
                   :user-tenant-permissions/user a-user-id}])

(d/transact conn {:tx-data permissions})


;; WARNING: We must create a new db snapshot
;; db is immutable!
(def db (d/db conn))

(d/q '[:find ?email
       :where [?permission :user-tenant-permissions/user ?user]
       [?workspace :workspace/name "My First Workspace"]
       [?permission :user-tenant-permissions/workspace ?workspace]
       [?user :user/email ?email]]
     db)



