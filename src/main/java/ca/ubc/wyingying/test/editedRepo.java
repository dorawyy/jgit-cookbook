package ca.ubc.wyingying.test;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;



/***
 * @Description: repackage the repository, with all types of stats
 * 
 */


public class editedRepo {

    // Repo info
    public String repoName;
    public String localDir;
    public File gitFile ;
    
    
    public static Git git;
    
    public File outputFile; 
    public String outputDir;
    
    
    
    // repo level stats
    public int commitCount; // number of commits of the whole rpeo      
    public int mergeCount; // number of all merge commits of the whole repo
    public int twoPrtsMergeCount; // number of two parents merge commits of the whole repo
    public int textConflictCount; // number of textual conflicts merge commits of the whole repo
    public int potenSemanConfCount; // number of potential merge conflicts merge commits of the whole repo

    
    
    // all commits of the repo
    public List<editedCommit> editedCommitsSet ;
  
    
    
    /***
     * Return true if the editedCommit is already added to the Repo Commit List
     * @param editedCommit
     * @return
     */
    public boolean containEditedCommit(editedCommit commit)
    {    
        for(editedCommit editedcommit: this.editedCommitsSet)
        {
            if(editedcommit.getRevCommit().getId() == commit.getRevCommit().getId())
                return true;
            }
        return false;
    }
    
    
    
    
    /***
     * Return true if the editedCommit is already added to the Repo Commit List
     * @param RevCommit
     * @return
     */
    public boolean containRevCommit(RevCommit revcommit){
        if(this.editedCommitsSet != null){
            for(editedCommit editedcommit: this.editedCommitsSet)
            {
                if(editedcommit.getRevCommit().equals(revcommit))
                    return true;
                }
            }
        return false;
    }
    
  
    /***
     * Return the editedCommit if it is contained in the repository already
     * @param commit
     * @return
     */
    public editedCommit findEditedCommitByEditedCommit(editedCommit commit)
    {    
        for(editedCommit editedcommit: this.editedCommitsSet)
        {
            if(editedcommit.getRevCommit().equals(commit.getRevCommit()))
                return editedcommit;
            }
        return null;
    }
     
    
    /***
     * Return the editedCommit if it is contained in the repository already
     * @param commit
     * @return
     */
    public editedCommit findEditedCommitByRevCommit(RevCommit commit)
    {    
        for(editedCommit editedcommit: this.editedCommitsSet)
        {
            //if(editedcommit.getRevCommit().getId() == commit.getId())
            if(editedcommit.getRevCommit().equals(commit))
                return editedcommit;
            }
        return null;
    }
     
    
    
    
    /***
     * Add new editedCommit to List<commitPrep> editedCommitsSet
     */
    public void addEditedCommitToEditedCommitsSet(editedCommit commit)
    {
        if( this.containEditedCommit(commit) == false) {
            this.editedCommitsSet.add(commit);
            
            // update stats based on the type of this merge
            this.increCommitCount();
            if(commit.isMergeCommit()){
                this.increMergeCount();
                if(commit.is2ParentsMergeCommit()){
                    this.increTwoPrtsMergeCount();
                }
            }
        }
        
    }
    
    
    

    
    /***
     * Output all commits info of the editedRepo to a file
     * @param repositroyPrep
     * @return
     * @throws GitAPIException
     * @throws IOException
     */
    public void printRepoAllCommits(String fileDir) throws IOException{
        String outFileName = fileDir + this.repoName + "_AllCommits.txt";
        File outfile = new File(outFileName);
        FileOutputStream outputStream = new FileOutputStream(outfile);
        
        StringBuilder repoStats = new StringBuilder();
        repoStats.append("RepoName is:" + repoName +"\n \n");
        
        for(editedCommit editedcommit: this.editedCommitsSet){
            repoStats.append(editedcommit.printEditedCommit());
            }
        repoStats.append("\n");
        
        outputStream.write(repoStats.toString().getBytes());
        outputStream.close();

    }
    
    
    
    /***
     * Output all commits info of the editedRepo to a file
     * @param repositroyPrep
     * @return
     * @throws GitAPIException
     * @throws IOException
     */
    public void printRepoStats(String fileDir) throws IOException{
        String outFileName = fileDir + this.repoName + "_RepoStats.txt";
        File outfile = new File(outFileName);
        FileOutputStream outputStream = new FileOutputStream(outfile);
        
        StringBuilder repoStats = new StringBuilder();
        repoStats.append("RepoName is:" + repoName +"\n \n");
        
        repoStats.append("    1. repo total number of commits: "+ commitCount + "\n");
        repoStats.append("    2. repo total number of merge commits (parents >= 2): "+ mergeCount + "\n");
        repoStats.append("    3. repo total number of merge commits (parents = 2): "+ twoPrtsMergeCount + "\n");
        repoStats.append("    4. repo total number of textual conflicts: "+ textConflictCount + "\n");
        repoStats.append("    5. repo total number of potential semantic conflicts: "+ potenSemanConfCount + "\n");

        String analysisFinishStr = "Repo "+repoName+" all commits scan finish!\n"; 
        repoStats.append(analysisFinishStr);
        
        System.out.println(analysisFinishStr);
        repoStats.append("\n");
        
        outputStream.write(repoStats.toString().getBytes());
        outputStream.close();

    }
    
    
    
    
    
    //------------ Constructor --------------------//
    
    
    /***
     * Customized constructor
     * @throws IOException 
     */
    public editedRepo(String localDir, String outputDir, String repoName) throws IOException{
        
        this.repoName = repoName;
        this.localDir = localDir;
       
        this.gitFile = new File( this.localDir +"/.git");
        editedRepo.git = Git.open(this.gitFile);
        
        //initialization of repo-level statistics
        this.commitCount = 0;
        this.mergeCount = 0;
        this.twoPrtsMergeCount = 0;
        this.textConflictCount = 0;
        this.potenSemanConfCount = 0;
        
        // output directory and files set up
        this.outputDir = outputDir;
        this.outputFile = new File(this.outputDir + this.repoName);
        
        
        this.editedCommitsSet = new ArrayList<>();
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
        editedRepo.git = git;
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




    
    public List<editedCommit> getEditedCommitsSet() {
        return editedCommitsSet;
    }




    
    public void setEditedCommitsSet(List<editedCommit> editedCommitsSet) {
        this.editedCommitsSet = editedCommitsSet;
    }



    
}
