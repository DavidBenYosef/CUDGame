package message;

public class UserInMessage {

	private String type;
	private Integer round;
	private Integer selectedCand;

	public UserInMessage (String type, Integer round,Integer selectedCand)
	{
		this.type = type;
		this.round = round;
		this.selectedCand = selectedCand;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}


	public Integer getSelectedCand() {
		return selectedCand;
	}

	public void setSelectedCand(Integer selectedCand) {
		this.selectedCand = selectedCand;
	}
	public Integer getRound() {
		return round;
	}
	public void setRound(Integer round) {
		this.round = round;
	}

}
