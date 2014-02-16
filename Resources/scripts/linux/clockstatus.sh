#!/bin/sh

for host in 192.168.1.6 192.168.1.7 192.168.1.8 192.168.1.10 
do 
	ssh $host "echo $host; date"
	ntpq -p
done