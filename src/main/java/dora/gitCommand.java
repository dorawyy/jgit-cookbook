package dora;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

public class gitCommand {
    
    
    /***
     * get all remote-tracking branches, aka. execute `git branch -r`
     * @return
     * @throws GitAPIException
     * @throws IOException
     */
    public static List<Ref> getRemoteBranches(Git git) throws GitAPIException, IOException{
        // get all remote tracking branches
        List<Ref> remoteBranches = git.branchList().setListMode(ListMode.REMOTE).call();
        return remoteBranches;
    }
    
    
    
    /***
     * get all (local + remote-tracking) branches, aka. execute `git branch -a`
     * @param outputStream
     * @param gitObject
     * @return
     * @throws GitAPIException
     * @throws IOException
     */
    public static List<Ref> getAllBranches(Git git) throws GitAPIException, IOException{
        // get all remote+local tracking branches        
        List<Ref> allBranches = git.branchList().setListMode(ListMode.ALL).call();
        return allBranches;
    }
    
  
    
    
    /***
     * return all local branches
     * @param outputStream
     * @param gitObject
     * @return
     * @throws GitAPIException
     * @throws IOException 
     */
    public static List<Ref> getLocalBranches(Git git) throws GitAPIException, IOException{
        // get all local  branches
        List<Ref> localBranches =  git.branchList().call();
        return localBranches;
    }
    
    
    
    
    /***
     * return all tags
     * @param git
     * @return
     * @throws GitAPIException
     * @throws IOException 
     */
    public static List<Ref> getAllTags(Git git) throws GitAPIException, IOException{
        // get all tags
        List<Ref> allTags = git.tagList().call();
        return allTags;
    }
    
  

    
    
}
