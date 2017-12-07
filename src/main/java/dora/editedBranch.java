package dora;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

public class editedBranch {

    // count how many merge commits on the branch

    public List<editedCommit> editedCommitList;
    public Ref branch;
    public int branchCommitCount;
    public int branchMergeCount;
    public int branch2ParentsMergeCount;
    public int branchTextualConfCount;
    public int branchSemanticConfCount;


    /***
     * constructor
     */
    public editedBranch() {
        // initialize all branch-level stats
        this.editedCommitList = new ArrayList<>();
        this.branch = null;
        this.branchCommitCount = 0;
        this.branchMergeCount = 0;
        this.branch2ParentsMergeCount = 0;
        this.branchTextualConfCount = 0;
        this.branchSemanticConfCount = 0;

    }

    

    /***
     * constructor with branch
     */ 
    public editedBranch(Ref branch) {
        // initialize all branch-level stats
        this.editedCommitList = new ArrayList<>();
        this.branch = branch;
        this.branchCommitCount = 0;
        this.branchMergeCount = 0;
        this.branch2ParentsMergeCount = 0;
        this.branchTextualConfCount = 0;
        this.branchSemanticConfCount = 0;

    }
    
    
    /***
     * constructor with branch and revcommit
     */ 
    public editedBranch(Ref branch, RevCommit revcommit) {
        // initialize all branch-level stats
        this.editedCommitList = new ArrayList<>();
        this.addCommitByRevCommit(revcommit);
        
        this.branch = branch;
        this.branchCommitCount = 0;
        this.branchMergeCount = 0;
        this.branch2ParentsMergeCount = 0;
        this.branchTextualConfCount = 0;
        this.branchSemanticConfCount = 0;

    }
    
    
    /***
     * constructor with branch and EditedCommit
     */ 
    public editedBranch(Ref branch, editedCommit editedcommit) {
        // initialize all branch-level stats
        this.editedCommitList = new ArrayList<>();
        this.addCommitByEditedCommit(editedcommit);
        
        this.branch = branch;
        this.branchCommitCount = 0;
        this.branchMergeCount = 0;
        this.branch2ParentsMergeCount = 0;
        this.branchTextualConfCount = 0;
        this.branchSemanticConfCount = 0;

    }
    

    /***
     * constructor
     */
    public editedBranch(editedCommit editedcommit) {
        
        // initialize all branch-level stats
        this.editedCommitList = new ArrayList<>();
        this.addCommitByEditedCommit(editedcommit);
        
        this.branchCommitCount = 0;
        this.branchMergeCount = 0;
        this.branch2ParentsMergeCount = 0;
        this.branchTextualConfCount = 0;
        this.branchSemanticConfCount = 0;

    }
    
    
    /***
     * constructor
     */
    public editedBranch(RevCommit revcommit) {
        // initialize all branch-level stats
        this.editedCommitList = new ArrayList<>();
        this.addCommitByRevCommit(revcommit);
        
        this.branchCommitCount = 0;
        this.branchMergeCount = 0;
        this.branch2ParentsMergeCount = 0;
        this.branchTextualConfCount = 0;
        this.branchSemanticConfCount = 0;

    }
    
    
    
    /***
     * add the commit to the branch's commit list
     * @param revcommit
     */
    public void addCommitByRevCommit(RevCommit revcommit){
        if(this.getEditedCommitList().contains(revcommit) == false){
            editedCommit editedcommit = new editedCommit(revcommit);
            
            this.getEditedCommitList().add(editedcommit);
            this.increBranchCommitCount();
            
            // if merge commit/ 2parents merge commit, update stats
            if(editedcommit.isMergeCommit() == true){
                this.increBranchMergeCount();
                if(editedcommit.is2ParentsMergeCommit() == true)
                {
                    this.increBranch2ParentsMergeCount();
                }
            }
        }
    }
    
    
    /***
     * add the commit to the branch's commit list
     * @param commit
     */
    public void addCommitByEditedCommit(editedCommit editedcommit){
        if(this.getEditedCommitList().contains(editedcommit) == false){
            this.getEditedCommitList().add(editedcommit);
            this.increBranchCommitCount();
            // if merge commit/ 2parents merge commit, update stats
            if(editedcommit.isMergeCommit() == true){
                this.increBranchMergeCount();
                if(editedcommit.is2ParentsMergeCommit() == true)
                {
                    this.increBranch2ParentsMergeCount();
                    if(editedcommit.isHasTextualConflict() == true)
                        this.increBranchTextualConfCount();
                    else if (editedcommit.isHasPotentialSemanticConf() == true)
                        this.increBranchSemanticConfCount();
                }
            }
        }
    }
    

    /***
     * whether in history, the commit is on this branch or not
     * @param editedcommit
     * @return
     */
    public boolean supposeOnThisBranch(editedCommit editedcommit)
    {
        if(editedcommit.getContainedBranches().contains(this.getBranch()) == true)
            return true;
        return false;
    }

    
    
    /***
     * currently, whether the commit is already added to current EditedBranch or not
     * @param editedcommit
     * @return
     */
    public boolean containEditedCommit(editedCommit editedcommit)
    {
        if(this.getEditedCommitList().contains(editedcommit) == true)
            return true;
        return false;
    }
    
    
    
    /***
     * print all commits on the branch
     * @return
     */
    public StringBuilder printEditedBranchInfo()
    {
        StringBuilder branchStr = new StringBuilder();
        
        branchStr.append("Branch Name: " + this.getBranch().getName().toString()+"\n");
        int i = 0;
        for(editedCommit editedcommit : this.getEditedCommitList())
        {
            String isMergeStr = "";
            String hasConflictStr = ""; 
            if(editedcommit.isMergeCommit())
            {
                isMergeStr = "is a MERGE Commit with " + editedcommit.getRevCommit().getParentCount()+ " parents;";
                if(editedcommit.isHasTextualConflict() == true)
                    hasConflictStr = " Has TEXTUAL CONFLICT ;\n";
                else if(editedcommit.isHasPotentialSemanticConf() == true)
                    hasConflictStr = " Has POTENTIAL SEMANTIAL CONFLICT ; \n";
            }
            branchStr.append("\n  Commit"+i+":"+ editedcommit.commitId + "; " + isMergeStr +  hasConflictStr); 
            i++;
        }
        branchStr.append("\n===================================================\n");
        
        return branchStr;
    }
    
    
    /***
     * print stats of the branch
     * @return
     */
    public StringBuilder printBranchStats()
    {
        StringBuilder branchStr = new StringBuilder();
        branchStr.append("\n");
        branchStr.append("Branch Stats Overview:\n");
        
        branchStr.append("1. Branch Name:" + this.getBranch().getName().toString()+"\n");
        branchStr.append("2. Branch Total Commits:" + this.getBranchCommitCount()+"\n");
        branchStr.append("3. Branch Total Merge Commits:" + this.getBranchMergeCount()+"\n");
        branchStr.append("4. Branch Total 2 Parents Merge Commits:" + this.getBranch2ParentsMergeCount() +"\n");
        branchStr.append("5. Branch Total Textual Conflicts:" + this.getBranchTextualConfCount() +"\n");
        branchStr.append("6. Branch Total Potential Semantic Conflicts:" + this.getBranchSemanticConfCount()+"\n");
        
        branchStr.append("\n===================================================\n");
        
        return branchStr;
        
        
    }

    
    
    
    
    public int getBranchCommitCount() {
        return branchCommitCount;
    }



    public void setBranchCommitCount(int branchCommitCount) {
        this.branchCommitCount = branchCommitCount;
    }


    /***
     * this.branchCommitCount ++;
     */
    public void increBranchCommitCount() {
        this.branchCommitCount++;
    }


    public int getBranchMergeCount() {
        return branchMergeCount;
    }



    public void setBranchMergeCount(int branchMergeCount) {
        this.branchMergeCount = branchMergeCount;
    }


    /***
     * this.branchMergeCount ++;
     */
    public void increBranchMergeCount() {
        this.branchMergeCount++;
    }


    public int getBranch2ParentsMergeCount() {
        return branch2ParentsMergeCount;
    }



    public void setBranch2ParentsMergeCount(int branch2ParentsMergeCount) {
        this.branch2ParentsMergeCount = branch2ParentsMergeCount;
    }


    /***
     * this.branch2ParentsMergeCount ++;
     */
    public void increBranch2ParentsMergeCount() {
        this.branch2ParentsMergeCount++;
    }



    public int getBranchTextualConfCount() {
        return branchTextualConfCount;
    }



    public void setBranchTextualConfCount(int branchTextualConfCount) {
        this.branchTextualConfCount = branchTextualConfCount;
    }



    /***
     * this.branchTextualConfCount ++;
     */
    public void increBranchTextualConfCount() {
        this.branchTextualConfCount++;
    }



    public int getBranchSemanticConfCount() {
        return branchSemanticConfCount;
    }



    public void setBranchSemanticConfCount(int branchSemanticConfCount) {
        this.branchSemanticConfCount = branchSemanticConfCount;
    }


    /***
     * this.branchSemanticConfCount ++;
     */
    public void increBranchSemanticConfCount() {
        this.branchSemanticConfCount++;
    }




    
    public List<editedCommit> getEditedCommitList() {
        return editedCommitList;
    }




    
    public void setEditedCommitList(List<editedCommit> editedCommitList) {
        this.editedCommitList = editedCommitList;
    }



    
    public Ref getBranch() {
        return branch;
    }



    
    public void setBranch(Ref branch) {
        this.branch = branch;
    }



    


}

