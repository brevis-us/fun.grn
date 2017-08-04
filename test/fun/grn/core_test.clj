(ns fun.grn.core-test
  (:require [clojure.test :refer :all]
            [brevis-utils.parameters :as params]
            [fun.grn.core :refer :all]))

(deftest initialize-test
  (testing "Initialization"
           (initialize-grneat)
           (is (not (nil? (params/get-param :grn-rng))))))


