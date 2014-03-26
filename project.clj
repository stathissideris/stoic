(defproject stoic "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]

                 [zookeeper-clj "0.9.1" :exclusions [org.apache.zookeeper/zookeeper
;;                                                     commons-codec
                                                     ]]

                 [org.apache.zookeeper/zookeeper "3.4.5" :exclusions [;;commons-codec
                                                                      com.sun.jmx/jmxri
                                                                      com.sun.jdmk/jmxtools
                                                                      javax.jms/jms
                                                                      org.slf4j/slf4j-log4j12
                                                                      log4j]]
                 [environ "0.4.0"]

                 [com.stuartsierra/component "0.2.1"]
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]]
  :plugins [[s3-wagon-private "1.1.2"]]
  :repositories {"snapshots" {:url "s3p://m2repo.agentsmutual.co.uk/snapshots/"
                              :username "AKIAID5JFTF7UKTEQVJQ"
                              :passphrase "p4j9dh7ftxXOk8DxkaQVk+/tJNm0181CpABq6skj"}
                 "releases" {:url "s3p://m2repo.agentsmutual.co.uk/releases/"
                             :username "AKIAID5JFTF7UKTEQVJQ"
                             :passphrase "p4j9dh7ftxXOk8DxkaQVk+/tJNm0181CpABq6skj"}})
