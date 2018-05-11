#!/bin/bash
path=/Users/zumboboga/Desktop/prmstest

#uuidgen
uuid=`uuidgen`

mkdir $path/$uuid
cp $path/input/data.csv $path/$uuid
cp $path/input/efcarson.sim $path/$uuid
cp $path/input/mixed_params.csv $path/$uuid
echo $uuid

#upload sub-directory to s3 bucket
aws s3 cp $path/$uuid s3://uwt-prms/$uuid --recursive

#call lambda function using curl
json={"\"uuid\"":"\"$uuid\",\"calcs\"":1000,\"sleep\"":0,\"loops\"":20}
echo $json

curl -H "Content-Type: application/json" -X POST -d  $json https://124bqpr191.execute-api.us-east-1.amazonaws.com/stage_prms 

#download the output 
aws s3 cp s3://uwt-prms/$uuid/out $path/$uuid/output --recursive

#delete files on S3
aws s3 rm s3://uwt-prms/$uuid --recursive

exit









