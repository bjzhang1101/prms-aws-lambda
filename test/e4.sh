#!/bin/bash

echo "parallel,containers,newcontainers,recycont,hosts,recyvms,avgruntime,runs_per_container,runs_per_cont_stdev,runs_per_host,runs_per_host_stdev9" >> out_e4.csv

aws lambda update-function-configuration --function-name test7FromCLI --memory-size=1664

output=`./test.sh 100 100 1 1`
for (( i=0; i < 4; i++))
do
  sleep 10
  output=`./test.sh 100 100 2 2`
done

for (( i=10; i <= 100; i++ ))
do 
  sleep 10
  output=`./test.sh $i $i 2 2`
  echo $i,$output >> out_e4.csv
done
exit
