package ca.ubc.wyingying.parserepo;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.dstadler.jgit.helper.CookbookHelper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class repoAllCommits {

    
    /***
     * "git log" to get all commits of the repo
     * @param git
     * @return
     * @throws NoHeadException
     * @throws GitAPIException
     * @throws IOException
     */
    public static Iterable<RevCommit> getAllCommitsFromLog(Git git) throws NoHeadException, GitAPIException, IOException{
        // get all commits from git log
        Iterable<RevCommit> commits = git.log().all().call();
        return commits;
    }
    
    
    
    public static StringBuilder strBuilderOfAllCommits(Iterable<RevCommit> commits){
        StringBuilder strBuilder = new StringBuilder("");
        int count = 0;
        for(RevCommit commit:commits){
            //System.out.println("LogCommit: " + commit);
            strBuilder.append(commit);
            count++;
        }
        return strBuilder;
    }
    
    
    
    
    
    /***
     * print all commits of the repo out to a file
     * @param commits
     * @param path
     * @param file
     * @throws IOException 
     * @throws GitAPIException 
     * @throws NoHeadException 
     */
    public static void saveAllCommitsToFile(Git git, String filePath, String fileName) throws IOException, NoHeadException, GitAPIException{
        
        // create output file
        File outputFile = output.createFile(filePath, fileName);
        // set up outputStream
        OutputStream os = output.setupStream(outputFile);
        
        // get all commits, set up StringBuilder
        Iterable<RevCommit> commits = getAllCommitsFromLog(git);
        StringBuilder str = strBuilderOfAllCommits(commits);
        
        // output the strings 
        output.outputToFileUsingStream(str.toString(), os);
        
        // close the outputStream
        output.closeStream(os);
        
    }
    
    
    
    
}

