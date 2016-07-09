package com.github.xzzpig.mgpl;

import java.io.IOException;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.xzzpig.mgpl.listeners.GameListener;
import com.github.xzzpig.mgpl.listeners.TCPListener;
import com.github.xzzpig.pigapi.bukkit.TCommandHelp;
import com.github.xzzpig.pigapi.bukkit.TConfig;
import com.github.xzzpig.pigapi.event.Event;
import com.github.xzzpig.pigapi.tcp.Server;

public class Main extends JavaPlugin {
	public static Main self;
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String arg0 = "help";
		if (args.length > 0)
			arg0 = args[0];
		if (arg0.equalsIgnoreCase("help")) {
			for (TCommandHelp comm : Help.MGPL.getAllSubs()) {
				if (sender instanceof Player)
					comm.getHelpMessage(getName()).send((Player) sender);
				else
					sender.sendMessage(comm.getHelpMessage(getName()).toString());
			}
			return true;
		}
		if (!sender.hasPermission("mgpl.admin")) {
			sender.sendMessage("[MGPL]你没有权限执行该命令");
			return true;
		}
		if (arg0.equalsIgnoreCase("set")) {
			return true;
		}
		return false;

	}

	// 插件停用函数
	@Override
	public void onDisable() {
		try {
			Server.server.ss.close();
		} catch (IOException e) {
			e.printStackTrace();
			getLogger().info("TCP服务器关闭失败");
		}
		getLogger().info(getName() + "插件已被停用");
	}

	@Override
	public void onEnable() {
		self = this;
		getLogger().info(getName() + getDescription().getVersion() + "插件已被加载");
		saveDefaultConfig();
		Vars.config = TConfig.getConfigFile("MGPL", "config.yml");
		Vars.port = Vars.config.getInt("mgpl.port",10727);
		Vars.maxcount = Vars.config.getInt("mgpl.max_count_pre_ip",1);
		try {
			new Server(Vars.port);
			getLogger().info("TCP服务器创建成功");
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().warning("TCP服务器创建失败");
			return;
		}
		Event.registListener(TCPListener.self);
		getServer().getPluginManager().registerEvents(GameListener.self, this);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return Help.MGPL.getTabComplete(getName(), sender, command, alias, args);
	}
}
