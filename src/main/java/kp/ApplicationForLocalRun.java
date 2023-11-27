package kp;

import static kp.Constants.TOPIC_DEST_1;
import static kp.Constants.TOPIC_DEST_2;
import static kp.Constants.TOPIC_ORIG_1;
import static kp.Constants.TOPIC_ORIG_2;
import static kp.Constants.TOPIC_SELECT_DEST;
import static kp.Constants.TOPIC_SELECT_ORIG;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kp.destinations.KpDestination;
import kp.originators.KpOriginator;
import kp.reporters.KpReporter;
import kp.selectors.KpSelector;
import kp.utils.Marker;
import kp.utils.Utils;

/**
 * This application should be executed not in Docker, but only locally.
 * 
 */
public class ApplicationForLocalRun {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

	/**
	 * The hidden constructor.
	 */
	private ApplicationForLocalRun() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * The entry point for the application.
	 * 
	 * @param args the command-line arguments
	 */
	public static void main(String[] args) {

		Utils.setRunLocally(true);
		Utils.setServiceUrl("pulsar://localhost:6650");
		final String selection = Optional.ofNullable(args)//
				.filter(arg -> arg.length > 0).map(arg -> arg[0]).orElse("");

		if ("reporter".equals(selection)) {
			new Thread(() -> new KpReporter().process()).start();
		} else {
			new Thread(() -> new KpDestination().process(TOPIC_DEST_1)).start();
			new Thread(() -> new KpDestination().process(TOPIC_DEST_2)).start();
			new Thread(() -> new KpSelector().process(TOPIC_SELECT_ORIG, TOPIC_ORIG_1, TOPIC_ORIG_2)).start();
			new Thread(() -> new KpSelector().process(TOPIC_SELECT_DEST, TOPIC_DEST_1, TOPIC_DEST_2)).start();
			new Thread(() -> new KpOriginator().process(TOPIC_ORIG_1, Marker.ODD)).start();
			new Thread(() -> new KpOriginator().process(TOPIC_ORIG_2, Marker.EVEN)).start();
		}
		logger.info("main():");
	}
}
