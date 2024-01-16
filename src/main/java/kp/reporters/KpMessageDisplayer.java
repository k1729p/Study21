package kp.reporters;

import static kp.Constants.THIN_LINE;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.ZoneOffset;

import org.apache.pulsar.client.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kp.models.Department;
import kp.models.Employee;
import kp.models.Information;

/**
 * Displays the {@link Message}.
 */
public class KpMessageDisplayer {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

	/**
	 * The constructor.
	 * 
	 */
	public KpMessageDisplayer() {
		super();
	}

	/**
	 * Displays the {@link Message}.
	 * 
	 * @param message the {@link Message}
	 */
	void displayMessage(Message<Information> message) {

		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(System.lineSeparator()).append(THIN_LINE).append(System.lineSeparator());
		stringBuilder.append("message:").append(System.lineSeparator());
		stringBuilder.append(String.format("topic[%s],%n", message.getTopicName()));
		stringBuilder.append(String.format("key[%s], ", message.getKey()));
		stringBuilder.append(String.format("sequenceId[%s], ", message.getSequenceId()));
		stringBuilder.append(String.format("messageId[%s],%n", message.getMessageId()));
		stringBuilder.append(String.format("publishTime[%tT.%<tL] 'UTC'%n",
				Instant.ofEpochMilli(message.getPublishTime()).atOffset(ZoneOffset.UTC)));
		displayInformation(stringBuilder, message.getValue());
		stringBuilder.append(THIN_LINE);
		logger.info("displayMessage(): {}", stringBuilder);
	}

	/**
	 * Displays the {@link Information}.
	 * 
	 * @param stringBuilder the {@link StringBuilder}
	 * @param information   the {@link Information}
	 */
	void displayInformation(StringBuilder stringBuilder, Information information) {

		stringBuilder.append(String.format("information:%n- id[%d], label[%s], approvalStatus[%s],%n",
				information.getId(), information.getLabel(), information.getApprovalStatus()));
		information.getDepartments().forEach(department -> displayDepartment(stringBuilder, department));
	}

	/**
	 * Displays the {@link Department}.
	 * 
	 * @param stringBuilder the {@link StringBuilder}.
	 * @param department    the {@link Department}
	 */
	private void displayDepartment(StringBuilder stringBuilder, Department department) {

		stringBuilder.append(String.format("- department:%n- - id[%d], name[%s],%n", //
				department.getId(), department.getName()));
		stringBuilder.append(String.format("- - money[%s], createdAt[%tT.%<tL]%n", //
				department.getMoney(), department.getCreatedAt()));
		department.getEmployees().forEach(employee -> displayEmployee(stringBuilder, employee));
	}

	/**
	 * Displays the {@link Employee}.
	 * 
	 * @param stringBuilder the {@link StringBuilder}.
	 * @param employee      the {@link Employee}
	 */
	private void displayEmployee(StringBuilder stringBuilder, Employee employee) {

		stringBuilder.append(String.format("- - employee:%n- - - id[%d], firstName[%s], lastName[%s], title[%s]%n", //
				employee.getId(), employee.getFirstName(), employee.getLastName(), employee.getTitle()));
	}

}
