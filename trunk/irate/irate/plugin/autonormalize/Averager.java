/**
 * Auto-normalize plugin for the iRate project. Automatically normalizes track volume levels.
 *
 * @author Stephen Blackheath <stephen@blacksapphire.com>
 */
package irate.plugin.autonormalize;

/**
 * This class maintains a rolling average of the sums of the squared levels over
 * a number of frames specified in the constructor.
 * It takes the peak value for this rolling average over the whole song.  This is
 * used as the estimate of how loud the song is.
 */
public class Averager
{
    private double frameSumSq;
    private int frameN;

    private int noOfFrames;
    private int head, tail, size;

    private double[] rollingSumSq;
    private int[] rollingN;

    private double sumRollingSumSq;
    private int    sumRollingN;

    private int    rollingAveragesCounted;

    private double peakSquared;

    public Averager(int noOfFrames)
    {
        this.noOfFrames = noOfFrames;
        rollingSumSq = new double[noOfFrames];
        rollingN = new int[noOfFrames];
        head = tail = size = 0;
    }

    public void process(float[] levels, int start, int end)
    {
        for (int i = start; i < end; i++) {
            double l = levels[i];
            frameSumSq += (l*l);
            frameN++;
        }
    }

    public void endOfFrame()
    {
        if (size == noOfFrames) {
            sumRollingSumSq -= rollingSumSq[head];
            sumRollingN     -= rollingN[head];
            head = (head+1) % noOfFrames;
            size--;
        }
        rollingSumSq[tail] = frameSumSq;
        rollingN[tail] = frameN;
        sumRollingSumSq += frameSumSq;
        sumRollingN += frameN;
        tail = (tail+1) % noOfFrames;
        size++;

        if (size == noOfFrames) {
            double levelSquared = calcRollingAverageSquared();
            if (rollingAveragesCounted == 0 || levelSquared > peakSquared)
                peakSquared = levelSquared;
            rollingAveragesCounted++;
        }

        frameSumSq = 0.0;
        frameN = 0;
    }

    private double calcRollingAverageSquared()
    {
        if (sumRollingN == 0)
            return 0.0;
        return sumRollingSumSq / sumRollingN;
    }

    public double getLoudness()
    {
          /* If we have counted no rolling averages yet, then we calculate one level
           * from everything we have seen so far and call that the peak.
           * This happens when we are calculating rolling averages over N frames, but
           * the file is less than N frames long. */
        if (rollingAveragesCounted == 0) {
            peakSquared = calcRollingAverageSquared();
            rollingAveragesCounted++;
        }

        return Math.sqrt(peakSquared);
    }
}
