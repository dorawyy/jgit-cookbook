package ca.ubc.wyingying.traversebranches;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

public class listBranches {
    
    public static Git git;
    
    /***
     * return all local branches
     * @return
     * @throws GitAPIException
     */
    public static List<Ref> getLocalBranches() throws GitAPIException{
        // get all local branches
        System.out.println("Listing all local branches:");
        List<Ref> call = git.branchList().call();
        return call;
    }
    
    
    
    
    /***
     * return all remote branches
     * @param gitObejct
     * @return
     * @throws GitAPIException
     * @throws IOException 
     */
    public static List<Ref> getRemoteBranches() throws GitAPIException, IOException{
        // get all local branches
        System.out.println("Listing all remote branches:\n");
        
        // String str = "Listing all remote branches:\n";
                
        // byte[] strToBytes = str.getBytes();
        // outputStream.write(strToBytes);
        
        List<Ref> call = git.branchList().setListMode(ListMode.REMOTE).call();
        return call;
    }
    
    
    
    /***
     * return all branches (local+remote)
     * @param gitObject
     * @return
     * @throws GitAPIException
     */
    public static List<Ref> getAllBranches() throws GitAPIException{
        // get all local branches
        System.out.println("Listing all branches:");
        List<Ref> call = git.branchList().setListMode(ListMode.ALL).call();
        return call;
    }
    
  
}
