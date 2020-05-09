(ns practicalli.leveraging-spec
  (:require [clojure.spec.alpha :as spec]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; What is clojure.spec
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; https://clojure.org/about/spec


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Example uses of clojure.spec
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; API requests (schema is often used here, but so can spec)
;; Checking data pulled from / pushed to message systems (e.g. Kafka, TIBCO)
;; Data specifications (eg. Vega-lite)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Getting Started
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; https://clojure.org/guides/spec
;; - a comprehensive introduction to clojure.
;; Start with some simple specifications

;; Predicates
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Does a function call with the value return true or false?
;; Any existing Clojure function that takes a single argument
;; and returns a truthy value is a valid predicate spec.

(odd? 1)
(string? "am i a string")
(int? 2.3)
(type 2.3)


;; Does a value conform to a spec
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; conform takes two arguments
;; - a specification
;; - a value

(spec/conform odd? 101)
;; => 101

;; If the value conforms to the spec,
;; a conformed value is returned
;; The conformed value is the original value (in these simple examples)

(spec/conform even? 101)
;; => :clojure.spec.alpha/invalid

;; When a value does not conform to a spec,
;; the value `:clojure.spec.alpha/invalid` is returned
;; This specific value shows that the value deviates from the spec.

(spec/conform integer? 1)
;; => 1

(spec/conform seq? [1 2 3])
;; => :clojure.spec.alpha/invalid

(spec/conform seq? (range 10))
;; => (0 1 2 3 4 5 6 7 8 9)

(spec/conform map? {})
;; => {}

(spec/conform map? (hash-map :a 1 :b 2))
;; => {:b 2, :a 1}



;; Is the value valid?
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; spec/valid also checks a value against a specification
;; returning true or false
;; rather than :clojure.spec.alpha/invalid

(spec/valid? even? 180)

(spec/valid? string? "Am I a valid string")
;; => true


;; using custom predicate functions

(spec/valid? (fn [value] (> value 10000)) 30076)

(spec/valid? #(> % 10000) 30076)

(spec/conform #(> % 10000) 30076)



;; Down the rabbit hole
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(import java.util.Date)
;; Does the value satisfy the Inst protocol
(spec/valid? inst? (Date.))

;; Another way to check which things satisfy the Inst protocol
(-> Inst :impls keys)
(keys (:impls Inst))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; Literal values
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Sets can be used as predicate functions
;; returning true if the value is within the set

;; Checking valid playing cards

(spec/valid? #{:club :diamond :heart :spade} :club) ;; true
(spec/valid? #{:club :diamond :heart :spade} 42) ;; false

;; Answer to the ultimate question?

(spec/valid? #{42} 42)


;; We have previously seen the `some` function comparing sets
(some #{:club :diamond :heart :spade} #{:sheild})


;; Question: when use valid? rather than conform?


;; Using a Registry for unique and re-usable specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; So far we have just use predicate functions directly in the code examples.

;; Using a registry, specs can be uniquely defined across the whole project
;; Defining a spec gives that spec a name that has a fully qualified namespace


;; Use the spec specific `def` function to bind a new spec name
;; and fully qualified namespace
;; and place it in the registry

(spec/def :playing-card/suit  #{:club :diamond :heart :spade} )

(spec/conform :playing-card/suit :diamond)

:namespace/keyword

(spec/def ::cat-bread #{:abyssinian :birman :chartreau :devon-rex
                        :domestic-short-hair :domestic-long-hair})

;; NOTE: define specs in an appropriate namespace
;; and use `::` to auto-resolve a keywords fully qualified namespace
;; using the current namespace.
;; The `::` notation can also make a refactor of specs easy,
;; as specs can be moved to a different namespace by saving them
;; in the appropriate source code file.

;; spec namespaces typically include the project name, url,
;; or organization to avoid name conflicts.
;; This is especially important for libraries.


;; clojure.spec documents your code
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Using doc to show the spec

(require '[clojure.repl])

(clojure.repl/doc map)

(clojure.repl/doc :playing-card/suit)
;; prints in the REPL buffer:
;; :playing-card/suit
;; Spec
;; #{:spade :heart :diamond :club}

(clojure.repl/doc ::cat-bread)


;; Removing specs from the registry
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(spec/def ::unwanted #{:abandoned})


(spec/conform ::unwanted :abandoned)

;; remove a spec from the registry by binding it to nil
(spec/def ::unwanted nil)

(spec/conform ::unwanted :abandoned)

;; ​(spec/unform (spec/conform :abandoned))



;; Discussion: Where to save spec code?
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; One of the interesting discussions is which file to save your spec definitions too.
;; Are they part of the test namespaces, the application namespaces,
;; or should they have their own.

;; If there are only a few definitions, it seems to make sense to include them
;; in the application code.

;; If you are doing generative testing, then there is a case for adding specs
;; to the testing namespaces.

;; I like the idea of specs having their own namespace though,
;; especially if used extensively.
;; Perhaps adding relative conform / valid? examples to each namespace

;; More thought required on this...



;; Composing specifications
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; No spec is an island


;; `and` and `or` macros can be used to compose specs

;;clojure.core
(and false true false)
(or true false)

;; When multiple specs should be true
(spec/def ::meaning-of-life
  (spec/and int?
            even?
            #(= 42 %)))


;; or for when at least one spec should be true

(spec/def ::meaning-of-life-int-or-string
  (spec/or :integer #(= 42 %)
           :string  #(= "forty two" %)))

;; Each condition in the spec is annotated with a tag
;; tags give each conditional branch names
;; those are part of the return value from conform and other spec functions
;; providing context as to why a value passed the spec.

;; When an or is conformed, it returns a vector with the condition name and conformed value:

(spec/conform ::meaning-of-life-int-or-string 42)

(spec/conform ::meaning-of-life-int-or-string "forty two")


(spec/conform ::meaning-of-life-int-or-string :entropy)



;; What about nil values
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Some predicates do not consider `nil` a valid value
;; typically those predicates that check for a specific type

;; spec/nilable will transform a predicate to use nil

(spec/valid? string? nil)

(type "what type am I")
(type nil)

(spec/valid? (spec/nilable string?) nil)


;; What if my values fail to conform to the spec ?
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; spec/explain will show why a value does not conform to a spec
;; sends explination to system out (REPL, command line)

(spec/explain ::meaning-of-life 24)
;; 24 - failed: (= 42 %) spec: :practicalli.leveraging-spec/meaning-of-life

;; In this case explain returned the value being checked against the spec
;; The result of that check (failed)
;; The predicate used to check the value
;; The spec name used to check the value

;; Notice that the value failed on the first condition and stopped
;; this suggests the `spec/and` macro works the same as `clojure.core/and`
;; in that is stops as soon as something fails


(spec/explain ::meaning-of-life-int-or-string 24)
;; 24 - failed: (= 42 %) at: [:integer] spec: :practicalli.leveraging-spec/meaning-of-life-int-or-string
;; 24 - failed: (= "forty two" %) at: [:string] spec: :practicalli.leveraging-spec/meaning-of-life-int-or-string

;; In this case we still have the value checked, the result and the predicate
;; More information is provided as to where in the spec the value failed
;; :at shows the path in the spec where the failure occurred, very useful for nested structures
;; This shows the value of naming your specs descriptively


;; rather than send information to the system out


(spec/explain-str ::meaning-of-life 24)
;; => "24 - failed: (= 42 %) spec: :practicalli.leveraging-spec/meaning-of-life\n"



(spec/explain-data ::meaning-of-life 24)
;; => #:clojure.spec.alpha{:problems [{:path [], :pred (clojure.core/fn [%] (clojure.core/= 42 %)), :val 24, :via [:practicalli.leveraging-spec/meaning-of-life], :in []}], :spec :practicalli.leveraging-spec/meaning-of-life, :value 24}


;; using pretty print is more useful for humans though :)
(spec/explain-data ::meaning-of-life 24)
;; => #:clojure.spec.alpha{:problems
;;                         [{:path [],
;;                           :pred (clojure.core/fn [%] (clojure.core/= 42 %)),
;;                           :val  24,
;;                           :via  [:practicalli.leveraging-spec/meaning-of-life],
;;                           :in   []}],
;;                         :spec  :practicalli.leveraging-spec/meaning-of-life,
;;                         :value 24}



;; Map literal syntax - `#:` and `#::`
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; `#:` prefix for maps adds the fully qualified namespace to all the keys in the map

#:clojure.spec.alpha{:problems
                     [{:path [],
                       :pred (clojure.core/fn [%] (clojure.core/= 42 %)),
                       :val  24,
                       :via  [:practicalli.leveraging-spec/meaning-of-life],
                       :in   []}],
                     :spec  :practicalli.leveraging-spec/meaning-of-life,
                     :value 24}


;; means the same as

{:clojure.spec.alpha/problems
 [{:clojure.spec.alpha/path []
   :clojure.spec.alpha/pred (clojure.core/fn [%] (clojure.core/= 42 %))
   :clojure.spec.alpha/val  24
   :clojure.spec.alpha/via  []
   :clojure.spec.alpha/in   []}]
 :clojure.spec.alpha/spec  :spec-name
 :clojure.spec.alpha/value 24}



;; using the autoresolve version of the map tag literal
;; uses the fully qualified name from the current namespace
;; practicalli.leveraging-spec

#::{:simplyfying      []
    :keyword-names    []
    :with-autoresolve []
    :map-literal      []}

;; Is the same as

{:practicalli.leveraging-spec/simplyfying      []
 :practicalli.leveraging-spec/keyword-names    []
 :practicalli.leveraging-spec/with-autoresolve []
 :practicalli.leveraging-spec/map-literal      []}

;; This will be more relevant when looking at Entity maps with spec.


;; Entity maps
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Using Clojure hash-maps is a highly common approach to passing around data


;; Example: covid19-dashboard
;; The `coronavirus-cases-data` function was changed to take a hash-map of values
;; to make that function easier to extend without breaking existing calls
;; Default values can be used if no value is passed as an argument
;; Extra values can be ignored
;; All this without breaking the code

(defn fun-name
  [csv location date])

(defn coronavirus-cases-data
  "Extract and transform cases data for specific locations and date"
  [{:keys [csv-file locations date]}]
  #_(-> (extract-data-from-csv csv-file)
        (data-set-remove-locations locations)
        (data-set-specific-date date)))

(coronavirus-cases-data
  {:csv-file  "data-sets/uk-coronavirus-cases.csv"
   :locations #{"Nation" "Country" "Region"}
   :date      "2020-04-30"})



;; spec takes individual attributes and assembles them into a spec for data structures
;; individual specs are usable in any larger specifications


;; Define a spec for an online back account
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Designing a spec outside-in

;; Users are referred to as account-holders, so lets define that specification
;; each account holder has mandatory information

(spec/def ::account-holder
  (spec/keys :req [::account-id ::first-name ::last-name ::email-address ::home-address ::social-secuirty-id]
             :opt [::accounts-associated]))


(spec/def ::account-id uuid?)
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

;; Optionally the account holder can have accounts associated to them.
;; A bank account has multiple attributes also
;; using spec/keys we can say all associated accounts must be a bank accounts

(spec/def ::accounts-associated
  (spec/keys :req [::bank-account]))


;; Define what makes up a bank account, for now there is only one type of account.

(spec/def ::bank-account
  (spec/keys :req [::bank-account-id
                   ::account-balance
                   ::account-status
                   ::arranged-overdraft]
             :opt [::bank-account-alerts]))


;; This spec could be extended to cover different types of accounts
;; eg. mortgages, loans, savings, current, ISA (and all their variations), etc.


;; So now we need to specify what the component parts of a bank are


(spec/def ::bank-account-id uuid?)
(spec/def ::account-balance number?)
(spec/def ::account-status #{:credit :overdrawn})
(spec/def ::arranged-overdraft (spec/and int? #(> 1000 %)))
(spec/def ::bank-account-alerts #{:yes :warnings-only :no})


;; NOTE: The spec should be written in reverse order in the clojure source file
;; but we can evaluate in reverse to test it.



;; Testing the specification

(spec/valid? ::account-holder
             {::first-name "John"
              ::last-name  "Practicalli"
              ::email      "nospam@practicalli.spm"})


(spec/conform ::account-holder
              {::first-name "John"
               ::last-name  "Practicalli"
               ::email      "nospam@practicalli.spm"})


;; Investigate what the error is with explain

(spec/explain ::account-holder
              {::first-name "John"
               ::last-name  "Practicalli"
               ::email      "nospam@practicalli.spm"})

;; #:practicalli.leveraging-spec{:first-name "John", :last-name "Practicalli", :email "nospam@practicalli.spm"} - failed: (contains? % :practicalli.leveraging-spec/social-secuirty-id) spec: :practicalli.leveraging-spec/account-holder


;; Its less obvious to human eyes what has failed with explain-data though

(spec/explain-data ::account-holder
                   {::first-name "John"
                    ::last-name  "Practicalli"
                    ::email      "nospam@practicalli.spm"})
;; => #:clojure.spec.alpha{:problems
;;                         ({:path [],
;;                           :pred
;;                           (clojure.core/fn
;;                            [%]
;;                            (clojure.core/contains?
;;                             %
;;                             :practicalli.leveraging-spec/email-address)),
;;                           :val
;;                           #:practicalli.leveraging-spec{:first-name "John",
;;                                                         :last-name "Practicalli",
;;                                                         :email
;;                                                         "nospam@practicalli.spm"},
;;                           :via [:practicalli.leveraging-spec/account-holder],
;;                           :in []}
;;                          {:path [],
;;                           :pred
;;                           (clojure.core/fn
;;                            [%]
;;                            (clojure.core/contains?
;;                             %
;;                             :practicalli.leveraging-spec/home-address)),
;;                           :val
;;                           #:practicalli.leveraging-spec{:first-name "John",
;;                                                         :last-name "Practicalli",
;;                                                         :email
;;                                                         "nospam@practicalli.spm"},
;;                           :via [:practicalli.leveraging-spec/account-holder],
;;                           :in []}
;;                          {:path [],
;;                           :pred
;;                           (clojure.core/fn
;;                            [%]
;;                            (clojure.core/contains?
;;                             %
;;                             :practicalli.leveraging-spec/social-secuirty-id)),
;;                           :val
;;                           #:practicalli.leveraging-spec{:first-name "John",
;;                                                         :last-name "Practicalli",
;;                                                         :email
;;                                                         "nospam@practicalli.spm"},
;;                           :via [:practicalli.leveraging-spec/account-holder],
;;                           :in []}),
;;                         :spec :practicalli.leveraging-spec/account-holder,
;;                         :value
;;                         #:practicalli.leveraging-spec{:first-name "John",
;;                                                       :last-name "Practicalli",
;;                                                       :email
;;                                                       "nospam@practicalli.spm"}}







;; Experimenting with card game decks and spec
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Representing different aspects of card game decks

;; Suits from different regions are called by different names
;; There are 4 suits in a card deck

(spec/def ::suits-french #{:hearts :tiles :clovers :pikes})
(spec/def ::suits-german #{:hearts :bells :acorns :leaves})
(spec/def ::suits-spanish #{:cups :coins :clubs :swords})
(spec/def ::suits-italian #{:cups :coins :clubs :swords})
(spec/def ::suits-swiss-german #{:roses :bells :acorns :shields})


;; check if a deck contains 4 suits


;; check if a deck is one of the regions above



;; Info: Jack queen king are called face cards (USA) or court cards (UK)
:face-cards
:court-cards

;; Each suit in the deck has the same rank of cards
;; explicitly defining a rank
(spec/def ::rank #{:ace 2 3 4 5 6 7 8 9 10 :jack :queen :king})


;; rank can be defined more succinctly with the range function
(spec/def ::rank (into #{:ace :jack :queen :king} (range 2 11)))

(clojure.repl/doc ::rank)
;; :practicalli.leveraging-spec/rank
;; Spec
;; (into #{:king :queen :ace :jack} (range 2 11))

(into #{:ace :jack :queen :king} (range 2 11))
;; => #{7 :king 4 :queen :ace 6 3 2 :jack 9 5 10 8}













;; Example: Oz data structures for Vega-lite
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Ensure the relevant sections are included
;; Define what sections are optional
;; Check the structure of GeoJSON files ?
