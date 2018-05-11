#!/bin/bash
update(){
  memory=$1
  aws lambda update-function-configuration --function-name test7FromCLI --memory-size=$memory
  sleep 10
  for (( i=1 ; i <= 3; i++ ))
  do
    sleep 10
    ./test.sh 100 100
  done
}
export -f update

warm(){
  memory=$1
  output=`./test.sh 100 100 1 1`
  echo $memory, $output >> tmp.csv
}
export -f warm

echo "memory,containers,newcontainers,recycont,hosts,recyvms,avgruntime,runs_per_container,runs_per_cont_stdev,runs_per_host,runs_per_host_stdev9" >> tmp.csv

# 256MB
update 256
warm 256
# 384MB
update 384
warm 384
# 512MB
update 512
warm 512
# 640MB
update 640
warm 640
# 768MB
update 768
warm 768
# 896MB
update 896
warm 896
# 1024
update 1024
warm 1024
# 1152
update 1152
warm 1152
# 1280
update 1280
warm 1280
# 1408
update 1408
warm 1408
# 1536
update 1536
warm 1536
# 1664
update 1664
warm 1664
# 1792
update 1792
warm 1792
# 1920
update 1920
warm 1920
# 2048
update 2048
warm 2048
# 2176
update 2176
warm 2176
# 2368
update 2368
warm 2368
# 2496
update 2496
warm 2496
# 2624
update 2624
warm 2624
# 2752
update 2752
warm 2752
# 2880
update 2880
warm 2880
# 3008
update 3008
warm 3008
