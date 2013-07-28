(ns clj-uclassify.uclassify
  (:require [clj-uclassify.core :refer :all]
            [clojure.data.xml :as xml]
            [clojure.zip :as zip])
  (:use [clojure.string :only (join)]
        [clojure.data.codec.base64 :only (encode)]))

(defn create-classifier [keys classifier]
  (if (check-keys keys)
    (post-request
     (xml/emit-str
      (zip/root
       (zip/append-child
        (zip/xml-zip uclassify)
        (make-xml-node :writeCalls {:writeApiKey (keys :write-key) :classifierName classifier}
                       (make-xml-node :create {:id "Create"}))))))
    (throw (Throwable. "API key not found"))))

(defn add-class
  "Adds class to the existing classifier"
  [keys classifier class-name]
  (if (check-keys keys)
    (let [xml-elements (map #(make-xml-node :addClass
                                            {:id (join (seq ["AddClass" %]))
                                             :className %}) class-name)
          write-calls (make-xml-node :writeCalls
                                     {:writeApiKey (keys :write-key)
                                      :classifierName classifier}
                                     xml-elements)
          final-xml (zip/root (zip/insert-child
                               (zip/xml-zip uclassify)
                               (zip/xml-zip write-calls)))]
      (post-request
       (xml/emit-str final-xml)))
    (throw (Throwable. "API Key not found"))))

(defn remove-classifier
  "Removes pre-existing classifier"
  [keys classifier]
  (if (check-keys keys)
    (post-request
     (xml/emit-str
      (zip/root
       (zip/append-child
        (zip/xml-zip uclassify)
        (make-xml-node :writeCalls {:writeApiKey (keys :write-key) :classifierName classifier}
                       (make-xml-node :remove {:id "Remove"}))))))
    (throw (Throwable. "API key not found"))))

(defn remove-class
  "Removes class from the existing classifier"
  [keys classifier class-name]
  (if (check-keys keys)
    (let [xml-elements (map #(make-xml-node :removeClass
                                            {:id (join (seq ["RemoveClass" %]))
                                             :className %}) class-name)
          write-calls (make-xml-node :writeCalls
                                     {:writeApiKey (keys :write-key)
                                      :classifierName classifier}
                                     xml-elements)
          final-xml (zip/root (zip/insert-child
                               (zip/xml-zip uclassify)
                               (zip/xml-zip write-calls)))]
      (post-request
       (xml/emit-str final-xml)))
    (throw (Throwable. "API Key not found"))))


;(get-information api-keys "some_clas")

(defn get-information
  "Returns information about the classifier
TODO: Return response properly instead of true"
  [keys classifier]
  (let [info-tag (make-xml-node :getInformation
                                {:id "GetInformation"
                                 :classifierName classifier})
        read-calls (make-xml-node :readCalls
                                     {:readApiKey (keys :read-key)}
                                     info-tag)
        final-xml (zip/root (zip/insert-child
                             (zip/xml-zip uclassify)
                             (zip/xml-zip read-calls)))]
    (post-request
     (xml/emit-str final-xml))))

(defn train
  "Trains the classifier on text for a specified class"
  [keys texts class-name classifier]
  (let [textbase64-tag (map #(make-xml-node :textBase64
                                       {:id (str "Text" (str (index-of % texts)))}
                                       (String. (encode (.getBytes %)) )) texts)
        train-tag (map #(make-xml-node :train
                                       {:id (str "Train" %) :className class-name
                                        :textId (str "Text" %) })
                       (range (count texts)))
        texts-tag (make-xml-node :texts {} textbase64-tag)
        write-calls (make-xml-node :writeCalls
                                   {:writeApiKey (keys :write-key) :classifierName classifier}
                                   train-tag)
        ]
    (post-request
     (xml/emit-str
      (xml-append-elements uclassify (list texts-tag write-calls))))))

;(def api-keys {:read-key "aD02ApbU29kNOG2xezDGXPEIck" :write-key "fsqAft7Hs29BgAc1AWeCIWdGnY"})
;(train api-keys '("hi" "bye") "male" "gender_classifier")


