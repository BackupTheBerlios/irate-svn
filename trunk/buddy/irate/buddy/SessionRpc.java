/*
 * Created on 27/09/2005
 */
package irate.buddy;

import com.sleepycat.je.DatabaseException;

public class SessionRpc {

  private final Context context;

  private Session session;

  public SessionRpc(Context context, Session session) throws DatabaseException {
    this.context = context;
    this.session = session;
  }

  public String login(String account, String password, boolean create) {
    context.logger.info("RPC: login " + account);
    try {
      UniqueId userId = session.login(account, password, create);
      return userId.toString();
    } catch (DatabaseException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void logout(String userIdString) {
    context.logger.info("RPC: logout " + userIdString);
    UniqueId userId = new UniqueId(userIdString);
    session.logout(userId);
  }

//  public void close() {
//    if (session != null) {
//      session.close();
//      session = null;
//    }
//  }
}
