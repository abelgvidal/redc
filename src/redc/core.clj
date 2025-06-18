(ns redc.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:gen-class))


(def default-log-entry
  {:timestamp nil
   :hostname nil
   :severity nil
   :facility nil
   :message nil
   :source-ip nil
   :destination-ip nil
   :user nil
   :process nil
   :event-id nil
   :status-code nil
   :bytes nil
   :protocol nil
   :method nil
   :url nil
   :user-agent nil
   :referrer nil
   :extensions {}
   :raw-line nil
   :format nil})


;; Funciones auxiliares
(defn parse-clf-timestamp [date-part timezone-part]
  ;; "[18/Jun/2025:10:30:45" "+0000]" -> "2025-06-18T10:30:45Z"
  (when (and date-part timezone-part)
    (let [clean-date (str/replace date-part #"[\[\]]" "")
          clean-tz (str/replace timezone-part #"[\[\]]" "")]
      ;; Conversión a ISO 8601 (simplificada)
      clean-date))) ; implementar conversión completa

(defn extract-method [request-part]
  ;; "\"GET" -> "GET"
  (when request-part
    (str/replace request-part #"[\"]" "")))

(defn extract-protocol [protocol-part]
  ;; "HTTP/1.1\"" -> "HTTP/1.1"
  (when protocol-part
    (str/replace protocol-part #"[\"]" "")))



;; ejemplo CLF
;; 192.168.1.100 - - [18/Jun/2025:10:30:45 +0000] "GET /index.html HTTP/1.1" 200 1234
(defn parse-line-clf-to-common [line]
  (let [parts (str/split line #"\s+" 10)]
    (merge default-log-entry 
           {:source-ip (get parts 0 "")
            :user (let [user (get parts 2 "")]
                    (when (not= user "-") user))
            :timestamp (parse-clf-timestamp 
                        (get parts 3 "") 
                        (get parts 4 ""))
            :method (extract-method (get parts 5 ""))
            :url (get parts 6 "")
            :protocol (extract-protocol (get parts 7 ""))
            :status-code (get parts 8 "")
            :bytes (let [bytes (get parts 9 "")]
                     (when (not= bytes "-") bytes))
            :format :clf
            :raw-line line})))


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
            (let [lines (read-lines "log" 100 current-position)]         ;; leo dos lineas
              (if (empty? lines)                                       ;; si llego al fin , salgo
                (do
                  (println "Fin del archivo alcanzado")
                  (System/exit 0))
                (do                                                    ;; si no, imprimo lineas
                  (doseq [line lines]
                    (println (parse-line-clf-to-common line)))
                  (Thread/sleep 1000)                                  ;; me echo la siesta
                  (+ current-position (count lines)))))                ;; muevo el puntero 2 lineas adelante
            (catch Exception e
              (println "Error reading file:" (.getMessage e))
              current-position))]
      (recur new-position))))
