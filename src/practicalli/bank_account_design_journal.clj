(ns practicalli.bank-account-design-journal
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.test.alpha :as spec-test]
            [practicalli.bank-account]))


;; Functions for the online banking system
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Register with bank (user)
;; Open a bank account (user)
;; Make a payment (user)
;; Send account notification
;; Check for overdraft (system)


;; Define what a customer looks like
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; A customer will need several pieces of information to create a bank account
(def customer-mock
  {:firstname           "Jenny"
   :lastname            "Jetpack"
   :email-address       "jenny@jetpack.org"
   :residential-address "42 meaning of life street, Earth"
   :postal-code         "AB3 0EF"})

;; An account holder would have the same information, but also have a unquie id with the bank.

;; Generating a random account number
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; http://practicalli.github.io/clojure/reference/tagged-literals/uuid.html

(java.util.UUID/randomUUID)
;; => #uuid "44f3ffd7-6702-4b8a-af25-11bee4b5ec4f"


;; A uuid is a string of numbers that has the #uuid tag as meta data,
;; adding to the meaning of the string.


;; Write a test (TDD style)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; First we would write a test to define what the register function should do

;; Including clojure.set to use its functions to define a test
(require '[clojure.test :refer [deftest is testing]])

;; Define some mock data
(def customer-mock
  {:firstname           "Jenny"
   :lastname            "Jetpack"
   :email-address       "jenny@jetpack.org"
   :residential-address "42 meaning of life street, Earth"
   :postal-code         "AB3 0EF"})

(def account-holder-mock
  {:acount-id           #uuid "97bda55b-6175-4c39-9e04-7c0205c709dc"
   :firstname           "Jenny"
   :lastname            "Jetpack"
   :email-address       "jenny@jetpack.org"
   :residential-address "42 meaning of life street, Earth"
   :postal-code         "AB3 0EF"})

;; Write our first tests (which will fail at first)

(deftest register-account-holder-test
  (testing "Basic registration - happy path"
    (is (= (keys (register-account-holder customer-mock))
           (keys (customer-mock))))))


;;  Write skeleton of function for our test to call
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The skeleton takes the argument and returns it.

(defn register-account-holder
  "Register a new customer with the bank
  Arguments:
  - hash-map of customer-details
  Return:
  - hash-map of an account-holder (adds account id)"
  [customer-details]
  customer-details)

;; run the test and confirm its failing


;; Define function with a spec in mind
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Using a specification for the argument and return value for a funciton
;; makes the documentation very explicit

;; run the tests
;; fails as the account-id is not added

;; update the function to assoc an account id, for now we will just use a random number
(rand-int 1000000)

;; of the form, but using the customer-details as the hash-map
(assoc {} :account-id (rand-int 1000000))


(defn register-account-holder
  "Register a new customer with the bank
  "
  [customer-details]
  ;; Return a data structure that matches the ::account-holder specification
  (assoc customer-details :account-id (rand-int 1000000))
  )


;; run tests
;; fails because lists have the same values but not in the same sequence

;; convert lists into sets and compare, sets are unordered

(deftest register-account-holder-test
  (testing "Basic registration - happy path"
    (is (= (set (keys (register-account-holder customer-mock)))
           (set (keys account-holder-mock))))))

;; run tests
;; test passes


;; Create a spec for customer details
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
(spec/def ::residential-address string?)
(spec/def ::social-secuirty-id string?)

;; NOTE: https://github.com/nikortel/ssn is a library for
;; social-security number validation and generation via spec
;; HACK: project not on clojars, so could just be copied into project as a separate library

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





;; Create spec for account-holder
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; This can use the custom details
;; and add specific information created when a customer is registered with the bank

(spec/def ::account-id uuid?)

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

(uuid? #uuid " ")
;; Invalid uuid string

;; Combine ::account-id with the ::customer-details spec to make ::account-holder spec

(spec/def ::account-holder
  (spec/keys
    :req [::account-id ::customer-details]))


;; Create a namespace to hold the specifications
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; practicalli.bank-account-spec

;; The spec definitions can simply be copied to this namespace
;; and they will use that namespace as their base domain


;; Validating specifications
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; The specification for customer details from practicalli.bank-account-spec namespace
#_(spec/def ::customer-details
    (spec/keys
      :req [::first-name ::last-name ::email-address ::residential-address ::social-security-id]))

(spec/conform :practicalli.bank-account-spec/customer-details
              {:first-name          "Jenny"
               :last-name           "Jetpack"
               :email-address       "jenny@jetpack.org"
               :residential-address "42 meaning of life street"
               :postal-code         "AB3 0EF"
               :social-security-id  "123456789"}  )
;; => :clojure.spec.alpha/invalid

(spec/explain :practicalli.bank-account-spec/customer-details
              {:first-name          "Jenny"
               :last-name           "Jetpack"
               :email-address       "jenny@jetpack.org"
               :residential-address "42 meaning of life street"
               :postal-code         "AB3 0EF"
               :social-security-id  "123456789"}  )

;; {:first-name "Jenny", :last-name "Jetpack", :email-address "jenny@jetpack.org", :residential-address "42 meaning of life street", :postal-code "AB3 0EF", :social-security-id"123456789"}
;; - failed: (contains? % :practicalli.bank-account-spec/first-name) spec: :practicalli.bank-account-spec/customer-details
;; {:first-name "Jenny", :last-name "Jetpack", :email-address "jenny@jetpack.org", :residential-address "42 meaning of life street", :postal-code "AB3 0EF", :social-security-id"123456789"}
;; - failed: (contains? % :practicalli.bank-account-spec/last-name) spec: :practicalli.bank-account-spec/customer-details
;; {:first-name "Jenny", :last-name "Jetpack", :email-address "jenny@jetpack.org", :residential-address "42 meaning of life street", :postal-code "AB3 0EF", :social-security-id"123456789"}
;; - failed: (contains? % :practicalli.bank-account-spec/email-address) spec: :practicalli.bank-account-spec/customer-details
;; {:first-name "Jenny", :last-name "Jetpack", :email-address "jenny@jetpack.org", :residential-address "42 meaning of life street", :postal-code "AB3 0EF", :social-security-id "123456789"}
;; - failed: (contains? % :practicalli.bank-account-spec/residential-address) spec: :practicalli.bank-account-spec/customer-details
;; {:first-name "Jenny", :last-name "Jetpack", :email-address "jenny@jetpack.org", :residential-address "42 meaning of life street", :postal-code "AB3 0EF", :social-security-id "123456789"}
;; - failed: (contains? % :practicalli.bank-account-spec/social-security-id) spec: :practicalli.bank-account-spec/customer-details



;; Using qualified keywords in the map works in the relevant namespace
;; the following works in the practicalli.bank-account-spec namespace

(spec/explain :practicalli.bank-account-spec/customer-details
              {::first-name            "Jenny"
               ::last-name             "Jetpack"
               ::email-address         "jenny@jetpack.org"
               ::residential-address   "42 meaning of life street"
               ::postal-code           "AB3 0EF"
               ::social-security-id-uk "123456789"}  )


;; Changing spec/keys to use unqualified keys
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; spec/keys has an unqualified version which will
;; allow you to connect unqualified keys to specs

;; update the ::customer-details spec to use :req-un
(spec/def ::customer-details
  (spec/keys
    :req-un [::first-name ::last-name ::email-address ::residential-address ::social-security-id]))


;; Now the following will work from any namespace
(spec/explain :practicalli.bank-account-spec/customer-details
              {:first-name            "Jenny"
               :last-name             "Jetpack"
               :email-address         "jenny@jetpack.org"
               :residential-address   "42 meaning of life street"
               :postal-code           "AB3 0EF"
               :social-security-id-uk "123456789"}  )


;; FAIL: using unqualified keys means that qualified keys will fail :()
;; so its an either or situation (unless there is another approach)

;; The auto-resolve macro can be used

(spec/explain :practicalli.bank-account-spec/customer-details
              #::{:first-name          "Jenny"
                  :last-name           "Jetpack"
                  :email-address       "jenny@jetpack.org"
                  :residential-address "42 meaning of life street"
                  :postal-code         "AB3 0EF"
                  :social-security-id  "123456789"}  )

(spec/explain :practicalli.bank-account-spec/customer-details
              #:practicalli.bank-account-spec
              {:first-name          "Jenny"
               :last-name           "Jetpack"
               :email-address       "jenny@jetpack.org"
               :residential-address "42 meaning of life street"
               :postal-code         "AB3 0EF"
               :social-security-id  "123456789"}  )


;; Or what seems to be more hacky is to have both `req` and `:req-un` in the spec/keys
(spec/def ::customer-details
  (or (spec/keys
        :req [::first-name ::last-name ::email-address ::residential-address ::social-security-id])
      (spec/keys
        :req-un [::first-name ::last-name ::email-address ::residential-address ::social-security-id])))


;; Create a spec fdef for the register-account-holder function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(spec/fdef register-account-holder
  :args :practicalli.bank-account-spec/customer-details
  :ret :practicalli.bank-account-spec/account-holder)


;; ?? what :fn should be defined to express the relationship between these two


;; View the documentation of the function

(require '[clojure.repl])
(clojure.repl/doc register-account-holder)



;; Instrument the function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Testing function calls against the specification

;; Requires the spec test namespace
;; (require '[clojure.spec.test.alpha :as spec-test])

;; Instrument the spec to add checking
;; this only checks the arguments are correct.

;; ?? Where should we instrument functions?

(spec-test/instrument `practicalli.bank-account/register-account-holder)


(register-account-holder {:first-name          "Jenny"
                          :last-name           "Jetpack"
                          :email-address       "jenny@jetpack.org"
                          :residential-address "42 meaning of life street"
                          :postal-code         "AB3 0EF"
                          :social-security-id  "123456789"})



;; Generate test data

;; done
