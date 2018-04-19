/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bjzhang;

import java.io.*;
import java.util.*;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.transfer.*;
import com.amazonaws.AmazonServiceException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.stream.Collectors;


/**
 *
 * @author zumboboga
 */
public class PRMSTest implements RequestHandler<Request, Response>{

    /**
     * @param args the command line arguments
     */
    public Response handleRequest(Request request, Context context){
        String bucket_input = "zumboboga";
        String bucket_output = "zumbobogaresized";
        AmazonS3 client = new AmazonS3Client();
        S3Object xFile = client.getObject(bucket_input, "data.csv");
        
        
        
        InputStream contents = xFile.getObjectContent();
        
        String tmp = new BufferedReader(new InputStreamReader(contents))
                .lines().collect(Collectors.joining(System.lineSeparator()));
        
        //upload by string
        client.putObject(bucket_output, "2.csv", tmp);
        
        
        File file = new File("/tmp");
        File file_dir = new File("/tmp/dir");
        file_dir.mkdirs();
        File f2 = new File("/tmp/dir/1.txt");
        try{
            f2.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(f2));
            out.write("hi\r\n");
            out.flush();
            out.close();
        } catch(IOException e){
            e.printStackTrace();
        }
        
        File f3 = new File("/tmp/dir/3.txt");
        try{
            f3.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(f3));
            out.write("hihi\r\n");
            out.flush();
            out.close();
        } catch(IOException e){
            e.printStackTrace();
        }
        
        try{
            Thread.sleep(150000);
        } catch (InterruptedException i){
            i.printStackTrace();
        }
        /*
        File f = new File("/tmp");
        Path p = Paths.get("/tmp/test");
        
        try (BufferedWriter bw = Files.newBufferedWriter(p, StandardCharsets.US_ASCII, StandardOpenOption.CREATE_NEW))
            {
                bw.write("hello");
                bw.close();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        */
        /*
        Path p1 = Paths.get("/tmp/test1");
        
        try (BufferedWriter bw = Files.newBufferedWriter(p1, StandardCharsets.US_ASCII, StandardOpenOption.CREATE_NEW))
            {
                bw.write("hello");
                bw.close();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        */
        /*
        client.putObject(bucket_output, tmp, file);
        */

        //File f1 = new File("/tmp");
        //TransferManager trans = new TransferManager();
        TransferManager trans = TransferManagerBuilder.standard().build();
        MultipleFileUpload xfer = trans.uploadDirectory(bucket_output, null, file, true);
        try{
            xfer.waitForCompletion();
        } catch (InterruptedException i){
            i.printStackTrace();
        }
        //xfer.waitForCompletion();
        
        //trans.uploadDirectory(bucket_output, "", file, false);
        
        //upload by file
        /*
        File file = null;
        byte[] buf = new byte[1024];
        try{
            OutputStream out = new FileOutputStream(file);
            out.write(buf, 0, 10);
        }catch (Exception e){
            e.getStackTrace();
        }
        client.putObject(bucket_output, "2.txt", file);
        */
        File ff = new File("");
        
        int num = 0;
        //for(String str : file.list()) ++num;

        //String s = p.toString();
        
        //int age = request.calcs;
        int age = num;
        //String name = tmp + "!!!!!!";
        String name = ff.getAbsolutePath();
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