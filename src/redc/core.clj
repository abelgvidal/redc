
(ns redc.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:gen-class))

(defn read-last-n-lines [file-path n]
  (with-open [rdr (io/reader file-path)]
    (vec (take-last n (line-seq rdr)))))

(defn -main [& args]
  (let [lines (read-last-n-lines "log" 10)]
    (doseq [line lines]
      (println line))))

