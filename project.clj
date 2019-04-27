(defproject us.brevis/fun.grn "0.1.2-SNAPSHOT"
  :description "Gene-regulatory networks in Clojure"
  :url "https://github.com/kephale/fun.grn"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-random "0.1.8"]
                 [us.brevis/brevis-utils "eb70f90d49"]
                 [us.brevis/grneat "2e68743ff7"]]
  :repositories [["brevis-bintray" "https://dl.bintray.com/kephale/brevis"]
                 ["jitpack" "https://jitpack.io"]
                 ["snapshots" {:url "https://clojars.org/repo"
                     :username :env/CI_DEPLOY_USERNAME
                     :password :env/CI_DEPLOY_PASSWORD
                     :sign-releases false}]
                 ["releases" {:url "https://clojars.org/repo"
                                     :username :env/CI_DEPLOY_USERNAME
                                     :password :env/CI_DEPLOY_PASSWORD
                                     :sign-releases false}]])
