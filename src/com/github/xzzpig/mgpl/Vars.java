package com.github.xzzpig.mgpl;

import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.github.xzzpig.pigapi.tcp.Client;

public class Vars {
	public static FileConfiguration config;
	public static int port,maxcount;
	public static HashMap<String,Boolean> logined = new HashMap<>();
	public static HashMap<Player,Client> clients = new HashMap<>();
}
