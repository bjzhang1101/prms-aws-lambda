/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bjzhang;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.transfer.*;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.util.IOUtils;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.stream.Collectors;
import java.io.*;
import java.util.*;


/**
 *
 * @author zumboboga
 */
public class PRMSTest implements RequestHandler<Request, Response>{

    /**
     * @param args the command line arguments
     */
    public Response handleRequest(Request request, Context context){
        
        
        
        ////////////////////////////////////////////////////////////////////////
        /////////////////////     pull data from S3     ////////////////////////
        ////////////////////////////////////////////////////////////////////////
        String bucket_input = "zumboboga";
        String bucket_output = "zumbobogaresized";
        
        AmazonS3 client = new AmazonS3Client();

        //input data.csv
        S3Object x1 = client.getObject(bucket_input, "data.csv");
        InputStream contents = x1.getObjectContent();
        
        /*
        // upload files by string
        String tmp = new BufferedReader(new InputStreamReader(contents))
                .lines().collect(Collectors.joining(System.lineSeparator()));
        
        client.putObject(bucket_output, "data.csv", tmp);
        */
       
        File f1 = new File("/tmp/data.csv");
        try{
            Files.copy(contents, f1.toPath());
        } catch (IOException i){
            i.printStackTrace();
        }
        
        //input mixed_params.csv
        S3Object x2 = client.getObject(bucket_input, "mixed_params.csv");
        InputStream contents2 = x2.getObjectContent();
        
        File f2 = new File("/tmp/mixed_params.csv");
        try{
            Files.copy(contents2, f2.toPath());
        } catch (IOException i){
            i.printStackTrace();
        }

        //input efcarson.sim
        S3Object x3 = client.getObject(bucket_input, "efcarson.sim");
        InputStream contents3 = x3.getObjectContent();
        
        File f3 = new File("/tmp/efcarson.sim");
        try{
            Files.copy(contents3, f3.toPath());
        } catch (IOException i){
            i.printStackTrace();
        }
        
        ////////////////////////////////////////////////////////////////////////
        //////////////////           Run Project        ////////////////////////
        ////////////////////////////////////////////////////////////////////////
        // PRMS project home dir (Change this path to reflect your path to "prmsprj")
        String prj = "/tmp";

        // Pass the project home to OMS
        System.setProperty("oms_prj", prj);

        // runs the sim file via OMS CLI in a certain logging level
        oms3.CLI.main(new String[]{
            "-l", "OFF", // can be "OFF", "ALL", .... (all JDK loging levels)
            "-r", prj + "/efcarson.sim" // runs the sim file in project home.
        });
        
        
        
        ////////////////////////////////////////////////////////////////////////
        ////////           Upload the output Directory to S3        ////////////
        ////////////////////////////////////////////////////////////////////////
        TransferManager trans = TransferManagerBuilder.standard().build();
        MultipleFileUpload xfer = trans.uploadDirectory(bucket_output, null
                , new File("/tmp"), true);
        try{
            xfer.waitForCompletion();
        } catch (InterruptedException i){
            i.printStackTrace();
        }
        
        ////////////////////////////////////////////////////////////////////////
        //     Response
        ////////////////////////////////////////////////////////////////////////
        //int age = request.calcs;
        int age = 0;
        //String name = tmp + "!!!!!!";
        //String name = file.getAbsolutePath();
        String name = request.base;
        String Base64 = request.base;
        Response res = new Response(name, age, Base64);
        return res;
    }
   
    /*
    public static void main (String[] args)
    {
        
    }
    */
}