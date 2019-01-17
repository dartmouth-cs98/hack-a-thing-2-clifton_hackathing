import java.security.*;
import java.util.ArrayList;

public class Transaction {
	public String transactionID;
	public PublicKey sender;
	public PublicKey recipient;
	public float value;
	public byte[] signature;
	
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

	private static int sequence = 0;
	
	public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.recipient = to;
		this.value = value;
		this.inputs = inputs;
	}
	
	private String calculateHash() {
		sequence++;
		return StringUtil.applySha256(
				StringUtil.getStringFromKey(sender) +
				StringUtil.getStringFromKey(recipient) +
				Float.toString(value) + sequence
				);
	}
	
	public void generateSignature(PrivateKey privateKey) {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value)	;
		signature = StringUtil.applyECDSASig(privateKey,data);		
	}
	
	public boolean verifySignature() {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value)	;
		return StringUtil.verifyECDSASig(sender, data, signature);
	}
	
	public boolean processTransaction() {
		if(!verifySignature()) {
			System.out.println("###Transaction signature failed to verify###");
			return(false);
		}
		
		for(TransactionInput i : inputs) {
			i.unspentTX = Blockchain.unspentTransactions.get(i.transactionOutputID);
		}
		
		if(getInputsValue() < Blockchain.minimumTransaction) {
			System.out.println("Transaction inputs lower than " + Blockchain.minimumTransaction + ".");
			return(false);
		}
		
		float leftOver = getInputsValue() - value;
		transactionID = calculateHash();
		outputs.add(new TransactionOutput( this.recipient, value,transactionID)); 
		outputs.add(new TransactionOutput( this.sender, leftOver,transactionID));
				
		for(TransactionOutput o : outputs) {
			Blockchain.unspentTransactions.put(o.id, o);
		}
		
		for(TransactionInput i : inputs) {
			if(i.unspentTX == null) continue;
			Blockchain.unspentTransactions.remove(i.unspentTX.id);
		}
		
		return true;
	}
	
	public float getInputsValue() {
		float total = 0;
		for(TransactionInput i : inputs) {
			if(i.unspentTX == null) continue; //if Transaction can't be found skip it 
			total += i.unspentTX.value;
		}
		return total;
	}

	public float getOutputsValue() {
		float total = 0;
		for(TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;
	}
}
