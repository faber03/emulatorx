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

./mybroker/bin/artemis.cmd run
