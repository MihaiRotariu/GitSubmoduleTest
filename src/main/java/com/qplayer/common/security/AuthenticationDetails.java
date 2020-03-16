package com.qplayer.common.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holder for custom information about the authenticated user, currently the list of accessible courses.
 * @author Mircea Nagy
 *
 */
public class AuthenticationDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -59076795161261230L;

	private final List<Long> accessibleLevelIds = new ArrayList<>();
	
	public List<Long> getAccessibleLevelIds() {
		return Collections.unmodifiableList(accessibleLevelIds);
	}
	
	public void addAccessibleLevelId(List<Long> ids) {
		accessibleLevelIds.addAll(ids);
	}
	
	public void addAccessibleLevelId(Long id) {
		accessibleLevelIds.add(id);
	}
	
	public void removeAccessibleLevelId(Long id) {
		accessibleLevelIds.remove(id);
	}
}
