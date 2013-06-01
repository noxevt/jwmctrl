package cn.com.jwmctrl;

import gnu.getopt.Getopt;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

public final class GetOptUtils {
	private static final String MESSAGE_CHARSET = "message.charset";

	public static void fixI18nBug(final Getopt getopt) {
		try {
			// When posixly_correct is true, Locale.US is used to for
			// ResourceBundle
			if (!(Boolean) FieldUtils.readDeclaredField(getopt,
					"posixly_correct", true)) {
				// Get actual locale used for ResourceBundle
				final Locale defaultLocale = Locale.getDefault();
				String locale = null;
				if (Locale.SIMPLIFIED_CHINESE.equals(defaultLocale)
						|| Locale.CHINESE.equals(defaultLocale)) {
					locale = "chs";
				} else if (Locale.TRADITIONAL_CHINESE.equals(defaultLocale)) {
					locale = "cht";
				}

				if (locale != null) {
					final ResourceBundle resourceBundle = new ResourceBundleWrapper(
							ResourceBundle.getBundle(String.format(
									"gnu/getopt/MessagesBundle_%s", locale)),
							locale);
					FieldUtils.writeDeclaredField(getopt, "_messages",
							resourceBundle, true);
				}
			}
		} catch (Exception e) {
			// Ignore exception
		}
	}

	private static class ResourceBundleWrapper extends ResourceBundle {
		private final ResourceBundle resourceBundle;
		private final Method handleGetObjectMethod;
		private final String locale;

		public ResourceBundleWrapper(final ResourceBundle resourceBundle,
				final String locale) {
			this.resourceBundle = resourceBundle;
			handleGetObjectMethod = MethodUtils.getAccessibleMethod(
					resourceBundle.getClass(), "handleGetObject", String.class);
			this.locale = locale;
		}

		@Override
		public Enumeration<String> getKeys() {
			return resourceBundle.getKeys();
		}

		@Override
		protected Object handleGetObject(final String key) {
			Object value = null;
			try {
				value = handleGetObjectMethod.invoke(resourceBundle, key);
				if (value instanceof String) {
					Charset charset = Charset.defaultCharset();

					final String messageCharset = System
							.getProperty(MESSAGE_CHARSET);
					if ((messageCharset != null)
							&& Charset.isSupported(messageCharset)) {
						// Get message resource bundle's charset from command
						// line argument
						charset = Charset.forName(messageCharset);
					} else if (("chs".equals(locale) || "cht".equals(locale))
							&& Charset.isSupported("GBK")) {
						charset = Charset.forName("GBK");
					}

					value = new String(((String) value).getBytes("ISO-8859-1"),
							charset);
				}
			} catch (Exception e) {
				// Ignore exception
			}
			return value;
		}
	}
}
