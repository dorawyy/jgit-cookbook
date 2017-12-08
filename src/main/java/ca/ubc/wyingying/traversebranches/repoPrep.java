package ca.ubc.wyingying.traversebranches;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

public class repoPrep {


    // Repo info
    public String repoName;
    public String localDir;
    public File gitFile ;
    
    
    public static Git git;
    
    public File outputFile; 
    public String outputDir;
    
    // public FileOutputStream outputStream = setUpOutputFile(outputDir, outputFile);
    
    
    // repo level stats
    public int commitCount; // number of commits of the whole rpeo      
    public int mergeCount; // number of all merge commits of the whole repo
    public int twoPrtsMergeCount; // number of two parents merge commits of the whole repo
    public int textConflictCount; // number of textual conflicts merge commits of the whole repo
    public int potenSemanConfCount; // number of potential merge conflicts merge commits of the whole repo

    
    
    // all commits of the repo
    public Set<RevCommit> repoCommits = new HashSet<>();
    public Set<commitPrep> commitsPrepSet = null;
  
  
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// methods about List<commitPrep> allCommitsPrep  /////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /***
     * Return true if the prepCommit is already added to the Repo Commit List
     * @param commit
     * @return
     */
    public boolean containCommitPrep (commitPrep commit)
    {
        
            
        if(this.commitsPrepSet.contains(commit) == true)
            return true;
        return false;
    }
    
    
    /***
     * Add new commitPrep to List<commitPrep> allCommitsPrep
     */
    public void addCommitPrep(commitPrep commit)
    {
        if( this.containCommitPrep(commit) == false) {
            this.commitsPrepSet.add(commit);
        }
        
    }
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
  
    /***
     * main function
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }
    

 
    
    
    /***
     * get all remote-tracking branches, aka. execute `git branch -r`
     * @param outputStream
     * @param gitObject
     * @return
     * @throws GitAPIException
     * @throws IOException
     */
    public static List<Ref> getRemoteBranches(FileOutputStream outputStream) throws GitAPIException, IOException{
        // get all remote tracking branches
        
        String str = "Listing all remote branches:\n";
        System.out.println(str);
        // output to file
        outputStream.write(str.getBytes());
        
        List<Ref> remoteBranches = git.branchList().setListMode(ListMode.REMOTE).call();
        return remoteBranches;
    }
    
    
    
    
    /***
     * get all (local + remote-trancking) branches, aka. execute `git branch -a`
     * @param outputStream
     * @param gitObject
     * @return
     * @throws GitAPIException
     * @throws IOException
     */
    public static List<Ref> getAllBranches(FileOutputStream outputStream) throws GitAPIException, IOException{
        // get all remote tracking branches
        
        String str = "Listing all branches:\n";
        System.out.println(str);
        // output to file
        outputStream.write(str.getBytes());
        
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
    public static List<Ref> getLocalBranches(FileOutputStream outputStream) throws GitAPIException, IOException{
        // get all remote tracking branches
        
        String str = "Listing all local branches:\n";
        System.out.println(str);
        // output to file
        outputStream.write(str.getBytes());

        List<Ref> localBranches =  git.branchList().call();
        return localBranches;
    }
    
    
    
    
    /***
     * return all tags
     * @param outputStream
     * @param gitObject
     * @return
     * @throws GitAPIException
     * @throws IOException 
     */
    public static List<Ref> getAllTags(FileOutputStream outputStream,  Git gitObject) throws GitAPIException, IOException{
        // get all remote tracking branches
        
        String str = "Listing all tags:\n";
        System.out.println(str);
        // output to file
        outputStream.write(str.getBytes());

        List<Ref> allTags = gitObject.tagList().call();
        return allTags;
    }
    

    
    
    
    //------------ Constructor --------------------//
    
    
    /***
     * Customized constructor
     * @throws IOException 
     */
    public repoPrep(String localDir, String outputDir, String repoName) throws IOException{
        
        this.repoName = repoName;
        this.localDir = localDir;
       
        this.gitFile = new File( this.localDir +"/.git");
        repoPrep.git = Git.open(this.gitFile);
        
        //initialization of repo-level statistics
        this.commitCount = 0;
        this.mergeCount = 0;
        this.twoPrtsMergeCount = 0;
        this.textConflictCount = 0;
        this.potenSemanConfCount = 0;
        
        // output directory and files set up
        this.outputDir = outputDir;
        this.outputFile = new File(this.outputDir + this.repoName);
    }
    
    
  


    
    public String getRepoName() {
        return repoName;
    }



    
    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }



    
    public String getLocalDir() {
        return localDir;
    }



    
    public void setLocalDir(String localDir) {
        this.localDir = localDir;
    }



    
    public File getGitFile() {
        return gitFile;
    }



    
    public void setGitFile(File gitFile) {
        this.gitFile = gitFile;
    }



    
    public Git getGit() {
        return git;
    }



    
    public void setGit(Git git) {
        repoPrep.git = git;
    }



    
    public File getOutputFile() {
        return outputFile;
    }



    
    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }



    
    public String getOutputDir() {
        return outputDir;
    }



    
    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }



    
    public int getCommitCount() {
        return commitCount;
    }



    
    public void setCommitCount(int commitCount) {
        this.commitCount = commitCount;
    }

    
    /***
     * this.commitCount ++
     */
    public void increCommitCount()
    {
        this.commitCount ++;
    }


    
    public int getMergeCount() {
        return mergeCount;
    }

    
    public void setMergeCount(int mergeCount) {
        this.mergeCount = mergeCount;
    }


    /***
     * this.mergeCount ++
     */
    public void increMergeCount()
    {
        this.mergeCount ++;
    }
    
    
    public int getTwoPrtsMergeCount() {
        return twoPrtsMergeCount;
    }

    
    public void setTwoPrtsMergeCount(int twoPrtsMergeCount) {
        this.twoPrtsMergeCount = twoPrtsMergeCount;
    }


    /***
     * this.twoPrtsMergeCount ++
     */
    public void increTwoPrtsMergeCount()
    {
        this.twoPrtsMergeCount ++ ;
    }
    
    

    
    public int getTextConflictCount() {
        return textConflictCount;
    }

    
    public void setTextConflictCount(int textConflictCount) {
        this.textConflictCount = textConflictCount;
    }


    /***
     * this.textConflictCount ++
     */
    public void increTextConflictCount()
    {
        this.textConflictCount ++ ;
    }

    
    public int getPotenSemanConfCount() {
        return potenSemanConfCount;
    }



    
    public void setPotenSemanConfCount(int potenSemanConfCount) {
        this.potenSemanConfCount = potenSemanConfCount;
    }

    
    /***
     * this.potenSemanConfCount ++
     */
    public void increPotenSemanConfCount()
    {
        this.potenSemanConfCount ++ ;
    }


    
    public Set<RevCommit> getRepoCommits() {
        return repoCommits;
    }



    
    public void setRepoCommits(Set<RevCommit> repoCommits) {
        this.repoCommits = repoCommits;
    }
    
    
    
    /***
     * add the commit in parameter to the repoCommits list
     * @param commit
     */
    public void addRepoCommits(RevCommit commit)
    {
        this.repoCommits.add(commit);
    }
    
    
    /***
     * add the commit in parameter to the repoCommits list
     * @param commit
     */
    public boolean isContainedInRepoCommits(RevCommit commit)
    {
        if(this.repoCommits.contains(commit))
            return true;
        return false;
    }


    
    public Set<commitPrep> getCommitsPrepSet() {
        return commitsPrepSet;
    }


    
    public void setCommitsPrepSet(Set<commitPrep> commitsPrepSet) {
        this.commitsPrepSet = commitsPrepSet;
    }
    
    
}
