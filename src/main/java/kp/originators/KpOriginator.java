package kp.originators;

import static kp.Constants.KP_KEY;
import static kp.Constants.THIN_LINE;
import static kp.Constants.TOPIC_SELECT_DEST;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kp.models.Information;
import kp.utils.Marker;
import kp.utils.Utils;

/**
 * The originator.
 * <p>
 * Sends the {@link Message}s with the {@link Information} to the destination
 * and receives the responses from it.
 */
public class KpOriginator {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

	private final AtomicLong atomicLong = new AtomicLong();

	private final String label = Utils.createLabel();

	/**
	 * The constructor.
	 * 
	 */
	public KpOriginator() {
		super();
	}

	/**
	 * Processes the {@link Message}s with the {@link Information}.
	 * 
	 * @param consumerTopic the consumer topic
	 * @param marker        the {@link Marker}
	 */
	public void process(String consumerTopic, Marker marker) {

		try (final PulsarClient pulsarClient = Utils.createPulsarClient();
				final Producer<Information> producer = Utils.createProducer(pulsarClient, TOPIC_SELECT_DEST);
				final Consumer<Information> consumer = Utils.createConsumer(pulsarClient, consumerTopic,
						this::receive)) {
			do {
				sendMessage(producer, marker);
			} while (Utils.sleepSeconds());
		} catch (IOException e) {
			logger.error("process(): consumerTopic[{}], marker[{}], exception[{}]", consumerTopic, marker,
					e.getMessage());
		}
	}

	/**
	 * Sends the {@link Message} with {@link Information}.
	 * 
	 * @param producer the producer
	 * @param marker   the marker
	 */
	private void sendMessage(Producer<Information> producer, Marker marker) {

		if (Marker.ODD == marker && atomicLong.get() % 2 == 1 || Marker.EVEN == marker && atomicLong.get() % 2 == 0) {
			atomicLong.incrementAndGet();
		}
		final long informationId = atomicLong.incrementAndGet();
		producer.newMessage().key(KP_KEY).value(Utils.createInformation(informationId, label)).sendAsync()
				.thenAccept(this::sendOperationCompleted);
		logger.info("sendMessage(): marker[{}], informationId[{}], label[{}]", marker, informationId, label);
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

	/**
	 * This method is called whenever a new {@link Message} is received.
	 *
	 * @param consumer the {@link Consumer} that received the message
	 * @param message  the {@link Message} with the {@link Information}
	 */
	private void receive(Consumer<Information> consumer, Message<Information> message) {

		try {
			if (logger.isInfoEnabled()) {
				logger.info(String.format("receive():%n" //
						+ "\ttopic[%s],%n\tkey[%s], sequence id[%s], message id[%s], information id[%d]", //
						message.getTopicName(), message.getKey(), message.getSequenceId(), message.getMessageId(),
						message.getValue().getId()));
			}
			consumer.acknowledge(message);
		} catch (Exception e) {
			logger.error("receive(): exception[{}]", e.getMessage());
			consumer.negativeAcknowledge(message);
			// by default, failed messages are replayed after a 1-minute delay
		}
		if (logger.isInfoEnabled() && Utils.isRunLocally()) {
			logger.info(String.format("%n%s", THIN_LINE));
		}
	}

}
