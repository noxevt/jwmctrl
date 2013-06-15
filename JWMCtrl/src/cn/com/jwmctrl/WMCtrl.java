package cn.com.jwmctrl;

import gnu.getopt.Getopt;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.unix.X11.Atom;
import com.sun.jna.platform.unix.X11.AtomByReference;
import com.sun.jna.platform.unix.X11.Display;
import com.sun.jna.platform.unix.X11.Window;
import com.sun.jna.platform.unix.X11.WindowByReference;
import com.sun.jna.platform.unix.X11.XClientMessageEvent;
import com.sun.jna.platform.unix.X11.XEvent;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

public class WMCtrl {
	private static final X11 x11;
	private static X11Ext x11Ext = null;
	private static Xmu xmu = null;

	private static final String PROG_NAME = "jwmctrl";
	private static final String VERSION = "1.0-beta2";
	private static final String HELP = "jwmctrl "
			+ VERSION
			+ "\n"
			+ "Usage: "
			+ PROG_NAME
			+ " [OPTION]...\n"
			+ "Actions:\n"
			+ "  -m                   Show information about the window manager and\n"
			+ "                       about the environment.\n"
			+ "  -l                   List windows managed by the window manager.\n"
			+ "  -d                   List desktops. The current desktop is marked\n"
			+ "                       with an asterisk.\n"
			+ "  -s <DESK>            Switch to the specified desktop.\n"
			+ "  -a <WIN>             Activate the window by switching to its desktop and\n"
			+ "                       raising it.\n"
			+ "  -c <WIN>             Close the window gracefully.\n"
			+ "  -R <WIN>             Move the window to the current desktop and\n"
			+ "                       activate it.\n"
			+ "  -r <WIN> -t <DESK>   Move the window to the specified desktop.\n"
			+ "  -r <WIN> -e <MVARG>  Resize and move the window around the desktop.\n"
			+ "                       The format of the <MVARG> argument is described below.\n"
			+ "  -r <WIN> -b <STARG>  Change the state of the window. Using this option it's\n"
			+ "                       possible for example to make the window maximized,\n"
			+ "                       minimized or fullscreen. The format of the <STARG>\n"
			+ "                       argument and list of possible states is given below.\n"
			+ "  -r <WIN> -N <STR>    Set the name (long title) of the window.\n"
			+ "  -r <WIN> -I <STR>    Set the icon name (short title) of the window.\n"
			+ "  -r <WIN> -T <STR>    Set both the name and the icon name of the window.\n"
			+ "  -k (on|off)          Activate or deactivate window manager's\n"
			+ "                       \"showing the desktop\" mode. Many window managers\n"
			+ "                       do not implement this mode.\n"
			+ "  -o <X>,<Y>           Change the viewport for the current desktop.\n"
			+ "                       The X and Y values are separated with a comma.\n"
			+ "                       They define the top left corner of the viewport.\n"
			+ "                       The window manager may ignore the request.\n"
			+ "  -n <NUM>             Change number of desktops.\n"
			+ "                       The window manager may ignore the request.\n"
			+ "  -g <W>,<H>           Change geometry (common size) of all desktops.\n"
			+ "                       The window manager may ignore the request.\n"
			+ "  -h                   Print help.\n"
			+ "\n"
			+ "Options:\n"
			+ "  -i                   Interpret <WIN> as a numerical window ID.\n"
			+ "  -p                   Include PIDs in the window list. Very few\n"
			+ "                       X applications support this feature.\n"
			+ "  -G                   Include geometry in the window list.\n"
			+ "  -x                   Include WM_CLASS in the window list or\n"
			+ "                       interpret <WIN> as the WM_CLASS name.\n"
			+ "  -u                   Override auto-detection and force UTF-8 mode.\n"
			+ "  -F                   Modifies the behavior of the window title matching\n"
			+ "                       algorithm. It will match only the full window title\n"
			+ "                       instead of a substring, when this option is used.\n"
			+ "                       Furthermore it makes the matching case sensitive.\n"
			+ "  -v                   Be verbose. Useful for debugging.\n"
			+ "  -w <WA>              Use a workaround. The option may appear multiple\n"
			+ "                       times. List of available workarounds is given below.\n"
			+ "\n"
			+ "Arguments:\n"
			+ "  <WIN>                This argument specifies the window. By default it's\n"
			+ "                       interpreted as a string. The string is matched\n"
			+ "                       against the window titles and the first matching\n"
			+ "                       window is used. The matching isn't case sensitive\n"
			+ "                       and the string may appear in any position\n"
			+ "                       of the title.\n"
			+ "\n"
			+ "                       The -i option may be used to interpret the argument\n"
			+ "                       as a numerical window ID represented as a decimal\n"
			+ "                       number. If it starts with \"0x\", then\n"
			+ "                       it will be interpreted as a hexadecimal number.\n"
			+ "\n"
			+ "                       The -x option may be used to interpret the argument\n"
			+ "                       as a string, which is matched against the window's\n"
			+ "                       class name (WM_CLASS property). Th first matching\n"
			+ "                       window is used. The matching isn't case sensitive\n"
			+ "                       and the string may appear in any position\n"
			+ "                       of the class name. So it's recommended to  always use\n"
			+ "                       the -F option in conjunction with the -x option.\n"
			+ "\n"
			+ "                       The special string \":SELECT:\" (without the quotes)\n"
			+ "                       may be used to instruct wmctrl to let you select the\n"
			+ "                       window by clicking on it.\n"
			+ "\n"
			+ "                       The special string \":ACTIVE:\" (without the quotes)\n"
			+ "                       may be used to instruct wmctrl to use the currently\n"
			+ "                       active window for the action.\n"
			+ "\n"
			+ "  <DESK>               A desktop number. Desktops are counted from zero.\n"
			+ "\n"
			+ "  <MVARG>              Specifies a change to the position and size\n"
			+ "                       of the window. The format of the argument is:\n"
			+ "\n"
			+ "                       <G>,<X>,<Y>,<W>,<H>\n"
			+ "\n"
			+ "                       <G>: Gravity specified as a number. The numbers are\n"
			+ "                          defined in the EWMH specification. The value of\n"
			+ "                          zero is particularly useful, it means \"use the\n"
			+ "                          default gravity of the window\".\n"
			+ "                       <X>,<Y>: Coordinates of new position of the window.\n"
			+ "                       <W>,<H>: New width and height of the window.\n"
			+ "\n"
			+ "                       The value of -1 may appear in place of\n"
			+ "                       any of the <X>, <Y>, <W> and <H> properties\n"
			+ "                       to left the property unchanged.\n"
			+ "\n"
			+ "  <STARG>              Specifies a change to the state of the window\n"
			+ "                       by the means of _NET_WM_STATE request.\n"
			+ "                       This option allows two properties to be changed\n"
			+ "                       simultaneously, specifically to allow both\n"
			+ "                       horizontal and vertical maximization to be\n"
			+ "                       altered together.\n"
			+ "\n"
			+ "                       The format of the argument is:\n"
			+ "\n"
			+ "                       (remove|add|toggle),<PROP1>[,<PROP2>]\n"
			+ "\n"
			+ "                       The EWMH specification defines the\n"
			+ "                       following properties:\n"
			+ "\n"
			+ "                           modal, sticky, maximized_vert, maximized_horz,\n"
			+ "                           shaded, skip_taskbar, skip_pager, hidden,\n"
			+ "                           fullscreen, above, below\n"
			+ "\n"
			+ "Workarounds:\n"
			+ "\n"
			+ "  DESKTOP_TITLES_INVALID_UTF8      Print non-ASCII desktop titles correctly\n"
			+ "                                   when using Window Maker.\n"
			+ "\n"
			+ "The format of the window list:\n"
			+ "\n"
			+ "  <window ID> <desktop ID> <client machine> <window title>\n"
			+ "\n"
			+ "The format of the desktop list:\n"
			+ "\n"
			+ "  <desktop ID> [-*] <geometry> <viewport> <workarea> <title>\n"
			+ "\n"
			+ "\n"
			+ "Author, current maintainer: zhengbo.wang <zhengbowang1984@gmail.com>\n"
			+ "Released under the GNU General Public License.\n"
			+ "Copyright (C) 2013\n";

	/** C-style boolean "true" */
	private static final int TRUE = 1;
	/** C-style boolean "false" */
	private static final int FALSE = 0;

	private static final boolean EXIT_SUCCESS = true;
	private static final boolean EXIT_FAILURE = false;

	private static final int MAX_PROPERTY_VALUE_LEN = 4096;
	private static final String SELECT_WINDOW_MAGIC = ":SELECT:";
	private static final String ACTIVE_WINDOW_MAGIC = ":ACTIVE:";

	private static final int _NET_WM_STATE_REMOVE = 0; /* remove/unset property */
	private static final int _NET_WM_STATE_ADD = 1; /* add/set property */
	private static final int _NET_WM_STATE_TOGGLE = 2; /* toggle property */

	private static final Options options = new Options();

	private static final String MAC_DEFAULT_JNA_LIBRARY_PATH = "/opt/X11/lib";

	static {
		if (Platform.isMac()) {
			// Set jna.library.path to '/opt/X11/lib' for Mac OS X if it's not
			// specified
			if (StringUtils.isBlank(System.getProperty("jna.library.path"))) {
				System.setProperty("jna.library.path",
						MAC_DEFAULT_JNA_LIBRARY_PATH);
			}
		}
		x11 = X11.INSTANCE;
	}

	public static void main(String[] args) {
		int opt = 0;
		char action = 0;
		boolean ret = true;
		boolean missing_option = true;
		Display disp;

		/*
		 * make "--help" and "--version" work. I don't want to use getopt_long
		 * for portability reasons
		 */
		if ((args.length > 0) && StringUtils.isNotEmpty(args[0])) {
			if ("--help".equals(args[0])) {
				printf(HELP);
				System.exit(0);
				return;
			} else if ("--version".equals(args[0])) {
				printf(VERSION);
				System.exit(0);
				return;
			}
		}

		final Getopt getopt = new Getopt(PROG_NAME, args,
				"FGVvhlupidmxa:r:s:c:t:w:k:o:n:g:e:b:N:I:T:R:");
		GetOptUtils.fixI18nBug(getopt);

		while ((opt = getopt.getopt()) != -1) {
			missing_option = false;
			final String optarg = getopt.getOptarg();
			switch (opt) {
			case 'F':
				options.full_window_title_match = true;
				break;
			case 'G':
				options.show_geometry = true;
				break;
			case 'i':
				options.match_by_id = true;
				break;
			case 'v':
				options.verbose = true;
				break;
			case 'u':
				options.force_utf8 = true;
				break;
			case 'x':
				options.match_by_cls = true;
				options.show_class = true;
				break;
			case 'p':
				options.show_pid = true;
				break;
			case 'a':
			case 'c':
			case 'R':
				options.param_window = optarg;
				action = (char) opt;
				break;
			case 'r':
				options.param_window = optarg;
				break;
			case 't':
			case 'e':
			case 'b':
			case 'N':
			case 'I':
			case 'T':
				options.param = optarg;
				action = (char) opt;
				break;
			case 's':
				options.param = optarg;
				action = (char) opt;
				break;
			case 'w':
				if ("DESKTOP_TITLES_INVALID_UTF8".equals(optarg)) {
					options.wa_desktop_titles_invalid_utf8 = true;
				} else {
					p_error("Unknown workaround: %s\n", optarg);
					System.exit(1);
					return;
				}
				break;
			case 'k':
			case 'o':
			case 'n':
			case 'g':
				options.param = optarg;
				action = (char) opt;
				break;
			case '?':
				System.exit(-1);
				return;
			default:
				action = (char) opt;
			}
		}

		if (missing_option) {
			p_error(HELP);
			System.exit(1);
			return;
		}

		init_charset();

		if ((disp = x11.XOpenDisplay(null)) == null) {
			p_error("Cannot open display.\n");
			System.exit(1);
			return;
		}

		switch (action) {
		case 'V':
			printf(VERSION);
			break;
		case 'h':
			printf(HELP);
			break;
		case 'l':
			ret = list_windows(disp);
			break;
		case 'd':
			ret = list_desktops(disp);
			break;
		case 's':
			ret = switch_desktop(disp);
			break;
		case 'm':
			ret = wm_info(disp);
			break;
		case 'a':
		case 'c':
		case 'R':
		case 't':
		case 'e':
		case 'b':
		case 'N':
		case 'I':
		case 'T':
			if (StringUtils.isEmpty(options.param_window)) {
				p_error("No window was specified.\n");
				System.exit(1);
				return;
			}
			if (options.match_by_id) {
				ret = action_window_pid(disp, action);
			} else {
				ret = action_window_str(disp, action);
			}
			break;
		case 'k':
			ret = showing_desktop(disp);
			break;
		case 'o':
			ret = change_viewport(disp);
			break;
		case 'n':
			ret = change_number_of_desktops(disp);
			break;
		case 'g':
			ret = change_geometry(disp);
			break;
		}

		x11.XCloseDisplay(disp);
		System.exit(ret ? 0 : 1);
	}

	private static void init_charset() {
		p_verbose("envir_utf8: %d\n", TRUE);
	}

	public static boolean client_msg(final Display disp, final Window win,
			final String msg, final long data0, final long data1,
			final long data2, final long data3, final long data4) {
		final NativeLong mask = new NativeLong(X11.SubstructureRedirectMask
				| X11.SubstructureNotifyMask);

		final XClientMessageEvent xclient = new XClientMessageEvent();
		xclient.type = X11.ClientMessage;
		xclient.serial = new NativeLong(0);
		xclient.send_event = TRUE;
		xclient.message_type = x11.XInternAtom(disp, msg, false);
		xclient.window = win;
		xclient.format = 32;
		xclient.data.setType(NativeLong[].class);
		xclient.data.l[0] = new NativeLong(data0);
		xclient.data.l[1] = new NativeLong(data1);
		xclient.data.l[2] = new NativeLong(data2);
		xclient.data.l[3] = new NativeLong(data3);
		xclient.data.l[4] = new NativeLong(data4);

		final XEvent event = new XEvent();
		event.setTypedValue(xclient);

		if (x11.XSendEvent(disp, x11.XDefaultRootWindow(disp), FALSE, mask,
				event) != FALSE) {
			return EXIT_SUCCESS;
		} else {
			p_error(String.format("Cannot send %s event.\n", msg));
			return EXIT_FAILURE;
		}
	}

	public static boolean wm_info(final Display disp) {
		Window sup_window = null;
		String wm_name = null;
		String wm_class = null;
		Long wm_pid = null;
		Long showing_desktop = null;

		if ((sup_window = get_property_as_window(disp,
				x11.XDefaultRootWindow(disp), X11.XA_WINDOW,
				"_NET_SUPPORTING_WM_CHECK")) == null) {
			if ((sup_window = get_property_as_window(disp,
					x11.XDefaultRootWindow(disp), X11.XA_CARDINAL,
					"_WIN_SUPPORTING_WM_CHECK")) == null) {
				p_error("Cannot get window manager info properties.\n"
						+ "(_NET_SUPPORTING_WM_CHECK or _WIN_SUPPORTING_WM_CHECK)\n");
				return EXIT_FAILURE;
			}
		}

		/* WM_NAME */
		if ((wm_name = get_property_as_utf8_string(disp, sup_window,
				x11.XInternAtom(disp, "UTF8_STRING", false), "_NET_WM_NAME")) == null) {
			if ((wm_name = get_property_as_string(disp, sup_window,
					X11.XA_STRING, "_NET_WM_NAME")) == null) {
				p_verbose("Cannot get name of the window manager (_NET_WM_NAME).\n");
			}
		}

		/* WM_CLASS */
		if ((wm_class = get_property_as_utf8_string(disp, sup_window,
				x11.XInternAtom(disp, "UTF8_STRING", false), "WM_CLASS")) == null) {
			if ((wm_class = get_property_as_string(disp, sup_window,
					X11.XA_STRING, "WM_CLASS")) == null) {
				p_verbose("Cannot get class of the window manager (WM_CLASS).\n");
			}
		}

		/* WM_PID */
		if ((wm_pid = get_property_as_long(disp, sup_window, X11.XA_CARDINAL,
				"_NET_WM_PID")) == null) {
			p_verbose("Cannot get pid of the window manager (_NET_WM_PID).\n");
		}

		/* _NET_SHOWING_DESKTOP */
		if ((showing_desktop = get_property_as_long(disp,
				x11.XDefaultRootWindow(disp), X11.XA_CARDINAL,
				"_NET_SHOWING_DESKTOP")) == null) {
			p_verbose("Cannot get the _NET_SHOWING_DESKTOP property.\n");
		}

		/* print out the info */
		p_info("Name: %s\n", defaultString(wm_name, "N/A"));
		p_info("Class: %s\n", defaultString(wm_class, "N/A"));

		if (wm_pid != null) {
			p_info("PID: %d\n", wm_pid.longValue());
		} else {
			p_info("PID: N/A\n");
		}

		if (showing_desktop != null) {
			p_info("Window manager's \"showing the desktop\" mode: %s\n",
					(showing_desktop == TRUE) ? "ON" : "OFF");
		} else {
			p_info("Window manager's \"showing the desktop\" mode: N/A\n");
		}

		return EXIT_SUCCESS;
	}

	public static boolean showing_desktop(final Display disp) {
		boolean on;

		if ("on".equals(options.param)) {
			on = true;
		} else if ("off".equals(options.param)) {
			on = false;
		} else {
			p_error("The argument to the -k option must be either \"on\" or \"off\"\n");
			return EXIT_FAILURE;
		}

		return showing_desktop(disp, on);
	}

	public static boolean showing_desktop(final Display disp, final boolean on) {
		return client_msg(disp, x11.XDefaultRootWindow(disp),
				"_NET_SHOWING_DESKTOP", on ? 1 : 0, 0, 0, 0, 0);
	}

	public static boolean change_viewport(final Display disp) {
		final String argerr = "The -o option expects two integers separated with a comma.\n";

		final List<Long> list = sscanf(options.param);
		if (list.size() == 2) {
			return change_viewport(disp, list.get(0), list.get(1));
		} else {
			p_error(argerr);
			return EXIT_FAILURE;
		}
	}

	public static boolean change_viewport(final Display disp, final long x,
			final long y) {
		return client_msg(disp, x11.XDefaultRootWindow(disp),
				"_NET_DESKTOP_VIEWPORT", x, y, 0, 0, 0);
	}

	public static boolean change_geometry(final Display disp) {
		final String argerr = "The -g option expects two integers separated with a comma.\n";

		final List<Long> list = sscanf(options.param);
		if (list.size() == 2) {
			return change_geometry(disp, list.get(0), list.get(1));
		} else {
			p_error(argerr);
			return EXIT_FAILURE;
		}
	}

	public static boolean change_geometry(final Display disp, final long x,
			final long y) {
		return client_msg(disp, x11.XDefaultRootWindow(disp),
				"_NET_DESKTOP_GEOMETRY", x, y, 0, 0, 0);
	}

	public static boolean change_number_of_desktops(final Display disp) {
		return change_number_of_desktops(disp, defaultInt(options.param, null));
	}

	public static boolean change_number_of_desktops(final Display disp,
			final Integer number) {
		if (number == null) {
			p_error("The -n option expects an integer.\n");
			return EXIT_FAILURE;
		}
		return client_msg(disp, x11.XDefaultRootWindow(disp),
				"_NET_NUMBER_OF_DESKTOPS", number, 0, 0, 0, 0);
	}

	private static boolean switch_desktop(final Display disp) {
		return switch_desktop(disp, defaultInt(options.param, -1));
	}

	public static boolean switch_desktop(final Display disp, final long target) {
		if (target < 0) {
			p_error("Invalid desktop ID.\n");
			return EXIT_FAILURE;
		}
		if (client_msg(disp, x11.XDefaultRootWindow(disp),
				"_NET_CURRENT_DESKTOP", target, 0, 0, 0, 0)) {
			x11.XFlush(disp);
			return true;
		}
		return false;
	}

	public static List<Window> get_client_list(final Display disp) {
		final NativeLongByReference size = new NativeLongByReference();

		Pointer clientList = get_property(disp, x11.XDefaultRootWindow(disp),
				X11.XA_WINDOW, "_NET_CLIENT_LIST", size);
		if (clientList == null) {
			clientList = get_property(disp, x11.XDefaultRootWindow(disp),
					X11.XA_CARDINAL, "_WIN_CLIENT_LIST", size);
			if (clientList == null) {
				p_error("Cannot get client list properties. \n"
						+ "(_NET_CLIENT_LIST or _WIN_CLIENT_LIST)\n");
				return null;
			}
		}

		final List<Window> client_list = new ArrayList<Window>();
		final int SIZE_OF_WINDOW = 4;
		for (int i = 0, count = (int) size.getValue().longValue()
				/ SIZE_OF_WINDOW; i < count; i++) {
			client_list.add(new Window(Pointer.nativeValue(clientList
					.getPointer(i * Window.SIZE))));

		}

		return client_list;
	}

	public static void window_set_title(final Display disp, final Window win,
			final String title, final WindowSetTitleMode mode) {
		window_set_title(disp, win, title, mode.getMode());
	}

	public static void window_set_title(final Display disp, final Window win,
			final String title, final char mode) {
		Pointer data = null;
		int dataLength = 0;

		if ((title != null) && (mode == 'T' || mode == 'N' || mode == 'I')) {
			dataLength = title.getBytes().length;
			data = new Memory(dataLength + 1);
			data.setString(0, title);
		}

		if (mode == 'T' || mode == 'N') {
			/* set name */
			if (title != null) {
				if (x11.XChangeProperty(disp, win,
						x11.XInternAtom(disp, "_NET_WM_NAME", false),
						x11.XInternAtom(disp, "UTF8_STRING", false), 8,
						X11.PropModeReplace, data, dataLength) != X11.Success) {
					x11.XChangeProperty(disp, win, X11.XA_WM_NAME,
							X11.XA_STRING, 8, X11.PropModeReplace, data,
							dataLength);
				}
			} else {
				if (x11.XDeleteProperty(disp, win,
						x11.XInternAtom(disp, "_NET_WM_NAME", false)) != X11.Success) {
					x11.XDeleteProperty(disp, win, X11.XA_WM_NAME);
				}
			}
		}

		if (mode == 'T' || mode == 'I') {
			/* set icon name */
			if (title != null) {
				if (x11.XChangeProperty(disp, win,
						x11.XInternAtom(disp, "_NET_WM_ICON_NAME", false),
						x11.XInternAtom(disp, "UTF8_STRING", false), 8,
						X11.PropModeReplace, data, dataLength) != X11.Success) {
					x11.XChangeProperty(disp, win, X11.XA_WM_ICON_NAME,
							X11.XA_STRING, 8, X11.PropModeReplace, data,
							dataLength);
				}
			} else {
				if (x11.XDeleteProperty(disp, win,
						x11.XInternAtom(disp, "_NET_WM_ICON_NAME", false)) != X11.Success) {
					x11.XDeleteProperty(disp, win, X11.XA_WM_ICON_NAME);
				}
			}
		}
	}

	public static boolean window_to_desktop(final Display disp, final Window win) {
		return window_to_desktop(disp, win, -1);
	}

	public static boolean window_to_desktop(final Display disp,
			final Window win, int desktop) {
		if (desktop < 0) {
			final int cur_desktop = get_current_desktop(disp);
			if (cur_desktop < 0) {
				p_error("Cannot get current desktop properties. "
						+ "(_NET_CURRENT_DESKTOP or _WIN_WORKSPACE property)\n");
				return EXIT_FAILURE;
			}
			desktop = cur_desktop;
		}

		return client_msg(disp, win, "_NET_WM_DESKTOP", desktop, 0, 0, 0, 0);
	}

	public static int get_current_desktop(final Display disp) {
		final Window root = x11.XDefaultRootWindow(disp);
		Integer cur_desktop = null;
		if ((cur_desktop = get_property_as_int(disp, root, X11.XA_CARDINAL,
				"_NET_CURRENT_DESKTOP")) == null) {
			if ((cur_desktop = get_property_as_int(disp, root, X11.XA_CARDINAL,
					"_WIN_WORKSPACE")) == null) {
				p_error("Cannot get current desktop properties. "
						+ "(_NET_CURRENT_DESKTOP or _WIN_WORKSPACE property)\n");
			}
		}

		return (cur_desktop == null) ? -1 : cur_desktop;
	}

	public static boolean activate_window(final Display disp, final Window win,
			final boolean switch_desktop) {
		Integer desktop = null;

		/* desktop ID */
		if ((desktop = get_property_as_int(disp, win, X11.XA_CARDINAL,
				"_NET_WM_DESKTOP")) == null) {
			if ((desktop = get_property_as_int(disp, win, X11.XA_CARDINAL,
					"_WIN_WORKSPACE")) == null) {
				p_verbose("Cannot find desktop ID of the window.\n");
			}
		}

		if (switch_desktop && (desktop != null)) {
			if (!client_msg(disp, x11.XDefaultRootWindow(disp),
					"_NET_CURRENT_DESKTOP", desktop, 0, 0, 0, 0)) {
				p_verbose("Cannot switch desktop.\n");
			}
		}

		client_msg(disp, win, "_NET_ACTIVE_WINDOW", 0, 0, 0, 0, 0);
		x11.XMapRaised(disp, win);

		return EXIT_SUCCESS;
	}

	public static boolean close_window(final Display disp, final Window win) {
		return client_msg(disp, win, "_NET_CLOSE_WINDOW", 0, 0, 0, 0, 0);
	}

	public static boolean window_state(final Display disp, final Window win,
			final String arg) {
		int action = 0;
		Atom prop2 = null;
		final String argerr = "The -b option expects a list of comma separated parameters: \"(remove|add|toggle),<PROP1>[,<PROP2>]\"\n";

		if (StringUtils.isEmpty(arg)) {
			p_error(argerr);
			return EXIT_FAILURE;
		}

		final String[] parameters = StringUtils
				.splitPreserveAllTokens(arg, ',');
		if ((parameters.length) == 2 || (parameters.length == 3)) {
			/* action */
			if ("remove".equals(parameters[0])) {
				action = _NET_WM_STATE_REMOVE;
			} else if ("add".equals(parameters[0])) {
				action = _NET_WM_STATE_ADD;
			} else if ("toggle".equals(parameters[0])) {
				action = _NET_WM_STATE_TOGGLE;
			} else {
				p_error("Invalid action. Use either remove, add or toggle.\n");
				return EXIT_FAILURE;
			}

			/* the second property */
			if (parameters.length > 2) {
				if (StringUtils.isBlank(parameters[2])) {
					p_error("Invalid zero length property.\n");
					return EXIT_FAILURE;
				}
				final String tmp_prop2 = String.format("_NET_WM_STATE_%s",
						parameters[2]);
				p_verbose("State 2: %s\n", tmp_prop2);
				prop2 = x11.XInternAtom(disp, tmp_prop2, false);
			}

			/* the first property */
			if (StringUtils.isBlank(parameters[1])) {
				p_error("Invalid zero length property.\n");
				return EXIT_FAILURE;
			}
			final String tmp_prop1 = String.format("_NET_WM_STATE_%s",
					parameters[1].toUpperCase());
			p_verbose("State 1: %s\n", tmp_prop1);
			final Atom prop1 = x11.XInternAtom(disp, tmp_prop1, false);

			return client_msg(disp, win, "_NET_WM_STATE", action,
					prop1.longValue(), (prop2 == null) ? 0 : prop2.longValue(),
					0, 0);
		} else {
			p_error(argerr);
			return EXIT_FAILURE;
		}
	}

	public static boolean wm_supports(final Display disp, final String prop) {
		if (StringUtils.isBlank(prop)) {
			return false;
		}

		final Atom xa_prop = x11.XInternAtom(disp, prop, false);
		Pointer list = null;
		final NativeLongByReference size = new NativeLongByReference();

		if ((list = get_property(disp, x11.XDefaultRootWindow(disp),
				X11.XA_ATOM, "_NET_SUPPORTED", size)) == null) {
			p_verbose("Cannot get _NET_SUPPORTED property.\n");
			return false;
		}

		for (int i = 0, count = (int) (size.getValue().longValue() / Atom.SIZE); i < count; i++) {
			if (new Atom(Pointer.nativeValue(list.getPointer(i * Atom.SIZE)))
					.equals(xa_prop)) {
				g_free(list);
				return true;
			}
		}

		g_free(list);
		return false;
	}

	public static boolean window_move_resize(final Display disp,
			final Window win, final String arg) {
		final String argerr = "The -e option expects a list of comma separated integers: \"gravity,X,Y,width,height\"\n";

		if (StringUtils.isEmpty(arg)) {
			p_error(argerr);
			return EXIT_FAILURE;
		}

		final List<Long> list = sscanf(arg);
		if (list.size() != 5) {
			p_error(argerr);
			return EXIT_FAILURE;
		}

		final long grav = list.get(0);
		if (grav < 0) {
			p_error("Value of gravity mustn't be negative. Use zero to use the default gravity of the window.\n");
			return EXIT_FAILURE;
		}

		final long x = list.get(1);
		final long y = list.get(2);
		final long w = list.get(3);
		final long h = list.get(4);
		long grflags = grav;
		if (x != -1) {
			grflags |= (1 << 8);
		}
		if (y != -1) {
			grflags |= (1 << 9);
		}
		if (w != -1) {
			grflags |= (1 << 10);
		}
		if (h != -1) {
			grflags |= (1 << 11);
		}

		p_verbose("grflags: %d\n", grflags);

		if (wm_supports(disp, "_NET_MOVERESIZE_WINDOW")) {
			return client_msg(disp, win, "_NET_MOVERESIZE_WINDOW", grflags, x,
					y, w, h);
		} else {
			p_verbose("WM doesn't support _NET_MOVERESIZE_WINDOW. Gravity will be ignored.\n");
			if ((w < 1 || h < 1) && (x >= 0 && y >= 0)) {
				getX11Ext().XMoveWindow(disp, win, x, y);
			} else if ((x < 0 || y < 0) && (w >= 1 && h >= -1)) {
				getX11Ext().XResizeWindow(disp, win, w, h);
			} else if (x >= 0 && y >= 0 && w >= 1 && h >= 1) {
				getX11Ext().XMoveResizeWindow(disp, win, x, y, w, h);
			}
			return EXIT_SUCCESS;
		}
	}

	public static boolean action_window(final Display disp, final Window win,
			final char mode) {
		p_verbose("Using window: 0x%08x\n", win.longValue());
		switch (mode) {
		case 'a':
			return activate_window(disp, win, true);
		case 'c':
			return close_window(disp, win);
		case 'e':
			/* resize/move the window around the desktop => -r -e */
			return window_move_resize(disp, win, options.param);
		case 'b':
			/* change state of a window => -r -b */
			return window_state(disp, win, options.param);
		case 't':
			/* move the window to the specified desktop => -r -t */
			return window_to_desktop(disp, win, Integer.parseInt(options.param));
		case 'R':
			/* move the window to the current desktop and activate it => -r */
			if (window_to_desktop(disp, win, -1) == EXIT_SUCCESS) {
				sleep(100); /*
							 * 100 ms - make sure the WM has enough time to move
							 * the window, before we activate it
							 */
				return activate_window(disp, win, false);
			} else {
				return EXIT_FAILURE;
			}
		case 'N':
		case 'I':
		case 'T':
			window_set_title(disp, win, options.param, mode);
			return EXIT_SUCCESS;
		default:
			p_error("Unknown action: '%c'\n", mode);
			return EXIT_FAILURE;
		}
	}

	public static boolean action_window_pid(final Display disp, final char mode) {
		final Long wid = defaultLong(options.param_window, null);
		if (wid == null) {
			p_error("Cannot convert argument to number.\n");
			return EXIT_FAILURE;
		}

		return action_window_pid(disp, wid, mode);
	}

	public static boolean action_window_pid(final Display disp, final long wid,
			final char mode) {
		return action_window(disp, new Window(wid), mode);
	}

	public static boolean action_window_str(final Display disp, final char mode) {
		Window activate = null;
		List<Window> client_list;

		if (SELECT_WINDOW_MAGIC.equals(options.param_window)) {
			activate = Select_Window(disp);
			if (activate != null) {
				return action_window(disp, activate, mode);
			} else {
				return EXIT_FAILURE;
			}
		}
		if (ACTIVE_WINDOW_MAGIC.equals(options.param_window)) {
			activate = get_active_window(disp);
			if (activate != null) {
				return action_window(disp, activate, mode);
			} else {
				return EXIT_FAILURE;
			}
		} else {
			if ((client_list = get_client_list(disp)) == null) {
				return EXIT_FAILURE;
			}

			for (int i = 0; i < client_list.size(); i++) {
				String match_utf8 = null;
				if (options.show_class) {
					match_utf8 = get_window_class(disp, client_list.get(i)); /* UTF8 */
				} else {
					match_utf8 = get_window_title(disp, client_list.get(i)); /* UTF8 */
				}
				if (match_utf8 != null) {
					if ((options.full_window_title_match && match_utf8
							.equals(options.param_window))
							|| (!options.full_window_title_match && match_utf8
									.contains(options.param_window))) {
						activate = client_list.get(i);
						break;
					}
				}
			}

			if (activate != null) {
				return action_window(disp, activate, mode);
			} else {
				return EXIT_FAILURE;
			}
		}
	}

	public static int get_number_of_desktops(final Display disp) {
		final Window root = x11.XDefaultRootWindow(disp);
		Integer num_desktops = null;
		if ((num_desktops = get_property_as_int(disp, root, X11.XA_CARDINAL,
				"_NET_NUMBER_OF_DESKTOPS")) == null) {
			if ((num_desktops = get_property_as_int(disp, root,
					X11.XA_CARDINAL, "_WIN_WORKSPACE_COUNT")) == null) {
				p_error("Cannot get number of desktops properties. "
						+ "(_NET_NUMBER_OF_DESKTOPS or _WIN_WORKSPACE_COUNT)\n");
			}
		}
		return (num_desktops == null) ? -1 : num_desktops;
	}

	public static boolean list_desktops(final Display disp) {
		final Window root = x11.XDefaultRootWindow(disp);
		int num_desktops = 0;
		int cur_desktop = 0;

		Pointer desktop_geometry = null;
		final NativeLongByReference desktop_geometry_size = new NativeLongByReference();
		String[] desktop_geometry_str = null;

		Pointer desktop_viewport = null;
		final NativeLongByReference desktop_viewport_size = new NativeLongByReference();
		String[] desktop_viewport_str = null;

		Pointer desktop_workarea = null;
		final NativeLongByReference desktop_workarea_size = new NativeLongByReference();
		String[] desktop_workarea_str = null;

		Pointer list = null;
		final NativeLongByReference desktop_list_size = new NativeLongByReference();
		String[] names = null;

		if ((num_desktops = get_number_of_desktops(disp)) < 0) {
			return EXIT_FAILURE;
		}

		if ((cur_desktop = get_current_desktop(disp)) < 0) {
			return EXIT_FAILURE;
		}

		if (options.wa_desktop_titles_invalid_utf8
				|| (list = get_property(disp, root,
						x11.XInternAtom(disp, "UTF8_STRING", false),
						"_NET_DESKTOP_NAMES", desktop_list_size)) == null) {
			if ((list = get_property(disp, root, X11.XA_STRING,
					"_WIN_WORKSPACE_NAMES", desktop_list_size)) == null) {
				p_verbose("Cannot get desktop names properties. "
						+ "(_NET_DESKTOP_NAMES or _WIN_WORKSPACE_NAMES)\n");
				/* ignore the error - list the desktops without names */
			}
		}

		/* common size of all desktops */
		if ((desktop_geometry = get_property(disp, root, X11.XA_CARDINAL,
				"_NET_DESKTOP_GEOMETRY", desktop_geometry_size)) == null) {
			p_verbose("Cannot get common size of all desktops (_NET_DESKTOP_GEOMETRY).\n");
		}

		/* desktop viewport */
		if ((desktop_viewport = get_property(disp, root, X11.XA_CARDINAL,
				"_NET_DESKTOP_VIEWPORT", desktop_viewport_size)) == null) {
			p_verbose("Cannot get common size of all desktops (_NET_DESKTOP_VIEWPORT).\n");
		}

		/* desktop workarea */
		if ((desktop_workarea = get_property(disp, root, X11.XA_CARDINAL,
				"_NET_WORKAREA", desktop_workarea_size)) == null) {
			if ((desktop_workarea = get_property(disp, root, X11.XA_CARDINAL,
					"_WIN_WORKAREA", desktop_workarea_size)) == null) {
				p_verbose("Cannot get _NET_WORKAREA property.\n");
			}
		}

		/* prepare the array of desktop names */
		names = new String[num_desktops];
		if (list != null) {
			int id = 0;
			final List<Byte> byteList = new ArrayList<Byte>();
			for (int i = 0, size = (int) desktop_list_size.getValue()
					.longValue(); i < size; i++) {
				final byte b = list.getByte(i);
				if (((char) b == '\0') || (i == size - 1)) {
					final byte[] buffer = new byte[byteList.size()];
					for (int j = 0; j < buffer.length; j++) {
						buffer[j] = byteList.get(j);
					}
					names[id++] = new String(buffer);

					if (id >= num_desktops) {
						break;
					}
					byteList.clear();
				} else {
					byteList.add(b);
				}
			}
		}

		/* prepare desktop geometry strings */
		desktop_geometry_str = new String[num_desktops];
		if ((desktop_geometry != null)
				&& (desktop_geometry_size.getValue().longValue() > 0)) {
			final int SIZE_OF_DESKTOP_GEOMETRY = 4;
			if (desktop_geometry_size.getValue().longValue() == 2 * SIZE_OF_DESKTOP_GEOMETRY) {
				/* only one value - use it for all desktops */
				p_verbose("WM provides _NET_DESKTOP_GEOMETRY value common for all desktops.\n");
				for (int i = 0; i < num_desktops; i++) {
					desktop_geometry_str[i] = String
							.format("%dx%d",
									Pointer.nativeValue(desktop_geometry
											.getPointer(0)), Pointer
											.nativeValue(desktop_geometry
													.getPointer(Pointer.SIZE)));
				}
			} else {
				/* seperate values for desktops of different size */
				p_verbose("WM provides separate _NET_DESKTOP_GEOMETRY value for each desktop.\n");
				for (int i = 0; i < num_desktops; i++) {
					if (i < desktop_geometry_size.getValue().longValue()
							/ SIZE_OF_DESKTOP_GEOMETRY / 2) {
						desktop_geometry_str[i] = String.format(
								"%dx%d",
								desktop_geometry.getPointer(i * 2).getLong(0),
								desktop_geometry.getPointer(i * 2 + 1).getLong(
										0));
					} else {
						desktop_geometry_str[i] = "N/A";
					}
				}
			}
		} else {
			for (int i = 0; i < num_desktops; i++) {
				desktop_geometry_str[i] = "N/A";
			}
		}

		/* prepare desktop viewport strings */
		desktop_viewport_str = new String[num_desktops];
		if ((desktop_viewport != null)
				&& (desktop_viewport_size.getValue().longValue() > 0)) {
			final int SIZE_OF_DESKTOP_VIEWPORT = 4;
			if (desktop_viewport_size.getValue().longValue() == 2 * SIZE_OF_DESKTOP_VIEWPORT) {
				/* only one value - use it for current desktop */
				p_verbose("WM provides _NET_DESKTOP_VIEWPORT value only for the current desktop.\n");
				for (int i = 0; i < num_desktops; i++) {
					if (i == cur_desktop) {
						desktop_viewport_str[i] = String.format("%d,%d",
								Pointer.nativeValue(desktop_viewport
										.getPointer(0)), Pointer
										.nativeValue(desktop_viewport
												.getPointer(NativeLong.SIZE)));
					} else {
						desktop_viewport_str[i] = "N/A";
					}
				}
			} else {
				/* seperate values for each of desktops */
				for (int i = 0; i < num_desktops; i++) {
					if (i < desktop_viewport_size.getValue().longValue()
							/ SIZE_OF_DESKTOP_VIEWPORT / 2) {
						desktop_viewport_str[i] = String.format(
								"%d,%d",
								desktop_viewport.getPointer(i * 2).getLong(0),
								desktop_viewport.getPointer(i * 2 + 1).getLong(
										0));
					} else {
						desktop_viewport_str[i] = "N/A";
					}
				}
			}
		} else {
			for (int i = 0; i < num_desktops; i++) {
				desktop_viewport_str[i] = "N/A";
			}
		}

		/* prepare desktop workarea strings */
		desktop_workarea_str = new String[num_desktops];
		if ((desktop_workarea != null)
				&& (desktop_workarea_size.getValue().longValue() > 0)) {
			final int SIZE_OF_DESKTOP_WORKAREA = 4;
			if (desktop_workarea_size.getValue().longValue() == 4 * SIZE_OF_DESKTOP_WORKAREA) {
				/* only one value - use it for current desktop */
				p_verbose("WM provides _NET_WORKAREA value only for the current desktop.\n");
				for (int i = 0; i < num_desktops; i++) {
					if (i == cur_desktop) {
						desktop_workarea_str[i] = String.format("%d,%d %dx%d",
								Pointer.nativeValue(desktop_workarea
										.getPointer(0)), Pointer
										.nativeValue(desktop_workarea
												.getPointer(NativeLong.SIZE)),
								Pointer.nativeValue(desktop_workarea
										.getPointer(2 * NativeLong.SIZE)),
								Pointer.nativeValue(desktop_workarea
										.getPointer(3 * NativeLong.SIZE)));
					} else {
						desktop_workarea_str[i] = "N/A";
					}
				}
			} else {
				/* seperate values for each of desktops */
				for (int i = 0; i < num_desktops; i++) {
					if (i < desktop_workarea_size.getValue().longValue()
							/ SIZE_OF_DESKTOP_WORKAREA / 4) {
						desktop_workarea_str[i] = String.format("%d,%d %dx%d",
								Pointer.nativeValue(desktop_workarea
										.getPointer(i * NativeLong.SIZE * 4)),
								Pointer.nativeValue(desktop_workarea
										.getPointer(i * NativeLong.SIZE * 4
												+ NativeLong.SIZE)), Pointer
										.nativeValue(desktop_workarea
												.getPointer(i * NativeLong.SIZE
														* 4 + 2
														* NativeLong.SIZE)),
								Pointer.nativeValue(desktop_workarea
										.getPointer(i * NativeLong.SIZE * 4 + 3
												* NativeLong.SIZE)));
					} else {
						desktop_workarea_str[i] = "N/A";
					}
				}
			}
		} else {
			for (int i = 0; i < num_desktops; i++) {
				desktop_workarea_str[i] = "N/A";
			}
		}

		/* print the list */
		for (int i = 0; i < num_desktops; i++) {
			printf("%-2d %c DG: %-" + longest_str(desktop_geometry_str)
					+ "s  VP: %-" + longest_str(desktop_viewport_str)
					+ "s  WA: %-" + longest_str(desktop_workarea_str)
					+ "s  %s\n", i, (i == cur_desktop) ? '*' : '-',
					desktop_geometry_str[i], desktop_viewport_str[i],
					desktop_workarea_str[i], defaultString(names[i], "N/A"));
		}

		p_verbose("Total number of desktops: %d\n", num_desktops);
		p_verbose("Current desktop ID (counted from zero): %d\n", cur_desktop);

		g_free(desktop_geometry);
		g_free(desktop_viewport);
		g_free(desktop_workarea);
		g_free(list);

		return EXIT_SUCCESS;
	}

	private static int longest_str(final String[] strv) {
		int max = 0;

		for (int i = 0; i < strv.length; i++) {
			if (strv[i].length() > max) {
				max = strv[i].length();
			}
		}

		return max;
	}

	public static boolean list_windows(final Display disp) {
		return list_windows(disp, options);
	}

	public static boolean list_windows(final Display disp, final Options options) {
		List<Window> client_list;
		int max_client_machine_len = 0;

		if ((client_list = get_client_list(disp)) == null) {
			return EXIT_FAILURE;
		}

		/* find the longest client_machine name */
		for (int i = 0; i < client_list.size(); i++) {
			String client_machine = null;
			if ((client_machine = get_property_as_string(disp,
					client_list.get(i), X11.XA_STRING, "WM_CLIENT_MACHINE")) != null) {
				max_client_machine_len = Math.max(max_client_machine_len,
						client_machine.length());
			}
		}

		/* print the list */
		for (int i = 0; i < client_list.size(); i++) {
			final Window client = client_list.get(i);
			final String title_out = get_window_title(disp, client); /* UTF8 */
			final String class_out = get_window_class(disp, client); /* UTF8 */
			final IntByReference x = new IntByReference();
			final IntByReference y = new IntByReference();
			final IntByReference junkx = new IntByReference();
			final IntByReference junky = new IntByReference();
			final IntByReference wwidth = new IntByReference();
			final IntByReference wheight = new IntByReference();
			final IntByReference bw = new IntByReference();
			final IntByReference depth = new IntByReference();
			final WindowByReference junkroot = new WindowByReference();

			/* desktop ID */
			Integer desktop;
			if ((desktop = get_property_as_int(disp, client, X11.XA_CARDINAL,
					"_NET_WM_DESKTOP")) == null) {
				desktop = get_property_as_int(disp, client, X11.XA_CARDINAL,
						"_WIN_WORKSPACE");
			}

			/* client machine */
			final String client_machine = get_property_as_string(disp, client,
					X11.XA_STRING, "WM_CLIENT_MACHINE");

			/* pid */
			final int pid = get_window_pid(disp, client);

			/* geometry */
			x11.XGetGeometry(disp, client, junkroot, junkx, junky, wwidth,
					wheight, bw, depth);
			x11.XTranslateCoordinates(disp, client_list.get(i),
					junkroot.getValue(), junkx.getValue(), junky.getValue(), x,
					y, junkroot);

			/*
			 * special desktop ID -1 means "all desktops", so we have to convert
			 * the desktop value to signed long
			 */
			printf("0x%08x %2d", client.longValue(), (desktop == null) ? 0
					: desktop);
			if (options.show_pid) {
				printf(" %-6d", (pid == -1) ? 0 : pid);
			}
			if (options.show_geometry) {
				printf(" %-4d %-4d %-4d %-4d", x.getValue(), y.getValue(),
						wwidth.getValue(), wheight.getValue());
			}
			if (options.show_class) {
				printf(" %-20s ", defaultString(class_out, "N/A"));
			}

			printf(" %" + max_client_machine_len + "s %s\n",
					defaultString(client_machine, "N/A"),
					defaultString(title_out, "N/A"));
		}

		return EXIT_SUCCESS;
	}

	public static String get_window_icon_name(final Display disp,
			final Window win) {
		final PointerByReference icon_name_return = new PointerByReference();

		if (getX11Ext().XGetIconName(disp, win, icon_name_return) == 0) {
			g_free(icon_name_return.getPointer());
			return null;
		}

		// for(int i = 0; i < 10; i++) {
		// System.err.println((char)icon_name_return.getPointer().getByte(i));
		// }

		return g_strdup(icon_name_return.getPointer());
	}

	public static String get_active_window_class(final Display disp) {
		final Window win = get_active_window(disp);
		return (win == null) ? null : get_window_class(disp, win);
	}

	public static String get_window_class(final Display disp, final Window win) {
		String class_utf8 = null;
		final NativeLongByReference size = new NativeLongByReference();

		final Pointer wm_class = get_property(disp, win, X11.XA_STRING,
				"WM_CLASS", size);
		if (wm_class != null) {
			int p_0 = 0;
			while (wm_class.getByte(p_0) != 0) {
				p_0++;
			}
			if (size.getValue().longValue() - 1 > p_0) {
				wm_class.setByte(p_0, (byte) '.');
			}
			class_utf8 = g_locale_to_utf8(wm_class);
		}

		g_free(wm_class);

		return class_utf8;
	}

	public static String get_active_window_title(final Display disp) {
		final Window win = get_active_window(disp);
		return (win == null) ? null : get_window_title(disp, win);
	}

	public static String get_window_title(final Display disp, final Window win) {
		String title_utf8 = null;

		final Pointer wm_name = get_property(disp, win, X11.XA_STRING,
				"WM_NAME", null);
		final Pointer net_wm_name = get_property(disp, win,
				x11.XInternAtom(disp, "UTF8_STRING", false), "_NET_WM_NAME",
				null);

		if (net_wm_name != null) {
			title_utf8 = g_strdup(net_wm_name);
		} else if (wm_name != null) {
			title_utf8 = g_locale_to_utf8(wm_name);
		}

		g_free(wm_name);
		g_free(net_wm_name);

		return title_utf8;
	}

	public static long get_active_window_id(final Display disp) {
		final Window win = get_active_window(disp);
		return (win == null) ? -1 : get_window_id(win);
	}

	public static long get_window_id(final Window win) {
		return win.longValue();
	}

	public static int get_active_window_pid(final Display disp) {
		final Window win = get_active_window(disp);
		return (win == null) ? -1 : get_window_pid(disp, win);
	}

	public static int get_window_pid(final Display disp, final Window win) {
		final Integer pid = get_property_as_int(disp, win, X11.XA_CARDINAL,
				"_NET_WM_PID");
		return (pid == null) ? -1 : pid.intValue();
	}

	public static Pointer get_property(final Display disp, final Window win,
			final Atom xa_prop_type, final String prop_name) {
		return get_property(disp, win, xa_prop_type, prop_name, null);
	}

	public static Pointer get_property(final Display disp, final Window win,
			final Atom xa_prop_type, final String prop_name,
			final NativeLongByReference size) {
		final AtomByReference xa_ret_type = new AtomByReference();
		final IntByReference ret_format = new IntByReference();
		final NativeLongByReference ret_nitems = new NativeLongByReference();
		final NativeLongByReference ret_bytes_after = new NativeLongByReference();
		final PointerByReference ret_prop = new PointerByReference();

		final Atom xa_prop_name = x11.XInternAtom(disp, prop_name, false);

		/*
		 * MAX_PROPERTY_VALUE_LEN / 4 explanation (XGetWindowProperty manpage):
		 * 
		 * long_length = Specifies the length in 32-bit multiples of the data to
		 * be retrieved.
		 */
		if (x11.XGetWindowProperty(disp, win, xa_prop_name, new NativeLong(0),
				new NativeLong(MAX_PROPERTY_VALUE_LEN / 4), false,
				xa_prop_type, xa_ret_type, ret_format, ret_nitems,
				ret_bytes_after, ret_prop) != X11.Success) {
			p_verbose("Cannot get %s property.\n", prop_name);
			return null;
		}

		if ((xa_ret_type.getValue() == null)
				|| (xa_ret_type.getValue().longValue() != xa_prop_type
						.longValue())) {
			p_verbose("Invalid type of %s property.\n", prop_name);
			g_free(ret_prop.getPointer());
			return null;
		}

		if (size != null) {
			size.setValue(new NativeLong((ret_format.getValue() / 8)
					* ret_nitems.getValue().longValue()));
		}

		return ret_prop.getValue();
	}

	private static Window get_property_as_window(final Display disp,
			final Window win, final Atom xa_prop_type, final String prop_name) {
		Window ret = null;

		final Pointer prop = get_property(disp, win, xa_prop_type, prop_name,
				null);
		if (prop != null) {
			ret = new Window(prop.getLong(0));
			g_free(prop);
		}

		return ret;
	}

	private static String get_property_as_string(final Display disp,
			final Window win, final Atom xa_prop_type, final String prop_name) {
		String strProp = null;

		final Pointer prop = get_property(disp, win, xa_prop_type, prop_name,
				null);
		if (prop != null) {
			strProp = g_strdup(prop);
			g_free(prop);
		}

		return strProp;
	}

	private static String get_property_as_utf8_string(final Display disp,
			final Window win, final Atom xa_prop_type, final String prop_name) {
		String strProp = null;

		final Pointer prop = get_property(disp, win, xa_prop_type, prop_name,
				null);
		if (prop != null) {
			strProp = g_locale_to_utf8(prop);
			g_free(prop);
		}

		return strProp;
	}

	private static Integer get_property_as_int(final Display disp,
			final Window win, final Atom xa_prop_type, final String prop_name) {
		Integer intProp = null;

		final Pointer prop = get_property(disp, win, xa_prop_type, prop_name,
				null);
		if (prop != null) {
			intProp = prop.getInt(0);
			g_free(prop);
		}

		return intProp;
	}

	private static Long get_property_as_long(final Display disp,
			final Window win, final Atom xa_prop_type, final String prop_name) {
		return get_property_as_long(disp, win, xa_prop_type, prop_name, null);
	}

	private static Long get_property_as_long(final Display disp,
			final Window win, final Atom xa_prop_type, final String prop_name,
			final NativeLongByReference size) {
		Long longProp = null;

		final Pointer prop = get_property(disp, win, xa_prop_type, prop_name,
				size);
		if (prop != null) {
			longProp = prop.getLong(0);
			g_free(prop);
		}

		return longProp;
	}

	public static Window get_active_window(final Display disp) {
		return get_property_as_window(disp, x11.XDefaultRootWindow(disp),
				X11.XA_WINDOW, "_NET_ACTIVE_WINDOW");
	}

	public static Window Select_Window(final Display disp) {
		/*
		 * Routine to let user select a window using the mouse Taken from
		 * xfree86.
		 */

		final XEvent event = new XEvent();
		Window target_win = null;
		final Window root = x11.XDefaultRootWindow(disp);
		int buttons = 0;
		final IntByReference dummyi = new IntByReference();
		final IntByReference dummy = new IntByReference();

		/* Make the target cursor */
		final X11.Cursor cursor = getX11Ext().XCreateFontCursor(disp,
				X11Ext.XC_crosshair);

		/* Grab the pointer using target cursor, letting it room all over */
		final int status = getX11Ext().XGrabPointer(disp, root, false,
				X11.ButtonPressMask | X11.ButtonReleaseMask, X11.GrabModeSync,
				X11.GrabModeAsync, root, cursor, X11.CurrentTime);
		if (status != X11.GrabSuccess) {
			p_error("ERROR: Cannot grab mouse.\n");
			return null;
		}

		/* Let the user select a window... */
		while ((target_win == null) || (buttons != 0)) {
			/* allow one more event */
			getX11Ext().XAllowEvents(disp, X11.SyncPointer, X11.CurrentTime);
			x11.XWindowEvent(disp, root, new NativeLong(X11.ButtonPressMask
					| X11.ButtonReleaseMask), event);
			switch (event.type) {
			case X11.ButtonPress:
				if (target_win == null) {
					target_win = event.xbutton.subwindow; /* window selected */
					if ((target_win == null) || (target_win.longValue() == 0)) {
						target_win = root;
					}
				}
				buttons++;
				break;
			case X11.ButtonRelease:
				/* there may have been some down before we started */
				if (buttons > 0) {
					buttons--;
				}
				break;
			}
		}

		getX11Ext().XUngrabPointer(disp, X11.CurrentTime); /* Done with pointer */

		final WindowByReference windowRef = new WindowByReference();
		if ((x11.XGetGeometry(disp, target_win, windowRef, dummyi, dummyi,
				dummy, dummy, dummy, dummy) != FALSE)
				&& !target_win.equals(windowRef.getValue())) {
			target_win = getXmu().XmuClientWindow(disp, target_win);
		}

		return target_win;
	}

	private static void sleep(final long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// Ignore Exception
		}
	}

	private static void g_free(final Pointer pointer) {
		if (pointer != null) {
			x11.XFree(pointer);
		}
	}

	private static String g_strdup(final Pointer pointer) {
		final String value = pointer.getString(0);
		// g_free(pointer);
		return value;
	}

	private static String g_locale_to_utf8(final Pointer pointer) {
		return g_strdup(pointer);
	}

	private static Integer defaultInt(final String string,
			final Integer defaultInt) {
		if ((string != null) && !string.isEmpty()) {
			try {
				return Integer.parseInt(string);
			} catch (NumberFormatException e) {
				// Ignore Exception
			}
		}

		return defaultInt;
	}

	private static Long defaultLong(final String string, final Long defaultLong) {
		if ((string != null) && !string.isEmpty()) {
			try {
				return Long.parseLong(string);
			} catch (NumberFormatException e) {
				// Ignore Exception
			}
		}

		return defaultLong;
	}

	private static String defaultString(final String string,
			final String defaultString) {
		return (string == null) ? defaultString : string;
	}

	private static void printf(final String format, final Object... args) {
		p_info(format, args);
	}

	private static void p_verbose(final String format, final Object... args) {
		if (options.verbose) {
			p_error(format, args);
		}
	}

	private static void p_info(final String format, final Object... args) {
		System.out.print(String.format(format, args));
		System.out.flush();
	}

	private static void p_error(final String format, final Object... args) {
		System.err.print(String.format(format, args));
		System.err.flush();
	}

	private static List<Long> sscanf(final String param) {
		final List<Long> list = new ArrayList<Long>();
		final Scanner scanner = new Scanner(param);
		scanner.useDelimiter(",");

		while (scanner.hasNextLong()) {
			list.add(scanner.nextLong());
		}
		scanner.close();

		return list;
	}

	private static X11Ext getX11Ext() {
		if (x11Ext == null) {
			x11Ext = (X11Ext) Native.loadLibrary("X11", X11Ext.class);
		}
		return x11Ext;
	}

	private static Xmu getXmu() {
		if (xmu == null) {
			xmu = (Xmu) Native.loadLibrary("Xmu", Xmu.class);
		}
		return xmu;
	}

	public static enum WindowSetTitleMode {
		NAME('N'),

		ICON('I'),

		NAME_AND_ICON('T');

		final char mode;

		private WindowSetTitleMode(final char mode) {
			this.mode = mode;
		}

		public char getMode() {
			return mode;
		}

		@Override
		public String toString() {
			return String.valueOf(mode);
		}
	}

	private static interface X11Ext extends Library {
		int XC_crosshair = 34;

		void XMoveWindow(final Display disp, final Window win, final long x,
				final long y);

		void XResizeWindow(final Display disp, final Window win,
				final long width, final long height);

		void XMoveResizeWindow(final Display disp, final Window win,
				final long x, final long y, final long width, final long height);

		X11.Cursor XCreateFontCursor(final Display disp, final long shape);

		int XGrabPointer(final Display disp, final Window grab_window,
				final boolean owner_events, final long event_mask,
				final int pointer_mode, final int keyboard_mode,
				final Window confine_to, final X11.Cursor cursor, final int time);

		int XAllowEvents(final Display disp, final int event_mode,
				final int time);

		int XUngrabPointer(final Display disp, final int time);

		Window XmuClientWindow(final Display disp, final Window win);

		int XGetIconName(final Display disp, final Window win,
				final PointerByReference icon_name_return);
	}

	private static interface Xmu extends Library {
		Window XmuClientWindow(final Display disp, final Window win);
	}

	public static class Options {
		public boolean verbose;
		public boolean force_utf8;
		public boolean show_class;
		public boolean show_pid;
		public boolean show_geometry;
		public boolean match_by_id;
		public boolean match_by_cls;
		public boolean full_window_title_match;
		public boolean wa_desktop_titles_invalid_utf8;
		public String param_window = "";
		public String param = "";
	};
}
