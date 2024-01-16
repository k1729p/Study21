package kp;

import static kp.Constants.READER_TIMEOUT;
import static kp.Constants.TOPIC_DEST_1;
import static kp.Constants.TOPIC_DEST_2;
import static kp.Constants.TOPIC_ORIG_1;
import static kp.Constants.TOPIC_ORIG_2;
import static kp.Constants.TOPIC_SELECT_DEST;
import static kp.Constants.TOPIC_SELECT_ORIG;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.policies.data.TenantInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PulsarContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import kp.destinations.KpDestination;
import kp.models.Department;
import kp.models.Employee;
import kp.models.Information;
import kp.originators.KpOriginator;
import kp.selectors.KpSelector;
import kp.utils.Marker;
import kp.utils.Utils;

/**
 * The integration test for the message exchange between {@link KpOriginator},
 * {@link KpSelector}, and {@link KpDestination}. It uses the
 * {@link PulsarContainer}.
 * 
 */
class ApplicationTests {

	private static final DockerImageName PULSAR_IMAGE = DockerImageName.parse("apachepulsar/pulsar:latest");
	private static final String TENANT = "kp-tenant";
	private static final String TENANT_NAMESPACE = "kp-tenant/kp-namespace";
	private static final int WAIT_AFTER_START = 5;
	private static final Function<Message<Information>, String> MESSAGE_FUN = message -> String.format(
			"message: topic[%s], key[%s], sequenceId[%s], messageId[%s]", message.getTopicName(), message.getKey(),
			message.getSequenceId(), message.getMessageId());

	/**
	 * The {@link PulsarContainer}.
	 */
	@Container
	PulsarContainer pulsarContainer = new PulsarContainer(PULSAR_IMAGE);

	/**
	 * The constructor.
	 */
	ApplicationTests() {
		super();
	}

	/**
	 * Runs before each test.
	 * 
	 * @throws Exception the exception
	 */
	@BeforeEach
	void initialize() throws Exception {

		pulsarContainer.start();
		Assertions.assertTrue(pulsarContainer.isRunning(), "pulsar container is not running");
		try (PulsarAdmin pulsarAdmin = PulsarAdmin.builder().serviceHttpUrl(pulsarContainer.getHttpServiceUrl())
				.build()) {
			final TenantInfo tenantInfo = TenantInfo.builder().adminRoles(new HashSet<>(Collections.emptyList()))
					.allowedClusters(new HashSet<>(pulsarAdmin.clusters().getClusters())).build();
			pulsarAdmin.tenants().createTenant(TENANT, tenantInfo);
			pulsarAdmin.namespaces().createNamespace(TENANT_NAMESPACE);
		}
		Utils.setRunLocally(true);
		Utils.setServiceUrl(pulsarContainer.getPulsarBrokerUrl());
	}

	/**
	 * The {@link KpDestination} should respond to the {@link KpOriginator}.
	 * 
	 * @throws Exception the exception
	 */
	@Test
	@DisplayName("should respond to the originator")
	void shouldRespondToOriginator() throws Exception {
		// GIVEN
		// WHEN
		new Thread(() -> new KpOriginator().process(TOPIC_ORIG_1, Marker.ODD)).start();
		new Thread(() -> new KpOriginator().process(TOPIC_ORIG_2, Marker.EVEN)).start();
		new Thread(() -> new KpSelector().process(TOPIC_SELECT_ORIG, TOPIC_ORIG_1, TOPIC_ORIG_2)).start();
		new Thread(() -> new KpSelector().process(TOPIC_SELECT_DEST, TOPIC_DEST_1, TOPIC_DEST_2)).start();
		new Thread(() -> new KpDestination().process(TOPIC_DEST_1)).start();
		new Thread(() -> new KpDestination().process(TOPIC_DEST_2)).start();
		final CountDownLatch countDownLatch = new CountDownLatch(1);
		countDownLatch.await(WAIT_AFTER_START, TimeUnit.SECONDS);
		// THEN
		for (String topic : List.of(TOPIC_ORIG_1, TOPIC_ORIG_2)) {
			final Message<Information> message = readFromTopic(topic);
			Assertions.assertNotNull(message, MESSAGE_FUN.apply(message));
			checkInformation(message.getValue());
		}
	}

	/**
	 * Reads the {@link Information} from the topic.
	 * 
	 * @param topic the topic
	 * @return the {@link Message}
	 * @throws Exception the exception
	 */
	private Message<Information> readFromTopic(String topic) throws Exception {

		try (final PulsarClient pulsarClient = PulsarClient.builder().serviceUrl(Utils.getServiceUrl()).build();
				final Reader<Information> reader = pulsarClient.newReader(Schema.JSON(Information.class)).topic(topic)
						.startMessageId(MessageId.earliest).create()) {
			return reader.readNext(READER_TIMEOUT, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * Checks the {@link Information}.
	 * 
	 * @param information the {@link Information}
	 */
	private void checkInformation(Information information) {

		Assertions.assertNotNull(information, "information");
		Assertions.assertTrue(information.getId() > 0, "information id not positive");
		Assertions.assertNotNull(information.getApprovalStatus(), "information approval status");
		List<Department> departmentList = information.getDepartments();
		Assertions.assertNotNull(departmentList, "department list");
		Assertions.assertFalse(departmentList.isEmpty(), "department list is empty");
		departmentList.forEach(this::checkDepartment);
	}

	/**
	 * Checks the {@link Department}.
	 * 
	 * @param department the {@link Department}
	 */
	private void checkDepartment(Department department) {

		Assertions.assertTrue(department.getId() > 0, "department id not positive");
		Assertions.assertNotNull(department.getName(), "department name");
		Assertions.assertFalse(department.getName().isBlank(), "department name is blank");
		Assertions.assertNotNull(department.getMoney(), "department money");
		Assertions.assertNotNull(department.getCreatedAt(), "department created at");
		Assertions.assertTrue(department.getCreatedAt().isAfter(OffsetDateTime.parse("2023-01-01T00:00:00+00:00")),
				"department created at");
		final List<Employee> employeeList = department.getEmployees();
		Assertions.assertNotNull(employeeList, "employee list");
		Assertions.assertFalse(employeeList.isEmpty(), "employee list is empty");
		employeeList.forEach(this::checkEmployee);
	}

	/**
	 * Checks the {@link Employee}.
	 * 
	 * @param employee the {@link Employee}
	 */
	private void checkEmployee(Employee employee) {

		Assertions.assertTrue(employee.getId() > 0, "employee id not positive");
		Assertions.assertNotNull(employee.getFirstName(), "employee first name");
		Assertions.assertFalse(employee.getFirstName().isBlank(), "employee first name is blank");
		Assertions.assertNotNull(employee.getLastName(), "employee last name");
		Assertions.assertFalse(employee.getLastName().isBlank(), "employee last name is blank");
		Assertions.assertNotNull(employee.getTitle(), "employee title");
	}

}
