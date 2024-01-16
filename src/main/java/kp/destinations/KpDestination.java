package kp.destinations;

import static kp.Constants.KP_KEY;
import static kp.Constants.TOPIC_SELECT_ORIG;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kp.models.ApprovalStatus;
import kp.models.Information;
import kp.utils.Utils;

/**
 * The destination.
 * <p>
 * Receives the {@link Message}s with the {@link Information} and responds to
 * the origin.
 */
public class KpDestination {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

	private final AtomicBoolean atomicBoolean = new AtomicBoolean();

	/**
	 * The constructor.
	 * 
	 */
	public KpDestination() {
		super();
	}

	/**
	 * Processes the {@link Message}s with the {@link Information}.
	 * 
	 * @param consumerTopic the consumer topic
	 */
	public void process(String consumerTopic) {

		try (final PulsarClient pulsarClient = Utils.createPulsarClient();
				final Consumer<Information> consumer = Utils.createConsumer(pulsarClient, consumerTopic);
				final Producer<Information> producer = Utils.createProducer(pulsarClient, TOPIC_SELECT_ORIG)) {
			while (Utils.sleepMillis()) {
				receiveAndRespond(consumer, producer);
			}
		} catch (IOException e) {
			logger.error("process(): consumerTopic[{}], exception[{}]", consumerTopic, e.getMessage());
		}
	}

	/**
	 * Receive at the destination and respond to the origin.
	 * <p>
	 * This is the <i>Consume</i>, <i>Process</i>, <i>Produce</i> sequence.
	 *
	 * @param consumer the {@link Consumer}
	 * @param producer the {@link Producer}
	 * @throws PulsarClientException the {@link PulsarClientException}
	 */
	private void receiveAndRespond(Consumer<Information> consumer, Producer<Information> producer)
			throws PulsarClientException {

		// Consume
		final Message<Information> message = consumer.receive();
		try {
			final Information information = message.getValue();
			if (logger.isInfoEnabled()) {
				logger.info(String.format("receiveAndRespond():%n" //
						+ "\ttopic[%s],%n\tkey[%s], sequenceId[%s], messageId[%s], information id[%d]", //
						message.getTopicName(), message.getKey(), message.getSequenceId(), message.getMessageId(),
						information.getId()));
			}
			// Process
			information.setApprovalStatus(atomicBoolean.get() ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
			atomicBoolean.set(!atomicBoolean.get());
			// Produce
			producer.newMessage().key(KP_KEY).value(information).sendAsync().thenAccept(this::sendOperationCompleted);
			consumer.acknowledge(message);
		} catch (Exception e) {
			logger.error("receiveAndRespond(): exception[{}]", e.getMessage());
			consumer.negativeAcknowledge(message);
			// by default, failed messages are replayed after a 1-minute delay
		}
	}

	/**
	 * Tracks the completion of the send operation.
	 * 
	 * @param messageId the {@link MessageId} assigned by the broker to the
	 *                  published message
	 */
	private void sendOperationCompleted(MessageId messageId) {
		logger.debug("sendOperationCompleted(): messageId[{}]", messageId);
	}

}
