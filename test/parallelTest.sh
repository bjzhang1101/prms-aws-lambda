#!/bin/bash

totalruns=$1
threads=$2
vmreport=$3
contreport=$4
containers=()
cuses=()
ctimes=()


#################################################################
#       Call Lambda Function
#################################################################
callservice(){
  totalruns=$1
  threadid=1221
  path=/Users/zumboboga/Desktop/prmstest
  #onesecond=1000

  for((i=1;i<=$totalruns;i++))
  do
    #generate a uuid randomly as the local path 
    uuid=`uuidgen`
    
    # Upload input files from Local Host. But we have decided not to use it anymore
    #mkdir $path/$uuid
    #cp $path/input/data.csv $path/$uuid
    #cp $path/input/efcarson.sim $path/$uuid
    #cp $path/input/mixed_params.csv $path/$uuid
    #x=`aws s3 cp $path/$uuid s3://uwt-prms/$uuid --recursive`
    
    json={"\"uuid\"":"\"$uuid\""}
    
    #time1=( $(($(date +%s)/1000000)) )      
    #Because Mac don't have a `date %N` command, so change a way calculate nanosecond, when changing to EC2 we could use former command or this too
    time1=`perl -MTime::HiRes -e 'printf("%.0f\n",Time::HiRes::time()*1000)'`
    time1=`expr $time1 % 1000000`
    
    #run PRMS
    output=`curl -H "Content-Type: application/json" -X POST -d  $json https://124bqpr191.execute-api.us-east-1.amazonaws.com/stage_prms 2>/dev/null` 
    
    # grab end time
    #time2=200
    #time2=( $(($(date +%s%N)/1000000)) )
    time2=`perl -MTime::HiRes -e 'printf("%.0f\n",Time::HiRes::time()*1000)'`
    time2=`expr $time2 % 1000000`
    #pull result CSV to local enviorment
    x=`aws s3 cp s3://uwt-prms/$uuid/out $path/$uuid/output --recursive`
    #delete files on S3
    x=`aws s3 rm s3://uwt-prms/$uuid --recursive`

    uuid=`echo $output | jq '.uuid'`
    cpuusr=`echo $output | jq '.cpuUsr'`
    cpukrn=`echo $output | jq '.cpuKrn'`
    pid=`echo $output | jq '.pid'`
    cputype="unknown"
    cpusteal=`echo $output | jq '.vmcpusteal'`
    vuptime=`echo $output | jq '.vmuptime'`
    newcont=`echo $output | jq '.newcontainer'`
    cont=`echo $output | jq '.container'`

    elapsedtime=`expr $time2 - $time1`
    #sleeptime=`echo $onesecond - $elapsedtime | bc -l`
    #sleeptimems=`echo $sleeptime/$onesecond | bc -l`
    echo "$i,$threadid,$uuid,$cputype,$cpusteal,$vuptime,$pid,$cpuusr,$cpukrn,$elapsedtime,$newcont"
    echo "$cont,$elapsedtime,$vuptime,$newcont" >> .uniqcont

  done

exit
}
export -f callservice

#################################################################
# START OF THE SCRIPT
#################################################################
runsperthread=`echo $totalruns/$threads | bc -l`
runsperthread=${runsperthread%.*}
date
echo "Setting up test: runsperthread=$runsperthread threads=$threads totalruns=$totalruns"

for((i=0;i<$threads;i++))
do
  a[$i]=$runsperthread
done

# paralle call
parallel --no-notice -j $threads -k callservice ::: "${a[@]}"

newconts=0
recycont=0
recyvms=0
#################################################################
#  Generate CSV ouput - group by container
#################################################################
rm -f container.csv

if [[ ! -z $contreport && $contreport -eq 1 ]]
    then
      rm -f .origcont
fi

alltimes=0

filename=".uniqcont"
while read -r line
do
    uuid=`echo $line | cut -d',' -f 1`
    time=`echo $line | cut -d',' -f 2`
    host=`echo $line | cut -d',' -f 3`
    isnewcont=`echo $line | cut -d',' -f 4`
    alltimes=`expr $alltimes + $time`
    #if uuid is already in array
    found=0
    (( newconts += isnewcont))

    for ((i=0;i < ${#containers[@]};i++)) {
    	if [ "${containers[$i]}" == "${uuid}" ]; then
            (( cuses[$i]++ ))
	    ctimes[$i]=`expr ${ctimes[$i]} + $time`
	    found=1
	fi
    }
    
    ## Add unfound container to array
    if [ $found != 1 ]; then
        containers+=($uuid)
	chosts+=($host)
	cuses+=(1)
	ctimes+=($time)
    fi

    # Populate array of unique hosts
    hfound=0
    for ((i=0;i < ${#hosts[@]};i++)) {
    	if [ "${hosts[$i]}" == "${host}"  ]; then
	    (( huses[$i]++ ))
	    htimes[$i]=`expr ${htimes[$i]} + $time`
	    hfound=1
	fi
    }
    if [ $hfound != 1 ]; then
	hosts+=($host)
	huses+=(1)
	htimes+=($time)
    fi

done < "$filename"

## Determine count of recycled containers...

if [[ ! -z $contreport && $contreport -eq 1 ]]
then 
    for ((i=0;i < ${#containers[@]};i++)) {
        echo "${containers[$i]}" >> .origcont
    }
fi

if [[ ! -z $contreport && $contreport -eq 2 ]]
then 
    for ((i=0;i < ${#containers[@]};i++)){
        filename=".origcont"
	while read -r line
	do
	    if [ "${containers[$i]}" == "${line}" ]
	    then
	        (( recycont ++ ))
		break;
	    fi
	done < "$filename"
    }
fi

runspercont=`echo $totalruns / ${#containers[@]} | bc -l`
runsperhost=`echo $totalruns / ${#hosts[@]} | bc -l`
avgtime=`echo $alltimes / $totalruns | bc -l`

rm .uniqcont
echo "##############################################################"
echo "##############################################################"
echo "uuid,host,uses,totaltime,avgruntime_cont,uses_minus_avguses_sq"

echo "uuid,host,uses,totaltime,avgruntime_cont,uses_minus_avguses_sq" >> container.csv

total=0
for ((i=0;i < ${#containers[@]};i++)) {
    avg=`echo ${ctimes[$i]} / ${cuses[$i]} | bc -l`
    stdiff=`echo ${cuses[$i]} - $runspercont | bc -l`
    stdiffsq=`echo "$stdiff * $stdiff" | bc -l`
    total=`echo $total + $stdiffsq | bc -l`
    echo "${containers[$i]},${chosts[$i]},${cuses[$i]},${ctimes[$i]},$avg,$stdiffsq"
    echo "${containers[$i]},${chosts[$i]},${cuses[$i]},${ctimes[$i]},$avg,$stdiffsq" >> container.csv
}
##################################################
# Generate CSV Ourput - group by VM host
##################################################
rm -f vm.csv

stdev=`echo $total / ${#containers[@]} | bc -l`
currtime=$(date +%s)
echo "##############################################################"
echo "##############################################################"
echo "Current time of test=$currtime"
echo "host,host_up_time,uses,containers,totaltime,avgruntime_host,uses_minus_avguses_sq"

echo "Current time of test=$currtime" >> vm.csv
echo "host,host_up_time,uses,containers,totaltime,avgruntime_host,uses_minus_avguses_sq" >> vm.csv
total=0
if [[ ! -z $vmreport && $vmreport -eq 1 ]]
then
   rm -f vm.csv
fi

for ((i=0;i < ${#hosts[@]};i++)) {
    avg=`echo ${htimes[$i]} / ${huses[$i]} | bc -l`
    stdiff=`echo ${huses[$i]} - $runsperhost | bc -l`
    stdiffsq=`echo "$stdiff * $stdiff" | bc -l`
    total=`echo $total + $stdiffsq | bc -l`
    ccount=0
    uptime=`echo $currtime - ${hosts[$i]} | bc -l`
    for ((j=0;j < ${#containers[@]};j++)) {
        if [ ${hosts[$i]} == ${chosts[$j]} ]
	then
	    (( ccount ++ ))
	fi
    }
    echo "${hosts[$i]},$uptime,${huses[$i]},$ccount,${htimes[$i]},$avg,$stdiffsq"
    
    echo "${hosts[$i]},$uptime,${huses[$i]},$ccount,${htimes[$i]},$avg,$stdiffsq" >> vm.csv

    if [[ ! -z $vmreport && $vmreport -eq 1 ]]
    then 
    echo "${hosts[$i]}" >> .origvm
    fi

    if [[ ! -z $vmreport && $vmreport -eq 2 ]]
    then 
        filename=".origvm"
	while read -r line
	do
	  if [ ${hosts[$i]} == ${line} ]
	  then
	      (( recyvms ++ ))
	      break;
	  fi
	done < "$filename"
    fi
}
stdevhost=`echo $total / ${#hosts[@]} | bc -l`
#############################################
# Generate CSV output - report summary, final data
#############################################
rm -f res.csv

echo "containers,newcontainers,recycont,hosts,recyvms,avgruntime,runs_per_container,runs_per_cont_stdev,runs_per_host,runs_per_host_stdev"
echo "containers,newcontainers,recycont,hosts,recyvms,avgruntime,runs_per_container,runs_per_cont_stdev,runs_per_host,runs_per_host_stdev" >> res.csv
echo "${#containers[@]},$newconts,$recycont,${#hosts[@]},$recyvms,$avgtime,$runspercont,$stdev,$runsperhost,$stdevhost"
echo "${#containers[@]},$newconts,$recycont,${#hosts[@]},$recyvms,$avgtime,$runspercont,$stdev,$runsperhost,$stdevhost" >> res.csv


