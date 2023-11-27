package kp.utils;

import static kp.Constants.KP_SUBSCRIPTION;
import static kp.Constants.MILLIS_TO_SLEEP;
import static kp.Constants.SECONDS_TO_SLEEP;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.MessageListener;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kp.models.ApprovalStatus;
import kp.models.Department;
import kp.models.Employee;
import kp.models.Information;
import kp.models.Title;

/**
 * The utilities.
 *
 */
public class Utils {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

	private static Random random = null;
	static {
		try {
			random = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			System.exit(1);
		}
	}
	private static String serviceUrl = "pulsar://pulsar:6650";
	private static boolean runLocally = false;

	/**
	 * The hidden constructor.
	 * 
	 */
	private Utils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Gets the Pulsar service URL.
	 * 
	 * @return the serviceUrl
	 */
	public static String getServiceUrl() {
		return serviceUrl;
	}

	/**
	 * Sets the Pulsar service URL.
	 * 
	 * @param serviceUrl the serviceUrl to set
	 */
	public static void setServiceUrl(String serviceUrl) {
		Utils.serviceUrl = serviceUrl;
	}

	/**
	 * Gets 'run locally' flag.
	 * <p>
	 * The flag, which is set 'true' for the local execution.
	 * 
	 * @return the runLocally
	 */
	public static boolean isRunLocally() {
		return runLocally;
	}

	/**
	 * Sets 'run locally' flag.
	 * 
	 * @param runLocally the runLocally to set
	 */
	public static void setRunLocally(boolean runLocally) {
		Utils.runLocally = runLocally;
	}

	/**
	 * Creates the {@link PulsarClient}.
	 * 
	 * @return the {@link PulsarClient}
	 * @throws PulsarClientException the {@link PulsarClientException}
	 */
	public static PulsarClient createPulsarClient() throws PulsarClientException {
		return PulsarClient.builder().serviceUrl(Utils.serviceUrl).build();
	}

	/**
	 * Creates the {@link Producer}.
	 * 
	 * @param pulsarClient the {@link PulsarClient}
	 * @param topic        the topic
	 * @return the {@link Producer}
	 * @throws PulsarClientException the {@link PulsarClientException}
	 */
	public static Producer<Information> createProducer(PulsarClient pulsarClient, String topic)
			throws PulsarClientException {
		return pulsarClient.newProducer(Schema.JSON(Information.class)).topic(topic).create();
	}

	/**
	 * Creates the {@link Consumer}.
	 * 
	 * @param pulsarClient    the {@link PulsarClient}
	 * @param topic           the topic
	 * @param messageListener the {@link MessageListener}.
	 * @return the {@link Consumer}
	 * @throws PulsarClientException the {@link PulsarClientException}
	 */
	public static Consumer<Information> createConsumer(PulsarClient pulsarClient, String topic,
			MessageListener<Information> messageListener) throws PulsarClientException {

		return pulsarClient.newConsumer(Schema.JSON(Information.class)).subscriptionName(KP_SUBSCRIPTION).topic(topic)
				.messageListener(messageListener).subscribe();
	}

	/**
	 * Creates the {@link Consumer} and subscribes to it.
	 * 
	 * @param pulsarClient the {@link PulsarClient}
	 * @param topic        the topic
	 * @return the {@link Consumer}
	 * @throws PulsarClientException the {@link PulsarClientException}
	 */
	public static Consumer<Information> createConsumer(PulsarClient pulsarClient, String topic)
			throws PulsarClientException {

		return pulsarClient.newConsumer(Schema.JSON(Information.class)).subscriptionName(KP_SUBSCRIPTION).topic(topic)
				.subscribe();
	}

	/**
	 * Creates the {@link Consumer} and subscribes to it with the <i>Failover</i>
	 * {@link SubscriptionType}.
	 * 
	 * @param pulsarClient the {@link PulsarClient}
	 * @param topic        the topic
	 * @return the {@link Consumer}
	 * @throws PulsarClientException the {@link PulsarClientException}
	 */
	public static Consumer<Information> createConsumerWithFailover(PulsarClient pulsarClient, String topic)
			throws PulsarClientException {

		return pulsarClient.newConsumer(Schema.JSON(Information.class)).subscriptionName(KP_SUBSCRIPTION).topic(topic)
				.subscriptionType(SubscriptionType.Failover).subscribe();
	}

	/**
	 * Creates the {@link Reader}.
	 * 
	 * @param pulsarClient the {@link PulsarClient}
	 * @param topic        the topic
	 * @return the {@link Reader}
	 * @throws PulsarClientException the {@link PulsarClientException}
	 */
	public static Reader<Information> createReader(PulsarClient pulsarClient, String topic)
			throws PulsarClientException {

		return pulsarClient.newReader(Schema.JSON(Information.class)).topic(topic).startMessageId(MessageId.earliest)
				.create();
	}

	/**
	 * Creates the {@link Reader}.
	 * 
	 * @param pulsarClient the {@link PulsarClient}
	 * @param topicList    the list of the topics
	 * @param messageId    the {@link MessageId}
	 * @return the {@link Reader}
	 * @throws PulsarClientException the {@link PulsarClientException}
	 */
	public static Reader<Information> createReader(PulsarClient pulsarClient, List<String> topicList,
			MessageId messageId) throws PulsarClientException {

		return pulsarClient.newReader(Schema.JSON(Information.class)).topics(topicList).startMessageId(messageId)
				.create();
	}

	/**
	 * Creates the {@link Information}.
	 * 
	 * @param id    the {@link Information} id
	 * @param label the label
	 * @return the {@link Information}.
	 */
	public static Information createInformation(long id, String label) {

		return new Information(id, label, ApprovalStatus.OPEN, createDepartments(id));
	}

	/**
	 * Creates the list of {@link Department}s.
	 * 
	 * @param informationId the {@link Information} id
	 * @return the list of {@link Department}s
	 */
	private static List<Department> createDepartments(long informationId) {

		return IntStream.rangeClosed(1, 2).boxed().map(counter -> {
			final long id = 10 * informationId + counter;
			final BigDecimal money = BigDecimal.valueOf(random.nextLong()).add(BigDecimal.valueOf(random.nextDouble()));
			return new Department(id, "name-" + id, money, createEmployees(id));
		}).toList();
	}

	/**
	 * Creates the list of {@link Employee}s.
	 * 
	 * @param departmentId the {@link Department} id
	 * @return the list of {@link Employee}s.
	 */
	private static List<Employee> createEmployees(long departmentId) {

		return IntStream.rangeClosed(1, 3).boxed().map(counter -> {
			long id = 10 * departmentId + counter;
			return new Employee(id, "f-name-" + id, "l-name-" + id, Title.values()[counter - 1]);
		}).toList();
	}

	/**
	 * Sleeps milliseconds.
	 * 
	 * @return the flag
	 */
	public static boolean sleepMillis() {

		return sleepMillis(MILLIS_TO_SLEEP);
	}

	/**
	 * Sleeps milliseconds.
	 * 
	 * @param milliseconds the milliseconds
	 * @return the flag
	 */
	public static boolean sleepMillis(long milliseconds) {
		try {
			TimeUnit.MILLISECONDS.sleep(milliseconds);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("sleepMillis(): InterruptedException[{}]", e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Sleeps seconds.
	 * 
	 * @return the flag
	 */
	public static boolean sleepSeconds() {

		try {
			TimeUnit.SECONDS.sleep(SECONDS_TO_SLEEP);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("sleepSeconds(): InterruptedException[{}]", e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Creates random label.
	 * 
	 * @return the label
	 */
	public static String createLabel() {

		return String.format("%03d", random.nextInt(999));
	}

}
