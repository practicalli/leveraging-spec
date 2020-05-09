(ns practicalli.card-game
  (:require [clojure.spec.alpha :as spec]))



(spec/def ::card-suit  #{:club :diamond :heart :spade} )



;; A card can be a picture card or an integer value of 2-10
(spec/def ::card-value (spec/or #{:ace :jack :queen :king}
                                int?) )

;; This is a bit too generic for numbers, does not limit it to range.
;; (range 2 11) will generate the right numbers
;; and then we can just include that in the set

(spec/def
  ::card-value (into #{:ace :jack :queen :king}
                     (range 2 11)) )


(defn random-card
  []
  {:card-value :ace
   :card-suit  :heart})
