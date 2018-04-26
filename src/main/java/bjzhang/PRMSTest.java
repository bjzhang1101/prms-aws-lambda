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
import java.io.*;
import java.util.UUID;


/**
 * bjzhang.PRMSTest::handleRequest
 * @author zumboboga
 */
public class PRMSTest implements RequestHandler<Request, Response>{

    //private static String bucket_output = "uwt-prms-output";
    //private static String bucket_input = "uwt-prms-input";
    private static String bucket_input = "uwt-prms";
    private static String bucket_output = "uwt-prms";
    
    public Response handleRequest(Request request, Context context){
        
        String uuid = request.uuid;
        
        //pull data from S3
        pullData(bucket_input, uuid);
        
        // Initialize viarables  
        int newcontainer = 0;
        CpuTime c1 = getCpuUtilization();
        VmCpuStat v1 = getVmCpuStat();
        
        // Run PRMS 
        runPRMS();
        
        // After running PRMS
        CpuTime c2 = getCpuUtilization();
        VmCpuStat v2 = getVmCpuStat();
        CpuTime cused = getCpuTimeDiff(c1, c2);
        VmCpuStat vused = getVmCpuStatDiff(v1, v2);
        long vuptime = getUpTime(v2);
        String fileout = request.name;
        
        // Response
        Response res = new Response(fileout, uuid, cused.utime, cused.stime, cused.cutime, cused.cstime, vused.cpuusr,
                                  vused.cpunice, vused.cpukrn, vused.cpuidle, vused.cpuiowait, vused.cpuirq, 
                                  vused.cpusirq, vused.cpusteal, vuptime, newcontainer);
        res.setPid(getPID());
        
        // upload the output file to S3
        //pushData("/tmp", bucket_output, null, false);
        pushData("/tmp/output/Efcarson", bucket_output, uuid, true);
        
        return res;
    }
   
    
    public static void pushData(String dir_path, String bucket_name,
            String key_prefix, boolean recursive)
    {
        TransferManager xfer_mgr = TransferManagerBuilder.standard().build();
        try {
            MultipleFileUpload xfer = xfer_mgr.uploadDirectory(bucket_name,
                    key_prefix, new File(dir_path), recursive);
            xfer.waitForCompletion();
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        } catch (InterruptedException ii){
            ii.printStackTrace();
        }
        xfer_mgr.shutdownNow();
    }
    
    public static void pullData(String bucket_input, String uuid){
        
        AmazonS3 client = new AmazonS3Client();

        //pull data.csv
        S3Object x1 = client.getObject(bucket_input, uuid + "/data.csv");
        InputStream contents = x1.getObjectContent();
       
        File f1 = new File("/tmp/data.csv");
        try{
            Files.copy(contents, f1.toPath());
        } catch (IOException i){
            i.printStackTrace();
        }
        
        //pull mixed_params.csv
        S3Object x2 = client.getObject(bucket_input, uuid + "/mixed_params.csv");
        InputStream contents2 = x2.getObjectContent();
        
        File f2 = new File("/tmp/mixed_params.csv");
        try{
            Files.copy(contents2, f2.toPath());
        } catch (IOException i){
            i.printStackTrace();
        }
        
        //pull efcarson.sim
        S3Object x3 = client.getObject(bucket_input, uuid + "/efcarson.sim");
        InputStream contents3 = x3.getObjectContent();
        
        File f3 = new File("/tmp/efcarson.sim");
        try{
            Files.copy(contents3, f3.toPath());
        } catch (IOException i){
            i.printStackTrace();
        }
    }
    
    public static void runPRMS(){
        // PRMS project home dir (Change this path to reflect your path to "prmsprj")
        String prj = "/tmp";

        // Pass the project home to OMS
        System.setProperty("oms_prj", prj);

        // runs the sim file via OMS CLI in a certain logging level
        oms3.CLI.main(new String[]{
            "-l", "OFF", // can be "OFF", "ALL", .... (all JDK loging levels)
            "-r", prj + "/efcarson.sim" // runs the sim file in project home.
        });
    }
    
    class CpuTime
    {
        long utime;
        long stime;
        long cutime;
        long cstime;
        CpuTime(long utime, long stime, long cutime, long cstime)
        {
            this.utime = utime;
            this.stime = stime;
            this.cutime = cutime;
            this.cstime = cstime;
        }
        CpuTime()
        {            
        }
        
        @Override
        public String toString()
        {
            return "utime=" + utime + " stime=" + stime + " cutime=" + cutime + " cstime=" + cstime + " ";
        }
    }
    
    public CpuTime getCpuUtilization()
    {
        String filename = "/proc/1/stat";
        File f = new File(filename);
        Path p = Paths.get(filename);
        String text = "";
        StringBuffer sb = new StringBuffer();
        if (f.exists()) 
        {
            try (BufferedReader br = Files.newBufferedReader(p))
            {
                text = br.readLine();
                br.close();
            }
            catch (IOException ioe)
            {
                sb.append("Error reading file=" + filename);
            }
            String params[] = text.split(" ");
            return new CpuTime(Long.parseLong(params[13]),
                               Long.parseLong(params[14]),
                               Long.parseLong(params[15]),
                               Long.parseLong(params[16]));
        }
        else
            return new CpuTime();
    }
    
    class VmCpuStat
    {
        long cpuusr;
        long cpunice;
        long cpukrn;
        long cpuidle;
        long cpuiowait;
        long cpuirq;
        long cpusirq;
        long cpusteal;
        long btime;
        VmCpuStat(long cpuusr, long cpunice, long cpukrn, long cpuidle, 
                  long cpuiowait, long cpuirq, long cpusirq, long cpusteal)
        {
            this.cpuusr = cpuusr;
            this.cpunice = cpunice;
            this.cpukrn = cpukrn;
            this.cpuidle = cpuidle;
            this.cpuiowait = cpuiowait;
            this.cpuirq = cpuirq;
            this.cpusirq = cpusirq;
            this.cpusteal = cpusteal;
        }
        VmCpuStat() { }
    }
    
    public VmCpuStat getVmCpuStat()
    {
        String filename = "/proc/stat";
        File f = new File(filename);
        Path p = Paths.get(filename);
        String text = "";
        StringBuffer sb = new StringBuffer();
        if (f.exists()) 
        {
            try (BufferedReader br = Files.newBufferedReader(p))
            {
                text = br.readLine();
                String params[] = text.split(" ");
                VmCpuStat vcs = new VmCpuStat(Long.parseLong(params[2]),
                                              Long.parseLong(params[3]),
                                              Long.parseLong(params[4]),
                                              Long.parseLong(params[5]),
                                              Long.parseLong(params[6]),
                                              Long.parseLong(params[7]),
                                              Long.parseLong(params[8]),
                                              Long.parseLong(params[9]));
                while ((text = br.readLine()) != null && text.length() != 0) {
                    // get boot time in ms since epoch
                    if (text.contains("btime"))
                    {
                        String prms[] = text.split(" ");
                        vcs.btime = Long.parseLong(prms[1]);
                    }
                }
                br.close();
                return vcs;
            }
            catch (IOException ioe)
            {
                sb.append("Error reading file=" + filename);
                return new VmCpuStat();
            }
        }
        else
            return new VmCpuStat();
    }
    
    public VmCpuStat getVmCpuStatDiff(VmCpuStat v1, VmCpuStat v2)
    {
        return new VmCpuStat(v2.cpuusr - v1.cpuusr, v2.cpunice - v1.cpunice, v2.cpukrn - v1.cpukrn,
                             v2.cpuidle - v1.cpuidle, v2.cpuiowait - v1.cpuiowait, v2.cpuirq - v1.cpuirq,
                             v2.cpusirq - v1.cpusirq, v2.cpusteal - v1.cpusteal);
    }
    
    public CpuTime getCpuTimeDiff(CpuTime c1, CpuTime c2)
    {
        return new CpuTime(c2.utime - c1.utime, c2.stime-c1.stime, c2.cutime - c1.cutime, c2.cstime - c1.cstime);
    }
    
    public long getUpTime(VmCpuStat vmcpustat)
    {
        return vmcpustat.btime;
    }
    
    public int getPID() 
    {
        try
        {
            java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();
            java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
            jvm.setAccessible(true);
            sun.management.VMManagement mgmt = (sun.management.VMManagement) jvm.get(runtime);
            java.lang.reflect.Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
            pid_method.setAccessible(true);

            return ((Integer) pid_method.invoke(mgmt)).intValue();
        }
        catch (Exception e)
        {
            return 0;
        }
    } 
}