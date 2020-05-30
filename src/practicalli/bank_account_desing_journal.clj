(ns practicalli.bank-account-design-journal
  (:require [clojure.spec.alpha :as spec]))


;; Register with bank (user)
;; Open a bank account (user)
;; Make a payment (user)
;; Send account notification
;; Check for overdraft (system)


;; Using a specification for the argument and return value for a funciton
;; makes the documentation very explicit

(defn register-account-holder
  "Register a new customer with the bank
  Arguments:
  - :practicalli.bank-account-design-journal/customer-details specification
  Return:
  - :practicalli.bank-account-design-journal/account-holder specification"
  [customer-details]
  ;; Return a data structure that matches the ::account-holder specification
  )


;; Refactor the bank account specifications
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Use a specific customer-details specification
;; which will be a required part of the account holder details

(spec/def ::customer-details map?)

;; The bank legally requires specific information about a customer
;; in order to add them as an account holder

(spec/def ::first-name string?)
(spec/def ::last-name string?)

;; An email must be a string and match the email regex (copied from the internet)
(spec/def ::email-address
  (spec/and string?
            #(re-matches #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$"
                         %)))

;; These specs are not very useful - refactor
(spec/def ::home-address string?)
(spec/def ::social-secuirty-id string?)

;; NOTE: https://github.com/nikortel/ssn is a library for
;; social-security number validation and generation via spec

;; Social security numbers may take different forms
;; and even have different names in different countries
;; eg. USA - SSN
;;  a nine-digit number in the format "AAA-GG-SSSS"
;; https://en.wikipedia.org/wiki/Social_Security_number

(spec/def ::social-secuirty-id-usa string?)

;; eg. UK - National Insurance QQ123456C
;; https://en.wikipedia.org/wiki/National_Insurance_number

(spec/def ::social-secuirty-id-uk string?)

;; Social Security ID then can be one of these
(spec/def ::social-secuirty-id (or ::social-secuirty-id-uk
                                   ::social-secuirty-id-usa))


;; Putting that all together

(spec/def ::customer-details
  (spec/and
    map?
    (spec/keys
      :req [::first-name ::last-name ::email-address ::home-address ::social-secuirty-id])))


;; However, keys macro makes explicitly checking for a hash map redundant
(spec/def ::customer-details
  (spec/keys
    :req [::first-name ::last-name ::email-address ::home-address ::social-secuirty-id]))


;; Define a spec for a registered customer
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; This can use the custom details
;; and add specific information created when a customer is registered with the bank

(spec/def ::customer-id uuid?)

;; Or their could be a specific format the bank uses for customer id's

(defn super-secret-customer-id
  [customer-details]
  (java.util.UUID/randomUUID))

(java.util.UUID/randomUUID)
;; => #uuid "44f3ffd7-6702-4b8a-af25-11bee4b5ec4f"

(type (java.util.UUID/randomUUID))
;; => java.util.UUID

;; Test to see a value is a uuid

(uuid? (java.util.UUID/randomUUID))
;; => true


;; (uuid? #uuid " ")
;; Invalid uuid string
;; (type  #uuid " ")
;; Invalid uuid string
