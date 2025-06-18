(ns redc.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:gen-class))

;; ejemplo
;; 192.168.1.100 - - [18/Jun/2025:10:30:45 +0000] "GET /index.html HTTP/1.1" 200 1234
(defn parse-line [line]
  (let [parts (str/split line #"\s+" 10)]  ; máximo 4 partes
    {:ip (get parts 0 "")
     :status (get parts 8 "")
     :bytes (get parts 9 "")
    }))

(defn read-lines [file-path num-lines start-position]
  (with-open [rdr (io/reader file-path)]
    (->> (line-seq rdr)                     ; Lazily read lines from the file
         (drop start-position)              ; Skip lines up to the start position
         (take num-lines)                   ; Take only the desired number of lines
         (map str/trim)                     ; Trim whitespace from each line
         (vec))))                           ; Convert the result to a vector


(defn -main [& args]
  (loop [current-position 0]                        ;; defino el puntero en el archivo current-position
    (let [new-position                              ;; defino un puntero futuro
          (try
            (let [lines (read-lines "log" 2 current-position)]         ;; leo dos lineas
              (if (empty? lines)                                       ;; si llego al fin , salgo
                (do
                  (println "Fin del archivo alcanzado")
                  (System/exit 0))
                (do                                                    ;; si no, imprimo lineas
                  (doseq [line lines]
                    (let [parsed (parse-line line)
                          {:keys [ip status bytes]} parsed]
                      (println "IP:" ip)
                      (println "Status:" status)
                      (println "Bytes:" bytes))
                    )
                  (Thread/sleep 1000)                                  ;; me hecho la siesta
                  (+ current-position (count lines)))))                ;; muevo el puntero 2 lineas adelante
            (catch Exception e
              (println "Error reading file:" (.getMessage e))
              current-position))]
      (recur new-position))))
