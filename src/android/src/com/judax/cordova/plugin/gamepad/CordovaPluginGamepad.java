package com.judax.cordova.plugin.gamepad;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Build;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnKeyListener;

@SuppressLint("NewApi")
public class CordovaPluginGamepad extends CordovaPlugin implements
		OnGenericMotionListener, OnKeyListener, InputManager.InputDeviceListener
{
	private static final int NUMBER_OF_BUTTONS = 17;
	private static final int NUMBER_OF_AXES = 4;
	private static final String ID = "id";
	private static final String INDEX = "index";
	private static final String CONNECTED = "connected";
	private static final String TIME_STAMP = "timestamp";
	private static final String MAPPING = "mapping";
	private static final String AXES = "axes";
	private static final String BUTTONS = "buttons";
	private static final int BUTTON_INDEX_DPAD_UP = 12;
	private static final int BUTTON_INDEX_DPAD_DOWN = 13;
	private static final int BUTTON_INDEX_DPAD_LEFT = 14;
	private static final int BUTTON_INDEX_DPAD_RIGHT = 15;
	private static final String GAMEPAD = "gamepad";
	private static final String GAMEPAD_CONNECTED = "gamepadconnected";
	private static final String GAMEPAD_DISCONNECTED = "gamepaddisconnected";
	private static final HashMap<Integer, Integer> KEY_CODE_TO_BUTTON_INDEX_MAP = new HashMap<Integer, Integer>();
	private static final HashMap<Integer, Integer> AXE_INDEX_TO_BUTTON_INDEX_MAP = new HashMap<Integer, Integer>();
	private static final HashMap<Integer, Integer> AXE_INDEX_TO_AXE_INDEX_MAP = new HashMap<Integer, Integer>();
	// This structure allows to control some keys/buttons in a different manner.
	// The key codes included in this structure will just be processed on action
	// down and cancelled in the next iteration.
	private static final ArrayList<Integer> KEY_CODES_TO_JUST_PROCESS_ACTION_DOWN = new ArrayList<Integer>();
	private static final double ZERO = 0.0;
	private static final double ONE = 1.0;
	static
	{
		KEY_CODE_TO_BUTTON_INDEX_MAP.put(96, 0); // BUTTON_O
		KEY_CODE_TO_BUTTON_INDEX_MAP.put(97, 1); // BUTTON_A
		KEY_CODE_TO_BUTTON_INDEX_MAP.put(99, 2); // BUTTON_U
		KEY_CODE_TO_BUTTON_INDEX_MAP.put(100, 3); // BUTTON_Y
		KEY_CODE_TO_BUTTON_INDEX_MAP.put(102, 4); // LEFT_BUMPER_BUTTON
		KEY_CODE_TO_BUTTON_INDEX_MAP.put(103, 5); // RIGHT_BUMPER_BUTTON
		AXE_INDEX_TO_BUTTON_INDEX_MAP.put(17, 6); // LEFT_TRIGGER
		AXE_INDEX_TO_BUTTON_INDEX_MAP.put(18, 7); // RIGHT_TRIGGER
		// KEY_CODE_TO_BUTTON_INDEX_MAP.put(SELECT/BACK, 8);
		// KEY_CODE_TO_BUTTON_INDEX_MAP.put(START/FORWARD, 9);
		KEY_CODE_TO_BUTTON_INDEX_MAP.put(106, 10); // LEFT_JOYSTICK_BUTTON
		KEY_CODE_TO_BUTTON_INDEX_MAP.put(107, 11); // RIGHT_JOYSTICK_BUTTON
		// DPAD buttons are treated separately
		KEY_CODE_TO_BUTTON_INDEX_MAP.put(19, 12); // DPAD_UP
		KEY_CODE_TO_BUTTON_INDEX_MAP.put(20, 13); // DPAD_DOWN
		KEY_CODE_TO_BUTTON_INDEX_MAP.put(21, 14); // DPAD_LEFT
		KEY_CODE_TO_BUTTON_INDEX_MAP.put(22, 15); // DPAD_RIGHT
		AXE_INDEX_TO_AXE_INDEX_MAP.put(0, 0); // LEFT_JOYSTICK_AXIS_X
		AXE_INDEX_TO_AXE_INDEX_MAP.put(1, 1); // LEFT_JOYSTICK_AXIS_Y
		AXE_INDEX_TO_AXE_INDEX_MAP.put(11, 2); // RIGHT_JOYSTICK_AXIS_X
		AXE_INDEX_TO_AXE_INDEX_MAP.put(14, 3); // RIGHT_JOYSTICK_AXIS_Y

		if (Build.MODEL.toLowerCase().contains("ouya"))
		{
			KEY_CODE_TO_BUTTON_INDEX_MAP.put(82, 16); // BUTTON_MENU
			KEY_CODES_TO_JUST_PROCESS_ACTION_DOWN.add(82);
		}
	}
	private JSONObject eventArgument = null;
	private JSONArray gamepads = null;
	private boolean dpadHandledByOnKeyEvent = false;
	private long initialTimeMillis = System.currentTimeMillis();
	// Stores the used indices
	private ArrayList<Boolean> usedIndices = null;
	// Stores the mapping between device ids and indices
	private HashMap<Integer, Long> deviceIdToIndex = null;

	private CallbackContext gamepadConnectedCallbackContext = null;
	private CallbackContext gamepadDisconnectedCallbackContext = null;

	class ButtonToJustProcessActionDown
	{
		private int gamepadIndex;
		private int buttonIndex;
		private boolean getGamepadsCalled;

		public ButtonToJustProcessActionDown(int gamepadIndex, int buttonIndex)
		{
			this.gamepadIndex = gamepadIndex;
			this.buttonIndex = buttonIndex;
			this.getGamepadsCalled = false;
		}

		public int getGamepadIndex()
		{
			return gamepadIndex;
		}

		public int getButtonIndex()
		{
			return buttonIndex;
		}

		public boolean getGamepadsCalled()
		{
			return getGamepadsCalled;
		}

		public void setGetGamepadsCalled(boolean getGamepadsCalled)
		{
			this.getGamepadsCalled = getGamepadsCalled;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + buttonIndex;
			result = prime * result + gamepadIndex;
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ButtonToJustProcessActionDown other = (ButtonToJustProcessActionDown) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (buttonIndex != other.buttonIndex)
				return false;
			if (gamepadIndex != other.gamepadIndex)
				return false;
			return true;
		}

		private CordovaPluginGamepad getOuterType()
		{
			return CordovaPluginGamepad.this;
		}
	}

	private ArrayList<ButtonToJustProcessActionDown> buttonsToJustProcessActionDown = null;

	private int getFirstFreeIndex()
	{
		int index = -1;
		for (int i = 0; index == -1 && i < usedIndices.size(); i++)
		{
			if (!usedIndices.get(i))
			{
				index = i;
				usedIndices.set(i, true);
			}
		}
		if (index == -1)
		{
			usedIndices.add(true);
			index = usedIndices.size() - 1;
		}
		return index;
	}

	@TargetApi(12)
	private JSONObject createGamepadForInputDevice(InputDevice inputDevice)
			throws JSONException
	{
		JSONObject gamepad = new JSONObject();
		JSONArray axes = new JSONArray();
		for (int i = 0; i < NUMBER_OF_AXES; i++)
		{
			axes.put(ZERO);
		}
		JSONArray buttons = new JSONArray();
		for (int i = 0; i < NUMBER_OF_BUTTONS; i++)
			buttons.put(ZERO);
		int deviceId = inputDevice.getId();
		long index = deviceIdToIndex.containsKey(deviceId) ? deviceIdToIndex
				.get(deviceId) : getFirstFreeIndex();
		deviceIdToIndex.put(deviceId, index);
		gamepad.put(ID, inputDevice.getName());
		gamepad.put(INDEX, index);
		gamepad.put(CONNECTED, true);
		gamepad.put(TIME_STAMP, System.currentTimeMillis() - initialTimeMillis);
		gamepad.put(MAPPING, "standard");
		gamepad.put(AXES, axes);
		gamepad.put(BUTTONS, buttons);
		return gamepad;
	}

	@TargetApi(16)
	private void refreshGamepads() throws JSONException
	{
		int[] deviceIds = InputDevice.getDeviceIds();
		this.gamepads = new JSONArray();
		for (int i = 0; i < deviceIds.length; i++)
		{
			int deviceId = deviceIds[i];

			InputDevice inputDevice = InputDevice.getDevice(deviceId);
			int sources = inputDevice.getSources();
			boolean isJoystick = (sources & InputDevice.SOURCE_JOYSTICK) != 0;
			boolean isGamepad = (sources & InputDevice.SOURCE_GAMEPAD) != 0;
			boolean isVirtual = inputDevice.isVirtual();
			int motionRangesCount = inputDevice.getMotionRanges().size();
			// Only handle joysticks or gamepads that have more than one motionRanges
			// and that are not virtual (the NVIDIA Shield has virtual mouse cursor
			// that is identified as a joystick).
			if (!isVirtual && (isJoystick || isGamepad) && motionRangesCount > 0)
			{
				// System.out.println(inputDevice.getName() + ": isJoystick = "
				// + isJoystick + ", isGamepad = " + isGamepad + ", isVirtual = "
				// + isVirtual + ", motionRangesCount = " + motionRangesCount);
				gamepads.put(createGamepadForInputDevice(InputDevice
						.getDevice(deviceId)));
			}
		}
	}

	@Override
	public void initialize(final CordovaInterface cordova, CordovaWebView webView)
	{
		super.initialize(cordova, webView);
		try
		{
			usedIndices = new ArrayList<Boolean>();
			deviceIdToIndex = new HashMap<Integer, Long>();
			buttonsToJustProcessActionDown = new ArrayList<ButtonToJustProcessActionDown>();
			eventArgument = new JSONObject();
			gamepadConnectedCallbackContext = null;
			gamepadDisconnectedCallbackContext = null;
			refreshGamepads();
			initialTimeMillis = System.currentTimeMillis();

			webView.setOnGenericMotionListener(this);
			webView.setOnKeyListener(this);

			cordova.getActivity().runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					InputManager inputManager = (InputManager) cordova.getActivity()
							.getSystemService(Context.INPUT_SERVICE);
					inputManager.registerInputDeviceListener(CordovaPluginGamepad.this,
							null);
				}
			});
		}
		catch (JSONException e)
		{
			// TODO: Notify an error
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onDestroy()
	{
		this.webView.setOnGenericMotionListener(null);
		this.webView.setOnKeyListener(null);

		usedIndices = null;
		buttonsToJustProcessActionDown = null;
		eventArgument = null;
		gamepads = null;

		this.cordova.getActivity().runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				InputManager inputManager = (InputManager) cordova.getActivity()
						.getSystemService(Context.INPUT_SERVICE);
				inputManager.unregisterInputDeviceListener(CordovaPluginGamepad.this);
			}
		});
	}

	@Override
	public boolean execute(String action, String rawArgs,
			CallbackContext callbackContext) throws JSONException
	{
		boolean valid = true;
		if (action.equals("getGamepads"))
		{
			// Before returning the gamepads, check on the status of the
			// buttonsToJustProcessActionDown
			// and for all of thow that the getGamepads has already been called, ZERO
			// them.
			// For the others, just mark as getGamepads has already been called (this
			// time!).
			if (gamepads.length() > 0)
			{
				for (ButtonToJustProcessActionDown button : buttonsToJustProcessActionDown)
				{
					if (button.getGamepadsCalled())
					{
						JSONObject gamepad = gamepads.getJSONObject(button.getGamepadIndex());
						JSONArray buttons = gamepad.getJSONArray(BUTTONS);
						buttons.put(button.getButtonIndex(), ZERO);
					}
					else
					{
						button.setGetGamepadsCalled(true);
					}
				}
			}
			else if (!buttonsToJustProcessActionDown.isEmpty())
			{
				// Security measure...
				buttonsToJustProcessActionDown.clear();
			}
			callbackContext.success(gamepads);
		}
		else if (action.equals("setGamepadConnectedCallback"))
		{
			gamepadConnectedCallbackContext = callbackContext;
		}
		else if (action.equals("setGamepadDisconnectedCallback"))
		{
			gamepadDisconnectedCallbackContext = callbackContext;
		}
		else
		{
			valid = false;
		}
		return valid;
	}

	private void showDeviceIdsToIndices()
	{
		System.out.println("DeviceIds To Indices:");
		for (Integer id : deviceIdToIndex.keySet())
		{
			System.out.println("\t" + id + ": " + deviceIdToIndex.get(id));
		}
	}

	private void showGamepads() throws JSONException
	{
		System.out.println("Gamepads:");

		for (int i = 0; i < gamepads.length(); i++)
		{
			JSONObject gamepad = gamepads.getJSONObject(i);
			System.out.println("\t" + gamepad.get(INDEX));
		}
	}

	@Override
	@TargetApi(9)
	public void onInputDeviceAdded(int deviceId)
	{
		try
		{
			// System.out.println("BEGIN Input Device Added! " + deviceId +
			// ", gamepads.length = " + gamepads.length);
			// showDeviceIdsToIndices();
			// showGamepads();

			JSONObject gamepad = createGamepadForInputDevice(InputDevice
					.getDevice(deviceId));

			gamepads.put(gamepad);

			eventArgument.put(GAMEPAD, gamepad);
			gamepadConnectedCallbackContext.success(eventArgument);
		}
		catch (JSONException e)
		{
			// TODO: Notify an error
			throw new RuntimeException(e);
		}

		// System.out.println("END Input Device Added! " + deviceId +
		// ", gamepads.length = " + gamepads.length);
		// showDeviceIdsToIndices();
		// showGamepads();
	}

	@Override
	public void onInputDeviceChanged(int deviceId)
	{
		// System.out.println("onInputDeviceChanged: " + deviceId);
	}

	@Override
	public void onInputDeviceRemoved(int deviceId)
	{
		try
		{
//			System.out.println("BEGIN Input Device Removed! " + deviceId
//					+ ", gamepads.length = " + gamepads.length());
//			showDeviceIdsToIndices();
//			showGamepads();

			// It seems that the JSONArray class does not implement the remove method. This is why we need to 
			// create a newGamepads array and copy the gamepads to it.
			// TODO: Research on obfuscation possible solution as stated in: http://stackoverflow.com/questions/20389105/changing-or-upgrading-built-in-org-json-libraries-is-possible-and-a-good-idea
			
			// System.out.println("onInputDeviceRemoved: " + deviceId);
			if (deviceIdToIndex.containsKey(deviceId))
			{
				JSONObject gamepad = null;
				long index = deviceIdToIndex.get(deviceId); 
				JSONArray newGamepads = new JSONArray();
				for (int i = 0; i < gamepads.length(); i++)
				{
					gamepad = gamepads.getJSONObject(i);
					if (gamepads.getJSONObject(i).getLong(INDEX) == index)
					{
						// Remove any reference to the buttonsToJustProcessActionDown for the removed gamepad
						for (int k = 0; k < buttonsToJustProcessActionDown.size(); k++) 
						{
							if (buttonsToJustProcessActionDown.get(k).getGamepadIndex() == i)
							{
								buttonsToJustProcessActionDown.remove(k);
								k--;
							}
						}
					}
					else 
					{
						newGamepads.put(gamepad);
					}
				}
				gamepads = newGamepads;
				usedIndices.set((int)index,  false);
				deviceIdToIndex.remove(deviceId);
				
				eventArgument.put(GAMEPAD, gamepad);
				gamepadDisconnectedCallbackContext.success(eventArgument);
			}
			else 
			{
				System.err
						.println("ERROR: A device with id '" + deviceId + "' has been disconnected and it was not inside the gamepad array!");
			}

			// This is the version that uses JSONArray remove call. Much cleaner, simple and efficient. The problem is 
			// that is seems that some Android versions do not implement this function (event there is no compilation error).
//			if (deviceIdToIndex.containsKey(deviceId))
//			{
//				JSONObject gamepad = null;
//				long index = deviceIdToIndex.get(deviceId);
//				for (int i = 0, j = 0; gamepad == null && i < gamepads.length(); i++)
//				{
//					if (gamepads.getJSONObject(i).getLong(INDEX) == index)
//					{
//						gamepad = gamepads.getJSONObject(i);
//						// Remove any reference to the buttonsToJustProcessActionDown for
//						// the removed gamepad
//						for (int k = 0; k < buttonsToJustProcessActionDown.size(); k++)
//						{
//							if (buttonsToJustProcessActionDown.get(k).getGamepadIndex() == i)
//							{
//								buttonsToJustProcessActionDown.remove(k);
//								k--;
//							}
//						}
//						gamepads.remove(i);
//					}
//				}
//				usedIndices.set((int) index, false);
//				deviceIdToIndex.remove(deviceId);
//
//				eventArgument.put(GAMEPAD, gamepad);
//				gamepadDisconnectedCallbackContext.success(eventArguments);
//			}
//			else
//			{
//				System.err
//						.println("ERROR: A device with id '"
//								+ deviceId
//								+ "' has been disconnected and it was not inside the gamepad array!");
//			}

//			System.out.println("END Input Device Removed! " + deviceId
//					+ ", gamepads.length = " + gamepads.length());
//			showDeviceIdsToIndices();
//			showGamepads();
		}
		catch (JSONException e)
		{
			// TODO: Notify an error
			throw new RuntimeException(e);
		}

	}

	@TargetApi(9)
	private JSONObject findGamepad(int deviceId) throws JSONException
	{
		JSONObject gamepad = null;
		if (deviceIdToIndex.containsKey(deviceId))
		{
			long index = deviceIdToIndex.get(deviceId);
			for (int i = 0; gamepad == null && i < gamepads.length(); i++)
			{
				if (gamepads.getJSONObject(i).getLong(INDEX) == index)
				{
					gamepad = gamepads.getJSONObject(i);
				}
			}
		}
		return gamepad;
	}

	@Override
	@TargetApi(12)
	public boolean onGenericMotion(View v, MotionEvent event)
	{
		boolean processed = false;
		try
		{
			InputDevice inputDevice = event.getDevice();
			if (inputDevice != null)
			{
				JSONObject gamepad = findGamepad(inputDevice.getId());
				if (gamepad != null)
				{
					JSONArray gamepadButtons = gamepad.getJSONArray(BUTTONS);
					for (Integer axeIndex : AXE_INDEX_TO_BUTTON_INDEX_MAP.keySet())
					{
						gamepadButtons.put(
								(int) AXE_INDEX_TO_BUTTON_INDEX_MAP.get(axeIndex),
								event.getAxisValue(axeIndex));
					}
					// HACK: It seems that the controller dpad reading in a regular
					// Android
					// device and the OUYA are different.
					// OUYA: The events are sent as onKey events.
					// Android: The events are sent as onGenericMortion events.
					// (event.getSource() & InputDevice.SOURCE_DPAD) != 0 should have been
					// valid in order to identify if the source
					// was the dpad and do not take motion events into account when
					// handling
					// the dpad. But it seems that we cannot find
					// a way to handle this correctly so these internal flag has been
					// added to
					// identify if the dpad is handled using
					// onKey events or if they should be handled using onGenericMotion
					// events.
					if (!dpadHandledByOnKeyEvent)
					{
						gamepadButtons.put(BUTTON_INDEX_DPAD_UP,
								event.getAxisValue(MotionEvent.AXIS_HAT_Y) == -1 ? ONE : ZERO);
						gamepadButtons.put(BUTTON_INDEX_DPAD_DOWN,
								event.getAxisValue(MotionEvent.AXIS_HAT_Y) == 1 ? ONE : ZERO);
						gamepadButtons.put(BUTTON_INDEX_DPAD_LEFT,
								event.getAxisValue(MotionEvent.AXIS_HAT_X) == -1 ? ONE : ZERO);
						gamepadButtons.put(BUTTON_INDEX_DPAD_RIGHT,
								event.getAxisValue(MotionEvent.AXIS_HAT_X) == 1 ? ONE : ZERO);
					}

					JSONArray gamepadAxes = gamepad.getJSONArray(AXES);
					for (Integer axeIndex : AXE_INDEX_TO_AXE_INDEX_MAP.keySet())
					{
						gamepadAxes.put((int) AXE_INDEX_TO_AXE_INDEX_MAP.get(axeIndex),
								event.getAxisValue(axeIndex));
					}
					processed = true;

					// String s = "All motion ranges: ";
					// for (InputDevice.MotionRange motionRange:
					// inputDevice.getMotionRanges())
					// {
					// s += motionRange.getAxis() + ": " +
					// event.getAxisValue(motionRange.getAxis()) + ", ";
					// }
					// System.out.println(s);

					gamepad.put(TIME_STAMP, System.currentTimeMillis()
							- initialTimeMillis);
				}
			}
		}
		catch (JSONException e)
		{
			// TODO: Notify an error
			throw new RuntimeException(e);
		}
		return processed;
	}

	@Override
	@TargetApi(9)
	public boolean onKey(View v, int keyCode, KeyEvent event)
	{
		boolean processed = false;
		try
		{
			InputDevice inputDevice = event.getDevice();
			if (inputDevice != null)
			{
				JSONObject gamepad = findGamepad(inputDevice.getId());
				if (gamepad != null)
				{
					JSONArray gamepadButtons = gamepad.getJSONArray(BUTTONS);

					// for (Integer keyCodeToJustProcessActionDown:
					// KEY_CODES_TO_JUST_PROCESS_ACTION_DOWN)
					// {
					// Integer buttonIndex =
					// KEY_CODE_TO_BUTTON_INDEX_MAP.get(keyCodeToJustProcessActionDown);
					// if (buttonIndex != null && gamepadButtons[buttonIndex].equals(ONE))
					// {
					// gamepadButtons[buttonIndex] = ZERO;
					// }
					// }

					boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
					boolean keyCodeToJustProcessActionDown = KEY_CODES_TO_JUST_PROCESS_ACTION_DOWN
							.contains(keyCode);

					Integer buttonIndex = KEY_CODE_TO_BUTTON_INDEX_MAP.get(keyCode);
					if (buttonIndex != null)
					{

						// HACK: It seems that the controller dpad reading in a regular
						// Android
						// device and the OUYA are different.
						// OUYA: The events are sent as onKey events.
						// Android: The events are sent as onGenericMortion events.
						// (event.getSource() & InputDevice.SOURCE_DPAD) != 0 should have
						// been
						// valid in order to identify if the source
						// was the dpad and do not take motion events into account when
						// handling
						// the dpad. But it seems that we cannot find
						// a way to handle this correctly so these internal flag has been
						// added
						// to identify if the dpad is handled using
						// onKey events or if they should be handled using onGenericMotion
						// events.
						if (!dpadHandledByOnKeyEvent)
						{
							dpadHandledByOnKeyEvent = buttonIndex.intValue() == BUTTON_INDEX_DPAD_UP
									|| buttonIndex.intValue() == BUTTON_INDEX_DPAD_DOWN
									|| buttonIndex.intValue() == BUTTON_INDEX_DPAD_LEFT
									|| buttonIndex.intValue() == BUTTON_INDEX_DPAD_RIGHT;
						}

						// System.out.println("button index '" + buttonIndex +
						// "' found for keyCode '" + keyCode + "' and is action " + (down ?
						// "DOWN" : "UP") + ".");

						if (down)
						{
							// Only notify the down if it is the first time (not the
							// repetitions)
							if (event.getRepeatCount() == 0)
							{
								gamepadButtons.put((int) buttonIndex, ONE);

								if (keyCodeToJustProcessActionDown)
								{
									// This button is special as it only is ONE-d and not ZERO-ed.
									// In order to get it to ZERO again,
									// an entry to the buttonsToJustProcessActionDown is
									// generated. This structure holds all the buttons
									// that should be marked as ZERO but as they are handled only
									// on action down, they need to be shown
									// to the developer/app at least once. It is determined that
									// this happens when at least one getGamepads
									// call has been made. That is where the
									// ButtonToJustProcessActionDown class comes into play, to
									// store
									// the status of the button regarding the getGamepads call.
									// Actually, if there was already one, just mark the
									// getGamepadsCalled flag back to false (for the strange case
									// where the
									// button was activated, the a getGamepads was called (so the
									// next time it should be ZERO-ed) but then a new press
									// is specified.
									ButtonToJustProcessActionDown buttonToJustProcessActionDown = new ButtonToJustProcessActionDown(
											(int) (long) (Long) gamepad.get(INDEX), buttonIndex);
									int indexOfButton = buttonsToJustProcessActionDown
											.indexOf(buttonToJustProcessActionDown);
									if (indexOfButton >= 0)
									{
										buttonToJustProcessActionDown = buttonsToJustProcessActionDown
												.get(indexOfButton);
										buttonToJustProcessActionDown.setGetGamepadsCalled(false);
									}
									else
									{
										buttonsToJustProcessActionDown
												.add(buttonToJustProcessActionDown);
									}
								}
							}
						}
						else if (!keyCodeToJustProcessActionDown)
						{
							gamepadButtons.put((int) buttonIndex, ZERO);
						}
						processed = true;
					}

					gamepad.put(TIME_STAMP, System.currentTimeMillis()
							- initialTimeMillis);
				}
			}
		}
		catch (JSONException e)
		{
			// TODO: Notify an error
			throw new RuntimeException(e);
		}
		// System.out.println("keyCode: " + keyCode);
		return processed;
	}

}
