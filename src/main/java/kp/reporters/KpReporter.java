package kp.reporters;

import static kp.Constants.MILLIS_TO_SLEEP;
import static kp.Constants.READER_TIMEOUT;
import static kp.Constants.READ_LATEST_MAX_RETRIES;
import static kp.Constants.TOPIC_DEST_1;
import static kp.Constants.TOPIC_DEST_2;
import static kp.Constants.TOPIC_ORIG_1;
import static kp.Constants.TOPIC_ORIG_2;
import static kp.Constants.TOPIC_SELECT_DEST;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kp.models.Information;
import kp.utils.Utils;

/**
 * The reporter.
 * <p>
 * It checks by comparing the content of two messages - the message taken from
 * the destination topic and the message taken from the origin topic.
 * <p>
 * It displays the latest message found in destination topics.<br>
 * Requirement for the latest message display: the producer, which is sending to
 * the destination topic, should be up and running.<br/>
 * The latest message reader reads <b>the next message published after this
 * reader creation</b>.
 */
public class KpReporter {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());
	private final KpInformationChecker informationChecker = new KpInformationChecker();
	private final KpMessageDisplayer messageDisplayer = new KpMessageDisplayer();
	private final StringBuilder checkRecord = new StringBuilder();

	/**
	 * The constructor.
	 * 
	 */
	public KpReporter() {
		super();
	}

	/**
	 * Processes searching, checking and displaying the given messages.
	 * 
	 */
	public void process() {

		findAndCheck();
		findLatestAndDisplay();
	}

	/**
	 * Finds the given message and check it in origin topic and in destination
	 * topic.
	 * 
	 */
	private void findAndCheck() {

		checkRecord.setLength(0);
		try (final PulsarClient pulsarClient = PulsarClient.builder().serviceUrl(Utils.getServiceUrl()).build();
				final Reader<Information> readerDest = Utils.createReader(pulsarClient, TOPIC_SELECT_DEST);
				final Reader<Information> readerOrig = Utils.createReader(pulsarClient,
						List.of(TOPIC_ORIG_1, TOPIC_ORIG_2), MessageId.earliest)) {

			searchMessages(readerDest, readerOrig);
		} catch (IOException e) {
			logger.error("findAndCheck(): exception[{}]", e.getMessage());
		}
		if (checkRecord.isEmpty()) {
			logger.info("findAndCheck(): check result OK");
		} else {
			if (logger.isInfoEnabled()) {
				logger.info(String.format("findAndCheck(): check FAILED, check record:%n%s", checkRecord));
			}
		}
	}

	/**
	 * Searches for the messages and checks the message from the middle of the
	 * received messages list.
	 * 
	 * @param readerDest the {@link Reader} from destination topic
	 * @param readerOrig the {@link Reader} from origin topics
	 * @throws PulsarClientException the {@link PulsarClientException}
	 */
	private void searchMessages(Reader<Information> readerDest, Reader<Information> readerOrig)
			throws PulsarClientException {

		final Optional<Information> informationDestOpt = findInDestination(readerDest);
		if (informationDestOpt.isEmpty()) {
			final String msg = "information not found in the destination topic";
			checkRecord.append(msg).append(System.lineSeparator());
			logger.warn("searchMessages(): {}", msg);
			return;
		}
		final long id = informationDestOpt.get().getId();
		final String label = informationDestOpt.get().getLabel();
		final Optional<Information> informationOrigOpt = findInOrigin(readerOrig, id, label);
		if (informationOrigOpt.isEmpty()) {
			final String msg = String.format("information not found in the origin topics, id[%d], label[%s]", id,
					label);
			checkRecord.append(msg).append(System.lineSeparator());
			logger.warn("searchMessages(): {}", msg);
			return;
		}
		informationChecker.checkInformation(checkRecord, informationDestOpt.get(), informationOrigOpt.get());
	}

	/**
	 * Finds the given information in the destination topic.
	 * 
	 * @param reader the {@link Reader} of the messages from destination topic
	 * @return the {@link Optional} with the {@link Information}
	 * @throws PulsarClientException the {@link PulsarClientException}
	 */
	private Optional<Information> findInDestination(Reader<Information> reader) throws PulsarClientException {

		final List<MessageId> messageIdList = new ArrayList<>();
		Message<Information> message;
		while ((message = reader.readNext(READER_TIMEOUT, TimeUnit.MILLISECONDS)) != null) {
			messageIdList.add(message.getMessageId());
		}
		if (messageIdList.isEmpty()) {
			return Optional.empty();
		}
		reader.seek(messageIdList.get(messageIdList.size() / 2));
		Utils.sleepMillis(2 * MILLIS_TO_SLEEP);
		if (!reader.hasMessageAvailable()) {
			return Optional.empty();
		}
		return Optional.ofNullable(reader.readNext(READER_TIMEOUT, TimeUnit.MILLISECONDS))//
				.map(msg -> {
					logger.info(String.format("findInDestination():%n"//
							+ "\ttopic[%s],%n\tkey[%s], sequenceId[%s], messageId[%s], information id[%d], label[%s]", //
							msg.getTopicName(), msg.getKey(), msg.getSequenceId(), msg.getMessageId(),
							msg.getValue().getId(), msg.getValue().getLabel()));
					return msg.getValue();
				});
	}

	/**
	 * Finds the given information in the origin topics.
	 * 
	 * @param reader the {@link Reader} of the messages from origin topic
	 * @param id     the {@link Information} id
	 * @param label  the {@link Information} label
	 * @return the {@link Optional} with the {@link Information}
	 * @throws PulsarClientException the {@link PulsarClientException}
	 */
	private Optional<Information> findInOrigin(Reader<Information> reader, long id, String label)
			throws PulsarClientException {

		Message<Information> message;
		while ((message = reader.readNext(READER_TIMEOUT, TimeUnit.MILLISECONDS)) != null) {
			if (id == message.getValue().getId() && label.equals(message.getValue().getLabel())) {
				if (logger.isInfoEnabled()) {
					logger.info(String.format("findInOrigin():%n"//
							+ "\ttopic[%s],%n\tkey[%s], sequenceId[%s], messageId[%s], information id[%d], label[%s]", //
							message.getTopicName(), message.getKey(), message.getSequenceId(), message.getMessageId(),
							message.getValue().getId(), message.getValue().getLabel()));
				}
				return Optional.ofNullable(message.getValue());
			}
		}
		return Optional.empty();
	}

	/**
	 * Finds the latest message in the destination topic and displays it.
	 * 
	 */
	private void findLatestAndDisplay() {

		try (final PulsarClient pulsarClient = PulsarClient.builder().serviceUrl(Utils.getServiceUrl()).build();
				final Reader<Information> readerDest = Utils.createReader(pulsarClient,
						List.of(TOPIC_DEST_1, TOPIC_DEST_2), MessageId.latest)) {
			int retries = 0;
			while (retries < READ_LATEST_MAX_RETRIES) {
				if (readerDest.hasMessageAvailable()) {
					final Message<Information> message = readerDest.readNext(READER_TIMEOUT, TimeUnit.MILLISECONDS);
					if (Objects.nonNull(message)) {
						messageDisplayer.displayMessage(message);
					} else {
						logger.warn("findLatestAndDisplay(): message not found in the destination topics");
					}
					break;
				}
				retries++;
				logger.info("findLatestAndDisplay(): message not available yet");
				Utils.sleepSeconds();
			}
		} catch (IOException e) {
			logger.error("findLatestAndDisplay(): exception[{}]", e.getMessage());
		}
	}

}
