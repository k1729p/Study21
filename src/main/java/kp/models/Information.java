package kp.models;

import java.util.List;

/**
 * The information. This is the payload for the Pulsar messages.
 */
public class Information {
	private long id;
	private String label;
	private ApprovalStatus approvalStatus;
	private List<Department> departments;

	/**
	 * Constructor.
	 * 
	 */
	public Information() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param id             the id
	 * @param label          the label
	 * @param approvalStatus the {@link ApprovalStatus}
	 * @param departments    the list of the {@link Department}s
	 */
	public Information(long id, String label, ApprovalStatus approvalStatus, List<Department> departments) {
		super();
		this.id = id;
		this.label = label;
		this.approvalStatus = approvalStatus;
		this.departments = departments;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Gets the label.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 * 
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the approval status.
	 * 
	 * @return the approvalStatus
	 */
	public ApprovalStatus getApprovalStatus() {
		return approvalStatus;
	}

	/**
	 * Sets the approval status.
	 * 
	 * @param approvalStatus the approvalStatus to set
	 */
	public void setApprovalStatus(ApprovalStatus approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	/**
	 * Gets the list of the departments.
	 * 
	 * @return the departments
	 */
	public List<Department> getDepartments() {
		return departments;
	}

	/**
	 * Sets the list of the departments.
	 * 
	 * @param departments the departments to set
	 */
	public void setDepartments(List<Department> departments) {
		this.departments = departments;
	}

}
