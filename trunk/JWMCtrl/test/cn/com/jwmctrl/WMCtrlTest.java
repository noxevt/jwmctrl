package cn.com.jwmctrl;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import cn.com.jwmctrl.WMCtrl.Options;
import cn.com.jwmctrl.WMCtrl.WindowSetTitleMode;

import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.unix.X11.Display;
import com.sun.jna.platform.unix.X11.Window;

public class WMCtrlTest {
	private static Display disp = null;
	private static StringWriter sw = null;
	private static PrintStream out = null;

	@BeforeClass
	public static void beforeClass() {
		disp = X11.INSTANCE.XOpenDisplay(null);
		sw = new StringWriter();

		out = new PrintStream(new WriterOutputStream(sw));
		System.setOut(out);

		final File jwmctrl = new File("jwmctrl");
		jwmctrl.setExecutable(true);
	}

	@Before
	public void setUp() {
		clearOutBuffer();
	}

	private String getOut() {
		out.flush();
		final String out = sw.getBuffer().toString();

		// clear out buffer
		clearOutBuffer();

		return out;
	}

	private void clearOutBuffer() {
		sw.getBuffer().delete(0, sw.getBuffer().length());
	}

	@Test
	public void WMCtrl_errorMessage() {
		Assert.assertEquals(execJWMCtrl("-s"), "j" + execWMCtrl("-s"));

		Assert.assertEquals(execJWMCtrl("-s invalid"), "j"
				+ execWMCtrl("-s invalid"));

		Assert.assertEquals(execJWMCtrl("-a"), "j" + execWMCtrl("-a"));

		Assert.assertEquals(execJWMCtrl("-c"), "j" + execWMCtrl("-c"));

		Assert.assertEquals(execJWMCtrl("-r TestWindowTitle -e"), "j"
				+ execWMCtrl("-r TestWindowTitle -e"));

		Assert.assertEquals(execJWMCtrl("-r"), "j" + execWMCtrl("-r"));

		Assert.assertEquals(execJWMCtrl("-r TestWindowTitle -N"), "j"
				+ execWMCtrl("-r TestWindowTitle -N"));

		Assert.assertEquals(execJWMCtrl("-r TestWindowTitle -I"), "j"
				+ execWMCtrl("-r TestWindowTitle -I"));

		Assert.assertEquals(execJWMCtrl("-r TestWindowTitle -T"), "j"
				+ execWMCtrl("-r TestWindowTitle -T"));

		Assert.assertEquals(execJWMCtrl("-k"), "j" + execWMCtrl("-k"));

		Assert.assertEquals(execJWMCtrl("-o"), "j" + execWMCtrl("-o"));

		Assert.assertEquals(execJWMCtrl("-n"), "j" + execWMCtrl("-n"));
	}

	@Ignore
	@Test
	public void WMCtrl() {
		testJWMCtrl("");
	}

	@Ignore
	@Test
	public void WMCtrl_help() {
		// Print help.
		testJWMCtrl("-h");
	}

	@Test
	public void WMCtrl_with_args() {
		// Show information about the window manager and about the
		testJWMCtrl("-m");
		testJWMCtrl("-m", true);

		// List windows managed by the window manager.
		testJWMCtrl("-l");
		testJWMCtrl("-l -p");
		testJWMCtrl("-l -G");
		testJWMCtrl("-l -x");
		testJWMCtrl("-l -p -G -x");

		testJWMCtrl("-l", true);
		testJWMCtrl("-l -p", true);
		testJWMCtrl("-l -G", true);
		testJWMCtrl("-l -x", true);
		testJWMCtrl("-l -p -G -x", true);

		// List desktops. The current desktop is marked with an asterisk.
		testJWMCtrl("-d");
		testJWMCtrl("-d", true);
	}

	@Test
	public void WMCtrl_switchDesktop() {
		// Switch to the specified desktop.
		final int numberOfDesktops = WMCtrl.get_number_of_desktops(disp);
		Assert.assertTrue(numberOfDesktops > 1);
		final int currentDesktop = WMCtrl.get_current_desktop(disp);
		for (int i = 0; i < numberOfDesktops; i++) {
			execJWMCtrl("-s " + i);
			sleep(100);
			Assert.assertEquals(i, WMCtrl.get_current_desktop(disp));
		}
		// restore default desktop
		execJWMCtrl("-s " + currentDesktop);
		sleep(100);
		Assert.assertEquals(currentDesktop, WMCtrl.get_current_desktop(disp));

		execJWMCtrl("-s " + numberOfDesktops);
		sleep(100);
		Assert.assertEquals(currentDesktop, WMCtrl.get_current_desktop(disp));

		testJWMCtrl("-s -1");
	}

	@Test
	public void WMCtrl_activateWindow() {
		// Activate the window by switching to its desktop and raising it.
		final String winTitle1 = "TestActivateWindow1";
		final Frame frame1 = createFrame(winTitle1);
		frame1.setVisible(true);
		sleep(2000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));
		final String winClass1 = WMCtrl.get_active_window_class(disp);
		assertNotBlank(winClass1);
		final long winId1 = WMCtrl.get_active_window_id(disp);
		Assert.assertTrue(winId1 > 0);

		final String winTitle2 = "TestActivateWindow2";
		final Frame frame2 = createFrame(winTitle2);
		frame2.setVisible(true);
		sleep(2000);
		Assert.assertEquals(winTitle2,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));
		final String winClass2 = WMCtrl.get_active_window_class(disp);
		assertNotBlank(winClass2);
		final long winId2 = WMCtrl.get_active_window_id(disp);
		Assert.assertTrue(winId2 > 0);

		execJWMCtrl("-a " + winTitle1);
		sleep(2000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		execJWMCtrl("-a " + winTitle2);
		sleep(2000);
		Assert.assertEquals(winTitle2,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		execJWMCtrl("-a -F " + winTitle1.toLowerCase());
		sleep(2000);
		Assert.assertEquals(winTitle2,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		execJWMCtrl("-a -F " + winTitle1.toUpperCase());
		sleep(2000);
		Assert.assertEquals(winTitle2,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		execJWMCtrl("-a " + winTitle1);
		sleep(2000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		execJWMCtrl("-a -F " + winTitle2.substring(1));
		sleep(2000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		execJWMCtrl("-a " + winTitle2.substring(1));
		sleep(2000);
		Assert.assertEquals(winTitle2,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		execJWMCtrl("-a -F " + winTitle1.substring(1));
		sleep(2000);
		Assert.assertEquals(winTitle2,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		execJWMCtrl("-a " + winTitle1.substring(1));
		sleep(2000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		execJWMCtrl("-a " + winId2 + " -i");
		sleep(2000);
		Assert.assertEquals(winTitle2,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		execJWMCtrl("-a " + winId1 + " -i");
		sleep(2000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		frame1.setVisible(false);
		sleep(2000);
		Assert.assertEquals(winTitle2,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));
		frame1.dispose();

		frame2.setVisible(false);
		sleep(2000);
		Assert.assertFalse(
				"Expect current active window is not " + winTitle2,
				winTitle2.equals(WMCtrl.get_window_title(disp,
						WMCtrl.get_active_window(disp))));

		frame2.setVisible(true);
		frame2.setState(Frame.ICONIFIED);
		sleep(2000);
		Assert.assertFalse(
				"Expect current active window is not " + winTitle2,
				winTitle2.equals(WMCtrl.get_window_title(disp,
						WMCtrl.get_active_window(disp))));
		execJWMCtrl("-a " + winClass2);
		sleep(2000);
		Assert.assertFalse(
				"Expect current active window is not " + winTitle2,
				winTitle2.equals(WMCtrl.get_window_title(disp,
						WMCtrl.get_active_window(disp))));
		// activate window with class
		execJWMCtrl("-a " + winClass2 + " -x");
		sleep(2000);
		Assert.assertEquals(winTitle2, WMCtrl.get_active_window_title(disp));
		frame2.setVisible(false);
		frame2.dispose();

		testJWMCtrl("-a NotExistsWindow");
	}

	@Test
	public void WMCtrl_closeWindow() {
		// Close the window gracefully.
		final String winTitle1 = "TestCloseWindow1";
		final Frame frame1 = createFrame(winTitle1);
		frame1.setVisible(true);
		sleep(2000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		final String winTitle2 = "TestCloseWindow2";
		final Frame frame2 = createFrame(winTitle2);
		frame2.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				frame2.setVisible(false);
				frame2.dispose();
			}
		});
		frame2.setVisible(true);
		sleep(2000);
		Assert.assertEquals(winTitle2,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		execJWMCtrl("-c " + winTitle2);
		sleep(2000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		execJWMCtrl("-c " + winTitle1);
		sleep(2000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		sleep(2000);
		frame1.setVisible(false);
		frame1.dispose();
		Assert.assertFalse(
				"Expect current active window is not " + winTitle1,
				winTitle1.equals(WMCtrl.get_window_title(disp,
						WMCtrl.get_active_window(disp))));

		testJWMCtrl("-c NotExistsWindow");
	}

	@Test
	public void WMCtrl_moveWindowToCurrentDesktop() {
		// Move the window to the current desktop and activate it.
		final String winTitle1 = "TestMoveWindowToCurrentDesktop1";
		final Frame frame1 = createFrame(winTitle1);
		frame1.setVisible(true);
		sleep(2000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		final String winTitle2 = "TestMoveWindowToCurrentDesktop2";
		final Frame frame2 = createFrame(winTitle2);
		frame2.setVisible(true);
		sleep(2000);
		Assert.assertEquals(winTitle2,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		final int numberOfDesktops = WMCtrl.get_number_of_desktops(disp);
		Assert.assertTrue(numberOfDesktops > 1);
		final int currentDesktop = WMCtrl.get_current_desktop(disp);
		for (int i = 0; i < numberOfDesktops; i++) {
			execJWMCtrl("-s " + i);
			sleep(1000);
			Assert.assertEquals(i, WMCtrl.get_current_desktop(disp));
			execJWMCtrl("-R " + winTitle1);
			sleep(2000);
		}
		// restore default desktop
		execJWMCtrl("-s " + currentDesktop);
		sleep(1000);
		Assert.assertEquals(currentDesktop, WMCtrl.get_current_desktop(disp));
		execJWMCtrl("-R " + winTitle1);
		sleep(2000);

		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));
		frame1.setVisible(false);
		frame1.dispose();
		frame2.setVisible(false);
		frame2.dispose();
	}

	@Test
	public void WMCtrl_moveWindowToSpecifiedDesktop() {
		// Move the window to the specified desktop.
		final String winTitle1 = "TestMoveWindowToSpecifiedDesktop1";
		final Frame frame1 = createFrame(winTitle1);
		frame1.setVisible(true);
		sleep(2000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		final String winTitle2 = "TestMoveWindowToSpecifiedDesktop2";
		final Frame frame2 = createFrame(winTitle2);
		frame2.setVisible(true);
		sleep(2000);
		Assert.assertEquals(winTitle2,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		final int numberOfDesktops = WMCtrl.get_number_of_desktops(disp);
		Assert.assertTrue(numberOfDesktops > 1);
		final int currentDesktop = WMCtrl.get_current_desktop(disp);
		for (int i = 0; i < numberOfDesktops; i++) {
			execJWMCtrl("-r " + winTitle1 + " t " + i);
			sleep(2000);
			execJWMCtrl("-s " + i);
			sleep(1000);
			Assert.assertEquals(i, WMCtrl.get_current_desktop(disp));
		}
		execJWMCtrl("-r " + winTitle1 + " t " + currentDesktop);
		sleep(2000);
		// restore default desktop
		execJWMCtrl("-s " + currentDesktop);
		sleep(1000);
		Assert.assertEquals(currentDesktop, WMCtrl.get_current_desktop(disp));

		Assert.assertEquals(winTitle2,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));
		frame1.setVisible(false);
		frame1.dispose();
		frame2.setVisible(false);
		frame2.dispose();
	}

	@Test
	public void WMCtrl_resizeAndMoveWindow() {
		// Resize and move the window around the desktop.
		final String winTitle1 = "TestResizeAndMoveWindow";
		final Frame frame1 = createFrame(winTitle1);
		frame1.setVisible(true);
		sleep(2000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		execJWMCtrl("-r " + winTitle1 + " -e 0,10,10,600,400");
		sleep(1000);
		execJWMCtrl("-r " + winTitle1 + " -e 0,10,50,600,400");
		sleep(1000);
		execJWMCtrl("-r " + winTitle1 + " -e 0,80,50,600,400");
		sleep(1000);
		execJWMCtrl("-r " + winTitle1 + " -e 0,80,50,500,400");
		sleep(1000);
		execJWMCtrl("-r " + winTitle1 + " -e 0,80,50,500,200");
		sleep(1000);

		testJWMCtrl("-r " + winTitle1 + " -e 0,10,10,400,a");
		testJWMCtrl("-r " + winTitle1 + " -e x");
		testJWMCtrl("-r " + winTitle1);

		frame1.setVisible(false);
		frame1.dispose();
	}

	@Test
	public void WMCtrl_changeWindowState() {
		// Change the state of the window. Using this option it's possible for
		// example to make the window maximized, minimized or fullscreen.
		final String winTitle1 = "TestChangeWindowState";
		final Frame frame1 = createFrame(winTitle1);
		frame1.setVisible(true);
		sleep(2000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		execJWMCtrl("-r " + winTitle1 + " -b add,maximized_vert");
		sleep(1000);
		execJWMCtrl("-r " + winTitle1 + " -b remove,maximized_vert");
		sleep(1000);
		execJWMCtrl("-r " + winTitle1 + " -b toggle,maximized_vert");
		sleep(1000);
		execJWMCtrl("-r " + winTitle1 + " -b toggle,maximized_vert");
		sleep(1000);

		testJWMCtrl("-r " + winTitle1 + " -b x,maximized_vert");
		testJWMCtrl("-r " + winTitle1 + " -b add,maximized_vert, ");
		testJWMCtrl("-r " + winTitle1 + " -b add,maximized_vert,");
		testJWMCtrl("-r " + winTitle1 + " -b add, ");
		testJWMCtrl("-r " + winTitle1 + " -b add,");
		testJWMCtrl("-r " + winTitle1);

		frame1.setVisible(false);
		frame1.dispose();
	}

	@Test
	public void WMCtrl_setWindowTitle() {
		// Set the name (long title) of the window.
		final String winTitle1 = "TestSetWindowTitle";
		final Frame frame1 = createFrame(winTitle1);
		frame1.setVisible(true);
		sleep(2000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		final String newWinTitle1 = "TestNewWindowTitle";

		execJWMCtrl("-r " + winTitle1 + " -N " + newWinTitle1);
		sleep(1000);
		Assert.assertEquals(newWinTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		execJWMCtrl("-r " + newWinTitle1 + " -N " + winTitle1);
		sleep(1000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		testJWMCtrl("-r NotExistsWindow -N NewNotExistsWindow");

		frame1.setVisible(false);
		frame1.dispose();
	}

	@Test
	public void WMCtrl_setWindowIconName() {
		// Set the icon name (short title) of the window.
		final String winTitle1 = "TestSetWindowIcon";
		final String winIconName1 = "TestIconName";
		final Frame frame1 = createFrame(winTitle1);
		frame1.setVisible(true);
		sleep(2000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		execJWMCtrl("-r " + winTitle1 + " -I " + winIconName1);
		sleep(1000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));
		// Assert.assertEquals(winIconName1,
		// WMCtrl.get_window_icon_name(disp, WMCtrl.get_active_window(disp)));

		testJWMCtrl("-r NotExistsWindow -I TestIconName");

		frame1.setVisible(false);
		frame1.dispose();
	}

	@Test
	public void WMCtrl_setWindowTitleAndIconName() {
		// Set both the name and the icon name of the window.
		final String winTitle1 = "TestSetWindowTitleAndIconName";
		final Frame frame1 = createFrame(winTitle1);
		frame1.setVisible(true);
		sleep(2000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		final String newWinTitle1 = "TestNewWindowTitleAndIconName";

		execJWMCtrl("-r " + winTitle1 + " -T " + newWinTitle1);
		sleep(1000);
		Assert.assertEquals(newWinTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		execJWMCtrl("-r " + newWinTitle1 + " -T " + winTitle1);
		sleep(1000);
		Assert.assertEquals(winTitle1,
				WMCtrl.get_window_title(disp, WMCtrl.get_active_window(disp)));

		testJWMCtrl("-r NotExistsWindow -T NewNotExistsWindow");

		frame1.setVisible(false);
		frame1.dispose();
	}

	@Test
	public void WMCtrl_showingTheDesktop() {
		// Activate or deactivate window manager's "showing the desktop" mode.
		// Many window managers\n" do not implement this mode.
		execJWMCtrl("-k on");
		sleep(1000);
		final String jwmctrlOut1 = execJWMCtrl("-m");

		execJWMCtrl("-k off");
		sleep(1000);
		final String jwmctrlOut2 = execJWMCtrl("-m");

		execWMCtrl("-k on");
		sleep(1000);
		final String wmctrlOut1 = execWMCtrl("-m");

		execWMCtrl("-k off");
		sleep(1000);
		final String wmctrlOut2 = execWMCtrl("-m");

		Assert.assertEquals(jwmctrlOut1, wmctrlOut1);
		Assert.assertEquals(jwmctrlOut2, wmctrlOut2);

		testJWMCtrl("-k invalid");
	}

	@Test
	public void WMCtrl_changeDesktopViewport() {
		// Change the viewport for the current desktop. The X and Y values are
		// separated with a comma. They define the top left corner of the
		// viewport.
		testJWMCtrl("-o 500,400");
		testJWMCtrl("-o 400,300");

		testJWMCtrl("-o 300,a");
		testJWMCtrl("-o 300,");
		testJWMCtrl("-o 300");
		testJWMCtrl("-o a");
	}

	@Test
	public void WMCtrl_changeNumberOfDesktops() {
		// Change number of desktops.
		final int numberOfDesktop = WMCtrl.get_number_of_desktops(disp);

		execJWMCtrl("-n " + (numberOfDesktop + 1));
		sleep(200);
		Assert.assertEquals(numberOfDesktop + 1,
				WMCtrl.get_number_of_desktops(disp));

		execJWMCtrl("-n " + (numberOfDesktop + 2));
		sleep(200);
		Assert.assertEquals(numberOfDesktop + 2,
				WMCtrl.get_number_of_desktops(disp));

		execJWMCtrl("-n " + (numberOfDesktop + 1));
		sleep(200);
		Assert.assertEquals(numberOfDesktop + 1,
				WMCtrl.get_number_of_desktops(disp));

		testJWMCtrl("-n a");
		sleep(200);
		Assert.assertEquals(numberOfDesktop + 1,
				WMCtrl.get_number_of_desktops(disp));

		testJWMCtrl("-n 0");
		sleep(200);
		Assert.assertEquals(1, WMCtrl.get_number_of_desktops(disp));

		testJWMCtrl("-n 2");
		sleep(200);
		Assert.assertEquals(2, WMCtrl.get_number_of_desktops(disp));

		testJWMCtrl("-n -1");
		sleep(200);
		Assert.assertEquals(1, WMCtrl.get_number_of_desktops(disp));

		execJWMCtrl("-n " + numberOfDesktop);
		sleep(200);
		Assert.assertEquals(numberOfDesktop,
				WMCtrl.get_number_of_desktops(disp));
	}

	@Test
	public void wm_info() {
		Assert.assertTrue(WMCtrl.wm_info(disp));
	}

	@Test
	public void showing_desktop() {
		Assert.assertTrue(WMCtrl.showing_desktop(disp, true));
		sleep(1000);
		Assert.assertTrue(WMCtrl.showing_desktop(disp, false));
	}

	@Test
	public void change_viewport() {
		Assert.assertTrue(WMCtrl.change_viewport(disp, 20, 50));
	}

	@Test
	public void change_geometry() {
		Assert.assertTrue(WMCtrl.change_geometry(disp, 1024, 768));
	}

	@Test
	public void change_number_of_desktops() {
		Assert.assertTrue(WMCtrl.change_number_of_desktops(disp, 5));
	}

	@Test
	public void switch_desktop() {
		Assert.assertEquals(0, WMCtrl.get_current_desktop(disp));

		Assert.assertTrue(WMCtrl.switch_desktop(disp, 1));
		sleep(1000);
		Assert.assertEquals(1, WMCtrl.get_current_desktop(disp));

		Assert.assertTrue(WMCtrl.switch_desktop(disp, 0));
		sleep(1000);
		Assert.assertEquals(0, WMCtrl.get_current_desktop(disp));
	}

	@Test
	public void window_set_title() {
		final Window win = WMCtrl.get_active_window(disp);
		final String title = WMCtrl.get_window_title(disp, win);
		Assert.assertEquals(
				"Java - JWMCtrl/test/cn/com/jwmctrl/WMCtrlTest.java - Eclipse ",
				title);

		// change window title
		final String newTitle = "你好，New Title " + System.currentTimeMillis();
		WMCtrl.window_set_title(disp, win, newTitle, WindowSetTitleMode.NAME);
		Assert.assertEquals(newTitle, WMCtrl.get_window_title(disp, win));
		sleep(2000);

		// set window title to ""
		WMCtrl.window_set_title(disp, win, "", WindowSetTitleMode.NAME);
		Assert.assertEquals("", WMCtrl.get_window_title(disp, win));
		sleep(2000);

		// remove window title
		WMCtrl.window_set_title(disp, win, null, WindowSetTitleMode.NAME);
		Assert.assertNull(WMCtrl.get_window_title(disp, win));
		sleep(2000);

		// restore window title
		WMCtrl.window_set_title(disp, win, title, WindowSetTitleMode.NAME);
		Assert.assertEquals(title, WMCtrl.get_window_title(disp, win));
	}

	@Test
	public void window_to_desktop() {
		final Window win = WMCtrl.get_active_window(disp);
		Assert.assertTrue(WMCtrl.window_to_desktop(disp, win, 1));
	}

	@Test
	public void activate_window() {
		final Window win = new Window(0x03400004);
		Assert.assertTrue(WMCtrl.activate_window(disp, win, true));
	}

	@Test
	public void close_window() {
		final Window win = new Window(0x03400004);
		Assert.assertTrue(WMCtrl.close_window(disp, win));
	}

	@Test
	public void window_state() {

	}

	@Test
	public void wm_supports() {
		Assert.assertFalse(WMCtrl.wm_supports(disp, null));
		Assert.assertFalse(WMCtrl.wm_supports(disp, ""));
		Assert.assertFalse(WMCtrl.wm_supports(disp, " "));
		Assert.assertTrue(WMCtrl.wm_supports(disp, "_NET_MOVERESIZE_WINDOW"));
	}

	@Test
	public void window_move_resize() {
		final Window win = WMCtrl.get_active_window(disp);
		Assert.assertTrue(WMCtrl.window_move_resize(disp, win,
				"0,20,40,800,600"));
	}

	@Test
	public void action_window_pid() {
		Assert.assertTrue(WMCtrl.action_window_pid(disp, 0x03400004, 'a'));
	}

	@Test
	public void list_desktops() {
		WMCtrl.list_desktops(disp);
		String out = getOut();
		Assert.assertEquals(out, execWMCtrl("-d"));
	}

	@Test
	public void list_windows() {
		WMCtrl.list_windows(disp);
		String out = getOut();
		Assert.assertEquals(out, execWMCtrl("-l"));

		Options options = new Options();
		options.show_pid = true;
		WMCtrl.list_windows(disp, options);
		out = getOut();
		Assert.assertEquals(out, execWMCtrl("-l -p"));

		options = new Options();
		options.show_geometry = true;
		WMCtrl.list_windows(disp, options);
		out = getOut();
		Assert.assertEquals(out, execWMCtrl("-l -G"));

		options = new Options();
		options.show_class = true;
		WMCtrl.list_windows(disp, options);
		out = getOut();
		Assert.assertEquals(out, execWMCtrl("-l -x"));

		options = new Options();
		options.show_pid = true;
		options.show_geometry = true;
		options.show_class = true;
		WMCtrl.list_windows(disp, options);
		out = getOut();
		Assert.assertEquals(out, execWMCtrl("-l -p -G -x"));
	}

	@Test
	public void get_client_list() {
		final List<Window> clientList = WMCtrl.get_client_list(disp);
		Assert.assertNotNull(clientList);
		Assert.assertFalse(clientList.isEmpty());
	}

	@Test
	public void get_window_class() {
		Window win = WMCtrl.get_active_window(disp);
		Assert.assertNotNull(win);

		final String winClass = WMCtrl.get_window_class(disp, win);
		assertNotBlank(winClass);
		Assert.assertEquals("Eclipse.Eclipse", winClass);
	}

	@Test
	public void get_window_title() {
		Window win = WMCtrl.get_active_window(disp);
		Assert.assertNotNull(win);

		final String title = WMCtrl.get_window_title(disp, win);
		System.out.println(title);
		assertNotBlank(title);
	}

	@Test
	public void get_active_window() {
		Window win = WMCtrl.get_active_window(disp);
		Assert.assertNotNull(win);
		Assert.assertTrue(win.longValue() > 0);
	}

	private void assertNotBlank(final String string) {
		Assert.assertTrue(StringUtils.isNotBlank(string));
	}

	private void sleep(final long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void exec() {
		System.err.println(execWMCtrl(""));
	}

	private void testJWMCtrl(final String args) {
		testJWMCtrl(args, false);
	}

	private void testJWMCtrl(final String args, final boolean verbose) {
		final String out = execJWMCtrl(args + (verbose ? " -v" : ""));
		Assert.assertEquals(out, execWMCtrl(args + (verbose ? " -v" : "")));
	}

	private Frame createFrame(final String title) {
		Frame frame = new Frame(title);
		frame.setSize(400, 300);

		return frame;
	}

	/**
	 * Execute jwmctrl command.
	 * 
	 * @param args
	 *            Arguments for the jwmctrl command.
	 * @return Return output for the jwmctrl command.
	 */
	private String execJWMCtrl(final String args) {
		Assert.assertNotNull(args);
		return exec((new File("jwmctrl")).getAbsolutePath() + " " + args);
	}

	/**
	 * Execute wmctrl command.
	 * 
	 * @param args
	 *            Arguments for the wmctrl command.
	 * @return Return output for the wmctrl command.
	 */
	private String execWMCtrl(final String args) {
		Assert.assertNotNull(args);
		return exec("wmctrl " + args);
	}

	/**
	 * Execute command.
	 * 
	 * @param command
	 *            Command name or path.
	 * @return Return output for the command.
	 */
	private String exec(final String command) {
		assertNotBlank(command);
		// Process process = null;
		// BufferedReader br = null;
		try {
			// // process = Runtime.getRuntime().exec(command);
			// ProcessBuilder pb = new ProcessBuilder(StringUtils.split(command,
			// " "));
			//
			// pb.redirectErrorStream(true);
			// process = pb.start();
			//
			// // read process output
			// final InputStream input = process.getInputStream();
			// br = new BufferedReader(new InputStreamReader(input));
			// final StringBuilder sbInput = new StringBuilder();
			// String line = null;
			// while ((line = br.readLine()) != null) {
			// sbInput.append(String.format("%s%n", line));
			// }
			//
			// return sbInput.toString();

			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
			final PumpStreamHandler streamHandler = new PumpStreamHandler(
					outputStream, errorStream);

			final CommandLine commandline = CommandLine.parse(command);
			final DefaultExecutor exec = new DefaultExecutor();
			exec.setExitValues(null);
			exec.setStreamHandler(streamHandler);

			// execute command
			exec.execute(commandline);

			// get outputs for command
			return outputStream.toString() + errorStream.toString();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} finally {
			// if (br != null) {
			// try {
			// br.close();
			// } catch (IOException e) {
			// }
			// }
			// if (process != null) {
			// process.destroy();
			// }
		}
		return null;
	}
}
