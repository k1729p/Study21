package kp.models;

/**
 * The employee
 * 
 */
public class Employee {

	private Long id;

	private String firstName;

	private String lastName;

	private Title title;

	/**
	 * Constructor.
	 * 
	 */
	public Employee() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param id        the id
	 * @param firstName the first name
	 * @param lastName  the last name
	 * @param title     the title
	 */
	public Employee(Long id, String firstName, String lastName, Title title) {
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.title = title;
	}

	/**
	 * Gets the id
	 * 
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the id
	 * 
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Gets the first name
	 * 
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Sets the first name
	 * 
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Gets the last name
	 * 
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Sets the last name
	 * 
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Gets the title
	 * 
	 * @return the title
	 */
	public Title getTitle() {
		return title;
	}

	/**
	 * Sets the title
	 * 
	 * @param title the title to set
	 */
	public void setTitle(Title title) {
		this.title = title;
	}

}
