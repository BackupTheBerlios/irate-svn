package irate.common;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


public class DiskControl {

    File downloadDirectory;
    TrackDatabase allTracks;
    
    public DiskControl(File irateDirectory, TrackDatabase theTracks) {
        downloadDirectory = irateDirectory;
        allTracks = theTracks;
    }
    
    public void clearDiskSpace(int maxAmount) 
    {
        // Determine the amount of space used by iRate downloads
        long currentSize = getDirectorySize(downloadDirectory);

        // Compare with the maxAmount.  The maxAmount is in mb and the size
        // the system gives us is just bytes.  So, multiply by 1 048 576
        long trueMax = maxAmount * 1048576;
        
        
        if(currentSize > trueMax) {
            // To make this simple, we'll consider all songs to be about 5mb -- so some
            // simple math will tell us how many songs need to be axed.  Then we can ask
            // the TrackDatabase to give us the N+1 worst rated songs available and 
            // simply delete those.  This will ensure we're always under the max.
            long difference = currentSize - trueMax;
            int numberToDelete = (int)(difference / (1048576*5)) + 1;
            int currentMax = 2;
            Set tracksToDelete = new HashSet();
            while(numberToDelete > 0) {
                Track choice = allTracks.chooseTrack(currentMax, tracksToDelete);
                if(choice == null) {
                    // In this case, we've deleted everything, I believe.
                    if(currentMax == 10) {
                     numberToDelete = 0;   
                    }
                    // Otherwise, go up and delete better songs still.
                    currentMax += 3;
                }
                else {
                    tracksToDelete.add(choice);
                    numberToDelete--;
                }
            }
            System.out.println(tracksToDelete.toString());
            
            
            System.out.println(numberToDelete);
            
        }
        
    
    }
    
    private long getDirectorySize(File directory) {
        long finalSize = 0;  
        File files[] = directory.listFiles();
        for(int i=0; i<files.length; ++i) {
            finalSize += files[i].length();
        }
        return finalSize;
    }
    
}
