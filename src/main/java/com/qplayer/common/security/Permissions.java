package com.qplayer.common.security;

public enum Permissions {

	QORGANIZER_ALL("Q-Organizer/all"),
	QORGANIZER_WRITER("Q-Organizer/writer"),
	QORGANIZER_EDITOR("Q-Organizer/editor"),
	QORGANIZER_CREATOR("Q-Organizer/creator"),
	QORGANIZER_APPROVER("Q-Organizer/approver"),
	QORGANIZER_MEDIA("Q-Organizer/media"),
	
	QPLANNER_TEACHER("Q‐Planner/teacher"),
	QPLANNER_PUBLISHER("Q‐Planner/publisher"),
	QPLANNER_WRITER("Q‐Planner/writer"),
	QPLANNER_EDITOR("Q‐Planner/editor"),
	
	QPLAYER_STUDENT("Q‐Player/student"),
	QPLAYER_TEACHER("Q‐Player/teacher"),
	
	QMONITOR_STUDENT("Q‐Monitor/student"),
	QMONITOR_TEACHER("Q‐Monitor/teacher"),
	QMONITOR_MANAGER("Q‐Monitor/manager"),
	QMONITOR_SCHOOL("Q‐Monitor/school"),
	QMONITOR_NETWORK("Q‐Monitor/network"),
	QMONITOR_PUBLISHER("Q‐Monitor/publisher"),
	
	USER_MANAGEMENT("user_management"),
	LICENSING("licensing"),

	// DEPRECATED -->
	QORG_BLOCK_META("Q-Organizer/Main/block metadata"),
	QORG_TASK_META("Q-Organizer/Main/task metadata"),
	QORG_MAIN_NEXT("Q-Organizer/Main/next"),
	QORG_MESO("Q-Organizer/Main/meso"),
	QORG_EXISTING_LO("Q-Organizer/Main/existing LO"),
	
	QORG_MESO_MOVE("Q-Organizer/Meso/move"),
	QORG_MESO_DELETE("Q-Organizer/Meso/delete"),
	QORG_MESO_SUBMIT("Q-Organizer/Meso/submit"),
	
	QORG_CEF_PREVIOUS("Q-Organizer/CEF/previous"),
	QORG_CEF_MENU("Q-Organizer/CEF/menu"),
	QORG_CEF_REVIEW("Q-Organizer/CEF/review"),
	QORG_CEF_REWRITE("Q-Organizer/CEF/rewrite"),
	QORG_CEF_APPROVED("Q-Organizer/CEF/approved"),
	QORG_CEF_SUBMIT("Q-Organizer/CEF/submit"),
	QORG_CEF_PREVIEW("Q-Organizer/CEF/preview"),
	QORG_CEF_NEXT("Q-Organizer/CEF/next"),
	QORG_CEF_MEDIA_STATE("Q-Organizer/CEF/media state"),
	QORG_CEF_MEDIA_URL("Q-Organizer/CEF/media URL"),
	QORG_CEF_MEDIA_ADD("Q-Organizer/CEF/add media"),
	QORGANIZER_TEACHER("Q-Organizer/teacher"),
	QORGANIZER_PUBLISHER("Q-Organizer/publisher"),
	QORGANIZER_WHATEVER("Q-Organizer/whateves");
	// <-- DEPRECATED
	
	private String fullName;

	Permissions(String fullName) {
		this.fullName = fullName;
	}
	
	public String getFullName() {
		return fullName;
	}
}
