package fluid.entity;

import java.util.List;

/**
 * Interface to allow entities to be connected to each other. 
 * At the moment, a connection between two entities either exists or it doesn't.
 *
 * @author mjanes
 *
 */
public interface IConnectedEntity {
	
	public void addConnection(IConnectedEntity entity);
	public void removeConnection(IConnectedEntity entity);
	public List<IConnectedEntity> getConnections();

}
