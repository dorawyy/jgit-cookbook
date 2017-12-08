package ca.ubc.wyingying.traversebranches;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

public class commitPrep {

    public String commitId;
    public RevCommit revCommit; // the commit itself
    public List<String> containedBranches = null; // list of branches that contain the commit
    
    public List<RevCommit> parents; // the list of parent ids
    
    public String commitMsg; // commit message
    public int commitTime; // commit time
    public boolean hasTextualConflict = false;
    public boolean hasPotentialSemanticConf = false;
    
    
    /***
     * Constructor with only revCommit
     * @param revCommit
     */
    public commitPrep(RevCommit revCommit){
        this.revCommit = revCommit;
        
        this.commitId = revCommit.getId().getName().toString();

        // get all parents
        this.parents = null;
        for(RevCommit parent:revCommit.getParents()){
            this.parents.add(parent);
        }
         
        
    }

    
    

    
    public RevCommit getRevCommit() {
        return revCommit;
    }


    
    public void setRevCommit(RevCommit revCommit) {
        this.revCommit = revCommit;
    }


    
    public List<String> getContainedBranches() {
        return containedBranches;
    }


    
    public void setContainedBranches(List<String> containedBranches) {
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
    public void addCommitBranch(String branch)
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
