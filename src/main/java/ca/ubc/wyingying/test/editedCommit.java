package ca.ubc.wyingying.test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

public class editedCommit {



    public String commitId;
    public RevCommit revCommit; // the commit itself
    public List<Ref> containedBranches; // list of branches that contain the commit
    
    public List<RevCommit> parents; // the list of parent ids
    
    public String commitMsg; // commit message
    public int commitTime; // commit time
    public boolean hasTextualConflict = false;
    public boolean hasPotentialSemanticConf = false;
    
    
    /***
     * Constructor with only revCommit
     * @param revCommit
     */
    public editedCommit(RevCommit revCommit){
        this.revCommit = revCommit;
        
        this.commitId = revCommit.getId().getName().toString();

        this.containedBranches = new ArrayList<>();
        
        // get all parents
        this.parents = new ArrayList<>();
        for(RevCommit parent:revCommit.getParents()){
            this.parents.add(parent);
        }
         
        
    }
    
    
    
    /***
     * Constructor with revCommit and branchInfo
     * @param revCommit
     */
    public editedCommit(RevCommit revCommit, Ref branch){
        this.revCommit = revCommit;
        this.commitId = revCommit.getId().getName().toString();
        this.containedBranches = new ArrayList<>();
        this.addCommitBranch(branch);
        // get all parents
        this.parents = null;
        for(RevCommit parent:revCommit.getParents()){
            this.parents.add(parent);
        }
         
        
    }
    
    
    /***
     * print one commit info, including
     * Commit id
     * (1) commit parents
     * (2) commit branches
     * (3) commit message
     * (4) committer
     * (5) commit time
     * @param commit
     * @return
     */
    public StringBuilder printEditedCommit(){
        StringBuilder commitInfoStr = new StringBuilder();
        
        commitInfoStr.append("Commit id : " + this.commitId + "\n");
        commitInfoStr.append("    (1) Commit has " + this.getRevCommit().getParentCount()+ " parents: \n");
        int i=0;
        for(RevCommit parent: this.getParents())
        {
            commitInfoStr.append("          Parent"+ i+ ":" + parent.getId().getName().toString() +"\n");
            i++;
        }
        
        commitInfoStr.append("    (2) Commit is on branches: \n");
        i = 0;
        for(Ref branch: this.getContainedBranches()){
            commitInfoStr.append("          Branch"+i + ":"+ branch.getName().toString() +"\n");
            i++;
        }
        commitInfoStr.append("    (3) Commit Message: " + this.getRevCommit().getFullMessage().toString() + "\n");
        commitInfoStr.append("    (4) Committer: " + this.getRevCommit().getCommitterIdent().toString()+ "\n");
        commitInfoStr.append("    (5) Commit Time: " + this.getRevCommit().getCommitTime()+ "\n");
        commitInfoStr.append("\n");
        return commitInfoStr;
    } 

  
    
    
    /***
     * Get editedCommit object by RevCommit
     * @param revcommit
     * @return
     */
    public editedCommit findByRevCommit(RevCommit revcommit)
    {
        if(revcommit.getId() == this.revCommit.getId()){
            return this;
        }
        return null;
    }
    
    
    
    /***
     * Get editedCommit object by commitID
     * @param revcommit
     * @return
     */
    public editedCommit findByObjectID(ObjectId objectid)
    {
        if(objectid == this.revCommit.getId()){
            return this;
        }
        return null;
    }
    
    
    
    /***
     * Return True if the RevCommit of the two editedCommit have the same objectID
     * @param editcommit
     * @return
     */
    public boolean isEqual(editedCommit editcommit)
    {
        // if there is something with the same revCommit objectId, then equal
        if(this.revCommit.getId() == editcommit.getRevCommit().getId())
        {
            return true;
        }
        return false;
    }
    
    

    
    public RevCommit getRevCommit() {
        return revCommit;
    }


    
    public void setRevCommit(RevCommit revCommit) {
        this.revCommit = revCommit;
    }


    
    public List<Ref> getContainedBranches() {
        return containedBranches;
    }


    
    public void setContainedBranches(List<Ref> containedBranches) {
        this.containedBranches = containedBranches;
    }


    /*** 
     * return TRUE if this is a merge commit
     * @return
     */
    public boolean isMergeCommit()
    {
        if( this.revCommit.getParentCount()>1)
            return true;
        return false;
    }
    
    
    /*** 
     * return TRUE if this is a merge commit with 2 parents
     * @return
     */
    public boolean is2ParentsMergeCommit()
    {
        if( this.revCommit.getParentCount()==2)
            return true;
        return false;
    }



    
    public List<RevCommit> getParents() {
        return parents;
    }


    
    public void setParents(List<RevCommit> parents) {
        this.parents = parents;
    }


    
    public String getCommitMsg() {
        return commitMsg;
    }


    
    public void setCommitMsg(String commitMsg) {
        this.commitMsg = commitMsg;
    }


    
    public int getCommitTime() {
        return commitTime;
    }


    
    public void setCommitTime(int commitTime) {
        this.commitTime = commitTime;
    }
    
    
    /***
     * Add one branch to the containedBranches list when the branch is not in the list
     * @param branch
     */
    public void addCommitBranch(Ref branch)
    {
        // if the branch is not added to the list, then add the branch to list
        if(this.containedBranches.contains(branch) == false){
            this.containedBranches.add(branch);
        }
    }





    
    public boolean isHasTextualConflict() {
        return hasTextualConflict;
    }





    
    public void setHasTextualConflict(boolean hasTextualConflict) {
        this.hasTextualConflict = hasTextualConflict;
    }





    
    public boolean isHasPotentialSemanticConf() {
        return hasPotentialSemanticConf;
    }





    
    public void setHasPotentialSemanticConf(boolean hasPotentialSemanticConf) {
        this.hasPotentialSemanticConf = hasPotentialSemanticConf;
    }
    
    
}