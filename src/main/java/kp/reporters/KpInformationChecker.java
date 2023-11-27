package kp.reporters;

import static kp.Constants.LINE_SEP;

import java.util.List;

import kp.models.ApprovalStatus;
import kp.models.Department;
import kp.models.Employee;
import kp.models.Information;

/**
 * Checks the {@link Information}.
 * 
 */
public class KpInformationChecker {

	/**
	 * The constructor.
	 * 
	 */
	public KpInformationChecker() {
		super();
	}

	/**
	 * Checks the {@link Information}.
	 * 
	 * @param checkRecord     the check record
	 * @param informationDest the {@link Information} from destination topic
	 * @param informationOrig the {@link Information} from origin topic
	 */
	void checkInformation(StringBuilder checkRecord, Information informationDest, Information informationOrig) {

		if (ApprovalStatus.OPEN != informationDest.getApprovalStatus()
				|| ApprovalStatus.REJECTED != informationOrig.getApprovalStatus()
						&& ApprovalStatus.APPROVED != informationOrig.getApprovalStatus()) {
			checkRecord.append("ApprovalStatus").append(LINE_SEP);
		}
		final List<Department> departmentListDest = informationDest.getDepartments();
		final List<Department> departmentListOrig = informationOrig.getDepartments();
		if (departmentListDest.size() != departmentListOrig.size()) {
			checkRecord.append("Department list size").append(LINE_SEP);
			return;
		}
		for (int i = 0; i < departmentListDest.size(); i++) {
			checkDepartment(checkRecord, departmentListDest.get(i), departmentListOrig.get(i));
		}
	}

	/**
	 * Checks the {@link Department}.
	 * 
	 * @param checkRecord    the check record
	 * @param departmentDest the {@link Department} from destination topic
	 * @param departmentOrig the {@link Department} from origin topic
	 */
	private void checkDepartment(StringBuilder checkRecord, Department departmentDest, Department departmentOrig) {

		if (!departmentDest.getId().equals(departmentOrig.getId())) {
			checkRecord.append("Department id").append(LINE_SEP);
		}
		if (!departmentDest.getName().equals(departmentOrig.getName())) {
			checkRecord.append("Department name").append(LINE_SEP);
		}
		if (departmentDest.getMoney().compareTo(departmentOrig.getMoney()) != 0) {
			checkRecord.append("Department money").append(LINE_SEP);
		}
		if (!departmentDest.getCreatedAt().equals(departmentOrig.getCreatedAt())) {
			checkRecord.append("Department created at").append(LINE_SEP);
		}
		final List<Employee> employeeListDest = departmentDest.getEmployees();
		final List<Employee> employeeListOrig = departmentOrig.getEmployees();
		if (employeeListDest.size() != employeeListOrig.size()) {
			checkRecord.append("Employee list size").append(LINE_SEP);
			return;
		}
		for (int i = 0; i < employeeListDest.size(); i++) {
			checkEmployee(checkRecord, employeeListDest.get(i), employeeListOrig.get(i));
		}
	}

	/**
	 * Checks the {@link Employee}.
	 * 
	 * @param checkRecord  the check record
	 * @param employeeDest the {@link Employee} from destination topic
	 * @param employeeOrig the {@link Employee} from origin topic
	 */
	private void checkEmployee(StringBuilder checkRecord, Employee employeeDest, Employee employeeOrig) {

		if (!employeeDest.getId().equals(employeeOrig.getId())) {
			checkRecord.append("Employee id").append(LINE_SEP);
		}
		if (!employeeDest.getFirstName().equals(employeeOrig.getFirstName())) {
			checkRecord.append("Employee first name").append(LINE_SEP);
		}
		if (!employeeDest.getLastName().equals(employeeOrig.getLastName())) {
			checkRecord.append("Employee last name").append(LINE_SEP);
		}
		if (employeeDest.getTitle() != employeeOrig.getTitle()) {
			checkRecord.append("Employee title").append(LINE_SEP);
		}
	}

}
