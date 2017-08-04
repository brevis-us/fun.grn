(ns fun.grn.core
  (:require [clj-random.core :as random]
            [brevis-utils.parameters :as params])
  (:import [evolver GRNGenome GRNGene]
           [evaluators GRNGenomeEvaluator]
           [grn GRNProtein GRNModel]
           [java.util Random]
           [operators GRNAddGeneMutationOperator
            GRNAligningCrossoverOperator_ParentCountProb
            GRNAligningCrossoverOperator_v1
            GRNAligningCrossoverOperator_v1b
            GRNAligningCrossoverOperator_v2
            GRNCrossoverOperator
            GRNDeleteGeneMutationOperator
            GRNGeneMutationOperator
            GRNMutationOperator
            GRNOnePointCrossoverOperator]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Globals

(params/set-param
  :num-GRN-inputs 2
  :num-GRN-outputs 2
  :num-GRN-steps 1
  :grn-mutation-add-max-size Integer/MAX_VALUE
  :grn-mutation-add-probability 0.33
  :grn-mutation-del-min-size 0
  :grn-mutation-del-probability 0.33
  :grn-mutation-change-probability 0.33)

(defn initialize-grneat
  "Initiatialize globals, such as evolutionary features."
  []
  (let [mutators {(GRNAddGeneMutationOperator. (params/get-param :grn-mutation-add-max-size) (params/get-param :grn-mutation-add-probability)) (params/get-param :grn-mutation-add-probability),
                  (GRNDeleteGeneMutationOperator. ^int (max (params/get-param :grn-mutation-del-min-size) (+ (params/get-param :num-GRN-inputs) (params/get-param :num-GRN-outputs) 1) (params/get-param :grn-mutation-del-probability))) (params/get-param :grn-mutation-del-probability),
                  (GRNGeneMutationOperator. (params/get-param :grn-mutation-change-probability)) (params/get-param :grn-mutation-change-probability)}
        crossovers {;(GRNAligningCrossoverOperator_v1.)
                    ;(GRNAligningCrossoverOperator_v1b.)
                    ;(GRNAligningCrossoverOperator_v2.)
                    ;(GRNOnePointCrossoverOperator.)
                    (GRNAligningCrossoverOperator_ParentCountProb.) nil}] 
    (params/set-param :grn-rng (Random.))
    (params/set-param :grn-mutators mutators)
    (params/set-param :grn-crossovers crossovers)))

(defn make-genome
  "Make a genome."
  []
  (let [^GRNGenome genome (GRNGenome.)
        beta-max (.getBetaMax genome)
        beta-min (.getBetaMin genome)
        delta-max (.getDeltaMax genome)
        delta-min (.getDeltaMin genome)]
    (dotimes [k (params/get-param :num-GRN-inputs)]
      (.addGene genome 
        (GRNGene/generateRandomGene GRNProtein/INPUT_PROTEIN k (params/get-param :grn-rng))))
    (dotimes [k (params/get-param :num-GRN-outputs)]
      (.addGene genome
        (GRNGene/generateRandomGene GRNProtein/OUTPUT_PROTEIN k (params/get-param :grn-rng))))
    ; Could to great init here (small genomes)
    (dotimes [k (inc (random/lrand-int (- 50 (params/get-param :num-GRN-inputs) (params/get-param :num-GRN-outputs))))]
      (.addGene genome 
        (GRNGene/generateRandomRegulatoryGene (params/get-param :grn-rng))))
    (.setBeta genome (+ (* (- beta-max beta-min) (random/lrand)) beta-min))                        
    (.setDelta genome (+ (* (- delta-max delta-min) (random/lrand)) delta-min))
    genome))                        

(defn make-grn-state
  "Make the state of a GRN."
  [^GRNGenome genome]
  (GRNGenomeEvaluator/buildGRNFromGenome genome))

(defn make-grn
  "Return a GRN, with a genome and a state."
  []
  (let [^GRNGenome genome (make-genome)
        grn-state (make-grn-state genome)]
    {:state grn-state
     :num-proteins (.size (.proteins ^GRNModel grn-state))
     :genome genome}))

(defn load-from-file
  [filename]
  (let [grn-state (grn.GRNModel/loadFromFile filename)]
    {:state grn-state
     :num-proteins (.size (.proteins ^GRNModel grn-state))
     :genome (make-genome)}))

(defn reset-grn
  "Reset a GRN state to initial values."
  [grn]
  (.reset ^GRNModel (:state grn))
  grn)

(defn set-grn-inputs
  "Set the GRN inputs."
  [grn inputs]
  (let [proteins (.proteins ^GRNModel (:state grn))]
    (doall (map #(.setConcentration ^GRNProtein (.get proteins %1) %2)
                (range) inputs))
    grn))

(defn update-grn
  "Update the state of a GRN."
  [grn]
  (.evolve ^GRNModel (:state grn)
    ^int (params/get-param :num-GRN-steps))
  grn)

(defn get-grn-outputs
  "Get the GRN outputs."
  [grn]
  (let [proteins (.proteins ^GRNModel (:state grn))]
    (for [oid (range (params/get-param :num-GRN-inputs)
                     (+ (params/get-param :num-GRN-inputs) (params/get-param :num-GRN-outputs)))]
      (.getConcentration ^GRNProtein (.get proteins oid)))))

(defn select-mutation-operator
  "Select a mutation operator."
  []
  (let [rnd (random/lrand-nth (keys (params/get-param :grn-mutators)))]; we know uniform for now, laziness
    rnd))

(defn mutate
  "Mutate a GRN. Resets the grn-state."
  [grn]
  (let [mutant-genome (loop []
                        (let [^GRNMutationOperator mutator (select-mutation-operator)
                              mutant-genome (.cloneAndMutate mutator (:genome grn) (params/get-param :grn-rng) #_random/*RNG*)]
                          (if mutant-genome
                            mutant-genome
                            (recur))))
        grn-state (make-grn-state mutant-genome)]
    (.reset ^GRNModel grn-state)
    {:state grn-state
     :num-proteins (.size (.proteins ^GRNModel grn-state)) 
     :genome mutant-genome}))

(defn select-crossover-operator
  "Select a crossover operator."
  []
  (let [rnd (random/lrand-nth (keys (params/get-param :grn-crossovers)))]; we know uniform for now, laziness
    rnd))

(defn crossover
  "Mutate a GRN. Resets the grn-state."
  [p1 p2]
  (let [mutant-genome (loop []
                        (let [^GRNCrossoverOperator crossoveror (select-crossover-operator)
                              mutant-genome (.reproduce crossoveror
                                                 (:genome p1)
                                                 (:genome p2)
                                                 (params/get-param :grn-rng))]
                          (if mutant-genome
                            mutant-genome
                            (recur))))
        grn-state (make-grn-state mutant-genome)]
    (.reset ^GRNModel grn-state)
    {:state grn-state
     :num-proteins (.size (.proteins ^GRNModel grn-state)) 
     :genome mutant-genome}))

(defn write-to-file
  "Save a GRN to file."
  [grn filename]
  (.writeToFile (:state grn) filename))
