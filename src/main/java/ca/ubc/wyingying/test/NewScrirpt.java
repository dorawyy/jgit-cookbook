package ca.ubc.wyingying.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidMergeHeadsException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffEntry.Side;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import ca.ubc.wyingying.parserepo.output;



public class NewScrirpt {


    public static Git git;
    public static String outputRoot;
    public static String outputDir;
    public static ArrayList<String> repoList;
    public static String repoLocalDir;
    
   /***
    * parse repo list from user input args
    * @param args : same as Main()
    * @return
    * @throws IOException
    */
    public static ArrayList<String> parseRepoList(String[] args) throws IOException{
        // parse from the second parameter 
        int i = 1;
        repoList = new ArrayList<>();
        File repoListFile = new File(args[1]);
        
        
        // if file not existing, return directly
        if(!repoListFile.exists())
        {
            System.err.println("the input dir/file: "+ repoListFile.getName() +" , not exist!");
            return null;
        }
        
        
        // if the second param is a local repo dir, then parse all the rest 
        else if(repoListFile.isDirectory()){
            while (i < args.length) {
                repoList.add(args[i]);
                i++;
                }
            }
        
        
        // if the param is a File, then read each line and save to the repo list
        else if (repoListFile.exists() && repoListFile.isFile()) {
            String extension = repoListFile.getName().substring(repoListFile.getName().lastIndexOf("."));
            
            // if .txt file, then parse
            if (extension.equalsIgnoreCase(".txt") ) 
            {
                BufferedReader rdr = new BufferedReader(new FileReader(repoListFile));
                String line = null;
                while ((line = rdr.readLine()) != null)
                    repoList.add(line);
                rdr.close(); 
            }
            
            // else, ignore those files
            else{
                System.err.println("Invalid input file format: " + extension);
                return null;
            }
        }
       
        return repoList;
        
    }
    
    
    
    
    
    
    // the args: (1) output root dir (2) repo location
    public static void main(String[] args) throws IOException, RevisionSyntaxException, GitAPIException {
        // TODO Auto-generated method stub

        // validate if there are enough numbers of arguments (at least 2) 
        if(args.length<2)
        {
            System.out.println("args length <2, exit directly");
            return; 
        }

        
        // parse params, the first param is outputDir, create output root Dir
        outputRoot = args[0];
        output.createPath(outputRoot);
            
        
        // parse the rest params, update repo list
        repoList = parseRepoList(args);
        
        // if the repo list is empty, exit; else, analyze each repo 
        if(repoList.isEmpty()){
            System.out.println("no repo provided. Exit directly");
        }
        else{
            for(String repo: repoList){
                
                // update the output Dir
                repoLocalDir = repo;
                int repoNameIndex = repoLocalDir.lastIndexOf("/");
                String repoName = repoLocalDir.substring(repoNameIndex + 1).replace('/', '_');
                outputDir = Paths.get(outputRoot,File.separator, repoName, File.separator).toString();
                
                // create outputDir
                output.createPath(outputDir);
                
                // then, analyze the repo
                analyseRepo(repoLocalDir, outputDir, repoName);
            }
        }
    }  
    
    
    
    
    /***
     * the whole analysis process of one repository
     * @param localDir
     * @param outputdir
     * @param repoName
     * @throws IOException 
     * @throws GitAPIException 
     * @throws RevisionSyntaxException 
     */
    public static void analyseRepo(String localDir, String outputdir, String repoName) throws IOException, RevisionSyntaxException, GitAPIException {

        try{
        System.out.println("start analysing repo:" + repoName);

        // Initialize the EditedRepo for the repo as preparation
        editedRepo editedrepo = new editedRepo(localDir, outputdir, repoName);
        System.out.println("created the editedRepo ");


        // Set up Git connection
        git = editedrepo.getGit();
        System.out.println("editedrepo.getGit() created");



        // Step1: Scan all commits in the repo
        long phrase1StartTime = System.currentTimeMillis();
        editedrepo = getAllCommitsForRepo(editedrepo);
        long phrase1EndTime = System.currentTimeMillis();
        long phrase1Time = phrase1EndTime- phrase1StartTime;
        System.out.println("Phrase1, getAllCommitsForRepo(editedrepo) done, took " + phrase1Time);
        
        // print all commits out to a file, update total # of commits
        editedrepo.printRepoAllCommits(outputdir);
        editedrepo.printRepoStats(outputdir);
        System.out.println("Phrase1, printed all commits of the repo out to folder " + outputdir);
        
        
        
        
        // step2: get merge commits, replay merge, get diff
        long phase2StartTime = System.currentTimeMillis();
        System.out.println("Phrase2 starts");

        
        /////////////  edited till here  ///////////////////
        editedrepo = getMergeReplayDiff(editedrepo);

        System.out.println("phase2 done\n");

        long phrase2EndTime = System.currentTimeMillis();
        long phrase2Time = phrase2EndTime - phase2StartTime;
        System.out.println("Step2, Replay merging is done, current system time is: " + phrase2Time);

        editedrepo.printRepoStats(outputdir);

        
        
        
        // step3
        long phase3StartTime = System.currentTimeMillis();
        System.out.println("Phrase3 start time is: " + phase3StartTime);

        printStatsByBranches(editedrepo);

        System.out.println("phase3 done\n");

        long phrase3EndTime = System.currentTimeMillis();
        long phrase3Time = phrase3EndTime - phase3StartTime;
        System.out.println("Step3, Reporting stats is done, current system time is: " + phrase3Time);



        // last step: print repo stats
        editedrepo.printRepoStats(outputdir);

        
        System.out.println("Repo " + repoName +" analysis (3 steps) all done.");
        }
        catch(Exception e)
        {
            File exceptionFile = output.createFile(outputdir, "Exception");       
            System.out.println("Expetion when analysing the repo " + repoName+" ; exception file here:" + exceptionFile.getPath().toString() + "\n");
            
            FileOutputStream fos = new FileOutputStream(exceptionFile);
            fos.write(("Excpetion when analysing the repo "+ repoName).getBytes());
            fos.write("Here is the exception info \n\n\\n".getBytes());
            fos.write(e.getMessage().getBytes());
            
            fos.flush();
            fos.close();
        }
    }

    
    
    
    
    
    /***
     * Step1: Given one editedRepo, get all commits of the repo
     * 
     * @param editedrepo
     * @return
     * @throws GitAPIException
     * @throws IOException
     * @throws IncorrectObjectTypeException
     * @throws AmbiguousObjectException
     * @throws RevisionSyntaxException
     */
    public static editedRepo getAllCommitsForRepo(editedRepo editedrepo)
            throws GitAPIException, RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {
        List<Ref> allRemoteBranches = git.branchList().setListMode(ListMode.REMOTE).call();
        for (Ref branch : allRemoteBranches) {
            try (RevWalk revWalk = new RevWalk(git.getRepository())) {
                ObjectId startCommitId = git.getRepository().resolve(branch.getName());
                revWalk.markStart(revWalk.parseCommit(startCommitId));

                // for each revCommit
                for (RevCommit revCommit : revWalk) {
                    if (editedrepo.containRevCommit(revCommit) == true) {
                        // find the editedCommit, update branchInfo
                        editedrepo.findEditedCommitByRevCommit(revCommit).addCommitBranch(branch);
                    } else {
                        // create a editedCommit with branchInfo
                        // add to editedRepo.editedCommitsSet
                        editedCommit editedcommit = new editedCommit(revCommit);
                        editedcommit.addCommitBranch(branch);
                        editedrepo.addEditedCommitToEditedCommitsSet(editedcommit);
                    }

                }
            }
        }
        return editedrepo;
    }



    /***
     * Step2: Replay merge and output textual conflicts/ potential semantic conflicts
     * 
     * @param editedrepo
     * @return
     * @throws CheckoutConflictException
     * @throws IOException
     * @throws GitAPIException
     */
    public static editedRepo getMergeReplayDiff(editedRepo editedrepo)
            throws CheckoutConflictException, IOException, GitAPIException {

        // create output file for textual conflict commit  
        
        String textualOutputFileName =  editedrepo.getRepoName()+ "_TextualConf.txt";
        File textualOutput = Paths.get(editedrepo.getOutputDir() ,File.separator, textualOutputFileName ).toFile();
        @SuppressWarnings("resource")
        FileOutputStream textualFOS = new FileOutputStream(textualOutput);
        textualFOS.write("==========================================================================================\n".getBytes());
        textualFOS.write("Here are all textual conlfict merge:\n".getBytes());
        textualFOS.write("==========================================================================================\n".getBytes());
        textualFOS.write("\n\n".getBytes());
        
        StringBuilder textualConfStr = new StringBuilder();
        
        
        
        // create output file for potential semantic conflict commit
        StringBuilder semanConfStr = new StringBuilder();
        semanConfStr.append("===========================================================================================\n");
        semanConfStr.append("Here are all potential semantic conlfict merge:\n");
        semanConfStr.append("===========================================================================================\n");
        semanConfStr.append("\n\n\n");  

        String semanOutputFileName = editedrepo.getRepoName() + "_PotenSemanConf.txt";
        File semanOutput = Paths.get(editedrepo.getOutputDir(), File.separator, semanOutputFileName).toFile();
        @SuppressWarnings("resource")
        FileOutputStream semanticFOS = new FileOutputStream(semanOutput);



        // traverse all commits of the repo
        for (editedCommit editedcommit : editedrepo.getEditedCommitsSet()) {
            // get the real merge commit
            RevCommit realMergeCommit = editedcommit.getRevCommit();

            // if editedcommit.is2parentsMerge, replay merge
            if (editedcommit.is2ParentsMergeCommit() == true) {

                // get the revCommit and its parents
                // replay merge
                RevCommit parent0 = editedcommit.getParents().get(0);
                RevCommit parent1 = editedcommit.getParents().get(1);

                // replay merge
                AbortMerge(realMergeCommit);
                CheckoutCommit(parent0);
                replayThreeWayMerge(parent1);

                
                
                // if textual conflict, print out info
                if (meetTextualConflict() == true) {
                    // update status of the real editedCommit , repo textual conflict ++
                    editedrepo.findEditedCommitByEditedCommit(editedcommit).setHasTextualConflict(meetTextualConflict());
                    editedrepo.increTextConflictCount();
                    System.out.println("Detected "+ editedrepo.getTextConflictCount() +" textual conflicts in the repo by now");
                    
                    
                    // print merge commit info
                    textualConfStr.append("Merge Commit:").append(editedcommit.printEditedCommit());
                    textualConfStr.append("Parent 0:").append(editedrepo.findEditedCommitByRevCommit(parent0).printEditedCommit());
                    textualConfStr.append("Parent 1:").append(editedrepo.findEditedCommitByRevCommit(parent1).printEditedCommit());
                    textualConfStr.append("\n").append("===============================================================================\n").append("\n");
                    
                    textualFOS.write(textualConfStr.toString().getBytes());
                    textualConfStr.setLength(0);
                }

                
                // if no texutal conflict, further compare git diff to see if semantic conf
                else {
                    
                    
                    // execute git diff <index> <realMerge>
                    AbstractTreeIterator indexTreeIterator = IndexTreeIterator();
                    AbstractTreeIterator realMergeCommitTreeIterator = SpecificCommitTreeIterator(realMergeCommit);
                    // git diff <index> <realMerge>
                    List<DiffEntry> diffEntries = gitDiffNoStream(indexTreeIterator, realMergeCommitTreeIterator);
                   
                    
                    
                    // if GIT DIFF is empty, then rm created folder and files
                    if(diffEntries.isEmpty()){
                        System.out.println("GIT DIFF result is empty, no potential semantic conflict, deleted the diff output folder");
                    }
                    
                    
                    
                    // if GIT DIFF is not empty, mark it, output the results
                    else {
                        
                        // update the realMerge info, as well as #semanticConfCount of the repo
                        editedrepo.findEditedCommitByEditedCommit(editedcommit).setHasPotentialSemanticConf(!diffEntries.isEmpty());
                        editedrepo.increPotenSemanConfCount();

                        

                        
                        
                        // create diff output folder for the commit and created a diff summary file
                        String diffFolderPath = new StringBuilder(outputDir)
                                .append(File.separator)
                                .append(editedrepo.potenSemanConfCount)
                                .append("_")
                                .append(editedcommit.commitId)
                                .toString();
                        output.createPath(diffFolderPath);
                        
                        // only valid for this commit 
                        File diffOutputFile = output.createFile(diffFolderPath, "DiffSummary.txt");
                        FileOutputStream diffFOS = new FileOutputStream(diffOutputFile);
                        diffFOS.write("----------------------------------------------------------------------\n".getBytes());
                        diffFOS.write("------------ GIT  DIFF REPLAY_MERGE REAL_MERGE------------------------\n".getBytes());
                        diffFOS.write("----------------------------------------------------------------------\n".getBytes());
                        diffFOS.write(printDiff(diffEntries).toString().getBytes());
                        
                        

                        
                        // print diff summary to file REPO_PotenSemanConf.txt
                        semanConfStr.append("Merge Commit:").append(editedcommit.getCommitId()).append("\n");
                        semanConfStr.append("Parent 0:").append(editedrepo.findEditedCommitByRevCommit(parent0).getCommitId()).append("\n");
                        semanConfStr.append("Parent 1:").append(editedrepo.findEditedCommitByRevCommit(parent1).getCommitId()).append("\n");
                        semanConfStr.append("There are totally ").append(diffEntries.size()).append(" diff entries\n").append("\n");
                        semanConfStr.append(printDiffSummary(diffEntries));
                        semanConfStr.append("\n").append("===============================================================================\n").append("\n");
                        // write to file REPO_PotenSemanConf.txt
                        semanticFOS.write(semanConfStr.toString().getBytes());
                        semanConfStr.setLength(0);
                        
                        
                        
                        
                        // Print details of the semantic conflict (into its own folder diffFolder, with all needed info)
                        
                        
                        // copy version of the ReplayMerge
                        String replayMergeDiffDir =  new StringBuilder(diffFolderPath)
                                .append(File.separator)
                                .append("ReplayMerge_")
                                .append(editedcommit.getCommitId())
                                .toString();
                        output.createPath(replayMergeDiffDir);
                        // print ReplayMerge(index) version
                        ArrayList<String> replayFileList = extractDiffFiles(diffEntries, Side.OLD);
                        for (String sourceFile : replayFileList) 
                        {
                            String sourceFileAbsoluteDir = Paths.get(repoLocalDir, File.separator, sourceFile).toString(); 
                            copyFile(sourceFileAbsoluteDir, replayMergeDiffDir);
                        }
                        System.out.println("ReplayMerge version diff files all copied");
                        
                        
                        
                        
                        // copy version of the RealMerge
                        String realMergeDiffDir =  new StringBuilder(diffFolderPath)
                                .append(File.separator)
                                .append("RealMerge_")
                                .append(editedcommit.getCommitId())
                                .toString();
                        output.createPath(realMergeDiffDir);
                        // print ReplayMerge(index) version
                        ArrayList<String> realMergeFileList = extractDiffFiles(diffEntries, Side.NEW);
                        
                        // Abort merge, checkout to realMergeCommit, then output the results
                        AbortMerge(realMergeCommit);
                        git.checkout().setName(editedcommit.getCommitId()).call();
                        
                        for (String sourceFile : realMergeFileList) 
                        {
                            String sourceFileAbsoluteDir = Paths.get(repoLocalDir, File.separator, sourceFile).toString();
                            copyFile(sourceFileAbsoluteDir, realMergeDiffDir);
                        }
                        System.out.println("RealMerge_" + editedcommit.getCommitId() + " version diff files all copied");
                        
                        
                        
                        
                        // git diff index and parent0
                        // then, execute git diff <parent0> <parent1>, save files different in parent0 and parent1 
                        
                        AbstractTreeIterator parent0CommitTreeIterator = SpecificCommitTreeIterator(parent0);
                        AbstractTreeIterator parent1CommitTreeIterator = SpecificCommitTreeIterator(parent1);
                        List<DiffEntry> P0P1DiffEntries = gitDiffNoStream(parent0CommitTreeIterator,parent1CommitTreeIterator);
                       
                        
                        
                        diffFOS.write("\n\n\n\n".getBytes());
                        diffFOS.write("-----------------------------------------------------------------\n".getBytes());
                        diffFOS.write("------------  GIT DIFF PARENT0 PARENT1---------------------------\n".getBytes());
                        diffFOS.write("-----------------------------------------------------------------\n".getBytes());
                        diffFOS.write(printDiff(P0P1DiffEntries).toString().getBytes());
                        
                        // based on P0P1DiffEntries, save version for P0(OLD) 
                        String parent0DiffDir =  new StringBuilder(diffFolderPath)
                                .append(File.separator)
                                .append("Parent0_")
                                .append(parent0.getId().getName().toString())
                                .toString();
                        output.createPath(parent0DiffDir);
                        // print ReplayMerge(index) version
                        ArrayList<String> P0DiffFileList = extractDiffFiles(P0P1DiffEntries, Side.OLD);
                        git.checkout().setName(parent0.getId().getName().toString()).call();
                        
                        for (String sourceFile : P0DiffFileList) 
                        {
                            String sourceFileAbsoluteDir = Paths.get(repoLocalDir, File.separator, sourceFile).toString();
                            copyFile(sourceFileAbsoluteDir, parent0DiffDir);
                        }
                        System.out.println("Parent0_" + parent0.getId().getName().toString() + " version diff files all copied");
                        
                        
                        
                        
                        // based on P0P1DiffEntries, save version for P1(NEW)
                        String parent1DiffDir =  new StringBuilder(diffFolderPath)
                                .append(File.separator)
                                .append("Parent1_")
                                .append(parent1.getId().getName().toString())
                                .toString();
                        output.createPath(parent1DiffDir);
                        // print ReplayMerge(index) version
                        ArrayList<String> P1DiffFileList = extractDiffFiles(P0P1DiffEntries, Side.NEW);
                        
                        git.checkout().setName(parent1.getId().getName().toString()).call();
                        for (String sourceFile : P1DiffFileList) 
                        {
                            String sourceFileAbsoluteDir = Paths.get(repoLocalDir, File.separator, sourceFile).toString();
                            copyFile(sourceFileAbsoluteDir, parent1DiffDir);
                        }
                        System.out.println("Parent1_" + parent1.getId().getName().toString()  + " version diff files all copied");
                        
                        
                        // release memory
                        diffFOS.flush();
                        diffFOS.close();
                        
                        
                        
                        
                        // print out commit info of 4 commit (1) common ancestor (2) parent0 (3) parent1 (4) real Merge
                        
                        
                        File commitInfoFile = Paths.get(diffFolderPath, File.separator, "RelatedCommitOverview.txt").toFile();
                        FileOutputStream commitFOS = new FileOutputStream(commitInfoFile);
                        
                        // find common ancestor
                        RevCommit ancestor = getMergeBase( git, parent0,  parent1) ;
                        System.out.println("ancestorId is:" + ancestor.getId().getName().toString() +"\n");
                        

                        
                        
                        commitFOS.write("=================================================================\n".getBytes());
                        commitFOS.write("------------  Common Ancestor Commit ----------------------------\n".getBytes());
                        commitFOS.write("=================================================================\n".getBytes());
                        String strAncestor = editedrepo.findEditedCommitByRevCommit(ancestor).printEditedCommit().toString();
                        commitFOS.write(strAncestor.getBytes());
                        
                        
                        
                        
                        commitFOS.write("\n\n\n=================================================================\n".getBytes());
                        commitFOS.write("------------  Parent0 Commit-------------------------------------\n".getBytes());
                        commitFOS.write("=================================================================\n".getBytes());
                        
                        String strParent0 = editedrepo.findEditedCommitByRevCommit(parent0).printEditedCommit().toString();
                        commitFOS.write(strParent0.getBytes());
                        
                        commitFOS.write("\n\n\n=================================================================\n".getBytes());
                        commitFOS.write("------------  Parent1 Commit ------------------------------------\n".getBytes());
                        commitFOS.write("=================================================================\n".getBytes());
                        
                        String strParent1 = editedrepo.findEditedCommitByRevCommit(parent1).printEditedCommit().toString();
                        commitFOS.write(strParent1.getBytes());
                        
                        commitFOS.write("\n\n\n=================================================================\n".getBytes());
                        commitFOS.write("------------  Real Merge Commit ---------------------------------\n".getBytes());
                        commitFOS.write("=================================================================\n".getBytes());
                        
                        String strRealMerge = editedcommit.printEditedCommit().toString();
                        commitFOS.write(strRealMerge.getBytes());
                        
                        commitFOS.flush();
                        commitFOS.close();
                        
                    }
                }
   
                // remember to abort the merge in the end
                AbortMerge(realMergeCommit);
            }
        }

        textualFOS.flush();
        textualFOS.close();
        
        semanticFOS.flush();
        semanticFOS.close();

        // garbage collection
        System.gc();

        return editedrepo;
    }



    /***
     * Step3: Print stats of each branch
     * 
     * @param editedrepo
     * @throws GitAPIException
     * @throws IOException
     */
    public static void printStatsByBranches(editedRepo editedrepo) throws GitAPIException, IOException {
        String branchOutputDir = Paths.get(editedrepo.getOutputDir(),File.separator,"Branches").toString();
        output.createPath(branchOutputDir);
        List<Ref> allRemoteBranches = git.branchList().setListMode(ListMode.REMOTE).call();
        for (Ref branch : allRemoteBranches) {
            printDetailsPerBranch(branchOutputDir,editedrepo, branch);
        }
    }



    /***
     * Print details about (1) all commits on this branch, (2) branch stats
     * 
     * @param editedrepo
     * @param branch
     * @throws IOException
     */
    public static void printDetailsPerBranch(String branchOutputDir, editedRepo editedrepo, Ref branch) throws IOException {

        editedBranch editedbranch = new editedBranch(branch);

        for (editedCommit editedcommit : editedrepo.getEditedCommitsSet()) {
            // if the editedCommit is supposed to be on the branch, but not added yet, then add it
            if (editedcommit.getContainedBranches().contains(branch) == true &&
                    editedbranch.containEditedCommit(editedcommit) == false)
                editedbranch.addCommitByEditedCommit(editedcommit);

        }

        StringBuilder branchStr = new StringBuilder();
        branchStr.append(editedbranch.printEditedBranchInfo());

        branchStr.append(editedbranch.printBranchStats());


        int branchNameIndex = branch.getName().lastIndexOf("/");
        String branchName = branch.getName().substring(branchNameIndex + 1).replace('/', '_');

        String fileName = branchName + "_BranchOutput";
        File outputBranch = Paths.get(branchOutputDir+File.separator+fileName).toFile();
        @SuppressWarnings("resource")
        FileOutputStream branchFOS = new FileOutputStream(outputBranch);

        branchFOS.write(branchStr.toString().getBytes());

        branchFOS.flush();
        branchFOS.close();


    }


    
    

    /***
     * Print git diff summary results into the overview file
     * 
     * @param diffEntries
     * @param outputStream
     * @throws IOException
     */
    public static StringBuilder printDiffSummary(List<DiffEntry> diffEntries) throws IOException {
        System.out.println(diffEntries);

        StringBuilder diffStr = new StringBuilder();
        int index = 1;
        diffStr.append("------- There are" + diffEntries.size() + " Diff Entries -----------\n");
        for (DiffEntry diffEntry : diffEntries) {
            diffStr.append("[DiffEntry" + index + "]:\n");
            diffStr.append("    "+ diffEntry.toString() + "\n");
            index++;
        }
        return diffStr;
    }

    
    
  
    
    
    

    /***
     * get the list of files listed in the diff entry
     * 
     * @param diffEntries
     * @param side : OLD or NEW
     * @param outputStream
     * @throws IOException
     */
    public static ArrayList<String> extractDiffFiles(List<DiffEntry> diffEntries, Side side) throws IOException {
        ArrayList<String> fileList = new ArrayList<>();
        
        if(side != Side.NEW && side != Side.OLD){
            System.out.println("invalide 'Side' value; only 'NEW' or 'OLD' is supported!");
            return null;
        }
        
        
        // add all files into the fileList
        for (DiffEntry diffEntry : diffEntries) {

            // if NEW, then check newObjectId, for files got deleted, no entry can be found, skip
            if(side == Side.NEW){
                if(diffEntry.getChangeType() == ChangeType.DELETE)
                    continue;
                // otherwise, find the path of the file
                String fileName = diffEntry.getPath(side);
                fileList.add(fileName);
            }
             
            // on OLD side, exclude those that files with modify type "ADD"
            else if(side == Side.OLD){
                if(diffEntry.getChangeType() == ChangeType.ADD)
                    continue;
                // otherwise, find the path of the file
                String fileName = diffEntry.getPath(side);
                fileList.add(fileName);
            }
            
        }

        return fileList;
    }
    
    
    
    

    /***
     * Print git diff results
     * 
     * @param diffEntries
     * @param outputStream
     * @throws IOException
     */
    public static StringBuilder printDiff(List<DiffEntry> diffEntries) throws IOException {
        System.out.println(diffEntries);

        StringBuilder diffStr = new StringBuilder();
        int index = 0;
        for (DiffEntry diffEntry : diffEntries) {
            diffStr.append("Diff " + index + ":\n");
            diffStr.append("      " + diffEntry.toString() + "\n");
            diffStr.append("    (1) ChangeType: " + diffEntry.getChangeType().toString() + "\n");
            
            // if add, no need to print out old info
            if(diffEntry.getChangeType() == ChangeType.ADD){
                diffStr.append("    (2) New ID: " + diffEntry.getNewId().toString() + "\n");
                diffStr.append("    (3) New path: " + diffEntry.getNewPath().toString() + "\n");
            }
            // if delete, no need to print out new info
            else if(diffEntry.getChangeType() == ChangeType.DELETE){
                diffStr.append("    (2) Old ID: " + diffEntry.getOldId().toString() + "\n");
                diffStr.append("    (3) Old path: " + diffEntry.getOldPath().toString() + "\n");
            }
            
            // if path changed, print out both path
            else if(diffEntry.getNewPath().toString().equals(diffEntry.getOldPath().toString()) == false){
                diffStr.append("    (2) Old ID: " + diffEntry.getOldId().toString() + "\n");
                diffStr.append("        New ID: " + diffEntry.getNewId().toString() + "\n");
                diffStr.append("    (3) New path: " + diffEntry.getNewPath().toString() + "\n");
                diffStr.append("        Old path: " + diffEntry.getOldPath().toString() + "\n");
            }
            
            // otherwise, path no change, only Id changed
            else{
                diffStr.append("    (2) Old ID: " + diffEntry.getOldId().toString() + "\n");
                diffStr.append("        New ID: " + diffEntry.getNewId().toString() + "\n");
                diffStr.append("    (3) Old/New Path: " + diffEntry.getNewPath().toString() + "\n");
            }
            index++;
        }

        return diffStr;
    }



    /***
     * Command: git reset --hard commit
     * There is no direct git merge --abort, thus using git reset --hard
     * abort merge
     * 
     * @param gitObejct
     * @param destCommit
     * @return
     * @throws IOException
     * @throws CheckoutConflictException
     * @throws GitAPIException
     */
    public static void AbortMerge(RevCommit commit) throws IOException, CheckoutConflictException, GitAPIException {
        git.getRepository().writeMergeCommitMsg(null);
        git.getRepository().writeMergeHeads(null);
        git.reset().setMode(ResetType.HARD).setRef(commit.getId().getName()).call();

        
    }


    /***
     * command:
     * git checkout commit
     * 
     * @param commit
     * @throws RefAlreadyExistsException
     * @throws RefNotFoundException
     * @throws InvalidRefNameException
     * @throws CheckoutConflictException
     * @throws GitAPIException
     */
    public static void CheckoutCommit(RevCommit commit) throws RefAlreadyExistsException, RefNotFoundException,
            InvalidRefNameException, CheckoutConflictException, GitAPIException {
        System.out.println("--------- git checkout " + commit.getId().getName() + "----- \n");
        git.checkout().setName(commit.getId().getName()).call();
    }


    /***
     * command:
     * git commit commit --no-commit --no-ff
     * 
     * @param gitObject
     * @param commit
     * @return
     * @throws NoHeadException
     */
    public static void replayThreeWayMerge(RevCommit commit)
            throws NoHeadException, ConcurrentRefUpdateException, CheckoutConflictException, InvalidMergeHeadsException,
            WrongRepositoryStateException, NoMessageException, GitAPIException {

        System.out.println("--------- git merge " + commit.getId().getName() + "----- \n");
        git.merge().setFastForward(FastForwardMode.NO_FF).setCommit(false).include(commit.getId()).call();

    }



    /***
     * run command "git status", to check if there is textual conflict or not
     * 
     * @param gitObject
     * @return
     * @throws NoWorkTreeException
     * @throws GitAPIException
     */
    public static boolean meetTextualConflict() throws NoWorkTreeException, GitAPIException {
        if (git.status().call().getConflicting().isEmpty() == true)
            return false; // if no conflict files, then no textual conflict
        return true; // otherwise, meet textual conflict
    }



    
    
    /***
     * command: git diff oldCommitTreeIterator and oldCommitTreeIterator
     * 
     * @param oldCommitTreeIterator
     * @param newCommitTreeIterator
     * @return
     * @throws IOException 
     * @throws GitAPIException
     */
    public static File setUpDiffOutput(String outputDir, String diffFileName) throws IOException{
        // set up output path -- outputDir + folderName
        // set up output file -- diffFileName
        File diffFile = new File(outputDir + File.separatorChar + diffFileName) ;
        
        // set up output Stream with the output File 
        boolean folderCreated = output.createPath(outputDir);
        if(folderCreated == true){
            diffFile = output.createFile(outputDir, diffFileName);
        }
        else{
            System.out.println("Diff file output path: does not exist, and created failed.");
        }
        return diffFile;      
    }

    
    
    /***
     * command: git diff oldCommitTreeIterator and oldCommitTreeIterator
     * 
     * @param oldCommitTreeIterator
     * @param newCommitTreeIterator
     * @return
     * @throws GitAPIException
     */
    public static List<DiffEntry> gitDiff(FileOutputStream diffFOS , AbstractTreeIterator oldCommitTreeIterator, AbstractTreeIterator newCommitTreeIterator)
            throws GitAPIException {
        return git.diff().setOutputStream(diffFOS).setOldTree(oldCommitTreeIterator).setNewTree(newCommitTreeIterator).call();
        
    }
    
    
    
    /***
     * command: git diff oldCommitTreeIterator and oldCommitTreeIterator ( no output stream)
     * 
     * @param oldCommitTreeIterator
     * @param newCommitTreeIterator
     * @return
     * @throws GitAPIException
     */
    public static List<DiffEntry> gitDiffNoStream( AbstractTreeIterator oldCommitTreeIterator, AbstractTreeIterator newCommitTreeIterator)
            throws GitAPIException {
        return git.diff().setOldTree(oldCommitTreeIterator).setNewTree(newCommitTreeIterator).call();
        
    }
    



    /***
     * get the revision of Working Tree
     * 
     * @param repository
     * @return
     */
    public static AbstractTreeIterator WorkingTreeIterator() {
        return new FileTreeIterator(git.getRepository());
    }


    /***
     * get the Index revision
     * 
     * @param repository
     * @return
     * @throws NoWorkTreeException
     * @throws CorruptObjectException
     * @throws IOException
     */
    public static AbstractTreeIterator IndexTreeIterator() throws NoWorkTreeException, CorruptObjectException, IOException {
        return new DirCacheIterator(git.getRepository().readDirCache());
    }


    /***
     * get the TreeIterator of a specific commit
     * 
     * @param repository
     * @param commit
     * @return
     * @throws IncorrectObjectTypeException
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public static AbstractTreeIterator SpecificCommitTreeIterator(RevCommit commit)
            throws IncorrectObjectTypeException, IOException {
        ObjectId treeId = commit.getTree().getId();
        ObjectReader reader = git.getRepository().newObjectReader();
        return new CanonicalTreeParser(null, reader, treeId);
    }
    
    
    /***
     * copy the file "source" to destPath
     * @param source : source file path+filename
     * @param destPath : destfile dir
     * @return
     * @throws IOException
     */
    public static boolean copyFile(String source, String destPath) throws IOException
    {
        File sourceFile = new File(source);
        // get fileName
        String sourceFileName = source.substring(source.lastIndexOf(File.separator));
        File destFile = Paths.get(destPath, File.separator , sourceFileName).toFile();
        
        if(destFile.exists() && destFile.isFile()){
            destFile.delete();
        }
        
        if(sourceFile.exists() && !destFile.exists()){
            Files.copy(sourceFile.toPath(), destFile.toPath());
            return true;
        }
        
        System.out.println("failed to copy the file " +source + " due to: (1) sourceFile.exists() = " + sourceFile.exists() + "; (2) destFile.exists(): "+ destFile.exists());
        return false;
    }
    
    
    
    /***
     * command: git-merge-base commit1 commit2
     * find the best common ancestor of commit1 and commit2
     * @param commit1
     * @param commit2
     * @return
     * @throws IOException 
     * @throws IncorrectObjectTypeException 
     * @throws MissingObjectException 
     */
    public static RevCommit getMergeBase(Git gitObject, RevCommit commit1, RevCommit commit2) throws MissingObjectException, IncorrectObjectTypeException, IOException {
        @SuppressWarnings("resource")
        RevWalk walk = new RevWalk(gitObject.getRepository());


        walk.setRevFilter(RevFilter.MERGE_BASE);

        walk.markStart(walk.parseCommit(commit1));
        walk.markStart(walk.parseCommit(commit2));

        RevCommit mergeBase = walk.next();
        System.out.println("mergeBase commit is: " + mergeBase.toString());

        String mergeBaseId = mergeBase.getId().getName().toString();
        if (mergeBaseId.isEmpty()) {
            System.out.println("commit " + commit1.getId().getName().toString() + " and commit " +
                    commit2.getId().getName().toString() + " have no common ancestor!\n");
            return null;
        }

        System.out.println("commit" + commit1.getId().getName().toString() + " and commit " +
                commit2.getId().getName().toString() + " have common ancestor " + mergeBase.getName() + "\n");

        return mergeBase;

    }
    
    


}


