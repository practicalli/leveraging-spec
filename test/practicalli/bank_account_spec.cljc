(ns practicalli.bank-account-spec
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.test.alpha :as spec-test]))


;; Customer details specification
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The bank legally requires specific information about a customer
;; in order to add them as an account holder

(spec/def ::first-name string?)
(spec/def ::last-name string?)
(spec/def ::residential-address string?)
(spec/def ::email-address
  (spec/and string?
            #(re-matches #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$"
                         %)))


;;  USA - SSN - a nine-digit number in the format "AAA-GG-SSSS"
;; https://en.wikipedia.org/wiki/Social_Security_number
(spec/def ::social-security-id-usa string?)

;; eg. UK - National Insurance QQ123456C
;; https://en.wikipedia.org/wiki/National_Insurance_number
(spec/def ::social-security-id-uk string?)

;; Social Security ID then can be one of these
(spec/def ::social-security-id (or ::social-security-id-uk
                                   ::social-security-id-usa))

;; Composition of the individual customer details into a hash-map
(spec/def ::customer-details
  (spec/keys
    :req [::first-name ::last-name ::email-address ::residential-address ::social-security-id]))


;; account-holder specification
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Specific information created when a customer registers with the bank

(spec/def ::account-id uuid?)

;; Combine ::account-id with the individual specifications that make up ::customer-details spec
;; creating a flat hash-map for an ::account-holder specification

(spec/def ::account-holder
  (spec/keys
    :req [::account-id ::first-name ::last-name ::email-address ::residential-address ::social-security-id]))


#_(spec/def ::account-holder-hierachy
    (spec/keys
      :req [::account-id ::customer-details]))




;; Function definition specifications
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(spec/fdef register-account-holder
  :args :practicalli.bank-account-spec/customer-details
  :ret :practicalli.bank-account-spec/account-id)



;; Instrument function definition specifications
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

#_(spec-test/instrument `register-account-holder)







;; Test data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment

  ;; auto-resolution of keyworks only works in the
  ;; practicalli.bank-account-spec namespace

  (spec/valid? ::first-name "Jenny")
  ;; => true

  (spec/valid? ::residential-address "42 meaning of life street")
  ;; => true


  (spec/valid? ::customer-details
               #::{:first-name          "Jenny"
                   :last-name           "Jetpack"
                   :email-address       "jenny@jetpack.org"
                   :residential-address "42 meaning of life street"
                   :postal-code         "AB3 0EF"
                   :social-security-id  "123456789"}  )
  ;; => true

  (spec/valid? ::customer-details
               {::first-name          "Jenny"
                ::last-name           "Jetpack"
                ::email-address       "jenny@jetpack.org"
                ::residential-address "42 meaning of life street"
                ::postal-code         "AB3 0EF"
                ::social-security-id  "123456789"}  )
  ;; => true


  (spec/valid? ::customer-details
               #::{:first-name          "Jenny"
                   :last-name           "Jetpack"
                   :email-address       "jenny@jetpack.org"
                   :residential-address "42 meaning of life street"
                   :postal-code         "AB3 0EF"
                   :social-security-id  "123456789"}  )
;; => true


  ;; only works in the practicalli.bank-account-spec namespace
  (spec/explain ::customer-details
                {::first-name          "Jenny"
                 ::last-name           "Jetpack"
                 ::email-address       "jenny@jetpack.org"
                 ::residential-address "42 meaning of life street"
                 ::postal-code         "AB3 0EF"
                 ::social-security-id  "123456789"}  )
;; => nil


  (spec/valid? ::account-holdera
               #:practicalli.bank-account-spec
               {:first-name          "Jenny"
                :last-name           "Jetpack"
                :email-address       "jenny@jetpack.org"
                :residential-address "42 meaning of life street, Earth"
                :postal-code         "AB3 0EF"
                :social-security-id  "123456789"
                :account-id          (java.util.UUID/randomUUID)}
               )
  ;; => false
  ;; customer-details should be a key of account holder and values should be a nested map of the customer details

  (spec/valid? ::account-holder
               #:practicalli.bank-account-spec
               {:account-id       (java.util.UUID/randomUUID)
                :customer-details {:first-name          "Jenny"
                                   :last-name           "Jetpack"
                                   :email-address       "jenny@jetpack.org"
                                   :residential-address "42 meaning of life street, Earth"
                                   :postal-code         "AB3 0EF"
                                   :social-security-id  "123456789"}})
;; => false

  ;; need to qualify the customer details hash-map
  (spec/explain ::account-holder
                #:practicalli.bank-account-spec
                {:account-id       (java.util.UUID/randomUUID)
                 :customer-details {:first-name          "Jenny"
                                    :last-name           "Jetpack"
                                    :email-address       "jenny@jetpack.org"
                                    :residential-address "42 meaning of life street, Earth"
                                    :postal-code         "AB3 0EF"
                                    :social-security-id  "123456789"}})

  (spec/valid? ::account-holder
               #::{:account-id       (java.util.UUID/randomUUID)
                   :customer-details #:: {:first-name          "Jenny"
                                          :last-name           "Jetpack"
                                          :email-address       "jenny@jetpack.org"
                                          :residential-address "42 meaning of life street, Earth"
                                          :postal-code         "AB3 0EF"
                                          :social-security-id  "123456789"
                                          }})
;; => true

  (spec/explain ::account-holder
                #::{:account-id       (java.util.UUID/randomUUID)
                    :customer-details #:: {:first-name          "Jenny"
                                           :last-name           "Jetpack"
                                           :email-address       "jenny@jetpack.org"
                                           :residential-address "42 meaning of life street, Earth"
                                           :postal-code         "AB3 0EF"
                                           :social-security-id  "123456789"
                                           }})
;; => Success (in REPL buffer)



  ) ;; End of comment
