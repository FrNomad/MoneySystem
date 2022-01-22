package MoneySource;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.md_5.bungee.api.ChatColor;

public class MoneyMain extends JavaPlugin implements Listener, TabExecutor {
	
	ChatColor NoticeChatcolor = ChatColor.AQUA;
	
	public static List<MoneyAddress> address = new ArrayList<MoneyAddress>();
	public static List<MoneyUser> user = new ArrayList<MoneyUser>();
	
	private String userDataPath = getDataFolder() + "/userdata.json";
	private String addressDataPath = getDataFolder() + "/addressdata.json";
	
	public File userData = new File(userDataPath);
	public File addressData = new File(addressDataPath);
	
	private final Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}]");
	private String format(String msg) {
		if(Bukkit.getVersion().contains("1.16")) {
			Matcher match = pattern.matcher(msg);
			while(match.find()) {
				String color = msg.substring(match.start(), match.end());
				msg = msg.replace(color, ChatColor.of(color) + "");
				match = pattern.matcher(msg);
			}
		}
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
	
	
	@Override
	public void onEnable() {
		getCommand("money").setExecutor(this);
		getCommand("money").setTabCompleter(this);
		getServer().getPluginManager().registerEvents(this, this);
		PluginDescriptionFile pdfile = this.getDescription();
		System.out.println(NoticeChatcolor + "==================================================");
		createRootFolder();
		createFile(userData);
		createFile(addressData);
		fileToJson();
		System.out.println(NoticeChatcolor + "[MoneyManager] �� ������ �������Դϴ�. ������ �߻��� �� �ֽ��ϴ�. ������ ���� �� ���ڵ� FrNomad#2821�� �������ּ���.");
		System.out.println(NoticeChatcolor + "[MoneyManager] ������ : FrNomad");
		System.out.println(NoticeChatcolor + "[MoneyManager] " + pdfile.getName() + " " + pdfile.getVersion() + " ������ ���� �Ϸ��.");
		System.out.println(NoticeChatcolor + "==================================================");
	}
	
	@Override
	public void onDisable() {
		PluginDescriptionFile pdfile = this.getDescription();
		System.out.println(NoticeChatcolor + "==================================================");
		jsonToFile();
		System.out.println(NoticeChatcolor + "[MoneyManager] " + pdfile.getName() + " " + pdfile.getVersion() + " ������ ����.");
		System.out.println(NoticeChatcolor + "==================================================");
	}
	
	public void createFile(File f) {
		if(!f.exists() || !f.isFile()) {
			try {
				f.createNewFile();
				System.out.println(NoticeChatcolor + "[MoneyManager]" + f.getName() + " ������ ���� �����Ǿ����ϴ�.");
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void createRootFolder() {
		File RootFolder = getDataFolder();
		if(!RootFolder.exists()) {
			try {
				RootFolder.mkdir();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		else return;
	}
	
	public List<String> userList() {
		List<String> users = new ArrayList<String>();
		for(int i = 0; i < user.size(); i++) {
			users.add(user.get(i).getUserName());
		}
		return users;
	}
	
	public List<String> addressList() {
		List<String> addresses = new ArrayList<String>();
		for(int i = 0; i < address.size(); i++) {
			addresses.add(address.get(i).getAddressName());
		}
		return addresses;
	}
	
	public void fileToJson() {
		List<String> userl = new ArrayList<String>();
		List<String> addressl = new ArrayList<String>();
		Charset cs = StandardCharsets.UTF_8;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			userl = Files.readAllLines(Paths.get(userData.getPath()), cs);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			addressl = Files.readAllLines(Paths.get(addressData.getPath()), cs);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		address = (addressl == null ? null : new ArrayList<>(Arrays.asList(gson.fromJson(String.join("", addressl), MoneyAddress[].class))));
		user = (userl == null ? null : new ArrayList<>(Arrays.asList(gson.fromJson(String.join("", userl), MoneyUser[].class))));
	}
	
	public void jsonToFile() {
		BufferedOutputStream bs = null;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			bs = new BufferedOutputStream(new FileOutputStream(userData));
			String userstr = gson.toJson(user.toArray(new MoneyUser[user.size()]));
			bs.write(userstr.getBytes());
		} catch(Exception e) {
			e.getStackTrace();
		} finally {
			try {
				bs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			bs = new BufferedOutputStream(new FileOutputStream(addressData));
			String addressstr = gson.toJson(address.toArray(new MoneyAddress[address.size()]));
			bs.write(addressstr.getBytes());
		} catch(Exception e) {
			e.getStackTrace();
		} finally {
			try {
				bs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<Player> playerList() {
		List<Player> plist = new ArrayList<Player>();
		for(int i = 0; i < Bukkit.getServer().getWorlds().size(); i++) {
			plist.addAll(Bukkit.getServer().getWorlds().get(i).getPlayers());
		}
		return plist;
	}
	
//-------------------<Event>----------------------------------------------------------------------------	
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		boolean isNew = true;
		for(int i = 0; i < user.size(); i++) {
			if(user.get(i).getUserUUID().equals(e.getPlayer().getUniqueId())) {
				isNew = false;
				if(user.get(i).getUserName().equals(e.getPlayer().getName())) {
					break;
				}
				else {
					user.get(i).changeUserName(e.getPlayer().getName());
					break;
				}
			}
		}
		if(isNew) {
			user.add(new MoneyUser(e.getPlayer()));
		}
		else;
		jsonToFile();
	}
//-------------------<Command>---------------------------------------------------------------------------

	private String consoleCmd[] = {"add", "subtract", "give", "all", "list"};
	private String playerCmd[] = {"send", "check", "company"};
	
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		if(label.equalsIgnoreCase("money")) {
			if(args.length < 1) {
				s.sendMessage(ChatColor.GOLD + "��ɾ� ��� ���: \n" + ChatColor.RED + "/money <list|check|send|company|add|subtract|give|all>");
			}
			else {
				if(contains(consoleCmd, args[0])) {
					if(!(s instanceof Player)) {
						if(args[0].equalsIgnoreCase("add")) {          //money add
							if(errorLength(args, 4, s, "/money add <company|user> <ȸ��|����ڸ�> <�׼�>", true)) {
								boolean isExist = false;
								if(args[1].equalsIgnoreCase("user")) {
									MoneyUser mu = null;
									for(int i = 0; i < user.size(); i++) {
										if(user.get(i).getUserName().equals(args[2])) {
											mu = user.get(i);
											isExist = true;
											break;
										}
									}
									if(isExist) {
										try {
											int pmoney = Integer.parseInt(args[3]);
											s.sendMessage(format("&f&l[&a&l!&f&l] &r&6"+ mu.getUserName() + " ���ΰ��¿� &a" + pmoney + " �� &6�� ���޵Ǿ����ϴ�."));
											mu.AddMoney(pmoney);
											s.sendMessage(format("&f&l[&a&l!&f&l] &r&6�ܾ��� &a" + mu.getUserMoney() + " �� &6�Դϴ�."));
											jsonToFile();
										} catch(NumberFormatException e) {
											s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�׼��� ������ �Է��ϼž� �մϴ�!"));
										}
										
									}
									else {
										s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�ش� ���ΰ��°� �������� �ʽ��ϴ�."));
									}
								}
								else if(args[1].equalsIgnoreCase("company")) {
									MoneyAddress mad = null;
									for(int i = 0; i < address.size(); i++) {
										if(address.get(i).getAddressName().equals(args[2])) {
											mad = address.get(i);
											isExist = true;
											break;
										}
									}
									if(isExist) {
										try {
											int pmoney = Integer.parseInt(args[3]);
											s.sendMessage(format("&f&l[&a&l!&f&l] &r&6"+ mad.getAddressName() + " ȸ����¿� &a" + pmoney + " �� &6�� ���޵Ǿ����ϴ�."));
											mad.AddMoney(pmoney);
											s.sendMessage(format("&f&l[&a&l!&f&l] &r&6�ܾ��� &a" + mad.getAddressMoney() + " �� &6�Դϴ�."));
											jsonToFile();
										} catch(NumberFormatException e) {
											s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�׼��� ������ �Է��ϼž� �մϴ�!"));
										}
										
									}
									else {
										s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�ش� ȸ����°� �������� �ʽ��ϴ�."));
									}
								}
								else {
									s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�˸��� ��ɾ �Է��Ͻʽÿ�!"));
								}
							}
						}
						else if(args[0].equalsIgnoreCase("subtract")) {          //money subtract
							if(errorLength(args, 4, s, "/money subtract <company|user> <ȸ��|����ڸ�> <�׼�>", true)) {
								boolean isExist = false;
								if(args[1].equalsIgnoreCase("user")) {
									MoneyUser mu = null;
									for(int i = 0; i < user.size(); i++) {
										if(user.get(i).getUserName().equals(args[2])) {
											mu = user.get(i);
											isExist = true;
											break;
										}
									}
									if(isExist) {
										try {
											int pmoney = Integer.parseInt(args[3]);
											s.sendMessage(format("&f&l[&a&l!&f&l] &r&6"+ mu.getUserName() + " ���ΰ��¿� &c" + pmoney + " �� &6�� �����Ǿ����ϴ�."));
											mu.RemoveMoney(pmoney);
											s.sendMessage(format("&f&l[&a&l!&f&l] &r&6�ܾ��� &a" + mu.getUserMoney() + " �� &6�Դϴ�."));
											jsonToFile();
										} catch(NumberFormatException e) {
											s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�׼��� ������ �Է��ϼž� �մϴ�!"));
										}
										
									}
									else {
										s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�ش� ���ΰ��°� �������� �ʽ��ϴ�."));
									}
								}
								else if(args[1].equalsIgnoreCase("company")) {
									MoneyAddress mad = null;
									for(int i = 0; i < address.size(); i++) {
										if(address.get(i).getAddressName().equals(args[2])) {
											mad = address.get(i);
											isExist = true;
											break;
										}
									}
									if(isExist) {
										try {
											int pmoney = Integer.parseInt(args[3]);
											s.sendMessage(format("&f&l[&a&l!&f&l] &r&6"+ mad.getAddressName() + " ȸ����¿� &c" + pmoney + " �� &6�� �����Ǿ����ϴ�."));
											mad.RemoveMoney(pmoney);
											s.sendMessage(format("&f&l[&a&l!&f&l] &r&6�ܾ��� &a" + mad.getAddressMoney() + " �� &6�Դϴ�."));
											jsonToFile();
										} catch(NumberFormatException e) {
											s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�׼��� ������ �Է��ϼž� �մϴ�!"));
										}
										
									}
									else {
										s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�ش� ȸ����°� �������� �ʽ��ϴ�."));
									}
								}
								else {
									s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�˸��� ��ɾ �Է��Ͻʽÿ�!"));
								}
							}
						}
						else if(args[0].equalsIgnoreCase("give")) {         //money give
							if(errorLength(args, 6, s, "/money give <company|user> <ȸ��1|����ڸ�1> <company|user> <ȸ��2|����ڸ�2> <�׼�>", true)) {
								boolean isExist1 = false;
								boolean isExist2 = false;
								if(args[1].equalsIgnoreCase("user")) {
									MoneyUser mu = null;
									for(int i = 0; i < user.size(); i++) {
										if(user.get(i).getUserName().equals(args[2])) {
											mu = user.get(i);
											isExist1 = true;
											break;
										}
									}
									if(args[3].equalsIgnoreCase("user")) {
										MoneyUser mu2 = null;
										for(int i = 0; i < user.size(); i++) {
											if(user.get(i).getUserName().equals(args[4])) {
												mu2 = user.get(i);
												isExist2 = true;
												break;
											}
										}
										if(isExist1 && isExist2) {
											try {
												int pmoney = Integer.parseInt(args[5]);
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&c"+ mu.getUserName() + " &6���ΰ��¿��� &a" + mu2.getUserName() + " &6���ΰ��·� &a" + pmoney + " �� &6�� �̵��Ǿ����ϴ�."));
												mu.RemoveMoney(pmoney);
												mu2.AddMoney(pmoney);
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&c" + mu.getUserName() + " &6���ΰ��� �ܾ��� &a" + mu.getUserMoney() + " �� &6�Դϴ�."));
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&a" + mu2.getUserName() + " &6���ΰ��� �ܾ��� &a" + mu2.getUserMoney() + " �� &6�Դϴ�."));
												jsonToFile();
											} catch(NumberFormatException e) {
												s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�׼��� ������ �Է��ϼž� �մϴ�!"));
											}
										}
										else {
											s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�ش� ȸ����� �Ǵ� ���ΰ��°� �������� �ʽ��ϴ�."));
										}
									}
									else if(args[3].equalsIgnoreCase("company")) {
										MoneyAddress mad2 = null;
										for(int i = 0; i < address.size(); i++) {
											if(address.get(i).getAddressName().equals(args[4])) {
												mad2 = address.get(i);
												isExist2 = true;
												break;
											}
										}
										if(isExist1 && isExist2) {
											try {
												int pmoney = Integer.parseInt(args[5]);
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&c"+ mu.getUserName() + " &6���ΰ��¿��� &a" + mad2.getAddressName() + " &6ȸ����·� &a" + pmoney + " �� &6�� �̵��Ǿ����ϴ�."));
												mu.RemoveMoney(pmoney);
												mad2.AddMoney(pmoney);
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&c" + mu.getUserName() + " &6���ΰ��� �ܾ��� &a" + mu.getUserMoney() + " �� &6�Դϴ�."));
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&a" + mad2.getAddressName() + " &6ȸ����� �ܾ��� &a" + mad2.getAddressMoney() + " �� &6�Դϴ�."));
												jsonToFile();
											} catch(NumberFormatException e) {
												s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�׼��� ������ �Է��ϼž� �մϴ�!"));
											}
										}
										else {
											s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�ش� ȸ����� �Ǵ� ���ΰ��°� �������� �ʽ��ϴ�."));
										}
									}
									else {
										s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�˸��� ��ɾ �Է��Ͻʽÿ�!"));
									}
								}
								else if(args[1].equalsIgnoreCase("company")) {
									MoneyAddress mad = null;
									for(int i = 0; i < address.size(); i++) {
										if(address.get(i).getAddressName().equals(args[2])) {
											mad = address.get(i);
											isExist1 = true;
											break;
										}
									}
									if(args[3].equalsIgnoreCase("user")) {
										MoneyUser mu2 = null;
										for(int i = 0; i < user.size(); i++) {
											if(user.get(i).getUserName().equals(args[4])) {
												mu2 = user.get(i);
												isExist2 = true;
												break;
											}
										}
										if(isExist1 && isExist2) {
											try {
												int pmoney = Integer.parseInt(args[5]);
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&c"+ mu2.getUserName() + " &6ȸ����¿��� &a" + mad.getAddressName() + " &6���ΰ��·� &a" + pmoney + " �� &6�� �̵��Ǿ����ϴ�."));
												mad.RemoveMoney(pmoney);
												mu2.AddMoney(pmoney);
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&c" + mu2.getUserName() + " &6ȸ����� �ܾ��� &a" + mu2.getUserMoney() + " �� &6�Դϴ�."));
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&a" + mad.getAddressName() + " &6���ΰ��� �ܾ��� &a" + mad.getAddressMoney() + " �� &6�Դϴ�."));
												jsonToFile();
											} catch(NumberFormatException e) {
												s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�׼��� ������ �Է��ϼž� �մϴ�!"));
											}
										}
										else {
											s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�ش� ȸ����� �Ǵ� ���ΰ��°� �������� �ʽ��ϴ�."));
										}
									}
									else if(args[3].equalsIgnoreCase("company")) {
										MoneyAddress mad2 = null;
										for(int i = 0; i < address.size(); i++) {
											if(address.get(i).getAddressName().equals(args[4])) {
												mad2 = address.get(i);
												isExist2 = true;
												break;
											}
										}
										if(isExist1 && isExist2) {
											try {
												int pmoney = Integer.parseInt(args[5]);
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&c"+ mad.getAddressName() + " &6ȸ����¿��� &a" + mad2.getAddressName() + " &6ȸ����·� &a" + pmoney + " �� &6�� �̵��Ǿ����ϴ�."));
												mad.RemoveMoney(pmoney);
												mad2.AddMoney(pmoney);
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&c" + mad.getAddressName() + " &6ȸ����� �ܾ��� &a" + mad.getAddressMoney() + " �� &6�Դϴ�."));
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&a" + mad2.getAddressName() + " &6ȸ����� �ܾ��� &a" + mad2.getAddressMoney() + " �� &6�Դϴ�."));
												jsonToFile();
											} catch(NumberFormatException e) {
												s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�׼��� ������ �Է��ϼž� �մϴ�!"));
											}
										}
										else {
											s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�ش� ȸ����� �Ǵ� ���ΰ��°� �������� �ʽ��ϴ�."));
										}
									}
									else {
										s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�˸��� ��ɾ �Է��Ͻʽÿ�!"));
									}
									
								}
								else {
									s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�˸��� ��ɾ �Է��Ͻʽÿ�!"));
								}
							}
						}
						else if(args[0].equalsIgnoreCase("all")) {
							if(errorLength(args, 1, s, "/money all", true)) {
								int allmoney = 0;
								for(int i = 0; i < user.size(); i++) {
									allmoney += user.get(i).getUserMoney();
								}
								for(int i = 0; i < address.size(); i++) {
									allmoney += address.get(i).getAddressMoney();
								}
								s.sendMessage(format("&f&l[&a&l!&f&l] &r&6�� ������ �� ��ȭ���� &a" + allmoney + " �� &6�Դϴ�."));
							}
						}
						else if(args[0].equalsIgnoreCase("list")) {
							if(errorLength(args, 2, s, "/money list <company|user>", true)) {
								String allstr = "&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�\n";
								boolean isValid = false;
								if(args[1].equalsIgnoreCase("user")) {
									isValid = true;
									allstr += "&f&l[&a&l!&f&l] &r&a[����� ���]\n";
									for(int i = 0; i < user.size(); i++) {
										allstr += "&f&l[&a&l!&f&l] &r&a&l" + user.get(i).getUserName() + "   &r&7-------------   &a" + user.get(i).getUserMoney() + " &6��\n";
									}
								}
								else if(args[1].equalsIgnoreCase("company")) {
									isValid = true;
									allstr += "&f&l[&a&l!&f&l] &r&a[ȸ�� ���]\n";
									for(int i = 0; i < address.size(); i++) {
										allstr += "&f&l[&a&l!&f&l] &r&a&l" + address.get(i).getAddressName() + "   &r&7-------------   &a" + address.get(i).getAddressMoney() + " &6��\n   &f&l[&6&l!&f&l] &r&a&l[������] : &a" + address.get(i).getAddressChief().getUserName() + "\n   &r&f&l[&6&l!&f&l] &r&a&l[������] : \n";
										for(int j = 0; j < address.get(i).getAddressPlayer().size(); j++) {
											allstr += "      &r&f&l[&6&l!&f&l] &r&a&l" + address.get(i).getAddressPlayer().get(j).getUserName() + "\n";
										}
									}
								}
								else {
									s.sendMessage(format("&f&l[&c&l!&f&l] &r&c�˸��� ��ɾ �Է��Ͻʽÿ�!"));
								}
								if(isValid) {
									allstr += "&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�";
									s.sendMessage(format(allstr));
								}
								else;
							}
						}
						else;
					}
					else {
						s.sendMessage(format("&f&l[&c&l!&f&l] &r&6/money " + args[0] + " &c��ɾ�� ���� �ֿܼ����� ����� �����մϴ�."));
					}
				}
				else if(contains(playerCmd, args[0])) {
					if(s instanceof Player) {
						Player p = (Player) s;
						if(args[0].equalsIgnoreCase("send")) {
							if(errorLength(args, 4, s, "/money send <company|user> <ȸ��|����ڸ�> <�׼�>", true)) {
								boolean isExist1 = false;
								boolean isExist2 = false;
								MoneyUser smu = null;
								for(int i = 0; i < user.size(); i++) {
									if(user.get(i).getUserName().equals(p.getName())) {
										smu = user.get(i);
										isExist1 = true;
										break;
									}
								}
								if(isExist1) {
									if(args[1].equalsIgnoreCase("user")) {
										MoneyUser mu = null;
										for(int i = 0; i < user.size(); i++) {
											if(user.get(i).getUserName().equals(args[2])) {
												mu = user.get(i);
												isExist2 = true;
												break;
											}
										}
										if(isExist2) {
											try {
												int pmoney = Integer.parseInt(args[3]);
												p.sendMessage(format("&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�\n&f&l[&a&l!&f&l] &r&b"+ mu.getUserName() + " &6���ΰ��¿� &a" + pmoney + " �� &6�� �۱��Ͽ����ϴ�."));
												mu.AddMoney(pmoney);
												smu.RemoveMoney(pmoney);
												p.sendMessage(format("&f&l[&a&l!&f&l] &r&6��� ������ �ܾ��� &a" + smu.getUserMoney() + " �� &6�Դϴ�.\n&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�"));
												jsonToFile();
											} catch(NumberFormatException e) {
												p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�׼��� ������ �Է��ϼž� �մϴ�!"));
											}
											
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�ش� ���ΰ��°� �������� �ʽ��ϴ�."));
										}
									}
									else if(args[1].equalsIgnoreCase("company")) {
										MoneyAddress mad = null;
										for(int i = 0; i < address.size(); i++) {
											if(address.get(i).getAddressName().equals(args[2])) {
												mad = address.get(i);
												isExist2 = true;
												break;
											}
										}
										if(isExist2) {
											try {
												int pmoney = Integer.parseInt(args[3]);
												p.sendMessage(format("&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�\n&f&l[&a&l!&f&l] &r&b"+ mad.getAddressName() + " &6ȸ����¿� &a" + pmoney + " �� &6�� �۱��Ͽ����ϴ�."));
												mad.AddMoney(pmoney);
												smu.RemoveMoney(pmoney);
												p.sendMessage(format("&f&l[&a&l!&f&l] &r&6��� ������ �ܾ��� &a" + smu.getUserMoney() + " �� &6�Դϴ�.\n&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�"));
												jsonToFile();
											} catch(NumberFormatException e) {
												p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�׼��� ������ �Է��ϼž� �մϴ�!"));
											}
											
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�ش� ȸ����°� �������� �ʽ��ϴ�."));
										}
									}
									else {
										p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�˸��� ��ɾ �Է��Ͻʽÿ�!"));
									}
								}
								else {
									p.sendMessage(format("&f&l[&c&l!&f&l] &r&c����� ���°� �������� �ʽ��ϴ�! ������ �Ͻʽÿ�!"));
								}
								
							}
						}
						else if(args[0].equalsIgnoreCase("check")) {
							if(errorLength(args, 1, s, "/money check", true)) {
								boolean isExist = false;
								MoneyUser mu = null;
								for(int i = 0; i < user.size(); i++) {
									if(user.get(i).getUserUUID().equals(p.getUniqueId())) {
										mu = user.get(i);
										isExist = true;
										break;
									}
								}
								if(isExist) {
									p.sendMessage(format("&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�\n&f&l[&a&l!&f&l] &a&l���¸� : &r&b" + mu.getUserName() + "\n&f&l[&a&l!&f&l] &a&l�� �� : &r&b" + mu.getUserMoney() + " &6��\n&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�"));
								}
								else {
									p.sendMessage(format("&f&l[&c&l!&f&l] &r&c����� ���°� �������� �ʽ��ϴ�! ������ �Ͻʽÿ�!"));
								}
								
							}
						}
						else if(args[0].equalsIgnoreCase("company")) {
							if(errorLength(args, 2, s, "/money company <create|disposal|check|add|remove|send|handover|certificate>", false)) {
								if(args[1].equalsIgnoreCase("check")) {
									if(errorLength(args, 2, s, "/money company check", true)) {
										MoneyAddress mad = null;
										boolean isExist = false;
										for(int i = 0; i < address.size(); i++) {
											if(address.get(i).getAddressChief().getUserUUID().equals(p.getUniqueId())) {
												mad = address.get(i);
												isExist = true;
												break;
											}
											else {
												for(int j = 0; j < address.get(i).getAddressPlayer().size(); j++) {
													if(address.get(i).getAddressPlayer().get(j).getUserUUID().equals(p.getUniqueId())) {
														mad = address.get(i);
														isExist = true;
														break;
													}
												}
											}
										}
										if(isExist) {
											String msg = "&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�\n&f&l[&a&l!&f&l] &a&lȸ�� ���¸� : &r&b" + mad.getAddressName() + "\n&f&l[&a&l!&f&l] &a&l��    �� : &r&b" + mad.getAddressMoney() + "&6��\n&f&l[&a&l!&f&l] &a&l������ ��� : &r\n";
											for(int j = 0; j < mad.getAddressPlayer().size(); j++) {
												msg += "   &r&f&l[&6&l!&f&l] &r&b" + mad.getAddressPlayer().get(j).getUserName() + "\n";
											}
											msg += "&r&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�";
											p.sendMessage(format(msg));
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c����� ȸ���� �����ְ� �ƴմϴ�!"));
										}
									}
								}
								else if(args[1].equalsIgnoreCase("create")) {
									if(errorLength(args, 3, s, "/money company create <ȸ���>", true)) {
										boolean isAlready = false;
										boolean isExist = false;
										for(int i = 0; i < address.size(); i++) {
											if(address.get(i).getAddressChief().getUserUUID().equals(p.getUniqueId())) {
												isAlready = true;
												break;
											}
										}
										if(!isAlready) {
											MoneyUser mu = null;
											for(int i = 0; i < user.size(); i++) {
												if(user.get(i).getUserUUID().equals(p.getUniqueId())) {
													mu = user.get(i);
													isExist = true;
													break;
												}
											}
											if(isExist) {
												if(!(addressList().contains(args[2]))) {
													MoneyAddress mad = new MoneyAddress(args[2], mu);
													address.add(new MoneyAddress(args[2], mu));
													mu.RemoveMoney(500);
													p.sendMessage(format("&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�\n&f&l[&a&l!&f&l] &r&aȸ�簡 �����Ǿ����ϴ�.\n&f&l[&a&l!&f&l] &a&lȸ��� : &r&b" + mad.getAddressName() + "\n&r&f&l[&6&l!&f&l] &e&n�� ȸ�� ���� �� ���� ���¿��� 500���� �����˴ϴ�.\n&r&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�"));
													jsonToFile();
												}
												else {
													p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�̹� �����ϴ� ȸ����Դϴ�! �ٸ� �̸��� ����Ͻʽÿ�!"));
												}
												
											}
											else {
												p.sendMessage(format("&f&l[&c&l!&f&l] &r&c����� ���°� �������� �ʽ��ϴ�! ������ �Ͻʽÿ�!"));
											}
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�̹� ȸ�縦 �����ϰ� �ֽ��ϴ�! ���δ� 1���� ȸ�縸 ���� �����մϴ�!"));
										}
									}
								}
								else if(args[1].equalsIgnoreCase("disposal")) {
									if(errorLength(args, 2, s, "/money company disposal", true)) {
										boolean isExist = false;
										boolean isExist2 = false;
										int num = 0;
										for(int i = 0; i < address.size(); i++) {
											if(address.get(i).getAddressChief().getUserUUID().equals(p.getUniqueId())) {
												isExist = true;
												num = i;
												break;
											}
										}
										if(isExist) {
											MoneyUser mu = null;
											for(int i = 0; i < user.size(); i++) {
												if(user.get(i).getUserUUID().equals(p.getUniqueId())) {
													isExist2 = true;
													mu = user.get(i);
													break;
												}
											}
											if(isExist2) {
												if(p.getInventory().getItemInMainHand().getType() == Material.PAPER && p.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase("Ȯ�μ�")) {
													int dmoney = (int) Math.round(address.get(num).getAddressMoney() * 0.90);
													mu.AddMoney(dmoney);
													p.sendMessage(format("&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�\n&f&l[&c&l!&f&l] &r&cȮ�μ� ������.\n&r&f&l[&c&l!&f&l] &r&cȸ�簡 �Ű��Ǿ����ϴ�.\n&r&f&l[&c&l!&f&l] &r&c&lȸ �� �� : &r&6" + address.get(num).getAddressName() + "\n&r&f&l[&c&l!&f&l] &r&c&l��ǥ �ż��� : &r&6" + dmoney + "&r&c ��\n&r&f&l[&c&l!&f&l] &r&c&nȸ�� �Ű� �� ȸ�� ����� 10%�� �������� ���ε˴ϴ�.\n&r&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�"));
													address.remove(num);
													p.getInventory().clear(p.getInventory().getHeldItemSlot());
													jsonToFile();
												}
												else {
													p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�տ� Ȯ�μ��� ��� ��ɾ ����Ͻʽÿ�!\n&r&f&l[&c&l!&f&l] &r&6/money company certificate &c��ɾ ���� ���� �� �ֽ��ϴ�!"));
												}
											}
											else {
												p.sendMessage(format("&f&l[&c&l!&f&l] &r&c����� ���°� �������� �ʽ��ϴ�! ������ �Ͻʽÿ�!"));
											}
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c����� ȸ���� �����ְ� �ƴմϴ�!"));
										}
									}
								}
								else if(args[1].equalsIgnoreCase("add")) {
									if(errorLength(args, 3, s, "/money company add <����ڸ�>", true)) {
										MoneyAddress mad = null;
										boolean isExist = false;
										boolean isExist2 = false;
										boolean isAlready = false;
										for(int i = 0; i < address.size(); i++) {
											if(address.get(i).getAddressChief().getUserUUID().equals(p.getUniqueId())) {
												mad = address.get(i);
												isExist = true;
												break;
											}
										}
										for(int i = 0; i < address.size(); i++) {
											for(int j = 0; j < address.get(i).getAddressPlayer().size(); j++) {
												if(address.get(i).getAddressPlayer().get(j).getUserName().equals(args[2])) {
													isAlready = true;
													break;
												}
											}
										}
										if(isExist) {
											MoneyUser smu = null;
											for(int i = 0; i < user.size(); i++) {
												if(user.get(i).getUserName().equals(args[2])) {
													smu = user.get(i);
													isExist2 = true;
													break;
												}
											}
											if(isExist2) {
												if(!(mad.getAddressPlayer().contains(smu))) {
													if(!isAlready) {
														mad.AddUser(smu);
														p.sendMessage(format("&f&l[&a&l!&f&l] &6" + smu.getUserName() + " &a��(��) ȸ�� �����ڷ� �Ӹ��߽��ϴ�."));
														jsonToFile();
													}
													else {
														p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�ش� ����ڴ� �̹� �ٸ� ȸ���� �������Դϴ�."));
													}
												}
												else {
													p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�ش� ����ڴ� �̹� �������Դϴ�."));
												}
											}
											else {
												p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�ش� ���ΰ��°� �������� �ʽ��ϴ�."));
											}
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c����� ȸ���� �����ְ� �ƴմϴ�!"));
										}
									}
								}
								else if(args[1].equalsIgnoreCase("remove")) {
									if(errorLength(args, 3, s, "/money company remove <����ڸ�>", true)) {
										MoneyAddress mad = null;
										boolean isExist = false;
										boolean isExist2 = false;
										for(int i = 0; i < address.size(); i++) {
											if(address.get(i).getAddressChief().getUserUUID().equals(p.getUniqueId())) {
												mad = address.get(i);
												isExist = true;
												break;
											}
										}
										if(isExist) {
											MoneyUser smu = null;
											for(int i = 0; i < user.size(); i++) {
												if(user.get(i).getUserName().equals(args[2])) {
													smu = user.get(i);
													isExist2 = true;
													break;
												}
											}
											if(isExist2) {
												if(!(smu.getUserUUID().equals(p.getUniqueId()))) {
													if(mad.DeleteUser(smu)) {
														p.sendMessage(format("&f&l[&c&l!&f&l] &6" + smu.getUserName() + " &c��(��) ȸ�� �����ڿ��� ��Ż�߽��ϴ�."));
														jsonToFile();
													}
													else {
														p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�ش� ����ڴ� ��� ȸ���� ȸ�� �����ڰ� �ƴմϴ�."));
													}	
												}
												else {
													p.sendMessage(format("&f&l[&c&l!&f&l] &r&c������ ��Ͽ��� ��ǥ�ڸ� ������ �� �����ϴ�."));
												}
											}
											else {
												p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�ش� ���ΰ��°� �������� �ʽ��ϴ�."));
											}
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c����� ȸ���� �����ְ� �ƴմϴ�!"));
										}
									}
								}
								else if(args[1].equalsIgnoreCase("send")) {
									if(errorLength(args, 5, s, "/money company send <company|user> <ȸ���|����ڸ�> <�׼�>", true)) {
										boolean isExist1 = false;
										boolean isExist2 = false;
										MoneyAddress mad = null;
										for(int i = 0; i < address.size(); i++) {
											if(address.get(i).getAddressChief().getUserUUID().equals(p.getUniqueId())) {
												mad = address.get(i);
												isExist1 = true;
												break;
											}
										}
										if(isExist1) {
											if(args[2].equalsIgnoreCase("user")) {
												MoneyUser mu = null;
												for(int i = 0; i < user.size(); i++) {
													if(user.get(i).getUserName().equals(args[3])) {
														mu = user.get(i);
														isExist2 = true;
														break;
													}
												}
												if(isExist2) {
													try {
														int pmoney = Integer.parseInt(args[4]);
														p.sendMessage(format("&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�\n&f&l[&a&l!&f&l] &r&b"+ mu.getUserName() + " &6���ΰ��¿� &a" + pmoney + " �� &6�� �۱��Ͽ����ϴ�."));
														mu.AddMoney(pmoney);
														mad.RemoveMoney(pmoney);
														p.sendMessage(format("&f&l[&a&l!&f&l] &r&b" + mad.getAddressName() +  " &6ȸ������� �ܾ��� &a" + mad.getAddressMoney() + " �� &6�Դϴ�.\n&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�"));
														jsonToFile();
													} catch(NumberFormatException e) {
														p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�׼��� ������ �Է��ϼž� �մϴ�!"));
													}
													
												}
												else {
													p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�ش� ���ΰ��°� �������� �ʽ��ϴ�."));
												}
											}
											else if(args[2].equalsIgnoreCase("company")) {
												MoneyAddress mad2 = null;
												for(int i = 0; i < address.size(); i++) {
													if(address.get(i).getAddressName().equals(args[3])) {
														mad2 = address.get(i);
														isExist2 = true;
														break;
													}
												}
												if(isExist2) {
													try {
														int pmoney = Integer.parseInt(args[4]);
														p.sendMessage(format("&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�\n&f&l[&a&l!&f&l] &r&b"+ mad2.getAddressName() + " &6ȸ����¿� &a" + pmoney + " �� &6�� �۱��Ͽ����ϴ�."));
														mad2.AddMoney(pmoney);
														mad.RemoveMoney(pmoney);
														p.sendMessage(format("&f&l[&a&l!&f&l] &r&6��� ȸ������� �ܾ��� &a" + mad.getAddressMoney() + " �� &6�Դϴ�.\n&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�"));
														jsonToFile();
													} catch(NumberFormatException e) {
														p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�׼��� ������ �Է��ϼž� �մϴ�!"));
													}
													
												}
												else {
													p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�ش� ȸ����°� �������� �ʽ��ϴ�."));
												}
											}
											else {
												p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�˸��� ��ɾ �Է��Ͻʽÿ�!"));
											}
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c����� ���°� �������� �ʽ��ϴ�! ������ �Ͻʽÿ�!"));
										}
									}
								}
								else if(args[1].equalsIgnoreCase("handover")) {
									if(errorLength(args, 3, s, "/money company handover <����ڸ�>", true)) {
										MoneyAddress mad = null;
										MoneyUser mu = null;
										Player sp = null;
										boolean isExist = false;
										boolean isExist2 = false;
										boolean isInServer = false;
										for(int i = 0; i < address.size(); i++) {
											if(address.get(i).getAddressChief().getUserUUID().equals(p.getUniqueId())) {
												mad = address.get(i);
												isExist = true;
												break;
											}
										}
										if(isExist) {
											for(int i = 0; i < mad.getAddressPlayer().size(); i++) {
												if(mad.getAddressPlayer().get(i).getUserName().equals(args[2])) {
													mu = mad.getAddressPlayer().get(i);
													isExist2 = true;
													break;
												}
											}
											if(isExist2) {
												for(int i = 0; i < playerList().size(); i++) {
													if(playerList().get(i).getName().equals(args[2])) {
														isInServer = true;
														sp = playerList().get(i);
														break;
													}
												}
												if(isInServer) {
													if((p.getInventory().getItemInMainHand().getType() == Material.PAPER && p.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase("Ȯ�μ�")) && (sp.getInventory().getItemInMainHand().getType() == Material.PAPER && sp.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase("Ȯ�μ�"))) {
														p.sendMessage(format("&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�\n&f&l[&c&l!&f&l] &r&6" + mad.getAddressName() + " &cȸ���� ��ǥ ������ &6" + sp.getName() + " &c���Է� �絵�߽��ϴ�.\n&r&f&l[&c&l!&f&l] &r&c&nȸ�� ��ǥ�� �絵 �� ȸ�� ����� 5%�� �������� ���ε˴ϴ�.\n&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�"));
														sp.sendMessage(format("&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�\n&f&l[&c&l!&f&l] &r&6" + mad.getAddressName() + " &cȸ���� ��ǥ ������ ��ſ��� �絵�Ǿ����ϴ�.\n&r&f&l[&c&l!&f&l] &r&c&nȸ�� ��ǥ�� �絵 �� ȸ�� ����� 5%�� �������� ���ε˴ϴ�.\n&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�"));
														p.getInventory().clear(p.getInventory().getHeldItemSlot());
														sp.getInventory().clear(sp.getInventory().getHeldItemSlot());
														int dmoney = (int) Math.round(mad.getAddressMoney() * 0.05);
														mad.RemoveMoney(dmoney);
														mad.ChangeAddressChief(mu);
														jsonToFile();
													}
													else {
														p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�絵�ڿ� ����� ��� �տ� �������� ��� �־�� �մϴ�!\n&r&f&l[&c&l!&f&l] &r&6/money company certificate &c��ɾ ���� ���� �� �ֽ��ϴ�!"));
													}
												}
												else {
													p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�絵 ����ڰ� ���� ������ �����ؾ� �մϴ�!"));
												}
											}
											else {
												p.sendMessage(format("&f&l[&c&l!&f&l] &r&c��ǥ ���� �絵�� ȸ�� ���� �����ڿ��Ը� �� �� �ֽ��ϴ�!"));
											}
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c����� ȸ���� �����ְ� �ƴմϴ�!"));
										}
									}
								}
								else if(args[1].equalsIgnoreCase("certificate")) {
									if(errorLength(args, 2, s, "/money company certificate", true)) {
										List<String> itemLore = new ArrayList<String>();
										itemLore.add(format("&cȸ�� �Ű� �� �ʿ��� Ȯ�μ��Դϴ�."));
										itemLore.add(format("&c�����տ� ��� ����Ͻʽÿ�."));
										ItemStack stack = new ItemStack(Material.PAPER);
										ItemMeta meta = stack.getItemMeta();
										meta.setDisplayName("Ȯ�μ�");
										meta.setLore(itemLore);
										stack.setItemMeta(meta);
										p.getInventory().setItemInOffHand(stack);
										p.sendMessage(format("&f&l[&a&l!&f&l] &a�޼տ� ȸ�� �Ű� Ȯ�μ��� ���޵Ǿ����ϴ�."));
									}
								}
								else {
									p.sendMessage(format("&f&l[&c&l!&f&l] &r&c�˸��� ��ɾ �Է��Ͻʽÿ�!"));
								}
							}
						}
					}
					else {
						s.sendMessage(format("&f&l[&c&l!&f&l] &r&6/money " + args[0] + " &c��ɾ�� �÷��̾ ����� �����մϴ�."));
					}
				}
				else {
					if(args[0].equalsIgnoreCase("help")) {
						if(errorLength(args, 1, s, "/money help", true)) {
							s.sendMessage(format("&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�\n&r&f&l[&a&l!&f&l] &r&b/money add <company|user> <ȸ���|����ڸ�> <�׼�>\n   &6���ش� ȸ�糪 ����ڿ��� ���� �����մϴ�.\n&r&f&l[&a&l!&f&l] &r&b/money subtract <company|user> <ȸ���|����ڸ�> <�׼�>\n   &6���ش� ȸ�糪 ����ڿ��� ���� �����մϴ�.\n&r&f&l[&a&l!&f&l] &r&b/money give <company|user> <ȸ���1|����ڸ�1> <company|user> <ȸ���2|����ڸ�2> <�׼�>\n   &6��[1]�� ���·κ��� [2]�� ���·� ���� �����ϴ�.\n&r&f&l[&a&l!&f&l] &r&b/money list <company|user>\n   &6�ü����� �����ϴ� ��� ���ΰ��³� ȸ����¸� Ȯ���մϴ�.\n&r&f&l[&a&l!&f&l] &r&b/money all\n   &6�ü����� �����ϴ� �� ��ȭ���� Ȯ���մϴ�.\n&r&6&l-----------------------\n&r&f&l[&9&l!&f&l] &r&b/money check\n   &6�ú��� ������ �ܾ� ��Ȳ�� Ȯ���մϴ�.\n&r&f&l[&9&l!&f&l] &r&b/money send <company|user> <ȸ���|����ڸ�> <�׼�>\n   &6�ú��� ���¿��� �ش� ȸ�糪 ����ڿ��� ���� �۱��մϴ�.\n&r&f&l[&9&l!&f&l] &r&b/money company <create|disposal|check|add|remove|send|handover|certificate>\n   &r&f&l[&6&l!&f&l] &r&b/money company create <ȸ���>\n      &6�ú��� ������ ȸ�縦 �����մϴ�.\n       &c�غ��� ������ ȸ��� 1���� ���� �����մϴ�.\n   &r&f&l[&6&l!&f&l] &r&b/money company disposal\n      &6�ú��� ������ ȸ�縦 ���ο� �Ű��մϴ�. &r&d&l��\n       &c�غ� ��ɾ� ��� �� ȸ�� �ں��� 10%�� �������� ���ε˴ϴ�.\n   &r&f&l[&6&l!&f&l] &r&b/money company add <����ڸ�>\n      &6���ش� ����ڸ� ȸ�� ������ ���������� �Ӹ��մϴ�.\n       &6�ذ������� /money company check ������ ������ �����ϴ�.\n   &r&f&l[&6&l!&f&l] &r&b/money company remove <����ڸ�>\n      &6���ش� ����ڸ� ȸ�� ������ �����ο��� ��Ż�մϴ�.\n   &r&f&l[&6&l!&f&l] &r&b/money company send <company|user> <ȸ���|����ڸ�> <�׼�>\n      &6�ú��� ȸ�� ���¿��� �ش� ȸ�糪 ����ڿ��� ���� �۱��մϴ�.\n   &r&f&l[&6&l!&f&l] &r&b/money company check\n      &6�ú��� ���� ȸ���� ��Ȳ�� Ȯ���մϴ�.\n   &r&f&l[&6&l!&f&l] &r&b/money company handover <����ڸ�>\n      &6���ش� ����ڿ��� ���� ȸ���� ��ǥ���� �絵�մϴ�. &r&d&l��\n       &r&c���ش� ��ɾ� ��� �� ȸ�� �ں��� 5%�� �������� ���ε˴ϴ�.\n       &c�ؾ絵 ����ڴ� �ش� ȸ���� �������̾�� �մϴ�.\n   &r&f&l[&6&l!&f&l] &r&b/money company ceftificate\n      &6��ȸ�� �Ű��� �ʿ��� �������� �߱��մϴ�.\n&r&f&l[&7&l!&f&l] &r&b/money help\n   &6�ú� ����� ǥ���մϴ�.\n\n&r&f&l[&a&l!&f&l] &r&f���� �ܼ� ����     &r&f&l[&9&l!&f&l] &r&f�÷��̾� ����\n&r&f&l[&6&l!&f&l] &r&f���� ���     &r&f&l[&7&l!&f&l] &r&f����     &r&d&l�� &r&f�Ű� ������ �ʿ�\n&r&2�ɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢɢ�"));
						}
					}
				}
			}
		}
		return true;
	}
	
	
	private boolean contains(String[] str, String str1) {
		return Arrays.stream(str).anyMatch(str1::equals);
	}
	
	private boolean errorLength(String args[], int i, CommandSender s, String usage, boolean more) {
		if(args.length < i) {
			String args1[] = usage.split(" ");
			List<String> cmd = new ArrayList<String>();
			for(int j = 0; j < args1.length; j++) {
				if(j > args.length) {
					cmd.add("&r&6&l" + args1[j]);
				}
				else {
					cmd.add("&r&7" + args1[j]);
				}
			}
			s.sendMessage(format("&c&l������ ��ɾ�:\n&r" + String.join(" ", cmd)));
			return false;
		}
		else if(args.length > i && more) {
			String args1[] = usage.split(" ");
			List<String> args2 = new ArrayList<>(Arrays.asList(args));
			args2.add(0, args1[0]);
			List<String> cmd = new ArrayList<String>();
			for(int j = 0; j < args2.size(); j++) {
				if(j > args1.length - 1) {
					cmd.add("&r&c&l" + args2.get(j));
				}
				else {
					cmd.add("&r&7" + args2.get(j));
				}
			}
			s.sendMessage(format("&c&l�߸��� ��ɾ�:\n&r" + String.join(" ", cmd)));
			return false;
		}
		else return true;
	}
	
//---------------<TAB>--------------------------------------------------
	List<String> arguments = new ArrayList<String>();
	
	@Override
	public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] args) {
		if(label.equalsIgnoreCase("money")) {
			if(args.length == 1) {
				String str1[] = {"all", "add", "subtract", "give", "send", "check", "company", "list","help"};
				arguments = new ArrayList<>(Arrays.asList(str1));
			}
			else {
				if(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("subtract") || args[0].equalsIgnoreCase("send")) {
					if(args.length == 2) {
						String str2[] = {"user", "company"};
						arguments = new ArrayList<>(Arrays.asList(str2));
					}
					else {
						if(args.length == 3) {
							if(args[1].equalsIgnoreCase("user")) {
								arguments = userList();
							}
							else if(args[1].equalsIgnoreCase("company")) {
								arguments = addressList();
							}
						}
						else {
							arguments.clear();
						}
					}
				}
				else if(args[0].equalsIgnoreCase("give")) {
					if(args.length == 2 || args.length == 4) {
						String str3[] = {"user", "company"};
						arguments = new ArrayList<>(Arrays.asList(str3));
					}
					else {
						if(args.length == 3 || args.length == 5) {
							if(args[args.length - 2].equalsIgnoreCase("user")) {
								arguments = userList();
							}
							else if(args[args.length - 2].equalsIgnoreCase("company")) {
								arguments = addressList();
							}
							else {
								arguments.clear();
							}
						}
						else {
							arguments.clear();
						}
					}
				}
				else if(args[0].equalsIgnoreCase("list")) {
					if(args.length == 2) {
						String str4[] = {"user", "company"};
						arguments = new ArrayList<>(Arrays.asList(str4));
					}
					else {
						arguments.clear();
					}
				}
				else if(args[0].equalsIgnoreCase("company")) {
					if(s instanceof Player) {
						Player p = (Player) s;
						if(args.length == 2) {
							String str5[] = {"create", "disposal", "check", "add", "remove", "send", "handover", "certificate"};
							arguments = new ArrayList<>(Arrays.asList(str5));
						}
						else {
							if(args.length == 3 && (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("handover"))) {
								for(int i = 0; i < address.size(); i++) {
									if(address.get(i).getAddressChief().getUserUUID().equals(p.getUniqueId())) {
										arguments.clear();
										for(int j = 0; j < address.get(i).getAddressPlayer().size(); j++) {
											if(!(address.get(i).getAddressPlayer().get(j).getUserUUID().equals(p.getUniqueId()))) {
												arguments.add(address.get(i).getAddressPlayer().get(j).getUserName());
											}
											else;
										}
										break;
									}
								}
							}
							else if(args[1].equalsIgnoreCase("send")) {
								if(args.length == 3) {
									String str2[] = {"user", "company"};
									arguments = new ArrayList<>(Arrays.asList(str2));
								}
								else {
									if(args.length == 4) {
										if(args[2].equalsIgnoreCase("user")) {
											arguments = userList();
										}
										else if(args[2].equalsIgnoreCase("company")) {
											arguments = addressList();
										}
									}
									else {
										arguments.clear();
									}
								}
							}
							else if(args.length == 3 && args[1].equalsIgnoreCase("add")) {
								arguments = userList();
								for(int i = 0; i < address.size(); i++) {
									if(address.get(i).getAddressChief().getUserUUID().equals(p.getUniqueId())) {
										for(int j = 0; j < address.get(i).getAddressPlayer().size(); j++) {
											for(int k = 0; k < userList().size(); k++) {
												if(userList().get(k).equals(address.get(i).getAddressPlayer().get(j).getUserName())) {
													arguments.remove(k);
												}
												else;
											}
										}
										break;
									}
								}
							}
							else {
								arguments.clear();
							}
						}
					}
					else {
						arguments.clear();
					}
				}
			}
		}
		
		List<String> result = new ArrayList<String>();
		
		for(String a : arguments) {
			if(a.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
				result.add(a);
			}
		}
		return result;
		
	}
}