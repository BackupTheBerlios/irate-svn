package irate.client;

import java.util.Vector;
import irate.common.Track;

/**
 * The job of the VolumeMeister is to determine what volume adjustment should
 * be applied to a given track.
 * It maintains a list of policies, each with a different priority.
 * Policies are processed in order of priority, with each taking the last
 * policy's judgement as a suggestion.  So, the policy with the highest
 * priority may make the final judgement as to what the volume level should
 * be.
 *
 * @author Stephen Blackheath
 */
public class VolumeMeister
{
  private Vector priorities = new Vector();
  private Vector policies = new Vector();

  public VolumeMeister()
  {
  }

  public int determineVolume(Track track)
  {
    int volume = track.getVolume();
    for (int i = 0; i < policies.size(); i++)
      volume = ((VolumePolicy)policies.get(i)).determineVolume(track, volume);
    return volume;
  }

  public void addVolumePolicy(VolumePolicy policy, int priority)
  {
    int insertPos = 0;
    while (insertPos < priorities.size()) {
      Integer thisP = (Integer) priorities.get(insertPos);
      if (thisP.intValue() > priority)
        break;
      else
        insertPos++;
    }
    priorities.insertElementAt(new Integer(priority), insertPos);
    policies.insertElementAt(policy, insertPos);
  }

  public void removeVolumePolicy(VolumePolicy policy)
  {
    for (int i = 0; i < policies.size(); i++) {
      if (policies.get(i) == policy) {
        priorities.remove(i);
        policies.remove(i);
        break;
      }
    }
  }
}
