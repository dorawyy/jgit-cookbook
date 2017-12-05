package dora;


public class branchPrep {

    // count how many merge commits on the branch
    public int branchCommitCount ;
    public int branchMergeCount ; 
    public int branch2ParentsMergeCount ;
    public int branchTextualConfCount ;
    public int branchSemanticConfCount ;

    
    /***
     * constructor
     */
    branchPrep()
    {   
        // initialize all branch-level stats
       this.branchCommitCount=0 ;
       this.branchMergeCount=0 ; 
       this.branch2ParentsMergeCount=0 ;
       this.branchTextualConfCount=0 ;
       this.branchSemanticConfCount=0 ;
        
    }
    
    
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        

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
    public void increBranchCommitCount()
    {
        this.branchCommitCount ++;
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
    public void increBranchMergeCount()
    {
        this.branchMergeCount ++;
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
    public void increBranch2ParentsMergeCount()
    {
        this.branch2ParentsMergeCount ++;
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
    public void increBranchTextualConfCount()
    {
        this.branchTextualConfCount ++;
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
    public void increBranchSemanticConfCount()
    {
        this.branchSemanticConfCount ++;
    }

}
