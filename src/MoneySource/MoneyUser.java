package MoneySource;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MoneyUser {

	private String username;
	private long money;
	private UUID user;
	
	public MoneyUser(Player user) {
		this.money = 0;
		this.user = user.getUniqueId();
		this.username = user.getName();
	}
	
	
//------------------------------<FUNCTION>--------------------------------
	
	public boolean AddMoney(int m) {
		if(money + m > Integer.MAX_VALUE) {
			return false;
		}
		else {
			money += m;
			return true;
		}
	}
	
	public void RemoveMoney(int m) {
		money -= m;
	}
	
	public Player getUser() {
		Player p = Bukkit.getPlayer(user);
		return p;
	}
	
	public String getUserName() {
		return username;
	}
	
	public UUID getUserUUID() {
		return user;
	}
	
	public void changeUserName(String name) {
		this.username = name;
	}
	
	public long getUserMoney() {
		return money;
	}
	
}
