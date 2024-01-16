package kp;

/**
 * The constants.
 *
 */
public final class Constants {
	/**
	 * The message key..
	 */
	public static final String KP_KEY = "kp-key";
	/**
	 * The topic subscription.
	 */
	public static final String KP_SUBSCRIPTION = "kp-subscription";
	/**
	 * The type of the topic and the topic tenant and the topic namespace.
	 */
	private static final String TYPE_TENANT_NAMESPACE = "persistent://kp-tenant/kp-namespace/";
	/**
	 * The 1st originator topic.
	 */
	public static final String TOPIC_ORIG_1 = TYPE_TENANT_NAMESPACE + "orig-1";
	/**
	 * The 2nd originator topic.
	 */
	public static final String TOPIC_ORIG_2 = TYPE_TENANT_NAMESPACE + "orig-2";
	/**
	 * The destination selector topic.
	 */
	public static final String TOPIC_SELECT_DEST = TYPE_TENANT_NAMESPACE + "select-dest";
	/**
	 * The originator selector topic.
	 */
	public static final String TOPIC_SELECT_ORIG = TYPE_TENANT_NAMESPACE + "select-orig";
	/**
	 * The 1st destination topic.
	 */
	public static final String TOPIC_DEST_1 = TYPE_TENANT_NAMESPACE + "dest-1";
	/**
	 * The 2nd destination topic.
	 */
	public static final String TOPIC_DEST_2 = TYPE_TENANT_NAMESPACE + "dest-2";
	/**
	 * The number of seconds to sleep.
	 */
	public static final long SECONDS_TO_SLEEP = 5;
	/**
	 * The number of milliseconds to sleep.
	 */
	public static final long MILLIS_TO_SLEEP = 100;
	/**
	 * The reader timeout in milliseconds.
	 */
	public static final int READER_TIMEOUT = 100;
	/**
	 * The maximal number of the retries for the latest message reading.
	 */
	public static final int READ_LATEST_MAX_RETRIES = 3;
	/**
	 * The thin line.
	 */
	public static final String THIN_LINE = "-".repeat(50);

	/**
	 * The hidden constructor.
	 */
	private Constants() {
		throw new IllegalStateException("Utility class");
	}
}