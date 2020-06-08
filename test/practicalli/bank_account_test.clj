(ns practicalli.bank-account-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as spec]
            [practicalli.bank-account :as SUT]))

;; Mock data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Using practicalli.bank-account-spec specifications
;; #:practicalli.bank-account-spec qualifies all keywords in the hash-map
;; so they are checked against the correct specification.

(def customer-mock
  #:practicalli.bank-account-spec
  {:first-name          "Jenny"
   :last-name           "Jetpack"
   :email-address       "jenny@jetpack.org"
   :residential-address "42 meaning of life street, Earth"
   :postal-code         "AB3 0EF"
   :social-security-id  "123456789"})


(def account-holder
  #:practicalli.bank-account-spec
  {:first-name          "Jenny"
   :last-name           "Jetpack"
   :email-address       "jenny@jetpack.org"
   :residential-address "42 meaning of life street, Earth"
   :postal-code         "AB3 0EF"
   :social-security-id  "123456789"
   :account-id          (java.util.UUID/randomUUID)})


;; Unit Tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(deftest register-account-holder-test
  (testing "Basic registration - happy path"
    (is (= (set (keys (SUT/register-account-holder customer-mock)))
           (set (keys account-holder))))

    (is (spec/valid? :practicalli.bank-account-spec/account-holder
                     (SUT/register-account-holder customer-mock) ) )
    ))
