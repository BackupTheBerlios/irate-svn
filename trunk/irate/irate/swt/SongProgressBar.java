package irate.swt;


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;


public class SongProgressBar extends Composite {
  
  private Label currentTimeText;
  private Label totalTimeText;
  private ProgressBar progressBar;
  
  private int currentTotalTime;
  
  public SongProgressBar(Composite parent, int flags) {
    super(parent, flags);
    currentTotalTime = 0;
   
    setLayout(new GridLayout(3,false));
    
    currentTimeText = new Label(this, SWT.NONE);
    currentTimeText.setText("---:---");
    currentTimeText.pack();
    
    progressBar = new ProgressBar(this, SWT.SMOOTH);
    progressBar.pack();
    
    totalTimeText = new Label(this, SWT.NONE);
    totalTimeText.setText("---:---");
    totalTimeText.pack();
  }
  
  /**
   * Set the progress bar ahead by a tick and update the text to the 
   * current time.
   * @param currentTimeInSeconds the current number of seconds into the song
   */
  public void setCurrentTime(int currentTimeInSeconds) {
    progressBar.setSelection(currentTimeInSeconds);
    currentTimeText.setText(formatTimeToString(currentTimeInSeconds));
  }
  
  /**
   * Set the text at the right end of the progress bar and the maximum
   * property on the progress bar.  This is the total length of the song.
   * @param totalTimeInSeconds total length of the song in seconds
   */
  public void setTotalTime(int totalTimeInSeconds) {
    // Don't do any work if the total time hasn't changed.
    if(currentTotalTime != totalTimeInSeconds) {
      progressBar.setMaximum(totalTimeInSeconds); 
      totalTimeText.setText(formatTimeToString(totalTimeInSeconds));
    }
  }
  
  /** 
   * Given an int, this returns a string number repersentation of that 
   * number, and if it is a single digit, it inserts '0' before the number.
   * @param number the number to be formatted
   * @return a string value of the number with '0' inserted if needed
   */
  private String format00(int number)
  {
    if (number < 10)
      return "0"+Integer.toString(number);
    else
      return Integer.toString(number);
  }

  /**
   * Take a play time in seconds and convert it to a string in the
   * form of 00:00
   * @param playTime number of seconds to be converted
   * @return a string in the form of 00:00
   */
  private String formatTimeToString(int playTime)
  {
    int minutes = (int) (playTime / 60L);
    int seconds = (int) ((playTime - 60*minutes));
    return new String(format00(minutes)+":"+format00(seconds));
  }
}