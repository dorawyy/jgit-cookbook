package ca.ubc.wyingying.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;


public class NewScrirpt {


    public static Git git;


    public static void main(String[] args) throws IOException, RevisionSyntaxException, GitAPIException {
        // TODO Auto-generated method stub


        for (String arg : args) {
            String localDir = arg;
            long startTime = System.currentTimeMillis();
            System.out.println("start time is: " + startTime);



            int repoNameIndex = localDir.lastIndexOf("/");
            String repoName = localDir.substring(repoNameIndex + 1).replace('/', '_');

            String outputDir = "/Users/dora/SamSungRepos/Output/" + repoName + "/";

            System.out.println("repoName is: " + repoName);

            editedRepo editedrepo = new editedRepo(localDir, outputDir, repoName);

            System.out.println("created the editedRepo ");

            git = editedrepo.getGit();


            System.out.println("editedrepo.getGit()");


            // step1: get all commits in the repo
            editedrepo = getAllCommitsForRepo(editedrepo);
            System.out.println("getAllCommitsForRepo(editedrepo) done");

            long phrase1EndTime = System.currentTimeMillis();
            System.out.println("Step1, Scanning all commits is done, current system time is: " + phrase1EndTime);


            editedrepo.printRepoStats(outputDir);
            
            // step2: get merge commits, replay merge, get diff
            long phase2StartTime = System.currentTimeMillis();
            System.out.println("Phrase2 start time is: " + phase2StartTime);


            editedrepo = getMergeReplayDiff(editedrepo);

            System.out.println("phase2 done\n");

            long phrase2EndTime = System.currentTimeMillis();
            System.out.println("Step2, Replay merging is done, current system time is: " + phrase2EndTime);

            editedrepo.printRepoStats(outputDir);

            // step3
            long phase3StartTime = System.currentTimeMillis();
            System.out.println("Phrase3 start time is: " + phase3StartTime);

            printStatsByBranches(editedrepo);

            System.out.println("phase3 done\n");

            long phrase3EndTime = System.currentTimeMillis();
            System.out.println("Step3, Reporting stats is done, current system time is: " + phrase3EndTime);



            // last step: print repo stats
            editedrepo.printRepoStats(outputDir);

            editedrepo.printRepoAllCommits(outputDir);
            System.out.println("Whole repo analysis (3 steps) all done.");

        }
    }


    /***
     * Step1: Given one editedRepo, get all commits on that repo
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
                        // find the editedCommit
                        // update branchInfo
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
     * Step2: Replay merge and output textual confs/ potential semantic confs
     * 
     * @param editedrepo
     * @return
     * @throws CheckoutConflictException
     * @throws IOException
     * @throws GitAPIException
     */
    public static editedRepo getMergeReplayDiff(editedRepo editedrepo)
            throws CheckoutConflictException, IOException, GitAPIException {

        StringBuilder textualConfStr = new StringBuilder();
        textualConfStr.append(
                "======================================================================================================================================================\n");
        textualConfStr.append("Here are all textual conlfict merge:\n");
        textualConfStr.append(
                "======================================================================================================================================================\n");
        textualConfStr.append("\n\n");


        StringBuilder semanConfStr = new StringBuilder();
        semanConfStr.append(
                "======================================================================================================================================================\n");
        semanConfStr.append("Here are all potential semantic conlfict merge:\n");
        semanConfStr.append(
                "======================================================================================================================================================\n");
        semanConfStr.append("\n\n\n");


        File textualOutput = new File(editedrepo.getOutputDir() + editedrepo.getRepoName() + "_TextualConf.txt");
        @SuppressWarnings("resource")
        FileOutputStream textualFOS = new FileOutputStream(textualOutput);

        File semanOutput = new File(editedrepo.getOutputDir() + editedrepo.getRepoName() + "_PotenSemanConf.txt");
        @SuppressWarnings("resource")
        FileOutputStream semanticFOS = new FileOutputStream(semanOutput);



        // traverse editedCommit
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

                // if textual conflict
                if (meetTextualConflict() == true) {
                    // update status of the real editedCommit , repo textual conflict ++
                    editedrepo.findEditedCommitByEditedCommit(editedcommit).setHasTextualConflict(meetTextualConflict());
                    editedrepo.increTextConflictCount();

                    // print merge commit info
                    textualConfStr.append("Merge Commit:");
                    textualConfStr.append(editedcommit.printEditedCommit());
                    textualConfStr.append("Parent 0:");
                    textualConfStr.append(editedrepo.findEditedCommitByRevCommit(parent0).printEditedCommit());
                    textualConfStr.append("Parent 1:");
                    textualConfStr.append(editedrepo.findEditedCommitByRevCommit(parent1).printEditedCommit());
                    textualConfStr.append("\n");
                    textualConfStr.append(
                            "======================================================================================================================================================\n");
                    textualConfStr.append("\n");


                }

                // if no texutal conflict, compare git diff
                else {
                    AbstractTreeIterator indexTreeIterator = IndexTreeIterator();
                    AbstractTreeIterator mergeCommitTreeIterator = SpecificCommitTreeIterator(realMergeCommit);
                    List<DiffEntry> diffEntries = gitDiff(mergeCommitTreeIterator, indexTreeIterator);

                    if (diffEntries.isEmpty() == false) {
                        editedrepo.findEditedCommitByEditedCommit(editedcommit).setHasPotentialSemanticConf(
                                !diffEntries.isEmpty());
                        editedrepo.increPotenSemanConfCount();

                        // print diffEntries, commit info


                        // print merge commit info
                        semanConfStr.append("Merge Commit:");
                        semanConfStr.append(editedcommit.printEditedCommit());
                        semanConfStr.append("Parent 0:");
                        semanConfStr.append(editedrepo.findEditedCommitByRevCommit(parent0).printEditedCommit());
                        semanConfStr.append("Parent 1:");
                        semanConfStr.append(editedrepo.findEditedCommitByRevCommit(parent1).printEditedCommit());
                        // find the editedCommit of its parents
                        semanConfStr.append(printDiff(diffEntries));
                        semanConfStr.append("\n");
                        semanConfStr.append(
                                "======================================================================================================================================================\n");
                        semanConfStr.append("\n");


                    }
                }

                // remember to abort the merge in the end
                AbortMerge(realMergeCommit);
            }


        }



        textualFOS.write(textualConfStr.toString().getBytes());
        semanticFOS.write(semanConfStr.toString().getBytes());

        textualFOS.close();
        semanticFOS.close();


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
        List<Ref> allRemoteBranches = git.branchList().setListMode(ListMode.REMOTE).call();
        for (Ref branch : allRemoteBranches) {
            printDetailsPerBranch(editedrepo, branch);
        }
    }



    /***
     * Print details about (1) all commits on this branch, (2) branch stats
     * 
     * @param editedrepo
     * @param branch
     * @throws IOException
     */
    public static void printDetailsPerBranch(editedRepo editedrepo, Ref branch) throws IOException {

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

        File outputBranch = new File(editedrepo.getOutputDir() + branchName + "_BranchOutput");
        FileOutputStream branchFOS = new FileOutputStream(outputBranch);

        branchFOS.write(branchStr.toString().getBytes());

        branchFOS.close();


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
            diffStr.append("    (0) Diff entry  : " + diffEntry.toString() + "\n");
            diffStr.append("    (1) Change type: " + diffEntry.getChangeType().toString() + "\n");
            diffStr.append("    (2) New path: " + diffEntry.getNewPath().toString() + "\n");
            diffStr.append("    (3) Old path: " + diffEntry.getOldPath().toString() + "\n");
            diffStr.append("    (4) New type: " + diffEntry.getNewMode().toString() + "\n");
            diffStr.append("    (5) Old type: " + diffEntry.getOldMode().toString() + "\n");
            diffStr.append("    (6) New ID: " + diffEntry.getNewId().toString() + "\n");
            diffStr.append("    (7) Old ID: " + diffEntry.getOldId().toString() + "\n");
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
            return false;
        return true;
    }



    /***
     * command: git diff oldCommitTreeIterator and oldCommitTreeIterator
     * 
     * @param oldCommitTreeIterator
     * @param newCommitTreeIterator
     * @return
     * @throws GitAPIException
     */
    public static List<DiffEntry> gitDiff(AbstractTreeIterator oldCommitTreeIterator, AbstractTreeIterator newCommitTreeIterator)
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
     * Print commit information
     * 
     * @param diffEntries
     * @param outputStream
     * @throws IOException
     */
//    public static StringBuilder printCommit(RevCommit commit, RevCommit parent0, RevCommit parent1) throws IOException {
//        
//        StringBuilder commitInfoStr = new StringBuilder();
//        commitInfoStr.append("Parent 0 ");
//        commitInfoStr.append("Commit id : " + commit.getId().name().toString()+ "\n");
//        commitInfoStr.append("    (1) Committer: " + commit.getCommitterIdent().toString()+ "\n");
//        commitInfoStr.append("    (2) Commit Time: " + commit.getCommitTime()+ "\n");
//        commitInfoStr.append("    (3) Commit Message: " + commit.getFullMessage().toString());
//        commitInfoStr.append("    (4) Commit has " + commit.getParentCount()+ " parents, which are: \n");
//        int parentNO=0;
//        for(RevCommit parent:commit.getParents())
//        {
//            commitInfoStr.append("          "+ parentNO + parent.getId().getName().toString() +"; ");
//            parentNO ++;
//        }
//        
//        commitInfoStr.append("\n \n");
//        
//        
//        // parent0 
//        commitInfoStr.append("Parent 1 ");
//        commitInfoStr.append("Commit id : " + parent0.getId().name().toString()+ "\n");
//        commitInfoStr.append("    (1) Committer: " + parent0.getCommitterIdent().toString()+ "\n");
//        commitInfoStr.append("    (2) Commit Time: " + parent0.getCommitTime()+ "\n");
//        commitInfoStr.append("    (3) Commit Message: " + parent0.getFullMessage().toString());
//        commitInfoStr.append("    (4) Commit has " + parent0.getParentCount()+ " parents, which are: \n");
//        parentNO=0;
//        for(RevCommit parent:commit.getParents())
//        {
//            commitInfoStr.append("          "+ parentNO + parent.getId().getName().toString() +"; ");
//            parentNO ++;
//        }
//        
//        commitInfoStr.append("\n \n");
//        
//        
//        // parent1 
//        commitInfoStr.append("Real Merge Commit ");
//        commitInfoStr.append("Commit id : " + parent1.getId().name().toString()+ "\n");
//        commitInfoStr.append("    (1) Committer: " + parent1.getCommitterIdent().toString()+ "\n");
//        commitInfoStr.append("    (2) Commit Time: " + parent1.getCommitTime()+ "\n");
//        commitInfoStr.append("    (3) Commit Message: " + parent1.getFullMessage().toString());
//        commitInfoStr.append("    (4) Commit has " + parent1.getParentCount()+ " parents, which are: \n");
//        parentNO=0;
//        for(RevCommit parent:commit.getParents())
//        {
//            commitInfoStr.append("          "+ parentNO + parent.getId().getName().toString() +"; ");
//            parentNO ++;
//        }
//        
//        commitInfoStr.append("\n \n");
//        
//        
//        return commitInfoStr;
//    }
//



}


