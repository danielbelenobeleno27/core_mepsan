#!/bin/bash
echo "### INICIALIZANDO EL PROGRAMA ESPERE 30 SEGUNDOS PARA INNICIAR ###"
sleep 1
echo "EJECUTANDO EL PROGRAMA"
cd /home/pi/NetBeansProjects/NeoService/dist
sudo java -jar /home/pi/NetBeansProjects/NeoService/dist/NeoService.jar