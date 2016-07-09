package com.github.xzzpig.mgpl.listeners;

import java.nio.charset.Charset;
import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.xzzpig.mgpl.Vars;
import com.github.xzzpig.pigapi.json.JSONObject;
import com.github.xzzpig.pigapi.tcp.Client;
import com.github.xzzpig.pigapi.tcp.Server;

public class GameListener implements Listener {
	public static GameListener self = new GameListener();

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		Client client = null;
		for (Client c : Server.server.getClients()) {
			if (c.s.getInetAddress().getHostAddress()
					.equalsIgnoreCase(player.getAddress().getAddress().getHostAddress()))
				client = c;
		}
		if (client == null) {
			player.kickPlayer("未连接启动器");
			return;
		}
		Vars.clients.put(player, client);
		client.data.setString("name", player.getName());
		JSONObject json = new JSONObject();
		json.accumulate("command", "loginask");
		int time = new Random().nextInt(10);
		json.accumulate("time", time);
		client.data.setInt("time", time);
		client.sendData(json.toString().getBytes(Charset.forName("UTF-8")));
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
					if (!Vars.logined.containsKey(player.getName()) || Vars.logined.get(player.getName()) == false) {
						player.kickPlayer("登录验证超时");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (!Vars.clients.containsKey(player)) {
			player.kickPlayer("未连接启动器");
			return;
		}
		if (!Vars.logined.containsKey(player.getName()) || Vars.logined.get(player.getName()) == false) {
			event.setCancelled(true);
			player.sendMessage("[MGPL]你暂未登录,请等待客户端验证");
		}
	}
	
	@EventHandler
	public void onPlayerLogOff(PlayerQuitEvent event){
		Vars.clients.remove(event.getPlayer());
		Vars.logined.remove(event.getPlayer().getName());
	}
}
