(ns practicalli.bank-account
  (:require [practicalli.bank-account-spec :as bank-account-spec]
            [clojure.spec.alpha :as spec]
            [clojure.spec.test.alpha :as spec-test]))



;; Public API - application logic
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn register-account-holder
  "Register a new customer with the bank
  Arguments:
  - hash-map of customer-details
  Return:
  - hash-map of an account-holder (adds account id)"
  [customer-details]

  (assoc customer-details
         :practicalli.bank-account-spec/account-id
         (java.util.UUID/randomUUID)))



(spec/fdef register-account-holder
  :args :practicalli.bank-account-spec/customer-details
  :ret :practicalli.bank-account-spec/account-holder)


(spec-test/instrument `register-account-holder)

;; Test data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment

  ;; failing specification test
  (register-account-holder {:first-name          "Jenny"
                            :last-name           "Jetpack"
                            :email-address       "jenny@jetpack.org"
                            :residential-address "42 meaning of life street"
                            :postal-code         "AB3 0EF"})

  (register-account-holder {:first-name          "Jenny"
                            :last-name           "Jetpack"
                            :email-address       "jenny@jetpack.org"
                            :residential-address "42 meaning of life street"
                            :postal-code         "AB3 0EF"
                            :social-security-id  "123456789"})



  ) ;; End of comment
