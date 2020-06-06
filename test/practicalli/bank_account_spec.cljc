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


(spec/def ::customer-details
  (spec/keys
    :req [::first-name ::last-name ::email-address ::residential-address ::social-security-id]))


;; account-holder specification
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Specific information created when a customer registers with the bank

(spec/def ::account-id uuid?)

;; Combine ::account-id with the ::customer-details spec to make ::account-holder spec
(spec/def ::account-holder
  (spec/keys
    :req-un [::account-id ::customer-details]))




;; Function definition specifications
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(spec/fdef register-account-holder
  :args :practicalli.bank-account-spec/customer-details
  :ret :practicalli.bank-account-spec/account-id)



;; Instrument function definition specifications
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(spec-test/instrument `register-account-holder)







;; Test data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(spec/valid? ::first-name "Jenny")

(spec/valid? ::residential-address "42 meaning of life street")


(spec/valid? :practicalli.bank-account-spec/customer-details
             {:first-name          "Jenny"
              :last-name           "Jetpack"
              :email-address       "jenny@jetpack.org"
              :residential-address "42 meaning of life street"
              :postal-code         "AB3 0EF"
              :social-security-id  "123456789"}  )
;; => true

(spec/valid? :practicalli.bank-account-spec/customer-details
             {::first-name          "Jenny"
              ::last-name           "Jetpack"
              ::email-address       "jenny@jetpack.org"
              ::residential-address "42 meaning of life street"
              ::postal-code         "AB3 0EF"
              ::social-security-id  "123456789"}  )
;; => true

(spec/valid? :practicalli.bank-account-spec/customer-details
             #::{:first-name          "Jenny"
                 :last-name           "Jetpack"
                 :email-address       "jenny@jetpack.org"
                 :residential-address "42 meaning of life street"
                 :postal-code         "AB3 0EF"
                 :social-security-id  "123456789"}  )


(spec/explain :practicalli.bank-account-spec/customer-details
              {::first-name          "Jenny"
               ::last-name           "Jetpack"
               ::email-address       "jenny@jetpack.org"
               ::residential-address "42 meaning of life street"
               ::postal-code         "AB3 0EF"
               ::social-security-id  "123456789"}  )
;; Success!
