package dora;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class ExploreRepo {
    //private Git git;
   
    
    
    /***
     * buid a repo with .git location 
     * @param gitDir
     * @return
     * @throws IOException
     */
    public Repository setUpRepo(File gitDir) throws IOException{
        FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
        //Require the repository to exist before it can be opened.
        //Set the Git directory storing the repository metadata.
        repoBuilder.setMustExist(true).setGitDir(gitDir);
        
        // after setting up everything, use repoBuilder.build() to get the repo
        Repository repo = repoBuilder.build();
        
        return repo;     
    }
    
    
    
    /***
     * Given the .git location, set up the connection to the repo
     * But in this way, it cannot make sure the repo exists; prefer to use FileRepositoryBuilder
     * @param gitDir
     * @return
     * @throws IOException
     */
    public Git openGit(File gitDir) throws IOException{
        return Git.open(gitDir);
    }
   
    
    
    /***
     * git log 
     * @return
     * @throws NoHeadException
     * @throws GitAPIException
     */
    public Iterable<RevCommit> gitLog(Git git) throws NoHeadException, GitAPIException{
        Iterable<RevCommit> iterable = git.log().call();
        return iterable;
        // the returned iterator can be used to loop over all commits that are found by the git log command
    }
    
    
    
    /***
     * return the repo related to the Git object
     * @return
     */
    public Repository getRepoFromGit(Git git){
        return git.getRepository();
    }
    
    
    /***
     * recursively return all commit messages, starting from the head commit, of the branch you provided as param
     * @param repository
     * @param branch
     * @throws RevisionSyntaxException
     * @throws AmbiguousObjectException
     * @throws IncorrectObjectTypeException
     * @throws IOException
     */
    public RevWalk GetAllCommits(Repository repository, String branch) 
            throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException{
        // revWalk the repo
        try (RevWalk revWalk = new RevWalk(repository)){
            // get the head of side-branch
            ObjectId commitId = repository.resolve("refs/heads/"+branch);
            // Mark the commit to start graph traversal from.
            // parseCommit() will return a RevCommit, containing all info about the commit; mark the head as start point
            revWalk.markStart(revWalk.parseCommit(commitId));
            
            // reutrn revWalk
            return revWalk;
           
        }  
    }
    
    
    
    
    /***
     * do whatever you like to all commit of the branch
     * @param revWalk
     */
    public void repoOperations(RevWalk revWalk){
        for (RevCommit commit : revWalk){
         // for each commit in the revWalk, return its commit message
            System.out.println(commit.getFullMessage());
            }
    }
    
    // revwalk can also be configured to filter commits, either by
    // (1) matching attributes of the commit object itself, or by
    // (2) matching paths of the directory tree that it represents
    
    //================================
    // how to access a git repo with JGit
    
    
}
