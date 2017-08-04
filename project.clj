(defproject fun.grn "0.1.1"
  :description "Gene-regulatory networks in Clojure"
  :url "https://github.com/kephale/fun.grn"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-random "0.1.8"]
                 [brevis.us/brevis-utils "0.1.1"]
                 [us.brevis/GRNEAT "0.0.3"]]
  :plugins [[lein-localrepo "0.5.4"]]
  :repositories [["brevis-bintray" "https://dl.bintray.com/kephale/brevis"]
                 ["snapshots" {:url "https://clojars.org/repo"
                     :username :env/CI_DEPLOY_USERNAME
                     :password :env/CI_DEPLOY_PASSWORD
                     :sign-releases false}]
                 ["releases" {:url "https://clojars.org/repo"
                                     :username :env/CI_DEPLOY_USERNAME
                                     :password :env/CI_DEPLOY_PASSWORD
                                     :sign-releases false}]
                 ])
