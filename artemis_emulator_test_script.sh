#!/usr/bin/env bash

#export TILLER_NAMESPACE=promenade
#
#cd ~/Desktop/Promenade/helm-charts/activemq-artemis/activemq-artemis || exit
#
#helm del --purge artemis
#
#oc delete pvc data-artemis-activemq-artemis-master-0
#
#helm install --name artemis .

cd ../broker || exit #path per la creazione del broker

rm -rf mybroker

artemis create mybroker --user licit --password licit --allow-anonymous

yes | cp -rf broker.xml mybroker/etc/broker.xml

./mybroker/bin/artemis.cmd run

#--------------------------------
#avviare l'emulatore da terminale
#--------------------------------
# mvn exec:java -Dexec.mainClass="Emulatorx"