package kp.models;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * The department
 * 
 */
public class Department {

	private Long id;

	private String name;

	private BigDecimal money;

	private OffsetDateTime createdAt;

	private List<Employee> employees;

	/**
	 * Constructor.
	 * 
	 */
	public Department() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param id        the id
	 * @param name      the name
	 * @param money     the money
	 * @param employees the list of employees
	 */
	public Department(Long id, String name, BigDecimal money, List<Employee> employees) {
		super();
		this.id = id;
		this.name = name;
		this.employees = employees;
		this.money = money;
		this.createdAt = OffsetDateTime.now();
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the money.
	 * 
	 * @return the money
	 */
	public BigDecimal getMoney() {
		return money;
	}

	/**
	 * Sets the money.
	 * 
	 * @param money the money to set
	 */
	public void setMoney(BigDecimal money) {
		this.money = money;
	}

	/**
	 * Gets the created at.
	 * 
	 * @return the createdAt
	 */
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	/**
	 * Sets the created at.
	 * 
	 * @param createdAt the createdAt to set
	 */
	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Gets the employees.
	 * 
	 * @return the employees
	 */
	public List<Employee> getEmployees() {
		return employees;
	}

	/**
	 * Sets the employees.
	 * 
	 * @param employees the employees to set
	 */
	public void setEmployees(List<Employee> employees) {
		this.employees = employees;
	}

}
