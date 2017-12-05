package dora;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.revwalk.RevCommit;

public class gitBasicCommands {
   //private static Git git;
    
    /***
     * git clone repo_uri local_dir 
     * @param repo_uri
     * @param local_dir
     * @return
     * @throws InvalidRemoteException
     * @throws TransportException
     * @throws GitAPIException
     */
    public static Git clone_repo(String repo_uri, File local_dir) throws InvalidRemoteException, TransportException, GitAPIException{
        Git git = Git.cloneRepository().setURI(repo_uri).setDirectory(local_dir).call();
        return git;
    }

    
    /***
     * git add <filepattern>
     * @param filepattern
     * @return
     * @throws NoFilepatternException
     * @throws GitAPIException
     */
    public static DirCache gitAdd(String filepattern, Git git) throws NoFilepatternException, GitAPIException{
     // git add <filepattern> to index
        // the filepattern given to addFilePattern() must be relative to the work directory root
        // if the path does not point to an existing file, it is simply ignored
        // by passing a '.', will add all files within the working dir recursively, but "*" is not supported yet
        DirCache index = git.add().addFilepattern(filepattern).call();
        
     // get the total number of files involved
        int totalFilesAdded = index.getEntryCount();
        System.out.println(totalFilesAdded);
        
        
        return index;
    }

    
    /***
     * git rm <filepattern>
     * @param filepattern
     * @return
     * @throws NoFilepatternException
     * @throws GitAPIException
     */
    public static DirCache gitRm(String filepattern, Git git) throws NoFilepatternException, GitAPIException{
     // git rm <filepattern> to index
        DirCache index = git.rm().addFilepattern(filepattern).call();
        
     // get the total number of files involved
        int totalFilesRmed = index.getEntryCount();
        System.out.println(totalFilesRmed);
        
        return index;
    }

    
   
    /***
     * git status of the repo
     * @return
     * @throws NoFilepatternException
     * @throws GitAPIException
     */
    public static Status gitStatus(Git git) throws NoFilepatternException, GitAPIException{
        Status status = git.status().call();
        return status;
    }

    
    /***
     * git status <filepattern>
     * example: gitCertainFileStatus("documentation") will return status of all files under the folder "documentation"
     * @param filepattern : must be either name a file or a directory; regExp is not supported yet
     * @return
     * @throws NoFilepatternException
     * @throws GitAPIException
     */
    public static Status gitCertainFilesStatus(String filepattern, Git git) throws NoFilepatternException, GitAPIException{
        Status status = git.status().addPath(filepattern).call();
        return status;
    }
  
    
    /***
     * Is the working tree/index clean?
     * @return
     * @throws NoFilepatternException
     * @throws GitAPIException
     */
    public static boolean isClean(Status status) throws NoFilepatternException, GitAPIException{
        return status.isClean();
    }
   
    
    /***
     * git commit -a <commitMsg>
     * @param args
     * @throws GitAPIException 
     * @throws NoFilepatternException 
     */
    public static RevCommit PopulateRepo(String commitMsg, Git git) throws NoFilepatternException, GitAPIException{   
        // git commit 
        RevCommit commit = git.commit().setMessage(commitMsg).call();
        return commit;
        
        // the returned RevCommit contains the commit with its message, author, committer, time stamp, and of course a pointer
        // to the tree of files and directories that constitute this commit
    }
    
  
}
