(ns backtype.storm.daemon.acker
  (:import [backtype.storm.task OutputCollector TopologyContext IBolt])
  (:import [backtype.storm.tuple Tuple Fields])
  (:import [backtype.storm.utils RotatingMap MutableObject Utils])
  (:import [java.util List Map])
  (:import [backtype.storm Constants])
  (:use [backtype.storm config util log])
  (:gen-class
   :init init
   :implements [backtype.storm.task.IBolt]
   :constructors {[] []}
   :state state ))

(def ACKER-COMPONENT-ID "__acker")
(def ACKER-INIT-STREAM-ID "__ack_init")
(def ACKER-ACK-STREAM-ID "__ack_ack")
(def ACKER-FAIL-STREAM-ID "__ack_fail")

(defn- update-ack [curr-entry val]
  (let [old (get curr-entry :val 0)]
    (assoc curr-entry :val (bit-xor old val))
    ))

(defn- acker-emit-direct [^OutputCollector collector ^Integer task ^String stream ^List values]
  (.emitDirect collector task stream values)
  )

(defn mk-acker-bolt []
  (let [output-collector (MutableObject.)
        pending (MutableObject.)
        acker-id (MutableObject.)]
    (reify IBolt
      (^String toString [this] (.getObject acker-id))
      (^void prepare [this ^Map storm-conf ^TopologyContext context ^OutputCollector collector]
               (.setObject output-collector collector)
               (.setObject acker-id (clojure.string/join ":" [(.getThisComponentId context) (.getThisTaskId context)]))
               (.setObject pending (RotatingMap. 2))
               )
      (^void execute [this ^Tuple tuple]
             (let [^RotatingMap pending (.getObject pending)
                   stream-id (.getSourceStreamId tuple)
                   ^String acker-id (.getObject acker-id)]
               (if (= stream-id Constants/SYSTEM_TICK_STREAM_ID)
                 (.rotate pending)
                 (let [id (.getValue tuple 0)
                       ^OutputCollector output-collector (.getObject output-collector)
                       curr (.get pending id)
                       curr (condp = stream-id
                                ACKER-INIT-STREAM-ID (-> curr
                                                         (update-ack (.getValue tuple 1))
                                                         (assoc :spout-task (.getValue tuple 2)))
                                ACKER-ACK-STREAM-ID (update-ack curr (.getValue tuple 1))
                                ACKER-FAIL-STREAM-ID (assoc curr :failed true))]
                   (.put pending id curr)
                   ;; (log-debug (Utils/logString "acker:added" acker-id "" (into-array String [
                   ;;              "stream" (.toString stream-id) "curr" (.toString curr) "id" (.toString id)
                   ;;              "tuple" (.toString tuple) "pending" (.toString pending) ])))
                   (when (and curr (:spout-task curr))
                     (cond (= 0 (:val curr))
                           (do
                             (.remove pending id)
                             ;; (log-debug (Utils/logString "acker:cleared" acker-id "" (into-array String [
                             ;;              "stream" ACKER-ACK-STREAM-ID "curr" (.toString curr) "id" (.toString id)
                             ;;              "tuple" (.toString tuple) "dest" (.toString (:spout-task curr)) ])))

                             (acker-emit-direct output-collector
                                                (:spout-task curr)
                                                ACKER-ACK-STREAM-ID
                                                [id]
                                                ))
                           (:failed curr)
                           (do
                             (.remove pending id)
                            ;; (log-debug (Utils/logString "acker:fail" acker-id "" (into-array String [
                            ;;              "stream" ACKER-FAIL-STREAM-ID "curr" (.toString curr) "id" (.toString id)
                            ;;              "tuple" (.toString tuple) "dest" (.toString (:spout-task curr)) ])))

                             (acker-emit-direct output-collector
                                                (:spout-task curr)
                                                ACKER-FAIL-STREAM-ID
                                                [id]
                                                ))
                           ))
                   (.ack output-collector tuple)
                   ;; (log-debug (Utils/logString "acker:re-acks" acker-id "" (into-array String [
                   ;;               "tuple" (.toString tuple) "collector" (.toString output-collector) ])))
                   ))))
      (^void cleanup [this]
        )
      )))

(defn -init []
  [[] (container)])

(defn -prepare [this conf context collector]
  (let [^IBolt ret (mk-acker-bolt)]
    (container-set! (.state ^backtype.storm.daemon.acker this) ret)
    (.prepare ret conf context collector)
    ))

(defn -execute [this tuple]
  (let [^IBolt delegate (container-get (.state ^backtype.storm.daemon.acker this))]
    (.execute delegate tuple)
    ))

(defn -cleanup [this]
  (let [^IBolt delegate (container-get (.state ^backtype.storm.daemon.acker this))]
    (.cleanup delegate)
    ))
