#!/bin/bash
echo "parallel,containers,newcontainers,recycont,hosts,recyvms,avgruntime,runs_per_container,runs_per_cont_stdev,runs_per_host,runs_per_host_stdev9" >> out_e2.csv

aws lambda update-function-configuration --function-name test7FromCLI --memory-size=1664
sleep 10

for (( i=1 ; i <= 30; i++ ))
do
  output=`./test.sh $i $i 1 1`
  echo $i, $output >> out_e2.csv
  sleep 10
done

for (( i=40 ; i <= 100; i=i+10 ))
do 
  output=`./test.sh $i $i 1 1`
  echo $i, $output >> out_e2.csv
  sleep 10
done
exit
