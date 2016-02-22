import java.io.Serializable;

public class Message implements Serializable{
	private static final long serialVersionUID = 1L;
	private Node sender;
	private String msgType;
	
	public Node getSender() {
		return sender;
	}
	public void setSender(Node sender) {
		this.sender = sender;
	}
	public String getMsgType() {
		return msgType;
		//	Find / Ack / Nack
		}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
