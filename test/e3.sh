#!/bin/bash

echo "parallel,containers,newcontainers,recycont,hosts,recyvms,avgruntime,runs_per_container,runs_per_cont_stdev,runs_per_host,runs_per_host_stdev9" >> out_e3.csv

aws lambda update-function-configuration --function-name test7FromCLI --memory-size=1664

for (( i=10; i <= 100; i=i+10 ))
do 
  sleep 2700
  output=`./test.sh $i $i 1 1`
  echo $output
  echo $i,$output >> out_e3.csv
done
exit
