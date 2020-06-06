(ns practicalli.bank-account-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as spec]
            [practicalli.bank-account :as SUT]))

;; Mock data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def customer-mock
  {:firstname           "Jenny"
   :lastname            "Jetpack"
   :email-address       "jenny@jetpack.org"
   :residential-address "42 meaning of life street, Earth"
   :postal-code         "AB3 0EF"})

(def account-holder-mock
  {:account-id          #uuid "97bda55b-6175-4c39-9e04-7c0205c709dc"
   :firstname           "Jenny"
   :lastname            "Jetpack"
   :email-address       "jenny@jetpack.org"
   :residential-address "42 meaning of life street, Earth"
   :postal-code         "AB3 0EF"})

(deftest register-account-holder-test
  (testing "Basic registration - happy path"
    (is (= (set (keys (SUT/register-account-holder customer-mock)))
           (set (keys account-holder-mock))))

    (is (= (spec/valid? ::account-holder ))
    ))
