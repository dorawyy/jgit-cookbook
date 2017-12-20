package ca.ubc.wyingying.parserepo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class output {

    
    /***
     * print an output file, given a path and a filename
     * @param filePath
     * @param fileName
     * @return
     * @throws IOException
     */
    public static File createFile(String filePath, String fileName) throws IOException{
        File file = Paths.get(filePath,File.separator, fileName).toFile();
        
        // if not exist, create the file
        if (!file.exists()){
            System.out.println("File " + file.toString() + " not exist, create file;");
            file.createNewFile();
        }
        
        // now, if the file exist, but not a File type, then create a File
        if(file.exists()  && file.isFile()){
            System.out.println("file exists, and it is not a file; now, create a file;");
            file.createNewFile();
        }
        return file;
    }
    
    
    
    /***
     * create a path
     * @param filePath
     * @param fileName
     * @return
     * @throws IOException
     */
    public static boolean createPath(String filePath) throws IOException{
        boolean exist=false;
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                //fail to create directory
                e.printStackTrace();
            }
        }
        
        if(Files.exists(path)==true && path.toFile().isDirectory() == true)
            exist=true;
        
        return exist;
    }
    
    
    /***
     * set up the outputStream for output strings
     * @param file
     * @return
     */
    public static OutputStream setupStream(File file){
        OutputStream oStream = null;
        try{
            oStream = new FileOutputStream(file);
        }
        catch(IOException e) {
            e.printStackTrace();
            }
        return oStream;
    }
    
    
    
    /***
     * output string to the stream
     * @param data
     * @param os
     */
    public static void outputToFileUsingStream(String data, OutputStream os) {
        try {
            os.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    
    /***
     * close the outputStream when all strings are already output
     * @param file
     * @return
     */
    public static void closeStream(OutputStream os){
        try{
            os.close();
        }
        catch(IOException e) {
            e.printStackTrace();
            }
    }
    
    
    
    
    /***
     * delete a folder and data inside the folder
     * @param path
     */
    public static void deleteDirectory(String path) {
        
        File file  = new File(path);
        if(file.isDirectory()){
            String[] childFiles = file.list();
            if(childFiles == null) {
                //Directory is empty. Proceed for deletion
                file.delete();
            }
            else {
                //Directory has other files.
                //Need to delete them first
                for (String childFilePath :  childFiles) {
                    //recursive delete the files
                    deleteDirectory(childFilePath);
                }
            }
             
        }
        else {
            System.out.print(path+ " is a file, please use function 'deleteFile()' instead.");
        }
         
    }
    
    
    
    /***
     * delete a folder(with/without data inside)
     * @param path
     */
    public static void deleteFile(String path) {
        File file  = new File(path);
        if(file.exists() && file.isFile()) {
            //it is a simple file. Proceed for deletion
            file.delete();
            System.out.println("deleted file "+path);
        }
         
    }  
    
    
}




