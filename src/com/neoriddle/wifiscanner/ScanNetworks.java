package com.neoriddle.wifiscanner;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.neoriddle.wifiscanner.utils.AndroidUtils;

/**
 * This activity show a list which has all detected wifi networks.
 * @author Israel Buitron
 *
 */
public class ScanNetworks extends Activity {

	private static final int ABOUT_DIALOG = 0;
	private WifiManager manager;
	private WifiReceiver receiver;

	private ListView networksList;
	private ScanResultsAdapter adapter;
	private List<ScanResult> results;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.network_list);

		manager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		receiver = new WifiReceiver();

		if(manager.isWifiEnabled()) {
			scanNetworks();
			networksList = networksList==null ? (ListView)findViewById(R.id.lstNetworks) : networksList;
			adapter = new ScanResultsAdapter(this, results);
			networksList.setAdapter(adapter);
		} else
			Toast.makeText(this, R.string.wifi_is_not_enabled_msg, Toast.LENGTH_LONG).show();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		LayoutInflater factory = LayoutInflater.from(this);

		switch (id) {
		case ABOUT_DIALOG:
			final View aboutView = factory.inflate(R.layout.about_dialog, null);

			TextView versionLabel = (TextView)aboutView.findViewById(R.id.version_label);
			versionLabel.setText(getString(R.string.version_msg, AndroidUtils.getAppVersionName(getApplicationContext())));

			return new AlertDialog.Builder(this).
			setIcon(R.drawable.icon).
			setTitle(R.string.app_name).
			setView(aboutView).
			setPositiveButton(R.string.close, null).
			create();
		}

		return null;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mnuRescan:
			scanNetworks();
			adapter.notifyDataSetChanged();
			return true;
		case R.id.mnuAbout:
			showDialog(ABOUT_DIALOG);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}

	/**
	 * Trigger a wifi network scanning.
	 */
	public void scanNetworks() {
		boolean scan = manager.startScan();

		if(scan) {
			results = manager.getScanResults();
			Toast.makeText(this, getString(R.string.networks_found_msg, results.size()), Toast.LENGTH_LONG).show();
		} else
			switch(manager.getWifiState()) {
			case WifiManager.WIFI_STATE_DISABLING:
				Toast.makeText(this, R.string.wifi_disabling_msg, Toast.LENGTH_LONG).show();
				break;
			case WifiManager.WIFI_STATE_DISABLED:
				Toast.makeText(this, R.string.wifi_disabled_msg, Toast.LENGTH_LONG).show();
				break;
			case WifiManager.WIFI_STATE_ENABLING:
				Toast.makeText(this, R.string.wifi_enabling_msg, Toast.LENGTH_LONG).show();
				break;
			case WifiManager.WIFI_STATE_ENABLED:
				Toast.makeText(this, R.string.wifi_enabled_msg, Toast.LENGTH_LONG).show();
				break;
			case WifiManager.WIFI_STATE_UNKNOWN:
				Toast.makeText(this, R.string.wifi_unknown_state_msg, Toast.LENGTH_LONG).show();
				break;
			}

	}

	class WifiReceiver extends BroadcastReceiver {

		public List<ScanResult> getResults() {
			return results;
		}

		public WifiManager getManager() {
			return manager;
		}

		@Override
		public void onReceive(Context c, Intent intent) {
			results = manager.getScanResults();
			adapter.notifyDataSetChanged();
		}
	}
}

