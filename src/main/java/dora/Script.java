
package dora;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.ListTagCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidMergeHeadsException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;



public class Script {
    
    private static Git git;
    private static FileOutputStream outputStream;
    // private static FileOutputStream outputStreamAllCommits;
    
    public static void main(String[] args) throws IOException, GitAPIException {
        
        
        
        
   // traverse all repos --> TO DO: allow text input, parse repo locs according to the text
   for(String arg : args ){
       
       
       
       //print startTime
       long startTime = System.currentTimeMillis() ;
       
       
       // parse localDir, outputDir, and repoName from arg
       String localDir = arg;
       String outputDir = "/Users/dora/SamSungRepos/Output/";
       
       
       /*
       int tagCount = 0;
       int annotatedTagCount = 0;
       int lightWeightTagCount = 0;
      */ 
       
        
       // get Repository Name
       int repoNameIndex = localDir.lastIndexOf("/");
       String repoName = localDir.substring(repoNameIndex+1).replace('/', '_');
       
       
       // create a configureRepo instance
       repoPrep buildRepo = new repoPrep(localDir, outputDir, repoName);
       
       git = buildRepo.getGit();
         
       outputStream = new FileOutputStream( buildRepo.getOutputFile() );
        
        
       // initialize hashSet for all commits of the repo
       Set<RevCommit> repoCommits = buildRepo.getRepoCommits();
      
        
       // declare repo name in result file
       String startTimeStr = "StartTime is: "+ startTime + "\n";
       System.out.print(startTimeStr);
       outputStream.write(startTimeStr.getBytes());
       
       
       System.out.print("Current repo is: "+ repoName);   
       outputStream.write(repoName.getBytes());
                
       // for each remote branch, find all merge commits
       // getRemoteBranches: git branch --remotes, list all in .git/ref/remotes
       
       int branchCount = 0;
       for(Ref ref: buildRepo.getRemoteBranches(outputStream)){
           branchCount ++; 
           // branchName
           String branchName = ref.getName();

           // output branch info
           String branchInfo = branchCount +". Current branch is: " + branchName +", here are merge commits on this branch: \n";
           System.out.println(branchInfo);
           outputStream.write(branchInfo.getBytes());
           
           branchPrep buildBranch = new branchPrep();

           // for each branch, get the revWalk, then iterate for all commits
           try (RevWalk revWalk = new RevWalk(git.getRepository())){ 
               // set up RevWalk
               ObjectId startCommitId = git.getRepository().resolve(branchName);
               revWalk.markStart(revWalk.parseCommit(startCommitId));
                
               // for all commits on the branch, create another 
               for (RevCommit commit : revWalk){
                    
                   // update #commits of the branch
                   buildBranch.increBranchCommitCount();
                    
                    // flag of whether the commit was in the repo commit set or not
                    boolean isContained = repoCommits.contains(commit);
                    
                    // if new commit, add it to the repo commits set
                    if ( buildRepo.isContainedInRepoCommits(commit) == false){
                        buildRepo.addRepoCommits(commit);
                        buildRepo.increCommitCount();
                        }
                    
                    // if merge commit
                    if(isMergeCommit(commit) == true)
                    {
                        if(isContained == false) 
                        {
                            // repo mergeCommit ++; 
                            buildRepo.increMergeCount();
                            }
                       
                        // branch merge commit ++
                        buildBranch.increBranchMergeCount();
                                   
                        
                       
                       // if is a merge commit with 2 parents
                       if( is2ParentsMergeCommit(commit) == true ){
                       
                           // print merge commit and its parents info
                           StringBuilder commitInfo = strCommitAndParentsInfo(commit, buildBranch.getBranch2ParentsMergeCount());
                           outputStream.write(commitInfo.toString().getBytes());
                           
                           // update statistics
                           buildBranch.increBranch2ParentsMergeCount();

                           if(isContained == false)
                           {
                               buildRepo.increTwoPrtsMergeCount();
                           }
                          
                           
                           
                           /////// --- formal replay process ---- /////////
                           
                           // reset first, ensuring "git checkout" works
                           AbortMerge(commit);
                           replayThreeWayMerge(commit);
                           
                           
                           
                           // if textual conflict, do nothing, but print out
                           if  (meetTextualConflict() == true)
                           {
                               // update statistics
                               buildBranch.increBranchTextualConfCount();
                               
                               if(isContained == false)
                               {
                                   buildRepo.increTextConflictCount();
                               }
                               
                               String textConfStr = "Merge commit "+ commit.getId().getName().substring(0, 9) +" have Textual conflicts. \n \n" ;
                               System.out.println(textConfStr);
                               outputStream.write(textConfStr.getBytes());

                           }
                           
                           // if no textual conflict, then "git diff"
                           else {    
                           
                               // Index revision
                               AbstractTreeIterator indexTreeIterator = IndexTreeIterator();
                               // Real Merge Commit revision
                               AbstractTreeIterator mergeCommitTreeIterator = SpecificCommitTreeIterator(commit);
                               // "git diff"
                               List<DiffEntry> diffEntries = gitDiff(mergeCommitTreeIterator, indexTreeIterator) ;
                               
                               
                               // if "git diff" not empty, then print results
                               if(isGitDiffEmpty(diffEntries)==false)
                               {
                                   //update statistics
                                   buildBranch.increBranchSemanticConfCount();
                                   
                                   if(isContained == false)
                                   {
                                       buildRepo.increPotenSemanConfCount();
                                   }
                                   
                                   
                                   // print all semantic conflicts info
                                   String potenSemanConfStr = "Potential semantic conflicts! \n" ;
                                   System.out.println(potenSemanConfStr);
                                   outputStream.write(potenSemanConfStr.getBytes());
                                   
                                   // print `git diff` result details
                                   printDiff(diffEntries);
                                   
                                   }
                               }
                           // abort merge, continue to the next merge commit
                           AbortMerge(commit); 
                           }
                       
                                 
                       }  } 
                }  
                
           // one branch finish here  
           StringBuilder branchStats = buildBranchStats(branchName, 
                   buildBranch.getBranchCommitCount(),
                   buildBranch.getBranchMergeCount(), 
                   buildBranch.getBranch2ParentsMergeCount(), 
                   buildBranch.getBranchTextualConfCount(), 
                   buildBranch.getBranchSemanticConfCount());
            
           // print brnach stats
           outputStream.write(branchStats.toString().getBytes());    
        }
      
       
       /*
       
       ///////////// till here, all operations based on branches are done. /////////////////////////// 
        
       ////////////////// Then, get all tags  /////////////////////////////////////
       
       // get the list of tags
       ListTagCommand listTags = git.tagList();
       List<Ref> tags = listTags.call();

       

       // stats about this tag, local variables
       int tagNewCommitCount = 0;
       int tagNewMergeCommitCount = 0;
       int tagNew2ParentsMergeCommitCount = 0;
       int tagNewTextualConfCount = 0;
       int tagNewPotenSemanConfCount = 0;
       
       
       
       
       // for each tag, get all related commits that belong to no branch
       for(Ref tag : tags)
       {
           tagCount ++ ;
      
           ObjectId tagPointCommitId ;
           // StringBuilder tagIntro= new StringBuilder() ; 
           
           String tagType = "";
           String tagIntro = "";
           
           // find the type of the tag
           
           if(isAnnotatedTag(tag) == true)
           {
               annotatedTagCount ++;
               tagType = "an annotated tag";
               
               // peel the TAG, and get the COMMIT the TAG is pointing to 
               Ref peeledTag = git.getRepository().peel(tag);
               tagPointCommitId = peeledTag.getPeeledObjectId();

           }
           
           else if(isLightWeightTag(tag) == true){
               
               lightWeightTagCount ++;
               tagType = "a lightweight tag";
               
               // directly get the COMMIT the TAG is point to
               tagPointCommitId = tag.getObjectId(); 
           }
           
           
           // else, warning, pass the current tag
           else{
               String waringMsg = " neither lightweight or annotated tag, requries invetigation \n";
               System.out.println(waringMsg);
               outputStream.write(waringMsg.getBytes());
               
               continue;
           }
          
           
           
           // print out TAG info
           tagIntro.append(tagCount+ ". The tag " + tag.getName() + " is "+ tagType +", points to commit: " + tagPointCommitId.getName().substring(0, 9) + "\n");
           // System.out.println(tagIntro.toString());
           outputStream.write(tagIntro.toString().getBytes());
           
           
           // git log TAG
           // Iterable<RevCommit> logs  = git.log().add(tagPointCommitId).call();
           Iterable<RevCommit> logs  = git.log().add(tag.getObjectId()).call();
         
            
           // iterate the list, operate on each commit
           for (RevCommit commit : logs)
           {
               
               // contained in the repoCommits? 
               boolean isContained = buildRepo.isContainedInRepoCommits(commit);
               
               // if contained, do nothing
               if (isContained == true) { break; }
               
               // if not contained, (--> target commits), then add to repoCommits
               buildRepo.addRepoCommits(commit);
               buildRepo.increCommitCount();
               
               // if merge commit? 
               
               tagNewCommitCount ++;      
               
               if(isMergeCommit(commit) == true)
               {
                   // if this is a merge commit, update stats
                   buildRepo.increMergeCount();
                   tagNewMergeCommitCount ++ ;
                   
                   // print commit and parents info
                   strCommitAndParentsInfo(commit, tagNewMergeCommitCount);
                   
                   // if 2-parents merge commit
                   if(is2ParentsMergeCommit(commit) == true)
                   {
                       
                       
                       // update stats
                       buildRepo.increTwoPrtsMergeCount();
                       tagNew2ParentsMergeCommitCount ++ ;
                       
                       // replay merge

                       AbortMerge(commit);
                       replayThreeWayMerge(commit);
                       
                       
                       
                       // if textual conflict, do nothing, but print out
                       if  (meetTextualConflict() == true)
                       {
                           buildRepo.increTextConflictCount();
                           tagNewTextualConfCount ++;
                           
                           String textConfStr = "On tag "+ tag.getName() +", the MERGE commit"+ commit.getId().getName().substring(0, 9) + " have Textual conflicts. \n \n" ;
                           System.out.println(textConfStr);
                           outputStream.write(textConfStr.getBytes());

                       }
                       
                       // if no textual conflict, then "git diff"
                       else {    
                       
                           // Index revision
                           AbstractTreeIterator indexTreeIterator = IndexTreeIterator();
                           // Real Merge Commit revision
                           AbstractTreeIterator mergeCommitTreeIterator = SpecificCommitTreeIterator(commit);
                           // "git diff"
                           List<DiffEntry> diffEntries = gitDiff(mergeCommitTreeIterator, indexTreeIterator) ;
                           
                           
                           // if "git diff" not empty, then print results
                           if(isGitDiffEmpty(diffEntries)==false)
                           {
                               //update statistics
                               buildRepo.increPotenSemanConfCount();
                               tagNewPotenSemanConfCount ++ ;
                               
                               // print all semantic conflicts info
                               String potenSemanConfStr = "On tag "+ tag.getName() + ", commit:" + commit.getId().getName().substring(0, 9) + " Potential semantic conflicts! \n" ;
                               System.out.println(potenSemanConfStr);
                               outputStream.write(potenSemanConfStr.getBytes());
                               
                               // print `git diff` result details
                               printDiff(diffEntries);
                               
                               }
                           }
                       // abort merge, continue to the next merge commit
                       AbortMerge(commit); 
                       
                       
                   } 
               }  
           } 
               
           
       }    
      

       // print stats of all tags
       StringBuilder buildTagsStats = buildTagStats(tagCount, 
               tagNewCommitCount, 
               tagNewMergeCommitCount, 
               tagNew2ParentsMergeCommitCount, 
               tagNewTextualConfCount, 
               tagNewPotenSemanConfCount);
       
       outputStream.write(buildTagsStats.toString().getBytes());
      */  
       
       
       
        
        // one repo analysis finish here
        StringBuilder buildRepoStats = buildRepoStats(repoName, 
                buildRepo.getCommitCount(), 
                buildRepo.getMergeCount(),  
                buildRepo.getTwoPrtsMergeCount(), 
                buildRepo.getTextConflictCount(), 
                buildRepo.getPotenSemanConfCount());
        // print repo stats
        outputStream.write(buildRepoStats.toString().getBytes());
        
        
        
        
        // print all commits of the repo to a new file suffix "_AllCommits"
        if (buildRepo.getCommitCount() == buildRepo.repoCommits.size()){
            File outputAllCommits = new File(outputDir + repoName +"_AllCommits");
            strRepoCommits(repoCommits, buildRepo.getCommitCount(), outputAllCommits);
            }
        // if number not matching, print all commits of the repo to original output file
        else
        {
            String commitCountNotMatchStr = "repository total commits number not match, something is wrong!";
            outputStream.write(commitCountNotMatchStr.getBytes());
        }
        
        
        
        //print endTime
        long endTime = System.currentTimeMillis();
        String endTimeStr = "Analysis finished time: " +endTime + "\n";
        System.out.print(endTimeStr);
        outputStream.write(endTimeStr.getBytes());
        
        
        // close the outputStream
        outputStream.close();
        }
   }

    
   
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
        
        String str = "Listing all remote branches:\n";
                
        byte[] strToBytes = str.getBytes();
        outputStream.write(strToBytes);
        
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
    
  
    
    
    
    /***
     * get the revision of Working Tree
     * @param repository
     * @return
     */
    public static AbstractTreeIterator WorkingTreeIterator(){
        return new FileTreeIterator(git.getRepository());
    }
    
    
    /***
     * get the Index revision
     * @param repository
     * @return
     * @throws NoWorkTreeException
     * @throws CorruptObjectException
     * @throws IOException
     */
    public static AbstractTreeIterator IndexTreeIterator() throws NoWorkTreeException, CorruptObjectException, IOException{
        return new DirCacheIterator(git.getRepository().readDirCache());
    }
 
    
    /***
     * get the TreeIterator of a specific commit
     * @param repository
     * @param commit
     * @return
     * @throws IncorrectObjectTypeException
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public static AbstractTreeIterator SpecificCommitTreeIterator(RevCommit commit) throws IncorrectObjectTypeException, IOException{
        ObjectId treeId = commit.getTree().getId();
        ObjectReader reader = git.getRepository().newObjectReader() ;
        return new CanonicalTreeParser( null, reader, treeId); 
    }
    
    
    
    /*** command: git reset --hard destCommit
     * There is no direct git merge --abort, thus using git reset --hard
     * abort merge 
     * @param gitObejct
     * @param destCommit
     * @return
     * @throws IOException
     * @throws CheckoutConflictException
     * @throws GitAPIException
     */
    public static void AbortMerge(RevCommit destCommit) throws IOException, CheckoutConflictException, GitAPIException{

        // clear the merge state
        //git.getRepository().writeMergeCommitMsg( null );
        //git.getRepository().writeMergeHeads( null );
        // reset the index and work directory to HEAD
        git.reset().setMode(ResetType.HARD).setRef(destCommit.getId().getName()).call();

    }
    
    
    
    /***
     * run command "git status", to check if there is textual conflict or not 
     * @param gitObject
     * @return 
     * @throws NoWorkTreeException
     * @throws GitAPIException
     */
    public static boolean meetTextualConflict() throws NoWorkTreeException, GitAPIException
    {
        if(git.status().call().getConflicting().isEmpty())
            return false;
        return true;
       
        
    }

    
    
    /***
     * print commit id of: (1) the merge commit (2) all its parents
     * @param mergeCommit
     * @param mergeCommitCount
     */
    public static StringBuilder strCommitAndParentsInfo(RevCommit commit, int commitNO){
        
        StringBuilder commitAndParentInfoStr = new StringBuilder();
        int parentCount = commit.getParentCount();
        String commitInfo = "";
 
        // for regular commits
        if(parentCount < 2)
        {
            commitInfo = commitNO + ". commitId: "+ commit.getId().getName().substring(0, 9) + "; commitTime: "+commit.getCommitTime()+ ";" + parentCount + " parent \n";
            commitAndParentInfoStr.append(commitInfo);
        }
        else
        {
            commitInfo = commitNO + ". commitId: "+ commit.getId().getName().substring(0, 9) + "; commitTime: "+commit.getCommitTime()+ ";" + parentCount + " parents, which are: \n";
            commitAndParentInfoStr.append(commitInfo);
            int i=0;
            String parentInfo = "";
            for(RevCommit parent: commit.getParents())
            {
                // get parent info, add to the stringBuilder
                parentInfo = "Parent" + i + ": "+ parent.getId().getName().substring(0, 9) + "\n";
                commitAndParentInfoStr.append(parentInfo);
                i++;
            }
            
            commitAndParentInfoStr.append("\n");
            
        }
        return commitAndParentInfoStr; 
    }

    


    
    /***
     * command: 
     * (1) git checkout commit.getParent(0)
     * (2) git commit commit.getParent(1) --no-commit --no-ff
     * @param gitObject
     * @param commit
     * @return
     * @throws GitAPIException 
     * @throws NoMessageException 
     * @throws WrongRepositoryStateException 
     * @throws InvalidMergeHeadsException 
     * @throws CheckoutConflictException 
     * @throws ConcurrentRefUpdateException 
     * @throws NoHeadException 
     */
    public static void replayThreeWayMerge(RevCommit commit) throws NoHeadException, ConcurrentRefUpdateException, CheckoutConflictException, InvalidMergeHeadsException, WrongRepositoryStateException, NoMessageException, GitAPIException{
        
        RevCommit[] parents = commit.getParents();
        
        int parentCount = commit.getParentCount();
        if(parentCount == 2 )
        {
            // git checkout parent0
            System.out.println("--------- git checkout " + parents[0].getId().getName() + "----- \n");
            git.checkout().setName(parents[0].getId().getName()).call();
        
            // git merge parent1 --no-commit --no-ff
            System.out.println("--------- git merge " + parents[1].getId().getName() + "----- \n");
            git.merge()
            .setFastForward(FastForwardMode.NO_FF)
            .setCommit(false)
            .include(parents[1].getId())
            .call();
        }
        
        else {
            System.out.println("Warning! Commit "+ commit.getId().getName().substring(0, 9)+" is not a commit with 2 parents! It has "+ parentCount +" parents \n");
        }
        
    }

   
    
    /***
     * command: git diff oldCommitTreeIterator and oldCommitTreeIterator
     * @param oldCommitTreeIterator
     * @param newCommitTreeIterator
     * @return
     * @throws GitAPIException
     */
    public static List<DiffEntry> gitDiff(AbstractTreeIterator oldCommitTreeIterator,AbstractTreeIterator newCommitTreeIterator ) throws GitAPIException{
        // "git diff"
        return git.diff()
                .setOldTree( oldCommitTreeIterator )
                .setNewTree( newCommitTreeIterator )
                .call();
    }
    
    
    
    /***
     * return true if the "git diff" result is empty 
     * @param diffEntries
     * @return
     */
    public static boolean isGitDiffEmpty(List<DiffEntry> diffEntries)
    {
        // only output info, when there is diffEntry
        if(diffEntries.isEmpty())
        {
            return true;
        }
        return false;
    }
    
    

    
    
    /***
     * build branch-level statistics
     * @param branch
     * @param branchCommitCount
     * @param branchMergeCount
     * @param branch2ParentsMergeCount
     * @param branchTextualConfCount
     * @param branchSemanticConfCount
     */
    public static StringBuilder buildBranchStats(String branch, int branchCommitCount, int branchMergeCount, int branch2ParentsMergeCount, int branchTextualConfCount, int branchSemanticConfCount)
    {
        StringBuilder branchStr = new StringBuilder(branch + " branch statistics: \n");
       
        branchStr.append("    1. branch "+branch+" total number of commits: "+ branchCommitCount + "\n");
        branchStr.append("    2. branch "+branch+" total number of merge commits (parents >= 2): "+ branchMergeCount + "\n");
        branchStr.append("    3. branch "+branch+" total number of merge commits (parents = 2) : "+ branch2ParentsMergeCount + "\n");
        branchStr.append("    4. branch "+branch+" total number of textual conflicts: "+ branchTextualConfCount + "\n");
        branchStr.append("    5. branch "+branch+" total number of potential semantic conflicts: "+ branchSemanticConfCount + "\n");
        
        branchStr.append("branch "+branch+" analysis finish!\n");
        branchStr.append("------------------------------------------------\n");
        branchStr.append("                                                  \n");
        return branchStr;
    }
    
    
    
    
    
    
    
    /***
     * build branch-level statistics
     * @param branch
     * @param branchCommitCount
     * @param branchMergeCount
     * @param branch2ParentsMergeCount
     * @param branchTextualConfCount
     * @param branchSemanticConfCount
     */
    public static StringBuilder buildRepoStats(String repoName, int commitCount, int mergeCount, int twoPrtsMergeCount, int textConflictCount, int potenSemanConfCount)
    {
        StringBuilder repoStr = new StringBuilder("Repository "+ repoName +" statistics: \n");
        
        repoStr.append("    1. repo total number of commits: "+ commitCount + "\n");
        repoStr.append("    2. repo total number of merge commits (parents >= 2): "+ mergeCount + "\n");
        repoStr.append("    3. repo total number of merge commits (parents = 2): "+ twoPrtsMergeCount + "\n");
        repoStr.append("    4. repo total number of textual conflicts: "+ textConflictCount + "\n");
        repoStr.append("    5. repo total number of potential semantic conflicts: "+ potenSemanConfCount + "\n");

        String analysisFinishStr = "Repo "+repoName+" analysis finish!\n"; 
        repoStr.append(analysisFinishStr);
        
        System.out.println(analysisFinishStr);
        
        return repoStr;
        
    }
   
  
    /***
     * print stats of all TAGs inside the repo
     * @param tagCount
     * @param tagNewCommitCount
     * @param tagNewMergeCommitCount
     * @param tagNew2ParentsMergeCommitCount
     * @param tagNewTextualConfCount
     * @param tagNewPotenSemanConfCount
     * @return
     */
    public static StringBuilder buildTagStats(int tagCount, int tagNewCommitCount, int tagNewMergeCommitCount, int tagNew2ParentsMergeCommitCount, int tagNewTextualConfCount, int tagNewPotenSemanConfCount)
    {
       
        StringBuilder tagStr = new StringBuilder("Tags statistics: (only count newly added commits) \n");
        
        tagStr.append("    1. total number of tags: "+ tagCount + "\n");
        tagStr.append("    2. tag total number of new commits: "+ tagNewCommitCount + "\n");
        tagStr.append("    3. tag total number of new merge commits (parents >= 2): "+ tagNewMergeCommitCount + "\n");
        tagStr.append("    4. tag total number of new merge commits (parents = 2): "+ tagNew2ParentsMergeCommitCount + "\n");
        tagStr.append("    5. tag total number of new textual conflicts: "+ tagNewTextualConfCount + "\n");
        tagStr.append("    6. tag total number of new potential semantic conflicts: "+ tagNewPotenSemanConfCount + "\n");

        String tagAnalysisFinishStr = "Tags analysis finish!\n"; 
        tagStr.append(tagAnalysisFinishStr);
        
        System.out.println(tagAnalysisFinishStr);
        
        return tagStr;

        
    }
    
    
    
    /***
     * stringBuilder of all commits of the repo
     * @param repoCommits
     * @return
     * @throws IOException 
     */
    public static void strRepoCommits(Set<RevCommit> repoCommits, int commitCount, File output) throws IOException{
        
        FileOutputStream outputStreamAllCommits = new FileOutputStream(output);
        
        StringBuilder repoCommitsStr = new StringBuilder();
        int count = 0;
        repoCommitsStr.append("                                                  \n");
        repoCommitsStr.append("==================================================\n"); 
        

        for (RevCommit commit:repoCommits){
            count++;
            repoCommitsStr.append(count+". commit "+commit.getId().getName());
            if(commit.getParentCount() < 2) 
            {
                repoCommitsStr.append(" \n");
                } 
            else {
                repoCommitsStr.append(", MERGE with "+ commit.getParentCount()+" parents;\n");
                }
        }
        
        repoCommitsStr.append("==================================================\n");
        repoCommitsStr.append("                                                  \n");
         
        outputStreamAllCommits.write(repoCommitsStr.toString().getBytes());
        outputStreamAllCommits.close();
            
        }

    
    /***
     * if the commit is a merge commit (parentsCount>=2)
     * @param commit
     * @return
     */
    public static boolean isMergeCommit(RevCommit commit)
    {
        if(commit.getParentCount()>=2)
            return true;
        return false;
    }
    
   
    
    /***
     * if the commit is a two-parents merge commit (parentsCount=2)
     * @param commit
     * @return
     */
    public static boolean is2ParentsMergeCommit(RevCommit commit)
    {
        if(commit.getParentCount() == 2)
            return true;
        return false;
    }
    
    
    /***
     * print gid diff results
     * @param diffEntries
     * @param outputStream
     * @throws IOException 
     */
    public static void printDiff(List<DiffEntry> diffEntries) throws IOException
    {

        System.out.println(diffEntries);
        
        for(DiffEntry diffEntry: diffEntries){
            String diffEntryStr = diffEntry.toString() +"\n";
            outputStream.write(diffEntryStr.getBytes());
            }
    }  

    
    
    
    // ===========================================================================================================================
    // ======================== unused functions =================================================================================
    // ===========================================================================================================================
    
    
    /***
     * fileOutputStream commit id of: (1) the merge commit (2) all its parents
     * @param mergeCommit
     * @param mergeCommitCount
     * @throws IOException 
     */
    public static void outputStreamMergeAndParentsInfo(RevCommit mergeCommit, int mergeCommitCount) throws IOException{
        
        int parentCount = mergeCommit.getParentCount();
        
        
        //System.out.println(mergeCommitCount +": Time: "+mergeCommit.getCommitTime() + ". The merge commit "+mergeCommit.getId().getName() +" has "+ parentCount +" parents, which are: ");  
        String mergeCommitInfo = mergeCommitCount +
                ": Time: "+mergeCommit.getCommitTime() +
                ". The merge commit "+mergeCommit.getId().getName().substring(0, 7) +
                " has "+ parentCount +" parents, which are: \n";
        byte[] mergeCommitInfoToBytes = mergeCommitInfo.getBytes();
        outputStream.write(mergeCommitInfoToBytes);
        
        int i=0;
        for(RevCommit parent: mergeCommit.getParents())
        {
            //System.out.println( "Parent" + i + ": "+ parent.getId().getName()); 
            String parentInfo = "Parent" + i + ": "+ parent.getId().getName().substring(0, 7) + "\n";
            byte[] parentInfoToBytes = parentInfo.getBytes();
            outputStream.write(parentInfoToBytes);
           
            i++;
        }

    }

    
    
    /***
     * Given the .git location, set up the connection to the repo
     * But in this way, it cannot make sure the repo exists; prefer to use FileRepositoryBuilder
     * @param gitDir
     * @return
     * @throws IOException
     */
    public static Git openGit(File gitDir) throws IOException{
        return Git.open(gitDir);
    }
   

    
    /***
     * buid a repo with .git location 
     * @param gitDir
     * @return
     * @throws IOException
     */
    public static Repository setUpRepo(File gitDir) throws IOException{
        FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
        //Require the repository to exist before it can be opened.
        //Set the Git directory storing the repository metadata.
        repoBuilder.setMustExist(true).setGitDir(gitDir);
        
        // after setting up everything, use repoBuilder.build() to get the repo
        Repository repository = repoBuilder.build();
        
        return repository;     
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
    public static RevWalk setUpRevWalk(String branch) 
            throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException{

        try (RevWalk revWalk = new RevWalk(git.getRepository())){
            
            ObjectId commitId = git.getRepository().resolve(branch);
            
            revWalk.markStart(revWalk.parseCommit(commitId));
            return revWalk;
        }  
    }
    
    /***
     * return true if it is an annotated tag
     * @param tag
     * @return
     */
    public static boolean isAnnotatedTag(Ref tag){
        // if the tag can return a peeled object, and the object is not empty, then it is an annotated tag
        if ( git.getRepository().peel(tag) != null )
            return true;
        return false;
    }
 
    
    
    /***
     * return true if it is an lightweight tag
     * @param tag
     * @return
     */
    public static boolean isLightWeightTag(Ref tag){
        // if the tag can return a normal object, and the returned object is not empty, then it is a lightweight tag
        if ( git.getRepository().peel(tag) == null
                && tag.isPeeled() == true)
            return true;
        return false;
    }
    
}