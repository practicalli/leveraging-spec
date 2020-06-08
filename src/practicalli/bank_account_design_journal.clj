(ns practicalli.bank-account-design-journal
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.spec.alpha :as spec]
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
  {:first-name          "Jenny"
   :last-name           "Jetpack"
   :email-address       "jenny@jetpack.org"
   :residential-address "42 meaning of life street, Earth"
   :postal-code         "AB3 0EF"})

;; An account holder would have the same information, but also have a unquie id with the bank.


;;  Write skeleton of function for our test to call
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The skeleton takes the argument and returns it.
;; It is common for Clojure to take a map and return a map
;; so the signature of the function will probably remain unchanged,
;; with the body of the function updated

(defn register-account-holder
  "Register a new customer with the bank
  Arguments:
  - hash-map of customer-details
  Return:
  - hash-map of an account-holder (adds account id)"
  [customer-details]
  customer-details)


;; Write a test
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; A test to define what the register function should do

;; Including clojure.set to use its functions to define a test
(require '[clojure.test :refer [deftest is testing]])

;; Define some mock data
(def customer-mock
  {:first-name          "Jenny"
   :last-name           "Jetpack"
   :email-address       "jenny@jetpack.org"
   :residential-address "42 meaning of life street, Earth"
   :postal-code         "AB3 0EF"
   :social-security-id  "123456789"})

;; account is a customer with a bank account id added
(def account-holder-mock
  {:acount-id           #uuid "97bda55b-6175-4c39-9e04-7c0205c709dc"
   :first-name          "Jenny"
   :last-name           "Jetpack"
   :email-address       "jenny@jetpack.org"
   :residential-address "42 meaning of life street, Earth"
   :postal-code         "AB3 0EF"
   :social-security-id  "123456789"})

;; Write the first tests checking keys can be compared
#_(deftest register-account-holder-test
    (testing "Basic registration - happy path"
      (is (= (keys (register-account-holder customer-mock))
             (keys (customer-mock))))))

;; run the test and confirm its failing
;; fails because keys function returns a sequence of values
;; and even though sequences have the same values they may not be in the same order
;; so the logic of the test fails

;; convert the sequences returned by keys into sets and compare,
;; sets are unordered as its the values they contain that have significance

(deftest register-account-holder-test
  (testing "Basic registration - happy path"
    (is (= (set (keys (register-account-holder customer-mock)))
           (set (keys account-holder-mock))))))

;; run the tests
;; fails as the account-id is not added


;; Generating a random account number
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Java has a method on the UUID class to generate a random universally unique id
(java.util.UUID/randomUUID)
;; => #uuid "44f3ffd7-6702-4b8a-af25-11bee4b5ec4f"

;; A uuid is a string of numbers that has the #uuid tag as meta data,
;; adding to the meaning of the string.
;; http://practicalli.github.io/clojure/reference/tagged-literals/uuid.html

;; Using tag literals makes it simpler to check for types

(type (java.util.UUID/randomUUID))
;; => java.util.UUID

;; Test to see a value is a uuid
(uuid? (java.util.UUID/randomUUID))
;; => true

;; just putting the uuid tag in front of a string does not make a uuid,
;; the string needs to conform to the uuid type.
(uuid? #uuid " ")
;; Invalid uuid string


;; Update register-account-holder to return an id
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The register-account-holder takes a map of customer details
;; create a new hash-map from customer details, adding a new key for account id

(assoc {} :account-id (java.util.UUID/randomUUID))


(defn register-account-holder
  "Register a new customer with the bank
  Arguments:
  - hash-map of customer-details
  Return:
  - hash-map of an account-holder (adds account id)"

  [customer-details]

  (assoc customer-details :account-id (java.util.UUID/randomUUID)))

;; run tests
;; test passes


;; Create a spec for customer details
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Use a specific customer-details specification
;; which will be a required part of the account holder details

;; Customer details is a map, so the specification could just use the map? predicate
#_(spec/def ::customer-details map?)

;; However, the bank legally requires specific information about a customer
;; in order to add them as an account holder
;; so there is value in defining a specification for each part of the customer details hash-map

(spec/def ::first-name string?)
(spec/def ::last-name string?)

;; An email must be a string and match the email regex (copied from the internet)
(spec/def ::email-address
  (spec/and string?
            #(re-matches #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$"
                         %)))

;; A residential address is usually made of several pieces of information
;; so this could be a composite specification in future
;; Initially it is just a string of the whole address
(spec/def ::residential-address string?)

;; A social security number specification is also a candidate for a composite specification
;; Social security numbers may take different forms
;; and even have different names in different countries
;; eg. USA - SSN
;;  a nine-digit number in the format "AAA-GG-SSSS"
;; https://en.wikipedia.org/wiki/Social_Security_number

(spec/def ::social-security-id-usa
  (spec/and string?
            #(= 11 (count %))))

;; NOTE: https://github.com/nikortel/ssn is a library for USA specific
;; social-security number validation and generation via spec
;; HACK: project not on clojars, so could just be copied into project as a separate library

;; eg. UK - National Insurance QQ123456C
;; https://en.wikipedia.org/wiki/National_Insurance_number

(spec/def ::social-security-id-uk string?)


;; Social Security ID specification then can be one of any of the country specific specifications
(spec/def ::social-security-id (or ::social-security-id-uk
                                   ::social-security-id-usa))


;; Composing the customer details specification
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; A customer details specification is a hash-map of key value pairs,
;; The keys are the specifications that have just been defined.

;; Using `spec/keys` a specification is defined as a map with required and optional keys
#_(spec/def ::customer-details
    (spec/and
      map?
      (spec/keys
        :req [::first-name ::last-name ::email-address ::residential-address ::social-security-id])))


;; The `spec/keys` macro makes explicitly checking for a hash map redundant
(spec/def ::customer-details
  (spec/keys
    :req [::first-name ::last-name ::email-address ::residential-address ::social-security-id]))


;; Validating the customer details specifications
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Check an example hash-map from our test conforms to the specification

(spec/conform ::customer-details
              {:first-name          "Jenny"
               :last-name           "Jetpack"
               :email-address       "jenny@jetpack.org"
               :residential-address "42 meaning of life street"
               :postal-code         "AB3 0EF"
               :social-security-id  "123456789"})
;; => :clojure.spec.alpha/invalid

;; So the mock test data does not confirm to the specifications
;; even though it has all the same keys as the map in the specification

(spec/valid? ::customer-details
             {:first-name          "Jenny"
              :last-name           "Jetpack"
              :email-address       "jenny@jetpack.org"
              :residential-address "42 meaning of life street"
              :postal-code         "AB3 0EF"
              :social-security-id  "123456789"})
;; => false


;; `spec/explain` will provide more information to help diagnose the issue

(spec/explain ::customer-details
              {:first-name          "Jenny"
               :last-name           "Jetpack"
               :email-address       "jenny@jetpack.org"
               :residential-address "42 meaning of life street"
               :postal-code         "AB3 0EF"
               :social-security-id  "123456789"})

;; {:first-name "Jenny", :last-name "Jetpack", :email-address "jenny@jetpack.org", :residential-address "42 meaning of life street", :postal-code "AB3 0EF", :social-security-id "123456789"}
;; - failed: (contains? % :practicalli.bank-account-design-journal/first-name) spec: :practicalli.bank-account-design-journal/customer-details
;; {:first-name "Jenny", :last-name "Jetpack", :email-address "jenny@jetpack.org", :residential-address "42 meaning of life street", :postal-code "AB3 0EF", :social-security-id "123456789"}
;; - failed: (contains? % :practicalli.bank-account-design-journal/last-name) spec: :practicalli.bank-account-design-journal/customer-details
;; {:first-name "Jenny", :last-name "Jetpack", :email-address "jenny@jetpack.org", :residential-address "42 meaning of life street", :postal-code "AB3 0EF", :social-security-id "123456789"}
;; - failed: (contains? % :practicalli.bank-account-design-journal/email-address) spec: :practicalli.bank-account-design-journal/customer-details
;; {:first-name "Jenny", :last-name "Jetpack", :email-address "jenny@jetpack.org", :residential-address "42 meaning of life street", :postal-code "AB3 0EF", :social-security-id "123456789"}
;; - failed: (contains? % :practicalli.bank-account-design-journal/residential-address) spec: :practicalli.bank-account-design-journal/customer-details
;; {:first-name "Jenny", :last-name "Jetpack", :email-address "jenny@jetpack.org", :residential-address "42 meaning of life street", :postal-code "AB3 0EF", :social-security-id "123456789"}
;; - failed: (contains? % :practicalli.bank-account-design-journal/social-security-id) spec: :practicalli.bank-account-design-journal/customer-details


;; The `::customer-details` spec is given a map with unqualified keys
;; and is failing the `:req` part of the `spec/keys` part of the specification


;; Changing spec/keys to use unqualified keys
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; `spec/keys` has an unqualified version which will
;; allow you to use unqualified keys with specifications

;; Using qualified keywords in the map works in the relevant namespace

;; update the ::customer-details spec to use :req-un

(spec/def ::customer-details
  (spec/keys
    :req-un [::first-name ::last-name ::email-address ::residential-address ::social-security-id]))


;; Now the following will work from any namespace
(spec/valid? ::customer-details
             {:first-name          "Jenny"
              :last-name           "Jetpack"
              :email-address       "jenny@jetpack.org"
              :residential-address "42 meaning of life street"
              :postal-code         "AB3 0EF"
              :social-security-id  "123456789"})
;; => true


(spec/valid? ::customer-details
             {::first-name          "Jenny"
              ::last-name           "Jetpack"
              ::email-address       "jenny@jetpack.org"
              ::residential-address "42 meaning of life street"
              ::postal-code         "AB3 0EF"
              ::social-security-id  "123456789"})
;; => false

;; FAIL: using unqualified keys means that qualified keys will fail :()
;; so its an either or situation (unless there is another approach)


;; Qualifying keys with auto-resolve macro
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The auto-resolve macro will add the current namespace to all the keys in a hash-map

;; Change the `::customer-details` specification to use qualified keys again

(spec/def ::customer-details
  (spec/keys
    :req [::first-name ::last-name ::email-address ::residential-address ::social-security-id]))


(spec/conform ::customer-details
              #::{:first-name          "Jenny"
                  :last-name           "Jetpack"
                  :email-address       "jenny@jetpack.org"
                  :residential-address "42 meaning of life street"
                  :postal-code         "AB3 0EF"
                  :social-security-id  "123456789"}  )
;; => #:practicalli.bank-account-design-journal{:first-name "Jenny", :last-name "Jetpack", :email-address "jenny@jetpack.org", :residential-address "42 meaning of life street", :postal-code "AB3 0EF", :social-security-id "123456789"}

(spec/valid? ::customer-details
             #::{:first-name          "Jenny"
                 :last-name           "Jetpack"
                 :email-address       "jenny@jetpack.org"
                 :residential-address "42 meaning of life street"
                 :postal-code         "AB3 0EF"
                 :social-security-id  "123456789"}  )
;; => true


;; HACK: use both `req` and `:req-un` versions of `spec/keys`
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Although this approach works it feels a bit of a hack
;; and to me suggests there is something wrong elsewhere.
;; Each `spec/keys` expression is wrapped with `spec/or`.
;; so as long as either the right set of keys is passed
;; it does not matter if they are explicitly qualified.
;;`spec/or` requires labels for each possible branch.
;; labels are used to show which branch has failed

(spec/def ::customer-details
  (spec/or
    :qualified-keys
    (spec/keys
      :req [::first-name ::last-name ::email-address ::residential-address ::social-security-id])

    :unqualified-keys
    (spec/keys
      :req-un [::first-name ::last-name ::email-address ::residential-address ::social-security-id])))


;; With this (polymorphic ???) spec, both forms of example data
;; validate against the specification.

(spec/valid? ::customer-details
             {:first-name          "Jenny"
              :last-name           "Jetpack"
              :email-address       "jenny@jetpack.org"
              :residential-address "42 meaning of life street"
              :postal-code         "AB3 0EF"
              :social-security-id  "123456789"})
;; => true

(spec/valid? ::customer-details
             {::first-name          "Jenny"
              ::last-name           "Jetpack"
              ::email-address       "jenny@jetpack.org"
              ::residential-address "42 meaning of life street"
              ::postal-code         "AB3 0EF"
              ::social-security-id  "123456789"})
;; => true


;; This does feel like a HACK so am sticking with the qualified keys
;; and using auto-resolve macro where required.



;; Create spec for account-holder
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The account holder has the same information as custom details
;; with the addition of an account-id

;; In the register-account-holder a uuid is generated for the account id
;; So a spec can be defined for this type

(spec/def ::account-id uuid?)


;; There are several approaches to combining, depending on the shape of the data used

;; The account holder is a hash-map, so `spec/keys` will create the map from specification keys

;; Including the customer-details specification in `spec/keys`
;; would include the customer details as a nested hash-map

(spec/def ::account-holder-hierachy
  (spec/keys
    :req [::account-id ::customer-details]))


;; A valid data structure for this specification is a map with two keys
;; account-id and customer-details
;; account-id is a uuid value
;; customer-details is a hash-map of values that conform to the customer-details specification

(spec/valid? ::account-holder-hierachy
             #::{:account-id       (java.util.UUID/randomUUID)
                 :customer-details #:: {:first-name          "Jenny"
                                        :last-name           "Jetpack"
                                        :email-address       "jenny@jetpack.org"
                                        :residential-address "42 meaning of life street, Earth"
                                        :postal-code         "AB3 0EF"
                                        :social-security-id  "123456789"}})
;; => true


;; Flat data structures are usually preferred in Clojure over a nested hierarchy
;; Rather than use the ::customer-details specification as a key in the `spec/keys` expression
;; the individual specifications that make up ::customer-details can be used.

(spec/def ::account-holder-composition
  (spec/keys
    :req [::account-id ::first-name ::last-name ::email-address ::residential-address ::social-security-id]))


(spec/valid? ::account-holder-composition
             #::{:account-id          (java.util.UUID/randomUUID)
                 :first-name          "Jenny"
                 :last-name           "Jetpack"
                 :email-address       "jenny@jetpack.org"
                 :residential-address "42 meaning of life street, Earth"
                 :postal-code         "AB3 0EF"
                 :social-security-id  "123456789"})



;; Create a namespace to hold the specifications
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; practicalli.bank-account-spec

;; The spec definitions can simply be copied to this namespace
;; and they will use that namespace as their base domain



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
