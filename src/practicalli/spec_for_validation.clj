(ns practicalli.spec-for-validation
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.test.alpha :as spec-test]))


;; clojure specifications can be used to validate runtime data

;; A simple approach is to call valid on a function pre or post condition.


;; Function pre and post conditions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; https://clojure.org/reference/special_forms#_fn_name_param_condition_map_expr
;; A function definition, `fn`, has the form

;; (fn name? [param*] condition-map? expr*)   - not polymorphic
;; (fn name? ([param*] condition-map? expr*)) - polymorphic

;; name? is the name of the function within the scope of fn (? means optional)
;; param* are parameters the function requires (* means zero to many)
;; - params are in brackets as functions can be polymorphic (numbers of parameters)

;; condition-map? is a hash-map with :pre and :post conditions (? optional)

;; expr* are all the expressions that make up the body of the function definition, the algorithm


;; simple polymorphic function (with one branch calling the other with a default value)
(defn i-am-polly
  ([] (i-am-polly "Jenny"))
  ([name] (str "Hello " name ", my name is polly")))



;; The conditional map is a Clojure hash-map with two specific keys
;; each key is bound to a vector containing code that defines one or more conditions
;; I assume that its a vector for containing multiple conditions

#_
{:pre  [pre-expr*]
 :post [post-expr*]}

;; pre-expr* and post-expr* are boolean expressions (conditions)
;; - they can use arguments passed to the function

;; post-expr* can use % as a placeholder for the return value (the value the function evaluates to).


;; A simple example - a function definition to square a numbers
;; :pre condition - number must be positive
;; :post condition - result must be greater than 16 and less than 255
(defn number-squared [x]
  {:pre  [(pos? x)]
   :post [(> % 16), (< % 225)]}
  (* x x))

;; A function call that meets the conditions
(number-squared 10)


;; Trowing Exceptions
;; If any of the boolean conditions return `false` then
;; a `java.lang.AssertionError` is thrown.

(number-squared -10)
;; Assert failed: (pos? x)

(number-squared 2)
;; Assert failed: (> % 16)

(number-squared 4)
;; Assert failed: (> % 16)


;; keys in the conditional map are optional

(defn number-squared-positive-negative [x]
  {:post [(> % 16), (< % 225)]}
  (* x x))

(number-squared-positive-negative -10)
;; => 100

(number-squared-positive-negative -18)
;; Assert failed: (< % 225)


;; Advantages of `:pre` and `:post`
;; simple:
;; - just add predicate functions, no specification design required
;; - predicate functions are simple to test


;; Interesting references
;; * Constraining functions with post conditions (aspect oriented programming)
;; http://blog.fogus.me/2009/12/21/clojures-pre-and-post/
;;
;; * Throwing exceptions with added details
;; https://stackoverflow.com/questions/24834116/how-can-i-get-clojure-pre-post-to-report-their-failing-value
;;
;; * https://clojureverse.org/t/why-are-pre-and-post-conditions-not-used-more-often/2238
;; * https://jonase.github.io/nil-recur/posts/11-1-2015-pre-post-conditions.html
;; * http://swannodette.github.io/2015/01/09/life-with-dynamic-typing/


;; Create a customer specification
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; define a specification for a customer
;; Used to check the data passed to a function is correctly formed customer data
;; a customer is a hash-map of first-name, last-name and email-address

;; define a spec for each part of the customer hash-map

(spec/def ::first-name string?)
(spec/def ::last-name string?)
(spec/def ::email-address
  (spec/and string?
            #(re-matches #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$"
                         %)))


;; Compose a customer spec with the 3 other specifications
(spec/def ::customer
  (spec/keys
    :req [::first-name ::last-name ::email-address]))

;; Test the specification with example data
(spec/valid?
  ::customer
  {::first-name "Jenny" ::last-name "Jetpack" ::email-address "jen@jetpack.org"})


(spec/valid?
  ::customer
  {::first-name "Jenny" ::last-name "Jetpack" })


;; Using Clojure Spec with :pre and :post in a function definition
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; As spec/valid? is a boolean, it can be used as a :pre or :post condition


;; Define a function to get the full name of the customer,
;; To display on the website when they login

;; use a pre condition to check the argument passed to the function
;; meets the customer specification
;; use a post condition to check the return value is a string

(defn customer-fullname
  [customer-details]
  {:pre  [(spec/valid? ::customer customer-details)]
   :post [(spec/valid? string? %)]}
  (str (::first-name customer-details)
       " "
       (::last-name customer-details)))


(customer-fullname
  {::first-name "Jenny" ::last-name "Jetpack" ::email-address "jen@jetpack.org"})


;; Not providing the right customer specification

(customer-fullname
  {:buggy "data"})
;; Assert failed:
;; (spec/valid? :practicalli.spec-for-validation/customer
;;              customer-details)


;; If the function behaves differently to the conditions
;; then we will get useful feedback
;; println returns `nil`, the string is sent to standard out / REPL

(defn customer-fullname-bug
  [customer-details]
  {:pre  [(spec/valid? ::customer customer-details)]
   :post [(spec/valid? string? %)]}
  (println (::first-name customer-details)
           " "
           (::last-name customer-details)))


(customer-fullname-bug
  {::first-name "Jenny" ::last-name "Jetpack" ::email-address "jen@jetpack.org"})
;; Assert failed: (spec/valid? string? %)



;; Using spec/assert for detailed feedback
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Replace spec/valid? with spec/assert
;; to get more than a true / false result

(defn customer-fullname-assert
  [customer-details]
  {:pre  [(spec/assert ::customer customer-details)]
   :post [(spec/valid? string? %)]}
  (str (::first-name customer-details)
       " "
       (::last-name customer-details)))

;; For assert to work, they need to be enabled.
(spec/check-asserts true)

;; Are asserts being check for?
(spec/check-asserts?)

;; NOTE: This code could be used to enable assert checking only
;; during development and switch off for production

(customer-fullname-assert {:buggy "data"})
;; Spec assertion failed.
;; Spec: :practicalli.spec-for-validation/customer
;; Value: {:buggy "data"}
;; Problems:
;; failed: (contains? % :practicalli.spec-for-validation/first-name)
;; failed: (contains? % :practicalli.spec-for-validation/last-name)
;; failed: (contains? % :practicalli.spec-for-validation/email-address)


(customer-fullname-assert 42)
;; Spec assertion failed.
;; Spec: :practicalli.spec-for-validation/customer
;; Value: 42
;; Problems:
;; failed: map?



(defn customer-fullname-assert-bug
  [customer-details]
  {:pre  [(spec/assert ::customer customer-details)]
   :post [(spec/assert string? %)]}
  (println (::first-name customer-details)
           " "
           (::last-name customer-details)))

(customer-fullname-assert-bug
  {::first-name "Jenny" ::last-name "Jetpack" ::email-address "jen@jetpack.org"})
;; Spec assertion failed.
;; Spec: #function[clojure.core/string?--5410]
;; Value: nil
;; Problems:
;; failed: string?



;; Using spec/confirm and destructuring for more detailed specifications
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; INCOMPLETE EXAMPLE

;; (defn- set-config [property value]
;;   "Mock function"
;;   (println "Setting " property " to " value))

;; (defn configure [input]
;;   (let [parsed (spec/conform ::config input)]
;;     (if (= parsed ::spec/invalid)
;;       (throw (ex-info "Invalid input" (spec/explain-data ::config input)))
;;       (for [{prop :prop [_ val] :val} parsed]
;;         (set-config (subs prop 1) val)))))

;; (configure ["-server" "foo" "-verbose" true "-user" "joe"])



;; Create a specification for a function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; `spec/fdef` define a specification for a function (`fn` `defn`) or macro (`defmacro`)
;; - arguments
;; - return value
;; - a function defining relationship between arguments and return value

;; NOTE: `defmacro` does not support a conditions map, only `fn` (and hence `defn`)
;; so clojure spec is more encompassing in this respect


;; This was the function with :pre and :post conditions

#_(defn customer-fullname
    [customer-details]
    {:pre  [(spec/assert ::customer customer-details)]
     :post [(spec/assert string? %)]}
    (str (::first-name customer-details)
         " "
         (::last-name customer-details)))


;; Now define a specification for that function

(defn customer-fullname
  "Return customer full name from customer details"
  [customer-details]
  (str (::first-name customer-details)
       " "
       (::last-name customer-details)))


(spec/fdef customer-fullname
  :args ::customer
  :ret int?)

(customer-fullname
  {::first-name "Jenny" ::last-name "Jetpack" ::email-address "jen@jetpack.org"})

(customer-fullname "customer")


(spec/fdef customer-fullname
  :args (spec/cat :customer-details ::customer)
  :ret string?
  :fn #(= (:ret %)
          (str (::first-name :args) " " (::last-name :args))))

;; Instrument the spec before it actually does anything
;; (require '[clojure.spec.test.alpha :as stest])

(spec-test/instrument `customer-fullname)


;; TODO
;; explain regex and argument lists - cat, alt, ...





;; Clojure.org example:
;; TODO: need to describe collections for this example
;; https://clojure.org/guides/spec#_collections


(defn ranged-rand
  "Returns random int in range start <= rand < end"
  [start end]
  (+ start (long (rand (- end start)))))


(spec/fdef ranged-rand
  :args (spec/and
          (spec/cat :start int? :end int?)
          #(< (:start %) (:end %)))

  :ret int?

  :fn (spec/and
        #(>= (:ret %) (-> % :args :start))
        #(< (:ret %) (-> % :args :end))))






;; Advantages of clojure spec
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Usability
;; - very easy to use well defined specifications throughout your applications
;;   and across all your projects
;;
;; Transparency
;; - detailed information can be returned when specifications find a failure,
;;   preventing and fixing issues becomes simpler.
;;
;; Clean application code
;; - specifications are defined separate from the application code,
;;   making that code easier to focus on.
