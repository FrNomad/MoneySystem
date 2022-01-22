package MoneySource;

import java.util.ArrayList;
import java.util.List;

public class MoneyAddress {

	private String name;
	private int money;
	private MoneyUser chief;
	private List<MoneyUser> users = new ArrayList<MoneyUser>();
	
	public MoneyAddress(String name, MoneyUser mu) {
		this.money = 0;
		this.name = name;
		this.chief = mu;
		users.add(mu);
	}
	
	
//------------------------------<FUNCTION>--------------------------------
	
	
	public void AddUser(MoneyUser mu) {
		users.add(mu);
	}
	
	public boolean DeleteUser(MoneyUser mu) {
		for(int i = 0; i < users.size(); i++) {
			if(users.get(i) == mu) {
				users.remove(i);
				return true;
			}
		}
		return false;
	}
	
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
	
	public MoneyUser getAddressChief() {
		return chief;
	}
	
	public void ChangeAddressChief(MoneyUser mu) {
		chief = mu;
	}
	
	public int getAddressMoney() {
		return money;
	}
	
	public String getAddressName() {
		return name;
	}
	
	public List<MoneyUser> getAddressPlayer() {
		return users;
	}
}
