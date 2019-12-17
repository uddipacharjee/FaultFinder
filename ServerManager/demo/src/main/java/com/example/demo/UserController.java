package com.example.demo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//import javax.servlet.ServletContext.*;
import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    @Autowired
    private HttpServletRequest request;
  
    private String returnText="";
    
    @RequestMapping(value="/users", method=RequestMethod.POST)
    @ResponseBody
    public String getMessage(@RequestParam("file") MultipartFile multipartFile,String className,String testClass) throws Exception {
      
        String filePath = request.getServletContext().getRealPath("/");
        File f1 = new File(filePath+"/"+multipartFile.getOriginalFilename());
        multipartFile.transferTo(f1);

        String zipFilePath = f1.getAbsolutePath();
        //System.out.println("class: "+className+"  test : "+testClass);
        //System.out.println("path: "+zipFilePath);
        //System.out.println(className+"  test : "+testClass);
        unzip(zipFilePath);
        writeFilesNameInBin(className,testClass);
        copyFilesTOBin();
        executeBin();
        //System.out.println("here");
        //ClassTester c=new ClassTester();
        //c.run();
        //makeJar();
        //System.out.println("End");
        
        return returnText;
        
       //return f1.getName();
        
    }
    
  private void writeFilesNameInBin(String className, String testClass) throws IOException {
      BufferedWriter bw=new BufferedWriter(new FileWriter("C:\\Users\\Hp\\eclipse-workspace\\FaultFinder\\bin\\names.txt"));
      bw.write(className+"\n");
      bw.write(testClass);
      bw.close();
  }

  public void executeBin() throws IOException {
      Process proc = Runtime.getRuntime().exec("java -cp \"C:\\Users\\Hp\\eclipse-workspace\\FaultFinder\\bin\" analysis.ClassTester");
		
		  BufferedReader stdInput = new BufferedReader(new 
			InputStreamReader(proc.getInputStream()));

			BufferedReader stdError = new BufferedReader(new 
			InputStreamReader(proc.getErrorStream()));

			// Read the output from the command
			//System.out.println("Here is the standard output of the command:\n");
      String s="";
      returnText="";
			while ((s = stdInput.readLine()) != null) {
          System.out.println(s);
          returnText+=s+"\n";
			}

			// Read any errors from the attempted command
			//System.out.println("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
			    System.out.println(s);
			}
    }


  public void copyFilesTOBin() throws IOException {
      File src=new File("C:\\\\Users\\\\Hp\\\\Documents\\\\spring3\\\\demo\\\\source");
      File dest=new File("C:\\Users\\Hp\\eclipse-workspace\\FaultFinder\\bin");
      File[] listOfFiles = src.listFiles();
      for (File file : listOfFiles) {
        FileUtils.copyDirectoryToDirectory(file,dest);
      }
  }
  public void makeJar() throws IOException
	{
	  Manifest manifest = new Manifest();
	  manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
	  JarOutputStream target = new JarOutputStream(new FileOutputStream("output.jar"), manifest);
	  add(new File("C:\\Users\\Hp\\eclipse-workspace\\FaultFinder"), target);
	  target.close();
	}

	private void add(File source, JarOutputStream target) throws IOException
	{
	  BufferedInputStream in = null;
	  try
	  {
	    if (source.isDirectory())
	    {
	      String name = source.getPath().replace("\\", "/");
	      if (!name.isEmpty())
	      {
	        if (!name.endsWith("/"))
	          name += "/";
	        JarEntry entry = new JarEntry(name);
	        entry.setTime(source.lastModified());
	        target.putNextEntry(entry);
	        target.closeEntry();
	      }
	      for (File nestedFile: source.listFiles())
	        add(nestedFile, target);
	      return;
	    }

	    JarEntry entry = new JarEntry(source.getPath().replace("\\", "/"));
	    entry.setTime(source.lastModified());
	    target.putNextEntry(entry);
	    in = new BufferedInputStream(new FileInputStream(source));

	    byte[] buffer = new byte[1024];
	    while (true)
	    {
	      int count = in.read(buffer);
	      if (count == -1)
	        break;
	      target.write(buffer, 0, count);
	    }
	    target.closeEntry();
	  }
	  finally
	  {
	    if (in != null)
	      in.close();
	  }
	}

    private static void unzip(String zipFilePath) {
        try {
            // Open the zip file
            ZipFile zipFile = new ZipFile(zipFilePath);
            Enumeration<?> enu = zipFile.entries();
            
            while (enu.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) enu.nextElement();

                String name = zipEntry.getName();
                long size = zipEntry.getSize();
                long compressedSize = zipEntry.getCompressedSize();
            /*
                System.out.printf("name: %-20s | size: %6d | compressed size: %6d\n", 
                        name, size, compressedSize);
            */
                // Do we need to create a directory ?
                File file = new File(name);
            //    System.out.println("name "+name);
            //    System.out.println("unzipped "+file.getAbsolutePath());
                if (name.endsWith("/")) {
                    file.mkdirs();
                    continue;
                }

                File parent = file.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
            //    System.out.println("abcaaaaaaaaaaaaaaaaaaa "+parent.getName());
                // Extract the file
                InputStream is = zipFile.getInputStream(zipEntry);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = is.read(bytes)) >= 0) {
                    fos.write(bytes, 0, length);
                }
                is.close();
                fos.close();

            }
            zipFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}