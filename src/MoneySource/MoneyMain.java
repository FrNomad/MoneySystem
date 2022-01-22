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
		System.out.println(NoticeChatcolor + "[MoneyManager] 본 버전은 스냅샷입니다. 오류가 발생할 수 있습니다. 오류가 있을 시 디스코드 FrNomad#2821로 문의해주세요.");
		System.out.println(NoticeChatcolor + "[MoneyManager] 제작자 : FrNomad");
		System.out.println(NoticeChatcolor + "[MoneyManager] " + pdfile.getName() + " " + pdfile.getVersion() + " 버전이 실행 완료됨.");
		System.out.println(NoticeChatcolor + "==================================================");
	}
	
	@Override
	public void onDisable() {
		PluginDescriptionFile pdfile = this.getDescription();
		System.out.println(NoticeChatcolor + "==================================================");
		jsonToFile();
		System.out.println(NoticeChatcolor + "[MoneyManager] " + pdfile.getName() + " " + pdfile.getVersion() + " 버전이 꺼짐.");
		System.out.println(NoticeChatcolor + "==================================================");
	}
	
	public void createFile(File f) {
		if(!f.exists() || !f.isFile()) {
			try {
				f.createNewFile();
				System.out.println(NoticeChatcolor + "[MoneyManager]" + f.getName() + " 파일이 새로 생성되었습니다.");
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
				s.sendMessage(ChatColor.GOLD + "명령어 사용 방법: \n" + ChatColor.RED + "/money <list|check|send|company|add|subtract|give|all>");
			}
			else {
				if(contains(consoleCmd, args[0])) {
					if(!(s instanceof Player)) {
						if(args[0].equalsIgnoreCase("add")) {          //money add
							if(errorLength(args, 4, s, "/money add <company|user> <회사|사용자명> <액수>", true)) {
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
											s.sendMessage(format("&f&l[&a&l!&f&l] &r&6"+ mu.getUserName() + " 개인계좌에 &a" + pmoney + " 원 &6이 지급되었습니다."));
											mu.AddMoney(pmoney);
											s.sendMessage(format("&f&l[&a&l!&f&l] &r&6잔액은 &a" + mu.getUserMoney() + " 원 &6입니다."));
											jsonToFile();
										} catch(NumberFormatException e) {
											s.sendMessage(format("&f&l[&c&l!&f&l] &r&c액수는 정수로 입력하셔야 합니다!"));
										}
										
									}
									else {
										s.sendMessage(format("&f&l[&c&l!&f&l] &r&c해당 개인계좌가 존재하지 않습니다."));
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
											s.sendMessage(format("&f&l[&a&l!&f&l] &r&6"+ mad.getAddressName() + " 회사계좌에 &a" + pmoney + " 원 &6이 지급되었습니다."));
											mad.AddMoney(pmoney);
											s.sendMessage(format("&f&l[&a&l!&f&l] &r&6잔액은 &a" + mad.getAddressMoney() + " 원 &6입니다."));
											jsonToFile();
										} catch(NumberFormatException e) {
											s.sendMessage(format("&f&l[&c&l!&f&l] &r&c액수는 정수로 입력하셔야 합니다!"));
										}
										
									}
									else {
										s.sendMessage(format("&f&l[&c&l!&f&l] &r&c해당 회사계좌가 존재하지 않습니다."));
									}
								}
								else {
									s.sendMessage(format("&f&l[&c&l!&f&l] &r&c알맞은 명령어를 입력하십시오!"));
								}
							}
						}
						else if(args[0].equalsIgnoreCase("subtract")) {          //money subtract
							if(errorLength(args, 4, s, "/money subtract <company|user> <회사|사용자명> <액수>", true)) {
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
											s.sendMessage(format("&f&l[&a&l!&f&l] &r&6"+ mu.getUserName() + " 개인계좌에 &c" + pmoney + " 원 &6이 차감되었습니다."));
											mu.RemoveMoney(pmoney);
											s.sendMessage(format("&f&l[&a&l!&f&l] &r&6잔액은 &a" + mu.getUserMoney() + " 원 &6입니다."));
											jsonToFile();
										} catch(NumberFormatException e) {
											s.sendMessage(format("&f&l[&c&l!&f&l] &r&c액수는 정수로 입력하셔야 합니다!"));
										}
										
									}
									else {
										s.sendMessage(format("&f&l[&c&l!&f&l] &r&c해당 개인계좌가 존재하지 않습니다."));
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
											s.sendMessage(format("&f&l[&a&l!&f&l] &r&6"+ mad.getAddressName() + " 회사계좌에 &c" + pmoney + " 원 &6이 차감되었습니다."));
											mad.RemoveMoney(pmoney);
											s.sendMessage(format("&f&l[&a&l!&f&l] &r&6잔액은 &a" + mad.getAddressMoney() + " 원 &6입니다."));
											jsonToFile();
										} catch(NumberFormatException e) {
											s.sendMessage(format("&f&l[&c&l!&f&l] &r&c액수는 정수로 입력하셔야 합니다!"));
										}
										
									}
									else {
										s.sendMessage(format("&f&l[&c&l!&f&l] &r&c해당 회사계좌가 존재하지 않습니다."));
									}
								}
								else {
									s.sendMessage(format("&f&l[&c&l!&f&l] &r&c알맞은 명령어를 입력하십시오!"));
								}
							}
						}
						else if(args[0].equalsIgnoreCase("give")) {         //money give
							if(errorLength(args, 6, s, "/money give <company|user> <회사1|사용자명1> <company|user> <회사2|사용자명2> <액수>", true)) {
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
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&c"+ mu.getUserName() + " &6개인계좌에서 &a" + mu2.getUserName() + " &6개인계좌로 &a" + pmoney + " 원 &6이 이동되었습니다."));
												mu.RemoveMoney(pmoney);
												mu2.AddMoney(pmoney);
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&c" + mu.getUserName() + " &6개인계좌 잔액은 &a" + mu.getUserMoney() + " 원 &6입니다."));
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&a" + mu2.getUserName() + " &6개인계좌 잔액은 &a" + mu2.getUserMoney() + " 원 &6입니다."));
												jsonToFile();
											} catch(NumberFormatException e) {
												s.sendMessage(format("&f&l[&c&l!&f&l] &r&c액수는 정수로 입력하셔야 합니다!"));
											}
										}
										else {
											s.sendMessage(format("&f&l[&c&l!&f&l] &r&c해당 회사계좌 또는 개인계좌가 존재하지 않습니다."));
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
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&c"+ mu.getUserName() + " &6개인계좌에서 &a" + mad2.getAddressName() + " &6회사계좌로 &a" + pmoney + " 원 &6이 이동되었습니다."));
												mu.RemoveMoney(pmoney);
												mad2.AddMoney(pmoney);
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&c" + mu.getUserName() + " &6개인계좌 잔액은 &a" + mu.getUserMoney() + " 원 &6입니다."));
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&a" + mad2.getAddressName() + " &6회사계좌 잔액은 &a" + mad2.getAddressMoney() + " 원 &6입니다."));
												jsonToFile();
											} catch(NumberFormatException e) {
												s.sendMessage(format("&f&l[&c&l!&f&l] &r&c액수는 정수로 입력하셔야 합니다!"));
											}
										}
										else {
											s.sendMessage(format("&f&l[&c&l!&f&l] &r&c해당 회사계좌 또는 개인계좌가 존재하지 않습니다."));
										}
									}
									else {
										s.sendMessage(format("&f&l[&c&l!&f&l] &r&c알맞은 명령어를 입력하십시오!"));
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
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&c"+ mu2.getUserName() + " &6회사계좌에서 &a" + mad.getAddressName() + " &6개인계좌로 &a" + pmoney + " 원 &6이 이동되었습니다."));
												mad.RemoveMoney(pmoney);
												mu2.AddMoney(pmoney);
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&c" + mu2.getUserName() + " &6회사계좌 잔액은 &a" + mu2.getUserMoney() + " 원 &6입니다."));
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&a" + mad.getAddressName() + " &6개인계좌 잔액은 &a" + mad.getAddressMoney() + " 원 &6입니다."));
												jsonToFile();
											} catch(NumberFormatException e) {
												s.sendMessage(format("&f&l[&c&l!&f&l] &r&c액수는 정수로 입력하셔야 합니다!"));
											}
										}
										else {
											s.sendMessage(format("&f&l[&c&l!&f&l] &r&c해당 회사계좌 또는 개인계좌가 존재하지 않습니다."));
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
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&c"+ mad.getAddressName() + " &6회사계좌에서 &a" + mad2.getAddressName() + " &6회사계좌로 &a" + pmoney + " 원 &6이 이동되었습니다."));
												mad.RemoveMoney(pmoney);
												mad2.AddMoney(pmoney);
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&c" + mad.getAddressName() + " &6회사계좌 잔액은 &a" + mad.getAddressMoney() + " 원 &6입니다."));
												s.sendMessage(format("&f&l[&a&l!&f&l] &r&a" + mad2.getAddressName() + " &6회사계좌 잔액은 &a" + mad2.getAddressMoney() + " 원 &6입니다."));
												jsonToFile();
											} catch(NumberFormatException e) {
												s.sendMessage(format("&f&l[&c&l!&f&l] &r&c액수는 정수로 입력하셔야 합니다!"));
											}
										}
										else {
											s.sendMessage(format("&f&l[&c&l!&f&l] &r&c해당 회사계좌 또는 개인계좌가 존재하지 않습니다."));
										}
									}
									else {
										s.sendMessage(format("&f&l[&c&l!&f&l] &r&c알맞은 명령어를 입력하십시오!"));
									}
									
								}
								else {
									s.sendMessage(format("&f&l[&c&l!&f&l] &r&c알맞은 명령어를 입력하십시오!"));
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
								s.sendMessage(format("&f&l[&a&l!&f&l] &r&6본 서버의 총 통화량은 &a" + allmoney + " 원 &6입니다."));
							}
						}
						else if(args[0].equalsIgnoreCase("list")) {
							if(errorLength(args, 2, s, "/money list <company|user>", true)) {
								String allstr = "&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨\n";
								boolean isValid = false;
								if(args[1].equalsIgnoreCase("user")) {
									isValid = true;
									allstr += "&f&l[&a&l!&f&l] &r&a[사용자 목록]\n";
									for(int i = 0; i < user.size(); i++) {
										allstr += "&f&l[&a&l!&f&l] &r&a&l" + user.get(i).getUserName() + "   &r&7-------------   &a" + user.get(i).getUserMoney() + " &6원\n";
									}
								}
								else if(args[1].equalsIgnoreCase("company")) {
									isValid = true;
									allstr += "&f&l[&a&l!&f&l] &r&a[회사 목록]\n";
									for(int i = 0; i < address.size(); i++) {
										allstr += "&f&l[&a&l!&f&l] &r&a&l" + address.get(i).getAddressName() + "   &r&7-------------   &a" + address.get(i).getAddressMoney() + " &6원\n   &f&l[&6&l!&f&l] &r&a&l[소유자] : &a" + address.get(i).getAddressChief().getUserName() + "\n   &r&f&l[&6&l!&f&l] &r&a&l[관리자] : \n";
										for(int j = 0; j < address.get(i).getAddressPlayer().size(); j++) {
											allstr += "      &r&f&l[&6&l!&f&l] &r&a&l" + address.get(i).getAddressPlayer().get(j).getUserName() + "\n";
										}
									}
								}
								else {
									s.sendMessage(format("&f&l[&c&l!&f&l] &r&c알맞은 명령어를 입력하십시오!"));
								}
								if(isValid) {
									allstr += "&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨";
									s.sendMessage(format(allstr));
								}
								else;
							}
						}
						else;
					}
					else {
						s.sendMessage(format("&f&l[&c&l!&f&l] &r&6/money " + args[0] + " &c명령어는 서버 콘솔에서만 사용이 가능합니다."));
					}
				}
				else if(contains(playerCmd, args[0])) {
					if(s instanceof Player) {
						Player p = (Player) s;
						if(args[0].equalsIgnoreCase("send")) {
							if(errorLength(args, 4, s, "/money send <company|user> <회사|사용자명> <액수>", true)) {
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
												p.sendMessage(format("&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨\n&f&l[&a&l!&f&l] &r&b"+ mu.getUserName() + " &6개인계좌에 &a" + pmoney + " 원 &6을 송금하였습니다."));
												mu.AddMoney(pmoney);
												smu.RemoveMoney(pmoney);
												p.sendMessage(format("&f&l[&a&l!&f&l] &r&6당신 계좌의 잔액은 &a" + smu.getUserMoney() + " 원 &6입니다.\n&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨"));
												jsonToFile();
											} catch(NumberFormatException e) {
												p.sendMessage(format("&f&l[&c&l!&f&l] &r&c액수는 정수로 입력하셔야 합니다!"));
											}
											
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c해당 개인계좌가 존재하지 않습니다."));
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
												p.sendMessage(format("&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨\n&f&l[&a&l!&f&l] &r&b"+ mad.getAddressName() + " &6회사계좌에 &a" + pmoney + " 원 &6을 송금하였습니다."));
												mad.AddMoney(pmoney);
												smu.RemoveMoney(pmoney);
												p.sendMessage(format("&f&l[&a&l!&f&l] &r&6당신 계좌의 잔액은 &a" + smu.getUserMoney() + " 원 &6입니다.\n&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨"));
												jsonToFile();
											} catch(NumberFormatException e) {
												p.sendMessage(format("&f&l[&c&l!&f&l] &r&c액수는 정수로 입력하셔야 합니다!"));
											}
											
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c해당 회사계좌가 존재하지 않습니다."));
										}
									}
									else {
										p.sendMessage(format("&f&l[&c&l!&f&l] &r&c알맞은 명령어를 입력하십시오!"));
									}
								}
								else {
									p.sendMessage(format("&f&l[&c&l!&f&l] &r&c당신의 계좌가 존재하지 않습니다! 재접속 하십시오!"));
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
									p.sendMessage(format("&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨\n&f&l[&a&l!&f&l] &a&l계좌명 : &r&b" + mu.getUserName() + "\n&f&l[&a&l!&f&l] &a&l잔 액 : &r&b" + mu.getUserMoney() + " &6원\n&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨"));
								}
								else {
									p.sendMessage(format("&f&l[&c&l!&f&l] &r&c당신의 계좌가 존재하지 않습니다! 재접속 하십시오!"));
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
											String msg = "&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨\n&f&l[&a&l!&f&l] &a&l회사 계좌명 : &r&b" + mad.getAddressName() + "\n&f&l[&a&l!&f&l] &a&l잔    액 : &r&b" + mad.getAddressMoney() + "&6원\n&f&l[&a&l!&f&l] &a&l관리자 목록 : &r\n";
											for(int j = 0; j < mad.getAddressPlayer().size(); j++) {
												msg += "   &r&f&l[&6&l!&f&l] &r&b" + mad.getAddressPlayer().get(j).getUserName() + "\n";
											}
											msg += "&r&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨";
											p.sendMessage(format(msg));
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c당신은 회사의 소유주가 아닙니다!"));
										}
									}
								}
								else if(args[1].equalsIgnoreCase("create")) {
									if(errorLength(args, 3, s, "/money company create <회사명>", true)) {
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
													p.sendMessage(format("&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨\n&f&l[&a&l!&f&l] &r&a회사가 생성되었습니다.\n&f&l[&a&l!&f&l] &a&l회사명 : &r&b" + mad.getAddressName() + "\n&r&f&l[&6&l!&f&l] &e&n※ 회사 생성 시 개인 계좌에서 500원이 차감됩니다.\n&r&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨"));
													jsonToFile();
												}
												else {
													p.sendMessage(format("&f&l[&c&l!&f&l] &r&c이미 존재하는 회사명입니다! 다른 이름을 사용하십시오!"));
												}
												
											}
											else {
												p.sendMessage(format("&f&l[&c&l!&f&l] &r&c당신의 계좌가 존재하지 않습니다! 재접속 하십시오!"));
											}
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c이미 회사를 소유하고 있습니다! 개인당 1개의 회사만 소유 가능합니다!"));
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
												if(p.getInventory().getItemInMainHand().getType() == Material.PAPER && p.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase("확인서")) {
													int dmoney = (int) Math.round(address.get(num).getAddressMoney() * 0.90);
													mu.AddMoney(dmoney);
													p.sendMessage(format("&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨\n&f&l[&c&l!&f&l] &r&c확인서 인증됨.\n&r&f&l[&c&l!&f&l] &r&c회사가 매각되었습니다.\n&r&f&l[&c&l!&f&l] &r&c&l회 사 명 : &r&6" + address.get(num).getAddressName() + "\n&r&f&l[&c&l!&f&l] &r&c&l대표 매수액 : &r&6" + dmoney + "&r&c 원\n&r&f&l[&c&l!&f&l] &r&c&n회사 매각 시 회사 재산의 10%는 세금으로 납부됩니다.\n&r&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨"));
													address.remove(num);
													p.getInventory().clear(p.getInventory().getHeldItemSlot());
													jsonToFile();
												}
												else {
													p.sendMessage(format("&f&l[&c&l!&f&l] &r&c손에 확인서를 들고 명령어를 사용하십시오!\n&r&f&l[&c&l!&f&l] &r&6/money company certificate &c명령어를 통해 얻을 수 있습니다!"));
												}
											}
											else {
												p.sendMessage(format("&f&l[&c&l!&f&l] &r&c당신의 계좌가 존재하지 않습니다! 재접속 하십시오!"));
											}
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c당신은 회사의 소유주가 아닙니다!"));
										}
									}
								}
								else if(args[1].equalsIgnoreCase("add")) {
									if(errorLength(args, 3, s, "/money company add <사용자명>", true)) {
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
														p.sendMessage(format("&f&l[&a&l!&f&l] &6" + smu.getUserName() + " &a을(를) 회사 관리자로 임명했습니다."));
														jsonToFile();
													}
													else {
														p.sendMessage(format("&f&l[&c&l!&f&l] &r&c해당 사용자는 이미 다른 회사의 관리자입니다."));
													}
												}
												else {
													p.sendMessage(format("&f&l[&c&l!&f&l] &r&c해당 사용자는 이미 관리자입니다."));
												}
											}
											else {
												p.sendMessage(format("&f&l[&c&l!&f&l] &r&c해당 개인계좌가 존재하지 않습니다."));
											}
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c당신은 회사의 소유주가 아닙니다!"));
										}
									}
								}
								else if(args[1].equalsIgnoreCase("remove")) {
									if(errorLength(args, 3, s, "/money company remove <사용자명>", true)) {
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
														p.sendMessage(format("&f&l[&c&l!&f&l] &6" + smu.getUserName() + " &c을(를) 회사 관리자에서 박탈했습니다."));
														jsonToFile();
													}
													else {
														p.sendMessage(format("&f&l[&c&l!&f&l] &r&c해당 사용자는 당신 회사의 회사 관리자가 아닙니다."));
													}	
												}
												else {
													p.sendMessage(format("&f&l[&c&l!&f&l] &r&c관리자 목록에서 대표자를 삭제할 수 없습니다."));
												}
											}
											else {
												p.sendMessage(format("&f&l[&c&l!&f&l] &r&c해당 개인계좌가 존재하지 않습니다."));
											}
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c당신은 회사의 소유주가 아닙니다!"));
										}
									}
								}
								else if(args[1].equalsIgnoreCase("send")) {
									if(errorLength(args, 5, s, "/money company send <company|user> <회사명|사용자명> <액수>", true)) {
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
														p.sendMessage(format("&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨\n&f&l[&a&l!&f&l] &r&b"+ mu.getUserName() + " &6개인계좌에 &a" + pmoney + " 원 &6을 송금하였습니다."));
														mu.AddMoney(pmoney);
														mad.RemoveMoney(pmoney);
														p.sendMessage(format("&f&l[&a&l!&f&l] &r&b" + mad.getAddressName() +  " &6회사계좌의 잔액은 &a" + mad.getAddressMoney() + " 원 &6입니다.\n&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨"));
														jsonToFile();
													} catch(NumberFormatException e) {
														p.sendMessage(format("&f&l[&c&l!&f&l] &r&c액수는 정수로 입력하셔야 합니다!"));
													}
													
												}
												else {
													p.sendMessage(format("&f&l[&c&l!&f&l] &r&c해당 개인계좌가 존재하지 않습니다."));
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
														p.sendMessage(format("&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨\n&f&l[&a&l!&f&l] &r&b"+ mad2.getAddressName() + " &6회사계좌에 &a" + pmoney + " 원 &6을 송금하였습니다."));
														mad2.AddMoney(pmoney);
														mad.RemoveMoney(pmoney);
														p.sendMessage(format("&f&l[&a&l!&f&l] &r&6당신 회사계좌의 잔액은 &a" + mad.getAddressMoney() + " 원 &6입니다.\n&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨"));
														jsonToFile();
													} catch(NumberFormatException e) {
														p.sendMessage(format("&f&l[&c&l!&f&l] &r&c액수는 정수로 입력하셔야 합니다!"));
													}
													
												}
												else {
													p.sendMessage(format("&f&l[&c&l!&f&l] &r&c해당 회사계좌가 존재하지 않습니다."));
												}
											}
											else {
												p.sendMessage(format("&f&l[&c&l!&f&l] &r&c알맞은 명령어를 입력하십시오!"));
											}
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c당신의 계좌가 존재하지 않습니다! 재접속 하십시오!"));
										}
									}
								}
								else if(args[1].equalsIgnoreCase("handover")) {
									if(errorLength(args, 3, s, "/money company handover <사용자명>", true)) {
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
													if((p.getInventory().getItemInMainHand().getType() == Material.PAPER && p.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase("확인서")) && (sp.getInventory().getItemInMainHand().getType() == Material.PAPER && sp.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase("확인서"))) {
														p.sendMessage(format("&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨\n&f&l[&c&l!&f&l] &r&6" + mad.getAddressName() + " &c회사의 대표 권한을 &6" + sp.getName() + " &c에게로 양도했습니다.\n&r&f&l[&c&l!&f&l] &r&c&n회사 대표권 양도 시 회사 재산의 5%는 세금으로 납부됩니다.\n&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨"));
														sp.sendMessage(format("&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨\n&f&l[&c&l!&f&l] &r&6" + mad.getAddressName() + " &c회사의 대표 권한이 당신에게 양도되었습니다.\n&r&f&l[&c&l!&f&l] &r&c&n회사 대표권 양도 시 회사 재산의 5%는 세금으로 납부됩니다.\n&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨"));
														p.getInventory().clear(p.getInventory().getHeldItemSlot());
														sp.getInventory().clear(sp.getInventory().getHeldItemSlot());
														int dmoney = (int) Math.round(mad.getAddressMoney() * 0.05);
														mad.RemoveMoney(dmoney);
														mad.ChangeAddressChief(mu);
														jsonToFile();
													}
													else {
														p.sendMessage(format("&f&l[&c&l!&f&l] &r&c양도자와 대상자 모두 손에 인증서를 들고 있어야 합니다!\n&r&f&l[&c&l!&f&l] &r&6/money company certificate &c명령어를 통해 얻을 수 있습니다!"));
													}
												}
												else {
													p.sendMessage(format("&f&l[&c&l!&f&l] &r&c양도 대상자가 현재 서버에 존재해야 합니다!"));
												}
											}
											else {
												p.sendMessage(format("&f&l[&c&l!&f&l] &r&c대표 권한 양도는 회사 계좌 관리자에게만 할 수 있습니다!"));
											}
										}
										else {
											p.sendMessage(format("&f&l[&c&l!&f&l] &r&c당신은 회사의 소유주가 아닙니다!"));
										}
									}
								}
								else if(args[1].equalsIgnoreCase("certificate")) {
									if(errorLength(args, 2, s, "/money company certificate", true)) {
										List<String> itemLore = new ArrayList<String>();
										itemLore.add(format("&c회사 매각 시 필요한 확인서입니다."));
										itemLore.add(format("&c오른손에 들고 사용하십시오."));
										ItemStack stack = new ItemStack(Material.PAPER);
										ItemMeta meta = stack.getItemMeta();
										meta.setDisplayName("확인서");
										meta.setLore(itemLore);
										stack.setItemMeta(meta);
										p.getInventory().setItemInOffHand(stack);
										p.sendMessage(format("&f&l[&a&l!&f&l] &a왼손에 회사 매각 확인서가 지급되었습니다."));
									}
								}
								else {
									p.sendMessage(format("&f&l[&c&l!&f&l] &r&c알맞은 명령어를 입력하십시오!"));
								}
							}
						}
					}
					else {
						s.sendMessage(format("&f&l[&c&l!&f&l] &r&6/money " + args[0] + " &c명령어는 플레이어만 사용이 가능합니다."));
					}
				}
				else {
					if(args[0].equalsIgnoreCase("help")) {
						if(errorLength(args, 1, s, "/money help", true)) {
							s.sendMessage(format("&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨\n&r&f&l[&a&l!&f&l] &r&b/money add <company|user> <회사명|사용자명> <액수>\n   &6▣해당 회사나 사용자에게 돈을 지급합니다.\n&r&f&l[&a&l!&f&l] &r&b/money subtract <company|user> <회사명|사용자명> <액수>\n   &6▣해당 회사나 사용자에서 돈을 차감합니다.\n&r&f&l[&a&l!&f&l] &r&b/money give <company|user> <회사명1|사용자명1> <company|user> <회사명2|사용자명2> <액수>\n   &6▣[1]번 계좌로부터 [2]번 계좌로 돈을 보냅니다.\n&r&f&l[&a&l!&f&l] &r&b/money list <company|user>\n   &6▣서버에 존재하는 모든 개인계좌나 회사계좌를 확인합니다.\n&r&f&l[&a&l!&f&l] &r&b/money all\n   &6▣서버에 존재하는 총 통화량을 확인합니다.\n&r&6&l-----------------------\n&r&f&l[&9&l!&f&l] &r&b/money check\n   &6▣본인 계좌의 잔액 현황을 확인합니다.\n&r&f&l[&9&l!&f&l] &r&b/money send <company|user> <회사명|사용자명> <액수>\n   &6▣본인 계좌에서 해당 회사나 사용자에게 돈을 송금합니다.\n&r&f&l[&9&l!&f&l] &r&b/money company <create|disposal|check|add|remove|send|handover|certificate>\n   &r&f&l[&6&l!&f&l] &r&b/money company create <회사명>\n      &6▣본인 소유의 회사를 생성합니다.\n       &c※본인 소유의 회사는 1개만 생성 가능합니다.\n   &r&f&l[&6&l!&f&l] &r&b/money company disposal\n      &6▣본인 소유의 회사를 정부에 매각합니다. &r&d&l♠\n       &c※본 명령어 사용 시 회사 자본의 10%는 세금으로 납부됩니다.\n   &r&f&l[&6&l!&f&l] &r&b/money company add <사용자명>\n      &6▣해당 사용자를 회사 계좌의 관리인으로 임명합니다.\n       &6※관리인은 /money company check 까지의 권한을 가집니다.\n   &r&f&l[&6&l!&f&l] &r&b/money company remove <사용자명>\n      &6▣해당 사용자를 회사 계좌의 관리인에서 박탈합니다.\n   &r&f&l[&6&l!&f&l] &r&b/money company send <company|user> <회사명|사용자명> <액수>\n      &6▣본인 회사 계좌에서 해당 회사나 사용자에게 돈을 송금합니다.\n   &r&f&l[&6&l!&f&l] &r&b/money company check\n      &6▣본인 소유 회사의 현황을 확인합니다.\n   &r&f&l[&6&l!&f&l] &r&b/money company handover <사용자명>\n      &6▣해당 사용자에게 본인 회사의 대표권을 양도합니다. &r&d&l♠\n       &r&c※해당 명령어 사용 시 회사 자본의 5%가 세금으로 납부됩니다.\n       &c※양도 대상자는 해당 회사의 관리인이어야 합니다.\n   &r&f&l[&6&l!&f&l] &r&b/money company ceftificate\n      &6▣회사 매각에 필요한 인증서를 발급합니다.\n&r&f&l[&7&l!&f&l] &r&b/money help\n   &6▣본 목록을 표시합니다.\n\n&r&f&l[&a&l!&f&l] &r&f서버 콘솔 전용     &r&f&l[&9&l!&f&l] &r&f플레이어 전용\n&r&f&l[&6&l!&f&l] &r&f하위 목록     &r&f&l[&7&l!&f&l] &r&f공용     &r&d&l♠ &r&f매각 인증서 필요\n&r&2▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨▨"));
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
			s.sendMessage(format("&c&l누락된 명령어:\n&r" + String.join(" ", cmd)));
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
			s.sendMessage(format("&c&l잘못된 명령어:\n&r" + String.join(" ", cmd)));
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