package com.github.xzzpig.mgpl.listeners;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.github.xzzpig.mgpl.Main;
import com.github.xzzpig.mgpl.Vars;
import com.github.xzzpig.pigapi.MD5;
import com.github.xzzpig.pigapi.PigData;
import com.github.xzzpig.pigapi.customevent.ClientConnectEvent;
import com.github.xzzpig.pigapi.customevent.ServerDataReachEvent;
import com.github.xzzpig.pigapi.event.EventHandler;
import com.github.xzzpig.pigapi.event.Listener;
import com.github.xzzpig.pigapi.json.JSONObject;
import com.github.xzzpig.pigapi.tcp.Client;

public class TCPListener implements Listener {
	public static TCPListener self = new TCPListener();

	private TCPListener() {
	}

	@EventHandler
	public void onClientConnect(ClientConnectEvent event) {
		JSONObject json = new JSONObject();
		Client client = event.getClient();
		client.data.setString("key", client.hashCode() + "");
		json.accumulate("key", client.hashCode() + "");
		json.accumulate("command", "info");
		event.getClient().sendData(json.toString().getBytes(Charset.forName("UTF-8")));
	}

	@EventHandler
	public void onDataReach(ServerDataReachEvent event) {
		String str = (String) event.getData();
		JSONObject json = new JSONObject(str), ret = new JSONObject();
		if (json.getString("command").equalsIgnoreCase("logincheck")) {
			ret.accumulate("command", "logincheckresult");
			String id = json.getString("id");
			String pass = json.getString("password");
			File path = new File(Main.self.getDataFolder(), "/userdata");
			path.mkdirs();
			File file = new File(Main.self.getDataFolder(), "/userdata/" + id + ".pd");
			try {
				file.createNewFile();
				PigData data = new PigData(file);
				if (MD5.GetMD5Code(data.getString("password") + event.getClient().data.getString("key"))
						.equalsIgnoreCase(pass)) {
					ret.accumulate("result", "true");
				} else {
					ret.accumulate("result", "false");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			event.getClient().sendData(ret.toString().getBytes(Charset.forName("UTF-8")));
		} else if (json.getString("command").equalsIgnoreCase("register")) {
			ret.accumulate("command", "regresult");
			String id = json.getString("id");
			String pass = json.getString("password");
			File path = new File(Main.self.getDataFolder(), "/userdata");
			path.mkdirs();
			File file = new File(Main.self.getDataFolder(), "/userdata/" + id + ".pd");
			File ipdatafile = new File(Main.self.getDataFolder(), "ipdata.pd");
			try {
				file.createNewFile();
				ipdatafile.createNewFile();
				PigData data = new PigData(file);
				PigData ipData = new PigData(ipdatafile);
				String ip = event.getClient().s.getInetAddress().getHostAddress().replace(".", "-");
				if ((data.getString("password") != null) && (!data.getString("password").equalsIgnoreCase("")))
					ret.accumulate("result", "registed");
				else if (ipData.getList(ip).size() >= Vars.maxcount)
					ret.accumulate("result", "overmax");
				else {
					ret.accumulate("result", "success");
					data.set("password", pass).saveToFile(file);
					List<String> ids = ipData.getList(ip);
					ids.add(id);
					ipData.set(ip, ids).saveToFile(ipdatafile);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			event.getClient().sendData(ret.toString().getBytes(Charset.forName("UTF-8")));
		} else if (json.getString("command").equalsIgnoreCase("loginaskresult")) {
			String id = json.getString("id");
			String pass = json.getString("password");
			File path = new File(Main.self.getDataFolder(), "/userdata");
			path.mkdirs();
			File file = new File(Main.self.getDataFolder(), "/userdata/" + id + ".pd");
			try {
				file.createNewFile();
				PigData data = new PigData(file);
				String pass2 = data.getString("password");
				String password = "";
				int time = event.getClient().data.getInt("time");
				for (int i = 0; i < time; i++) {
					password = MD5.GetMD5Code(password + pass2 + event.getClient().data.getString("key"));
				}
				if (pass.equalsIgnoreCase(password)) {
					Vars.logined.put(id, true);
					try {
						Bukkit.getPlayer(UUID.fromString(id)).sendMessage("[MGPL]登录成功");
						;
					} catch (Exception e) {
					}
				} else {
					try {
						Bukkit.getPlayer(UUID.fromString(id)).kickPlayer("登录验证失败");
					} catch (Exception e) {
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
